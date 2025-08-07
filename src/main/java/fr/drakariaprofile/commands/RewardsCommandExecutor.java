package fr.drakariaprofile.commands;

import fr.drakariaprofile.menu.MenuManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class RewardsCommandExecutor implements CommandExecutor {
    private final MenuManager menuManager;
    public RewardsCommandExecutor(MenuManager menuManager) { this.menuManager = menuManager; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Commande uniquement pour les joueurs.");
            return true;
        }
        menuManager.openRewardsMenu((Player) sender, 1);
        return true;
    }
}
