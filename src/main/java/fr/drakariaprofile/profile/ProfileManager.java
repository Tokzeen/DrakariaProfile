package fr.drakariaprofile.profile;

import fr.drakariaprofile.DrakariaProfile;
import fr.drakariaprofile.config.ConfigManager;
import fr.drakariaprofile.storage.ProfileRepository;
import fr.drakariaprofile.utils.XPLoopCounter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ProfileManager {
    private final ProfileRepository repository = new ProfileRepository();
    private final Map<Player, Double> xpActionBarBuffer = new HashMap<>();
    private final Map<Player, XPLoopCounter> xpCounters = new HashMap<>();

    private final Map<String, Double> blockXpMap;
    private final Map<String, Double> smeltXpMap;
    private final Map<String, Double> shopSellXpMap;
    private final Map<String, DrakariaProfile.MobXpConfig> mobXpMap;
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

    // -------- FIX: support offline players via UUID&name
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

    public void addXp(Player player, double amount) {
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
        double totalBuffer = xpActionBarBuffer.getOrDefault(player, 0.0) + amount;
        xpActionBarBuffer.put(player, totalBuffer);
        sendXpActionBar(player, totalBuffer);
        XPLoopCounter counter = xpCounters.computeIfAbsent(player, p ->
                new XPLoopCounter(() -> {
                    xpActionBarBuffer.remove(p);
                    refreshXpDisplay(p);
                    xpCounters.remove(p);
                }, ACTION_BAR_DISPLAY_TICKS * 50));
        counter.startOrReset();
    }

    private void refreshXpDisplay(Player player) {
        if (!xpActionBarBuffer.containsKey(player) || xpActionBarBuffer.get(player) <= 0) {
            sendActionBar(player, "");
        } else {
            sendXpActionBar(player, xpActionBarBuffer.get(player));
        }
    }

    private void sendXpActionBar(Player player, double total) {
        sendActionBar(player, total > 0 ? "§a+" + Math.round(total * 10.0) / 10.0 + " XP !" : "");
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

    // ----------- MOB KILL XP -----------
    public DrakariaProfile.MobXpConfig getMobXpConfig(String mob) {
        return mobXpMap.get(mob.toUpperCase());
    }

    // ----------- Récompenses -----------
    public boolean hasClaimedReward(Player player, int level) {
        return getProfile(player).getClaimedRewards().contains(level);
    }

    // Pour menu GUI (avec Profile déjà obtenu)
    public void addClaimedReward(Profile profile, int level) {
        profile.getClaimedRewards().add(level);
        saveProfile(profile);
    }

    // Pour commande/usage API/player (optionnel)
    public void addClaimedReward(Player player, int level) {
        Profile profile = getProfile(player);
        profile.getClaimedRewards().add(level);
        saveProfile(profile);
    }


    // ----------- Level utils ----------
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

    // ----------- Placeholders pour PROFILE OFFLINE -----------
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

    // (Tu gardes aussi l'existante pour Player online si tu veux)
    public String replacePlaceholders(Player player, String str) {
        return replacePlaceholders(getProfile(player), str);
    }
}
