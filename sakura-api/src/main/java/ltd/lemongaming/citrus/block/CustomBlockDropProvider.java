package ltd.lemongaming.citrus.block;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface CustomBlockDropProvider {

    @NotNull
    List<ItemStack> getDrops(@NotNull CustomBlockType type, @NotNull Location location, @Nullable Entity breaker, @NotNull ItemStack tool);
}