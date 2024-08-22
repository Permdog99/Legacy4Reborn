package wily.legacy.mixin.tweaks;

import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SoulFireBlock.class)
public abstract class SoulFireBlockMixin extends BaseFireBlock {
    public SoulFireBlockMixin(Properties settings, float damage) {
        super(settings, damage);
    }

    /**
     * @author
     * Permdog99
     * @reason
     * To Replicate old Beta
     */
    @Overwrite
    public static boolean canSurviveOnBlock(BlockState state) {
        return false;
    }
}
