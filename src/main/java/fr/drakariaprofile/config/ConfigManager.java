package fr.drakariaprofile.config;

import org.bukkit.configuration.file.YamlConfiguration;
import fr.drakariaprofile.DrakariaProfile;

import java.io.File;

public class ConfigManager {
    private static YamlConfiguration breackConfig;
    public static YamlConfiguration levelConfig;

    public static void loadConfigs() {
        File pluginFolder = DrakariaProfile.getInstance().getDataFolder();
        breackConfig = YamlConfiguration.loadConfiguration(new File(pluginFolder, "config.yml"));

        File levelFile = new File(pluginFolder, "level.yml");
        if (!levelFile.exists()) {
            DrakariaProfile.getInstance().saveResource("level.yml", false);
        }
        levelConfig = YamlConfiguration.loadConfiguration(levelFile);
    }

    public static double getXpForBlock(String block) {
        return breackConfig.getDouble("blocks." + block, 0.0);
    }

    public static double getXpForSmelt(String smeltItem) {
        return breackConfig.getDouble("smelts." + smeltItem, 0.0);
    }

    public static int getXpForLevel(int level) {
        if (levelConfig == null) return 100;
        return levelConfig.getInt("levels." + level, 100);
    }

    public static String getRewardForLevel(int level) {
        if (levelConfig == null) return "";
        return levelConfig.getString("rewards." + level, "");
    }

    public static double getKillXp() {
        return breackConfig.getDouble("kill_xp", 100.0);
    }


    public static double getXpForAnvilLevel(int level) {
        if (!breackConfig.contains("anvil_rewards")) return 0.0;
        for (String range : breackConfig.getConfigurationSection("anvil_rewards").getKeys(false)) {
            String[] parts = range.split("-");
            if (parts.length != 2) continue;
            try {
                int min = Integer.parseInt(parts[0]);
                int max = Integer.parseInt(parts[1]);
                if (level >= min && level <= max) {
                    return breackConfig.getDouble("anvil_rewards." + range, 0.0);
                }
            } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }


    /**
     * XP pour enchantement selon config.yml (section enchant_rewards)
     */
    public static double getXpForEnchantLevel(int enchantLevel) {
        if (!breackConfig.contains("enchant_rewards")) return 0.0;

        for (String key : breackConfig.getConfigurationSection("enchant_rewards").getKeys(false)) {
            String[] parts = key.split("-");
            if (parts.length != 2) continue;
            try {
                int min = Integer.parseInt(parts[0]);
                int max = Integer.parseInt(parts[1]);
                if (enchantLevel >= min && enchantLevel <= max) {
                    return breackConfig.getDouble("enchant_rewards." + key, 0.0);
                }
            } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }
}
