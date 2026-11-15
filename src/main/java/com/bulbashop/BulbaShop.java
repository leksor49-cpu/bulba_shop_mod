package com.bulbashop;

import com.bulbashop.network.Net;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod(BulbaShop.MODID)
public class BulbaShop {
    public static final String MODID = "bulbashop";

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    /** Баланс бульбакоинов игрока (хранится в сохранении, переживает смерть). */
    public static final Supplier<AttachmentType<Long>> BALANCE = ATTACHMENTS.register("balance",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).copyOnDeath().build());

    public BulbaShop(IEventBus modBus, ModContainer container) {
        ATTACHMENTS.register(modBus);
        modBus.addListener(Net::register);
        container.registerConfig(ModConfig.Type.SERVER, ShopConfig.SPEC);
    }
}
