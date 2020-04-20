package src.bid;

public abstract class BonusScore {
    private int bonusScore;
    private boolean vulnerable;

    public boolean matches(Bid bid, int handScore, int tricks) {
        return bid.isVulnerable() == vulnerable;
    }
}
