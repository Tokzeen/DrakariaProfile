package fr.drakariaprofile.commands;

import fr.drakariaprofile.DrakariaProfile;
import fr.drakariaprofile.quest.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class QuestAdminCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "Usage: /drakariaquest quest <facile|moyen|difficile> <1-3> <joueur> <end|reset|restart>");
            return true;
        }

        String sub = args[0]; // quest
        String catStr = args[1];
        int questIndex;
        try {
            questIndex = Integer.parseInt(args[2]) - 1; // index 0-based
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Le numéro de quête doit être un entier 1-3 !");
            return true;
        }

        String playerName = args[3];
        String action = args[4].toLowerCase();

        QuestCategory category;
        try {
            category = QuestCategory.valueOf(catStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Catégorie invalide. Utilise: facile, moyen, difficile");
            return true;
        }

        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable ou hors ligne !");
            return true;
        }

        QuestManager qm = DrakariaProfile.getInstance().getQuestManager();
        QuestPlayerData data = qm.getPlayerData(target.getUniqueId());
        List<PlayerQuestProgress> quests = data.getQuests(category);

        if (questIndex < 0 || questIndex >= quests.size()) {
            sender.sendMessage(ChatColor.RED + "Index de quête invalide !");
            return true;
        }

        PlayerQuestProgress pq = quests.get(questIndex);

        switch (action) {
            case "end":
                pq.setProgress(pq.getQuest().getAmount());
                pq.setConsumed(false); // pas encore pris récompense
                sender.sendMessage(ChatColor.GREEN + "Quête marquée comme terminée pour " + target.getName());
                break;

            case "reset": {
                List<Quest> pool = qm.getQuestsByCategory(category);
                if (pool.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Aucune quête disponible dans cette catégorie !");
                    return true;
                }
                Collections.shuffle(pool);
                PlayerQuestProgress newQuest = new PlayerQuestProgress(pool.get(0)); // nouvelle quête
                quests.set(questIndex, newQuest);
                sender.sendMessage(ChatColor.YELLOW + "Quête réinitialisée pour " + target.getName() + " avec un nouvel objectif.");
                break;
            }



            case "restart":
                // remplacer par une nouvelle quête
                List<Quest> pool = qm.getQuestsByCategory(category);
                pool.removeIf(q -> quests.contains(new PlayerQuestProgress(q))); // éviter doublons
                if (pool.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Pas de nouvelles quêtes disponibles !");
                    return true;
                }
                java.util.Collections.shuffle(pool);
                PlayerQuestProgress newQuest = new PlayerQuestProgress(pool.get(0));
                quests.set(questIndex, newQuest);
                sender.sendMessage(ChatColor.AQUA + "Nouvelle quête assignée à " + target.getName());
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Action invalide ! Utilise: end, reset, restart");
                return true;
        }

        qm.savePlayerQuests(target.getUniqueId()); // sauver SQL
        return true;
    }
}
