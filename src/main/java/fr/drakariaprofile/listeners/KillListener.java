package fr.drakariaprofile.listeners;

import fr.drakariaprofile.config.ConfigManager;
import fr.drakariaprofile.profile.ProfileManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KillListener implements Listener {
    private final ProfileManager profileManager;

    public KillListener(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Vérifie que la victime est un Player et que le tueur est aussi un Player
        if (event.getEntity() instanceof Player && event.getEntity().getKiller() instanceof Player) {
            Player killer = event.getEntity().getKiller();
            double killXp = ConfigManager.getKillXp();
            profileManager.addXp(killer, killXp);
        }
    }

}
