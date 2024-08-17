package wily.legacy.fabric.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wily.legacy.util.ScreenUtil;

@Mixin(TooltipRenderUtil.class)
public class TooltipRenderUtilMixin {
    @Inject(method = "renderTooltipBackground", at = @At("HEAD"), cancellable = true)
    private static void renderTooltipBackground(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, CallbackInfo ci){
        ScreenUtil.renderPointerPanel(guiGraphics,i - (int)(5 * ScreenUtil.getTextScale()) - 5,j - (int)(9 *  ScreenUtil.getTextScale()) + 6,(int)((k + 11) *  ScreenUtil.getTextScale()),(int)((l + 6) *  ScreenUtil.getTextScale()));
        ci.cancel();
    }
}
