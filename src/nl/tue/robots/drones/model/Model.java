package nl.tue.robots.drones.model;

import nl.tue.robots.drones.algorithm.ExpandedNode;
import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;

import java.util.ArrayList;

public class Model {
    public static int getHeuristic(Node startNode, Node destinationNode) {
        return -1;
    }

    public static ArrayList<Node> getOptions(ExpandedNode nodeToExpand) {
        return null;
    }

    public static Transition getTransition(Node parentNode, Node currentNode) {
        return null;
    }

    public static int getTransitionDistance(Node parentNode, Node node) {
        return 0;
    }
}
