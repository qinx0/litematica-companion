package qinx.litematicacompanion.mixin;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qinx.litematicacompanion.gui.BlockListWidget;

@Mixin(net.minecraft.client.gui.screen.ingame.HandledScreen.class)
public abstract class InventoryScreenMixin extends Screen {

    @Unique
    private BlockListWidget blockListWidget;

    protected InventoryScreenMixin() {
        super(Text.empty());
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        blockListWidget = new BlockListWidget(4, 4, 120, 160);
        blockListWidget.refreshBlockList(); // pull from litematica
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