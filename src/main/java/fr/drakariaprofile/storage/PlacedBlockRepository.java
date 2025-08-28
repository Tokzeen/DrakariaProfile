package fr.drakariaprofile.storage;

import org.bukkit.block.Block;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlacedBlockRepository {

    private final Connection connection;

    public PlacedBlockRepository(Connection connection) {
        this.connection = connection;
        createTable();
    }

    // Crée la table si ce n'est pas déjà fait
    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS placed_blocks (" +
                "world TEXT NOT NULL, " +
                "x INTEGER NOT NULL, " +
                "y INTEGER NOT NULL, " +
                "z INTEGER NOT NULL, " +
                "type TEXT NOT NULL, " +
                "PRIMARY KEY (world, x, y, z, type)" +
                ")";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPlacedBlock(Block block) {
        String sql = "INSERT OR IGNORE INTO placed_blocks (world, x, y, z, type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, block.getWorld().getName());
            ps.setInt(2, block.getX());
            ps.setInt(3, block.getY());
            ps.setInt(4, block.getZ());
            ps.setString(5, block.getType().name());
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isBlockPlaced(Block block) {
        String sql = "SELECT 1 FROM placed_blocks WHERE world = ? AND x = ? AND y = ? AND z = ? AND type = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, block.getWorld().getName());
            ps.setInt(2, block.getX());
            ps.setInt(3, block.getY());
            ps.setInt(4, block.getZ());
            ps.setString(5, block.getType().name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removePlacedBlock(Block block) {
        String sql = "DELETE FROM placed_blocks WHERE world = ? AND x = ? AND y = ? AND z = ? AND type = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, block.getWorld().getName());
            ps.setInt(2, block.getX());
            ps.setInt(3, block.getY());
            ps.setInt(4, block.getZ());
            ps.setString(5, block.getType().name());
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
