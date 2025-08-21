package fr.drakariaprofile.menu;

import fr.drakariaprofile.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class MineurUpgradeMenu {

    public void openMineurUpgradeMenu(Player player, Profile profile) {
        Inventory inv = Bukkit.createInventory(null, 54, "Améliorations : Mineur");

        // Vitres rouges (non cliquables)
        ItemStack redGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15);
        ItemMeta glassMeta = redGlass.getItemMeta();
        glassMeta.setDisplayName(" ");
        redGlass.setItemMeta(glassMeta);

        // Barrières en fer (non cliquables)
        ItemStack ironBar = new ItemStack(Material.IRON_FENCE);
        ItemMeta ironMeta = ironBar.getItemMeta();
        ironMeta.setDisplayName(" ");
        ironBar.setItemMeta(ironMeta);

        // Tête du joueur (slot 13)
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3); // 3 = joueur
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName()); // À la place de setOwningPlayer (1.8 only)
        meta.setDisplayName("§a" + player.getName());
        skull.setItemMeta(meta);
        inv.setItem(13, skull);


        // Nether Star (slot 8) - Points d'upgrade
        ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta starMeta = netherStar.getItemMeta();
        starMeta.setDisplayName("§ePoints d'upgrade");
        List<String> starLore = new ArrayList<>();
        starLore.add("§7Productivité : §b" + profile.getUpgradeProductivite());
        starLore.add("§7Chance : §b" + profile.getUpgradeChance());
        starMeta.setLore(starLore);
        netherStar.setItemMeta(starMeta);
        inv.setItem(8, netherStar);

        // Redstone (slot 45, retour/menu principal)
        ItemStack redstone = new ItemStack(Material.REDSTONE);
        ItemMeta redMeta = redstone.getItemMeta();
        redMeta.setDisplayName("§cMenu principal");
        redstone.setItemMeta(redMeta);
        inv.setItem(45, redstone);

        // Barrières en fer (slots 18 à 44)
        for (int i = 18; i <= 44; i++) {
            inv.setItem(i, ironBar);
        }

        // Vitres rouges sur les bords et coins
        int[] redSlots = {0,1,2,3,4,5,6,7,8,46,47,48,49,50,51,52,53,9,17,27,36};
        for (int slot : redSlots) {
            inv.setItem(slot, redGlass);
        }

        // Stone (slot 10) - Amélioration Mineur
        int stoneLevel = profile.getStoneUpgradeLevel();
        ItemStack stone = new ItemStack(Material.STONE);
        ItemMeta stoneMeta = stone.getItemMeta();
        List<String> stoneLore = new ArrayList<>();
        if (stoneLevel == 0) {
            stoneMeta.setDisplayName("§fAmélioration Stone §7[lv. 0]");
            stoneLore.add("§7Coût : §c1 point de productivité");
            stoneLore.add("§8▸ Pour chaque stone cassée : §b0,1 xp");
            stoneLore.add("§eClique gauche pour acheter.");
        } else if (stoneLevel == 1) {
            stoneMeta.setDisplayName("§aAmélioration Stone §7[lv. 1]");
            stoneLore.add("§7Coût : §c2 points de productivité");
            stoneLore.add("§8▸ Pour chaque stone cassée : §b0,2 xp");
            stoneLore.add("§eClique gauche pour acheter.");
        } else if (stoneLevel == 2) {
            stoneMeta.setDisplayName("§aAmélioration Stone §7[lvl max]");
            stoneLore.add("§7Coût : §c5 points de productivité");
            stoneLore.add("§8▸ Pour chaque stone cassée : §b0,2 xp §a+ §610,10$ §7(Vault)");
            stoneLore.add("§cLvl max atteint");
        }
        stoneMeta.setLore(stoneLore);
        stone.setItemMeta(stoneMeta);
        inv.setItem(10, stone);

        // Ouvre le menu pour le joueur
        player.openInventory(inv);
    }
}
