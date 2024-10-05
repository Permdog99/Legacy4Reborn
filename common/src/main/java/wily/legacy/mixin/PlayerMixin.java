package wily.legacy.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wily.legacy.Legacy4JClient;
import wily.legacy.player.PlayerYBobbing;

import static net.minecraft.world.entity.player.Player.STANDING_DIMENSIONS;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerYBobbing {
    float oYBob;
    float yBob;

    @Shadow public abstract Abilities getAbilities();

    @Unique
    private static final ImmutableMap<Object, Object> POSES_OLD;

    @Override
    public float oYBob() {
        return oYBob;
    }

    @Override
    public void setOYBob(float bob) {
        oYBob = bob;
    }

    @Override
    public float yBob() {
        return yBob;
    }

    @Override
    public void setYBob(float bob) {
        yBob = bob;
    }

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }
    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "RETURN"))
    public void drop(ItemStack itemStack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> cir) {
        if (cir.getReturnValue() != null && !level().isClientSide && bl2) super.playSound(SoundEvents.ITEM_PICKUP,1.0f,1.0f);
    }
    @Inject(method = "getFlyingSpeed", at = @At(value = "RETURN"), cancellable = true)
    protected void getFlyingSpeed(CallbackInfoReturnable<Float> cir) {
        if (level().isClientSide && !Legacy4JClient.isModEnabledOnServer()) return;
        cir.setReturnValue(cir.getReturnValueF() * ( getAbilities().flying ? (isSprinting() ? level().isClientSide ? Math.max(10,Math.min(Legacy4JClient.getEffectiveRenderDistance(),18)) * 0.6f : 6 : 2) : 1));
    }
    @Inject(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;bob:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    public void aiStep(CallbackInfo ci) {
        handleYBobbing();
    }

    /**
     * @author
     * Permdog99
     * @reason
     * Remove crawling
     */
    @Overwrite
    public void updatePlayerPose() {
        if (this.canPlayerFitWithinBlocksAndEntitiesWhen(Pose.SWIMMING)) {
            Pose pose;
            if (this.isFallFlying()) {
                pose = Pose.FALL_FLYING;
            } else if (this.isSleeping()) {
                pose = Pose.SLEEPING;
            } else if (this.isSwimming()) {
                pose = Pose.STANDING;
            } else if (this.isAutoSpinAttack()) {
                pose = Pose.SPIN_ATTACK;
            } else if (this.isShiftKeyDown() && !this.getAbilities().flying) {
                pose = Pose.CROUCHING;
            } else {
                pose = Pose.STANDING;
            }

            Pose pose2;
            if (!this.isSpectator() && !this.isPassenger() && !this.canPlayerFitWithinBlocksAndEntitiesWhen(pose)) {
                if (this.canPlayerFitWithinBlocksAndEntitiesWhen(Pose.CROUCHING)) {
                    pose2 = Pose.CROUCHING;
                } else {
                    pose2 = Pose.STANDING;
                }
            } else {
                pose2 = pose;
            }

            this.setPose(pose2);
        }
    }

    @Shadow
    protected boolean canPlayerFitWithinBlocksAndEntitiesWhen(Pose pose) {
        return this.level().noCollision(this, this.getDimensions(pose).makeBoundingBox(this.position()).deflate(1.0E-7));
    }

    /**
     * @author
     * Permdog99
     * @reason
     * Change crouch height
     */
    @Overwrite
    public @NotNull EntityDimensions getDimensions(Pose pose) {
        return (EntityDimensions)POSES_OLD.getOrDefault(pose, STANDING_DIMENSIONS);
    }

    static {
        POSES_OLD = ImmutableMap.builder().put(Pose.STANDING, STANDING_DIMENSIONS).put(Pose.SLEEPING, SLEEPING_DIMENSIONS).put(Pose.FALL_FLYING, EntityDimensions.scalable(0.6F, 0.6F)).put(Pose.SWIMMING, EntityDimensions.scalable(0.6F, 0.6F)).put(Pose.SPIN_ATTACK, EntityDimensions.scalable(0.6F, 0.6F)).put(Pose.CROUCHING, EntityDimensions.scalable(0.6F, 1.8F)).put(Pose.DYING, EntityDimensions.fixed(0.2F, 0.2F)).build();
    }

    /**
     * @author
     * Permdog99
     * @reason
     * Change crouching eye level
     */
    @Overwrite
    public float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        switch (pose) {
            case SWIMMING:
            case FALL_FLYING:
            case SPIN_ATTACK:
                return 0.4F;
            case CROUCHING:
                return 1.55F;
            default:
                return 1.62F;
        }
    }
}
