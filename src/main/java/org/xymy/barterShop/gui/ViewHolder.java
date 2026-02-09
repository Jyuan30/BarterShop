package org.xymy.barterShop.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xymy.barterShop.Shop;
import org.xymy.barterShop.ShopItems;
import org.xymy.barterShop.listeners.ShopUseListener;
import org.xymy.barterShop.listeners.ShopUseListener.ActionType;
import org.xymy.barterShop.util.ShopUtils;

public class ViewHolder implements ShopGui {
    public final Inventory inv;
    final Shop shop;
    ShopItems items;
    List<PlayerShopListHolder> playerShopListHolder;

    public ViewHolder(Shop shop, Player p, List<PlayerShopListHolder> playerShopListHolder) {
        this.playerShopListHolder = playerShopListHolder;
        this.shop = shop;
        Block chestBlock = shop.getChestLocation().getBlock();
        if (!(chestBlock.getState() instanceof Chest)) {
            ShopUtils.send(p, String.valueOf(ChatColor.RED) + "商店箱子不见了，无法查看！");
            this.inv = null;
        } else {
            Chest mainChest = (Chest)chestBlock.getState();
            Inventory chestInv = mainChest.getInventory();
            this.items = new ShopItems(new ArrayList(), new ArrayList());
            List<ItemStack> neededListWithNull = ShopUtils.firstRow(chestInv);
            List<ItemStack> givenListWithNull = ShopUtils.secondRow(chestInv);

            for(int i = 0; i < 9; ++i) {
                ItemStack needed = (ItemStack)neededListWithNull.get(i);
                ItemStack given = (ItemStack)givenListWithNull.get(i);
                if (needed != null) {
                    this.items.neededList.add(needed);
                }

                if (given != null) {
                    this.items.givenList.add(given);
                }
            }

            OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerId());
            String var10003 = String.valueOf(ChatColor.GOLD);
            this.inv = Bukkit.createInventory((InventoryHolder)null, 27, var10003 + (shop.isAdmin() ? "管理商店" : "玩家商店") + String.valueOf(ChatColor.RESET) + " - " + owner.getName());
            this.update();
        }
    }

    public Inventory getInventory() {
        return this.inv;
    }

    public void update() {
        if (this.inv != null) {
            this.inv.clear();
            int maxItems = 45;

            for(int i = 0; i < this.items.neededList.size(); ++i) {
                ItemStack neededDisplay = ((ItemStack)this.items.neededList.get(i)).clone();
                ItemMeta nm = neededDisplay.getItemMeta();
                List<String> neededLore = new ArrayList();
                if (nm.hasLore()) {
                    neededLore.addAll(nm.getLore());
                }

                neededLore.add(String.valueOf(ChatColor.YELLOW) + "--- 【你需要支付】 ---");
                String var10001 = String.valueOf(ChatColor.GRAY);
                neededLore.add(var10001 + "需要数量: " + neededDisplay.getAmount());
                nm.setLore(neededLore);
                neededDisplay.setItemMeta(nm);
                this.inv.setItem(i, neededDisplay);
            }

            if (!this.playerShopListHolder.isEmpty()) {
                for(int i = 0; i < 9; ++i) {
                    ItemStack neededDisplay = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
                    ItemMeta meta = neededDisplay.getItemMeta();
                    meta.setDisplayName(String.valueOf(ChatColor.YELLOW) + "点我返回");
                    neededDisplay.setItemMeta(meta);
                    this.inv.setItem(i + 9, neededDisplay);
                }
            } else {
                for(int i = 0; i < 9; ++i) {
                    ItemStack neededDisplay = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
                    ItemMeta meta = neededDisplay.getItemMeta();
                    meta.setDisplayName(String.valueOf(ChatColor.YELLOW) + "查看全球商店");
                    neededDisplay.setItemMeta(meta);
                    this.inv.setItem(i + 9, neededDisplay);
                }
            }

            for(int i = 0; i < this.items.givenList.size(); ++i) {
                ItemStack neededDisplay = ((ItemStack)this.items.givenList.get(i)).clone();
                ItemMeta nm = neededDisplay.getItemMeta();
                List<String> neededLore = new ArrayList();
                if (nm.hasLore()) {
                    neededLore.addAll(nm.getLore());
                }

                neededLore.add(String.valueOf(ChatColor.YELLOW) + "--- 【给你的物品】 ---");
                String var16 = String.valueOf(ChatColor.GRAY);
                neededLore.add(var16 + "给予数量: " + neededDisplay.getAmount());
                if (!this.shop.isAdmin()) {
                    var16 = String.valueOf(ChatColor.GREEN);
                    neededLore.add(var16 + "库存数量: " + this.getStockCount(neededDisplay));
                }

                nm.setLore(neededLore);
                neededDisplay.setItemMeta(nm);
                this.inv.setItem(i + 18, neededDisplay);
            }

        }
    }

    private int getStockCount(ItemStack pattern) {
        if (this.shop.isAdmin()) {
            return Integer.MAX_VALUE;
        } else {
            Block chestBlock = this.shop.getChestLocation().getBlock();
            if (!(chestBlock.getState() instanceof Chest)) {
                return 0;
            } else {
                Chest lowerChest = ShopUtils.getLowerChest(chestBlock);
                return lowerChest == null ? 0 : ShopUtils.countSimilar(lowerChest.getInventory(), pattern);
            }
        }
    }

    public void handleAction(Player p, int slot, ShopUseListener.ActionType type) {
        if (slot >= 9 && slot < 18) {
            if (type == ActionType.LEFT_CLICK) {
                if (this.playerShopListHolder.isEmpty()) {
                    p.performCommand("bartershop");
                } else {
                    PlayerShopListHolder gui = (PlayerShopListHolder)this.playerShopListHolder.get(0);
                    ShopUseListener.openPlayerShopViewGUI(p, gui.targetOwnerId, gui.page, gui.oldPage, gui.playerShops);
                }
            }

        }
    }
}
