package fr.drakariaprofile.menu;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.profile.Profile;
import fr.drakariaprofile.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RewardsMenuListener implements Listener {
    private final MenuManager menuManager;
    private final ProfileManager profileManager;

    public RewardsMenuListener(MenuManager menuManager, ProfileManager profileManager) {
        this.menuManager = menuManager;
        this.profileManager = profileManager;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!event.getView().getTitle().startsWith("§6Récompenses")) return;
        event.setCancelled(true);

        int slotClicked = event.getRawSlot();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        int page = getPage(event.getView().getTitle());
        int[] rewardSlots = menuManager.getRewardSlots();

        // Navigation
        if (slotClicked == 45 && item.getType() == Material.ARROW) {
            menuManager.openRewardsMenu(player, Math.max(1, page - 1));
            return;
        }
        if (slotClicked == 53 && item.getType() == Material.ARROW) {
            menuManager.openRewardsMenu(player, Math.min(menuManager.getTotalPages(), page + 1));
            return;
        }

        // Trouver si case cliquer = case récompense, et calculer le bon "level" associé
        int i = -1;
        for (int z = 0; z < rewardSlots.length; z++) {
            if (rewardSlots[z] == slotClicked) {
                i = z;
                break;
            }
        }
        if (i == -1) return; // Pas dans la grille des coffres

        int level = (page - 1) * menuManager.getRewardsPerPage() + 1 + i; // ATTENTION +1 IMPORTANT !

        // Bloque si case "hors récompense" (ex page 3, cas où on a moins de 28 rewards à la fin)
        if (level > menuManager.totalRewards) return;

        Profile profile = profileManager.getProfile(player);

        if (profile.getClaimedRewards().contains(level)) return;
        if (profile.getLevel() < level) {
            player.sendMessage("§cTu dois être niveau " + level + " pour réclamer cette récompense !");
            return;
        }

        // Commande reward depuis level.yml (ou ta logique habituelle)
        String cmd = ConfigManager.getRewardForLevel(level);
        if (cmd != null && !cmd.isEmpty()) {
            cmd = cmd.replace("%player%", player.getName());
            player.getServer().dispatchCommand(player.getServer().getConsoleSender(), cmd);
        }
        // Marque la récompense comme claim
        profileManager.addClaimedReward(profile, level);
        player.sendMessage("§aTu as récupéré la récompense #" + level + " !");
        menuManager.openRewardsMenu(player, page);
    }

    private int getPage(String title) {
        try {
            String[] split = title.replace("§6Récompenses (Page ", "").replace(")", "").split("/");
            return Integer.parseInt(split[0]);
        } catch (Exception e) { return 1; }
    }
}
