package ltd.lemongaming.citrus.block;

import net.kyori.adventure.key.Key;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CustomBlockType {

    @NotNull
    Key key();

    @NotNull
    CustomBlockDefinition definition();

    @NotNull
    BlockData createBlockData();

    void place(@NotNull Block block);

    boolean matches(@NotNull Block block);

    @Nullable
    CustomBlockDropProvider dropProvider();

    /**
     * When unset, the loot table {@code <namespace>:blocks/<path>} is used.
     */
    void dropProvider(@Nullable CustomBlockDropProvider dropProvider);

    @Nullable
    ItemStack pickItem();

    /**
     * Item selected or given by pick block. When unset, pick block does nothing.
     */
    void pickItem(@Nullable ItemStack pickItem);
}