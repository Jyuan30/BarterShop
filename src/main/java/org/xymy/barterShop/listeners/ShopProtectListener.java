package org.xymy.barterShop.listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.xymy.barterShop.BarterShop;
import org.xymy.barterShop.Shop;
import org.xymy.barterShop.ShopManager;
import org.xymy.barterShop.util.ShopUtils;

public class ShopProtectListener implements Listener {
    private final ShopManager shopManager;

    public ShopProtectListener(BarterShop plugin) {
        this.shopManager = plugin.getShopManager();
    }

    private boolean checkProtection(Player p, Block block) {
        Shop shop = null;
        if (ShopUtils.isAnyShopSign(block)) {
            shop = this.shopManager.findBySign(block.getLocation());
        }

        if (shop == null && block.getState() instanceof Chest) {
            shop = this.shopManager.findByChest(block.getLocation());
        }

        if (shop != null) {
            if (ShopUtils.isAnyShopSign(block)) {
                return true;
            } else if (p.hasPermission("bartershop.admin.break")) {
                this.shopManager.delete(shop);
                ShopUtils.send(p, String.valueOf(ChatColor.YELLOW) + "成功移除商店！");
                return false;
            } else if (!p.getUniqueId().equals(shop.getOwnerId())) {
                ShopUtils.send(p, String.valueOf(ChatColor.RED) + "你不能破坏别人的商店！");
                return true;
            } else {
                this.shopManager.delete(shop);
                ShopUtils.send(p, String.valueOf(ChatColor.YELLOW) + "成功移除商店！");
                return false;
            }
        } else {
            return false;
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (this.checkProtection(e.getPlayer(), e.getBlock())) {
            e.setCancelled(true);
        }

    }
}
