package fr.drakariaprofile.menu;

import fr.drakariaprofile.DrakariaProfile;
import fr.drakariaprofile.quest.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class QuestMenuManager {

    public static void openMenu(Player player, QuestCategory cat) {
        Inventory inv = org.bukkit.Bukkit.createInventory(null, 9, "Quêtes " + cat.name());

        // Barreaux en fer (slots 0,1,5,7)
        ItemStack ironBar = createIronBar();
        inv.setItem(0, ironBar);
        inv.setItem(1, ironBar);
        inv.setItem(5, ironBar);
        inv.setItem(7, ironBar);

        QuestManager questManager = DrakariaProfile.getInstance().getQuestManager();
        QuestPlayerData data = questManager.getPlayerData(player.getUniqueId());
        List<PlayerQuestProgress> quests = data.getQuests(cat);

        // Si aucune quête, on les assigne
        if (quests.isEmpty()) {
            questManager.assignDailyQuests(player);
            quests = data.getQuests(cat);
        }

        // Slots 2,3,4 : quêtes ou barreaux
        for (int i = 0; i < 3; i++) {
            inv.setItem(2 + i, i < quests.size() ? createQuestBanner(quests.get(i)) : ironBar);
        }

        // Slot 6 : coffre bonus si 3 quêtes complètes et pas encore réclamé
        boolean allComplete = quests.size() == 3 &&
                quests.get(0).isComplete() &&
                quests.get(1).isComplete() &&
                quests.get(2).isComplete();

        if (allComplete && !data.isBonusClaimed(cat)) {
            inv.setItem(6, createChestBonus());
        } else {
            inv.setItem(6, ironBar);
        }

        // Slot 8 : bouton quitter
        inv.setItem(8, createQuitButton());

        player.openInventory(inv);
    }

    /** Met à jour le menu en direct sans fermeture pour un joueur déjà dedans */
    public static void updateMenuVisual(Inventory inv, List<PlayerQuestProgress> quests, QuestCategory cat, QuestPlayerData data) {
        ItemStack ironBar = createIronBar();

        // Quêtes
        for (int i = 0; i < 3; i++) {
            inv.setItem(2 + i, i < quests.size() ? createQuestBanner(quests.get(i)) : ironBar);
        }

        // Bonus
        boolean allComplete = quests.size() == 3 &&
                quests.get(0).isComplete() &&
                quests.get(1).isComplete() &&
                quests.get(2).isComplete();

        if (allComplete && !data.isBonusClaimed(cat)) {
            inv.setItem(6, createChestBonus());
        } else {
            inv.setItem(6, ironBar);
        }

        // Bouton quitter
        inv.setItem(8, createQuitButton());
    }

    /** Création d’une bannière colorée selon l’état de la quête (1.8) */
    private static ItemStack createQuestBanner(PlayerQuestProgress pq) {
        int current = pq.getProgress();
        int max = pq.getQuest().getAmount();

        short RED = 1, ORANGE = 14, GREEN = 10;
        short color = RED;
        String status = "§cQuête non commencée";

        if (pq.isComplete()) {
            color = GREEN;
            status = "§aQuête terminée";
        } else if (current > 0) {
            color = ORANGE;
            status = "§6Quête en progression";
        }

        ItemStack banner = new ItemStack(Material.BANNER, 1, color);
        ItemMeta meta = banner.getItemMeta();

        List<String> lore = new ArrayList<>();
        lore.add(pq.isComplete() ?
                "§aComplétée !" :
                "§7Progression : §e" + current + "§7/§e" + max);

        ItemMeta questMeta = pq.getQuest().getDisplayItem().getItemMeta();
        if (questMeta != null && questMeta.hasLore()) {
            lore.addAll(questMeta.getLore());
        }

        if (meta != null) {
            meta.setDisplayName(
                    questMeta != null && questMeta.getDisplayName() != null ? questMeta.getDisplayName() : status
            );
            meta.setLore(lore);
            banner.setItemMeta(meta);
        }
        return banner;
    }

    private static ItemStack createIronBar() {
        ItemStack ironBar = new ItemStack(Material.IRON_FENCE);
        ItemMeta meta = ironBar.getItemMeta();
        if (meta != null) meta.setDisplayName("§f");
        ironBar.setItemMeta(meta);
        return ironBar;
    }

    private static ItemStack createChestBonus() {
        ItemStack chest = new ItemStack(Material.CHEST);
        ItemMeta meta = chest.getItemMeta();
        if (meta != null) meta.setDisplayName("§6Récompense bonus");
        chest.setItemMeta(meta);
        return chest;
    }

    private static ItemStack createQuitButton() {
        ItemStack quit = new ItemStack(Material.INK_SACK, 1, (short) 1); // colorant rouge en 1.8
        ItemMeta meta = quit.getItemMeta();
        if (meta != null) meta.setDisplayName("§cQuitter");
        quit.setItemMeta(meta);
        return quit;
    }
}
