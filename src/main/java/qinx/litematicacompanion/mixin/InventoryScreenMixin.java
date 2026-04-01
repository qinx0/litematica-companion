package qinx.litematicacompanion.mixin;

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
import qinx.litematicacompanion.gui.BlockListWidget;

@Mixin(HandledScreen.class)
public abstract class InventoryScreenMixin extends Screen {

    private static final Logger log = LoggerFactory.getLogger(InventoryScreenMixin.class);
    // Shadow these fields from HandledScreen to get the GUI position
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;

    @Unique
    private BlockListWidget blockListWidget;

    protected InventoryScreenMixin() {
        super(Text.empty());
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int originalWidth = 120;
        int originalHeight = 160;
        int padding = 4;
        int sidebarBuffer = 40; // Accounts for JEI and other mods left-side bar

        // Create the widget first to let it calculate its true text-based width
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

        float finalScale = Math.max(0.6f, Math.min(1.0f, Math.min(widthScale, heightScale)));
        log.info(String.valueOf(finalScale));

        int widgetX = this.x - (int)(actualDesiredWidth * finalScale) - padding;
        int widgetY = this.y;

        blockListWidget.setX(widgetX);
        blockListWidget.setY(widgetY);
        blockListWidget.setRenderScale(finalScale);

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