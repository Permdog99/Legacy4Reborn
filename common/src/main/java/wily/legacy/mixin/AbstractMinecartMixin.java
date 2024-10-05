package wily.legacy.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static net.minecraft.world.entity.vehicle.AbstractMinecart.exits;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends VehicleEntity {
    public AbstractMinecartMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    protected double getMaxSpeed() {
        return ((this.isInWater() ? 4.0 : 8.0) / 20.0) * 3;
    }

    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    protected void changeMaxSpeed(CallbackInfoReturnable<Double> cir) {
        BlockPos blockPos = blockPosition();
        BlockState blockState = this.level().getBlockState(blockPos);
        Block block = blockState.getBlock();
        BlockState blockBelowState = this.level().getBlockState(blockPos.below());
        Block blockBelow = blockBelowState.getBlock();
        Vec3 minecartMovement = this.getDeltaMovement();

        if (Math.abs(minecartMovement.x) < 0.5 && Math.abs(minecartMovement.z) < 0.5) {
            return;
        }

        if (blockState.isAir() && blockBelow instanceof BaseRailBlock baseRailBlock) {
            RailShape currentRailShape = blockBelowState.getValue(baseRailBlock.getShapeProperty());
            switch (currentRailShape) {
                case ASCENDING_EAST, ASCENDING_NORTH, ASCENDING_SOUTH, ASCENDING_WEST -> {
                    return;
                }
            }
        }

        if (block instanceof BaseRailBlock baseRailBlock) {
            RailShape currentRailShape = blockState.getValue(baseRailBlock.getShapeProperty());
            switch (currentRailShape) {
                case SOUTH_EAST, SOUTH_WEST, NORTH_WEST, NORTH_EAST, ASCENDING_EAST, ASCENDING_NORTH, ASCENDING_SOUTH, ASCENDING_WEST -> {
                    return;
                }
            }

            Vec3i runningDirection = getMinecartRunningDirection(currentRailShape, minecartMovement);

            for (int i = 0; i < 2; i++) {
                RailShape railShapeAtOffset;

                if (runningDirection == null) {
                    return;
                }

                railShapeAtOffset = getRailShapeAtOffset(new Vec3i(runningDirection.getX() * i, 0, runningDirection.getZ() * i), blockPos, level());
                if (railShapeAtOffset == null) {
                    return;
                }

                switch (railShapeAtOffset) {
                    case SOUTH_EAST, SOUTH_WEST, NORTH_WEST, NORTH_EAST, ASCENDING_EAST, ASCENDING_NORTH, ASCENDING_SOUTH, ASCENDING_WEST -> {
                        return;
                    }
                }
            }
        }
        cir.setReturnValue(cir.getReturnValueD() * 3);
    }
    private static RailShape getRailShapeAtOffset(Vec3i blockOffset, BlockPos blockPos, Level level) {
        BlockState blockState = level.getBlockState(blockPos.offset(blockOffset));
        if (blockState.getBlock() instanceof BaseRailBlock abstractRailBlock) {
            return blockState.getValue(abstractRailBlock.getShapeProperty());
        } else {
            return null;
        }
    }

    private static Vec3i getMinecartRunningDirection(RailShape railShape, Vec3 vec) {
        if (railShape == RailShape.EAST_WEST || railShape == RailShape.NORTH_SOUTH ) {
            return new Vec3i(railShape == RailShape.EAST_WEST ? (int) Math.signum(vec.x) : 0, 0, railShape == RailShape.NORTH_SOUTH ? (int) Math.signum(vec.z) : 0);
        }
        return null;
    }

    @Inject(method = "comeOffTrack", at = @At("HEAD"), cancellable = true)
    private void changeFallSpeed(CallbackInfo ci) {
        double d = this.getMaxSpeed();
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(Mth.clamp(vec3.x, -d, d), vec3.y, Mth.clamp(vec3.z, -d, d));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(1.0));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95 * 1.5));
        }
        ci.cancel();
    }

    @Shadow
    private boolean isRedstoneConductor(BlockPos blockPos) {
        return this.level().getBlockState(blockPos).isRedstoneConductor(this.level(), blockPos);
    }

    @Shadow
    @Nullable
    public Vec3 getPos(double d, double e, double f) {
        int i = Mth.floor(d);
        int j = Mth.floor(e);
        int k = Mth.floor(f);
        if (this.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
            --j;
        }

        BlockState blockState = this.level().getBlockState(new BlockPos(i, j, k));
        if (BaseRailBlock.isRail(blockState)) {
            RailShape railShape = (RailShape)blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
            Pair<Vec3i, Vec3i> pair = exits(railShape);
            Vec3i vec3i = (Vec3i)pair.getFirst();
            Vec3i vec3i2 = (Vec3i)pair.getSecond();
            double g = (double)i + 0.5 + (double)vec3i.getX() * 0.5;
            double h = (double)j + 0.0625 + (double)vec3i.getY() * 0.5;
            double l = (double)k + 0.5 + (double)vec3i.getZ() * 0.5;
            double m = (double)i + 0.5 + (double)vec3i2.getX() * 0.5;
            double n = (double)j + 0.0625 + (double)vec3i2.getY() * 0.5;
            double o = (double)k + 0.5 + (double)vec3i2.getZ() * 0.5;
            double p = m - g;
            double q = (n - h) * 2.0;
            double r = o - l;
            double s;
            if (p == 0.0) {
                s = f - (double)k;
            } else if (r == 0.0) {
                s = d - (double)i;
            } else {
                double t = d - g;
                double u = f - l;
                s = (t * p + u * r) * 2.0;
            }

            d = g + p * s;
            e = h + q * s;
            f = l + r * s;
            if (q < 0.0) {
                ++e;
            } else if (q > 0.0) {
                e += 0.5;
            }

            return new Vec3(d, e, f);
        } else {
            return null;
        }
    }

    @Inject(method = "moveAlongTrack", at = @At("HEAD"), cancellable = true)
    private void changeMinecartsToLCE(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        this.resetFallDistance();
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        Vec3 vec3 = this.getPos(d, e, f);
        e = (double)blockPos.getY();
        boolean bl = false;
        boolean bl2 = false;
        if (blockState.is(Blocks.POWERED_RAIL)) {
            bl = (Boolean)blockState.getValue(PoweredRailBlock.POWERED);
            bl2 = !bl;
        }

        double g = 0.0078125;
        if (this.isInWater()) {
            g *= 0.2;
        }

        Vec3 vec32 = this.getDeltaMovement();
        RailShape railShape = (RailShape)blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
        switch (railShape) {
            case ASCENDING_EAST:
                this.setDeltaMovement(vec32.add(-g, 0.0, 0.0));
                ++e;
                break;
            case ASCENDING_WEST:
                this.setDeltaMovement(vec32.add(g, 0.0, 0.0));
                ++e;
                break;
            case ASCENDING_NORTH:
                this.setDeltaMovement(vec32.add(0.0, 0.0, g));
                ++e;
                break;
            case ASCENDING_SOUTH:
                this.setDeltaMovement(vec32.add(0.0, 0.0, -g));
                ++e;
        }

        vec32 = this.getDeltaMovement();
        Pair<Vec3i, Vec3i> pair = exits(railShape);
        Vec3i vec3i = (Vec3i)pair.getFirst();
        Vec3i vec3i2 = (Vec3i)pair.getSecond();
        double h = (double)(vec3i2.getX() - vec3i.getX());
        double i = (double)(vec3i2.getZ() - vec3i.getZ());
        double j = Math.sqrt(h * h + i * i);
        double k = vec32.x * h + vec32.z * i;
        if (k < 0.0) {
            h = -h;
            i = -i;
        }

        double l = Math.min(2.0, vec32.horizontalDistance());
        vec32 = new Vec3(l * h / j, vec32.y, l * i / j);
        this.setDeltaMovement(vec32);
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player) {
            Vec3 vec33 = entity.getDeltaMovement();
            double m = vec33.horizontalDistanceSqr();
            double n = this.getDeltaMovement().horizontalDistanceSqr();
            if (m > 1.0E-4 && n < 0.01) {
                this.setDeltaMovement(this.getDeltaMovement().add(vec33.x * 0.1, 0.0, vec33.z * 0.1));
                bl2 = false;
            }
        }

        double o;
        if (bl2) {
            o = this.getDeltaMovement().horizontalDistance();
            if (o < 0.03) {
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.0, 0.5));
            }
        }

        o = (double)blockPos.getX() + 0.5 + (double)vec3i.getX() * 0.5;
        double p = (double)blockPos.getZ() + 0.5 + (double)vec3i.getZ() * 0.5;
        double q = (double)blockPos.getX() + 0.5 + (double)vec3i2.getX() * 0.5;
        double r = (double)blockPos.getZ() + 0.5 + (double)vec3i2.getZ() * 0.5;
        h = q - o;
        i = r - p;
        double s;
        double t;
        double u;
        if (h == 0.0) {
            s = f - (double)blockPos.getZ();
        } else if (i == 0.0) {
            s = d - (double)blockPos.getX();
        } else {
            t = d - o;
            u = f - p;
            s = (t * h + u * i) * 2.0;
        }

        d = o + h * s;
        f = p + i * s;
        this.setPos(d, e, f);
        t = this.isVehicle() ? 0.75 : 1.0;
        u = this.getMaxSpeed();
        vec32 = this.getDeltaMovement();
        this.move(MoverType.SELF, new Vec3(Mth.clamp(t * vec32.x, -u, u), 0.0, Mth.clamp(t * vec32.z, -u, u)));
        if (vec3i.getY() != 0 && Mth.floor(this.getX()) - blockPos.getX() == vec3i.getX() && Mth.floor(this.getZ()) - blockPos.getZ() == vec3i.getZ()) {
            this.setPos(this.getX(), this.getY() + (double)vec3i.getY(), this.getZ());
        } else if (vec3i2.getY() != 0 && Mth.floor(this.getX()) - blockPos.getX() == vec3i2.getX() && Mth.floor(this.getZ()) - blockPos.getZ() == vec3i2.getZ()) {
            this.setPos(this.getX(), this.getY() + (double)vec3i2.getY(), this.getZ());
        }

        this.applyNaturalSlowdown();
        Vec3 vec34 = this.getPos(this.getX(), this.getY(), this.getZ());
        Vec3 vec35;
        double w;
        if (vec34 != null && vec3 != null) {
            double v = (vec3.y - vec34.y) * 0.05;
            vec35 = this.getDeltaMovement();
            w = vec35.horizontalDistance();
            if (w > 0.0) {
                this.setDeltaMovement(vec35.multiply((w + v) / w, 1.0, (w + v) / w));
            }

            this.setPos(this.getX(), vec34.y, this.getZ());
        }

        int x = Mth.floor(this.getX());
        int y = Mth.floor(this.getZ());
        if (x != blockPos.getX() || y != blockPos.getZ()) {
            vec35 = this.getDeltaMovement();
            w = vec35.horizontalDistance();
            this.setDeltaMovement(w * (double)(x - blockPos.getX()), vec35.y, w * (double)(y - blockPos.getZ()));
        }

        if (bl) {
            vec35 = this.getDeltaMovement();
            double vec35x = vec35.x * 3;
            double vec35z = vec35.z * 3;
            w = vec35.horizontalDistance();
            if (w > 0.01) {
                double z = 0.06;
                this.setDeltaMovement(vec35.add((vec35x / w * 0.06), 0.0, (vec35z / w * 0.06)));
            } else {
                Vec3 vec36 = this.getDeltaMovement();
                double aa = vec36.x * 3;
                double ab = vec36.z * 3;
                if (railShape == RailShape.EAST_WEST) {
                    if (this.isRedstoneConductor(blockPos.west())) {
                        aa = 0.02;
                    } else if (this.isRedstoneConductor(blockPos.east())) {
                        aa = -0.02;
                    }
                } else {
                    if (railShape != RailShape.NORTH_SOUTH) {
                        return;
                    }

                    if (this.isRedstoneConductor(blockPos.north())) {
                        ab = 0.02;
                    } else if (this.isRedstoneConductor(blockPos.south())) {
                        ab = -0.02;
                    }
                }

                this.setDeltaMovement(aa, vec36.y, ab);
            }
        }
        ci.cancel();
    }

    @Shadow
    protected void applyNaturalSlowdown() {
        double d = this.isVehicle() ? 1.0 : 1.0;
        Vec3 vec3 = this.getDeltaMovement();
        vec3 = vec3.multiply(d, 0.0, d);
        if (this.isInWater()) {
            vec3 = vec3.scale(0.949999988079071);
        }

        this.setDeltaMovement(vec3);
    }
}
