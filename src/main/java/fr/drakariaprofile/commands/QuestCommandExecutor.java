package fr.drakariaprofile.quest;

import fr.drakariaprofile.menu.QuestMenuManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestCommandExecutor implements CommandExecutor {
    private final QuestManager questManager;

    public QuestCommandExecutor(QuestManager manager) {
        this.questManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        QuestCategory cat;
        switch (label.toLowerCase()) {
            case "quete_facile": cat = QuestCategory.FACILE; break;
            case "quete_moyen": cat = QuestCategory.MOYEN; break;
            case "quete_difficile": cat = QuestCategory.DIFFICILE; break;
            default: return false;
        }
        // Ouvre le GUI correspondant (voir QuestMenuManager ci-dessous)
        QuestMenuManager.openMenu(player, cat);
        return true;
    }
}
