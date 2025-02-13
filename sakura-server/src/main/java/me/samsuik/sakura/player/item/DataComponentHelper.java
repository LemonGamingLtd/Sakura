package me.samsuik.sakura.player.item;

import me.samsuik.sakura.configuration.GlobalConfiguration;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;

public final class DataComponentHelper {
    public static int bucketMaxStackSize() {
        GlobalConfiguration config = GlobalConfiguration.get();
        return config == null || !config.players.bucketStackSize.isDefined() ? -1 : config.players.bucketStackSize.intValue();
    }

    public static boolean stackableMilkBuckets() {
        GlobalConfiguration config = GlobalConfiguration.get();
        return config != null && config.players.stackableMilkBuckets;
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
