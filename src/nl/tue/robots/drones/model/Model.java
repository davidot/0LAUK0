package nl.tue.robots.drones.model;

import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.simulation.Simulation;

import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class Model {

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
        for (int i = 0; i < numDrones; i++) {
            drones.add(new Drone(i, start));
        }
    }

    public Node getStartingNode() {
        return building.getNode(0);
    }

    public void drawFloor(Graphics2D g, int floor) {
        building.drawFloor(g, floor);

        drones.forEach(d -> d.render(g, floor));

    }

    public Node toNode(int x, int y, int z) {
        return building.getNearestNode(x, y, z);
    }

    public void addOrder(List<Node> nodes) {
        if (nodes.size() < 1) {
            System.out.println("Empty order");
            return;
        }
        orders.add(nodes);
        updateOrder();
    }

    public static int getHeuristic(Node startNode, Node destinationNode) {
        int xDiff = startNode.getX() - destinationNode.getX();
        int yDiff = startNode.getY() - destinationNode.getY();
        int zDiff = startNode.getZ() - destinationNode.getZ();
        return (int) Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }

    private void updateOrder() {
        if (!orders.isEmpty()) {
            Node start = orders.peekFirst().get(0);
            List<Drone> list = drones.stream().filter(Drone::notBusy).collect(Collectors.toList());
            if (!list.isEmpty()) {
                //drone available
                int minD = Integer.MAX_VALUE;
                Drone best = null;
                for (Drone d : list) {
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
        } else if (!drone.notBusy()) {
            //If a path could not be found but there was a task allocated,
            //enable the alarm
            simulation.droneSetAlarm(drone.getId(), true);
        }
    }

    public void droneBlocked(int blockedId, boolean permanent) {
        System.out.println("Drone:" + blockedId + " blocked permanent?" + permanent);
        Drone blockedDrone = getDrone(blockedId);

        // Find the transitions the drone is currently on and block it
        Transition currentTrans = blockedDrone.getCurrentTransition();
        if (currentTrans != null) {
            currentTrans.toggleTransition(false, permanent);
            Transition opposite = currentTrans.getOpposite();
            opposite.toggleTransition(false, permanent);
        }

        //drone is now moving back and is not really on the transition

        // Send drone back to its previous node
        simulation.clearInstruction(blockedId, true);
        if (currentTrans != null) {
            simulation.droneInstruction(blockedId,
                    Collections.singletonList(blockedDrone.getCurrentNode()));
            nextDroneInstruction(blockedDrone);
        } else {
            //todo add something in case we are stuck nowhere
        }

        updateRelatedPaths(blockedId, currentTrans, true);
        blockedDrone.updateCurrentTransition(null);
    }

    private void updateRelatedPaths(int ignoreId, Transition trans, boolean all) {
        if (trans == null) {
            return;
        }
        Transition op = trans.getOpposite();
        for (int id = 0; id < drones.size(); id++) {
            if (id != ignoreId) {
                if (simulation.travelsThrough(id, trans) || simulation.travelsThrough(id, op)) {
                    System.out.println("Clearing instructions of " + id);
                    simulation.clearInstruction(id, all);
                    Drone drone = getDrone(id);
                    simulation
                            .droneInstruction(drone.getId(),
                                    Collections.singletonList(drone.getCurrentNode()));
                    // drone.updateCurrent(from);
                    nextDroneInstruction(drone);
                }
            }
        }
    }

    public void droneArrived(int id, Node node) {
        // System.out.println("Drone: " + id + "arrived at" + node);
        Drone drone = getDrone(id);
        drone.updateCurrent(node);

        nextDroneInstruction(drone);
        if (drone.notBusy()) {
            updateOrder();
        }
    }

    private Drone getDrone(int id) {
        return drones.stream().filter(drone -> drone.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalStateException("Rut roh"));
    }

    public Node getNode(int id) {
        Node node = building.getNode(id);
        if (node == null) {
            throw new IllegalStateException("NOPE WRONG NODE " + id);
        }
        return node;
    }

    public boolean droneTransition(int id, Transition transition) {
        if (transitionLocked(transition) || !transition.open()) {
            return false;
        }
        getDrone(id).updateCurrentTransition(transition);
        return true;
    }

    private boolean transitionLocked(Transition transition) {
        final Transition opposite = transition.getOpposite();
        return drones.stream().map(Drone::getCurrentTransition)
                .anyMatch(t -> t == transition || t == opposite);
    }

    public void addOrderTo(Node node) {
        addOrder(Arrays.asList(getStartingNode(), node, getStartingNode()));
    }

    public Building getBuilding() {
        return building;
    }

    public void update() {
        boolean update = false;
        for (Transition t : building.update()) {
            update = true;
            //ignore nothing
            updateRelatedPaths(-1, t, false);
        }
        if (update) {
            drones.stream().filter(Drone::isStuck).forEach(this::nextDroneInstruction);
        }
    }
}
