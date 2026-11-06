package ltd.lemongaming.citrus.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Loads blocks from removed custom block definitions as a fallback block instead of air.
 */
public final class CustomBlockPersistence {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FALLBACK_PROPERTY = "citrus.customblocks.missing-fallback";
    private static final Set<Identifier> WARNED = ConcurrentHashMap.newKeySet();

    public static final Codec<Block> BLOCK_CODEC = Identifier.CODEC.comapFlatMap(
        CustomBlockPersistence::decode,
        block -> BuiltInRegistries.BLOCK.getKey(block)
    );

    private static volatile @Nullable Block fallback;

    private CustomBlockPersistence() {
    }

    public static @Nullable Block fallbackFor(final Identifier id) {
        if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return null;
        }
        final Block block = fallback();
        if (WARNED.add(id)) {
            LOGGER.warn("Missing custom block {}, loading it as {}", id, BuiltInRegistries.BLOCK.getKey(block));
        }
        return block;
    }

    private static DataResult<Block> decode(final Identifier id) {
        final Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        if (block.isPresent()) {
            return DataResult.success(block.get());
        }
        final Block fallbackBlock = fallbackFor(id);
        return fallbackBlock != null ? DataResult.success(fallbackBlock) : DataResult.error(() -> "Unknown block: " + id);
    }

    private static Block fallback() {
        Block block = fallback;
        if (block == null) {
            final Identifier id = Identifier.tryParse(System.getProperty(FALLBACK_PROPERTY, "minecraft:stone"));
            block = BuiltInRegistries.BLOCK.getOptional(id)
                .filter(candidate -> !(candidate instanceof CitrusCustomBlock))
                .orElse(Blocks.STONE);
            fallback = block;
        }
        return block;
    }
}