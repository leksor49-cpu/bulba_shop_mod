package com.bulbashop;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ShopConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.DoubleValue BUY_MARKUP;
    private static final ModConfigSpec.DoubleValue SELL_RATIO;
    private static final ModConfigSpec.DoubleValue CRAFT_MARKUP;
    private static final ModConfigSpec.ConfigValue<String> OVERRIDES;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        BUY_MARKUP = b.comment("Общий множитель всех цен покупки (1.0 = как посчитано)")
                .defineInRange("buyMarkup", 1.0, 0.05, 1000.0);
        SELL_RATIO = b.comment("Какую часть цены ты получаешь при продаже (0.5 = 50%)")
                .defineInRange("sellRatio", 0.5, 0.01, 1.0);
        CRAFT_MARKUP = b.comment("Наценка за крафт: цена вещи = сумма ингредиентов * это число")
                .defineInRange("craftMarkup", 1.1, 1.0, 10.0);
        OVERRIDES = b.comment("Ручные цены. Формат: id=цена, через запятую. Пример: minecraft:diamond=250,create:brass_ingot=40")
                .define("priceOverrides", "");
        SPEC = b.build();
    }

    private ShopConfig() {}

    public static double buyMarkup() { try { return BUY_MARKUP.get(); } catch (Exception e) { return 1.0; } }
    public static double sellRatio() { try { return SELL_RATIO.get(); } catch (Exception e) { return 0.5; } }
    public static double craftMarkup() { try { return CRAFT_MARKUP.get(); } catch (Exception e) { return 1.1; } }
    public static String overrides() { try { return OVERRIDES.get(); } catch (Exception e) { return ""; } }
}
