package fr.drakariaprofile.menu;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.profile.Profile;
import fr.drakariaprofile.storage.UpgradeRepository;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ChasseurMenuListener implements Listener {
    private final ProfileManager profileManager;
    private final UpgradeRepository upgradeRepository;
    private final MenuManager menuManager;

    public ChasseurMenuListener(ProfileManager pm, MenuManager mm) {
        this.profileManager = pm;
        this.upgradeRepository = pm.getUpgradeRepository();
        this.menuManager = mm;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Améliorations : Chasseur")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        Player player = (Player) event.getWhoClicked();
        Profile profile = profileManager.getProfile(player);

        // Quitter
        if (clicked.getType() == Material.REDSTONE && event.getRawSlot() == 45) {
            player.closeInventory();
            return;
        }

        // Achat d'upgrade "hunter"
        if (clicked.getType() == Material.BOW && event.getRawSlot() == 10) {
            int level = upgradeRepository.getUpgradeLevel(player.getUniqueId(), "hunter");
            int cost = (level == 0 ? 1 : 3);
            if (level >= 2) {
                player.sendMessage("§cAmélioration chasseur déjà au niveau max !");
                return;
            }
            if (profile.getUpgradeChance() < cost) {
                player.sendMessage("§cPas assez de points de chance !");
                return;
            }
            profile.setUpgradeChance(profile.getUpgradeChance() - cost);
            profileManager.saveProfile(profile);
            upgradeRepository.setUpgradeLevel(player.getUniqueId(), "hunter", level + 1);

            player.sendMessage("§aAmélioration Chasseur niveau " + (level + 1) + " achetée !");
            menuManager.openChasseurUpgradeMenu(player, profile);
        }
    }
}
