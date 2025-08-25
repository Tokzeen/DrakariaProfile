package fr.drakariaprofile.profile;

import fr.drakariaprofile.DrakariaProfile;
import fr.drakariaprofile.config.ConfigManager;
import fr.drakariaprofile.storage.ProfileRepository;
import fr.drakariaprofile.storage.SQLiteManager;
import fr.drakariaprofile.storage.UpgradeRepository;
import fr.drakariaprofile.utils.XPLoopCounter;
import fr.drakariaprofile.utils.VaultHook;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ProfileManager {
    private final ProfileRepository repository = new ProfileRepository();
    private final UpgradeRepository upgradeRepository = new UpgradeRepository();
    public UpgradeRepository getUpgradeRepository() { return upgradeRepository; }

    private final Map<Player, Double> xpActionBarBuffer = new HashMap<>();
    private final Map<Player, Double> moneyActionBarBuffer = new HashMap<>();
    private final Map<Player, XPLoopCounter> xpCounters = new HashMap<>();

    private final Map<String, Double> blockXpMap;
    private final Map<String, Double> smeltXpMap;
    private final Map<String, Double> shopSellXpMap;
    public final Map<String, DrakariaProfile.MobXpConfig> mobXpMap;
    private final int ACTION_BAR_DISPLAY_TICKS = 15; // 0.75s

    public ProfileManager(Map<String, Double> blockXpMap,
                          Map<String, Double> smeltXpMap,
                          Map<String, Double> shopSellXpMap,
                          Map<String, DrakariaProfile.MobXpConfig> mobXpMap) {
        this.blockXpMap = blockXpMap;
        this.smeltXpMap = smeltXpMap;
        this.shopSellXpMap = shopSellXpMap;
        this.mobXpMap = mobXpMap;
    }

    public Profile getProfile(Player player) {
        return repository.getOrCreateProfile(player.getUniqueId(), player.getName());
    }

    public Profile getProfile(UUID uuid, String name) {
        return repository.getOrCreateProfile(uuid, name);
    }

    public void saveProfile(Profile profile) {
        repository.saveProfile(profile);
    }

    public int getLevel(Player player) { return getProfile(player).getLevel(); }
    public double getXp(Player player) { return getProfile(player).getXp(); }

    public int getXpToNextLevel(Player player) {
        int level = getLevel(player);
        if (level >= getMaxConfiguredLevel()) return -1;
        return ConfigManager.getXpForLevel(level);
    }

    // Ajoute à la fois XP et argent (stack pour action bar)
    public void addXpAndMoney(Player player, double xp, double money) {
        if (xp > 0) addXpNoActionBar(player, xp); // méthode ci-dessous
        if (money > 0) VaultHook.deposit(player, money);

        double totalXp = xpActionBarBuffer.getOrDefault(player, 0.0) + xp;
        double totalMoney = moneyActionBarBuffer.getOrDefault(player, 0.0) + money;
        xpActionBarBuffer.put(player, totalXp);
        moneyActionBarBuffer.put(player, totalMoney);

        sendXpAndMoneyActionBar(player, totalXp, totalMoney);

        XPLoopCounter counter = xpCounters.computeIfAbsent(player, p ->
                new XPLoopCounter(() -> {
                    xpActionBarBuffer.remove(p);
                    moneyActionBarBuffer.remove(p);
                    refreshXpDisplay(p);
                    xpCounters.remove(p);
                }, ACTION_BAR_DISPLAY_TICKS * 50));
        counter.startOrReset();
    }

    // Version addXp qui ne modifie que la progression, pas l'action bar directement
    public void addXpNoActionBar(Player player, double amount) {
        Profile profile = getProfile(player);
        int currentLevel = profile.getLevel();
        double xp = profile.getXp() + amount;

        int lastDefinedLevel = getMaxConfiguredLevel();
        boolean leveledUp = false;
        while (true) {
            int xpNeeded = ConfigManager.getXpForLevel(currentLevel);
            if (currentLevel >= lastDefinedLevel) {
                xp = Math.min(xp, xpNeeded);
                break;
            }
            if (xp >= xpNeeded) {
                xp -= xpNeeded;
                currentLevel++;
                leveledUp = true;
            } else {
                break;
            }
        }
        profile.setLevel(currentLevel);
        profile.setXp(xp);
        repository.saveProfile(profile);
        if (leveledUp) {
            player.sendMessage("§6Bravo ! Tu es passé niveau §e" + currentLevel + "§6 !");
        }
    }

    // Compatibilité : ancienne méthode unitaire pour bonus xp (seulement XP)
    public void addXp(Player player, double amount) {
        addXpAndMoney(player, amount, 0.0);
    }

    private void refreshXpDisplay(Player player) {
        double xp = xpActionBarBuffer.getOrDefault(player, 0.0);
        double money = moneyActionBarBuffer.getOrDefault(player, 0.0);
        if (xp <= 0 && money <= 0) sendActionBar(player, "");
        else sendXpAndMoneyActionBar(player, xp, money);
    }

    // Affichage combiné XP + Argent (format propre)
    public void sendXpAndMoneyActionBar(Player player, double xp, double money) {
        if (xp <= 0 && money <= 0) return;
        StringBuilder msg = new StringBuilder();
        if (xp > 0) msg.append("§a+").append(String.format("%.2f", xp)).append(" XP ");
        if (money > 0) msg.append("§e+").append(String.format("%.2f", money)).append("$");
        sendActionBar(player, msg.toString().trim());
    }

    public void sendActionBar(Player player, String message) {
        try {
            Object icbc = Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent$ChatSerializer")
                    .getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + message.replace("\"", "\\\"") + "\"}");
            Object packet = Class.forName("net.minecraft.server.v1_8_R3.PacketPlayOutChat")
                    .getConstructor(Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent"), byte.class)
                    .newInstance(icbc, (byte)2);
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass()
                    .getMethod("sendPacket", Class.forName("net.minecraft.server.v1_8_R3.Packet"))
                    .invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeXp(Player player, double amount) {
        Profile profile = getProfile(player);
        profile.setXp(Math.max(0, Math.round((profile.getXp() - amount) * 100) / 100.0));
        repository.saveProfile(profile);
        if (!xpActionBarBuffer.containsKey(player) || xpActionBarBuffer.get(player) <= 0) {
            refreshXpDisplay(player);
        }
    }

    public void setFreeze(Player player, boolean freeze) {
        Profile profile = getProfile(player);
        profile.setFrozen(freeze);
        repository.saveProfile(profile);
    }

    public boolean isFrozen(Player player) {
        return getProfile(player).isFrozen();
    }

    public double getXpForBlock(String block) {
        return blockXpMap.getOrDefault(block, 0.0);
    }

    public double getXpForSmelt(String item) {
        return smeltXpMap.getOrDefault(item, 0.0);
    }

    public double getXpForShopSell(String item) {
        return shopSellXpMap.getOrDefault(item, 0.0);
    }

    public double getXpForEnchantLevel(int enchantLevel) {
        return ConfigManager.getXpForEnchantLevel(enchantLevel);
    }

    public DrakariaProfile.MobXpConfig getMobXpConfig(String mob) {
        return mobXpMap.get(mob.toUpperCase());
    }


    public boolean hasClaimedReward(Player player, int level) {
        return getProfile(player).getClaimedRewards().contains(level);
    }

    public double getXpForAnvilLevel(int level) {
        return ConfigManager.getXpForAnvilLevel(level);
    }

    public void addClaimedReward(Profile profile, int level) {
        profile.getClaimedRewards().add(level);
        saveProfile(profile);
    }

    public void addClaimedReward(Player player, int level) {
        Profile profile = getProfile(player);
        profile.getClaimedRewards().add(level);
        saveProfile(profile);
    }

    public int getMaxConfiguredLevel() {
        Set<String> keys = ConfigManager.levelConfig.getConfigurationSection("levels").getKeys(false);
        int max = 0;
        for (String key : keys) {
            try {
                int n = Integer.parseInt(key);
                if (n > max) max = n;
            } catch (Exception ignored) {}
        }
        return max;
    }

    private Set<Integer> deserializeClaimedRewards(String data) {
        Set<Integer> set = new HashSet<>();
        if (data == null || data.trim().isEmpty()) return set;

        String[] parts = data.split(",");
        for (String part : parts) {
            try {
                set.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return set;
    }

    public List<Profile> getTop10ProfilesByQuestPoints() {
        List<Profile> top = new ArrayList<>();
        try (PreparedStatement ps = SQLiteManager.getConnection().prepareStatement(
                "SELECT * FROM profiles ORDER BY quest_points DESC LIMIT 10")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String name = rs.getString("name");
                double xp = rs.getDouble("xp");
                int level = rs.getInt("level");
                boolean frozen = rs.getBoolean("frozen");
                Set<Integer> rewards = deserializeClaimedRewards(rs.getString("claimed_rewards"));
                int qPoints = rs.getInt("quest_points");
                top.add(new Profile(uuid, name, xp, level, frozen, rewards, qPoints));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return top;
    }

    public String replacePlaceholders(Profile profile, String str) {
        int level = profile.getLevel();
        int maxLevel = getMaxConfiguredLevel();
        String maxXpStr;
        if (level >= maxLevel) {
            maxXpStr = "MAX";
        } else {
            maxXpStr = String.format("%.0f", (double)ConfigManager.getXpForLevel(level));
        }
        return str
                .replace("%player_level%", String.valueOf(level))
                .replace("%player_xp%", String.format("%.2f", profile.getXp()))
                .replace("%player_max_xp%", maxXpStr);
    }

    public String replacePlaceholders(Player player, String str) {
        return replacePlaceholders(getProfile(player), str);
    }
}
