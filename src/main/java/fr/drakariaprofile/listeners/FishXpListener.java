package fr.drakariaprofile.listeners;

import fr.drakariaprofile.profile.ProfileManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class FishXpListener implements Listener {

    private final ProfileManager profileManager;
    private final Map<String, Double> lootXpMap;

    public FishXpListener(ProfileManager profileManager, Map<String, Double> lootXpMap) {
        this.profileManager = profileManager;
        this.lootXpMap = lootXpMap;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof org.bukkit.entity.Item)) return;

        Player player = event.getPlayer();
        ItemStack caught = ((org.bukkit.entity.Item) event.getCaught()).getItemStack();
        Material material = caught.getType();
        short data = caught.getDurability();

        String key = material.name();
        if (material == Material.RAW_FISH) {
            key += "-" + data;
        }

        // Pour tous les autres loots (pas RAW_FISH)
        if (lootXpMap.containsKey(key)) {
            double xp = lootXpMap.get(key);
            if (xp > 0) profileManager.addXp(player, xp);
        }
    }
}
