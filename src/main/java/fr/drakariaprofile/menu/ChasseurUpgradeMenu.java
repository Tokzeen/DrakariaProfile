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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChasseurUpgradeMenu {
    public static void openChasseurUpgradeMenu(Player player, Profile profile) {
        Inventory inv = Bukkit.createInventory(null, 54, "Améliorations : Chasseur");

        ItemStack redGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)14);
        ItemMeta glassMeta = redGlass.getItemMeta(); glassMeta.setDisplayName(" "); redGlass.setItemMeta(glassMeta);
        for (int i = 0; i < 54; i++) inv.setItem(i, redGlass);

        ItemStack ironBar = new ItemStack(Material.IRON_FENCE);
        ItemMeta ironMeta = ironBar.getItemMeta(); ironMeta.setDisplayName(" "); ironBar.setItemMeta(ironMeta);
        for (int row = 1; row <= 4; row++) for (int col = 1; col <= 7; col++) inv.setItem(row * 9 + col, ironBar);

        // Tête joueur (slot 4)
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        meta.setDisplayName("§e" + player.getName());
        meta.setLore(Arrays.asList(
                "§7Niveau : §a" + profile.getLevel(),
                "§7XP : §b" + String.format("%.2f", profile.getXp())
        ));
        skull.setItemMeta(meta);
        inv.setItem(4, skull);

        // Nether star (slot 8)
        ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta starMeta = netherStar.getItemMeta();
        starMeta.setDisplayName("§eVos Points d'upgrade");
        List<String> starLore = new ArrayList<>();
        starLore.add("§7Productivité : §b" + profile.getUpgradeProductivite());
        starLore.add("§7Chance : §b" + profile.getUpgradeChance());
        starMeta.setLore(starLore);
        netherStar.setItemMeta(starMeta);
        inv.setItem(8, netherStar);

        // Quitter slot 45
        ItemStack quit = new ItemStack(Material.REDSTONE);
        ItemMeta quitMeta = quit.getItemMeta();
        quitMeta.setDisplayName("§cQuitter le menu");
        quit.setItemMeta(quitMeta);
        inv.setItem(45, quit);

        // Slot 10: amélioration “chance kill mobs”
        int chasseurLevel = DrakariaProfile.getInstance().getProfileManager()
                .getUpgradeRepository().getUpgradeLevel(player.getUniqueId(), "hunter");
        ItemStack bow = new ItemStack(Material.BOW); // Icône au choix
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.setDisplayName("§bAmélioration Chasseur");
        List<String> bowLore = new ArrayList<>();
        bowLore.add("§7Améliore vos chances de gagner de l'XP en tuant un monstre");
        if (chasseurLevel == 0) {
            bowLore.add("§7%XP de base : §a20%");
        } else if (chasseurLevel == 1) {
            bowLore.add("§7%XP : §a35%");
        } else if (chasseurLevel == 2) {
            bowLore.add("§7%XP : §a50%");
        }
        bowLore.add("");
        bowLore.add("§7§lNiveaux :");
        bowLore.add("§8- Par défaut §a20% XP");
        bowLore.add("§8- Niveau 1   §a35% XP");
        bowLore.add("§8- Niveau 2   §a50% XP");
        bowLore.add("");
        if (chasseurLevel < 2) {
            bowLore.add("§eClique gauche pour améliorer");
            int nextCost = (chasseurLevel == 0) ? 1 : 3; // exemple : coût 1 puis 3 points de chance
            bowLore.add("§7Prochain niveau : §c" + nextCost + " points de chance");
        } else {
            bowLore.add("§a✓ Niveau maximal atteint");
        }
        bowMeta.setLore(bowLore);
        bow.setItemMeta(bowMeta);
        inv.setItem(10, bow);

        player.openInventory(inv);
    }
}
