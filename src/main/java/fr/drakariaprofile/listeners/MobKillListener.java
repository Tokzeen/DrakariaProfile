package fr.drakariaprofile.listeners;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.DrakariaProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.concurrent.ThreadLocalRandom;

public class MobKillListener implements Listener {
    private final ProfileManager profileManager;

    public MobKillListener(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        if (profileManager.isFrozen(player)) return;

        String mobKey = event.getEntityType().name();
        DrakariaProfile.MobXpConfig mobConfig = profileManager.getMobXpConfig(mobKey);
        if (mobConfig == null || mobConfig.xp <= 0) return;

        // Prend le niveau d'amélioration pour ce mob
        int mobChasseurLevel = profileManager.getUpgradeRepository().getUpgradeLevel(player.getUniqueId(), "hunter_" + mobKey);

        // Palier = même logique que menu
        int[] paliers = fr.drakariaprofile.menu.ChasseurUpgradeMenu.getMobChasseurUpgradeSteps(mobKey);

        double xpChance = mobConfig.chance;
        if (mobChasseurLevel > 0) {
            // Additionne le bonus de palier
            for (int i = 0; i < mobChasseurLevel && i < paliers.length; i++) {
                xpChance += paliers[i];
            }
        }
        if (mobChasseurLevel >= paliers.length) xpChance = 100;
        xpChance = Math.min(xpChance, 100);

        double roll = ThreadLocalRandom.current().nextDouble(0, 100);
        if (roll < xpChance) {
            profileManager.addXp(player, mobConfig.xp);
        }
    }
}
