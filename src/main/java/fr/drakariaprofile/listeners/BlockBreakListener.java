package fr.drakariaprofile.listeners;

import fr.drakariaprofile.profile.ProfileManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    private final ProfileManager profileManager;

    public BlockBreakListener(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInHand();

        // Vérifie si l'outil utilisé a Silk Touch
        if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
            // Si Silk Touch est présent, on ne donne pas d'XP
            return;
        }

        String block = event.getBlock().getType().name();
        double xp = profileManager.getXpForBlock(block);
        if (xp > 0) {
            profileManager.addXp(player, xp);
        }
    }
}