package fr.drakariaprofile.listeners;

import fr.drakariaprofile.profile.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SmeltListener implements Listener {
    private final ProfileManager profileManager;

    public SmeltListener(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    @EventHandler
    public void onFurnaceExtract(InventoryClickEvent event) {
        // Uniquement four, slot sortie, joueur
        if (!(event.getInventory() instanceof FurnaceInventory)) return;
        if (event.getRawSlot() != 2) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        // Pour la robustesse, décaler de 1 tick pour voir combien sont réellement partis
        Player player = (Player) event.getWhoClicked();
        FurnaceInventory inv = (FurnaceInventory) event.getInventory();

        ItemStack before = event.getCurrentItem();
        if (before == null || before.getType() == Material.AIR) return;
        String matName = before.getType().name();
        double xpUnit = profileManager.getXpForSmelt(matName);

        if (xpUnit <= 0) return;

        int amountBefore = before.getAmount();

        // Snapshot du slot AVANT le mouvement, puis on check APRÈS 1 tick pour voir les items retirés réellement
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack after = inv.getItem(2); // slot de sortie = 2
                int amountAfter = (after == null || after.getType() == Material.AIR) ? 0 : after.getAmount();
                int realExtract = Math.max(0, amountBefore - amountAfter);

                // Cas: inventaire plein, shift-clic → aucun item n’a été déplacé réellement
                if (realExtract <= 0) return;

                // Attribuer l’xp sur le mouvement RÉEL
                profileManager.addXp(player, xpUnit * realExtract);
            }
        }.runTask(Bukkit.getPluginManager().getPlugin("DrakariaProfile"));
    }
}
