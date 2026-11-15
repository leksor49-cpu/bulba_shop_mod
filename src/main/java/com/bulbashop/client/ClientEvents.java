package com.bulbashop.client;

import com.bulbashop.BulbaShop;
import com.bulbashop.ClientState;
import com.bulbashop.PriceTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@EventBusSubscriber(modid = BulbaShop.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    /** Кнопка "Магазин" над окном инвентаря. */
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post e) {
        if (e.getScreen() instanceof InventoryScreen inv) {
            int x = inv.getGuiLeft();
            int y = Math.max(2, inv.getGuiTop() - 22);
            e.addListener(Button.builder(Component.literal("Магазин"),
                            b -> Minecraft.getInstance().setScreen(new ShopScreen(inv)))
                    .bounds(x, y, 70, 20).build());
        }
    }

    @SubscribeEvent
    public static void onTags(TagsUpdatedEvent e) {
        if (e.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            PriceTable.CLIENT = null;
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientState.balance = 0;
        PriceTable.CLIENT = null;
    }
}
