package wily.legacy.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "getRenderOffset(Lnet/minecraft/client/player/AbstractClientPlayer;F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    public void getRenderOffset(AbstractClientPlayer abstractClientPlayer, float f, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(abstractClientPlayer.isCrouching() ? new Vec3(0.0, 0.125, 0.0) : super.getRenderOffset(abstractClientPlayer, f));
    }

    @Redirect(method = "renderHand", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/PlayerModel;swimAmount:F", opcode = Opcodes.PUTFIELD))
    private void renderHand(PlayerModel instance, float value, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer) {
        instance.swimAmount = abstractClientPlayer.getSwimAmount(Minecraft.getInstance().isPaused() ? Minecraft.getInstance().pausePartialTick : Minecraft.getInstance().getFrameTime());
        ((PlayerModel)getModel()).rightArmPose = HumanoidModel.ArmPose.EMPTY;
        ((PlayerModel)getModel()).leftArmPose = HumanoidModel.ArmPose.EMPTY;
    }
}
