package fr.drakariaprofile.quest;

import org.bukkit.inventory.ItemStack;

public class Quest {
    private final String id;
    private final QuestCategory category;
    private final int amount;
    private final QuestReward reward;
    private final ItemStack displayItem;

    public Quest(String id, QuestCategory category, int amount, QuestReward reward, ItemStack displayItem) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.reward = reward;
        this.displayItem = displayItem;
    }

    public String getId() { return id; }
    public QuestCategory getCategory() { return category; }
    public int getAmount() { return amount; }
    public QuestReward getReward() { return reward; }
    public ItemStack getDisplayItem() { return displayItem; }
}
