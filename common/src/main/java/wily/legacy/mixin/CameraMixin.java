package wily.legacy.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private Entity entity;
    @Shadow
    private float eyeHeight;

    public CameraMixin() {
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void startEyeRendering(CallbackInfo callback, @Share("eyeHeight") LocalFloatRef eyeHeight) {
        eyeHeight.set(this.eyeHeight);
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void endEyeRendering(CallbackInfo callback, @Share("eyeHeight") LocalFloatRef eyeHeight) {
        float entityEyeHeight = this.entity.getEyeHeight();
        if (this.eyeHeight < entityEyeHeight) {
            this.eyeHeight += (entityEyeHeight - this.eyeHeight) * 0.7F;
        } else {
            this.eyeHeight = entityEyeHeight;
        }

        Entity var5 = this.entity;
        if (var5 instanceof Player) {
            Player player = (Player)var5;
            if (entityEyeHeight == 1.62F && !player.getAbilities().flying && player.isCrouching()) {
                this.eyeHeight = 1.55F;
            }
        }
    }
}
