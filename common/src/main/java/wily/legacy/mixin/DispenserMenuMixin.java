package wily.legacy.mixin;

import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import wily.legacy.Legacy4JClient;
import wily.legacy.inventory.LegacySlotDisplay;

@Mixin(DispenserMenu.class)
public abstract class DispenserMenuMixin {

    @ModifyArg(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V",at = @At(value = "INVOKE",target = "Lnet/minecraft/world/inventory/DispenserMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;", ordinal = 0))
    private Slot addSlotFirst(Slot originalSlot){
        return LegacySlotDisplay.override(originalSlot, 77 + originalSlot.getContainerSlot() % 3 * 21,26 + originalSlot.getContainerSlot() / 3 * 21, new LegacySlotDisplay() {
            @Override
            public IconHolderOverride getIconHolderOverride() {
                return Legacy4JClient.ICON_HOLDER_360;
            }
        });
    }
    @ModifyArg(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V",at = @At(value = "INVOKE",target = "Lnet/minecraft/world/inventory/DispenserMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;", ordinal = 1))
    private Slot addInventorySlots(Slot originalSlot){
        return LegacySlotDisplay.override(originalSlot,14 + (originalSlot.getContainerSlot() - 9) % 9 * 21,107 + (originalSlot.getContainerSlot() - 9) / 9 * 21, new LegacySlotDisplay() {
            @Override
            public IconHolderOverride getIconHolderOverride() {
                return Legacy4JClient.ICON_HOLDER_360;
            }
        });
    }
    @ModifyArg(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V",at = @At(value = "INVOKE",target = "Lnet/minecraft/world/inventory/DispenserMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;", ordinal = 2))
    private Slot addHotbarSlots(Slot originalSlot){
        return LegacySlotDisplay.override(originalSlot,14 + originalSlot.getContainerSlot() * 21,177, new LegacySlotDisplay() {
            @Override
            public IconHolderOverride getIconHolderOverride() {
                return Legacy4JClient.ICON_HOLDER_360;
            }
        });
    }
}