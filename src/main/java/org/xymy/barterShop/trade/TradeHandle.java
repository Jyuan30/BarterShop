package org.xymy.barterShop.trade;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.xymy.barterShop.Shop;
import org.xymy.barterShop.ShopItems;
import org.xymy.barterShop.util.ShopUtils;

public class TradeHandle {
    Shop shop;
    ShopItems items;
    Inventory inv;

    public TradeHandle(Shop shop) {
        this.shop = shop;
        Block chestBlock = shop.getChestLocation().getBlock();
        Chest mainChest = (Chest)chestBlock.getState();
        Inventory chestInv = mainChest.getInventory();
        this.items = new ShopItems(new ArrayList(), new ArrayList());
        List<ItemStack> neededListWithNull = ShopUtils.firstRow(chestInv);
        List<ItemStack> givenListWithNull = ShopUtils.secondRow(chestInv);

        for(int i = 0; i < 9; ++i) {
            ItemStack needed = (ItemStack)neededListWithNull.get(i);
            ItemStack given = (ItemStack)givenListWithNull.get(i);
            if (needed != null) {
                this.items.neededList.add(needed);
            }

            if (given != null) {
                this.items.givenList.add(given);
            }
        }

    }

    private int getStockCount(ItemStack pattern) {
        if (this.shop.isAdmin()) {
            return Integer.MAX_VALUE;
        } else {
            Block chestBlock = this.shop.getChestLocation().getBlock();
            if (!(chestBlock.getState() instanceof Chest)) {
                return 0;
            } else {
                Chest lowerChest = ShopUtils.getLowerChest(chestBlock);
                return lowerChest == null ? 0 : ShopUtils.countSimilar(lowerChest.getInventory(), pattern);
            }
        }
    }

    public boolean attemptTrade(Player p) {
        Block chestBlock = this.shop.getChestLocation().getBlock();
        Chest upperChest = ShopUtils.getUpperChest(chestBlock);
        Chest lowerChest = ShopUtils.getLowerChest(chestBlock);
        if (upperChest != null && lowerChest != null) {
            if (!(chestBlock.getState() instanceof Chest)) {
                ShopUtils.send(p, String.valueOf(ChatColor.RED) + "交易失败，商店箱子缺失！");
                return false;
            } else {
                boolean firstRowState = ShopUtils.isRowEmpty(this.items.givenList);
                boolean secondRowState = ShopUtils.isRowEmpty(this.items.neededList);
                if (!firstRowState && !secondRowState) {
                    int maxNeed = this.items.neededList.size();
                    int maxGive = this.items.givenList.size();

                    for(int i = 0; i < maxNeed; ++i) {
                        ItemStack nowItem = (ItemStack)this.items.neededList.get(i);
                        if (ShopUtils.countSimilar(p.getInventory(), nowItem) < nowItem.getAmount()) {
                            String var10001 = String.valueOf(ChatColor.RED);
                            ShopUtils.send(p, var10001 + "你没有足够的 " + nowItem.getType().name() + " 来兑换！");
                            return false;
                        }
                    }

                    for(int i = 0; i < maxGive; ++i) {
                        ItemStack nowItem = (ItemStack)this.items.givenList.get(i);
                        int stock = this.getStockCount(nowItem);
                        if (stock < nowItem.getAmount() && !this.shop.isAdmin()) {
                            ShopUtils.send(p, String.valueOf(ChatColor.RED) + "商店库存不足！");
                            return false;
                        }
                    }

                    Inventory tempInv = Bukkit.createInventory((InventoryHolder)null, 36);
                    tempInv.setContents(p.getInventory().getStorageContents());

                    for(int i = 0; i < maxGive; ++i) {
                        ItemStack nowItem = (ItemStack)this.items.givenList.get(i);
                        boolean success = ShopUtils.addItems(tempInv, nowItem);
                        if (!success) {
                            ShopUtils.send(p, String.valueOf(ChatColor.RED) + "你的背包空间不足，无法接收兑换物！");
                            return false;
                        }
                    }

                    Inventory upperChestInventory = Bukkit.createInventory((InventoryHolder)null, upperChest.getInventory().getSize());
                    upperChestInventory.setContents(upperChest.getInventory().getContents());

                    for(int i = 0; i < maxNeed; ++i) {
                        ItemStack nowItem = (ItemStack)this.items.neededList.get(i);
                        boolean success = ShopUtils.addItems(upperChestInventory, nowItem);
                        if (!success) {
                            ShopUtils.send(p, String.valueOf(ChatColor.RED) + "收款箱子空间不足，请及时联系店主！");
                            return false;
                        }
                    }

                    for(int i = 0; i < maxNeed; ++i) {
                        ItemStack nowItem = (ItemStack)this.items.neededList.get(i);
                        ShopUtils.removeItems(p.getInventory(), nowItem, nowItem.getAmount());
                    }

                    for(int i = 0; i < maxGive; ++i) {
                        ItemStack nowItem = (ItemStack)this.items.givenList.get(i);
                        ShopUtils.addItems(p.getInventory(), nowItem.clone());
                    }

                    if (!this.shop.isAdmin()) {
                        for(int i = 0; i < maxGive; ++i) {
                            ItemStack nowItem = (ItemStack)this.items.givenList.get(i);
                            if (lowerChest != null) {
                                ShopUtils.removeItems(lowerChest.getInventory(), nowItem, nowItem.getAmount());
                            }
                        }

                        for(int i = 0; i < maxNeed; ++i) {
                            ItemStack nowItem = (ItemStack)this.items.neededList.get(i);
                            if (upperChest != null) {
                                ShopUtils.addItems(upperChest.getInventory(), nowItem.clone());
                            }
                        }
                    }

                    Chest chest = (Chest)chestBlock.getState();
                    Inventory chestInventory = chest.getInventory();

                    for(int i = 0; i < 9; ++i) {
                        ItemStack thirdRowItem = chestInventory.getItem(18 + i);
                        if (thirdRowItem != null && thirdRowItem.getType() == Material.WRITABLE_BOOK) {
                            ItemMeta meta = thirdRowItem.getItemMeta();
                            if (meta instanceof BookMeta) {
                                BookMeta bookMeta = (BookMeta)meta;
                                List<String> pages = bookMeta.getPages();
                                List<String> mutablePages = new ArrayList(pages);
                                String transactionRecord = "\n玩家 " + p.getName() + " 进行了交易";
                                int maxLen = 168;
                                int addLen = transactionRecord.length();
                                int findPage = 0;

                                for(String page : mutablePages) {
                                    if (page.length() + addLen < maxLen) {
                                        break;
                                    }

                                    ++findPage;
                                }

                                if (findPage == mutablePages.size()) {
                                    mutablePages.add(transactionRecord);
                                } else {
                                    String var10000 = (String)mutablePages.get(findPage);
                                    String updatedPage = var10000 + transactionRecord;
                                    mutablePages.set(findPage, updatedPage);
                                }

                                bookMeta.setPages(mutablePages);
                                thirdRowItem.setItemMeta(bookMeta);
                                chestInventory.setItem(18 + i, thirdRowItem);
                                break;
                            }
                        }
                    }

                    return true;
                } else {
                    ShopUtils.send(p, String.valueOf(ChatColor.RED) + "中间箱子交易模板缺失，请设置完整");
                    return false;
                }
            }
        } else {
            ShopUtils.send(p, String.valueOf(ChatColor.RED) + "交易失败，商店箱子缺失！");
            return false;
        }
    }

