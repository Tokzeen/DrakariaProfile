package fr.drakariaprofile.storage;

import java.sql.*;
import java.util.*;

public class UpgradeRepository {
    public int getUpgradeLevel(UUID uuid, String upgradeKey) {
        try (PreparedStatement pst = SQLiteManager.getConnection().prepareStatement(
                "SELECT level FROM player_upgrades WHERE uuid=? AND upgrade_key=?")) {
            pst.setString(1, uuid.toString());
            pst.setString(2, upgradeKey);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("level");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void setUpgradeLevel(UUID uuid, String upgradeKey, int level) {
        try (PreparedStatement pst = SQLiteManager.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO player_upgrades (uuid, upgrade_key, level) VALUES (?, ?, ?)"
        )) {
            pst.setString(1, uuid.toString());
            pst.setString(2, upgradeKey);
            pst.setInt(3, level);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Map<String, Integer> getUpgrades(UUID uuid) {
        Map<String, Integer> upgrades = new HashMap<>();
        try (PreparedStatement pst = SQLiteManager.getConnection().prepareStatement(
                "SELECT upgrade_key, level FROM player_upgrades WHERE uuid=?")) {
            pst.setString(1, uuid.toString());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    upgrades.put(rs.getString("upgrade_key"), rs.getInt("level"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return upgrades;
    }
}
