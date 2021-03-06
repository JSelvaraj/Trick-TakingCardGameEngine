package src.ai;

import java.util.LinkedList;
import java.util.List;

public class POMCPTreeNode {
    //How many times this node has been visited in the search
    private int visit;
    //The value of this node
    private double value;
    //The observation associated with the node.
    private GameObservation observation;
    //The children nodes.
    private List<POMCPTreeNode> children;

    public POMCPTreeNode(int visit, double value, GameObservation observation) {
        this.visit = visit;
        this.value = value;
        this.observation = observation;
        this.children = new LinkedList<>();
    }

    public POMCPTreeNode(GameObservation observation) {
        this(0, 0, observation);
    }

    public boolean addNode(GameObservation observation) {
        //Check that the observation is a subhistory of this.
        if (!observation.isPreviousHistory(this.observation)) {
            return false;
        }
        for (POMCPTreeNode child : this.children) {
            //Check if the node is a previous history of this observation.
            if (observation.isPreviousHistory(child.getObservation())) {
                return child.addNode(observation);
            }
        }
        //Add it instead from the root.
        this.children.add(new POMCPTreeNode(observation));
        return true;
    }

    /**
     * Find the node most closely associated with this observation sequence.
     *
     * @param observation The observation of the game.
     * @return A node that matches the card sequence the best.
     */
    public POMCPTreeNode findClosestNode(GameObservation observation) {
        if (!observation.isPreviousHistory(this.observation)) {
            return null;
        }
        for (POMCPTreeNode child : this.children) {
            if (observation.isPreviousHistory(child.getObservation())) {
                return child.findClosestNode(observation);
            }
        }
        return this;
    }

    public void setVisit(int visit) {
        this.visit = visit;
    }

    public void incrementVisit() {
        this.visit++;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void increaseValue(double value) {
        this.value += value;
    }

    public void setObservation(GameObservation observation) {
        this.observation = observation;
    }


    public int getVisit() {
        return visit;
    }

    public double getValue() {
        return value;
    }

    public GameObservation getObservation() {
        return observation;
    }


    public List<POMCPTreeNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "POMCPTreeNode{" +
                "visit=" + visit +
                ", value=" + value +
                ", observation=" + observation.toString() +
                ", children=" + children.size() +
                '}';
    }
}
