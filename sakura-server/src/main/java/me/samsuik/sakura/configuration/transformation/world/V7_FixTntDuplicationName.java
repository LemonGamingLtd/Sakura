package me.samsuik.sakura.configuration.transformation.world;

import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

public final class V7_FixTntDuplicationName {
    private static final int VERSION = 7;
    private static final NodePath OLD_PATH = NodePath.path("technical", "allow-t-n-t-duplication");
    private static final String NEW_NAME = "allow-tnt-duplication";

    public static void apply(ConfigurationTransformation.VersionedBuilder builder) {
        builder.addVersion(VERSION, ConfigurationTransformation.builder().addAction(OLD_PATH, TransformAction.rename(NEW_NAME)).build());
    }
}
