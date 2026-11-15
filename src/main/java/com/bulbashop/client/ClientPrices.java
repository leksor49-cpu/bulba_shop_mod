package com.bulbashop.client;

import com.bulbashop.PriceTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class ClientPrices {
    private ClientPrices() {}

    public static PriceTable get() {
        PriceTable t = PriceTable.CLIENT;
        if (t == null) {
            ClientLevel level = Minecraft.getInstance().level;
            t = PriceTable.compute(level.getRecipeManager(), level.registryAccess());
            PriceTable.CLIENT = t;
        }
        return t;
    }
}
