package fr.drakariaprofile.quest;

import java.util.*;

public class QuestPlayerData {
    private final UUID playerUUID;
    private final Map<QuestCategory, List<PlayerQuestProgress>> assignedQuests = new HashMap<>();
    private final Map<QuestCategory, Boolean> bonusClaimed = new HashMap<>();
    private int points;

    public QuestPlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() { return playerUUID; }

    public Map<QuestCategory, List<PlayerQuestProgress>> getAssignedQuests() {
        return assignedQuests;
    }

    public List<PlayerQuestProgress> getQuests(QuestCategory category) {
        return assignedQuests.getOrDefault(category, Collections.emptyList());
    }

    public void setQuests(QuestCategory category, List<PlayerQuestProgress> quests) {
        assignedQuests.put(category, quests);
    }

    public void addPoints(int amount) { this.points += amount; }
    public int getPoints() { return points; }

    public boolean isBonusClaimed(QuestCategory cat) {
        return bonusClaimed.getOrDefault(cat, false);
    }

    public void setBonusClaimed(QuestCategory cat, boolean claimed) {
        bonusClaimed.put(cat, claimed);
    }

    public void resetDailyQuests() {
        assignedQuests.clear();
        bonusClaimed.clear();
    }
}
