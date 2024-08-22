package wily.legacy.fabric.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import mod.adrenix.nostalgic.helper.candy.level.SkyHelper;
import mod.adrenix.nostalgic.tweak.config.CandyTweak;
import mod.adrenix.nostalgic.tweak.enums.Generic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.BiFunction;

import static mod.adrenix.nostalgic.helper.candy.level.SkyHelper.BLUE_VOID_BUFFER;

@Mixin(SkyHelper.class)
@Environment(EnvType.CLIENT)
public abstract class SkyHelperMixin {

    @ModifyVariable(method = "createBlueVoid", at = @At("STORE"), ordinal = 0, remap = false)
    private static float changeHorizonHeight(float x) {
        return -8.0f;
    }
}