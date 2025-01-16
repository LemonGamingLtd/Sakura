package me.samsuik.sakura.entity.merge;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import me.samsuik.sakura.configuration.WorldConfiguration.Cannons.Mechanics.TNTSpread;
import me.samsuik.sakura.utils.TickExpiry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.jetbrains.annotations.NotNull;

public final class TrackedMergeHistory {
    private final Long2ObjectMap<PositionHistory> historyMap = new Long2ObjectOpenHashMap<>();

    public boolean hasPreviousMerged(@NotNull Entity entity, @NotNull Entity into) {
        PositionHistory positions = this.getHistory(into);
        return positions != null && positions.has(entity.getPackedOriginPosition());
    }

    public void trackHistory(@NotNull Entity entity, @NotNull MergeEntityData mergeEntityData) {
        long originPosition = entity.getPackedOriginPosition();
        PositionHistory positions = this.historyMap.computeIfAbsent(originPosition, p -> new PositionHistory());
        LongOpenHashSet originPositions = mergeEntityData.getOriginPositions();
        boolean createHistory = positions.hasTimePassed(160);
        if (createHistory && (entity instanceof FallingBlockEntity || entity.level().sakuraConfig().cannons.mechanics.tntSpread == TNTSpread.ALL)) {
            originPositions.forEach(pos -> this.historyMap.put(pos, positions));
        }
        positions.trackPositions(originPositions, !createHistory);
    }

    public boolean hasMetCondition(@NotNull Entity entity, MergeCondition condition) {
        PositionHistory positions = this.getHistory(entity);
        return positions != null && positions.hasMetConditions(entity, condition);
    }

    private PositionHistory getHistory(Entity entity) {
        long originPosition = entity.getPackedOriginPosition();
        return this.historyMap.get(originPosition);
    }

    public void expire(int tick) {
        this.historyMap.values().removeIf(p -> p.expiry().isExpired(tick));
    }

    private static final class PositionHistory {
        private final LongOpenHashSet positions = new LongOpenHashSet();
        private final TickExpiry expiry = new TickExpiry(MinecraftServer.currentTick, 200);
        private final long created = MinecraftServer.currentTick;
        private int cycles = 0;

        public TickExpiry expiry() {
            return this.expiry;
        }

        public boolean has(long position) {
            this.expiry.refresh(MinecraftServer.currentTick);
            return this.positions.contains(position);
        }

        public void trackPositions(LongOpenHashSet positions, boolean retain) {
            if (retain) {
                this.positions.retainAll(positions);
            } else {
                this.positions.addAll(positions);
            }
            this.cycles++;
        }

        public boolean hasMetConditions(@NotNull Entity entity, @NotNull MergeCondition condition) {
            this.expiry.refresh(MinecraftServer.currentTick);
            return condition.accept(entity, this.cycles, this.timeSinceCreation());
        }

        public boolean hasTimePassed(int ticks) {
            return this.timeSinceCreation() > ticks;
        }

        private long timeSinceCreation() {
            return MinecraftServer.currentTick - this.created;
        }
    }
}
