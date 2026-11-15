package com.bulbashop.network;

import com.bulbashop.BulbaShop;
import com.bulbashop.PriceTable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Вся логика магазина выполняется на сервере - клиенту не доверяем. */
public final class ShopServer {
    private ShopServer() {}

    public static long balance(Player p) {
        return p.getData(BulbaShop.BALANCE.get());
    }

    public static void setBalance(ServerPlayer p, long value) {
        p.setData(BulbaShop.BALANCE.get(), Math.max(0, value));
        sync(p);
    }

    public static void sync(ServerPlayer p) {
        PacketDistributor.sendToPlayer(p, new BalancePayload(balance(p)));
    }

    public static void handle(ShopActionPayload pl, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer sp)) return;
        PriceTable table = PriceTable.server(sp.server);

        if (pl.action() == 2) {
            sell(sp, table, null, Integer.MAX_VALUE);
            return;
        }

        ResourceLocation id = ResourceLocation.tryParse(pl.itemId());
        if (id == null) return;
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null || item == Items.AIR || !table.tradeable(item)) return;

        if (pl.action() == 0) {
            buy(sp, table, item, Mth.clamp(pl.amount(), 1, 2304));
        } else if (pl.action() == 1) {
            sell(sp, table, item, Mth.clamp(pl.amount(), 1, 100000));
        }
    }

    private static void buy(ServerPlayer sp, PriceTable table, Item item, int amount) {
        long total = table.buyPrice(item) * amount;
        long bal = balance(sp);
        if (bal < total) {
            sp.displayClientMessage(Component.literal("Не хватает бульбакоинов! Нужно: " + total), true);
            return;
        }
        setBalance(sp, bal - total);
        int left = amount;
        while (left > 0) {
            int n = Math.min(left, item.getDefaultMaxStackSize());
            ItemStack s = new ItemStack(item, n);
            sp.getInventory().add(s);
            if (!s.isEmpty()) sp.drop(s, false);
            left -= n;
        }
        sp.inventoryMenu.broadcastChanges();
        sp.displayClientMessage(Component.literal("Куплено: " + amount + " шт. за " + total + " Б"), true);
    }

    private static void sell(ServerPlayer sp, PriceTable table, Item filter, int limit) {
        long coins = 0;
        int sold = 0;
        int remaining = limit;
        for (ItemStack s : sp.getInventory().items) {
            if (remaining <= 0) break;
            if (s.isEmpty() || (filter != null && !s.is(filter))) continue;
            if (!PriceTable.sellable(s)) continue;
            long unit = table.sellPrice(s);
            if (unit <= 0) continue;
            int n = Math.min(s.getCount(), remaining);
            coins += unit * n;
            sold += n;
            remaining -= n;
            s.shrink(n);
        }
        if (sold == 0) {
            sp.displayClientMessage(Component.literal("Нечего продавать"), true);
            return;
        }
        setBalance(sp, balance(sp) + coins);
        sp.inventoryMenu.broadcastChanges();
        sp.displayClientMessage(Component.literal("Продано: " + sold + " шт. за " + coins + " Б"), true);
    }
}
