package fr.drakariaprofile.menu;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MenuManager {
    private final ProfileManager profileManager;
    private final int rewardsPerPage = 28;
    final int totalRewards = 80;
    private final int totalPages = (int) Math.ceil(totalRewards / (double) rewardsPerPage);

    // 28 slots par page (4 lignes de 7 coffres)
    private final int[] rewardSlots = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
    };

    private final YamlConfiguration guiConfig;

    public MenuManager(ProfileManager profileManager, File dataFolder) {
        this.profileManager = profileManager;
        this.guiConfig = YamlConfiguration.loadConfiguration(new File(dataFolder, "rewards_gui.yml"));
    }

    public void openRewardsMenu(Player player, int page) {
        Profile profile = profileManager.getProfile(player);
        int start = (page - 1) * rewardsPerPage + 1; // 1-indexed
        int end = Math.min(page * rewardsPerPage, totalRewards);

        Inventory inv = Bukkit.createInventory(null, 6 * 9, "§6Récompenses (Page " + page + "/" + totalPages + ")");

        // --- 1. Tout le menu en vitres rouges (hors slots reward/tête/flèches) ---
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)14);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName("§f");
        glass.setItemMeta(glassMeta);
        for (int slot = 0; slot < 54; slot++) inv.setItem(slot, glass);

        // --- 2. Tête joueur au slot 4
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        meta.setDisplayName("§e" + player.getName());
        meta.setLore(Arrays.asList(
                "§7Niveau: " + profile.getLevel(),
                "§7XP: " + String.format("%.2f", profile.getXp()) + " / " +
                        (profileManager.getXpToNextLevel(player) > 0 ? profileManager.getXpToNextLevel(player) : "MAX")
        ));
        skull.setItemMeta(meta);
        inv.setItem(4, skull);

        // --- 3. Plaque chaque reward (28 max). ---
        int rewardsThisPage = end - start + 1;
        for (int i = 0; i < rewardsThisPage; i++) {
            int level = start + i;
            boolean claimed = profile.getClaimedRewards().contains(level);

            String name = guiConfig.getString("rewards." + level + ".name", "&7Récompense #" + (level));
            List<String> lore = guiConfig.getStringList("rewards." + level + ".lore");
            if (lore == null || lore.isEmpty()) lore = Collections.singletonList("&f");

            ItemStack item;
            if (claimed) {
                item = new ItemStack(Material.BEDROCK);
                ItemMeta im = item.getItemMeta();
                im.setDisplayName("§cRécompense récupérée");
                im.setLore(Collections.singletonList("§7Déjà prise !"));
                item.setItemMeta(im);
            } else {
                item = new ItemStack(Material.CHEST);
                ItemMeta im = item.getItemMeta();
                im.setDisplayName(color(name));
                im.setLore(lore.stream().map(this::color).collect(Collectors.toList()));
                item.setItemMeta(im);
            }
            inv.setItem(rewardSlots[i], item);
        }

        // --- 4. CORRECTION demandée : sur la dernière page, les 4 derniers slots reward non utilisés => barreaux de fer ---
        if (page == totalPages) {
            for (int i = rewardsThisPage; i < rewardSlots.length; i++) {
                ItemStack ironFence = new ItemStack(Material.IRON_FENCE, 1);
                ItemMeta fenceMeta = ironFence.getItemMeta();
                fenceMeta.setDisplayName("§f");
                ironFence.setItemMeta(fenceMeta);
                inv.setItem(rewardSlots[i], ironFence);
            }
        }

        // --- 5. Navigation (flèches) ---
        if (page > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta imPrev = prev.getItemMeta();
            imPrev.setDisplayName("§aRetour");
            prev.setItemMeta(imPrev);
            inv.setItem(45, prev);
        }
        if (page < totalPages) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta imNext = next.getItemMeta();
            imNext.setDisplayName("§aSuivant");
            next.setItemMeta(imNext);
            inv.setItem(53, next);
        }

        player.openInventory(inv);
    }

    private String color(String txt) {
        if (txt == null) return "";
        return txt.replace('&', '§');
    }

    public int getRewardsPerPage() { return rewardsPerPage; }
    public int[] getRewardSlots() { return rewardSlots; }
    public int getTotalPages() { return totalPages; }
}
