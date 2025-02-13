package me.samsuik.sakura.player.item;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;

public final class DataComponentHelper {
    public static int bucketMaxStackSize() {
        me.samsuik.sakura.configuration.GlobalConfiguration config = me.samsuik.sakura.configuration.GlobalConfiguration.get();
        return config == null || !config.players.bucketStackSize.isDefined() ? -1 : config.players.bucketStackSize.intValue();
    }

    public static DataComponentMap copyComponentsAndModifyMaxStackSize(DataComponentMap componentMap, int maxItemSize) {
        if (maxItemSize > 0 && maxItemSize <= 99) {
            return DataComponentMap.builder()
                .addAll(componentMap)
                .set(DataComponents.MAX_STACK_SIZE, maxItemSize)
                .build();
        }
        return componentMap;
    }
}
