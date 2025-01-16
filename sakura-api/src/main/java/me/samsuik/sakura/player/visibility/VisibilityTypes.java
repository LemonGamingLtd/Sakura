package me.samsuik.sakura.player.visibility;

import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public final class VisibilityTypes {
    private static final List<VisibilityType> TYPES = new ArrayList<>();

    public static final VisibilityType TNT = register(create("tnt", true));
    public static final VisibilityType SAND = register(create("sand", true));
    public static final VisibilityType EXPLOSIONS = register(create("explosions", true));
    public static final VisibilityType SPAWNERS = register(create("spawners", false));
    public static final VisibilityType PISTONS = register(create("pistons", false));

    public static ImmutableList<VisibilityType> types() {
        return ImmutableList.copyOf(TYPES);
    }

    private static VisibilityType create(String key, boolean minimal) {
        return VisibilityType.from(key, minimal);
    }

    private static VisibilityType register(VisibilityType type) {
        TYPES.add(type);
        return type;
    }
}
