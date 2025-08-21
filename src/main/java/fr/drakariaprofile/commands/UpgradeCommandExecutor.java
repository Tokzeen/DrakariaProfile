package fr.drakariaprofile.commands;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class UpgradeCommandExecutor implements CommandExecutor {
    private final ProfileManager profileManager;

    public UpgradeCommandExecutor(ProfileManager pm) {
        this.profileManager = pm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("drakariaprofile.admin")) {
            sender.sendMessage("§cPermission refusée.");
            return true;
        }
        if (args.length != 4) {
            sender.sendMessage("§cUtilisation : /upgrade give|remove chance|productivite <joueur> <quantité>");
            return true;
        }
        String action = args[0];
        String type = args[1];
        String pseudo = args[2];
        int quantite;

        try { quantite = Integer.parseInt(args[3]); }
        catch (Exception e) {
            sender.sendMessage("§cQuantité invalide.");
            return true;
        }
        Player cible = Bukkit.getPlayerExact(pseudo);
        if (cible == null) {
            sender.sendMessage("§cJoueur introuvable.");
            return true;
        }
        Profile profile = profileManager.getProfile(cible);

        if ("give".equalsIgnoreCase(action)) {
            if ("chance".equalsIgnoreCase(type)) profile.setUpgradeChance(profile.getUpgradeChance() + quantite);
            else if ("productivite".equalsIgnoreCase(type)) profile.setUpgradeProductivite(profile.getUpgradeProductivite() + quantite);
            else { sender.sendMessage("§cType invalide."); return true; }
        } else if ("remove".equalsIgnoreCase(action)) {
            if ("chance".equalsIgnoreCase(type)) profile.setUpgradeChance(Math.max(0, profile.getUpgradeChance() - quantite));
            else if ("productivite".equalsIgnoreCase(type)) profile.setUpgradeProductivite(Math.max(0, profile.getUpgradeProductivite() - quantite));
            else { sender.sendMessage("§cType invalide."); return true; }
        } else {
            sender.sendMessage("§cAction invalide.");
            return true;
        }
        profileManager.saveProfile(profile);
        sender.sendMessage("§aNouveaux points " + type + " de " + pseudo + " : " + (
                "chance".equalsIgnoreCase(type) ? profile.getUpgradeChance() : profile.getUpgradeProductivite()));
        return true;
    }
}
