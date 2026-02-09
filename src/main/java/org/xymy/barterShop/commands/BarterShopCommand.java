package org.xymy.barterShop.commands;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.xymy.barterShop.BarterShop;
import org.xymy.barterShop.Shop;
import org.xymy.barterShop.ShopManager;
import org.xymy.barterShop.listeners.ShopUseListener;

import java.util.ArrayList;
import java.util.UUID;

import static org.xymy.barterShop.util.ShopUtils.send;

public class BarterShopCommand implements CommandExecutor {

    private final BarterShop plugin;

    public BarterShopCommand(BarterShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // 1. 判断是否输入了子指令（例如 /bartershop clear）
        if (args.length > 0) {

            // 2. 匹配子指令 "clear"
            if (args[0].equalsIgnoreCase("clear")) {
                // 建议：增加权限检查，防止普通玩家清理数据
                if (!sender.hasPermission("bartershop.admin.clear")) {
                    sender.sendMessage("§c你没有权限执行此命令。");
                    return true;
                }

                // 执行清理逻辑
                int size = 0;

                ShopManager shopManager = this.plugin.getShopManager();

                for (Shop shop : shopManager.allShops()) {
                    Block chestBlock = shop.getChestLocation().getBlock();
                    if (!(chestBlock.getState() instanceof Chest)) {
                        shopManager.delete(shop);
                        size = size + 1;
                    }
                }

                sender.sendMessage("§a[BarterShop] 成功清空了数据库中的 " + size + " 个错误删除商店。");
                return true;
            } else if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage("§6--- BarterShop 帮助 ---");
                sender.sendMessage("§e/bartershop clear §7- 清空内存中的商店数据缓存");
                return true;
            }
        }

        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "该命令只能由玩家执行。");
            return true;
        }

        // 调用 ShopUseListener 中 public 的 GUI 打开方法
        ShopUseListener.openOwnerListGUI(p, 0);
        return true;
    }
}