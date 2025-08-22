package fr.drakariaprofile.menu;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.profile.Profile;
import fr.drakariaprofile.storage.UpgradeRepository;
import fr.drakariaprofile.utils.VaultHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MineurMenuListener implements Listener {
    private final ProfileManager profileManager;
    private final UpgradeRepository upgradeRepository;
    private final MenuManager menuManager;

    public MineurMenuListener(ProfileManager pm, MenuManager mm) {
        this.profileManager = pm;
        this.upgradeRepository = pm.getUpgradeRepository();
        this.menuManager = mm;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Améliorations : Mineur")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        Material mat = clicked.getType();
        if (mat == Material.STAINED_GLASS_PANE ||
                mat == Material.IRON_FENCE ||
                mat == Material.NETHER_STAR ||
                mat == Material.SKULL_ITEM) return;

        Player player = (Player) event.getWhoClicked();
        Profile profile = profileManager.getProfile(player);

        // Quitter le menu avec le colorant rouge (slot 36)
        if (mat == Material.REDSTONE && event.getRawSlot() == 36) {
            player.closeInventory();
            return;
        }

        // Achat/amélioration stone, slot 10
        if (mat == Material.STONE && event.getRawSlot() == 10) {
            int stoneLevel = upgradeRepository.getUpgradeLevel(player.getUniqueId(), "stone");
            int cout = stoneLevel == 0 ? 1 : (stoneLevel == 1 ? 2 : 5);
            if (stoneLevel >= 2) {
                player.sendMessage("§cAmélioration stone déjà au niveau max !");
                return;
            }
            if (profile.getUpgradeProductivite() < cout) {
                player.sendMessage("§cPas assez de points de productivité !");
                return;
            }
            profile.setUpgradeProductivite(profile.getUpgradeProductivite() - cout);
            profileManager.saveProfile(profile);
            upgradeRepository.setUpgradeLevel(player.getUniqueId(), "stone", stoneLevel + 1);

            player.sendMessage("§aAmélioration Stone lv." + (stoneLevel + 1) + " achetée !");
            if (stoneLevel + 1 == 2) {
                VaultHook.deposit(player, 0.1);
            }
            // Réouvre le menu mis à jour
            menuManager.openMineurUpgradeMenu(player, profile);
        }
    }
}
