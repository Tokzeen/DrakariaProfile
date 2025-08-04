package fr.drakariaprofile.profile;

import java.util.UUID;

public class Profile {
    private final UUID uuid;
    private String name;
    private double xp;
    private boolean frozen;

    public Profile(UUID uuid, String name, double xp, boolean frozen) {
        this.uuid = uuid;
        this.name = name;
        this.xp = xp;
        this.frozen = frozen;
    }
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public double getXp() { return xp; }
    public void setXp(double xp) { this.xp = xp; }
    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }
}
