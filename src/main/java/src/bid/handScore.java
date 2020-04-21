package src.bid;

public class handScore extends BonusScore {
    private int handScoreMin;
    private int handScoreMax;


    public handScore(int bonusScore, boolean vulnerable, int handScoreMin, int handScoreMax) {
        super(bonusScore, vulnerable);
        this.handScoreMin = handScoreMin;
        this.handScoreMax = handScoreMax;
    }

    @Override
    public boolean matches(Bid bid, int handScore, int tricks) {
        return super.matches(bid, handScore, tricks) && handScore >= handScoreMin && handScore <= handScoreMax;
    }
}
