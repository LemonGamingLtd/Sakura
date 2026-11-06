package ltd.lemongaming.citrus.block;

import ca.spottedleaf.moonrise.common.util.TickThread;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class CustomBlockMining {

    private static final float INSTAMINE_MARGIN = 1.0E-4F;
    private static final double FACTOR_TOLERANCE = 1.0E-5;

    private final ServerPlayer player;
    private double factor = 1.0;
    private @Nullable BlockState lastCustom;

    public CustomBlockMining(final ServerPlayer player) {
        this.player = player;
    }

    public void tick(final @Nullable BlockPos destroying) {
        this.update(destroying != null ? destroying : this.targetedBlock());
    }

    public void update(final @Nullable BlockPos pos) {
        final double factor = this.player.gameMode.isSurvival() ? this.factor(pos) : 1.0;
        if (factor == 1.0 ? this.factor == 1.0 : Math.abs(factor - this.factor) <= this.factor * FACTOR_TOLERANCE) {
            return;
        }
        this.factor = factor;
        this.player.connection.send(new ClientboundUpdateAttributesPacket(this.player.getId(), List.of(this.clientAttribute())));
    }

    /**
     * Replaces the real block break speed in attributes about to be sent to the player.
     */
    public void injectClientAttribute(final Collection<AttributeInstance> attributes) {
        if (this.factor != 1.0 && attributes.removeIf(instance -> instance.getAttribute() == Attributes.BLOCK_BREAK_SPEED)) {
            attributes.add(this.clientAttribute());
        }
    }

    public double factor() {
        return this.factor;
    }

    private @Nullable BlockPos targetedBlock() {
        if (CitrusCustomBlocks.isEmpty() || !this.player.gameMode.isSurvival()) {
            return null;
        }
        final double range = this.player.blockInteractionRange();
        // On Folia the ray may only read blocks owned by this region
        if (!TickThread.isTickThreadFor(this.player.level(), this.player.blockPosition(), Mth.ceil(range) + 1)) {
            return null;
        }
        final HitResult hit = this.player.pick(range, 1.0F, false);
        return hit.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) hit).getBlockPos() : null;
    }

    private double factor(final @Nullable BlockPos pos) {
        final BlockState state = pos == null || !TickThread.isTickThreadFor(this.player.level(), pos) ? null : this.player.level().getBlockStateIfLoaded(pos);
        final BlockPos at = pos != null ? pos : this.player.blockPosition();
        if (state != null && state.getBlock() instanceof CitrusCustomBlock) {
            this.lastCustom = state;
        } else if (this.lastCustom == null && (this.lastCustom = this.instaminedByAll(at)) == null) {
            return 1.0;
        }
        final double factor = this.customFactor(this.lastCustom, at);
        return state == null || state == this.lastCustom || this.breaksSame(state, at, factor) ? factor : 1.0;
    }

    private @Nullable BlockState instaminedByAll(final BlockPos pos) {
        BlockState slowest = null;
        float slowestProgress = Float.MAX_VALUE;
        for (final CitrusCustomBlockType type : CitrusCustomBlocks.types()) {
            final BlockState state = type.block().defaultBlockState();
            final float progress = state.getDestroyProgress(this.player, this.player.level(), pos);
            if (progress < 1.0F) {
                return null;
            }
            if (progress < slowestProgress) {
                slowest = state;
                slowestProgress = progress;
            }
        }
        if (slowest == null) {
            return null;
        }
        final double factor = this.customFactor(slowest, pos);
        for (final CitrusCustomBlockType type : CitrusCustomBlocks.types()) {
            if (type.visualState().getDestroyProgress(this.player, this.player.level(), pos) * factor < 1.0F + INSTAMINE_MARGIN) {
                return null;
            }
        }
        return slowest;
    }

    private double customFactor(final BlockState state, final BlockPos pos) {
        final float visualProgress = ((CitrusCustomBlock) state.getBlock()).type().visualState().getDestroyProgress(this.player, this.player.level(), pos);
        if (visualProgress <= 0.0F) {
            return 1.0;
        }
        final float progress = state.getDestroyProgress(this.player, this.player.level(), pos);
        final float target = progress >= 1.0F ? Math.max(progress, 1.0F + INSTAMINE_MARGIN) : Math.min(progress, 1.0F - INSTAMINE_MARGIN);
        return (double) target / visualProgress;
    }

    /**
     * Whether a vanilla block breaks the same for the client with the given factor: unbreakable, or instamined either way.
     */
    private boolean breaksSame(final BlockState state, final BlockPos pos, final double factor) {
        final float progress = state.getDestroyProgress(this.player, this.player.level(), pos);
        return progress <= 0.0F || progress >= 1.0F && progress * factor >= 1.0F + INSTAMINE_MARGIN;
    }

    private AttributeInstance clientAttribute() {
        final AttributeInstance real = this.player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (this.factor == 1.0) {
            return real;
        }
        final AttributeInstance scaled = new AttributeInstance(Attributes.BLOCK_BREAK_SPEED, ignored -> {
        });
        scaled.setBaseValue(Attributes.BLOCK_BREAK_SPEED.value().sanitizeValue(real.getValue() * this.factor));
        return scaled;
    }
}