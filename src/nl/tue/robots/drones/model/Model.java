package nl.tue.robots.drones.model;

import nl.tue.robots.drones.algorithm.ExpandedNode;
import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;

import java.util.ArrayList;

public class Model {

    public static int getHeuristic(Node startNode, Node destinationNode) {}
        int xSquareDiff = Math.pow(startNode.getX() - destinationNode.getX(), 2);
        int ySquareDiff = Math.pow(startNode.getY() - destinationNode.getY(), 2);
        int zSquareDiff = Math.pow(startNode.getZ() - destinationNode.getZ(), 2);
        return (int) Math.sqrt(xSquareDiff + ySquareDiff + zSquareDiff);
    }

    public static ArrayList<Node> getOptions(ExpandedNode nodeToExpand) {  
        Node currentNode = nodeToExpand.getNode();
        return currentNode.getConnectedNodes();
    }

    public static Transition getTransition(Node parentNode, Node currentNode) {
        return currentNode.getTransition(parentNode);
    }

    public static int getTransitionDistance(Node parentNode, Node node) {
        Transition transition = currentNode.getTransition(parentNode);
        return transition.get;
    }
}
