package nl.tue.robots.drones.model;

import nl.tue.robots.drones.common.Node;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class Model {

    public static int getHeuristic(Node startNode, Node destinationNode) {
        int xDiff = startNode.getX() - destinationNode.getX();
        int yDiff = startNode.getY() - destinationNode.getY();
        int zDiff = startNode.getZ() - destinationNode.getZ();
        return (int) Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }

    private final List<Drone> drones;
    private final Building building;

    public Model(Building building, int numDrones) {
        this.building = building;
        drones = new ArrayList<Drone>(numDrones);
    }



    public void drawFloor(Graphics2D g) {

    }



}
