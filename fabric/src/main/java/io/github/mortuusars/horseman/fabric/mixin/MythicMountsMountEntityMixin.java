package io.github.mortuusars.horseman.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.yahoo.chirpycricket.mythicmounts.entity.MountEntity;
import com.yahoo.chirpycricket.mythicmounts.registery.Entities;
import io.github.mortuusars.horseman.Hitching;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MountEntity.class, remap = false)
public class MythicMountsMountEntityMixin {

    //since hasChest is returning true for some reason and thisMount.setHasChest doesn't work to prevent that,
    //this mixin ensures that newly spawned mounts won't return true
    @Inject(method = "hasChest", at=@At("HEAD"), cancellable = true)
    public void onHasChest(CallbackInfoReturnable<Boolean> cir){
        MountEntity thisMount = (MountEntity) (Object)this;
        if(thisMount.mountAnimationManager == null){
            cir.setReturnValue(false);
        }
    }

    //MythicMounts multiplies columns by 6 here even though there's only 3 slots per column, so completely ignore original value
    @ModifyReturnValue(method = "getInventorySize", at = @At("RETURN"))
    private int onGetInventorySize(int original) {
        MountEntity thisMount = (MountEntity) (Object)this;
        int actualSize = 3 * thisMount.getInventoryColumns() + 2; //getInventoryColumns uses configured value per mount
        return Hitching.shouldHaveLeadSlot(thisMount) ? actualSize + 1 : actualSize; //add 1 for lead slot
    }

    //similarly getContainerSize adds 3 to the inventory even though by default there's only 2 slots
    @ModifyReturnValue(method = "getContainerSize", at = @At("RETURN"))
    private int onGetContainerSize(int original) {
        MountEntity thisMount = (MountEntity) (Object)this;
        int invSize = thisMount.inventory.getContainerSize() + 2;
        return Hitching.shouldHaveLeadSlot(thisMount) ? invSize + 1 : invSize;
    }

    //move lead to correct slot after chest added hackery
    @Inject(method = "createInventory", at = @At(value = "HEAD"))
    private void onCreateInventory(CallbackInfo ci) {
        MountEntity thisMount = (MountEntity) (Object)this;
        MythicMountsMountEntityInvoker invoker = (MythicMountsMountEntityInvoker)thisMount;
        SimpleContainer thisInventory = invoker.invokeGetItemInventory();

        if (thisMount.hasChest() && Hitching.shouldHaveLeadSlot(thisMount)) {
            @Nullable SimpleContainer prevInventory = thisInventory;
            thisInventory = new SimpleContainer(invoker.invokeGetInventorySize()); //new empty inventory
            if (prevInventory != null) {
                prevInventory.removeListener(thisMount);
                int slots = Math.min(prevInventory.getContainerSize(), thisInventory.getContainerSize());
                for (int i = 0; i < slots; ++i) {
                    ItemStack itemStack = prevInventory.getItem(i);
                    if (itemStack.isEmpty()) continue;
                    thisInventory.setItem(i, itemStack.copy());
                }

                // Swap lead that's now in the wrong slot to last inventory slot.
                if (thisInventory.getItem(2).is(Items.LEAD)) {
                    ItemStack lastItem = thisInventory.getItem(thisInventory.getContainerSize() - 1);
                    thisInventory.setItem(thisInventory.getContainerSize() - 1, thisInventory.getItem(2));
                    thisInventory.setItem(2, lastItem);
                }
            }
        }
    }

}
