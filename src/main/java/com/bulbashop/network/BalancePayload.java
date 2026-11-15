package com.bulbashop.network;

import com.bulbashop.BulbaShop;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BalancePayload(long balance) implements CustomPacketPayload {
    public static final Type<BalancePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BulbaShop.MODID, "balance"));
    public static final StreamCodec<ByteBuf, BalancePayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_LONG, BalancePayload::balance, BalancePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
