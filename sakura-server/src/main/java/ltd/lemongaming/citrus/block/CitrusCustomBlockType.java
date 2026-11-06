package ltd.lemongaming.citrus.block;

import net.kyori.adventure.key.Key;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CitrusCustomBlockType implements CustomBlockType {

    private final CustomBlockDefinition definition;
    private CitrusCustomBlock block;
    private BlockState visualState;
    private volatile CustomBlockDropProvider dropProvider;
    private volatile ItemStack pickItem;

    CitrusCustomBlockType(final CustomBlockDefinition definition) {
        this.definition = definition;
    }

    @Override
    public @NotNull Key key() {
        return this.definition.key();
    }

    @Override
    public @NotNull CustomBlockDefinition definition() {
        return this.definition;
    }

    @Override
    public @NotNull BlockData createBlockData() {
        return CraftBlockData.createData(this.block.defaultBlockState());
    }

    @Override
    public void place(final @NotNull Block target) {
        target.setBlockData(this.createBlockData());
    }

    @Override
    public boolean matches(final @NotNull Block target) {
        return ((CraftBlock) target).getBlockState().getBlock() == this.block;
    }

    @Override
    public @Nullable CustomBlockDropProvider dropProvider() {
        return this.dropProvider;
    }

    @Override
    public void dropProvider(final @Nullable CustomBlockDropProvider dropProvider) {
        this.dropProvider = dropProvider;
    }

    @Override
    public @Nullable ItemStack pickItem() {
        final ItemStack item = this.pickItem;
        return item == null ? null : item.clone();
    }

    @Override
    public void pickItem(final @Nullable ItemStack pickItem) {
        this.pickItem = pickItem == null || pickItem.isEmpty() ? null : pickItem.asOne();
    }

    public CitrusCustomBlock block() {
        return this.block;
    }

    void block(final CitrusCustomBlock block) {
        this.block = block;
    }

    public BlockState visualState() {
        return this.visualState;
    }

    void visualState(final BlockState visualState) {
        this.visualState = visualState;
    }
}