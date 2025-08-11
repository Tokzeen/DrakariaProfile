package fr.drakariaprofile;

import fr.drakariaprofile.commands.*;
import fr.drakariaprofile.config.ConfigManager;
import fr.drakariaprofile.listeners.*;
import fr.drakariaprofile.menu.MainMenuListener;
import fr.drakariaprofile.menu.MenuManager;
import fr.drakariaprofile.menu.QuestMenuListener;
import fr.drakariaprofile.menu.RewardsMenuListener;
import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.quest.QuestCommandExecutor;
import fr.drakariaprofile.quest.QuestManager;
import fr.drakariaprofile.storage.SQLiteManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DrakariaProfile extends JavaPlugin {
    private static DrakariaProfile instance;
    private ProfileManager profileManager;
    private QuestManager questManager;

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


        // Création des fichiers de config de base
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
        if (!new File(getDataFolder(), "level.yml").exists()) {
            saveResource("level.yml", false);
        }

        // --- Génère rewards_gui.yml automatiquement s'il n'existe pas ---
        File rewardsGuiFile = new File(getDataFolder(), "rewards_gui.yml");
        if (!rewardsGuiFile.exists()) {
            try {
                getDataFolder().mkdirs();
                rewardsGuiFile.createNewFile();
                YamlConfiguration config = new YamlConfiguration();
                for (int i = 0; i < 80; i++) {
                    config.set("rewards." + i + ".name", "&bRécompense #" + (i + 1));
                    config.set("rewards." + i + ".lore",
                            Collections.singletonList("&7Clique pour récupérer la récompense !"));
                }
                config.save(rewardsGuiFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        // Chargement des configs
        ConfigManager.loadConfigs();

        // Chargement des XP pour profils
        Map<String, Double> blockXpMap = loadBlockXpMap();
        Map<String, Double> smeltXpMap = loadSmeltXpMap();
        Map<String, Double> shopSellXpMap = loadShopSellXpMap();
        Map<String, MobXpConfig> mobXpMap = loadMobXpMap();

        profileManager = new ProfileManager(blockXpMap, smeltXpMap, shopSellXpMap, mobXpMap);

        // Listeners pour profils
        getServer().getPluginManager().registerEvents(new BlockBreakListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new SmeltListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new ShopTransactionListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new MobKillListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new EnchantListener(profileManager, this), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(profileManager, this), this);

        // -----------------
        // SYSTÈME DE QUÊTES
        // -----------------
        questManager = new QuestManager(getDataFolder());

        // Commandes quêtes journalières
        getCommand("quete_facile").setExecutor(new QuestCommandExecutor(questManager));
        getCommand("quete_moyen").setExecutor(new QuestCommandExecutor(questManager));
        getCommand("quete_difficile").setExecutor(new QuestCommandExecutor(questManager));
        getCommand("objectifs").setExecutor(new MainQuestMenuCommand());
        getCommand("objectif").setExecutor(new MainQuestMenuCommand());
        getCommand("quest").setExecutor(new MainQuestMenuCommand());
        getCommand("quests").setExecutor(new MainQuestMenuCommand());
        getCommand("quetes").setExecutor(new MainQuestMenuCommand());

        // Listeners quêtes
        getServer().getPluginManager().registerEvents(new PlayerJoinQuestListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitQuestListener(), this);
        getServer().getPluginManager().registerEvents(new QuestMenuListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakQuestListener(), this);
        getServer().getPluginManager().registerEvents(new MainMenuListener(), this);

        // Lancer le reset quotidien automatique des quêtes

        questManager = new QuestManager(getDataFolder());
        questManager.scheduleDailyResetProd();


        // -----------------
        // Menu des récompenses
        // -----------------
        MenuManager menuManager = new MenuManager(profileManager, getDataFolder());
        getCommand("reward").setExecutor(new RewardsCommandExecutor(menuManager));
        getCommand("rewards").setExecutor(new RewardsCommandExecutor(menuManager));
        getCommand("récompenses").setExecutor(new RewardsCommandExecutor(menuManager));
        getCommand("recompence").setExecutor(new RewardsCommandExecutor(menuManager));
        getServer().getPluginManager().registerEvents(new RewardsMenuListener(menuManager, profileManager), this);

        // Commandes profil
        getCommand("drakariaProfile").setExecutor(new ProfileCommandExecutor(profileManager));
        getCommand("drakariaprofile").setExecutor(new QuestAdminCommandExecutor());

        getCommand("profile").setExecutor(new ProfilePublicCommandExecutor(profileManager));
    }

    @Override
    public void onDisable() {
        if (questManager != null) {
            questManager.saveAllPlayers(); // Sauvegarde toutes les quêtes
        }
        SQLiteManager.disconnect();
    }

    public static DrakariaProfile getInstance() {
        return instance;
    }

    // =================== Chargement des maps XP ===================
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

    public QuestManager getQuestManager() {
        return questManager;
    }
}
