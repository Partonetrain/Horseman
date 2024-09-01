package io.github.mortuusars.horseman.fabric.mixin;

import com.yahoo.chirpycricket.mythicmounts.entity.MountEntity;
import com.yahoo.chirpycricket.mythicmounts.screen.MountScreenHandler;
import io.github.mortuusars.horseman.Hitching;
import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MountScreenHandler.class, remap = false)
public abstract class MythicMountsMountInventoryMenuMixin extends AbstractContainerMenu {
    @Shadow
    @Final
    public Container inventory;
    @Shadow @Final public MountEntity entity;
    @Unique
    private boolean horseman$leadSlotAdded = false;

    protected MythicMountsMountInventoryMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Unique //reimplement hasChest with Horseman logic since MountScreenHandler does not extend HorseInventoryMenu
    private boolean hasChest(AbstractHorse horse) {
        if (!horseman$leadSlotAdded && Hitching.shouldHaveLeadSlot(horse)) {
            int leadSlotIndex = Hitching.getLeadSlotIndex(horse);
            addSlot(new Slot(this.inventory, leadSlotIndex, 8, 54) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return Hitching.mayPlaceInLeadSlot(horse, stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    ItemStack stack = this.getItem();
                    return !stack.is(Items.LEAD) || stack.getTag() == null || !stack.getTag().getBoolean(Hitching.PREVENT_LEAD_DROP_TAG);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean isActive() {
                    return Hitching.isLeadSlotActive(horse);
                }
            });
            horseman$leadSlotAdded = true;
        }

        MountEntity thisMount = (MountEntity)horse;
        return thisMount.hasChest();
    }

    /**
     * When Lead stack in player inventory is shift-clicked - splits only one item from clicked stack and inserts into Lead slot.
     * On Forge it's mostly a QOL thing (it will work without it, but not stop moving the rest of a stack in other slots),
     * but on Fabric without this mixin it's possible to insert 64 items into a slot with stack size of 1.
     * Fabric does not check slot limits when inserting.
     */
    @Inject(method = "quickMoveStack", at = @At(value = "HEAD"), cancellable = true)
    private void onQuickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (!Hitching.shouldHaveLeadSlot(this.entity) || index < this.inventory.getContainerSize()) {
            return;
        }

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return;
        }

        ItemStack clickedStack = slot.getItem();
        if (!clickedStack.is(Items.LEAD)) {
            return;
        }

        ItemStack clickedStackCopy = clickedStack.copy();

        Slot leadSlot = getSlot(2);
        if (!leadSlot.mayPlace(clickedStack)) {
            return;
        }

        ItemStack movedStack = clickedStack.copyWithCount(1);

        if (!leadSlot.getItem().isEmpty()) {
            if (!moveItemStackTo(clickedStack, 3, this.inventory.getContainerSize(), false)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
            else {
                if (clickedStack.isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }

                if (clickedStack.getCount() == clickedStackCopy.getCount()) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }

                slot.onTake(player, clickedStack);

                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        }

        clickedStack.shrink(1);
        this.slots.get(2).setByPlayer(movedStack);

        if (clickedStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (clickedStack.getCount() == clickedStackCopy.getCount()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        slot.onTake(player, clickedStack);

        cir.setReturnValue(ItemStack.EMPTY);
    }

    //redirect calls to MountEntity.hasChest to this mixin's hasChest()
    @Redirect(method = "init", at=@At(value = "INVOKE", target = "Lcom/yahoo/chirpycricket/mythicmounts/entity/MountEntity;hasChest()Z"))
    private boolean redirectHasChest(MountEntity instance){
        return hasChest(instance);
    }

}
