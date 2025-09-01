package fr.drakariaprofile.menu;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.profile.Profile;
import fr.drakariaprofile.storage.UpgradeRepository;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class FarmeurMenuListener implements Listener {

    private final ProfileManager profileManager;
    private final UpgradeRepository upgradeRepository;
    private final MenuManager menuManager;
    private final Map<Integer, String> slotToCrop = new HashMap<>();

    public FarmeurMenuListener(ProfileManager pm, MenuManager mm) {
        this.profileManager = pm;
        this.upgradeRepository = pm.getUpgradeRepository();
        this.menuManager = mm;
        Map<String, Double> cropXpMap = pm.getCropXpMap();
        int[] upgradeSlots = FarmeurUpgradeMenu.getFarmeurUpgradeSlots();
        int slotIdx = 0;
        for (String cropKey : cropXpMap.keySet()) {
            if (slotIdx >= upgradeSlots.length) break;
            slotToCrop.put(upgradeSlots[slotIdx], cropKey);
            slotIdx++;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Améliorations : Farmeur")) return;

        event.setCancelled(true);
        // Empêche shift-click, drop, etc.
        if (event.getClickedInventory() == null || event.getSlotType() == InventoryType.SlotType.OUTSIDE) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        Player player = (Player) event.getWhoClicked();
        Profile profile = profileManager.getProfile(player);

        // Slot fermer
        if (clicked.getType() == Material.REDSTONE && event.getRawSlot() == 45) {
            player.closeInventory();
            return;
        }

        // Seule action interactive : click sur la crop
        if (slotToCrop.containsKey(event.getRawSlot())) {
            String cropKey = slotToCrop.get(event.getRawSlot());
            String upgradeKey = "farmer_" + cropKey;
            int level = upgradeRepository.getUpgradeLevel(player.getUniqueId(), upgradeKey);
            int max = 3;
            int cost = (level == 0 ? 1 : 3);
            if (level >= max) {
                player.sendMessage("§cAmélioration farmeur ("+cropKey+") déjà au niveau max !");
                return;
            }
            if (profile.getUpgradeProductivite() < cost) {
                player.sendMessage("§cPas assez de points de productivité !");
                return;
            }
            profile.setUpgradeProductivite(profile.getUpgradeProductivite() - cost);
            profileManager.saveProfile(profile);
            upgradeRepository.setUpgradeLevel(player.getUniqueId(), upgradeKey, level + 1);
            player.sendMessage("§aAmélioration Farmeur ("+cropKey+") niveau " + (level + 1) + " achetée !");
            menuManager.openFarmeurUpgradeMenu(player, profile);
        }
    }

    // Protection contre le drag dans le menu
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals("Améliorations : Farmeur")) {
            event.setCancelled(true);
        }
    }
}
