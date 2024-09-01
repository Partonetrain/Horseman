package io.github.mortuusars.horseman.fabric.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yahoo.chirpycricket.mythicmounts.screen.MountScreen;
import com.yahoo.chirpycricket.mythicmounts.screen.MountScreenHandler;
import io.github.mortuusars.horseman.Hitching;
import io.github.mortuusars.horseman.Horseman;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MountScreen.class)
public abstract class MythicMountsMountInventoryScreenMixin extends AbstractContainerScreen<MountScreenHandler> {
    @Shadow
    private AbstractHorse entity;
    @Unique
    private static final ResourceLocation LEAD_SLOT_TEXTURE = Horseman.resource("textures/gui/lead_slot.png");

    public MythicMountsMountInventoryScreenMixin(MountScreenHandler menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Hitching.shouldHaveLeadSlot(this.entity)) {
            for (Slot slot : getMenu().slots) {
                ItemStack stack = slot.getItem();
                if (stack.is(Items.LEAD) && stack.getTag() != null && stack.getTag().getBoolean(Hitching.PREVENT_LEAD_DROP_TAG)) {
                    int leftPos = (this.width - this.imageWidth) / 2;
                    int topPos = (this.height - this.imageHeight) / 2;
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    guiGraphics.blit(LEAD_SLOT_TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, 350, 0, 18, 18, 18, 256, 256);
                    RenderSystem.disableBlend();
                    break;
                }
            }
        }
    }

    @Inject(method = "renderBg", at = @At(value = "TAIL"))
    private void onRenderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (Hitching.shouldHaveLeadSlot(this.entity)) {
            int leftPos = (this.width - this.imageWidth) / 2;
            int topPos = (this.height - this.imageHeight) / 2;
            guiGraphics.blit(LEAD_SLOT_TEXTURE, leftPos + 7, topPos + 53, 0, 0, 18, 18);
        }
    }

}
