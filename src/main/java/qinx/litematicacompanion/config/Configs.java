package qinx.litematicacompanion.config;

import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.config.options.*;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;

public class Configs implements IConfigHandler {
    
    public static final Configs INSTANCE = new Configs();
    
    public static final IConfigBoolean enabled = new ConfigBoolean("enabled", true, "Enable or disable the material list widget");
    public static final IConfigInteger sidebarBuffer = new ConfigInteger("sidebarBuffer", 50, 0, 200, "Buffer space on the left side of inventory to avoid other mod GUIs (JEI, REI, EMI, etc.)");
    public static final IConfigInteger widgetOpacity = new ConfigInteger("widgetOpacity", 80, 0, 100, "Opacity of the widget background (percentage)");
    public static final IConfigInteger padding = new ConfigInteger("padding", 4, 0, 50, "Padding around the widget from inventory edges");
    public static final IConfigInteger minScale = new ConfigInteger("minScale", 60, 30, 100, "Minimum scale when shrinking widget to fit available space (percentage)");

    public static final ConfigOptionList backgroundPreset = new ConfigOptionList("backgroundPreset", BackgroundPreset.DARK, "Background preset");
    public static final ConfigColor backgroundColor = new ConfigColor("backgroundColor", "#000000", "Custom preset background color");
    public static int getBackgroundColorValue() {
        BackgroundPreset preset = (BackgroundPreset) backgroundPreset.getOptionListValue();
        if (preset == BackgroundPreset.LIGHT) return 0xDDDDDD;
        else if (preset == BackgroundPreset.DARK) return 0x222222;
        else return backgroundColor.getIntegerValue(); // CUSTOM
    }


    private Configs() {
        ConfigManager.getInstance().registerConfigHandler("litematica-companion", this);
    }
    
    @Override
    public void load() {
    }
    
    @Override
    public void save() {
    }
    
    public static int getOpacityValue() {
        return (int)(widgetOpacity.getIntegerValue() * 2.55);
    }
    
    public static float getScaleValue() {
        return minScale.getIntegerValue() / 100.0f;
    }
}
