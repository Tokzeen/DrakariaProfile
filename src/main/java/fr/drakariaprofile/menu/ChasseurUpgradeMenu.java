package fr.drakariaprofile.menu;

import fr.drakariaprofile.profile.Profile;
import fr.drakariaprofile.DrakariaProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class ChasseurUpgradeMenu {

    private static final Map<String, Short> mobEggData = new HashMap<>();
    static {
        mobEggData.put("ZOMBIE", (short)54);
        mobEggData.put("SKELETON", (short)51);
        mobEggData.put("CREEPER", (short)50);
        mobEggData.put("SPIDER", (short)52);
        mobEggData.put("PIG", (short)90);
        mobEggData.put("COW", (short)92);
        mobEggData.put("CHICKEN", (short)93);
        mobEggData.put("SHEEP", (short)91);
        mobEggData.put("RABBIT", (short)101);
        mobEggData.put("MOOSHROOM", (short)96);
        mobEggData.put("ENDERMAN", (short)58);
        mobEggData.put("BLAZE", (short)61);
        mobEggData.put("CAVE_SPIDER", (short)59);
        mobEggData.put("WITCH", (short)66);
        mobEggData.put("ZOMBIE_PIGMAN", (short)57);
    }

    public static int[] getMobChasseurUpgradeSteps(String mobKey) {
        switch (mobKey) {
            default: return new int[] {30, 100};
        }
    }

    public static void openChasseurUpgradeMenu(Player player, Profile profile) {
        Inventory inv = Bukkit.createInventory(null, 54, "Améliorations : Chasseur");

        // Bordure en verre rouge (slots du contour seulement – pas le milieu !)
        ItemStack redGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)14);
        ItemMeta glassMeta = redGlass.getItemMeta(); glassMeta.setDisplayName(" "); redGlass.setItemMeta(glassMeta);
        for (int i = 0; i < 54; i++) {
            boolean isContour =
                    i < 9 || i > 44 ||           // haut/bas
                            i % 9 == 0 || i % 9 == 8;    // côtés gauche/droite
            if (isContour) inv.setItem(i, redGlass);
        }

        // Slot 4 : tête joueur
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        meta.setDisplayName("§e" + player.getName());
        meta.setLore(Arrays.asList("§7Niveau : §a" + profile.getLevel(), "§7XP : §b" + String.format("%.2f", profile.getXp())));
        skull.setItemMeta(meta);
        inv.setItem(4, skull);

        // Slot 8 : nether star - points dispo
        ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta starMeta = netherStar.getItemMeta();
        starMeta.setDisplayName("§eVos Points d'upgrade");
        List<String> starLore = new ArrayList<>();
        starLore.add("§7Productivité : §b" + profile.getUpgradeProductivite());
        starLore.add("§7Chance : §b" + profile.getUpgradeChance());
        starMeta.setLore(starLore);
        netherStar.setItemMeta(starMeta);
        inv.setItem(8, netherStar);

        // Slot 45 : quitter
        ItemStack quit = new ItemStack(Material.REDSTONE);
        ItemMeta quitMeta = quit.getItemMeta();
        quitMeta.setDisplayName("§cQuitter le menu");
        quit.setItemMeta(quitMeta);
        inv.setItem(45, quit);

        // Milieu (slots internes) : barres de fer par défaut
        ItemStack ironBar = new ItemStack(Material.IRON_FENCE);
        ItemMeta ironMeta = ironBar.getItemMeta(); ironMeta.setDisplayName(" "); ironBar.setItemMeta(ironMeta);
        int[] upgradeSlots = {
                10,11,12,13,14,15,16,
                19,20,21,22,23,24,25,
                28,29,30,31,32,33,34,
                37,38,39,40,41,42,43
        };
        for (int slot : upgradeSlots) {
            inv.setItem(slot, ironBar);
        }

        // Place les upgrades (oeufs) sur le milieu en remplaçant les barreaux
        Map<String, DrakariaProfile.MobXpConfig> mobXpMap = DrakariaProfile.getInstance().getProfileManager().mobXpMap;
        int slotIdx = 0;
        for (Map.Entry<String, DrakariaProfile.MobXpConfig> entry : mobXpMap.entrySet()) {
            if (slotIdx >= upgradeSlots.length) break;
            String mobKey = entry.getKey();
            String upgradeKey = "hunter_" + mobKey;
            DrakariaProfile.MobXpConfig mobConf = entry.getValue();
            int upgradeLevel = DrakariaProfile.getInstance().getProfileManager().getUpgradeRepository()
                    .getUpgradeLevel(player.getUniqueId(), upgradeKey);

            int[] palier = getMobChasseurUpgradeSteps(mobKey);
            double baseChance = mobConf.chance;
            double[] stepDisplay = new double[palier.length + 1];
            stepDisplay[0] = baseChance;
            for (int i = 0; i < palier.length; i++)
                stepDisplay[i + 1] = Math.min(baseChance + palier[i], 100);

            short dataValue = mobEggData.getOrDefault(mobKey, (short)0);
            ItemStack icon = new ItemStack(Material.MONSTER_EGG, 1, dataValue);
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.setDisplayName("§b" + mobKey.charAt(0) + mobKey.substring(1).toLowerCase());
            List<String> lore = new ArrayList<>();
            lore.add("§7Augmente vos chances d'obtenir de l'XP sur ce mob :");
            lore.add("§7Chance actuelle : §a" + stepDisplay[Math.min(upgradeLevel, stepDisplay.length-1)] + "%");
            lore.add("");
            lore.add("§7§lNiveaux :");
            lore.add("§8- Par défaut : §a" + baseChance + "% XP");
            for (int i = 0; i < palier.length; i++) {
                String color = (upgradeLevel >= (i + 1)) ? "§a" : "§7";
                lore.add("§8- Niveau " + (i+1) + " : " + color + stepDisplay[i+1] + "% XP");
            }
            lore.add("");
            if (upgradeLevel < palier.length) {
                int nextCost = (upgradeLevel == 0 ? 1 : 3);
                lore.add("§eClique gauche pour améliorer");
                lore.add("§7Prochain niveau : §c" + nextCost + " points de productivité");
            } else {
                lore.add("§a✓ Niveau maximal atteint");
            }
            iconMeta.setLore(lore);
            icon.setItemMeta(iconMeta);

            inv.setItem(upgradeSlots[slotIdx], icon);
            slotIdx++;
        }

        player.openInventory(inv);
    }
}
