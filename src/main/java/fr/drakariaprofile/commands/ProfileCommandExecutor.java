package fr.drakariaprofile.commands;

import fr.drakariaprofile.profile.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ProfileCommandExecutor implements CommandExecutor {
    private final ProfileManager profileManager;
    public ProfileCommandExecutor(ProfileManager profileManager) { this.profileManager = profileManager; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("drakariaprofile.admin")) {
            sender.sendMessage("§cPermission refusée.");
            return true;
        }
        if (args.length == 0) {
            if (sender instanceof Player)
                sender.sendMessage("§bVotre xp: " + profileManager.getXp((Player)sender));
            else sender.sendMessage("§cCommande réservée à un joueur.");
            return true;
        }

        if ("give".equalsIgnoreCase(args[0]) && args.length == 3) {
            Player cible = Bukkit.getPlayerExact(args[1]);
            double add;
            try { add = Double.parseDouble(args[2]); } catch (Exception ex) { sender.sendMessage("§cValeur xp invalide."); return true; }
            if (cible == null) { sender.sendMessage("§cJoueur introuvable."); return true; }
            profileManager.addXp(cible, add);
            sender.sendMessage("§aAjouté §b" + add + " xp §aà §e" + cible.getName());
            cible.sendMessage("§aTu as reçu §b" + add + " xp §a!");
            return true;
        }

        if ("remove".equalsIgnoreCase(args[0]) && args.length == 3) {
            Player cible = Bukkit.getPlayerExact(args[1]);
            double rm;
            try { rm = Double.parseDouble(args[2]); } catch (Exception ex) { sender.sendMessage("§cValeur xp invalide."); return true; }
            if (cible == null) { sender.sendMessage("§cJoueur introuvable."); return true; }
            profileManager.removeXp(cible, rm);
            sender.sendMessage("§cRetiré §b" + rm + " xp §cà §e" + cible.getName());
            cible.sendMessage("§cOn t'a retiré §b" + rm + " xp.");
            return true;
        }

        if ("freeze".equalsIgnoreCase(args[0]) && args.length == 2) {
            Player cible = Bukkit.getPlayerExact(args[1]);
            if (cible == null) { sender.sendMessage("§cJoueur introuvable."); return true; }
            profileManager.setFreeze(cible, true);
            sender.sendMessage("§e" + cible.getName() + " §ca été §cfreeze !");
            cible.sendMessage("§cTu as été freeze par un administrateur !");
            return true;
        }

        if ("unfreeze".equalsIgnoreCase(args[0]) && args.length == 2) {
            Player cible = Bukkit.getPlayerExact(args[1]);
            if (cible == null) { sender.sendMessage("§cJoueur introuvable."); return true; }
            profileManager.setFreeze(cible, false);
            sender.sendMessage("§e" + cible.getName() + " §ca été §adéfreeze !");
            cible.sendMessage("§aTu as été défreeze !");
            return true;
        }

        if (args.length == 1) {
            Player cible = Bukkit.getPlayerExact(args[0]);
            if (cible == null) { sender.sendMessage("§cJoueur introuvable."); return true; }
            sender.sendMessage("§bXp de " + cible.getName() + ": " + profileManager.getXp(cible));
            return true;
        }
        sender.sendMessage("§eUsage:\n/drakariaProfile [joueur]\n/drakariaProfile give <joueur> <xp>\n/drakariaProfile remove <joueur> <xp>\n/drakariaProfile freeze <joueur>\n/drakariaProfile unfreeze <joueur>");
        return true;
    }
}
