package fr.drakariaprofile.storage;

import fr.drakariaprofile.profile.Profile;

import java.sql.*;
import java.util.UUID;

public class ProfileRepository {

    public Profile getOrCreateProfile(UUID uuid, String name) {
        Profile profile = getProfile(uuid);
        if (profile == null) {
            profile = new Profile(uuid, name, 0.0, false);
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
                    return new Profile(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("name"),
                            rs.getDouble("xp"),
                            rs.getBoolean("frozen")
                    );
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void saveProfile(Profile profile) {
        try (PreparedStatement pst = SQLiteManager.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO profiles(uuid, name, xp, frozen) VALUES (?, ?, ?, ?);")) {
            pst.setString(1, profile.getUuid().toString());
            pst.setString(2, profile.getName());
            pst.setDouble(3, profile.getXp());
            pst.setBoolean(4, profile.isFrozen());
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
