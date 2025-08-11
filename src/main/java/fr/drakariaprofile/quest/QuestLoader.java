package fr.drakariaprofile.quest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class QuestLoader {
    public static Map<String, List<Quest>> loadAllQuests(File questFile) {
        Map<String, List<Quest>> allQuests = new HashMap<>();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(questFile);

        for (String questId : config.getKeys(false)) {
            ConfigurationSection sec = config.getConfigurationSection(questId);
            if (sec == null) continue;

            for (String catName : sec.getKeys(false)) {
                QuestCategory category;
                try {
                    category = QuestCategory.valueOf(catName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("[QuestLoader] Catégorie invalide dans " + questId + ": " + catName);
                    continue;
                }

                List<Map<?, ?>> questsList = sec.getMapList(catName);
                for (Map<?, ?> questEntry : questsList) {
                    try {
                        int amount = (int) questEntry.get("amount");

                        Map<?, ?> rewardSec = (Map<?, ?>) questEntry.get("reward");
                        int points = (int) rewardSec.get("points");

                        // XP format "min-max"
                        String xpStr = (String) rewardSec.get("xp");
                        int xpMin = Integer.parseInt(xpStr.split("-")[0]);
                        int xpMax = Integer.parseInt(xpStr.split("-")[1]);

                        String command = (String) rewardSec.get("command");

                        // Lecture des infos item
                        Map<?, ?> itemMap = (Map<?, ?>) rewardSec.get("item");

                        // Nom de l'item dans la config (facultatif, sinon DIAMOND)
                        String matName = itemMap.containsKey("material")
                                ? (String) itemMap.get("material")
                                : "DIAMOND";

                        Material mat = Material.matchMaterial(matName.toUpperCase());
                        if (mat == null) mat = Material.DIAMOND; // fallback

                        String itemName = ChatColor.translateAlternateColorCodes('&',
                                String.valueOf(itemMap.get("name")));

                        List<String> lore = new ArrayList<>();
                        if (itemMap.get("lore") instanceof List) {
                            for (Object line : (List<?>) itemMap.get("lore")) {
                                lore.add(ChatColor.translateAlternateColorCodes('&', String.valueOf(line)));
                            }
                        } else if (itemMap.containsKey("lore")) {
                            lore.add(ChatColor.translateAlternateColorCodes('&',
                                    String.valueOf(itemMap.get("lore"))));
                        }

                        // Création ItemStack
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(itemName);
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                        }

                        QuestReward reward = new QuestReward(points, xpMin, xpMax, command);
                        Quest quest = new Quest(questId, category, amount, reward, item);

                        allQuests.computeIfAbsent(catName.toUpperCase(), k -> new ArrayList<>()).add(quest);

                    } catch (Exception e) {
                        Bukkit.getLogger().severe("[QuestLoader] Erreur lors du chargement de la quête "
                                + questId + " (" + catName + ")");
                        e.printStackTrace();
                    }
                }
            }
        }
        return allQuests;
    }
}