    public boolean attemptTradeAdmin(Player p) {
        Block chestBlock = this.shop.getChestLocation().getBlock();
        if (!(chestBlock.getState() instanceof Chest)) {
            ShopUtils.send(p, String.valueOf(ChatColor.RED) + "交易失败，商店箱子缺失！");
            return false;
        } else {
            boolean firstRowState = ShopUtils.isRowEmpty(this.items.givenList);
            boolean secondRowState = ShopUtils.isRowEmpty(this.items.neededList);
            if (!firstRowState && !secondRowState) {
                int maxNeed = this.items.neededList.size();
                int maxGive = this.items.givenList.size();

                for(int i = 0; i < maxNeed; ++i) {
                    ItemStack nowItem = (ItemStack)this.items.neededList.get(i);
                    if (ShopUtils.countSimilar(p.getInventory(), nowItem) < nowItem.getAmount()) {
                        String var10001 = String.valueOf(ChatColor.RED);
                        ShopUtils.send(p, var10001 + "你没有足够的 " + nowItem.getType().name() + " 来兑换！");
                        return false;
                    }
                }

                Inventory tempInv = Bukkit.createInventory((InventoryHolder)null, 36);
                tempInv.setContents(p.getInventory().getStorageContents());

                for(int i = 0; i < maxGive; ++i) {
                    ItemStack nowItem = (ItemStack)this.items.givenList.get(i);
                    boolean success = ShopUtils.addItems(tempInv, nowItem);
                    if (!success) {
                        ShopUtils.send(p, String.valueOf(ChatColor.RED) + "你的背包空间不足，无法接收兑换物！");
                        return false;
                    }
                }

                for(int i = 0; i < maxNeed; ++i) {
                    ItemStack nowItem = (ItemStack)this.items.neededList.get(i);
                    ShopUtils.removeItems(p.getInventory(), nowItem, nowItem.getAmount());
                }

                for(int i = 0; i < maxGive; ++i) {
                    ItemStack nowItem = (ItemStack)this.items.givenList.get(i);
                    ShopUtils.addItems(p.getInventory(), nowItem.clone());
                }

                Chest chest = (Chest)chestBlock.getState();
                Inventory chestInventory = chest.getInventory();

                for(int i = 0; i < 9; ++i) {
                    ItemStack thirdRowItem = chestInventory.getItem(18 + i);
                    if (thirdRowItem != null && thirdRowItem.getType() == Material.WRITABLE_BOOK) {
                        ItemMeta meta = thirdRowItem.getItemMeta();
                        if (meta instanceof BookMeta) {
                            BookMeta bookMeta = (BookMeta)meta;
                            List<String> pages = bookMeta.getPages();
                            List<String> mutablePages = new ArrayList(pages);
                            String transactionRecord = "\n玩家 " + p.getName() + " 进行了交易";
                            int maxLen = 168;
                            int addLen = transactionRecord.length();
                            int findPage = 0;

                            for(String page : mutablePages) {
                                if (page.length() + addLen < maxLen) {
                                    break;
                                }

                                ++findPage;
                            }

                            if (findPage == mutablePages.size()) {
                                mutablePages.add(transactionRecord);
                            } else {
                                String var10000 = (String)mutablePages.get(findPage);
                                String updatedPage = var10000 + transactionRecord;
                                mutablePages.set(findPage, updatedPage);
                            }

                            bookMeta.setPages(mutablePages);
                            thirdRowItem.setItemMeta(bookMeta);
                            chestInventory.setItem(18 + i, thirdRowItem);
                            break;
                        }
                    }
                }

                return true;
            } else {
                ShopUtils.send(p, String.valueOf(ChatColor.RED) + "中间箱子交易模板缺失，请设置完整");
                return false;
            }
        }
    }
}
