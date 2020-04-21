package src.bid;

public abstract class BonusScore {
    private int bonusScore;
    private boolean vulnerable;

    public BonusScore(int bonusScore, boolean vulnerable) {
        this.bonusScore = bonusScore;
        this.vulnerable = vulnerable;
    }

    public boolean matches(Bid bid, int handScore, int tricks) {
        return bid.isVulnerable() == vulnerable;
    }

    public int getBonusScore() {
        return bonusScore;
    }
}
