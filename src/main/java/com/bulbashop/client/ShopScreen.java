package com.bulbashop.client;

import com.bulbashop.ClientState;
import com.bulbashop.PriceTable;
import com.bulbashop.network.ShopActionPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class ShopScreen extends Screen {
    private static final int CELL = 18;
    private static final int GOLD = 0xFFFFC107;

    private record Entry(ItemStack stack, String name, String id) {}

    private final Screen parent;
    private int tab = 0; // 0 = купить, 1 = продать
    private String query = "";
    private int scroll = 0;
    private final List<Entry> buyEntries = new ArrayList<>();
    private List<Entry> shown = new ArrayList<>();
    private EditBox search;
    private Button buyTab, sellTab, sellAll;
    private int px, py, pw, ph, cols, rows, gx, gy, tickCount;

    public ShopScreen(Screen parent) {
        super(Component.literal("Магазин"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        pw = Math.min(width - 10, 330);
        ph = Math.min(height - 10, 270);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        cols = Math.max(1, (pw - 12) / CELL);
        rows = Math.max(1, (ph - 58 - 18) / CELL);
        gx = px + 6 + ((pw - 12) - cols * CELL) / 2;
        gy = py + 58;

        buyEntries.clear();
        PriceTable table = ClientPrices.get();
        for (Item item : table.items()) {
            ItemStack st = item.getDefaultInstance();
            buyEntries.add(new Entry(st, st.getHoverName().getString(), BuiltInRegistries.ITEM.getKey(item).toString()));
        }
        buyEntries.sort(Comparator.comparing(e -> e.name().toLowerCase(Locale.ROOT)));

        buyTab = addRenderableWidget(Button.builder(Component.literal("Купить"), b -> setTab(0))
                .bounds(px + 6, py + 18, 60, 16).build());
        sellTab = addRenderableWidget(Button.builder(Component.literal("Продать"), b -> setTab(1))
                .bounds(px + 68, py + 18, 60, 16).build());
        sellAll = addRenderableWidget(Button.builder(Component.literal("Продать всё"),
                        b -> send(2, "", 1))
                .bounds(px + pw - 6 - 100, py + 18, 100, 16).build());

        search = new EditBox(font, px + 6, py + 38, pw - 12, 14, Component.literal("Поиск"));
        search.setHint(Component.literal("Поиск по названию..."));
        search.setValue(query);
        search.setResponder(s -> {
            query = s;
            scroll = 0;
            refilter();
        });
        addRenderableWidget(search);

        updateTabButtons();
        refilter();
    }

    private void setTab(int t) {
        tab = t;
        scroll = 0;
        updateTabButtons();
        refilter();
    }

    private void updateTabButtons() {
        buyTab.active = tab != 0;
        sellTab.active = tab != 1;
        sellAll.visible = tab == 1;
    }

    private void refilter() {
        List<Entry> src;
        PriceTable table = ClientPrices.get();
        if (tab == 0) {
            src = buyEntries;
        } else {
            src = new ArrayList<>();
            Map<Item, ItemStack> reps = new LinkedHashMap<>();
            Player pl = minecraft == null ? null : minecraft.player;
            if (pl != null) {
                for (ItemStack s : pl.getInventory().items) {
                    if (s.isEmpty() || !PriceTable.sellable(s) || table.sellPrice(s) <= 0) continue;
                    ItemStack rep = reps.get(s.getItem());
                    if (rep == null) reps.put(s.getItem(), s.copy());
                    else rep.grow(s.getCount());
                }
            }
            for (ItemStack rep : reps.values()) {
                src.add(new Entry(rep, rep.getHoverName().getString(), BuiltInRegistries.ITEM.getKey(rep.getItem()).toString()));
            }
            src.sort(Comparator.comparing(e -> e.name().toLowerCase(Locale.ROOT)));
        }
        String q = query.toLowerCase(Locale.ROOT).trim();
        if (q.isEmpty()) {
            shown = new ArrayList<>(src);
        } else {
            List<Entry> f = new ArrayList<>();
            for (Entry e : src) {
                if (e.name().toLowerCase(Locale.ROOT).contains(q) || e.id().contains(q)) f.add(e);
            }
            shown = f;
        }
        clampScroll();
    }

    private void clampScroll() {
        int totalRows = (shown.size() + cols - 1) / cols;
        int max = Math.max(0, totalRows - rows);
        scroll = Math.max(0, Math.min(scroll, max));
    }

    @Override
    public void tick() {
        super.tick();
        if (tab == 1 && ++tickCount % 10 == 0) refilter();
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        super.renderBackground(g, mx, my, pt);
        g.fill(px, py, px + pw, py + ph, 0xF0101018);
        g.renderOutline(px, py, pw, ph, GOLD);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);

        g.drawString(font, "Магазин", px + 6, py + 6, GOLD, false);
        String bal = "Баланс: " + ClientState.balance + " Б";
        g.drawString(font, bal, px + pw - 6 - font.width(bal), py + 6, 0xFFFFFFFF, false);

        PriceTable table = ClientPrices.get();
        Entry hovered = null;
        for (int i = 0; i < rows * cols; i++) {
            int idx = scroll * cols + i;
            if (idx >= shown.size()) break;
            int cx = gx + (i % cols) * CELL;
            int cy = gy + (i / cols) * CELL;
            Entry e = shown.get(idx);
            g.renderItem(e.stack(), cx + 1, cy + 1);
            if (tab == 1) g.renderItemDecorations(font, e.stack(), cx + 1, cy + 1);
            if (mx >= cx && mx < cx + CELL && my >= cy && my < cy + CELL) {
                g.fill(cx, cy, cx + CELL, cy + CELL, 0x55FFFFFF);
                hovered = e;
            }
        }

        String hint = "Найдено: " + shown.size() + "   ЛКМ - 1 шт., Shift+ЛКМ - " + (tab == 0 ? "стак" : "все");
        g.drawString(font, hint, px + 6, py + ph - 12, 0xFF999999, false);

        if (hovered != null) {
            List<Component> tip = new ArrayList<>();
            tip.add(hovered.stack().getHoverName());
            tip.add(Component.literal(hovered.id()).withStyle(ChatFormatting.DARK_GRAY));
            Item item = hovered.stack().getItem();
            if (tab == 0) {
                long price = table.buyPrice(item);
                tip.add(Component.literal("Цена: " + price + " Б")
                        .withStyle(ClientState.balance >= price ? ChatFormatting.GOLD : ChatFormatting.RED));
            } else {
                tip.add(Component.literal("Продажа: " + table.sellPrice(hovered.stack()) + " Б за шт.")
                        .withStyle(ChatFormatting.GOLD));
                tip.add(Component.literal("В инвентаре: " + hovered.stack().getCount())
                        .withStyle(ChatFormatting.GRAY));
            }
            g.renderComponentTooltip(font, tip, mx, my);
        }
    }

    private Entry entryAt(double mx, double my) {
        if (mx < gx || my < gy) return null;
        int col = (int) ((mx - gx) / CELL);
        int row = (int) ((my - gy) / CELL);
        if (col >= cols || row >= rows) return null;
        int idx = (scroll + row) * cols + col;
        return idx < shown.size() ? shown.get(idx) : null;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        if (button != 0) return false;
        Entry e = entryAt(mx, my);
        if (e == null) return false;
        boolean shift = hasShiftDown();
        if (tab == 0) {
            send(0, e.id(), shift ? e.stack().getMaxStackSize() : 1);
        } else {
            send(1, e.id(), shift ? 100000 : 1);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll -= (int) Math.signum(sy);
        clampScroll();
        return true;
    }

    private void send(int action, String id, int amount) {
        PacketDistributor.sendToServer(new ShopActionPayload(action, id, amount));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
