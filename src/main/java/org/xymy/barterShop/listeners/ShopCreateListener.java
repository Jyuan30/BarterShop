package org.xymy.barterShop.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.xymy.barterShop.BarterShop;
import org.xymy.barterShop.ShopManager;
import org.xymy.barterShop.util.ShopUtils;

public class ShopCreateListener implements Listener {
    private final ShopManager shopManager;

    public ShopCreateListener(BarterShop plugin) {
        this.shopManager = plugin.getShopManager();
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        Player p = e.getPlayer();
        String l0raw = e.getLine(0) == null ? "" : e.getLine(0).trim();
        boolean admin = ShopUtils.isAdminLine(l0raw);
        if ("[shop]".equalsIgnoreCase(l0raw) || admin) {
            Block signBlock = e.getBlock();
            Block attached = ShopUtils.getAttachedBlock(signBlock);
            if (attached != null && attached.getState() instanceof Chest) {
                if (admin && !p.hasPermission("bartershop.admin.create")) {
                    ShopUtils.send(p, String.valueOf(ChatColor.RED) + "你没有权限创建管理员商店。");
                } else {
                    if (!admin) {
                        if (ShopUtils.getUpperChest(attached) == null) {
                            ShopUtils.send(p, String.valueOf(ChatColor.YELLOW) + "提示：在商店箱子正上方放一个箱子用于【货币/收款】。");
                        }

                        if (ShopUtils.getLowerChest(attached) == null) {
                            ShopUtils.send(p, String.valueOf(ChatColor.YELLOW) + "提示：在商店箱子正下方放一个箱子用于【库存/出货】。");
                        }
                    }

                    ShopUtils.send(p, String.valueOf(ChatColor.YELLOW) + "规则：第1行=需要；第2行=给予；上=收款箱，下=库存箱。");
                    ShopUtils.send(p, String.valueOf(ChatColor.YELLOW) + "第3行可选放入书与笔进行交易记录。");
                    Location signLoc = signBlock.getLocation();
                    Location chestLoc = attached.getLocation();
                    this.shopManager.create(signLoc, chestLoc, p.getUniqueId(), admin);
                    if (admin) {
                        e.setLine(0, String.valueOf(ChatColor.DARK_RED) + "[adminshop]");
                        String var10002 = String.valueOf(ChatColor.YELLOW);
                        e.setLine(1, var10002 + p.getName());
                        e.setLine(2, String.valueOf(ChatColor.GRAY) + "左键查看");
                        e.setLine(3, String.valueOf(ChatColor.GRAY) + "右键交易");
                        ShopUtils.send(p, String.valueOf(ChatColor.GREEN) + "管理员商店创建成功！");
                    } else {
                        e.setLine(0, String.valueOf(ChatColor.DARK_GREEN) + "[shop]");
                        String var9 = String.valueOf(ChatColor.YELLOW);
                        e.setLine(1, var9 + p.getName());
                        e.setLine(2, String.valueOf(ChatColor.GRAY) + "左键查看");
                        e.setLine(3, String.valueOf(ChatColor.GRAY) + "右键交易");
                        ShopUtils.send(p, String.valueOf(ChatColor.GREEN) + "商店创建成功！");
                    }

                }
            } else {
                ShopUtils.send(p, String.valueOf(ChatColor.RED) + "请把牌子贴在一只箱子上（墙上告示牌）。");
            }
        }
    }
}
