package fr.drakariaprofile.listeners;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.DrakariaProfile;
import org.bukkit.entity.EntityType;
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

        // Calculate if xp should be given
        double roll = ThreadLocalRandom.current().nextDouble(0, 100);
        if (roll <= mobConfig.chance) {
            profileManager.addXp(player, mobConfig.xp);
        }
    }
}
