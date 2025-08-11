package fr.drakariaprofile.quest;

public class PlayerQuestProgress {
    private final Quest quest;
    private int progress;
    private boolean complete;
    private boolean consumed; // pour savoir si la récompense a déjà été prise

    public PlayerQuestProgress(Quest quest) {
        this.quest = quest;
        this.progress = 0;
        this.complete = false;
        this.consumed = false;
    }

    public Quest getQuest() {
        return quest;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void resetProgress() {
        this.progress = 0;
        this.complete = false;
        this.consumed = false;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }


    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    /**
     * Ajoute un nombre d'actions à la progression
     */
    public void addProgress(int amount) {
        if (!complete) {
            this.progress += amount;
            if (this.progress >= quest.getAmount()) {
                this.progress = quest.getAmount();
                this.complete = true;
            }
        }
    }

    /**
     * Définit directement la progression (utile pour chargement SQL)
     */
    public void setProgress(int amount) {
        this.progress = Math.min(amount, quest.getAmount());
        if (this.progress >= quest.getAmount()) {
            this.complete = true;
        }
    }
}
