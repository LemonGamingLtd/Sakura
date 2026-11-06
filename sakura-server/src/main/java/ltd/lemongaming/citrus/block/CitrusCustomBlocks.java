package ltd.lemongaming.citrus.block;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.key.Key;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class CitrusCustomBlocks implements CustomBlocks.Service {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CitrusCustomBlocks INSTANCE = new CitrusCustomBlocks();

    private Map<Key, CitrusCustomBlockType> types = new LinkedHashMap<>();
    private final Set<Identifier> collapsed = new LinkedHashSet<>();
    private boolean registered;

    private CitrusCustomBlocks() {
    }

    public static void install() {
        CustomBlocks.service(INSTANCE);
    }

    public static void registerBlocks() {
        INSTANCE.registered = true;
        INSTANCE.types = Collections.unmodifiableMap(INSTANCE.types);
        for (final CitrusCustomBlockType type : INSTANCE.types.values()) {
            final CustomBlockDefinition definition = type.definition();
            final ResourceKey<Block> id = ResourceKey.create(Registries.BLOCK, toIdentifier(definition.key()));
            final Block soundsLike = vanillaBlock(definition.soundsLike());
            vanillaBlock(definition.fallbackMaterial());
            final int lightLevel = definition.lightLevel();

            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .setId(id)
                .strength(definition.hardness(), definition.blastResistance())
                .sound(soundsLike.defaultBlockState().getSoundType())
                .mapColor(soundsLike.defaultMapColor())
                .lightLevel(state -> lightLevel);
            if (definition.requiresCorrectTool()) {
                properties = properties.requiresCorrectToolForDrops();
            }
            type.block(Registry.register(BuiltInRegistries.BLOCK, id, new CitrusCustomBlock(properties, type)));
        }
    }

    public static void bake() {
        final Set<Block> collapsedBlocks = Collections.newSetFromMap(new IdentityHashMap<>());
        for (final Identifier id : INSTANCE.collapsed) {
            final Block block = BuiltInRegistries.BLOCK.getOptional(id)
                .orElseThrow(() -> new IllegalStateException("Unknown collapsed block " + id));
            collapsedBlocks.add(block);
        }

        final Set<BlockState> usedVisuals = Collections.newSetFromMap(new IdentityHashMap<>());
        for (final CitrusCustomBlockType type : INSTANCE.types.values()) {
            final BlockState visual = parseVisual(type.definition());
            if (!collapsedBlocks.contains(visual.getBlock())) {
                throw new IllegalStateException("Visual state of " + type.key() + " belongs to a block that is not collapsed: " + type.definition().visualState());
            }
            if (visual == visual.getBlock().defaultBlockState()) {
                throw new IllegalStateException("Visual state of " + type.key() + " is the default state of its block, which vanilla blocks are sent as");
            }
            if (!usedVisuals.add(visual)) {
                throw new IllegalStateException("Visual state of " + type.key() + " is already used: " + type.definition().visualState());
            }
            type.visualState(visual);
        }

        ClientBlockStates.bake(collapsedBlocks, INSTANCE.types.values());
        if (!INSTANCE.types.isEmpty()) {
            LOGGER.info("Registered {} custom blocks ({} vanilla block states)", INSTANCE.types.size(), ClientBlockStates.clientStateCount());
        }
    }

    static boolean isEmpty() {
        return INSTANCE.types.isEmpty();
    }

    static Collection<CitrusCustomBlockType> types() {
        return INSTANCE.types.values();
    }

    @Override
    public @NotNull CustomBlockType register(final @NotNull CustomBlockDefinition definition) {
        this.checkOpen();
        if (this.types.containsKey(definition.key())) {
            throw new IllegalArgumentException("Custom block already registered: " + definition.key());
        }
        final CitrusCustomBlockType type = new CitrusCustomBlockType(definition);
        this.types.put(definition.key(), type);
        return type;
    }

    @Override
    public void collapse(final @NotNull String blockKey) {
        this.checkOpen();
        final Identifier id = Identifier.parse(blockKey);
        if (!id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            throw new IllegalArgumentException("Only vanilla blocks can be collapsed: " + blockKey);
        }
        this.collapsed.add(id);
    }

    @Override
    public @Nullable CustomBlockType get(final @NotNull Key key) {
        return this.types.get(key);
    }

    @Override
    public @Nullable CustomBlockType get(final org.bukkit.block.@NotNull Block block) {
        return typeOf(((CraftBlock) block).getBlockState());
    }

    @Override
    public @Nullable CustomBlockType get(final @NotNull BlockData blockData) {
        return typeOf(((CraftBlockData) blockData).getState());
    }

    @Override
    public @NotNull Collection<CustomBlockType> all() {
        return Collections.unmodifiableCollection(this.types.values());
    }

    private void checkOpen() {
        if (this.registered) {
            throw new IllegalStateException("Custom blocks can only be registered from a plugin bootstrapper");
        }
    }

    private static @Nullable CustomBlockType typeOf(final BlockState state) {
        return state.getBlock() instanceof CitrusCustomBlock block ? block.type() : null;
    }

    private static BlockState parseVisual(final CustomBlockDefinition definition) {
        try {
            return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, definition.visualState(), false).blockState();
        } catch (final CommandSyntaxException exception) {
            throw new IllegalStateException("Invalid visual state for " + definition.key() + ": " + definition.visualState(), exception);
        }
    }

    private static Block vanillaBlock(final Material material) {
        return BuiltInRegistries.BLOCK.getOptional(Identifier.withDefaultNamespace(material.getKey().getKey()))
            .orElseThrow(() -> new IllegalArgumentException("Unknown vanilla block " + material));
    }

    private static Identifier toIdentifier(final Key key) {
        return Identifier.fromNamespaceAndPath(key.namespace(), key.value());
    }
}