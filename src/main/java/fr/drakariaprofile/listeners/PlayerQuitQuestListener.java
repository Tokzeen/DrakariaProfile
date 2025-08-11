package fr.drakariaprofile.listeners;

import fr.drakariaprofile.DrakariaProfile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitQuestListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Sauvegarde la progression des quêtes du joueur qui se déconnecte
        DrakariaProfile.getInstance()
                .getQuestManager()
                .savePlayerQuests(event.getPlayer().getUniqueId());
    }
}
