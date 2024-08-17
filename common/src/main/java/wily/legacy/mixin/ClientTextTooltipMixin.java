package wily.legacy.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientTextTooltip.class)

public class ClientTextTooltipMixin {
    @Shadow @Final private FormattedCharSequence text;
    @Unique
    private final Font.StringRenderOutput fontString;
    private static final ResourceLocation MOJANG_11_FONT = new ResourceLocation("minecraft","mojangles_11");

    public ClientTextTooltipMixin(Font.StringRenderOutput fontString) {
        this.fontString = fontString;
    }

    @Inject(method = "renderText", at = @At("HEAD"), cancellable = true)
    public void renderText(Font font, int i, int j, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource, CallbackInfo ci) {
        ci.cancel();
        float f = 0;
        float g = 0;

        font.drawInBatch(this.text, (float)i - 5, (float)j, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        font.getFontSet(MOJANG_11_FONT);
    }
}
