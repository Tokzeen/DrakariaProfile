package fr.drakariaprofile.storage;

import fr.drakariaprofile.profile.Profile;

import java.sql.*;
import java.util.*;

public class ProfileRepository {

    // Crée ou charge le Profil avec tous les champs à jour
    public Profile getOrCreateProfile(UUID uuid, String name) {
        Profile profile = getProfile(uuid);
        if (profile == null) {
            profile = new Profile(uuid, name, 0.0, 0, false, new HashSet<>(), 0, 0, 0); // Ajoute upgrades valeurs par défaut
            saveProfile(profile);
        }
        return profile;
    }

    // Charge un profil depuis la BDD (TOUS les champs)
    public Profile getProfile(UUID uuid) {
        try (PreparedStatement pst = SQLiteManager.getConnection().prepareStatement(
                "SELECT * FROM profiles WHERE uuid=?;")) {
            pst.setString(1, uuid.toString());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    double xp = rs.getDouble("xp");
                    int level = rs.getInt("level");
                    boolean frozen = rs.getBoolean("frozen");

                    int questPoints = 0;
                    try { questPoints = rs.getInt("quest_points"); } catch (Exception e) {}

                    int upgradeChance = 0;
                    try { upgradeChance = rs.getInt("upgrade_chance"); } catch (Exception e) {}

                    int upgradeProductivite = 0;
                    try { upgradeProductivite = rs.getInt("upgrade_productivite"); } catch (Exception e) {}

                    Set<Integer> claimed = new HashSet<>();
                    String claimedStr = rs.getString("claimed_rewards");
                    if (claimedStr != null && !claimedStr.isEmpty()) {
                        for (String part : claimedStr.split(",")) {
                            try { claimed.add(Integer.parseInt(part.trim())); } catch (Exception e) {}
                        }
                    }
                    return new Profile(
                            uuid,
                            name,
                            xp,
                            level,
                            frozen,
                            claimed,
                            questPoints,
                            upgradeChance,
                            upgradeProductivite
                    );
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Sauvegarde complète du profil (incluant points d'upgrade)
    public void saveProfile(Profile profile) {
        try (PreparedStatement pst = SQLiteManager.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO profiles"+
                        "(uuid, name, xp, level, frozen, claimed_rewards, quest_points, upgrade_chance, upgrade_productivite) "+
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);"
        )) {
            pst.setString(1, profile.getUuid().toString());
            pst.setString(2, profile.getName());
            pst.setDouble(3, profile.getXp());
            pst.setInt(4, profile.getLevel());
            pst.setBoolean(5, profile.isFrozen());
            String claimedStr = String.join(",",
                    profile.getClaimedRewards().stream().map(String::valueOf).toArray(String[]::new)
            );
            pst.setString(6, claimedStr);
            pst.setInt(7, profile.getQuestPoints());
            pst.setInt(8, profile.getUpgradeChance());
            pst.setInt(9, profile.getUpgradeProductivite());
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
