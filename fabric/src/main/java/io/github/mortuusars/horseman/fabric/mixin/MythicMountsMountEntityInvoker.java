package io.github.mortuusars.horseman.fabric.mixin;

import com.yahoo.chirpycricket.mythicmounts.entity.MountEntity;
import net.minecraft.world.SimpleContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = MountEntity.class, remap = false)
public interface MythicMountsMountEntityInvoker {
    @Invoker("getInventorySize")
    public int invokeGetInventorySize();
    @Invoker("getItemInventory")
    public SimpleContainer invokeGetItemInventory();
    @Accessor("maxInventoryColumns")
    public int accessMaxInventoryColumns();
}
