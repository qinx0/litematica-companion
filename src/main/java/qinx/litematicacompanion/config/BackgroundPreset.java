package qinx.litematicacompanion.config;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;

public enum BackgroundPreset implements IConfigOptionListEntry {
    DARK("Dark"),
    LIGHT("Light"),
    CUSTOM("Custom");

    private final String displayName;

    BackgroundPreset(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getStringValue() {
        return this.displayName;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public IConfigOptionListEntry cycle(boolean reverse) {
        int len = values().length;
        int next = (this.ordinal() + (reverse ? -1 : 1) + len) % len;
        return values()[next];
    }

    @Override
    public IConfigOptionListEntry fromString(String name) {
        for (BackgroundPreset preset : values()) {
            if (preset.name().equalsIgnoreCase(name) || preset.displayName.equalsIgnoreCase(name)) {
                return preset;
            }
        }
        return DARK; // fallback
    }
}
