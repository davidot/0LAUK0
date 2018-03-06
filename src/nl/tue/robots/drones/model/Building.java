package nl.tue.robots.drones.model;

import nl.tue.robots.drones.common.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Building {

    //id to node
    private HashMap<Integer, Node> nodes = new HashMap<>();

    public List<Node> getAllNodes() {
        return new ArrayList<>(nodes.values());
    }




}
