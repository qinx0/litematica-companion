package qinx.litematicacompanion.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.config.options.*;

import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Configs implements IConfigHandler {
    static String modId = FabricLoader.getInstance().getModContainer("litematica-companion")
            .orElseThrow().getMetadata().getId();
    private static final String CONFIG_FILE_NAME = modId + ".json";
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

    public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
            enabled,
            sidebarBuffer,
            widgetOpacity,
            padding,
            minScale,
            backgroundPreset,
            backgroundColor
    );

    private static void loadFromFile() {
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve("litematica-companion.json");

        if (Files.exists(configFile) && Files.isReadable(configFile)) {
            JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);

            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                ConfigUtils.readConfigBase(root, "litematica-companion", OPTIONS);
            }
        }
    }

    private static void saveToFile() {
        Path dir = FileUtils.getConfigDirectoryAsPath();

        if (!Files.exists(dir)) {
            FileUtils.createDirectoriesIfMissing(dir);
        }

        if (Files.isDirectory(dir)) {
            JsonObject root = new JsonObject();
            ConfigUtils.writeConfigBase(root, "litematica-companion", OPTIONS);
            JsonUtils.writeJsonToFileAsPath(root, dir.resolve("litematica-companion.json"));
        }
    }

    @Override
    public void load() {
//        ConfigUtils.readConfigBase("litematica-companion", OPTIONS);
        loadFromFile();
    }
    @Override
    public void save() {
//        ConfigUtils.writeConfigBase("litematica-companion");
        saveToFile();
    }
    
    public static int getOpacityValue() {
        return (int)(widgetOpacity.getIntegerValue() * 2.55);
    }
    
    public static float getScaleValue() {
        return minScale.getIntegerValue() / 100.0f;
    }
}
