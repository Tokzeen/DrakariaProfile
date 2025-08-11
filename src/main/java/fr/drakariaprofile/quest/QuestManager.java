package fr.drakariaprofile.quest;

import fr.drakariaprofile.DrakariaProfile;
import fr.drakariaprofile.storage.SQLiteManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class QuestManager {

    private final Map<UUID, QuestPlayerData> playerQuestData = new HashMap<>();
    private final Map<String, List<Quest>> questsByCategory;

    public QuestManager(File dataFolder) {
        File questFile = new File(dataFolder, "quests.yml");
        if (!questFile.exists()) {
            DrakariaProfile.getInstance().saveResource("quests.yml", false);
        }
        this.questsByCategory = QuestLoader.loadAllQuests(questFile);
    }

    public QuestPlayerData getPlayerData(UUID uuid) {
        QuestPlayerData data = playerQuestData.get(uuid);
        if (data != null) return data;
        data = loadPlayerQuests(uuid);
        if (data == null || data.getAssignedQuests().isEmpty()) {
            data = new QuestPlayerData(uuid);
            playerQuestData.put(uuid, data);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) assignDailyQuests(player);
        } else {
            playerQuestData.put(uuid, data);
        }
        return data;
    }

    public void assignDailyQuests(Player player) {
        UUID uuid = player.getUniqueId();
        QuestPlayerData data = playerQuestData.computeIfAbsent(uuid, QuestPlayerData::new);
        for (QuestCategory cat : QuestCategory.values()) {
            List<Quest> pool = new ArrayList<>(questsByCategory.getOrDefault(cat.name(), Collections.emptyList()));
            Collections.shuffle(pool);
            List<PlayerQuestProgress> picks = new ArrayList<>();
            for (int i = 0; i < 3 && i < pool.size(); i++) {
                picks.add(new PlayerQuestProgress(pool.get(i)));
            }
            data.setQuests(cat, picks);
            data.setBonusClaimed(cat, false); // reset bonus
        }
        savePlayerQuests(uuid);
    }

    /** Reset complet (quêtes + bonus) pour tous les joueurs (en SQL) */
    public void resetAllQuestsAndBonus() {
        playerQuestData.clear(); // reset mémoire
        try (Statement st = SQLiteManager.getConnection().createStatement()) {
            st.executeUpdate("DELETE FROM player_quests;");
            st.executeUpdate("UPDATE player_quest_bonus SET bonus_claimed = 0;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Bukkit.getLogger().info("[Quests] Toutes les quêtes et bonus ont été réinitialisés.");
    }

    /** Planificateur : reset chaque jour à minuit pile */
    public void scheduleDailyResetProd() {
        long ticksPerDay = 24 * 60 * 60 * 20L; // 24h = 1_728_000 ticks
        long initialDelay = computeInitialDelayToMidnightTicks();
        Bukkit.getScheduler().runTaskTimer(
                DrakariaProfile.getInstance(),
                this::resetAllQuestsAndBonus,
                initialDelay,
                ticksPerDay
        );
        Bukkit.getLogger().info("[Quests] Prochain reset dans " + (initialDelay/20) + " secondes.");
    }

    /** Pour tests : reset toutes les N minutes */
    public void scheduleResetTestMinutes(int minutes) {
        long ticksPerPeriod = minutes * 60L * 20L;
        Bukkit.getScheduler().runTaskTimer(
                DrakariaProfile.getInstance(),
                this::resetAllQuestsAndBonus,
                ticksPerPeriod,
                ticksPerPeriod
        );
    }

    /** Calcule le délai jusqu'à minuit (en ticks) */
    private long computeInitialDelayToMidnightTicks() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        long millis = Duration.between(now, nextMidnight).toMillis();
        return millis / 50;
    }

    public QuestPlayerData loadPlayerQuests(UUID uuid) {
        QuestPlayerData data = new QuestPlayerData(uuid);

        // Charger progression
        try (PreparedStatement ps = SQLiteManager.getConnection().prepareStatement(
                "SELECT category, quest_id, amount, progress, complete, consumed FROM player_quests WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                QuestCategory cat = QuestCategory.valueOf(rs.getString("category"));
                Quest quest = findQuestByIdAndAmount(cat, rs.getString("quest_id"), rs.getInt("amount"));
                if (quest != null) {
                    PlayerQuestProgress pq = new PlayerQuestProgress(quest);
                    pq.setProgress(rs.getInt("progress"));
                    pq.setConsumed(rs.getBoolean("consumed"));
                    if (rs.getBoolean("complete")) pq.setProgress(quest.getAmount());
                    data.getAssignedQuests().computeIfAbsent(cat, k -> new ArrayList<>()).add(pq);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Charger bonusClaimed
        try (PreparedStatement ps = SQLiteManager.getConnection().prepareStatement(
                "SELECT category, bonus_claimed FROM player_quest_bonus WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                QuestCategory cat = QuestCategory.valueOf(rs.getString("category"));
                data.setBonusClaimed(cat, rs.getBoolean("bonus_claimed"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return data;
    }

    public void savePlayerQuests(UUID uuid) {
        QuestPlayerData data = playerQuestData.get(uuid);
        if (data == null) return;

        // Save progression
        try (PreparedStatement delete = SQLiteManager.getConnection().prepareStatement(
                "DELETE FROM player_quests WHERE uuid = ?")) {
            delete.setString(1, uuid.toString());
            delete.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        try (PreparedStatement ps = SQLiteManager.getConnection().prepareStatement(
                "REPLACE INTO player_quests (uuid, category, quest_id, amount, progress, complete, consumed) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            for (QuestCategory cat : QuestCategory.values()) {
                for (PlayerQuestProgress pq : data.getQuests(cat)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, cat.name());
                    ps.setString(3, pq.getQuest().getId());
                    ps.setInt(4, pq.getQuest().getAmount());
                    ps.setInt(5, pq.getProgress());
                    ps.setBoolean(6, pq.isComplete());
                    ps.setBoolean(7, pq.isConsumed());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        } catch (SQLException e) { e.printStackTrace(); }

        // Save bonus
        try (PreparedStatement psBonus = SQLiteManager.getConnection().prepareStatement(
                "REPLACE INTO player_quest_bonus (uuid, category, bonus_claimed) VALUES (?, ?, ?)")) {
            for (QuestCategory cat : QuestCategory.values()) {
                psBonus.setString(1, uuid.toString());
                psBonus.setString(2, cat.name());
                psBonus.setBoolean(3, data.isBonusClaimed(cat));
                psBonus.addBatch();
            }
            psBonus.executeBatch();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Quest findQuestByIdAndAmount(QuestCategory cat, String id, int amount) {
        for (Quest q : questsByCategory.getOrDefault(cat.name(), Collections.emptyList())) {
            if (q.getId().equals(id) && q.getAmount() == amount) return q;
        }
        return null;
    }

    public List<Quest> getQuestsByCategory(QuestCategory cat) {
        return questsByCategory.getOrDefault(cat.name(), Collections.emptyList());
    }

    public void saveAllPlayers() {
        for (UUID uuid : playerQuestData.keySet()) {
            savePlayerQuests(uuid);
        }
    }
}
