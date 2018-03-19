package nl.tue.robots.drones.model;

import nl.tue.robots.drones.algorithm.Algorithm;
import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class Drone {

    private final int id;

    //current/last node
    private Node currentNode;
    private Transition currentTransistion;

    private Deque<Node> currentGoals = new ArrayDeque<>();


    public Drone(int id, Node start) {
        this.id = id;
        this.currentNode = start;
    }

    public boolean busy() {
        return currentGoals.isEmpty();
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void addGoals(List<Node> nodes) {
        currentGoals.addAll(nodes);
    }

    public List<Node> getNextNode() {
        if (currentGoals.isEmpty()) {
            return null;
        }
        if (currentNode == currentGoals.peekFirst()) {
            currentGoals.removeFirst();
            if (currentGoals.isEmpty()) {
                return null;
            }
        }
        return Algorithm.findPath(currentNode, currentGoals.peekFirst()).stream().map(Transition::getTo).collect(
                Collectors.toList());
    }

    public int getId() {
        return id;
    }

    public void updateCurrent(Node node) {
        currentNode = node;
    }
}
