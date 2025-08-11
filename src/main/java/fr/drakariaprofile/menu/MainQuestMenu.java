package fr.drakariaprofile.menu;

import fr.drakariaprofile.DrakariaProfile;
import fr.drakariaprofile.profile.Profile;
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

public class MainQuestMenu {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§cObjectifs / Quêtes");

        // Fond : rouge et noir comme sur le screen
        ItemStack redGlass = getGlassPane((short)14, "§c");
        ItemStack blackGlass = getGlassPane((short)15, "§0");
        for (int slot = 0; slot < 54; slot++) {
            int row = slot/9, col = slot%9;
            boolean isBorder = (row==0 || row==5 || col==0 || col==8);
            inv.setItem(slot, isBorder ? redGlass : blackGlass);
        }

        // Slot 4 : Tête du joueur (niveau & XP)
        ItemStack head = getPlayerHead(player);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setDisplayName("§e" + player.getName());
        List<String> headLore = new ArrayList<>();
        int niveau = DrakariaProfile.getInstance().getProfileManager().getLevel(player);
        double xp = DrakariaProfile.getInstance().getProfileManager().getXp(player);
        int xpMax = DrakariaProfile.getInstance().getProfileManager().getXpToNextLevel(player);
        headLore.add("§7Niveau : §a" + niveau);
        headLore.add("§7XP : §b" + ((int) xp) + "§7/§b" + xpMax);
        headMeta.setLore(headLore);
        head.setItemMeta(headMeta);
        inv.setItem(4, head);

        // Bannières (vert, orange, rouge)
        inv.setItem(20, createBanner("§aQuête verte (facile)", 10));
        inv.setItem(22, createBanner("§6Quête orange (moyen)", 14));
        inv.setItem(24, createBanner("§cQuête rouge (difficile)", 1));

        // Bas : bloc d’or (top), peinture, émeraude, tout comme dans le screen
        ItemStack goldBlock = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta goldMeta = goldBlock.getItemMeta();
        goldMeta.setDisplayName("§eTop 10 joueurs - Points de quêtes");
        goldMeta.setLore(getTop10Lore());
        goldBlock.setItemMeta(goldMeta);
        inv.setItem(39, goldBlock);

        ItemStack painting = new ItemStack(Material.PAINTING);
        ItemMeta paintingMeta = painting.getItemMeta();
        paintingMeta.setDisplayName("§bÀ propos des quêtes");
        paintingMeta.setLore(Arrays.asList(
                "§7Complète les objectifs pour gagner points et récompenses.",
                "§7Toutes les quêtes reset à minuit chaque jour."
        ));
        painting.setItemMeta(paintingMeta);
        inv.setItem(40, painting);

        ItemStack emeraldOre = new ItemStack(Material.EMERALD_ORE);
        ItemMeta emMeta = emeraldOre.getItemMeta();
        emMeta.setDisplayName("§aHistorique des récompenses");
        emMeta.setLore(Arrays.asList("§7À venir...", "§8(WIP)"));
        emeraldOre.setItemMeta(emMeta);
        inv.setItem(41, emeraldOre);

        // Slot 46 : bouton quitter
        ItemStack quit = new ItemStack(Material.INK_SACK, 1, (short)1);
        ItemMeta qmeta = quit.getItemMeta();
        qmeta.setDisplayName("§cQuitter");
        quit.setItemMeta(qmeta);
        inv.setItem(45, quit);

        player.openInventory(inv);
    }

    private static ItemStack getGlassPane(short color, String name) {
        ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, color);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(name);
        pane.setItemMeta(meta);
        return pane;
    }

    private static ItemStack getPlayerHead(Player player) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
        meta.setOwner(player.getName());
        skull.setItemMeta(meta);
        return skull;
    }

    private static ItemStack createBanner(String name, int color) {
        ItemStack banner = new ItemStack(Material.BANNER, 1, (short)color);
        ItemMeta meta = banner.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList("§7Clique pour accéder à cette catégorie"));
        banner.setItemMeta(meta);
        return banner;
    }

    private static List<String> getTop10Lore() {
        List<String> lore = new ArrayList<>();
        List<Profile> top = DrakariaProfile.getInstance().getProfileManager().getTop10ProfilesByQuestPoints();
        int rank = 1;
        for (Profile prof : top) {
            lore.add("§e#" + rank + " §f" + prof.getName() + " §7- §b" + prof.getQuestPoints() + "pts");
            rank++;
        }
        if (lore.isEmpty()) lore.add("§8Aucun classement disponible !");
        return lore;
    }
}
