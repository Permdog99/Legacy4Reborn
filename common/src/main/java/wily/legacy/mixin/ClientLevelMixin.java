package wily.legacy.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelTimeAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import wily.legacy.client.LegacyBiomeOverride;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Mixin(ClientLevel.class)
@Environment(EnvType.CLIENT)
public abstract class ClientLevelMixin extends Level {
    protected ClientLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, supplier, bl, bl2, l, i);
    }

    private ClientLevel self(){
        return (ClientLevel) (Object) this;
    }
    @Redirect(method = "calculateBlockTint",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ColorResolver;getColor(Lnet/minecraft/world/level/biome/Biome;DD)I"))
    public int calculateBlockTint(ColorResolver instance, Biome biome, double x, double z, BlockPos pos) {
        BlockPos.MutableBlockPos m = pos.mutable();
        LegacyBiomeOverride o = LegacyBiomeOverride.getOrDefault(self().getBiome(m.setX((int) x).setZ((int) z)).unwrapKey());
        return instance == BiomeColors.WATER_COLOR_RESOLVER && self().getFluidState(m).is(FluidTags.WATER) && o.waterColor() != null ? o.waterColor() : instance.getColor(biome,x,z);
    }

    /**
     * @author
     * Permdog99
     * @reason
     * Change when stars show up
     */
    @Overwrite
    public float getStarBrightness(float f) {
        float g = this.getTimeOfDay(f);
        float h = 1.0F - (Mth.cos(g * 6.2831855F) * 2.0F + 0.75F);
        h = Mth.clamp(h, 0.0F, 1.0F);
        return h * h * 0.5F;
    }
}
