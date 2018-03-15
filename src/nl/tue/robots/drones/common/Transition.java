package nl.tue.robots.drones.common;

public class Transition {

    private static final int OUTSIDE_FACTOR = 20;
    private static final double OUTSIDE_MULTIPLIER = 100.0;
    private final Node from;
    private final Node to;
    private final boolean outside;
    private final int distance;

    private boolean enabled;


    public Transition(Node from, Node to, boolean outside) {
        this.from = from;
        this.to = to;
        this.outside = outside;

        if (this.outside) {
            this.distance = (int) (OUTSIDE_FACTOR + OUTSIDE_MULTIPLIER * from.distanceTo(to));
            System.out.println("Added " + (distance - from.distanceTo(to)));
        } else {
            this.distance = from.distanceTo(to);
        }
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

    public boolean isOutside() {
        return outside;
    }
}
