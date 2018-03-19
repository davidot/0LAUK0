package nl.tue.robots.drones.model;

import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.simulation.Simulation;

import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class Model {

    public static int getHeuristic(Node startNode, Node destinationNode) {
        int xDiff = startNode.getX() - destinationNode.getX();
        int yDiff = startNode.getY() - destinationNode.getY();
        int zDiff = startNode.getZ() - destinationNode.getZ();
        return (int) Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }


    private Deque<List<Node>> orders;
    private final List<Drone> drones;
    private final Simulation simulation;
    private final Building building;


    public Model(Simulation simulation, Building building,
                 int numDrones) {
        this.simulation = simulation;
        this.building = building;
        drones = new ArrayList<>(numDrones);
        orders = new ArrayDeque<>();
        Node start = building.getNode(0);
        for(int i = 0; i < numDrones; i++) {
            drones.add(new Drone(i, start));
        }
    }

    public Node getStartingNode() {
        return building.getNode(0);
    }

    public void drawFloor(Graphics2D g, int floor) {
        building.drawFloor(g, floor);
    }


    public void addOrder(List<Node> nodes) {
        if (nodes.size() < 1) {
            System.out.println("Empty order");
            return;
        }
        orders.add(nodes);
        update();
    }

    private void update() {
        if (!orders.isEmpty()) {
            Node start = orders.peekFirst().get(0);
            List<Drone> list = drones.stream().filter(Drone::busy).collect(Collectors.toList());
            if (!list.isEmpty()) {
                //drone available
                int minD = Integer.MAX_VALUE;
                Drone best = null;
                for(Drone d: list) {
                    int heuristic = getHeuristic(start, d.getCurrentNode());
                    if (heuristic < minD) {
                        minD = heuristic;
                        best = d;
                    }
                }
                if (best != null) {
                    System.out.println("Sending order to " + best.getId());
                    best.addGoals(orders.removeFirst());
                    nextDroneInstruction(best);
                }
            }
        }
    }

    private void nextDroneInstruction(Drone drone) {
        List<Node> next = drone.getNextNode();
        if (next != null) {
            simulation.droneInstruction(drone.getId(), next);
        }
    }

    public void droneBlocked(int id, boolean permanent) {
        //todo
        // System.out.println("Drone:" + id + " blocked permanent?" + permanent);
    }

    public void droneArrived(int id, Node node) {
        //todo
        System.out.println("Drone: " + id + "arrived at" + node);
        Drone drone = getDrone(id);
        drone.updateCurrent(node);

         nextDroneInstruction(drone);
        if (!drone.busy()) {
            update();
        }
    }

    private Drone getDrone(int id) {
        return drones.stream().filter(drone -> drone.getId() == id).findFirst().orElseThrow(() -> new IllegalStateException("Rut roh"));
    }

    public Node getNode(int id) {
        Node node = building.getNode(id);
        if (node == null) {
            throw new IllegalStateException("NOPE WRONG NODE YOU W+FCJ    " + id);
        }
        return node;
    }
}
