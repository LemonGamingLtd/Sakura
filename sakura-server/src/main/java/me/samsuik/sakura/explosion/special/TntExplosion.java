package me.samsuik.sakura.explosion.special;

import ca.spottedleaf.moonrise.common.list.IteratorSafeOrderedReferenceSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.samsuik.sakura.entity.EntityState;
import me.samsuik.sakura.entity.merge.MergeLevel;
import me.samsuik.sakura.entity.merge.MergeableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.util.CraftVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class TntExplosion extends SpecialisedExplosion<PrimedTnt> {
    private static final int ALL_DIRECTIONS = 0b111;
    private static final int FOUND_ALL_BLOCKS = ALL_DIRECTIONS + 12;

    private final Vec3 originalPosition;
    private final List<Vec3> explosions = new ObjectArrayList<>();
    private AABB bounds;
    private int wrapped = 0;
    private boolean moved = false;

    public TntExplosion(ServerLevel level, PrimedTnt tnt, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator behavior, Vec3 center, float power, boolean createFire, BlockInteraction destructionType, Consumer<SpecialisedExplosion<PrimedTnt>> applyEffects) {
        super(level, tnt, damageSource, behavior, center, power, createFire, destructionType, applyEffects);
        this.originalPosition = center;
        this.bounds = new AABB(center, center);
    }

    // Sakura start - physics version api
    @Override
    protected double getExplosionOffset() {
        return this.physics.before(1_10_0) ? (double) 0.49f : super.getExplosionOffset();
    }
    // Sakura end - physics version api

    @Override
    protected int getExplosionCount() {
        if (this.cause.getMergeEntityData().getMergeLevel() == MergeLevel.NONE) {
            this.mergeEntitiesBeforeExplosion();
        }
        return this.cause.getMergeEntityData().getCount();
    }

    @Override
    protected void startExplosion() {
        for (int i = this.getExplosionCount() - 1; i >= 0; --i) {
            Vec3 previousMomentum = this.cause.entityState().momentum();
            boolean lastCycle = i == 0;
            List<BlockPos> toBlow = this.midExplosion(previousMomentum, lastCycle); // search for blocks and impact entities
            boolean destroyedBlocks = this.finalizeExplosionAndParticles(toBlow);

            if (!lastCycle) {
                EntityState entityState = this.nextSourceVelocity();
                this.postExplosion(toBlow, destroyedBlocks); // update wrapped, clear recent block cache
                this.updateExplosionPosition(entityState, destroyedBlocks);
            }
        }
    }

    private List<BlockPos> midExplosion(Vec3 previousMomentum, boolean lastCycle) {
        final List<BlockPos> explodedPositions;
        if (this.wrapped < FOUND_ALL_BLOCKS) {
            explodedPositions = this.calculateExplodedPositions();
        } else {
            explodedPositions = List.of();
        }

        if (this.wrapped < ALL_DIRECTIONS) {
            Vec3 momentum = this.cause.entityState().momentum();
            for (Direction.Axis axis : Direction.Axis.VALUES) {
                double current  = momentum.get(axis);
                double previous = previousMomentum.get(axis);
                if (current == previous || current * previous < 0) {
                    this.wrapped |= 1 << axis.ordinal();
                }
            }
        }

        this.bounds = this.bounds.expand(this.center);
        this.explosions.add(this.center);

        if (lastCycle || this.requiresImpactEntities(explodedPositions, this.center)) {
            this.locateAndImpactEntitiesInBounds();
            this.bounds = new AABB(this.center, this.center);
            this.explosions.clear();
        }

        return explodedPositions;
    }

    @Override
    protected void postExplosion(List<BlockPos> foundBlocks, boolean destroyedBlocks) {
        super.postExplosion(foundBlocks, destroyedBlocks);
        // Update wrapped, this is for tracking swinging and if blocks are found
        if (this.wrapped >= ALL_DIRECTIONS) {
            if (!destroyedBlocks && this.level().sakuraConfig().cannons.explosion.avoidRedundantBlockSearches) {
                this.wrapped++;
            } else {
                this.wrapped = ALL_DIRECTIONS;
            }
        }
    }

    private Vector getCauseOrigin() {
        Vector origin = this.cause.getOriginVector();
        return origin == null ? CraftVector.toBukkit(this.center) : origin;
    }

    private EntityState nextSourceVelocity() {
        Vector origin = this.getCauseOrigin(); // valid position to use while creating a temporary entity
        PrimedTnt tnt = new PrimedTnt(this.level(), origin.getX(), origin.getY(), origin.getZ(), null);
        this.cause.entityState().apply(tnt);
        this.impactCannonEntity(tnt, this.center, 1, this.radius() * 2.0f);
        return EntityState.of(tnt);
    }

    private void updateExplosionPosition(EntityState entityState, boolean destroyedBlocks) {
        // Before setting entity state, otherwise we might cause issues.
        final boolean hasMoved;
        if (this.moved) {
            hasMoved = true;
        } else if (this.center.equals(this.cause.position())) {
            hasMoved = false;
        } else {
            double newMomentum = entityState.momentum().lengthSqr();
            double oldMomentum = this.cause.entityState().momentum().lengthSqr();
            hasMoved = oldMomentum <= Math.pow(this.radius() * 2.0 + 1.0, 2.0) || newMomentum <= oldMomentum;
        }

        // Keep track of entity state
        entityState.apply(this.cause);
        this.cause.storeEntityState();

        // Ticking is always required after destroying a block.
        if (destroyedBlocks || hasMoved) {
            this.cause.setFuse(100);
            this.cause.tick();
            this.moved |= !this.center.equals(this.originalPosition);
            this.recalculateExplosionPosition();
        }
    }

    private void mergeEntitiesBeforeExplosion() {
        IteratorSafeOrderedReferenceSet<Entity> entities = this.level().entityTickList.entities;
        int index = entities.indexOf(this.cause);

        entities.createRawIterator();
        // iterate over the entityTickList to find entities that are exploding in the same position.
        while ((index = entities.advanceRawIterator(index)) != -1) {
            Entity foundEntity = entities.rawGet(index);
            if (!(foundEntity instanceof MergeableEntity mergeEntity) || foundEntity.isRemoved() || !foundEntity.compareState(this.cause) || !mergeEntity.isSafeToMergeInto(this.cause, true))
                break;
            this.level().mergeHandler.mergeEntity(mergeEntity, this.cause);
        }
        entities.finishRawIterator();
    }

    private void locateAndImpactEntitiesInBounds() {
        double radius = this.radius() * 2.0f;
        AABB bb = this.bounds;

        Vec3 center = bb.getCenter();
        double change = Math.max(bb.getXsize(), Math.max(bb.getYsize(), bb.getZsize()));
        double maxDistanceSqr = Math.pow(radius + change, 2.0);
        boolean moved = (change != 0.0);

        this.forEachEntitySliceInBounds(bb.inflate(radius), entities -> {
            if (moved) {
                this.impactEntitiesSwinging(entities, center, radius, maxDistanceSqr);
            } else {
                this.impactEntitiesFromPosition(entities, this.explosions.getFirst(), this.explosions.size(), radius);
            }
        });
    }

    private void impactEntitiesSwinging(Entity[] entities, Vec3 center, double radius, double maxDistanceSqr) {
        for (int i = 0; i < entities.length; ++i) {
            Entity entity = entities[i];
            if (entity == null) break;

            if (entity != this.source && !entity.ignoreExplosion(this) && entity.distanceToSqr(center.x, center.y, center.z) <= maxDistanceSqr) {
                this.impactEntity(entity, radius);
            }

            if (entities[i] != entity) {
                i--;
            }
        }
    }

    private void impactEntity(Entity entity, double radius) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < this.explosions.size(); i++) {
            this.impactEntity(entity, this.explosions.get(i), 1, radius);
        }
    }
}
