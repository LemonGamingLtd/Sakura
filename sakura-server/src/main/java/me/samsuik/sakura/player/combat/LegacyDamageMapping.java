package me.samsuik.sakura.player.combat;

import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.OptionalDouble;

public final class LegacyDamageMapping {
    private static final Reference2DoubleMap<Item> LEGACY_ITEM_DAMAGE_MAP = new Reference2DoubleOpenHashMap<>();

    public static OptionalDouble itemAttackDamage(Item item) {
        double result = LEGACY_ITEM_DAMAGE_MAP.getDouble(item);
        return result == Double.MIN_VALUE ? OptionalDouble.empty() : OptionalDouble.of(result);
    }

    private static double adjustDamageForItem(Item item, double attackDamage) {
        return switch (item) {
            case SwordItem i -> 1.0;
            case PickaxeItem i -> 1.0;
            case ShovelItem i -> -0.5;
            case HoeItem i -> -attackDamage;
            case null, default -> 0.0;
        };
    }

    static {
        LEGACY_ITEM_DAMAGE_MAP.defaultReturnValue(Double.MIN_VALUE);

        // tool material is no longer exposed
        LEGACY_ITEM_DAMAGE_MAP.put(Items.WOODEN_AXE, 3.0);
        LEGACY_ITEM_DAMAGE_MAP.put(Items.GOLDEN_AXE, 3.0);
        LEGACY_ITEM_DAMAGE_MAP.put(Items.STONE_AXE, 4.0);
        LEGACY_ITEM_DAMAGE_MAP.put(Items.IRON_AXE, 5.0);
        LEGACY_ITEM_DAMAGE_MAP.put(Items.DIAMOND_AXE, 6.0);
        LEGACY_ITEM_DAMAGE_MAP.put(Items.NETHERITE_AXE, 7.0);

        for (Item item : BuiltInRegistries.ITEM) {
            ItemAttributeModifiers modifiers = item.components().get(DataComponents.ATTRIBUTE_MODIFIERS);

            if (modifiers == null || LEGACY_ITEM_DAMAGE_MAP.containsKey(item)) {
                continue;
            }

            assert item instanceof AxeItem : "missing axe mapping";

            double attackDamage = modifiers.modifiers().stream()
                .filter(e -> e.attribute().is(Attributes.ATTACK_DAMAGE))
                .mapToDouble(e -> e.modifier().amount())
                .sum();

            if (attackDamage > 0.0) {
                double adjustment = adjustDamageForItem(item, attackDamage);
                LEGACY_ITEM_DAMAGE_MAP.put(item, attackDamage + adjustment);
            }
        }
    }

    private LegacyDamageMapping() {}
}
