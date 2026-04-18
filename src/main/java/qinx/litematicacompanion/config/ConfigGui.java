package qinx.litematicacompanion.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class ConfigGui extends GuiConfigsBase {

    public ConfigGui() {
        super(10, 50, "litematica-companion", null, "litematica-companion.gui.title.configs", "Litematica Companion Configs");
    }

    @Override
    public void initGui() {
        super.initGui();
        this.setConfigWidth(200);
    }

    @Override
    protected int getConfigWidth() {
        return 200;
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<ConfigOptionWrapper> configs = new ArrayList<>();

        configs.add(new ConfigOptionWrapper("§6§lGeneral Settings"));
        configs.add(new ConfigOptionWrapper(Configs.enabled));
        configs.add(new ConfigOptionWrapper(Configs.sidebarBuffer));
        configs.add(new ConfigOptionWrapper(Configs.padding));
        configs.add(new ConfigOptionWrapper(Configs.minScale));
        configs.add(new ConfigOptionWrapper("§6§lAppearance"));
        configs.add(new ConfigOptionWrapper(Configs.backgroundPreset));
        configs.add(new ConfigOptionWrapper(Configs.backgroundColor));
        configs.add(new ConfigOptionWrapper(Configs.widgetOpacity));

        return configs;
    }

}
