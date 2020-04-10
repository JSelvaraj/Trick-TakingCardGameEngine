package src.ai;

import src.card.Card;

import java.util.LinkedList;
import java.util.List;

public class POMCPTreeNode {
    //How many times this node has been visited in the search
    private int visit;
    //The value of this node
    private int value;
    //The observation associated with the node.
    private GameObservation observation;
    //The action of this node, which in this case is a card played.
    private Card action;
    //The children nodes.
    private List<POMCPTree> children;

    public POMCPTreeNode(int visit, int value, GameObservation observation, Card action) {
        this.visit = visit;
        this.value = value;
        this.observation = observation;
        this.action = action;
        this.children = new LinkedList<>();
    }

    public POMCPTreeNode(GameObservation observation, Card action){
        this(0,0,observation, action);
    }

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
