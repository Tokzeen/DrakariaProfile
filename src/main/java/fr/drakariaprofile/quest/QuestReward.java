package fr.drakariaprofile.quest;

public class QuestReward {
    private final int points;
    private final int xpMin, xpMax;
    private final String command;

    public QuestReward(int points, int xpMin, int xpMax, String command) {
        this.points = points;
        this.xpMin = xpMin;
        this.xpMax = xpMax;
        this.command = command;
    }

    public int getPoints() { return points; }
    public int getXpMin() { return xpMin; }
    public int getXpMax() { return xpMax; }
    public String getCommand() { return command; }
}
