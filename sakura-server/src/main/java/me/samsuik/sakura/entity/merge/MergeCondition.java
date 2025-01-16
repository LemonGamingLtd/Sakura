package me.samsuik.sakura.entity.merge;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface MergeCondition {
    default MergeCondition and(@NotNull MergeCondition condition) {
        return (e,c,t) -> this.accept(e,c,t) && condition.accept(e,c,t);
    }

    default MergeCondition or(@NotNull MergeCondition condition) {
        return (e,c,t) -> this.accept(e,c,t) || condition.accept(e,c,t);
    }

    boolean accept(@NotNull Entity entity, int attempts, long sinceCreation);
}
