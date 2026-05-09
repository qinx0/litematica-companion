package qinx.litematicacompanion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import qinx.litematicacompanion.gui.BlockListWidget;

@Mixin(HandledScreen.class)
public interface InventoryScreenMixinAccessor {
    @Accessor("blockListWidget")
    BlockListWidget litematicacompanion$getBlockListWidget();
}