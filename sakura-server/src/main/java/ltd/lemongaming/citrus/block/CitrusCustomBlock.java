package ltd.lemongaming.citrus.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;

public final class CitrusCustomBlock extends Block {

    private final CitrusCustomBlockType type;

    CitrusCustomBlock(final Properties properties, final CitrusCustomBlockType type) {
        super(properties);
        this.type = type;
    }

    public CitrusCustomBlockType type() {
        return this.type;
    }

    public Block visualBlock() {
        return this.type.visualState().getBlock();
    }

    public Material fallbackMaterial() {
        return this.type.definition().fallbackMaterial();
    }

    @Override
    protected ItemStack getCloneItemStack(final LevelReader level, final BlockPos pos, final BlockState state, final boolean includeData) {
        final org.bukkit.inventory.ItemStack item = this.type.pickItem();
        return item == null ? ItemStack.EMPTY : CraftItemStack.asNMSCopy(item);
    }

    @Override
    protected List<ItemStack> getDrops(final BlockState state, final LootParams.Builder params) {
        final CustomBlockDropProvider provider = this.type.dropProvider();
        if (provider == null) {
            return super.getDrops(state, params);
        }

        final Entity breaker = params.getOptionalParameter(LootContextParams.THIS_ENTITY);
        final ItemInstance tool = params.getOptionalParameter(LootContextParams.TOOL);
        final List<org.bukkit.inventory.ItemStack> drops = provider.getDrops(
            this.type,
            CraftLocation.toBukkit(params.getParameter(LootContextParams.ORIGIN), params.getLevel()),
            breaker == null ? null : breaker.getBukkitEntity(),
            CraftItemStack.asBukkitCopy(tool instanceof ItemStack stack ? stack : ItemStack.EMPTY)
        );

        final List<ItemStack> result = new ArrayList<>(drops.size());
        for (final org.bukkit.inventory.ItemStack drop : drops) {
            if (drop != null && !drop.isEmpty()) {
                result.add(CraftItemStack.asNMSCopy(drop));
            }
        }
        return result;
    }
}