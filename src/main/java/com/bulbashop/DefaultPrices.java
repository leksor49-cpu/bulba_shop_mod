package com.bulbashop;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Map;

/** Базовые цены "сырья" - от них считаются цены всего остального по рецептам. */
final class DefaultPrices {
    private DefaultPrices() {}

    static void fill(Map<Item, Double> m) {
        // земля и камень
        p(m, "cobblestone", 1); p(m, "cobbled_deepslate", 1); p(m, "dirt", 1); p(m, "sand", 1);
        p(m, "red_sand", 1); p(m, "gravel", 1); p(m, "netherrack", 1); p(m, "soul_sand", 3);
        p(m, "soul_soil", 3); p(m, "end_stone", 2); p(m, "obsidian", 30); p(m, "clay_ball", 3);
        p(m, "snowball", 1); p(m, "ice", 3); p(m, "granite", 2); p(m, "diorite", 2);
        p(m, "andesite", 2); p(m, "tuff", 2); p(m, "calcite", 3); p(m, "dripstone_block", 2);
        p(m, "blackstone", 2); p(m, "basalt", 2); p(m, "mud", 1); p(m, "moss_block", 4);
        // растения и еда
        p(m, "bamboo", 2); p(m, "cactus", 2); p(m, "sugar_cane", 3); p(m, "kelp", 2);
        p(m, "wheat", 4); p(m, "wheat_seeds", 1); p(m, "beetroot", 4); p(m, "beetroot_seeds", 1);
        p(m, "carrot", 4); p(m, "potato", 4); p(m, "apple", 6); p(m, "melon_slice", 2);
        p(m, "pumpkin", 6); p(m, "sweet_berries", 3); p(m, "glow_berries", 4); p(m, "cocoa_beans", 6);
        p(m, "honey_bottle", 20); p(m, "honeycomb", 12); p(m, "brown_mushroom", 3);
        p(m, "red_mushroom", 3); p(m, "sea_pickle", 4); p(m, "lily_pad", 3); p(m, "vine", 2);
        p(m, "chorus_fruit", 8); p(m, "beef", 5); p(m, "porkchop", 5); p(m, "chicken", 4);
        p(m, "mutton", 4); p(m, "rabbit", 4); p(m, "cod", 3); p(m, "salmon", 3);
        p(m, "tropical_fish", 4); p(m, "pufferfish", 6); p(m, "egg", 3);
        p(m, "milk_bucket", 25); p(m, "water_bucket", 20); p(m, "lava_bucket", 30);
        p(m, "powder_snow_bucket", 20);
        // дропы мобов
        p(m, "string", 5); p(m, "bone", 5); p(m, "rotten_flesh", 2); p(m, "spider_eye", 6);
        p(m, "gunpowder", 12); p(m, "feather", 3); p(m, "leather", 8); p(m, "rabbit_hide", 4);
        p(m, "rabbit_foot", 40); p(m, "ink_sac", 6); p(m, "glow_ink_sac", 14); p(m, "slime_ball", 15);
        p(m, "ender_pearl", 60); p(m, "blaze_rod", 50); p(m, "ghast_tear", 120);
        p(m, "phantom_membrane", 40); p(m, "prismarine_shard", 8); p(m, "prismarine_crystals", 10);
        p(m, "nautilus_shell", 150); p(m, "scute", 100); p(m, "shulker_shell", 150);
        p(m, "breeze_rod", 60); p(m, "flint", 3);
        // редкое
        p(m, "wither_skeleton_skull", 300); p(m, "nether_star", 3000); p(m, "dragon_egg", 5000);
        p(m, "dragon_head", 800); p(m, "elytra", 3000); p(m, "heart_of_the_sea", 600);
        p(m, "echo_shard", 150); p(m, "totem_of_undying", 600); p(m, "experience_bottle", 80);
        // руды и ресурсы
        p(m, "coal", 12); p(m, "raw_iron", 15); p(m, "raw_copper", 8); p(m, "raw_gold", 35);
        p(m, "diamond", 200); p(m, "emerald", 120); p(m, "lapis_lazuli", 12); p(m, "redstone", 8);
        p(m, "quartz", 15); p(m, "amethyst_shard", 15); p(m, "netherite_scrap", 400);
        p(m, "ancient_debris", 450); p(m, "glowstone_dust", 10);
        for (String d : new String[]{"", "deepslate_"}) {
            p(m, d + "iron_ore", 15); p(m, d + "copper_ore", 8); p(m, d + "gold_ore", 35);
            p(m, d + "coal_ore", 12); p(m, d + "redstone_ore", 8); p(m, d + "lapis_ore", 12);
            p(m, d + "diamond_ore", 200); p(m, d + "emerald_ore", 120);
        }
        p(m, "nether_gold_ore", 35); p(m, "nether_quartz_ore", 15);
    }

    private static void p(Map<Item, Double> m, String id, double price) {
        ResourceLocation rl = id.contains(":") ? ResourceLocation.parse(id) : ResourceLocation.withDefaultNamespace(id);
        BuiltInRegistries.ITEM.getOptional(rl).ifPresent(i -> {
            if (i != Items.AIR) m.put(i, price);
        });
    }
}
