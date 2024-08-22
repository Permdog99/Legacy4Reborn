package wily.legacy.mixin.tweaks;

import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ShovelItem.class)
public class ShovelItemMixin extends DiggerItem {
    public ShovelItemMixin(float attackDamage, float attackSpeed, Tier material, TagKey<Block> effectiveBlocks, Properties settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
    }

    /**
     * @author
     * Permdog99
     * @reason
     * To Replicate old Beta
     */
    @Overwrite
    public @NotNull InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }
}
