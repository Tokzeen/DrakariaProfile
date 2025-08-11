package fr.drakariaprofile.listeners;

import fr.drakariaprofile.DrakariaProfile;
import fr.drakariaprofile.quest.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BlockBreakQuestListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        // Vérifie l'outil et supprime la progression si Silk Touch
        ItemStack tool = player.getItemInHand(); // 1.8 API
        boolean hasSilkTouch = tool != null && tool.hasItemMeta()
                && tool.getEnchantments().containsKey(Enchantment.SILK_TOUCH);
        if (hasSilkTouch) return;

        QuestManager qm = DrakariaProfile.getInstance().getQuestManager();
        if (qm == null) return;

        // Récupère les quêtes du joueur dans chaque difficulté
        for (QuestCategory cat : QuestCategory.values()) {
            QuestPlayerData data = qm.getPlayerData(player.getUniqueId());
            if (data == null) continue;
            List<PlayerQuestProgress> quests = data.getQuests(cat);

            // Vérifie s'il a au moins une quête "break_*" (pas obligatoirement "_ore" maintenant)
            boolean hasBreakQuest = false;
            for (PlayerQuestProgress pq : quests) {
                Quest quest = pq.getQuest();
                if (!pq.isComplete() && !pq.isConsumed() && quest.getId().startsWith("break_")) {
                    hasBreakQuest = true;
                    break;
                }
            }
            if (!hasBreakQuest) continue;

            // Ajoute la progression uniquement sur les quêtes correspondantes au bloc
            for (PlayerQuestProgress pq : quests) {
                Quest quest = pq.getQuest();
                if (!pq.isComplete() && !pq.isConsumed() && quest.getId().startsWith("break_")) {

                    // Association id -> bloc cassé
                    if (quest.getId().equalsIgnoreCase("break_stone") && blockType == Material.STONE) {
                        pq.addProgress(1);
                    }
                    else if (quest.getId().equalsIgnoreCase("break_coal_ore") && blockType == Material.COAL_ORE) {
                        pq.addProgress(1);
                    }
                    else if (quest.getId().equalsIgnoreCase("break_iron_ore") && blockType == Material.IRON_ORE) {
                        pq.addProgress(1);
                    }
                    else if (quest.getId().equalsIgnoreCase("break_diamond_ore") && blockType == Material.DIAMOND_ORE) {
                        pq.addProgress(1);
                    }
                    else if (quest.getId().equalsIgnoreCase("break_redstone_ore")
                            && (blockType == Material.REDSTONE_ORE || blockType == Material.GLOWING_REDSTONE_ORE)) {
                        pq.addProgress(1);
                    }
                    else if (quest.getId().equalsIgnoreCase("break_lapis_ore") && blockType == Material.LAPIS_ORE) {
                        pq.addProgress(1);
                    }
                }
            }
        }
    }
}
