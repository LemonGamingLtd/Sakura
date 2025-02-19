package me.samsuik.sakura.player.item;

import me.samsuik.sakura.configuration.GlobalConfiguration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class BlockableSwordItem extends SwordItem {
    private static final Consumable BLOCKING_ANIMATION = Consumable.builder()
        .consumeSeconds(720000)
        .animation(ItemUseAnimation.BLOCK)
        .sound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY))
        .hasConsumeParticles(false)
        .build();

    public BlockableSwordItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties properties) {
        super(material, attackDamage, attackSpeed, properties);
    }

    @Override
    public void modifyComponentsSentToClient(PatchedDataComponentMap components) {
        if (blockWithSwords()) {
            components.set(DataComponents.CONSUMABLE, BLOCKING_ANIMATION);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (blockWithSwords()) {
            ItemStack itemInHand = player.getItemInHand(hand);
            return BLOCKING_ANIMATION.startConsuming(player, itemInHand, hand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return blockWithSwords() ? ItemUseAnimation.BLOCK : super.getUseAnimation(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return blockWithSwords() ? 720000 : super.getUseDuration(stack, entity);
    }

    private static boolean blockWithSwords() {
        GlobalConfiguration config = GlobalConfiguration.get();
        return config != null && config.players.combat.blockWithSwords;
    }
}
