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
        Profile profile = profileManager.getProfile(player);

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("mineur")) {
                menuManager.openMineurUpgradeMenu(player, profile);
                return true;
            }
            if (args[0].equalsIgnoreCase("chasseur")) {
                menuManager.openChasseurUpgradeMenu(player, profile);
                return true;
            }
            if (args[0].equalsIgnoreCase("farmeur")) {
                menuManager.openFarmeurUpgradeMenu(player, profile);
                return true;
            }
        }
        sender.sendMessage("§eUsage : /ameliorations mineur, /ameliorations chasseur, ou /ameliorations farmeur");
        return true;
    }
}
