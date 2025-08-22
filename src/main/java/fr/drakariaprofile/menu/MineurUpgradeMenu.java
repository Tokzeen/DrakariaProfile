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
import org.bukkit.DyeColor;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MineurUpgradeMenu {

    public static void openMineurUpgradeMenu(Player player, Profile profile) {
        Inventory inv = Bukkit.createInventory(null, 54, "Améliorations : Mineur");

        // Rouge (bords)
        ItemStack redGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)14);
        ItemMeta glassMeta = redGlass.getItemMeta();
        glassMeta.setDisplayName(" ");
        redGlass.setItemMeta(glassMeta);
        for (int i = 0; i < 54; i++) inv.setItem(i, redGlass);

        // Barreaux (grille centrale)
        ItemStack ironBar = new ItemStack(Material.IRON_FENCE);
        ItemMeta ironMeta = ironBar.getItemMeta(); ironMeta.setDisplayName(" "); ironBar.setItemMeta(ironMeta);
        for (int row = 1; row <= 4; row++) for (int col = 1; col <= 7; col++) inv.setItem(row * 9 + col, ironBar);

        // Tête joueur (slot 4)
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        meta.setDisplayName("§e" + player.getName());
        List<String> skullLore = Arrays.asList(
                "§7Niveau : §a" + profile.getLevel(),
                "§7XP : §b" + String.format("%.2f", profile.getXp())
        );
        meta.setLore(skullLore);
        skull.setItemMeta(meta);
        inv.setItem(4, skull);

        // Nether star (slot 6)
        ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta starMeta = netherStar.getItemMeta();
        starMeta.setDisplayName("§eVos Points d'upgrade");
        List<String> starLore = new ArrayList<>();
        starLore.add("§7Productivité : §b" + profile.getUpgradeProductivite());
        starLore.add("§7Chance : §b" + profile.getUpgradeChance());
        starMeta.setLore(starLore);
        netherStar.setItemMeta(starMeta);
        inv.setItem(8, netherStar);

        // Colorant rouge (quit) slot 36 (coin bas gauche)
        ItemStack quit = new ItemStack(Material.REDSTONE);
        ItemMeta quitMeta = quit.getItemMeta();
        quitMeta.setDisplayName("§cQuitter le menu");
        quit.setItemMeta(quitMeta);
        inv.setItem(45, quit);

        // Affichage STONE (slot 10)
        int stoneLevel = DrakariaProfile.getInstance().getProfileManager()
                .getUpgradeRepository().getUpgradeLevel(player.getUniqueId(), "stone");
        ItemStack stone = new ItemStack(Material.STONE);
        ItemMeta stoneMeta = stone.getItemMeta();
        List<String> stoneLore = new ArrayList<>();
        stoneMeta.setDisplayName("§bAmélioration Stone");
        stoneLore.add("§7XPxp obtenue par stone cassez");
        // Valeurs dynamiques d'upgrade selon level :
        if (stoneLevel == 0) {
            stoneLore.add("§7Vous gagnez : §a0.1xp");
        } else if (stoneLevel == 1) {
            stoneLore.add("§7Vous gagnez : §a0.2xp");
        } else if (stoneLevel == 2) {
            stoneLore.add("§7Vous gagnez : §a0.2xp §e+ 0.10$");
        }
        stoneLore.add("");
        stoneLore.add("§7§lNiveaux :");
        stoneLore.add("§8- Par défaut §a0.1xp");
        stoneLore.add("§8- Niveau 1   §a0.2xp");
        stoneLore.add("§8- Niveau 2   §a0.2xp §e+ 0.10$");

        stoneLore.add("");
        if (stoneLevel < 2) {
            stoneLore.add("§eClique gauche pour améliorer");
            if (stoneLevel == 0) stoneLore.add("§7Prochain niveau : §4(cout 1 productivité)");
            else stoneLore.add("§7Prochain niveau : §4(cout 2 productivités)");
        } else {
            stoneLore.add("§a✓ Niveau maximal atteint");
        }
        stoneMeta.setLore(stoneLore);
        stone.setItemMeta(stoneMeta);
        inv.setItem(10, stone);

        // Ouvre le menu pour le joueur
        player.openInventory(inv);
    }
}
