package fr.drakariaprofile.storage;

import fr.drakariaprofile.profile.Profile;

import java.sql.*;
import java.util.*;

public class ProfileRepository {

    public Profile getOrCreateProfile(UUID uuid, String name) {
        Profile profile = getProfile(uuid);
        if (profile == null) {
            profile = new Profile(uuid, name, 0.0, 0, false, new HashSet<>());
            saveProfile(profile);
        }
        return profile;
    }

    public Profile getProfile(UUID uuid) {
        try (PreparedStatement pst = SQLiteManager.getConnection().prepareStatement(
                "SELECT * FROM profiles WHERE uuid=?;")) {
            pst.setString(1, uuid.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int level = rs.getInt("level");
                    Set<Integer> claimed = new HashSet<>();
                    String claimedStr = rs.getString("claimed_rewards");
                    if (claimedStr != null && !claimedStr.isEmpty()) {
                        for (String part : claimedStr.split(",")) {
                            try { claimed.add(Integer.parseInt(part.trim())); } catch (Exception e) {}
                        }
                    }
                    return new Profile(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("name"),
                            rs.getDouble("xp"),
                            level,
                            rs.getBoolean("frozen"),
                            claimed
                    );
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void saveProfile(Profile profile) {
        try (PreparedStatement pst = SQLiteManager.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO profiles(uuid, name, xp, level, frozen, claimed_rewards) VALUES (?, ?, ?, ?, ?, ?);")) {
            pst.setString(1, profile.getUuid().toString());
            pst.setString(2, profile.getName());
            pst.setDouble(3, profile.getXp());
            pst.setInt(4, profile.getLevel());
            pst.setBoolean(5, profile.isFrozen());
            String claimedStr = String.join(",", profile.getClaimedRewards().stream()
                    .map(String::valueOf).toArray(String[]::new));
            pst.setString(6, claimedStr);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
