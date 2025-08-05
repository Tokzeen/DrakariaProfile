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
                    "frozen BOOLEAN" +
                    ");");
            st.close();

            // ----- Ajoute ce BLOC juste après la création de la table -----
            Statement alter = null;
            try {
                alter = connection.createStatement();
                alter.executeUpdate("ALTER TABLE profiles ADD COLUMN claimed_rewards TEXT;");
            } catch (SQLException ignored) {
                // Ignore l'erreur si la colonne existe déjà
            } finally {
                if (alter != null) try { alter.close(); } catch (SQLException ignore) {}
            }
            // -------------------------------------------------------------

        } catch (ClassNotFoundException e) {
            System.err.println("Le driver JDBC SQLite n'est pas présent dans le plugin !");
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
