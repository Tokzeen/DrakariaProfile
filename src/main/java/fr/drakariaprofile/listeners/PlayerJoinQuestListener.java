package fr.drakariaprofile.listeners;

import fr.drakariaprofile.DrakariaProfile;
import fr.drakariaprofile.quest.QuestManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinQuestListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        QuestManager qm = DrakariaProfile.getInstance().getQuestManager();
        // Force la création des data et l'assignation si vide
        if (qm.getPlayerData(event.getPlayer().getUniqueId()).getAssignedQuests().isEmpty()) {
            qm.assignDailyQuests(event.getPlayer());
        }
    }
}
