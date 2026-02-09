package org.xymy.barterShop.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.xymy.barterShop.BarterShop;
import org.xymy.barterShop.Shop;
import org.xymy.barterShop.ShopManager;
import org.xymy.barterShop.listeners.ShopUseListener;
import org.xymy.barterShop.listeners.ShopUseListener.ActionType;

public class ShopListHolder implements ShopGui {
    final Inventory inv;
    final Map<UUID, List<Shop>> groupedShops;
    final int page;
    final int maxPages;
    private final UUID targetOwnerId;
    private boolean isUpdated = false;

    public ShopListHolder(Player p, UUID targetOwnerId, int page, ShopManager shopManager) {
        this.targetOwnerId = targetOwnerId;
        this.groupedShops = new HashMap();

        for(Shop shop : shopManager.allShops()) {
            UUID ownerId = shop.isAdmin() ? BarterShop.ADMIN_GROUP_UUID : shop.getOwnerId();
            ((List)this.groupedShops.computeIfAbsent(ownerId, (k) -> new ArrayList())).add(shop);
        }

        this.maxPages = Math.max(1, (int)Math.ceil((double)this.groupedShops.size() / (double)45.0F));
        this.page = Math.max(0, Math.min(page, this.maxPages - 1));
        String title = "所有商店列表";
        String var10003 = String.valueOf(ChatColor.GOLD);
        this.inv = Bukkit.createInventory((InventoryHolder)null, 54, var10003 + title + String.valueOf(ChatColor.RESET) + " (第" + (this.page + 1) + "/" + this.maxPages + "页)");
        this.update();
    }

    public Inventory getInventory() {
        return this.inv;
    }

    public void update() {
        if (!this.isUpdated) {
            this.inv.clear();
            int start = this.page * 45;
            List<Shop> allShops = new ArrayList();

            for(List<Shop> shopList : this.groupedShops.values()) {
                allShops.addAll(shopList);
            }

            int i = 0;

            for(UUID ownerId : this.groupedShops.keySet()) {
                if (i >= start && i < start + 45) {
                    List<Shop> ownerShops = (List)this.groupedShops.get(ownerId);
                    ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta meta = playerHead.getItemMeta();
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerId);
                    SkullMeta skullMeta = (SkullMeta)meta;
                    String displayName = ((Shop)ownerShops.get(0)).isAdmin() ? String.valueOf(ChatColor.LIGHT_PURPLE) + "系统商店" : String.valueOf(ChatColor.GOLD) + owner.getName();
                    skullMeta.setDisplayName(displayName);
                    skullMeta.setOwner(owner.getName());
                    List<String> lore = new ArrayList();
                    String var10001 = String.valueOf(ChatColor.GRAY);
                    lore.add(var10001 + "商店数量: " + String.valueOf(ChatColor.WHITE) + ownerShops.size());
                    lore.add(String.valueOf(ChatColor.GREEN) + "左键: 查看所有商店");
                    skullMeta.setLore(lore);
                    playerHead.setItemMeta(skullMeta);
                    if (((Shop)ownerShops.get(0)).isAdmin()) {
                        playerHead.setType(Material.NETHER_STAR);
                    }

                    this.inv.setItem(i % 45, playerHead);
                    ++i;
                }
            }

            this.setPaginationButtons();
            this.isUpdated = true;
        }

    }

    private void setPaginationButtons() {
        ItemStack prevPageItem = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevPageItem.getItemMeta();
        prevMeta.setDisplayName(String.valueOf(ChatColor.AQUA) + "上一页");
        prevPageItem.setItemMeta(prevMeta);
        this.inv.setItem(45, prevPageItem);
        ItemStack nextPageItem = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextPageItem.getItemMeta();
        nextMeta.setDisplayName(String.valueOf(ChatColor.AQUA) + "下一页");
        nextPageItem.setItemMeta(nextMeta);
        this.inv.setItem(53, nextPageItem);
        if (this.page == 0) {
            this.inv.setItem(45, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }

        if (this.page == this.maxPages - 1) {
            this.inv.setItem(53, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }

    }

    public void handleAction(Player p, int slot, ShopUseListener.ActionType type) {
        if (slot >= 0 && slot < 54) {
            if (slot == 45) {
                if (this.page > 0) {
                    ShopUseListener.openOwnerListGUI(p, this.targetOwnerId, this.page - 1);
                }
            } else if (slot == 53) {
                if (this.page < this.maxPages - 1) {
                    ShopUseListener.openOwnerListGUI(p, this.targetOwnerId, this.page + 1);
                }
            } else if (slot >= 0 && slot < 45) {
                int listIndex = this.page * 45 + slot;
                List<Shop> allShops = new ArrayList();

                for(List<Shop> shopList : this.groupedShops.values()) {
                    allShops.addAll(shopList);
                }

                if (listIndex >= this.groupedShops.keySet().toArray().length) {
                    return;
                }

                UUID ownerId = (UUID)this.groupedShops.keySet().toArray()[listIndex];
                if (type == ActionType.LEFT_CLICK) {
                    this.openOwnerShopsGUI(p, ownerId, (List)this.groupedShops.get(ownerId));
                } else if (type == ActionType.RIGHT_CLICK) {
                }
            }

        }
    }

    private void openOwnerShopsGUI(Player p, UUID ownerId, List<Shop> shopList) {
        ShopUseListener.openPlayerShopViewGUI(p, ownerId, 0, this.page, shopList);
    }
}
