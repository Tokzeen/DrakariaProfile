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
}
