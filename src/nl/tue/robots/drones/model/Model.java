package nl.tue.robots.drones.model;

import nl.tue.robots.drones.algorithm.ExpandedNode;
import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;

import java.util.List;

public class Model {

    public static int getHeuristic(Node startNode, Node destinationNode) {
        int xDiff = startNode.getX() - destinationNode.getX();
        int yDiff = startNode.getY() - destinationNode.getY();
        int zDiff = startNode.getZ() - destinationNode.getZ();
        return (int) Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }

    public static List<Node> getOptions(ExpandedNode nodeToExpand) {
        Node currentNode = nodeToExpand.getNode();
        return currentNode.getConnectedNodes();
    }

    public static Transition getTransition(Node parentNode, Node currentNode) {
        return currentNode.getTransition(parentNode);
    }

    public static int getTransitionDistance(Node parentNode, Node node) {
        return node.getTransition(parentNode).getDistance();
    }
}
