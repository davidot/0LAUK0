package nl.tue.robots.drones.model;

import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;

import java.util.ArrayDeque;
import java.util.Deque;

public class Drone {

    private final int id;

    private Node currentNode;
    private Transition currentTransistion;

    private Node currentGoal;

    private Deque<Transition> steps = new ArrayDeque<>();


    public Drone(int id, Node start) {
        this.id = id;
        this.currentNode = start;
    }
}
