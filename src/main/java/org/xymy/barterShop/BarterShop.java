package org.xymy.barterShop;

import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;
import org.xymy.barterShop.commands.BarterShopCommand;
import org.xymy.barterShop.listeners.ShopCreateListener;
import org.xymy.barterShop.listeners.ShopProtectListener;
import org.xymy.barterShop.listeners.ShopUseListener;

public final class BarterShop extends JavaPlugin {
    private ShopManager shopManager;
    public static final UUID ADMIN_GROUP_UUID = new UUID(0L, 0L);

    public void onEnable() {
        this.getDataFolder().mkdirs();
        this.shopManager = new ShopManager(this);
        ShopUseListener useListener = new ShopUseListener(this);
        this.getServer().getPluginManager().registerEvents(new ShopCreateListener(this), this);
        this.getServer().getPluginManager().registerEvents(useListener, this);
        this.getServer().getPluginManager().registerEvents(new ShopProtectListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ServerListener(this), this);
        if (this.getCommand("bartershop") != null) {
            this.getCommand("bartershop").setExecutor(new BarterShopCommand(this));
        } else {
            this.getLogger().warning("未在 plugin.yml 中声明命令 /bartershop，命令将不可用。");
        }

        this.getLogger().info("BarterShop 已启用。");
    }

    public void onDisable() {
        if (this.shopManager != null) {
            this.shopManager.save();
        }

        this.getLogger().info("BarterShop 已关闭。");
    }

    public ShopManager getShopManager() {
        return this.shopManager;
    }
}
