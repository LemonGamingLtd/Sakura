package me.samsuik.sakura.entity.merge;

import org.jetbrains.annotations.NotNull;

public interface MergeableEntity {
    @NotNull MergeEntityData getMergeEntityData();

    boolean isSafeToMergeInto(@NotNull MergeableEntity entity, boolean ticksLived);

    default boolean respawnEntity() {
        MergeEntityData mergeData = this.getMergeEntityData();
        int count = mergeData.getCount();
        if (count > 1) {
            mergeData.setCount(0);
            this.respawnEntity(count);
            return true;
        }
        return false;
    }

    void respawnEntity(int count);
}
