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
        if (!(event.getInventory() instanceof FurnaceInventory)) return;
        if (event.getRawSlot() != 2) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        // --- Blocage XP si frozen ---
        if (profileManager.isFrozen(player)) {
            return;
        }

        FurnaceInventory inv = (FurnaceInventory) event.getInventory();

        ItemStack before = event.getCurrentItem();
        if (before == null || before.getType() == Material.AIR) return;
        String matName = before.getType().name();
        double xpUnit = profileManager.getXpForSmelt(matName);

        if (xpUnit <= 0) return;

        int amountBefore = before.getAmount();

        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack after = inv.getItem(2);
                int amountAfter = (after == null || after.getType() == Material.AIR) ? 0 : after.getAmount();
                int realExtract = Math.max(0, amountBefore - amountAfter);
                if (realExtract <= 0) return;

                // --- Blocage XP si frozen (sécurité double, utile si le freeze change pendant le tick) ---
                if (profileManager.isFrozen(player)) {
                    return;
                }

                profileManager.addXp(player, xpUnit * realExtract);
            }
        }.runTask(Bukkit.getPluginManager().getPlugin("DrakariaProfile"));
    }
}
