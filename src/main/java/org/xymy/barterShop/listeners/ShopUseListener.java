package org.xymy.barterShop.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.xymy.barterShop.BarterShop;
import org.xymy.barterShop.Shop;
import org.xymy.barterShop.ShopManager;
import org.xymy.barterShop.gui.PlayerShopListHolder;
import org.xymy.barterShop.gui.ShopGui;
import org.xymy.barterShop.gui.ShopListHolder;
import org.xymy.barterShop.gui.ViewHolder;
import org.xymy.barterShop.trade.TradeHandle;
import org.xymy.barterShop.util.ShopUtils;

public class ShopUseListener implements Listener {
    private final BarterShop plugin;
    private final ShopManager shopManager;
    private final Logger logger;
    private final Map<UUID, Shop> waitingForInput = new HashMap();
    private static final Map<UUID, ShopGui> openShops = new HashMap();
    public static ShopManager shopManagerGlobal;

    public ShopUseListener(BarterShop plugin) {
        this.logger = plugin.getLogger();
        this.plugin = plugin;
        this.shopManager = plugin.getShopManager();
        shopManagerGlobal = this.shopManager;
    }

    public static void openOwnerListGUI(Player p, int page) {
        openOwnerListGUI(p, p.getUniqueId(), page);
    }

    public static void openOwnerListGUI(Player p, UUID targetOwnerId, int page) {
        ShopListHolder gui = new ShopListHolder(p, targetOwnerId, page, shopManagerGlobal);
        openShops.put(p.getUniqueId(), gui);
        p.openInventory(gui.getInventory());
    }

    public static void openShopViewGUI(Player p, Shop shop, boolean openFromGui) {
        List<PlayerShopListHolder> playerShopListHolder;
        if (openFromGui) {
            playerShopListHolder = new ArrayList();
            playerShopListHolder.add((PlayerShopListHolder)openShops.get(p.getUniqueId()));
        } else {
            playerShopListHolder = new ArrayList();
        }

        ViewHolder gui = new ViewHolder(shop, p, playerShopListHolder);
        if (gui.inv != null) {
            openShops.put(p.getUniqueId(), gui);
            p.openInventory(gui.getInventory());
        }
    }

    public static void openPlayerShopViewGUI(Player p, UUID targetOwnerId, int page, int oldPage, List<Shop> shopList) {
        PlayerShopListHolder gui = new PlayerShopListHolder(p, targetOwnerId, page, oldPage, shopList);
        openShops.put(p.getUniqueId(), gui);
        p.openInventory(gui.getInventory());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            Block block = e.getClickedBlock();
            if (ShopUtils.isAnyShopSign(block)) {
                e.setCancelled(true);
                Shop shop = this.shopManager.findBySign(block.getLocation());
                if (shop == null) {
                    return;
                }

                Player p = e.getPlayer();
                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    openShopViewGUI(p, shop, false);
                } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (this.waitingForInput.containsKey(p.getUniqueId())) {
                        return;
                    }

                    ShopUtils.send(p, String.valueOf(ChatColor.YELLOW) + "请输入交易次数(输入cancel取消)：");
                    this.waitingForInput.put(p.getUniqueId(), shop);
                }
            }

        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (this.waitingForInput.containsKey(player.getUniqueId())) {
            if (message.equalsIgnoreCase("cancel")) {
                ShopUtils.send(player, String.valueOf(ChatColor.RED) + "交易已取消！");
                this.waitingForInput.remove(player.getUniqueId());
                event.setCancelled(true);
                return;
            }

            try {
                int tradeCount = Integer.parseInt(message);
                Shop shop = (Shop)this.waitingForInput.get(player.getUniqueId());
                if (tradeCount <= 0) {
                    ShopUtils.send(player, String.valueOf(ChatColor.RED) + "请输入一个有效的交易次数！（必须大于 0）");
                } else {
                    Location pos1 = shop.getChestLocation();
                    Location pos2 = player.getLocation();
                    if (pos1.getWorld().equals(pos2.getWorld()) && pos1.distance(pos2) < (double)10.0F) {
                        String var10001 = String.valueOf(ChatColor.GREEN);
                        ShopUtils.send(player, var10001 + "交易确认！你将进行 " + tradeCount + " 次交易...");
                        Bukkit.getScheduler().runTask(this.plugin, () -> {
                            int successTrade = 0;

                            for(int i = 0; i < tradeCount; ++i) {
                                TradeHandle trade = new TradeHandle(shop);
                                boolean tradeState;
                                if (shop.isAdmin()) {
                                    tradeState = trade.attemptTradeAdmin(player);
                                } else {
                                    tradeState = trade.attemptTrade(player);
                                }

                                if (!tradeState) {
                                    ShopUtils.send(player, String.valueOf(ChatColor.RED) + "交易终止。");
                                    break;
                                }

                                ++successTrade;
                            }

                            if (successTrade == tradeCount) {
                                ShopUtils.send(player, String.valueOf(ChatColor.GREEN) + "成功交易！");
                            }

                            this.waitingForInput.remove(player.getUniqueId());
                        });
                    } else {
                        ShopUtils.send(player, String.valueOf(ChatColor.RED) + "你距离商店太远，无法进行交易，或者输入 'cancel' 取消交易！");
                    }
                }

                event.setCancelled(true);
            } catch (NumberFormatException var8) {
                ShopUtils.send(player, String.valueOf(ChatColor.RED) + "请输入一个有效的数字来确认交易次数，交易已取消！");
                this.waitingForInput.remove(player.getUniqueId());
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        if (openShops.containsKey(p.getUniqueId())) {
            ShopGui gui = (ShopGui)openShops.get(p.getUniqueId());
            if (e.getInventory().equals(gui.getInventory())) {
                e.setCancelled(true);
                if (e.getClickedInventory() != null && e.getClickedInventory().equals(gui.getInventory())) {
                    int slot = e.getSlot();
                    if (gui instanceof ViewHolder) {
                        ViewHolder holder = (ViewHolder)gui;
                        if (slot >= 9 && slot < 18) {
                            holder.handleAction(p, slot, e.isRightClick() ? ShopUseListener.ActionType.RIGHT_CLICK : ShopUseListener.ActionType.LEFT_CLICK);
                        }
                    } else if (gui instanceof ShopListHolder) {
                        ShopListHolder holder = (ShopListHolder)gui;
                        if (slot >= 0) {
                            holder.handleAction(p, slot, e.isRightClick() ? ShopUseListener.ActionType.RIGHT_CLICK : ShopUseListener.ActionType.LEFT_CLICK);
                        }
                    } else if (gui instanceof PlayerShopListHolder) {
                        PlayerShopListHolder holder = (PlayerShopListHolder)gui;
                        if (slot >= 0) {
                            holder.handleAction(p, slot, e.isRightClick() ? ShopUseListener.ActionType.RIGHT_CLICK : ShopUseListener.ActionType.LEFT_CLICK);
                        }
                    }

                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        Player p = (Player)e.getWhoClicked();
        if (openShops.containsKey(p.getUniqueId())) {
            ShopGui gui = (ShopGui)openShops.get(p.getUniqueId());
            if (e.getInventory().equals(gui.getInventory())) {
                e.setCancelled(true);
            }
        }

    }

    public static enum ActionType {
        LEFT_CLICK,
        RIGHT_CLICK;
    }
}
