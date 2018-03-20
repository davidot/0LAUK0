package nl.tue.robots.drones.model;

import nl.tue.robots.drones.algorithm.Algorithm;
import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.gui.GUI;
import nl.tue.robots.drones.simulation.RealDrone;
import nl.tue.robots.drones.simulation.Simulation;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class Drone {

    private final int id;
    private final BufferedImage[] images;

    //current/last node
    private Node currentNode;
    private Transition currentTransition;

    private Deque<Node> currentGoals = new ArrayDeque<>();


    public Drone(int id, Node start) {
        this.id = id;
        this.currentNode = start;
        images = RealDrone.getImageSequence(id, 100);
    }

    public boolean notBusy() {
        return currentGoals.isEmpty();
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void addGoals(List<Node> nodes) {
        currentGoals.addAll(nodes);
    }

    public void addEmergencyGoal(Node node){
        currentGoals.addFirst(node);
    }

    public Transition getCurrentTransition(){
        return currentTransition;
    }

    // TODO: update that currentTransition!
    public void updateCurrentTransition(Transition trans){
        this.currentTransition = trans;
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
        List<Node> list = Algorithm.findPath(currentNode, currentGoals.peekFirst()).stream()
                .map(Transition::getTo).collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        return list;
    }

    public int getId() {
        return id;
    }

    public void updateCurrent(Node node) {
        currentNode = node;
        currentTransition = null;
    }

    public void render(Graphics2D g, int floor) {
        if (!notBusy() && currentNode != null && currentNode.getZ() == floor) {
            int num = Simulation.getHalfSecond();
            int x = currentNode.getX() * GUI.MULTIPLIER;
            int y = currentNode.getY() * GUI.MULTIPLIER;
            int w = RealDrone.getImgWidth() / 2;
            int h = RealDrone.getImgHeight() / 2;
            g.drawImage(images[num], x - w, y - h, null);
        }
    }

}
