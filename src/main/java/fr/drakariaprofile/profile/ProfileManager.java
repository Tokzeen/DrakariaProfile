package fr.drakariaprofile.profile;

import fr.drakariaprofile.DrakariaProfile;
import fr.drakariaprofile.storage.ProfileRepository;
import fr.drakariaprofile.utils.XPLoopCounter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ProfileManager {
    private final ProfileRepository repository = new ProfileRepository();

    // Buffers action bar & timers
    private final Map<Player, Double> xpActionBarBuffer = new HashMap<>();
    private final Map<Player, XPLoopCounter> xpCounters = new HashMap<>();

    // Sources XP
    private final Map<String, Double> blockXpMap;
    private final Map<String, Double> smeltXpMap;
    private final Map<String, Double> shopSellXpMap;

    private final int ACTION_BAR_DISPLAY_TICKS = 15; // 15 ticks = 0.75s

    // Ajoute shopSellXpMap au constructeur
    public ProfileManager(Map<String, Double> blockXpMap, Map<String, Double> smeltXpMap, Map<String, Double> shopSellXpMap) {
        this.blockXpMap = blockXpMap;
        this.smeltXpMap = smeltXpMap;
        this.shopSellXpMap = shopSellXpMap;
    }

    // Chargement/sauvegarde profil SQL
    public Profile getProfile(Player player) {
        return repository.getOrCreateProfile(player.getUniqueId(), player.getName());
    }

    public double getXp(Player player) {
        return getProfile(player).getXp();
    }

    // Ajout d’XP (toutes sources)
    public void addXp(Player player, double amount) {
        // 1. Sauvegarde en SQL
        Profile profile = getProfile(player);
        double newXp = Math.round((profile.getXp() + amount) * 100.0) / 100.0;
        profile.setXp(newXp);
        repository.saveProfile(profile);

        // 2. Buffer & affichage immédiat
        double totalBuffer = xpActionBarBuffer.getOrDefault(player, 0.0) + amount;
        xpActionBarBuffer.put(player, totalBuffer);
        sendXpActionBar(player, totalBuffer);

        // 3. Timer de purge automatique
        XPLoopCounter counter = xpCounters.computeIfAbsent(player, p ->
                new XPLoopCounter(() -> {
                    xpActionBarBuffer.remove(p);
                    refreshXpDisplay(p);
                    xpCounters.remove(p);
                }, ACTION_BAR_DISPLAY_TICKS * 50));
        counter.startOrReset();
    }

    // Nettoyage ou refresh action bar
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

    // Affichage NMS Spigot 1.8.8
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

    // Retrait d’XP et update affichage
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

    // Mapping XP cassage
    public double getXpForBlock(String block) {
        return blockXpMap.getOrDefault(block, 0.0);
    }
    // Mapping XP smelt
    public double getXpForSmelt(String item) {
        return smeltXpMap.getOrDefault(item, 0.0);
    }
    // Mapping XP vente shop
    public double getXpForShopSell(String item) {
        return shopSellXpMap.getOrDefault(item, 0.0);
    }
}
