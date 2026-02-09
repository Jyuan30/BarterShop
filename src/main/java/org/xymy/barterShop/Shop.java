package org.xymy.barterShop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Shop implements ConfigurationSerializable {
    private final Location signLocation;
    private final Location chestLocation;
    private final UUID ownerId;
    private final boolean isAdmin;

    public Shop(Location signLocation, Location chestLocation, UUID ownerId, boolean isAdmin) {
        this.signLocation = signLocation;
        this.chestLocation = chestLocation;
        this.ownerId = ownerId;
        this.isAdmin = isAdmin;
    }

    public static Shop deserialize(Map<String, Object> map) {
        Location signLoc = (Location)map.get("signLocation");
        Location chestLoc = (Location)map.get("chestLocation");
        UUID owner = UUID.fromString((String)map.get("ownerId"));
        boolean admin = (Boolean)map.get("isAdmin");
        return new Shop(signLoc, chestLoc, owner, admin);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap();
        map.put("signLocation", this.signLocation);
        map.put("chestLocation", this.chestLocation);
        map.put("ownerId", this.ownerId.toString());
        map.put("isAdmin", this.isAdmin);
        return map;
    }

    public String key() {
        String var10000 = this.signLocation.getWorld().getName();
        return "S:" + var10000 + ":" + this.signLocation.getBlockX() + "," + this.signLocation.getBlockY() + "," + this.signLocation.getBlockZ();
    }

    public Location getSignLocation() {
        return this.signLocation;
    }

    public Location getChestLocation() {
        return this.chestLocation;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }
}
