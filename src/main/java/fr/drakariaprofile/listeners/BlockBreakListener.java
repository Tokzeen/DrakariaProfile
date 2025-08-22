package fr.drakariaprofile.listeners;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.utils.VaultHook;
import org.bukkit.Material;
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

        if (profileManager.isFrozen(player)) return;

        ItemStack tool = player.getInventory().getItemInHand();
        if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH)) return;

        Material blockType = event.getBlock().getType();
        String upgradeKey = blockType.name().toLowerCase();

        int upgradeLevel = profileManager.getUpgradeRepository().getUpgradeLevel(player.getUniqueId(), upgradeKey);

        double xp = 0;
        double money = 0;
        if (blockType == Material.STONE) {
            if (upgradeLevel == 1) xp = 0.2;
            else if (upgradeLevel == 2) { xp = 0.2; money = 0.10; }
        }

        // Ajouter d'autres upgrades ici plus tard...

        if (xp == 0) {
            xp = profileManager.getXpForBlock(blockType.name());
        }

        // LIGNE CLEF : stacke tout sur l'action bar, reset auto après délai
        profileManager.addXpAndMoney(player, xp, money);
    }
}
