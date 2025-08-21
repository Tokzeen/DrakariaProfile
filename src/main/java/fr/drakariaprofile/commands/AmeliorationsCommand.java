package fr.drakariaprofile.commands;

import fr.drakariaprofile.menu.MenuManager;
import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.profile.Profile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AmeliorationsCommand implements CommandExecutor {
    private final MenuManager menuManager;
    private final ProfileManager profileManager;

    public AmeliorationsCommand(MenuManager menuManager, ProfileManager profileManager) {
        this.menuManager = menuManager;
        this.profileManager = profileManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length >= 1 && args[0].equalsIgnoreCase("mineur")) {
            Profile profile = profileManager.getProfile(player);
            menuManager.openMineurUpgradeMenu(player, profile);
            return true;
        }
        sender.sendMessage("§eUsage : /ameliorations mineur");
        return true;
    }
}
