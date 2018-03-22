package nl.tue.robots.drones.model;

import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.gui.GUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Building {

    private static final int MULTIPLIER = GUI.MULTIPLIER;
    private static final int NODE_R = 4;
    //id to node
    private HashMap<Integer, Node> nodes = new HashMap<>();
    private Map<Integer, Set<Node>> floorList = new HashMap<>();
    private List<Transition> transition = new ArrayList<>();

    public List<Node> getAllNodes() {
        return new ArrayList<>(nodes.values());
    }

    public Map<Integer, Node> getAllNodesWithId() {
        return Collections.unmodifiableMap(nodes);
    }

    /**
     * Retrieves the Node with the specified ID.
     *
     * @param nodeID The node which to retrieve
     * @return The found node or {@code null} if none exists
     */
    public Node getNode(int nodeID) {
        return nodes.get(nodeID);
    }

    /**
     * Adds the new node with the specified ID to the building if no node with this ID exists yet.
     *
     * @param n      The ode to add
     * @param nodeID The ID of the new node
     * @return
     */
    public void addNode(Node n, int nodeID) {
        if (nodes.get(nodeID) != null) {
            throw new IllegalArgumentException("A node with this ID already exists");
        }

        nodes.put(nodeID, n);
        floorList.computeIfAbsent(n.getZ(), k -> new HashSet<>());
        floorList.get(n.getZ()).add(n);
    }

    public Node getNearestNode(int x, int y, int z) {
        Node nearestNode = null;
        int minimalDx = 10;
        int minimalDy = 10;

        int d = 10000;

        for (Node node : nodes.values()) {
            if (node.getZ() == z) {
                int dx = (node.getX() - x) * (node.getX() - x);
                int dy = (node.getY() - y) * (node.getY() - y);

                if (dx < minimalDx && dy < minimalDy) {
                    if (dx + dy < d) {
                        d = dx + dy;
                        nearestNode = node;
                    }
                }
            }
        }

        return nearestNode;
    }

    private List<Transition> getTransitionsOnFloor(int floor) {
        return nodes.values().stream().filter(node -> node.getZ() == floor)
                .flatMap(node -> node.getTransitions().stream()).collect(Collectors.toList());
    }

    public void drawFloor(Graphics2D g, int floor) {
        g.setStroke(new BasicStroke(2));
        for (Transition t : getTransitionsOnFloor(floor)) {
            Node from = t.getFrom();
            Node to = t.getTo();
            if (from.getZ() != floor) {
                continue;
            }
            if (t.isPermanent()) {
                g.setColor(Color.BLUE);
            } else if (t.isOutside()) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.BLACK);
            }
            g.drawLine(from.getX() * MULTIPLIER, from.getY() * MULTIPLIER,
                    to.getX() * MULTIPLIER, to.getY() * MULTIPLIER);
        }

        g.setStroke(new BasicStroke(1));

        for (Node node : floorList.get(floor)) {
            g.setColor(Color.BLACK);
            g.fillOval(node.getX() * MULTIPLIER - NODE_R, node.getY() * MULTIPLIER - NODE_R,
                    NODE_R * 2, NODE_R * 2);
        }

    }

    public void addTransition(int from, int to, boolean outside) {
        Node a = getNode(from);
        Node b = getNode(to);

        // create transitions both ways
        Transition transAB = new Transition(a, b, outside);
        Transition transBA = new Transition(b, a, outside, transAB);

        // add transitions to nodes
        a.addTransition(transAB);
        b.addTransition(transBA);
        transition.add(transAB);
    }

    public List<Transition> getAllTransitions() {
        return transition;
    }

    /*
     else if (from.getZ() != to.getZ()) {
                g.setColor(Color.ORANGE);
                int x1 = from.getX() * MULTIPLIER + from.getZ() * floor;
                int x2 = to.getX() * MULTIPLIER + to.getZ() * floor;
                int y1 = from.getY() * MULTIPLIER;
                int y2 = to.getY() * MULTIPLIER;
                QuadCurve2D curve = new QuadCurve2D.Double(
                        x1, y1, (x1 + x2) / 2, (y1 + y2) / 2 - 75, x2, y2);
                g.draw(curve);
                continue;
            }
     */

}
