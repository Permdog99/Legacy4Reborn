package wily.legacy.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(SplashRenderer.class)
public class SplashRendererMixin {

    @Mutable
    @Final
    @Shadow
    private final String splash;

    public SplashRendererMixin(String splash) {
        this.splash = splash;
    }

    /**
     * @author
     * Permdog99
     * @reason
     * Make splash more accurate
     */
    @Overwrite
    public void render(GuiGraphics guiGraphics, int i, Font font, int j) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float)i / 2.0F + 123.0F, 69.0F, 0.0F);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-15.0F));
        float f = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * 6.2831855F) * 0.1F);
        f = f * 100.0F / (float)(font.width(this.splash) + 32);
        guiGraphics.pose().scale(f * 1.5f, f * 1.5f, f * 1.5f);
        guiGraphics.drawCenteredString(font, this.splash, 0, 0, 16776960 | j);
        guiGraphics.pose().popPose();
    }
}
