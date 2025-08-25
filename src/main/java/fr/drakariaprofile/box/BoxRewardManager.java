package fr.drakariaprofile.box;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.*;

public class BoxRewardManager {
    private final Map<String, List<RarityReward>> boxes = new HashMap<>();

    public BoxRewardManager(File dataFolder) {
        File configFile = new File(dataFolder, "box_reward.yml");
        if (!configFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.contains("Caisses")) return;
        for (String boxName : config.getConfigurationSection("Caisses").getKeys(false)) {
            List<RarityReward> list = new ArrayList<>();
            for (String rarity : config.getConfigurationSection("Caisses." + boxName + ".Rarities").getKeys(false)) {
                String base = "Caisses." + boxName + ".Rarities." + rarity;
                int chance = config.getInt(base + ".chance");
                String color = config.getString(base + ".color");
                String display = config.getString(base + ".display");
                List<String> items = config.getStringList(base + ".items");
                list.add(new RarityReward(rarity, chance, color, display, items));
            }
            boxes.put(boxName, list);
        }
    }

    public RarityReward drawReward(String boxName) {
        List<RarityReward> rarities = boxes.get(boxName);
        if (rarities == null) return null;

        int total = 0;
        for (RarityReward r : rarities)
            total += r.chance;
        if (total <= 0) return null; // sécurité supplémentaire

        int rand = new Random().nextInt(total) + 1;
        int cumul = 0;
        for (RarityReward r : rarities) {
            cumul += r.chance;
            if (rand <= cumul) {
                // PROTECTION contre liste vide ou nulle
                if (r.items == null || r.items.isEmpty()) {
                    System.out.println("BoxRewardManager WARN: La rareté '" + r.name + "' est vide dans la box '" + boxName + "'.");
                    continue; // saute la rareté
                }
                String command = r.items.get(new Random().nextInt(r.items.size()));
                return new RarityReward(r.name, r.chance, r.color, r.display, Collections.singletonList(command));
            }
        }
        return null;
    }

    public List<RarityReward> getRarities(String boxName) {
        return boxes.getOrDefault(boxName, Collections.emptyList());
    }

    public static class RarityReward {
        public final String name;
        public final int chance;
        public final String color;
        public final String display;
        public final List<String> items;

        public RarityReward(String name, int chance, String color, String display, List<String> items) {
            this.name = name;
            this.chance = chance;
            this.color = color;
            this.display = display;
            this.items = items;
        }
    }
}
