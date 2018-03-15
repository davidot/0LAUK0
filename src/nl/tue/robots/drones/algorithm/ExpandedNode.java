package nl.tue.robots.drones.algorithm;

import nl.tue.robots.drones.common.Node;

public class ExpandedNode implements Comparable<ExpandedNode> {

    private final Node node;
    private final int distanceTravelled;
    private final int heuristicDistance;
    private final ExpandedNode parent;

    public ExpandedNode(Node node, int distanceTravelled, int heuristicDistance, ExpandedNode parent) {
        this.node = node;
        this.distanceTravelled = distanceTravelled;
        this.heuristicDistance = heuristicDistance;
        this.parent = parent;
    }

    public Node getNode() {
        return node;
    }

    public ExpandedNode getParent() {
        return parent;
    }

    public int getDistanceTravelled() {
        return distanceTravelled;
    }

    public int getHeuristicDistance() {
        return heuristicDistance;
    }

    private int getTotalDistance() {
        return distanceTravelled + heuristicDistance;
    }

    @Override
    public int compareTo(ExpandedNode o) {
        if (distanceTravelled != o.distanceTravelled) {
            return distanceTravelled > o.distanceTravelled ? 1 : -1;
        } else {
            return Integer.compare(heuristicDistance, o.heuristicDistance);
        }
    }
}
