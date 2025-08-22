package fr.drakariaprofile.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;

public class BoxRewardMenuListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Vérifie que le joueur, que le menu lootbox est ouvert
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getInventory().getTitle().startsWith("Ouverture de ")) {
            event.setCancelled(true); // Cancel l'action, rien n'est déplaçable ni prenable
        }
    }
}
