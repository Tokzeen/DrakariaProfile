package fr.drakariaprofile.commands;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ProfilePublicCommandExecutor implements CommandExecutor {
    private final ProfileManager profileManager;
    public ProfilePublicCommandExecutor(ProfileManager profileManager) { this.profileManager = profileManager; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                String msg = "§bVotre niveau: §e%player_level% §7| §aXP: §b%player_xp%§7/§c%player_max_xp%";
                sender.sendMessage(profileManager.replacePlaceholders((Player)sender, msg));
            } else {
                sender.sendMessage("§cCommande réservée à un joueur.");
            }
            return true;
        }

        // /profile <joueur> (support offline)
        if (args.length == 1) {
            OfflinePlayer cible = Bukkit.getOfflinePlayer(args[0]);
            if (cible == null || cible.getUniqueId() == null || cible.getName() == null) {
                sender.sendMessage("§cJoueur introuvable.");
                return true;
            }
            Profile profile = profileManager.getProfile(cible.getUniqueId(), cible.getName());
            if (profile == null) {
                sender.sendMessage("§cAucun profil trouvé pour ce joueur.");
                return true;
            }
            String msg = "§b" + cible.getName() + " §7| §eNiveau: %player_level% §7| §aXP: §b%player_xp%§7/§c%player_max_xp%";
            sender.sendMessage(profileManager.replacePlaceholders(profile, msg));
            return true;
        }

        sender.sendMessage("§eUsage:\n/profile [joueur]");
        return true;
    }
}
