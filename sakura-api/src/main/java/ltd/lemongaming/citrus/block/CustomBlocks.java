package ltd.lemongaming.citrus.block;

import net.kyori.adventure.key.Key;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Objects;

public final class CustomBlocks {

    private static Service service;

    private CustomBlocks() {}

    /**
     * Only callable from a plugin bootstrapper.
     */
    @NotNull
    public static CustomBlockType register(@NotNull CustomBlockDefinition definition) {
        return service().register(definition);
    }

    /**
     * Sends every state of a vanilla block to clients as its default state.
     */
    public static void collapse(@NotNull String blockKey) {
        service().collapse(blockKey);
    }

    @Nullable
    public static CustomBlockType get(@NotNull Key key) {
        return service().get(key);
    }

    @Nullable
    public static CustomBlockType get(@NotNull Block block) {
        return service().get(block);
    }

    @Nullable
    public static CustomBlockType get(@NotNull BlockData blockData) {
        return service().get(blockData);
    }

    public static boolean isCustom(@NotNull Block block) {
        return get(block) != null;
    }

    @NotNull
    @Unmodifiable
    public static Collection<CustomBlockType> all() {
        return service().all();
    }

    @NotNull
    private static Service service() {
        if (service == null) {
            throw new IllegalStateException("Citrus custom block engine is not available");
        }
        return service;
    }

    @ApiStatus.Internal
    public static void service(@NotNull Service service) {
        if (CustomBlocks.service != null) {
            throw new IllegalStateException("Custom block service already set");
        }
        CustomBlocks.service = Objects.requireNonNull(service, "service");
    }

    @ApiStatus.Internal
    public interface Service {

        @NotNull
        CustomBlockType register(@NotNull CustomBlockDefinition definition);

        void collapse(@NotNull String blockKey);

        @Nullable
        CustomBlockType get(@NotNull Key key);

        @Nullable
        CustomBlockType get(@NotNull Block block);

        @Nullable
        CustomBlockType get(@NotNull BlockData blockData);

        @NotNull
        @Unmodifiable
        Collection<CustomBlockType> all();
    }
}