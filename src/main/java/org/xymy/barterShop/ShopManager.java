package org.xymy.barterShop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class ShopManager {
    private final Logger logger;
    private final File file;
    private final FileConfiguration cfg;
    private final Map<String, Shop> bySignKey = new HashMap();
    private final Map<String, Shop> byChestKey = new HashMap();

    public ShopManager(BarterShop plugin) {
        this.logger = plugin.getLogger();
        this.file = new File(plugin.getDataFolder(), "shops.yml");
        this.cfg = new YamlConfiguration();
    }

    public void initialize() {
        this.load();
    }

    public void create(Location signLoc, Location chestLoc, UUID owner, boolean isAdmin) {
        Shop s = new Shop(signLoc, chestLoc, owner, isAdmin);
        this.bySignKey.put(s.key(), s);
        this.byChestKey.put(this.chestKey(chestLoc), s);
        this.save();
    }

    public void delete(Shop s) {
        this.bySignKey.remove(s.key());
        this.byChestKey.remove(this.chestKey(s.getChestLocation()));
        this.save();
    }

    public Shop findBySign(Location signLoc) {
        String var10000 = signLoc.getWorld().getName();
        String key = "S:" + var10000 + ":" + signLoc.getBlockX() + "," + signLoc.getBlockY() + "," + signLoc.getBlockZ();
        return (Shop)this.bySignKey.get(key);
    }

    public Shop findByChest(Location chestLoc) {
        return (Shop)this.byChestKey.get(this.chestKey(chestLoc));
    }

    public List<Shop> allShops() {
        return new ArrayList(this.bySignKey.values());
    }

    public List<Shop> findByOwner(UUID ownerId) {
        List<Shop> list = new ArrayList();

        for(Shop s : this.bySignKey.values()) {
            if (s.getOwnerId().equals(ownerId)) {
                list.add(s);
            }
        }

        return list;
    }

    private String chestKey(Location l) {
        String var10000 = l.getWorld().getName();
        return "C:" + var10000 + ":" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }

    private void load() {
        if (this.file.exists()) {
            try {
                this.cfg.load(this.file);
                this.bySignKey.clear();
                this.byChestKey.clear();
                if (this.cfg.isConfigurationSection("shops")) {
                    for(String k : this.cfg.getConfigurationSection("shops").getKeys(false)) {
                        Map<String, Object> map = this.cfg.getConfigurationSection("shops." + k).getValues(false);
                        Shop s = Shop.deserialize(map);
                        this.bySignKey.put(k, s);
                        this.byChestKey.put(this.chestKey(s.getChestLocation()), s);
                    }
                    this.logger.info("加载 shops.yml 成功");
                }

            } catch (InvalidConfigurationException | IOException ex) {
                this.logger.severe("加载 shops.yml 失败：" + ((Exception)ex).getMessage());
            }
        }
    }

    public void save() {
        try {
            this.cfg.set("shops", (Object)null);

            for(Shop s : this.bySignKey.values()) {
                this.cfg.createSection("shops." + s.key(), s.serialize());
            }

            this.cfg.save(this.file);
        } catch (IOException ex) {
            this.logger.severe("保存 shops.yml 失败：" + ex.getMessage());
        }

    }

    static {
        ConfigurationSerialization.registerClass(Shop.class);
    }
}
