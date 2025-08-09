package fr.drakariaprofile.listeners;

import fr.drakariaprofile.profile.ProfileManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent; // <-- BON IMPORT
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import java.util.Set;
import java.util.Collections;
import java.util.WeakHashMap;

public class EnchantListener implements Listener {
    private final ProfileManager profileManager;
    private final Plugin plugin;
    private final Set<String> processedEnchantments = Collections.newSetFromMap(new WeakHashMap<>());

    public EnchantListener(ProfileManager profileManager, Plugin plugin) {
        this.profileManager = profileManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        int level = event.getExpLevelCost();

        String uniqueKey = player.getUniqueId() + ":"
                + event.getEnchantBlock().getLocation().toString()
                + ":" + System.currentTimeMillis();
        if (processedEnchantments.contains(uniqueKey)) return;
        processedEnchantments.add(uniqueKey);

        double xp = profileManager.getXpForEnchantLevel(level);
        if (xp > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    profileManager.addXp(player, xp);
                }
            }.runTaskLater(plugin, 1L);
        }
    }
}
