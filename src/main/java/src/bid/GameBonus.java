package src.bid;

public class GameBonus extends BonusScore {
    int tricks;

    public GameBonus(int bonusScore, boolean vulnerable, int tricks) {
        super(bonusScore, vulnerable);
        this.tricks = tricks;
    }

    @Override
    public boolean matches(Bid bid, int handScore, int tricks) {
        return super.matches(bid, handScore, tricks) && tricks == this.tricks;
    }

    public int getTricks() {
        return tricks;
    }
}
