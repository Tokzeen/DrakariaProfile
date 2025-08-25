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

import java.util.*;

public class ChasseurMenuListener implements Listener {
    private final ProfileManager profileManager;
    private final UpgradeRepository upgradeRepository;
    private final MenuManager menuManager;

    // Pour associer slots internes <-> mobs
    private final Map<Integer, String> slotToMob = new HashMap<>();

    public ChasseurMenuListener(ProfileManager pm, MenuManager mm) {
        this.profileManager = pm;
        this.upgradeRepository = pm.getUpgradeRepository();
        this.menuManager = mm;

        // Correspondance slot interne <-> mob : doit correspondre au menu !
        int[] upgradeSlots = {
                10,11,12,13,14,15,16,
                19,20,21,22,23,24,25,
                28,29,30,31,32,33,34,
                37,38,39,40,41,42,43
        };
        Map<String, ?> mobXpMap = fr.drakariaprofile.DrakariaProfile.getInstance().getProfileManager().mobXpMap;
        int slotIdx = 0;
        for (String mobKey : mobXpMap.keySet()) {
            if (slotIdx >= upgradeSlots.length) break;
            slotToMob.put(upgradeSlots[slotIdx], mobKey);
            slotIdx++;
        }
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

        // Upgrades dynamiques par mob
        if (clicked.getType() == Material.MONSTER_EGG && slotToMob.containsKey(event.getRawSlot())) {
            String mobKey = slotToMob.get(event.getRawSlot());
            String upgradeKey = "hunter_" + mobKey;
            int level = upgradeRepository.getUpgradeLevel(player.getUniqueId(), upgradeKey);

            int[] palier = ChasseurUpgradeMenu.getMobChasseurUpgradeSteps(mobKey);
            int max = palier.length;
            int cost = (level == 0 ? 1 : 3);
            // Points de productivité
            if (level >= max) {
                player.sendMessage("§cAmélioration chasse ("+mobKey+") déjà au niveau max !");
                return;
            }
            if (profile.getUpgradeProductivite() < cost) {
                player.sendMessage("§cPas assez de points de productivité !");
                return;
            }
            profile.setUpgradeProductivite(profile.getUpgradeProductivite() - cost);
            profileManager.saveProfile(profile);
            upgradeRepository.setUpgradeLevel(player.getUniqueId(), upgradeKey, level + 1);

            player.sendMessage("§aAmélioration Chasseur ("+mobKey+") niveau " + (level + 1) + " achetée !");
            menuManager.openChasseurUpgradeMenu(player, profile);
        }
    }
}
