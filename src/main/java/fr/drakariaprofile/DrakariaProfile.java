package fr.drakariaprofile;

import fr.drakariaprofile.config.ConfigManager;
import fr.drakariaprofile.listeners.ShopTransactionListener;
import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.commands.ProfileCommandExecutor;
import fr.drakariaprofile.listeners.BlockBreakListener;
import fr.drakariaprofile.listeners.SmeltListener;
import fr.drakariaprofile.storage.SQLiteManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DrakariaProfile extends JavaPlugin {
    private static DrakariaProfile instance;
    private ProfileManager profileManager;

    @Override
    public void onEnable() {
        instance = this;
        SQLiteManager.connect();

        // Crée config.yml et level.yml s'ils n'existent pas déjà (important)
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
        if (!new File(getDataFolder(), "level.yml").exists()) {
            saveResource("level.yml", false);
        }

        // Charge les configs (configManager initialisera bien levelConfig)
        ConfigManager.loadConfigs();

        Map<String, Double> blockXpMap = loadBlockXpMap();
        Map<String, Double> smeltXpMap = loadSmeltXpMap();
        Map<String, Double> shopSellXpMap = loadShopSellXpMap();

        profileManager = new ProfileManager(blockXpMap, smeltXpMap, shopSellXpMap);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new SmeltListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new ShopTransactionListener(profileManager), this);
        getCommand("drakariaProfile").setExecutor(new ProfileCommandExecutor(profileManager));
    }

    @Override
    public void onDisable() {
        SQLiteManager.disconnect();
    }

    public static DrakariaProfile getInstance() {
        return instance;
    }

    private Map<String, Double> loadBlockXpMap() {
        Map<String, Double> map = new HashMap<>();
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) saveResource("config.yml", false);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("blocks")) {
            for (String key : config.getConfigurationSection("blocks").getKeys(false)) {
                map.put(key, config.getDouble("blocks." + key, 0.0));
            }
        }
        return map;
    }

    private Map<String, Double> loadShopSellXpMap() {
        Map<String, Double> map = new HashMap<>();
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) saveResource("config.yml", false);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("shop_sells")) {
            for (String key : config.getConfigurationSection("shop_sells").getKeys(false)) {
                map.put(key, config.getDouble("shop_sells." + key, 0.0));
            }
        }
        return map;
    }

    private Map<String, Double> loadSmeltXpMap() {
        Map<String, Double> map = new HashMap<>();
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) saveResource("config.yml", false);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("smelts")) {
            for (String key : config.getConfigurationSection("smelts").getKeys(false)) {
                map.put(key, config.getDouble("smelts." + key, 0.0));
            }
        }
        return map;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }
}
