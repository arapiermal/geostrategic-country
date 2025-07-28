package com.erimali.cntrygame;
public enum CPowerTier {
    NON_POWER("Non-Power", 0, "No significant influence, often dependent on others."),
    SMALL_POWER("Small Power", 1, "Limited influence, often reacts to global events."),
    MIDDLE_POWER("Middle Power", 2, "Independent and diplomatically active in regional and global affairs."),
    GREAT_POWER("Great Power", 3, "Major influence in world politics, military, and economy."),
    SUPERPOWER("Superpower", 4, "Global dominance with unmatched projection of military, economic, and cultural power.");

    private final String label;
    private final int rank;
    private final String description;

    CPowerTier(String label, int rank, String description) {
        this.label = label;
        this.rank = rank;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public int getRank() {
        return rank;
    }
// Stuff changeable based on language (?!)
    public String getDescription() {
        return description;
    }
// Calculate score then assign (Military should be small input in score)
    public static CPowerTier fromRank(int score) {
        if (score >= 100000) return SUPERPOWER;
        else if (score >= 10000) return GREAT_POWER;
        else if (score >= 1000) return MIDDLE_POWER;
        else if (score >= 100) return SMALL_POWER;
        else return NON_POWER;
    }
}
