package fr.drakariaprofile.storage;

import fr.drakariaprofile.DrakariaProfile;
import java.io.File;
import java.sql.*;

public class SQLiteManager {
    private static Connection connection;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            File dbFile = new File(DrakariaProfile.getInstance().getDataFolder(), "profiles.db");
            dbFile.getParentFile().mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            Statement st = connection.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS profiles (" +
                    "uuid TEXT PRIMARY KEY," +
                    "name TEXT," +
                    "xp REAL," +
                    "level INT," +
                    "frozen BOOLEAN," +
                    "quest_points INT DEFAULT 0," + // ✅
                    "claimed_rewards TEXT" +
                    ");");
            st.close();

            // Colonne quest_points si manquante
            try (Statement alter = connection.createStatement()) {
                alter.executeUpdate("ALTER TABLE profiles ADD COLUMN quest_points INT DEFAULT 0;");
            } catch (SQLException ignored) {}

            // Bonus coffres
            try (Statement stBonus = connection.createStatement()) {
                stBonus.executeUpdate("CREATE TABLE IF NOT EXISTS player_quest_bonus (" +
                        "uuid TEXT," +
                        "category TEXT," +
                        "bonus_claimed BOOLEAN," +
                        "PRIMARY KEY (uuid, category)" +
                        ");");
            }

            Statement st2 = connection.createStatement();
            st2.executeUpdate("CREATE TABLE IF NOT EXISTS player_quests (" +
                    "uuid TEXT," +
                    "category TEXT," +
                    "quest_id TEXT," +
                    "amount INT," +
                    "progress INT," +
                    "complete BOOLEAN," +
                    "consumed BOOLEAN," +
                    "PRIMARY KEY (uuid, category, quest_id)" +
                    ");");
            st2.close();

        } catch (ClassNotFoundException e) {
            System.err.println("JDBC SQLite driver missing !");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() { return connection; }

    public static void disconnect() {
        if (connection != null) {
            try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
            connection = null;
        }
    }
}
