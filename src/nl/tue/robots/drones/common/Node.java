package nl.tue.robots.drones.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Node {

    private final int x;
    private final int y;
    private final int z;
    private boolean up = false;
    private boolean down = false;

    private final ArrayList<Transition> transitions = new ArrayList<>();

    public Node(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void addTransition(Transition transition) {
        transitions.add(transition);
        if (transition.getTo().getZ() > z) {
            up = true;
        }
        if (transition.getTo().getZ() < z) {
            down = true;
        }
    }
    
    public boolean getUp() {
        return this.up;
    }
    
    public boolean getDown() {
        return this.down;
    }

    public List<Node> getConnectedNodes() {
        return transitions.stream().filter(Transition::getStatus).map(Transition::getTo)
                .collect(Collectors.toList());
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public Transition getTransition(Node to) {
        return transitions.stream().filter(t -> t.getTo() == to).findFirst().orElse(null);
    }

    public int distanceTo(Node to) {
        int dx = x - to.x;
        int dy = y - to.y;
        int dz = z - to.z;
        return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public List<Transition> getTransitions() {
        return Collections.unmodifiableList(transitions);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
