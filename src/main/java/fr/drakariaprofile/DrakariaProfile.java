package fr.drakariaprofile;

import fr.drakariaprofile.box.BoxRewardManager;
import fr.drakariaprofile.commands.*;
import fr.drakariaprofile.config.ConfigManager;
import fr.drakariaprofile.listeners.*;
import fr.drakariaprofile.menu.*;
import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.quest.QuestCommandExecutor;
import fr.drakariaprofile.quest.QuestManager;
import fr.drakariaprofile.storage.SQLiteManager;
import fr.drakariaprofile.storage.PlacedBlockRepository;
import fr.drakariaprofile.storage.UpgradeRepository;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DrakariaProfile extends JavaPlugin {
    private static DrakariaProfile instance;
    private ProfileManager profileManager;
    private QuestManager questManager;
    private BoxRewardManager boxRewardManager;

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

        // Génère rewards_gui.yml automatiquement s'il n'existe pas
        File rewardsGuiFile = new File(getDataFolder(), "rewards_gui.yml");
        if (!rewardsGuiFile.exists()) {
            try {
                getDataFolder().mkdirs();
                rewardsGuiFile.createNewFile();
                YamlConfiguration config = new YamlConfiguration();
                for (int i = 0; i < 80; i++) {
                    config.set("rewards." + i + ".name", "&bRécompense #" + (i + 1));
                    config.set("rewards." + i + ".lore", Collections.singletonList("&7Clique pour récupérer la récompense !"));
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
        Map<String, Double> cropXpMap = loadCropXpMap();
        Map<String, Double> fishingLootXpMap = ConfigManager.getFishingLootXpMap();

        profileManager = new ProfileManager(blockXpMap, smeltXpMap, shopSellXpMap, mobXpMap, cropXpMap);


        // Instanciation du repository persistant pour la gestion des blocs posés (melon/citrouille)
        PlacedBlockRepository placedBlockRepo = new PlacedBlockRepository(SQLiteManager.getConnection());
        UpgradeRepository upgradeRepository = profileManager.getUpgradeRepository();

        // Listeners pour profils
        getServer().getPluginManager().registerEvents(new BlockBreakListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new SmeltListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new ShopTransactionListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new MobKillListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new EnchantListener(profileManager, this), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(profileManager, this), this);
        getServer().getPluginManager().registerEvents(new KillListener(profileManager), this);
        getServer().getPluginManager().registerEvents(new FishXpListener(profileManager, fishingLootXpMap), this);

        // CropListener : passe bien 4 paramètres ici !
        getServer().getPluginManager().registerEvents(new CropListener(profileManager, upgradeRepository, cropXpMap, placedBlockRepo), this);

        MenuManager menuManager = new MenuManager(profileManager, getDataFolder());
        getServer().getPluginManager().registerEvents(new MineurMenuListener(profileManager, menuManager), this);
        getServer().getPluginManager().registerEvents(new ChasseurMenuListener(profileManager, menuManager), this);
        getServer().getPluginManager().registerEvents(new FarmeurMenuListener(profileManager, menuManager), this);
        // Système de quêtes
        questManager = new QuestManager(getDataFolder());
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
        questManager.scheduleDailyResetProd();

        // Menu des récompenses
        getCommand("reward").setExecutor(new RewardsCommandExecutor(menuManager));
        getCommand("rewards").setExecutor(new RewardsCommandExecutor(menuManager));
        getCommand("récompenses").setExecutor(new RewardsCommandExecutor(menuManager));
        getCommand("recompence").setExecutor(new RewardsCommandExecutor(menuManager));
        getServer().getPluginManager().registerEvents(new RewardsMenuListener(menuManager, profileManager), this);

        // commandes /upgrade
        getCommand("upgrade").setExecutor(new UpgradeCommandExecutor(profileManager));
        getCommand("ameliorations").setExecutor(new AmeliorationsCommand(menuManager, profileManager));

        // Commandes profil
        getCommand("drakariaprofile").setExecutor(new ProfileCommandExecutor(profileManager));
        getCommand("drakariaquest").setExecutor(new QuestAdminCommandExecutor());
        getCommand("profile").setExecutor(new ProfilePublicCommandExecutor(profileManager));

        if (!new File(getDataFolder(), "box_reward.yml").exists()) {
            saveResource("box_reward.yml", false);
        }
        boxRewardManager = new BoxRewardManager(getDataFolder());
        getCommand("drakariareward").setExecutor(new fr.drakariaprofile.commands.BoxRewardCommand(boxRewardManager));
        getServer().getPluginManager().registerEvents(new fr.drakariaprofile.menu.BoxRewardMenuListener(), this);
    }

    @Override
    public void onDisable() {
        if (questManager != null) {
            questManager.saveAllPlayers();
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

    private Map<String, Double> loadCropXpMap() {
        Map<String, Double> map = new HashMap<>();
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) saveResource("config.yml", false);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("crops")) {
            for (String key : config.getConfigurationSection("crops").getKeys(false)) {
                map.put(key.toUpperCase(), config.getDouble("crops." + key, 0.0));
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
