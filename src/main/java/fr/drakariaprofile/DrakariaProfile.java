package fr.drakariaprofile;

import fr.drakariaprofile.commands.ProfilePublicCommandExecutor;
import fr.drakariaprofile.config.ConfigManager;
import fr.drakariaprofile.listeners.ShopTransactionListener;
import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.commands.ProfileCommandExecutor;
import fr.drakariaprofile.listeners.BlockBreakListener;
import fr.drakariaprofile.listeners.SmeltListener;
import fr.drakariaprofile.listeners.MobKillListener;
import fr.drakariaprofile.storage.SQLiteManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DrakariaProfile extends JavaPlugin {
    private static DrakariaProfile instance;
    private ProfileManager profileManager;

    // Pour le mob system (config)
    public static class MobXpConfig {
        public final double xp;
        public final double chance;
        public MobXpConfig(double xp, double chance) {
            this.xp = xp;
            this.chance = chance;
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        SQLiteManager.connect();

        // Crée config.yml et level.yml si n'existent pas
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
        if (!new File(getDataFolder(), "level.yml").exists()) {
            saveResource("level.yml", false);
        }

        ConfigManager.loadConfigs();

        Map<String, Double> blockXpMap = loadBlockXpMap();
        Map<String, Double> smeltXpMap = loadSmeltXpMap();
        Map<String, Double> shopSellXpMap = loadShopSellXpMap();
        Map<String, MobXpConfig> mobXpMap = loadMobXpMap();

        profileManager = new ProfileManager(blockXpMap, smeltXpMap, shopSellXpMap, mobXpMap);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new SmeltListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new ShopTransactionListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new MobKillListener(profileManager), this);
        getCommand("drakariaProfile").setExecutor(new ProfileCommandExecutor(profileManager));
        getCommand("profile").setExecutor(new ProfilePublicCommandExecutor(profileManager));

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

    // --- Load mobs mapping with XP and CHANCE ---
    private Map<String, MobXpConfig> loadMobXpMap() {
        Map<String, MobXpConfig> map = new HashMap<>();
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) saveResource("config.yml", false);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("mobs")) {
            for (String key : config.getConfigurationSection("mobs").getKeys(false)) {
                String base = "mobs." + key;
                double xp = config.getDouble(base + ".xp", 0.0);
                double chance = config.getDouble(base + ".chance", 100.0);
                map.put(key.toUpperCase(), new MobXpConfig(xp, chance));
            }
        }
        return map;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }
}
