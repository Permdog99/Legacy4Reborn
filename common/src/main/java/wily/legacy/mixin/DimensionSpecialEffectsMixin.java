package wily.legacy.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.awt.*;

@Mixin(DimensionSpecialEffects.class)
@Environment(EnvType.CLIENT)
public class DimensionSpecialEffectsMixin {

    @Unique
    private final float[] sunriseCol = new float[4];

    /**
     * @author
     * Permdog99
     * @reason
     * Change sunrise colors
     */
    @Overwrite
    @Nullable
    public float[] getSunriseColor(float f, float g) {
        Color base_color = new Color(63, 43, 51);
        float red = base_color.getRed() / 255f;
        float green = base_color.getGreen() / 255f;
        float blue = base_color.getBlue() / 255f;
        float alpha = base_color.getAlpha() / 255f;
        float h = 0.4F;
        float i = Mth.cos(f * 6.2831855F) - 0.0F;
        float j = -0.0F;
        if (i >= -0.2F && i <= 0.4F) {
            float k = (i - -0.0F) / (i <= 0.0F ? 0.2F : 0.4F) * 0.5F + 0.5F;
            float l = 1.0F - (1.0F - Mth.sin(k * 3.1415927F)) * 0.99F;
            l *= l;
            this.sunriseCol[0] = k * red + red;
            this.sunriseCol[1] = k * k * red + 0.2F;
            this.sunriseCol[2] = k * k * blue + 0.2F;
            this.sunriseCol[3] = l;
            return this.sunriseCol;
        } else {
            return null;
        }
    }
}
