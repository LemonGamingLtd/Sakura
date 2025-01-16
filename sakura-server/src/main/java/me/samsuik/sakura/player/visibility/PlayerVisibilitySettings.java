package me.samsuik.sakura.player.visibility;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public final class PlayerVisibilitySettings implements VisibilitySettings {
    private static final String SETTINGS_COMPOUND_TAG = "clientVisibilitySettings";
    private final Reference2ObjectMap<VisibilityType, VisibilityState> visibilityStates = new Reference2ObjectOpenHashMap<>();

    @Override
    public @NonNull VisibilityState get(@NonNull VisibilityType type) {
        VisibilityState state = this.visibilityStates.get(type);
        return state != null ? state : type.getDefault();
    }

    @Override
    public @NotNull VisibilityState set(@NonNull VisibilityType type, @NonNull VisibilityState state) {
        if (type.isDefault(state)) {
            this.visibilityStates.remove(type);
        } else {
            this.visibilityStates.put(type, state);
        }
        return state;
    }

    @Override
    public @NonNull VisibilityState currentState() {
        int modifiedCount = this.visibilityStates.size();
        if (modifiedCount == 0) {
            return VisibilityState.ON;
        } else if (modifiedCount != VisibilityTypes.types().size()) {
            return VisibilityState.MODIFIED;
        } else {
            return VisibilityState.OFF;
        }
    }

    @Override
    public boolean playerModified() {
        return !this.visibilityStates.isEmpty();
    }

    public void loadData(@NonNull CompoundTag tag) {
        if (!tag.contains(SETTINGS_COMPOUND_TAG, CompoundTag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag settingsTag = tag.getCompound(SETTINGS_COMPOUND_TAG);
        for (VisibilityType type : VisibilityTypes.types()) {
            if (settingsTag.contains(type.key(), CompoundTag.TAG_STRING)) {
                VisibilityState state = VisibilityState.valueOf(settingsTag.getString(type.key()));
                this.visibilityStates.put(type, state);
            }
        }
    }

    public void saveData(@NonNull CompoundTag tag) {
        CompoundTag settingsTag = new CompoundTag();
        this.visibilityStates.forEach((t, s) -> settingsTag.putString(t.key(), s.name()));
        tag.put(SETTINGS_COMPOUND_TAG, settingsTag);
    }
}
