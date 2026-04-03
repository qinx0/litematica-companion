package qinx.litematicacompanion;

import net.fabricmc.api.ClientModInitializer;
import qinx.litematicacompanion.config.Configs;

public class LitematicaCompanionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Configs.INSTANCE.getClass();
    }
}
