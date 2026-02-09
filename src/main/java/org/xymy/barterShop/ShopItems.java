package org.xymy.barterShop;

import java.util.List;
import org.bukkit.inventory.ItemStack;

public class ShopItems {
    public final List<ItemStack> neededList;
    public final List<ItemStack> givenList;

    public ShopItems(List<ItemStack> neededList, List<ItemStack> givenList) {
        this.neededList = neededList;
        this.givenList = givenList;
    }
}
