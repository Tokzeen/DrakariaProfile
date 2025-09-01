package fr.drakariaprofile.menu;

import fr.drakariaprofile.profile.Profile;
import fr.drakariaprofile.DrakariaProfile;
import fr.drakariaprofile.storage.UpgradeRepository;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class FarmeurUpgradeMenu {

    public static int[] getFarmeurUpgradeSlots() {
        return new int[] {
                10,11,12,13,14,15,16,
                19,20,21,22,23,24,25,
                28,29,30,31,32,33,34,
                37,38,39,40,41,42,43
        };
    }

    public static void openFarmeurUpgradeMenu(Player player, Profile profile) {
        Inventory inv = Bukkit.createInventory(null, 54, "Améliorations : Farmeur");

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

        // Slot 8 : nether star - productivité
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

        // Milieu : barres par défaut
        ItemStack ironBar = new ItemStack(Material.IRON_FENCE);
        ItemMeta ironMeta = ironBar.getItemMeta(); ironMeta.setDisplayName(" "); ironBar.setItemMeta(ironMeta);

        int[] upgradeSlots = getFarmeurUpgradeSlots();
        for (int slot : upgradeSlots) inv.setItem(slot, ironBar);

        // Place les crops dynamiquement depuis la config
        Map<String, Double> cropXpMap = DrakariaProfile.getInstance().getProfileManager().getCropXpMap();
        UpgradeRepository upgradeRepo = DrakariaProfile.getInstance().getProfileManager().getUpgradeRepository();
        List<String> cropKeys = new ArrayList<>(cropXpMap.keySet());
        int slotIdx = 0;
        for (String cropKey : cropKeys) {
            if (slotIdx >= upgradeSlots.length) break;
            String upgradeKey = "farmer_" + cropKey;
            double baseXp = cropXpMap.get(cropKey);
            int level = upgradeRepo.getUpgradeLevel(player.getUniqueId(), upgradeKey);

            int max = 3;
            Material material = getMaterialForCrop(cropKey);

            ItemStack icon = new ItemStack(material);
            ItemMeta iconMeta = icon.getItemMeta();
            iconMeta.setDisplayName("§a" + cropKey.replace('_', ' '));
            List<String> loreCrop = new ArrayList<>();
            loreCrop.add("§7Améliore le gain sur la récolte !");
            loreCrop.add("§7XP actuelle : §a" + (baseXp * (1+level*0.5)));
            loreCrop.add("§7Niveau : §a" + level + "§7/§c" + max);
            loreCrop.add("");
            if (level < max) {
                int nextCost = (level == 0 ? 1 : 3);
                loreCrop.add("§eClique gauche pour améliorer");
                loreCrop.add("§7Prochain niveau : §c" + nextCost + " points de productivité");
            } else {
                loreCrop.add("§a✓ Niveau maximal atteint");
            }
            iconMeta.setLore(loreCrop);
            icon.setItemMeta(iconMeta);

            inv.setItem(upgradeSlots[slotIdx], icon);
            slotIdx++;
        }

        player.openInventory(inv);
    }

    private static Material getMaterialForCrop(String cropKey) {
        switch (cropKey.toUpperCase()) {
            case "WHEAT": return Material.WHEAT;
            case "CARROT": return Material.CARROT_ITEM;
            case "POTATO": return Material.POTATO_ITEM;
            case "NETHER_WARTS": return Material.NETHER_STALK;
            case "MELON_BLOCK": return Material.MELON_BLOCK;
            case "PUMPKIN": return Material.PUMPKIN;
            case "COCOA": return Material.COCOA;
            case "SUGAR_CANE_BLOCK": return Material.SUGAR_CANE;
            case "CACTUS": return Material.CACTUS;
            default: return Material.APPLE;
        }
    }
}
