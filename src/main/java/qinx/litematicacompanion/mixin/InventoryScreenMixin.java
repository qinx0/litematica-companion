package qinx.litematicacompanion.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qinx.litematicacompanion.config.Configs;
import qinx.litematicacompanion.gui.BlockListWidget;

@Mixin(HandledScreen.class)
public abstract class InventoryScreenMixin extends Screen {

    private static final Logger log = LoggerFactory.getLogger(InventoryScreenMixin.class);
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;

    @Unique
    private BlockListWidget blockListWidget;

    @Unique
    private static boolean jeiLoaded = false;

    @Unique
    private static boolean reiLoaded = false;

    @Unique
    private static boolean emiLoaded = false;

    static {
        detectMods();
    }

    private static void detectMods() {
        try {
            Class.forName("mezz.jei.api.JeiPlugin");
            jeiLoaded = true;
            log.info("JEI detected");
        } catch (ClassNotFoundException e) {
            jeiLoaded = false;
        }

        try {
            Class.forName("me.shedaniel.rei.api.plugins.REIPluginV0");
            reiLoaded = true;
            log.info("REI detected");
        } catch (ClassNotFoundException e) {
            reiLoaded = false;
        }

        try {
            Class.forName("dev.emi.EMI");
            emiLoaded = true;
            log.info("EMI detected");
        } catch (ClassNotFoundException e) {
            emiLoaded = false;
        }
    }

    protected InventoryScreenMixin() {
        super(Text.empty());
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (!Configs.enabled.getBooleanValue()) {
            return;
        }

        int originalWidth = 120;
        int originalHeight = 160;
        int padding = Configs.padding.getIntegerValue();
        int sidebarBuffer = Configs.sidebarBuffer.getIntegerValue();

        if (jeiLoaded || reiLoaded || emiLoaded) {
            sidebarBuffer = Math.max(sidebarBuffer, 176);
        }

        blockListWidget = new BlockListWidget(0, 0, originalWidth, originalHeight);
        int actualDesiredWidth = blockListWidget.getWidth();

        int availableWidth = this.x - padding - sidebarBuffer;
        float widthScale = (availableWidth < actualDesiredWidth)
                ? (float) availableWidth / actualDesiredWidth
                : 1.0f;

        int availableHeight = this.height - this.y - padding;
        float heightScale = (availableHeight < originalHeight)
                ? (float) availableHeight / originalHeight
                : 1.0f;

        float minScale = Configs.getScaleValue();
        float finalScale = Math.max(minScale, Math.min(1.0f, Math.min(widthScale, heightScale)));

        int widgetX = this.x - (int)(actualDesiredWidth * finalScale) - padding;
        int widgetY = this.y;

        blockListWidget.setX(widgetX);
        blockListWidget.setY(widgetY);
        blockListWidget.setRenderScale(finalScale);
        blockListWidget.setOpacity(Configs.getOpacityValue());

        this.addDrawableChild(blockListWidget);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (blockListWidget != null && blockListWidget.isMouseOver(mouseX, mouseY)) {
            blockListWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            cir.setReturnValue(true);
        }
    }
}
