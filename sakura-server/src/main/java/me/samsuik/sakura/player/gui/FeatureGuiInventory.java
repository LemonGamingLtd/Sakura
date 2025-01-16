package me.samsuik.sakura.player.gui;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import me.samsuik.sakura.player.gui.components.GuiComponent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.Optional;

@NullMarked
public final class FeatureGuiInventory implements InventoryHolder {
    private final Inventory inventory;
    private final FeatureGui gui;
    private final Multimap<NamespacedKey, GuiComponent> componentsUnderKey = HashMultimap.create();
    private final Object2ObjectMap<GuiComponent, NamespacedKey> componentKeys = new Object2ObjectLinkedOpenHashMap<>();

    public FeatureGuiInventory(FeatureGui gui, int size, Component component) {
        this.inventory = Bukkit.createInventory(this, size, component);
        this.gui = gui;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public FeatureGui getGui() {
        return this.gui;
    }

    public ImmutableList<GuiComponent> getComponents() {
        return ImmutableList.copyOf(this.componentKeys.keySet());
    }

    public ImmutableList<GuiComponent> findComponents(NamespacedKey key) {
        return ImmutableList.copyOf(this.componentsUnderKey.get(key));
    }

    public Optional<GuiComponent> findFirst(NamespacedKey key) {
        Collection<GuiComponent> components = this.componentsUnderKey.get(key);
        return components.stream().findFirst();
    }

    public void removeComponents(NamespacedKey key) {
        Collection<GuiComponent> removed = this.componentsUnderKey.removeAll(key);
        for (GuiComponent component : removed) {
            this.componentKeys.remove(component);
        }
    }

    public void addComponent(GuiComponent component, NamespacedKey key) {
        Preconditions.checkArgument(!this.componentKeys.containsKey(component), "component has already been added");
        this.componentKeys.put(component, key);
        this.componentsUnderKey.put(key, component);
        this.inventoryUpdate(component);
    }

    public void removeComponent(GuiComponent component) {
        NamespacedKey key = this.componentKeys.remove(component);
        this.componentsUnderKey.remove(key, component);
    }

    public void replaceComponent(GuiComponent component, GuiComponent replacement) {
        NamespacedKey key = this.componentKeys.remove(component);
        Preconditions.checkNotNull(key, "component does not exist");
        this.componentKeys.put(replacement, key);
        this.componentsUnderKey.remove(key, component);
        this.componentsUnderKey.put(key, replacement);
        this.inventoryUpdate(replacement);
    }

    public void removeAllComponents() {
        this.componentKeys.clear();
        this.componentsUnderKey.clear();
    }

    private void inventoryUpdate(GuiComponent component) {
        component.creation(this.inventory);
    }
}
