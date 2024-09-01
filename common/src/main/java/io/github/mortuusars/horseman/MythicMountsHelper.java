package io.github.mortuusars.horseman;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class MythicMountsHelper {
    @ExpectPlatform
    public static boolean isMythicMount(AbstractHorse horse) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static int getLeadSlotIndex(AbstractHorse horse) {
        throw new AssertionError();
    }
}
