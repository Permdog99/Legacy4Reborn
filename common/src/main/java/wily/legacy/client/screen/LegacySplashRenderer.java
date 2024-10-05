package wily.legacy.client.screen;

import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class LegacySplashRenderer {
    public static final LegacySplashRenderer CHRISTMAS = new LegacySplashRenderer("Merry X-mas!");
    public static final LegacySplashRenderer NEW_YEAR = new LegacySplashRenderer("Happy new year!");
    public static final LegacySplashRenderer HALLOWEEN = new LegacySplashRenderer("OOoooOOOoooo! Spooky!");
    private static final int WIDTH_OFFSET = 123;
    private static final int HEIGH_OFFSET = 69;
    private final String splash;

    public LegacySplashRenderer(String string) {
        this.splash = string;
    }

    public void render(GuiGraphics guiGraphics, int i, Font font, int j) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float)i / 2.0F + 123.0F, 69.0F, 0.0F);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-15.0F));
        float f = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * 6.2831855F) * 0.1F);
        f = (f * 100.0F / (float)(font.width(this.splash) + 32)) * 1.5f;
        guiGraphics.pose().scale(f * 1.5f, f * 1.5f, f * 1.5f);
        guiGraphics.drawCenteredString(font, this.splash, 0, 0, 16776960 | j);
        guiGraphics.pose().popPose();
    }
}
