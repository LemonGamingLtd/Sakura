package me.samsuik.sakura.configuration.transformation;

import io.papermc.paper.configuration.transformation.Transformations;
import me.samsuik.sakura.configuration.transformation.global.V1_RelocateMessages;
import me.samsuik.sakura.configuration.transformation.global.V2_ConvertIconToMaterial;
import me.samsuik.sakura.configuration.transformation.world.*;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import java.util.List;

public final class ConfigurationTransformations {
    private static final List<NodePath> REMOVED_GLOBAL_PATHS = List.of(
        NodePath.path("cannons")
    );

    public static void worldTransformations(final ConfigurationNode node) throws ConfigurateException {
        final ConfigurationTransformation.VersionedBuilder versionedBuilder = Transformations.versionedBuilder();
        V2_VerticalKnockbackUseDefault.apply(versionedBuilder);
        V3_RenameKnockback.apply(versionedBuilder);
        V4_RenameNonStrictMergeLevel.apply(versionedBuilder);
        V5_CombineLoadChunksOptions.apply(versionedBuilder);
        V6_FixIncorrectExtraKnockback.apply(versionedBuilder);
        // ADD FUTURE VERSIONED TRANSFORMS TO versionedBuilder HERE
        versionedBuilder.build().apply(node);
    }

    public static void globalTransformations(final ConfigurationNode node) throws ConfigurateException {
        final ConfigurationTransformation.Builder builder = ConfigurationTransformation.builder();
        for (final NodePath path : REMOVED_GLOBAL_PATHS) {
            builder.addAction(path, TransformAction.remove());
        }
        builder.build().apply(node);

        final ConfigurationTransformation.VersionedBuilder versionedBuilder = Transformations.versionedBuilder();
        V1_RelocateMessages.apply(versionedBuilder);
        V2_ConvertIconToMaterial.apply(versionedBuilder);
        // ADD FUTURE VERSIONED TRANSFORMS TO versionedBuilder HERE
        versionedBuilder.build().apply(node);
    }

    private ConfigurationTransformations() {}
}
