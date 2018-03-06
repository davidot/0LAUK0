package nl.tue.robots.drones.common;

public class Transition {

    private final Node from;
    private final Node to;


    public Transition(Node from, Node to) {
        this.from = from;
        this.to = to;
    }

    public Node getTo() {
        return to;
    }

    public Node getFrom() {
        return from;
    }
}
