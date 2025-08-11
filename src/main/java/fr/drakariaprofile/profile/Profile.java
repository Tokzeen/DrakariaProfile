package fr.drakariaprofile.profile;

import java.util.*;

public class Profile {
    private final UUID uuid;
    private String name;
    private double xp;
    private int level;
    private boolean frozen;
    private Set<Integer> claimedRewards = new HashSet<>();
    private int questPoints; // ✅ Points de quêtes globaux

    public Profile(UUID uuid, String name, double xp, int level, boolean frozen, Set<Integer> claimedRewards, int questPoints) {
        this.uuid = uuid;
        this.name = name;
        this.xp = xp;
        this.level = level;
        this.frozen = frozen;
        this.claimedRewards = claimedRewards != null ? claimedRewards : new HashSet<>();
        this.questPoints = questPoints;
    }

    public Profile(UUID uuid, String name, double xp, int level, boolean frozen) {
        this(uuid, name, xp, level, frozen, new HashSet<>(), 0);
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public double getXp() { return xp; }
    public void setXp(double xp) { this.xp = xp; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }
    public Set<Integer> getClaimedRewards() { return claimedRewards; }
    public void setClaimedRewards(Set<Integer> claimedRewards) { this.claimedRewards = claimedRewards != null ? claimedRewards : new HashSet<>(); }

    // ✅ Points de quêtes
    public int getQuestPoints() { return questPoints; }
    public void setQuestPoints(int questPoints) { this.questPoints = questPoints; }
    public void addQuestPoints(int amount) { this.questPoints += amount; }
}
