package src.ai;

import src.card.Card;

public class POMCPTree {
    private POMCPTreeNode root;

    public POMCPTree(POMCPTreeNode root) {
        this.root = root;
    }

    public boolean addNode(GameObservation observation) {
        return root.addNode(observation);
    }
}
