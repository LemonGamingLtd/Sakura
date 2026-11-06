package ltd.lemongaming.citrus.block;

import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.GlobalPalette;
import net.minecraft.world.level.chunk.Palette;
import org.jspecify.annotations.Nullable;

/**
 * Maps server block state ids to the vanilla ids clients understand.
 */
public final class ClientBlockStates {

    private static boolean active;
    private static int[] serverToClient = new int[0];
    private static int clientStateCount;
    private static int clientGlobalBits;

    public static final IdMap<BlockState> CLIENT_ID_MAP = new IdMap<>() {
        @Override
        public int getId(final BlockState state) {
            final int id = Block.BLOCK_STATE_REGISTRY.getId(state);
            return id == -1 ? -1 : toClient(id);
        }

        @Override
        public @Nullable BlockState byId(final int id) {
            return Block.BLOCK_STATE_REGISTRY.byId(id);
        }

        @Override
        public int size() {
            return clientStateCount;
        }

        @Override
        public Iterator<BlockState> iterator() {
            return Block.BLOCK_STATE_REGISTRY.iterator();
        }
    };

    public static final StreamCodec<ByteBuf, BlockState> STREAM_CODEC = ByteBufCodecs.idMapper(
        id -> Block.BLOCK_STATE_REGISTRY.byIdOrThrow(id),
        ClientBlockStates::clientId
    );

    private ClientBlockStates() {
    }

    static void bake(final Set<Block> collapsed, final Collection<CitrusCustomBlockType> types) {
        final int total = Block.BLOCK_STATE_REGISTRY.size();
        int vanillaCount = total;
        for (final CitrusCustomBlockType type : types) {
            for (final BlockState state : type.block().getStateDefinition().getPossibleStates()) {
                vanillaCount = Math.min(vanillaCount, Block.getId(state));
            }
        }

        final int[] table = new int[total];
        for (int id = 0; id < total; id++) {
            final Block block = Block.BLOCK_STATE_REGISTRY.byIdOrThrow(id).getBlock();
            if (block instanceof CitrusCustomBlock customBlock) {
                table[id] = Block.getId(customBlock.type().visualState());
            } else if (id >= vanillaCount) {
                throw new IllegalStateException("Vanilla block " + block + " was registered after custom blocks");
            } else if (collapsed.contains(block)) {
                table[id] = Block.getId(block.defaultBlockState());
            } else {
                table[id] = id;
            }
        }

        serverToClient = table;
        clientStateCount = vanillaCount;
        clientGlobalBits = Mth.ceillog2(vanillaCount);
        active = !types.isEmpty() || !collapsed.isEmpty();
    }

    public static int clientStateCount() {
        return clientStateCount;
    }

    public static int toClient(final int serverId) {
        return active && serverId >= 0 && serverId < serverToClient.length ? serverToClient[serverId] : serverId;
    }

    public static int clientId(final BlockState state) {
        return toClient(Block.getId(state));
    }

    @SuppressWarnings("unchecked")
    public static <T> IdMap<T> forClient(final IdMap<T> globalMap) {
        return active && globalMap == Block.BLOCK_STATE_REGISTRY ? (IdMap<T>) CLIENT_ID_MAP : globalMap;
    }

    /**
     * Global palette sections store raw server ids, so they are re-encoded at the client's bit width.
     */
    public static boolean remapsGlobalPalette(final IdMap<?> globalMap, final Palette<?> palette) {
        return active && globalMap == Block.BLOCK_STATE_REGISTRY && palette instanceof GlobalPalette;
    }

    public static int globalPaletteBits() {
        return clientGlobalBits;
    }

    public static long[] remapGlobalStorage(final BitStorage storage) {
        final SimpleBitStorage remapped = new SimpleBitStorage(clientGlobalBits, storage.getSize());
        for (int i = 0; i < storage.getSize(); i++) {
            remapped.set(i, toClient(storage.get(i)));
        }
        return remapped.getRaw();
    }

    public static int globalStorageBytes(final int entries) {
        final int valuesPerLong = Long.SIZE / clientGlobalBits;
        return (entries + valuesPerLong - 1) / valuesPerLong * Long.BYTES;
    }

    public static int levelEventData(final int type, final int data) {
        return switch (type) {
            case LevelEvent.PARTICLES_AND_SOUND_DESTROY_BLOCK, LevelEvent.PARTICLES_DESTROY_BLOCK,
                 LevelEvent.PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE -> toClient(data);
            default -> data;
        };
    }

    @SuppressWarnings("unchecked")
    public static <R> R registryValue(final R value) {
        if (value instanceof CitrusCustomBlock block) {
            return (R) block.visualBlock();
        }
        if (value instanceof Holder<?> holder && holder.value() instanceof CitrusCustomBlock block) {
            return (R) block.visualBlock().builtInRegistryHolder();
        }
        return value;
    }
}