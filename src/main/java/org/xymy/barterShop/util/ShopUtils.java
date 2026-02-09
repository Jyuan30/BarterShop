package org.xymy.barterShop.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xymy.barterShop.BarterShop;

public final class ShopUtils {
    private ShopUtils() {
    }

    public static void send(Player p, String msg) {
        String var10001 = String.valueOf(ChatColor.GOLD);
        p.sendMessage(var10001 + "[以物易物] " + String.valueOf(ChatColor.RESET) + msg);
    }

    public static String ownerNameOrUuid(UUID id) {
        if (id.equals(BarterShop.ADMIN_GROUP_UUID)) {
            return "管理员";
        } else {
            OfflinePlayer op = Bukkit.getOfflinePlayer(id);
            String name = op != null ? op.getName() : null;
            return name != null ? name : id.toString().substring(0, 8);
        }
    }

    public static boolean isAnyShopSign(Block block) {
        if (block == null) {
            return false;
        } else {
            Material type = block.getType();
            if (!type.name().endsWith("_WALL_SIGN") && !type.name().endsWith("_SIGN")) {
                return false;
            } else if (!(block.getState() instanceof Sign)) {
                return false;
            } else {
                Sign sign = (Sign)block.getState();
                String line0 = ChatColor.stripColor(sign.getLine(0)).trim();
                return "[shop]".equalsIgnoreCase(line0) || "[adminshop]".equalsIgnoreCase(line0);
            }
        }
    }

    public static boolean isAdminLine(String line0) {
        return "[adminshop]".equalsIgnoreCase(line0);
    }

    public static Block getAttachedBlock(Block signBlock) {
        if (!(signBlock.getState() instanceof Sign)) {
            return null;
        } else {
            BlockData data = signBlock.getBlockData();
            if (data instanceof WallSign) {
                BlockFace face = ((Directional)data).getFacing().getOppositeFace();
                return signBlock.getRelative(face);
            } else {
                return null;
            }
        }
    }

    public static Chest getUpperChest(Block baseChestBlock) {
        Block up = baseChestBlock.getRelative(BlockFace.UP);
        return up != null && up.getState() instanceof Chest ? (Chest)up.getState() : null;
    }

    public static boolean isRowEmpty(List<ItemStack> row) {
        for(ItemStack it : row) {
            if (it != null && it.getType() != Material.AIR && it.getAmount() > 0) {
                return false;
            }
        }

        return true;
    }

    public static Chest getLowerChest(Block baseChestBlock) {
        Block down = baseChestBlock.getRelative(BlockFace.DOWN);
        return down != null && down.getState() instanceof Chest ? (Chest)down.getState() : null;
    }

    public static boolean isSameKind(ItemStack a, ItemStack b) {
        if (a != null && b != null) {
            if (a.getType() != b.getType()) {
                return false;
            } else {
                ItemMeta ma = a.hasItemMeta() ? a.getItemMeta() : null;
                ItemMeta mb = b.hasItemMeta() ? b.getItemMeta() : null;
                if (ma == null && mb == null) {
                    return true;
                } else if (ma != null && mb != null) {
                    if (!Objects.equals(ma.getDisplayName(), mb.getDisplayName())) {
                        return false;
                    } else if (!Objects.equals(ma.getLore(), mb.getLore())) {
                        return false;
                    } else if (ma.hasCustomModelData() != mb.hasCustomModelData()) {
                        return false;
                    } else if (ma.hasCustomModelData() && mb.hasCustomModelData() && !Objects.equals(ma.getCustomModelData(), mb.getCustomModelData())) {
                        return false;
                    } else {
                        return Objects.equals(ma.getEnchants(), mb.getEnchants());
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public static int countSimilar(Inventory inv, ItemStack pattern) {
        int sum = 0;

        for(ItemStack it : inv.getContents()) {
            if (it != null && isSameKind(it, pattern)) {
                sum += it.getAmount();
            }
        }

        return sum;
    }

    public static boolean removeItems(Inventory inv, ItemStack pattern, int amount) {
        int need = amount;

        for(int i = 0; i < inv.getSize(); ++i) {
            ItemStack it = inv.getItem(i);
            if (it != null && isSameKind(it, pattern)) {
                int take = Math.min(need, it.getAmount());
                it.setAmount(it.getAmount() - take);
                if (it.getAmount() <= 0) {
                    inv.setItem(i, (ItemStack)null);
                }

                need -= take;
                if (need <= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean addItems(Inventory inv, ItemStack stack) {
        HashMap<Integer, ItemStack> left = inv.addItem(new ItemStack[]{stack});
        return left.isEmpty();
    }

    public static List<ItemStack> firstRow(Inventory chestInv) {
        return slice(chestInv, 0, 9);
    }

    public static List<ItemStack> secondRow(Inventory chestInv) {
        return slice(chestInv, 9, 18);
    }

    public static List<ItemStack> slice(Inventory inv, int from, int to) {
        List<ItemStack> list = new ArrayList();

        for(int i = from; i < Math.min(to, inv.getSize()); ++i) {
            ItemStack it = inv.getItem(i);
            list.add(it == null ? null : it.clone());
        }

        return list;
    }
}
