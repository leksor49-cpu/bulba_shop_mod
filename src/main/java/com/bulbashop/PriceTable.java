package com.bulbashop;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.Tags;

import java.util.*;

/**
 * Таблица цен. Считается автоматически по рецептам игры (в том числе из модов):
 * цена вещи = сумма цен ингредиентов * наценка за крафт. Для "сырья" берутся базовые цены.
 */
public final class PriceTable {
    public static volatile PriceTable SERVER;
    public static volatile PriceTable CLIENT;

    private static final Set<String> EXCLUDED = Set.of(
            "minecraft:command_block", "minecraft:chain_command_block", "minecraft:repeating_command_block",
            "minecraft:command_block_minecart", "minecraft:structure_block", "minecraft:structure_void",
            "minecraft:jigsaw", "minecraft:barrier", "minecraft:light", "minecraft:debug_stick",
            "minecraft:bedrock", "minecraft:knowledge_book", "minecraft:spawner", "minecraft:trial_spawner",
            "minecraft:reinforced_deepslate", "minecraft:end_portal_frame", "minecraft:enchanted_book",
            "minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow",
            "minecraft:written_book", "minecraft:filled_map", "minecraft:player_head", "minecraft:suspicious_stew",
            "minecraft:test_block", "minecraft:test_instance_block", "minecraft:vault"
    );

    private final Map<Item, Long> buy = new HashMap<>();

    public static PriceTable server(MinecraftServer server) {
        PriceTable t = SERVER;
        if (t == null) {
            t = compute(server.getRecipeManager(), server.registryAccess());
            SERVER = t;
        }
        return t;
    }

    public boolean tradeable(Item item) { return buy.containsKey(item); }

    public Set<Item> items() { return buy.keySet(); }

    public long buyPrice(Item item) {
        Long v = buy.get(item);
        return v == null ? 0 : v;
    }

    public long sellPrice(ItemStack stack) {
        Long b = buy.get(stack.getItem());
        if (b == null) return 0;
        double v = b * ShopConfig.sellRatio();
        if (stack.isDamageableItem() && stack.getMaxDamage() > 0) {
            v *= 1.0 - (double) stack.getDamageValue() / stack.getMaxDamage();
        }
        long s = (long) Math.floor(v);
        if (s < 1 && !stack.isDamageableItem()) s = 1;
        return s;
    }

    /** Нельзя продавать контейнеры с содержимым (шалкеры, мешки) - чтобы не потерять вещи. */
    public static boolean sellable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ItemContainerContents c = stack.get(DataComponents.CONTAINER);
        if (c != null && c.nonEmptyItems().iterator().hasNext()) return false;
        BundleContents bc = stack.get(DataComponents.BUNDLE_CONTENTS);
        return bc == null || bc.isEmpty();
    }

    private static boolean excluded(Item item) {
        if (item == Items.AIR || item instanceof SpawnEggItem) return true;
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        if (EXCLUDED.contains(key.toString())) return true;
        return item.getDefaultInstance().isEmpty();
    }

    private static double rawPrice(Item item) {
        ItemStack s = item.getDefaultInstance();
        if (s.is(ItemTags.LOGS)) return 6;
        if (s.is(ItemTags.SAPLINGS)) return 6;
        if (s.is(ItemTags.FLOWERS)) return 3;
        if (s.is(ItemTags.LEAVES)) return 1;
        if (s.is(Tags.Items.ORES)) return 60;
        if (s.is(Tags.Items.GEMS)) return 60;
        if (s.is(Tags.Items.RAW_MATERIALS)) return 20;
        if (s.is(Tags.Items.INGOTS)) return 25;
        Rarity r = s.getRarity();
        return switch (r) {
            case COMMON -> 6;
            case UNCOMMON -> 40;
            case RARE -> 150;
            case EPIC -> 600;
        };
    }

    private record Rec(Item out, int count, List<Item[]> ings) {}

    public static PriceTable compute(RecipeManager rm, RegistryAccess access) {
        Map<Item, Double> fixed = new HashMap<>();
        DefaultPrices.fill(fixed);
        applyOverrides(fixed);
        double craftMarkup = ShopConfig.craftMarkup();

        List<Rec> recs = new ArrayList<>();
        Set<Item> produced = new HashSet<>();
        for (RecipeHolder<?> h : rm.getRecipes()) {
            try {
                Recipe<?> r = h.value();
                ItemStack res = r.getResultItem(access);
                if (res.isEmpty()) continue;
                List<Item[]> ings = new ArrayList<>();
                boolean bad = false;
                for (Ingredient ing : r.getIngredients()) {
                    if (ing.isEmpty()) continue;
                    Item[] arr = Arrays.stream(ing.getItems())
                            .filter(st -> !st.isEmpty()).map(ItemStack::getItem)
                            .distinct().toArray(Item[]::new);
                    if (arr.length == 0 || (arr.length == 1 && arr[0] == res.getItem())) { bad = true; break; }
                    ings.add(arr);
                }
                if (bad || ings.isEmpty()) continue;
                recs.add(new Rec(res.getItem(), res.getCount(), ings));
                produced.add(res.getItem());
            } catch (Exception ignored) {
                // кривой рецепт из мода - пропускаем
            }
        }

        Map<Item, Double> price = new HashMap<>(fixed);
        List<Item> all = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (excluded(item)) continue;
            all.add(item);
            if (price.containsKey(item)) continue;
            if (!produced.contains(item)) price.put(item, rawPrice(item));
        }

        for (int pass = 0; pass < 60; pass++) {
            boolean changed = false;
            for (Rec r : recs) {
                if (fixed.containsKey(r.out())) continue;
                double sum = 0;
                boolean ok = true;
                for (Item[] opts : r.ings()) {
                    double best = Double.MAX_VALUE;
                    for (Item o : opts) {
                        Double p = price.get(o);
                        if (p != null && p < best) best = p;
                    }
                    if (best == Double.MAX_VALUE) { ok = false; break; }
                    sum += best;
                }
                if (!ok) continue;
                double each = sum / r.count() * craftMarkup;
                Double cur = price.get(r.out());
                if (cur == null || each < cur - 1e-9) {
                    price.put(r.out(), each);
                    changed = true;
                }
            }
            if (!changed) break;
        }

        PriceTable t = new PriceTable();
        double markup = ShopConfig.buyMarkup();
        for (Item item : all) {
            double p = price.getOrDefault(item, rawPrice(item));
            t.buy.put(item, Math.max(1L, Math.round(p * markup)));
        }
        return t;
    }

    private static void applyOverrides(Map<Item, Double> fixed) {
        String raw = ShopConfig.overrides();
        if (raw == null || raw.isBlank()) return;
        for (String part : raw.split(",")) {
            String[] kv = part.trim().split("=");
            if (kv.length != 2) continue;
            try {
                ResourceLocation id = ResourceLocation.tryParse(kv[0].trim());
                if (id == null) continue;
                double v = Double.parseDouble(kv[1].trim());
                BuiltInRegistries.ITEM.getOptional(id).ifPresent(i -> fixed.put(i, v));
            } catch (NumberFormatException ignored) {
            }
        }
    }
}
