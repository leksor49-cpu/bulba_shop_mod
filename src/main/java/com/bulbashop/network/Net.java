package com.bulbashop.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class Net {
    private Net() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("1");
        r.playToServer(ShopActionPayload.TYPE, ShopActionPayload.CODEC, ShopServer::handle);
        r.playToClient(BalancePayload.TYPE, BalancePayload.CODEC, ClientHandlers::onBalance);
    }
}
