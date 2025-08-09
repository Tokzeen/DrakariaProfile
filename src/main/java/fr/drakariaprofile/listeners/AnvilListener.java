package fr.drakariaprofile.listeners;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import fr.drakariaprofile.profile.ProfileManager;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class AnvilListener implements Listener {
    private final ProfileManager profileManager;
    private final Plugin plugin;
    private final Set<String> processed = Collections.newSetFromMap(new WeakHashMap<>());

    public AnvilListener(ProfileManager profileManager, Plugin plugin) {
        this.profileManager = profileManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Inventory inv = event.getInventory();
        if (inv == null || inv.getType() != InventoryType.ANVIL) return;
        if (event.getRawSlot() != 2) return; // slot résultat

        Player player = (Player) event.getWhoClicked();
        ItemStack result = inv.getItem(2);
        if (result == null || result.getType() == Material.AIR) return;
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("§cTon inventaire est plein, impossible de récupérer l'objet.");
            return;
        }

        // Utilise une valeur générique, ex : 10xp pour toute utilisation
        double xpGain = 10.0;
        profileManager.addXp(player, xpGain);
    }


}
