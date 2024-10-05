package wily.legacy.mixin.tweaks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import wily.legacy.Legacy4J;

@Mixin(CocoaBlock.class)
public abstract class CocoaBlockMixin extends HorizontalDirectionalBlock {

    protected CocoaBlockMixin(Properties properties) {
        super(properties);
    }

    /**
     * @author
     * Permdog99
     * @reason
     * Remove Cocoa bean placement
     */
    @Overwrite
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.relative((Direction)blockState.getValue(FACING)));
        return Legacy4J.isTU25() && blockState2.is(BlockTags.JUNGLE_LOGS);
    }
}
