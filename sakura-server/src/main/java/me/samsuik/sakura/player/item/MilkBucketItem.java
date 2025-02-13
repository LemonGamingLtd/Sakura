package me.samsuik.sakura.player.item;

import me.samsuik.sakura.configuration.GlobalConfiguration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MilkBucketItem extends Item {
    public MilkBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        int maxStackSize = DataComponentHelper.bucketMaxStackSize();
        if (DataComponentHelper.stackableMilkBuckets() && maxStackSize > 0 && maxStackSize < 100) {
            stack.set(DataComponents.MAX_STACK_SIZE, maxStackSize);
        }
    }
}
