package fr.drakariaprofile.menu;

import fr.drakariaprofile.box.BoxRewardManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BoxRewardMenu {
    public static void openLootbox(Player player, String boxName, BoxRewardManager manager, Runnable rewardAction) {
        Inventory inv = Bukkit.createInventory(null, 27, "Ouverture de " + boxName);
        setDecor(inv);

        List<BoxRewardManager.RarityReward> allRarities = manager.getRarities(boxName);
        // Filtre les raretés SANS items
        List<BoxRewardManager.RarityReward> rarities = new ArrayList<>();
        for (BoxRewardManager.RarityReward rr : allRarities)
            if (rr.items != null && !rr.items.isEmpty())
                rarities.add(rr);
        if (rarities.isEmpty()) {
            player.sendMessage("§cErreur: aucune récompense configurée !");
            return;
        }

        Random r = new Random();
        int animationLength = 20;
        int slotsCount = 7;

        List<BoxRewardManager.RarityReward> sequence = new ArrayList<>();
        for (int i = 0; i < animationLength; i++) {
            sequence.add(rarities.get(r.nextInt(rarities.size())));
        }

        BoxRewardManager.RarityReward finalReward = manager.drawReward(boxName);
        if (finalReward == null || finalReward.items == null || finalReward.items.isEmpty()) {
            player.sendMessage("§cErreur: aucune récompense attribuée (config incomplète).");
            return;
        }

        for (int i = 0; i < slotsCount; i++) {
            sequence.add(finalReward);
        }

        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                for (int s = 0; s < slotsCount; s++) {
                    int slot = 10 + s;
                    int index = tick + s;
                    BoxRewardManager.RarityReward reward = sequence.get(Math.min(index, sequence.size() - 1));
                    inv.setItem(slot, getWoolReward(reward));
                }
                player.openInventory(inv);
                if (tick >= sequence.size() - slotsCount) {
                    player.sendMessage("§aTu as gagné : " + finalReward.display);
                    rewardAction.run();
                    cancel();
                }
                tick++;
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("DrakariaProfile"), 1L, 5L);
    }


    // Génére une laine de couleur selon rareté
    private static ItemStack getWoolReward(BoxRewardManager.RarityReward rarity) {
        ItemStack wool = new ItemStack(Material.WOOL, 1, getWoolColor(rarity.color));
        ItemMeta meta = wool.getItemMeta();
        meta.setDisplayName(rarity.display);
        wool.setItemMeta(meta);
        return wool;
    }

    // Couleur => data byte (Minecraft 1.8)
    private static byte getWoolColor(String color) {
        switch (color.toUpperCase()) {
            case "WHITE":        return 0;
            case "RED":          return 14;
            case "YELLOW":       return 4;
            case "AQUA":         return 9;
            case "LIGHT_PURPLE": return 2; // Magenta (ou adapte selon effet)
            default:             return 0;
        }
    }

    // Place vitres rouges et barrières
    private static void setDecor(Inventory inv) {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
        ItemStack ironFence = new ItemStack(Material.IRON_FENCE, 1);
        // Ligne 0 : slots 0,1,2,3,5,6,7,8 => glass ; 4 => ironFence
        int[] decoSlots = {0,1,2,3,5,6,7,8,18,19,20,21,23,24,25,26};
        for (int i : decoSlots) inv.setItem(i, glass);
        inv.setItem(4, ironFence);
        inv.setItem(22, ironFence);
        inv.setItem(9, glass);
        inv.setItem(17, glass);
    }
}
