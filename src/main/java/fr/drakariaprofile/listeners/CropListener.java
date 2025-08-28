package fr.drakariaprofile.listeners;

import fr.drakariaprofile.profile.ProfileManager;
import fr.drakariaprofile.storage.PlacedBlockRepository;
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
    private final Map<String, Double> cropXpMap;
    private final PlacedBlockRepository placedBlockRepo;

    public CropListener(ProfileManager profileManager, Map<String, Double> cropXpMap, PlacedBlockRepository placedBlockRepo) {
        this.profileManager = profileManager;
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
        double xp = 0.0;
        byte data = block.getData();

        switch (type) {
            // Cultures nécessitant la maturité
            case CROPS: // Blé mûr (data == 7)
                if (data == 7) xp = cropXpMap.getOrDefault("WHEAT", 0.0);
                break;
            case CARROT:
                if (data == 7) xp = cropXpMap.getOrDefault("CARROT", 0.0);
                break;
            case POTATO:
                if (data == 7) xp = cropXpMap.getOrDefault("POTATO", 0.0);
                break;
            case NETHER_WARTS:
                if (data == 3) xp = cropXpMap.getOrDefault("NETHER_WARTS", 0.0);
                break;
            case COCOA:
                if (data == 2) xp = cropXpMap.getOrDefault("COCOA", 0.0);
                break;
        }

        // Gestion melon/citrouille classique
        if (type == Material.MELON_BLOCK) {
            if (placedBlockRepo.isBlockPlaced(block)) {
                placedBlockRepo.removePlacedBlock(block);
                return;
            }
            xp = cropXpMap.getOrDefault("MELON_BLOCK", 0.0);
        }
        if (type == Material.PUMPKIN) {
            if (placedBlockRepo.isBlockPlaced(block)) {
                placedBlockRepo.removePlacedBlock(block);
                return;
            }
            xp = cropXpMap.getOrDefault("PUMPKIN", 0.0);
        }

        // Cactus/Canne : on donne xp pour la colonne entière
        if (type == Material.CACTUS || type == Material.SUGAR_CANE_BLOCK) {
            // On part du bloc cassé et on va VERS LE HAUT (toute la colonne)
            int blocksBroken = 0;
            Block current = block;
            do {
                if (placedBlockRepo.isBlockPlaced(current)) {
                    placedBlockRepo.removePlacedBlock(current);
                    // PAS d'XP pour ce bloc posé (mais on continue la colonne)
                } else {
                    String key = (type == Material.CACTUS) ? "CACTUS" : "SUGAR_CANE_BLOCK";
                    double blockXp = cropXpMap.getOrDefault(key, 0.0);
                    if (blockXp > 0) {
                        profileManager.addXp(player, blockXp);
                    }
                }
                current = current.getRelative(0, 1, 0);
                blocksBroken++;
            } while (current.getType() == type);

            // On ne fait rien ici car xp déjà donnée pour chaque bloc cassé
            return;
        }

        if (xp > 0) {
            profileManager.addXp(player, xp);
        }
    }
}
