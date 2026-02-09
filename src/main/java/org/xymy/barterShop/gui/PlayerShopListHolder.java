package org.xymy.barterShop.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.xymy.barterShop.Shop;
import org.xymy.barterShop.listeners.ShopUseListener;
import org.xymy.barterShop.listeners.ShopUseListener.ActionType;
import org.xymy.barterShop.util.ShopUtils;

public class PlayerShopListHolder implements ShopGui {
    final Inventory inv;
    final List<Shop> playerShops;
    final int page;
    final int maxPages;
    final UUID targetOwnerId;
    final int oldPage;
    private boolean isUpdated = false;

    public PlayerShopListHolder(Player p, UUID targetOwnerId, int page, int oldPage, List<Shop> playerShops) {
        this.targetOwnerId = targetOwnerId;
        this.playerShops = playerShops;
        this.oldPage = oldPage;
        this.maxPages = Math.max(1, (int)Math.ceil((double)playerShops.size() / (double)45.0F));
        this.page = Math.max(0, Math.min(page, this.maxPages - 1));
        String title = "玩家商店列表";
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

            for(int i = start; i < Math.min(start + 45, this.playerShops.size()); ++i) {
                Shop shop = (Shop)this.playerShops.get(i);
                ItemStack nowItem = new ItemStack(Material.CHEST);
                ItemMeta meta = nowItem.getItemMeta();
                String playerName = ShopUtils.ownerNameOrUuid(shop.getOwnerId());
                String var10001 = String.valueOf(ChatColor.GREEN);
                meta.setDisplayName(var10001 + (shop.isAdmin() ? "[管理商店]" : "[玩家商店]") + String.valueOf(ChatColor.YELLOW) + " by " + playerName);
                List<String> lore = new ArrayList();
                Location loc = shop.getSignLocation();
                var10001 = String.valueOf(ChatColor.GRAY);
                lore.add(var10001 + "世界: " + loc.getWorld().getName());
                var10001 = String.valueOf(ChatColor.GRAY);
                lore.add(var10001 + "坐标: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                lore.add(String.valueOf(ChatColor.AQUA) + "右键: 传送前往");
                lore.add(String.valueOf(ChatColor.AQUA) + "左键: 查看交易");
                meta.setLore(lore);
                nowItem.setItemMeta(meta);
                this.inv.setItem(i % 45, nowItem);
            }

            this.setPaginationButtons();
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta barrierMeta = barrier.getItemMeta();
            barrierMeta.setDisplayName(String.valueOf(ChatColor.RED) + "返回");
            barrier.setItemMeta(barrierMeta);
            this.inv.setItem(49, barrier);
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

    private Location pickSafeTeleport(Location baseCenter) {
        World w = baseCenter.getWorld();
        Block base = w.getBlockAt(baseCenter);
        Block above = base.getRelative(BlockFace.UP);
        Block above2 = above.getRelative(BlockFace.UP);
        Location planA = base.getLocation().add((double)0.5F, base.isPassable() ? (double)0.0F : (double)1.0F, (double)0.5F);
        return above.isPassable() && above2.isPassable() ? planA : base.getLocation().add((double)0.5F, (double)2.0F, (double)0.5F);
    }

    private void teleportToShopFront(Player p, Shop shop) {
        Location loc = shop.getSignLocation();
        if (!loc.isWorldLoaded()) {
            ShopUtils.send(p, String.valueOf(ChatColor.RED) + "世界未加载，无法传送。");
        } else {
            World w = loc.getWorld();
            Block signBlock = w.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            if (!(signBlock.getState() instanceof Sign)) {
                ShopUtils.send(p, String.valueOf(ChatColor.RED) + "该商店的牌子不存在了。");
            } else {
                BlockData data = signBlock.getBlockData();
                BlockFace face = BlockFace.SOUTH;
                if (data instanceof Directional) {
                    face = ((Directional)data).getFacing();
                }

                Block front = signBlock.getRelative(face);
                Location target = this.pickSafeTeleport(front.getLocation().add((double)0.5F, (double)0.0F, (double)0.5F));
                Vector dir = signBlock.getLocation().add((double)0.5F, (double)0.0F, (double)0.5F).toVector().subtract(target.toVector()).normalize();
                target.setDirection(dir);
                p.teleport(target);
                ShopUtils.send(p, String.valueOf(ChatColor.GREEN) + "已传送到商店。");
            }
        }
    }

    public void handleAction(Player p, int slot, ShopUseListener.ActionType type) {
        if (slot >= 0 && slot < 54) {
            if (slot == 45) {
                if (this.page > 0) {
                    ShopUseListener.openPlayerShopViewGUI(p, this.targetOwnerId, this.page - 1, this.oldPage, this.playerShops);
                }
            } else if (slot == 53) {
                if (this.page < this.maxPages - 1) {
                    ShopUseListener.openPlayerShopViewGUI(p, this.targetOwnerId, this.page + 1, this.oldPage, this.playerShops);
                }
            } else if (slot == 49) {
                ShopUseListener.openOwnerListGUI(p, this.targetOwnerId, this.oldPage);
            } else if (slot >= 0 && slot < 45) {
                int listIndex = this.page * 45 + slot;
                if (listIndex >= this.playerShops.size()) {
                    return;
                }

                Shop shop = (Shop)this.playerShops.get(listIndex);
                if (type == ActionType.RIGHT_CLICK) {
                    p.closeInventory();
                    this.teleportToShopFront(p, shop);
                } else if (type == ActionType.LEFT_CLICK) {
                    p.closeInventory();
                    ShopUseListener.openShopViewGUI(p, shop, true);
                }
            }

        }
    }
}
