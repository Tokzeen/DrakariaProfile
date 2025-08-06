package fr.drakariaprofile.commands;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.profile.Profile;
import fr.drakariaprofile.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

public class ProfileCommandExecutor implements CommandExecutor {
    private final ProfileManager profileManager;
    public ProfileCommandExecutor(ProfileManager profileManager) { this.profileManager = profileManager; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("drakariaprofile.admin")) {
            sender.sendMessage("§cPermission refusée.");
            return true;
        }
        // /drakariaProfile (info de soi)
        if (args.length == 0) {
            if (sender instanceof Player) {
                String msg = "§bVotre niveau: §e%player_level% §7| §aXP: §b%player_xp%§7/§c%player_max_xp%";
                sender.sendMessage(profileManager.replacePlaceholders((Player)sender, msg));
            } else {
                sender.sendMessage("§cCommande réservée à un joueur.");
            }
            return true;
        }

        // /drakariaProfile give/remove/freeze/unfreeze restent réservés online uniquement
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

        // /drakariaProfile rewards <joueur> <level> : supporte offline
        if ("rewards".equalsIgnoreCase(args[0]) && args.length == 3) {
            OfflinePlayer cible = Bukkit.getOfflinePlayer(args[1]);
            int level;
            try { level = Integer.parseInt(args[2]); } catch (Exception ex) { sender.sendMessage("§cLevel invalide."); return true; }
            if (cible == null || cible.getUniqueId() == null || cible.getName() == null) { sender.sendMessage("§cJoueur introuvable."); return true; }
            Profile profile = profileManager.getProfile(cible.getUniqueId(), cible.getName());

            if (profile.getClaimedRewards().contains(level)) {
                sender.sendMessage("§cLa récompense du niveau " + level + " a déjà été récupérée !");
                return true;
            }
            if (profile.getLevel() < level) {
                sender.sendMessage("§cLe joueur n'a pas encore atteint le niveau " + level);
                return true;
            }
            String rewardCmd = ConfigManager.getRewardForLevel(level);
            if (rewardCmd == null || rewardCmd.isEmpty()) {
                sender.sendMessage("§cAucune récompense définie pour ce niveau !");
                return true;
            }
            rewardCmd = rewardCmd.replace("%player%", cible.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rewardCmd);
            profileManager.addClaimedReward((Player) profile, level);

            sender.sendMessage("§aRécompense du niveau " + level + " donnée à " + cible.getName() + " !");
            if (cible.isOnline() && cible.getPlayer() != null) {
                cible.getPlayer().sendMessage("§6Tu as récupéré la récompense du niveau " + level + " !");
            }
            return true;
        }

        // /drakariaProfile <joueur> : supporte offline
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

        sender.sendMessage("§eUsage:\n"
                + "/drakariaProfile [joueur]\n"
                + "/drakariaProfile give <joueur> <xp>\n"
                + "/drakariaProfile remove <joueur> <xp>\n"
                + "/drakariaProfile freeze <joueur>\n"
                + "/drakariaProfile unfreeze <joueur>\n"
                + "/drakariaProfile rewards <joueur> <level>");
        return true;
    }
}
