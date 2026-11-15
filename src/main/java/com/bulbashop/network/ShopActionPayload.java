package com.bulbashop.network;

import com.bulbashop.BulbaShop;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** action: 0 = купить, 1 = продать, 2 = продать всё продаваемое из инвентаря. */
public record ShopActionPayload(int action, String itemId, int amount) implements CustomPacketPayload {
    public static final Type<ShopActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BulbaShop.MODID, "action"));
    public static final StreamCodec<ByteBuf, ShopActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ShopActionPayload::action,
            ByteBufCodecs.STRING_UTF8, ShopActionPayload::itemId,
            ByteBufCodecs.VAR_INT, ShopActionPayload::amount,
            ShopActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
