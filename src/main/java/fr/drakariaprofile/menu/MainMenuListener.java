package fr.drakariaprofile.menu;

import fr.drakariaprofile.quest.QuestCategory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MainMenuListener implements Listener {

    @EventHandler
    public void onMainMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = ChatColor.stripColor(event.getView().getTitle());
        if (title == null || !title.contains("Objectifs / Quêtes")) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.BANNER) {
            switch (item.getDurability()) {
                case 10: // Vert (lime)
                    player.closeInventory();
                    player.performCommand("quete_facile");

                    break;
                case 14: // Orange
                    player.closeInventory();
                    player.performCommand("quete_moyen");
                    break;
                case 1: // Rouge
                    player.closeInventory();
                    player.performCommand("quete_difficile");
                    break;
            }
            return;
        }

        if (item.getType() == Material.EMERALD_ORE) {
            player.sendMessage("§cL'historique des récompenses sera disponible prochainement !");
            return;
        }
        // Le top et la peinture n’ont pas d’action interactive
    }
}
