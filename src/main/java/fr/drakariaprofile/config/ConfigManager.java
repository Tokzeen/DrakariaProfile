package fr.drakariaprofile.config;

import org.bukkit.configuration.file.YamlConfiguration;
import fr.drakariaprofile.DrakariaProfile;

import java.io.File;

public class ConfigManager {
    private static YamlConfiguration breackConfig;
    private static YamlConfiguration levelConfig;

    public static void loadConfigs() {
        breackConfig = YamlConfiguration.loadConfiguration(new File(DrakariaProfile.getInstance().getDataFolder(), "config.yml"));
        levelConfig = YamlConfiguration.loadConfiguration(new File(DrakariaProfile.getInstance().getDataFolder(), "level.yml"));
    }

    public static double getXpForBlock(String block) {
        return breackConfig.getDouble("blocks." + block, 0.0);
    }
    public static double getXpForSmelt(String smeltItem) {
        return breackConfig.getDouble("smelts." + smeltItem, 0.0);
    }
    public static int getXpForLevel(int level) {
        return levelConfig.getInt("levels." + level, 100);
    }
}
