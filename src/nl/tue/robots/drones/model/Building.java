package nl.tue.robots.drones.model;

import nl.tue.robots.drones.common.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Building {

    //id to node
    private HashMap<Integer, Node> nodes = new HashMap<>();

    public List<Node> getAllNodes() {
        return new ArrayList<>(nodes.values());
    }

    public Map<Integer, Node> getAllNodesWithId() {
        return Collections.unmodifiableMap(nodes);
    }

    /**
     * Retrieves the Node with the specified ID.
     * @param nodeID The node which to retrieve
     * @return The found node or {@code null} if none exists
     */
    public Node getNode(int nodeID) {
        return nodes.get(nodeID);
    }

    /**
     * Adds the new node with the specified ID to the building if no node with this ID exists yet.
     * @param n The ode to add
     * @param nodeID The ID of the new node
     * @return
     */
    public void addNode(Node n, int nodeID) {
        if (nodes.get(nodeID) != null) {
            throw new IllegalArgumentException("A node with this ID already exists");
        }

        nodes.put(nodeID, n);
    }


}
