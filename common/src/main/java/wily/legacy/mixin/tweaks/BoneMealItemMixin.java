package wily.legacy.mixin.tweaks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin extends Item {

    public BoneMealItemMixin(Properties settings) {
        super(settings);
    }


    /**
     * @author
     * Permdog99
     * @reason
     * To Replicate old Beta
     */
    @Overwrite()
    public static boolean growWaterPlant(ItemStack stack, Level world, BlockPos blockPos, @Nullable Direction facing) {
        return false;
    }
}
