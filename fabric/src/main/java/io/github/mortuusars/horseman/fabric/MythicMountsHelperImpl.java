package io.github.mortuusars.horseman.fabric;

import com.yahoo.chirpycricket.mythicmounts.entity.MountEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class MythicMountsHelperImpl {

    public static boolean isMythicMount(AbstractHorse horse) {
        return horse instanceof MountEntity;
    }

    public static int getLeadSlotIndex(AbstractHorse horse) {
        MountEntity mythicMount = (MountEntity)horse;
        if (mythicMount.hasChest()) {
            int columns = mythicMount.getInventoryColumns();
            return 2 + columns * 3;
        } else {
            return 2;
        }
    }
}
