package com.bulbashop.network;

import com.bulbashop.ClientState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientHandlers {
    private ClientHandlers() {}

    public static void onBalance(BalancePayload payload, IPayloadContext ctx) {
        ClientState.balance = payload.balance();
    }
}
