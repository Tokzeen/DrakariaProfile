package fr.drakariaprofile.listeners;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.storage.PlacedBlockRepository;
import fr.drakariaprofile.storage.UpgradeRepository;
import fr.drakariaprofile.utils.VaultHook;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Map;

public class CropListener implements Listener {

    private final ProfileManager profileManager;
    private final UpgradeRepository upgradeRepository;
    private final Map<String, Double> cropXpMap;
    private final PlacedBlockRepository placedBlockRepo;

    public CropListener(ProfileManager profileManager, UpgradeRepository upgradeRepository,
                        Map<String, Double> cropXpMap, PlacedBlockRepository placedBlockRepo) {
        this.profileManager = profileManager;
        this.upgradeRepository = upgradeRepository;
        this.cropXpMap = cropXpMap;
        this.placedBlockRepo = placedBlockRepo;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Material type = event.getBlock().getType();
        if (type == Material.PUMPKIN
                || type == Material.MELON_BLOCK
                || type == Material.CACTUS
                || type == Material.SUGAR_CANE_BLOCK) {
            placedBlockRepo.addPlacedBlock(event.getBlock());
        }
    }

    @EventHandler
    public void onCropBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();
        double baseXp = 0.0;
        String cropKey = null;
        byte data = block.getData();

        // Trouver le bon crop/resource (clé, XP de base)
        switch (type) {
            case CROPS:
                if (data == 7) { baseXp = cropXpMap.getOrDefault("WHEAT", 0.0); cropKey = "WHEAT"; }
                break;
            case CARROT:
                if (data == 7) { baseXp = cropXpMap.getOrDefault("CARROT", 0.0); cropKey = "CARROT"; }
                break;
            case POTATO:
                if (data == 7) { baseXp = cropXpMap.getOrDefault("POTATO", 0.0); cropKey = "POTATO"; }
                break;
            case NETHER_WARTS:
                if (data == 3) { baseXp = cropXpMap.getOrDefault("NETHER_WARTS", 0.0); cropKey = "NETHER_WARTS"; }
                break;
            case COCOA:
                if (data == 2) { baseXp = cropXpMap.getOrDefault("COCOA", 0.0); cropKey = "COCOA"; }
                break;
        }

        if (type == Material.MELON_BLOCK) {
            cropKey = "MELON_BLOCK";
            if (placedBlockRepo.isBlockPlaced(block)) {
                placedBlockRepo.removePlacedBlock(block);
                return;
            }
            baseXp = cropXpMap.getOrDefault("MELON_BLOCK", 0.0);
        }
        if (type == Material.PUMPKIN) {
            cropKey = "PUMPKIN";
            if (placedBlockRepo.isBlockPlaced(block)) {
                placedBlockRepo.removePlacedBlock(block);
                return;
            }
            baseXp = cropXpMap.getOrDefault("PUMPKIN", 0.0);
        }

        // Pour cactus et canne à sucre : colonne entière (bonus appliqué à chaque bloc)
        if (type == Material.CACTUS || type == Material.SUGAR_CANE_BLOCK) {
            String key = (type == Material.CACTUS) ? "CACTUS" : "SUGAR_CANE_BLOCK";
            int level = upgradeRepository.getUpgradeLevel(player.getUniqueId(), "farmer_" + key);
            double coef = 1.0 + (level * 0.5);
            int gainMoney = level;
            double gainXp = cropXpMap.getOrDefault(key, 0.0) * coef;
            Block current = block;
            do {
                if (placedBlockRepo.isBlockPlaced(current)) {
                    placedBlockRepo.removePlacedBlock(current);
                } else {
                    if (gainXp > 0) profileManager.addXp(player, gainXp);
                    if (gainMoney > 0) VaultHook.deposit(player, gainMoney);
                }
                current = current.getRelative(0, 1, 0);
            } while (current.getType() == type);
            return;
        }

        // Pour les autres crops
        if (baseXp > 0 && cropKey != null) {
            int level = upgradeRepository.getUpgradeLevel(player.getUniqueId(), "farmer_" + cropKey);
            double coef = 1.0 + (level * 0.5);
            int gainMoney = level;
            double gainXp = baseXp * coef;
            profileManager.addXp(player, gainXp);
            if (gainMoney > 0) VaultHook.deposit(player, gainMoney);
        }
    }
}
