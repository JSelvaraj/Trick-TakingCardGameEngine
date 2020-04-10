package src.ai;

import src.card.Card;

import java.util.List;

public class POMCPTreeNode {
    private int visit;
    private int value;
    private GameObservation observation;
    private Card action;
    private List<POMCPTree> children;

    public void setVisit(int visit) {
        this.visit = visit;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setObservation(GameObservation observation) {
        this.observation = observation;
    }

    public void setAction(Card action) {
        this.action = action;
    }

    public int getVisit() {
        return visit;
    }

    public int getValue() {
        return value;
    }

    public GameObservation getObservation() {
        return observation;
    }

    public Card getAction() {
        return action;
    }

    public List<POMCPTree> getChildren() {
        return children;
    }
}
