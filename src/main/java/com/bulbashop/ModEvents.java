package com.bulbashop;

import com.bulbashop.network.ShopServer;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = BulbaShop.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class ModEvents {
    private ModEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) ShopServer.sync(sp);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) ShopServer.sync(sp);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent e) {
        try {
            PriceTable.SERVER = PriceTable.compute(e.getServer().getRecipeManager(), e.getServer().registryAccess());
        } catch (Exception ex) {
            PriceTable.SERVER = null; // посчитается позже при первой покупке/продаже
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent e) {
        PriceTable.SERVER = null;
    }

    @SubscribeEvent
    public static void onTags(TagsUpdatedEvent e) {
        if (e.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            PriceTable.SERVER = null;
        }
    }

    /** /bulba add <число> - выдать себе монет (нужны права оператора), /bulba balance - узнать баланс */
    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("bulba")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("add")
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    long a = LongArgumentType.getLong(ctx, "amount");
                                    ShopServer.setBalance(p, ShopServer.balance(p) + a);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Выдано " + a + " бульбакоинов"), false);
                                    return 1;
                                })))
                .then(Commands.literal("balance")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            long b = ShopServer.balance(p);
                            ctx.getSource().sendSuccess(() -> Component.literal("Баланс: " + b + " бульбакоинов"), false);
                            return 1;
                        })));
    }
}
