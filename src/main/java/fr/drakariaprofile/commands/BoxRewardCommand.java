package fr.drakariaprofile.commands;

import fr.drakariaprofile.box.BoxRewardManager;
import fr.drakariaprofile.menu.BoxRewardMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoxRewardCommand implements CommandExecutor {
    private final BoxRewardManager rewardManager;

    public BoxRewardCommand(BoxRewardManager rewardManager) {
        this.rewardManager = rewardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("drakariareward.admin")) {
            sender.sendMessage("§cTu n'as pas la permission !");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /drakariareward <player> <boxName>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cCe joueur n'est pas connecté.");
            return true;
        }

        String boxName = args[1];

        BoxRewardMenu.openLootbox(target, boxName, rewardManager, () -> {
            BoxRewardManager.RarityReward reward = rewardManager.drawReward(boxName);
            String commandToRun = reward.items.get(0).replace("{player}", target.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToRun);
        });

        sender.sendMessage("§aAnimation lancée pour " + target.getName());
        return true;

    }
}
