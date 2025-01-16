package me.samsuik.sakura.player.visibility;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.samsuik.sakura.player.gui.ItemStackUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public final class VisibilityGuiItems {
    static final Reference2ObjectMap<VisibilityType, ImmutableList<ItemStack>> GUI_ITEMS = new Reference2ObjectOpenHashMap<>();
    static final Reference2ObjectMap<VisibilityState, ItemStack> TOGGLE_BUTTON_ITEMS = new Reference2ObjectOpenHashMap<>();

    static {
        Reference2ObjectMap<VisibilityType, ItemStack> items = new Reference2ObjectOpenHashMap<>();

        items.put(VisibilityTypes.TNT, ItemStackUtil.itemWithName(Material.TNT, Component.text("Tnt", NamedTextColor.RED)));
        items.put(VisibilityTypes.SAND, ItemStackUtil.itemWithName(Material.SAND, Component.text("Sand", NamedTextColor.YELLOW)));
        items.put(VisibilityTypes.EXPLOSIONS, ItemStackUtil.itemWithName(Material.COBWEB, Component.text("Explosions", NamedTextColor.WHITE)));
        items.put(VisibilityTypes.SPAWNERS, ItemStackUtil.itemWithName(Material.SPAWNER, Component.text("Spawners", NamedTextColor.DARK_GRAY)));
        items.put(VisibilityTypes.PISTONS, ItemStackUtil.itemWithName(Material.PISTON, Component.text("Pistons", NamedTextColor.GOLD)));

        for (VisibilityType type : VisibilityTypes.types()) {
            ItemStack item = items.get(type);

            ImmutableList<ItemStack> stateItems = type.states().stream()
                .map(s -> createItemForState(item, s))
                .collect(ImmutableList.toImmutableList());

            GUI_ITEMS.put(type, stateItems);
        }

        TOGGLE_BUTTON_ITEMS.put(VisibilityState.ON, ItemStackUtil.itemWithName(Material.GREEN_STAINED_GLASS_PANE, Component.text("Enabled", NamedTextColor.GREEN)));
        TOGGLE_BUTTON_ITEMS.put(VisibilityState.MODIFIED, ItemStackUtil.itemWithName(Material.MAGENTA_STAINED_GLASS_PANE, Component.text("Modified", NamedTextColor.LIGHT_PURPLE)));
        TOGGLE_BUTTON_ITEMS.put(VisibilityState.OFF, ItemStackUtil.itemWithName(Material.RED_STAINED_GLASS_PANE, Component.text("Disabled", NamedTextColor.RED)));
    }

    private static String lowercaseThenCapitalise(String name) {
        String lowercaseName = name.toLowerCase(Locale.ENGLISH);
        return StringUtils.capitalize(lowercaseName);
    }

    private static ItemStack createItemForState(ItemStack in, VisibilityState state) {
        String capitalisedName = lowercaseThenCapitalise(state.name());
        Component component = Component.text(" | " + capitalisedName, NamedTextColor.GRAY);
        ItemStack itemCopy = in.clone();
        itemCopy.editMeta(m -> m.itemName(m.itemName().append(component)));
        return itemCopy;
    }
}
