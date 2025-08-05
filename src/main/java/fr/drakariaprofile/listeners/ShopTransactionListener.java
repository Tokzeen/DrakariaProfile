package fr.drakariaprofile.listeners;

import fr.drakariaprofile.profile.ProfileManager;
import net.brcdev.shopgui.event.ShopPreTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager;
import net.brcdev.shopgui.shop.item.ShopItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ShopTransactionListener implements Listener {

    private final ProfileManager profileManager;

    public ShopTransactionListener(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    @EventHandler
    public void onShopPreTransaction(ShopPreTransactionEvent event) {
        ShopManager.ShopAction action = event.getShopAction();

        // Vente seulement
        if (action == null || !action.name().toLowerCase().contains("sell")) {
            return;
        }

        Player player = event.getPlayer();

        // --- Blocage XP si frozen ---
        if (profileManager.isFrozen(player)) {
            return;
        }

        ShopItem shopItem = event.getShopItem();
        if (player == null || shopItem == null) return;

        ItemStack itemStack = shopItem.getItem();
        if (itemStack == null || itemStack.getType() == Material.AIR) return;

        int amount = event.getAmount();
        if (amount <= 0) return;

        String materialName = itemStack.getType().name();
        double xpPerItem = profileManager.getXpForShopSell(materialName);
        if (xpPerItem <= 0) return;

        profileManager.addXp(player, xpPerItem * amount);
    }
}
