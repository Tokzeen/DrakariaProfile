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

        // Récupère le niveau de l'amélioration "chasseur"
        int chasseurLevel = profileManager.getUpgradeRepository().getUpgradeLevel(player.getUniqueId(), "hunter");

        // Calcul de la probabilité de drop xp (addition du bonus chasseur)
        double chance = mobConfig.chance;
        if (chasseurLevel == 1) chance += 15; // ajuste ce bonus à ta convenance
        else if (chasseurLevel == 2) chance += 50;
        chance = Math.min(chance, 100.0); // clamp à 100%

        double roll = ThreadLocalRandom.current().nextDouble(0, 100);
        if (roll < chance) { // À 100%, c'est garanti
            profileManager.addXp(player, mobConfig.xp);
        }
    }
}
