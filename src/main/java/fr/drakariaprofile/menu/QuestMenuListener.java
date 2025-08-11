package fr.drakariaprofile.menu;

import fr.drakariaprofile.DrakariaProfile;
import fr.drakariaprofile.profile.Profile;
import fr.drakariaprofile.quest.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class QuestMenuListener implements Listener {

    @EventHandler
    public void onQuestMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (title == null || !title.startsWith("Quêtes ")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        // Quitter (slot 8)
        if (event.getSlot() == 8) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.CLICK, 1f, 1f);
            player.performCommand("quest");
            return;
        }

        QuestCategory category;
        try {
            category = QuestCategory.valueOf(title.replace("Quêtes ", "").toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }

        QuestManager qm = DrakariaProfile.getInstance().getQuestManager();
        QuestPlayerData data = qm.getPlayerData(player.getUniqueId());
        if (data == null) return;
        List<PlayerQuestProgress> quests = data.getQuests(category);

        // Coffre bonus (slot 6)
        if (event.getSlot() == 6 && event.getCurrentItem().getType() == Material.CHEST) {
            boolean allDone = quests.size() == 3 && quests.stream().allMatch(PlayerQuestProgress::isComplete);
            if (allDone && !data.isBonusClaimed(category)) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "give " + player.getName() + " diamond 5");
                player.sendMessage(ChatColor.GOLD + "Bravo ! Tu as pris ta récompense bonus !");
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
                data.setBonusClaimed(category, true);
                qm.savePlayerQuests(player.getUniqueId());
            } else if (data.isBonusClaimed(category)) {
                player.sendMessage(ChatColor.RED + "Tu as déjà récupéré la récompense bonus !");
            } else {
                player.sendMessage(ChatColor.RED + "Complète d'abord toutes les quêtes !");
            }
            QuestMenuManager.updateMenuVisual(player.getOpenInventory().getTopInventory(), quests, category, data);
            player.updateInventory();
            return;
        }

        // Quêtes (slots 2-4)
        if (event.getSlot() >= 2 && event.getSlot() <= 4) {
            int questIdx = event.getSlot() - 2;
            if (questIdx < quests.size()) {
                PlayerQuestProgress pq = quests.get(questIdx);

                if (!pq.isComplete()) {
                    player.sendMessage(ChatColor.RED + "Cette quête n'est pas encore complétée !");
                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
                } else if (!pq.isConsumed()) {
                    QuestReward reward = pq.getQuest().getReward();
                    data.addPoints(reward.getPoints());

                    // Après data.addPoints(reward.getPoints());
                    Profile profile = DrakariaProfile.getInstance().getProfileManager().getProfile(player);
                    profile.addQuestPoints(reward.getPoints());
                    DrakariaProfile.getInstance().getProfileManager().saveProfile(profile);


                    if (reward.getCommand() != null && !reward.getCommand().isEmpty()) {
                        String cmd = reward.getCommand().replace("%player%", player.getName());
                        if (cmd.startsWith("/")) cmd = cmd.substring(1);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    }

                    int xpGain = getRandomNumber(reward.getXpMin(), reward.getXpMax());
                    DrakariaProfile.getInstance().getProfileManager().addXp(player, xpGain);

                    player.sendMessage(ChatColor.GREEN + "Quête validée ! §e+" + reward.getPoints()
                            + " points§a, +" + xpGain + " XP");
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);

                    pq.setConsumed(true);
                    qm.savePlayerQuests(player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Tu as déjà récupéré la récompense de cette quête.");
                }
            }
            QuestMenuManager.updateMenuVisual(player.getOpenInventory().getTopInventory(), quests, category, data);
            player.updateInventory();
        }
    }

    private int getRandomNumber(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }
}
