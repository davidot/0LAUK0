package nl.tue.robots.drones.common;

public class Transition {

    private final Node from;
    private final Node to;
    private final int distance;

    private boolean enabled;


    public Transition(Node from, Node to) {
        this.from = from;
        this.to = to;

        this.distance = from.distanceTo(to);
    }

    public Node getTo() {
        return to;
    }

    public Node getFrom() {
        return from;
    }

    public int getDistance() {
        return distance;
    }

}
