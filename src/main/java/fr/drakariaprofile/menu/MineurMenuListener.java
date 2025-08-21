package fr.drakariaprofile.menu;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.profile.Profile;
import fr.drakariaprofile.utils.VaultHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MineurMenuListener implements Listener {
    private final ProfileManager profileManager;
    private final MenuManager menuManager;

    public MineurMenuListener(ProfileManager pm, MenuManager mm) {
        this.profileManager = pm;
        this.menuManager = mm;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Améliorations : Mineur")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        Material mat = clicked.getType();
        // Interdit le clic sur éléments non achetables
        if (mat == Material.STAINED_GLASS_PANE ||
                mat == Material.IRON_FENCE ||
                mat == Material.NETHER_STAR) return;

        Player player = (Player) event.getWhoClicked();
        Profile profile = profileManager.getProfile(player);

        // Achat d'amélioration Stone
        if (mat == Material.STONE) {
            int stoneLevel = profile.getStoneUpgradeLevel();
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
            profile.setStoneUpgradeLevel(stoneLevel + 1);
            profileManager.saveProfile(profile);

            player.sendMessage("§aAmélioration stone lv." + (stoneLevel + 1) + " achetée !");
            if (stoneLevel + 1 == 2) {
                VaultHook.deposit(player, 0.1);
            }
            menuManager.openMineurUpgradeMenu(player, profile);
        }
    }
}
