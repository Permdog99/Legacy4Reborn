package wily.legacy.mixin.tweaks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TallGrassBlock.class)
public abstract class ShortPlantBlockMixin extends BushBlock implements BonemealableBlock {
    public ShortPlantBlockMixin(Properties settings) {
        super(settings);
    }

    /**
     * @author
     * Permdog99
     * @reason
     * To Replicate old Beta
     */
    @Overwrite()
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return false;
    }
}
