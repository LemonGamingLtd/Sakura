package ltd.lemongaming.citrus.block;

import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CustomBlockDefinition {

    private final Key key;
    private final String visualState;
    private final float hardness;
    private final float blastResistance;
    private final boolean requiresCorrectTool;
    private final int lightLevel;
    private final Material soundsLike;
    private final Material fallbackMaterial;

    private CustomBlockDefinition(@NotNull Builder builder) {
        this.key = builder.key;
        this.visualState = builder.visualState;
        this.hardness = builder.hardness;
        this.blastResistance = builder.blastResistance;
        this.requiresCorrectTool = builder.requiresCorrectTool;
        this.lightLevel = builder.lightLevel;
        this.soundsLike = builder.soundsLike;
        this.fallbackMaterial = builder.fallbackMaterial;
    }

    @NotNull
    public static Builder builder(@NotNull Key key) {
        return new Builder(key);
    }

    @NotNull
    public Key key() {
        return key;
    }

    @NotNull
    public String visualState() {
        return visualState;
    }

    public float hardness() {
        return hardness;
    }

    public float blastResistance() {
        return blastResistance;
    }

    public boolean requiresCorrectTool() {
        return requiresCorrectTool;
    }

    public int lightLevel() {
        return lightLevel;
    }

    /**
     * Vanilla block whose sounds and map color the server uses.
     */
    @NotNull
    public Material soundsLike() {
        return soundsLike;
    }

    /**
     * Material reported by Bukkit, e.g. {@code Block#getType()}.
     */
    @NotNull
    public Material fallbackMaterial() {
        return fallbackMaterial;
    }

    public static final class Builder {

        private final Key key;
        private String visualState;
        private float hardness = 1.5F;
        private float blastResistance = 6.0F;
        private boolean requiresCorrectTool;
        private int lightLevel;
        private Material soundsLike = Material.STONE;
        private Material fallbackMaterial = Material.STONE;

        private Builder(@NotNull Key key) {
            this.key = Objects.requireNonNull(key, "key");
            if (key.namespace().equals(Key.MINECRAFT_NAMESPACE)) {
                throw new IllegalArgumentException("Custom blocks cannot use the minecraft namespace: " + key);
            }
        }

        @NotNull
        public Builder visualState(@NotNull String visualState) {
            this.visualState = Objects.requireNonNull(visualState, "visualState");
            return this;
        }

        @NotNull
        public Builder hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        @NotNull
        public Builder blastResistance(float blastResistance) {
            this.blastResistance = blastResistance;
            return this;
        }

        @NotNull
        public Builder requiresCorrectTool(boolean requiresCorrectTool) {
            this.requiresCorrectTool = requiresCorrectTool;
            return this;
        }

        @NotNull
        public Builder lightLevel(int lightLevel) {
            if (lightLevel < 0 || lightLevel > 15) {
                throw new IllegalArgumentException("Light level must be between 0 and 15: " + lightLevel);
            }
            this.lightLevel = lightLevel;
            return this;
        }

        @NotNull
        public Builder soundsLike(@NotNull Material soundsLike) {
            this.soundsLike = requireBlock(soundsLike, "soundsLike");
            return this;
        }

        @NotNull
        public Builder fallbackMaterial(@NotNull Material fallbackMaterial) {
            this.fallbackMaterial = requireBlock(fallbackMaterial, "fallbackMaterial");
            return this;
        }

        @NotNull
        public CustomBlockDefinition build() {
            if (visualState == null) {
                throw new IllegalStateException("Custom block " + key + " has no visual state");
            }
            return new CustomBlockDefinition(this);
        }

        /**
         * Only checks what is safe during bootstrap; the server verifies the material is a block when registering.
         */
        @NotNull
        private static Material requireBlock(@NotNull Material material, @NotNull String name) {
            Objects.requireNonNull(material, name);
            if (material.isLegacy()) {
                throw new IllegalArgumentException(name + " must be a modern block material: " + material);
            }
            return material;
        }
    }
}