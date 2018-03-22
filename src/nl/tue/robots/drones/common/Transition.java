package nl.tue.robots.drones.common;

import java.awt.geom.Line2D;

public class Transition {

    private static final int OUTSIDE_FACTOR = 20;
    private static final double OUTSIDE_MULTIPLIER = 2.0;
    private final Node from;
    private final Node to;
    private final boolean outside;
    private final int distance;
    private Transition otherDirection;

    private boolean enabled = true;
    private boolean permanentlyBlocked;


    public Transition(Node from, Node to, boolean outside, Transition otherDirection) {
        this(from, to, outside);
        this.otherDirection = otherDirection;
        otherDirection.otherDirection = this;
    }

    public Transition(Node from, Node to, boolean outside) {
        this.from = from;
        this.to = to;
        this.outside = outside;

        if (this.outside) {
            this.distance = (int) (OUTSIDE_FACTOR + OUTSIDE_MULTIPLIER * from.distanceTo(to));
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

    public Transition getOpposite() {
        return otherDirection;
    }

    public void toggleTransition(boolean state, boolean permanent) {
        enabled = state;
        if (state) {
            permanentlyBlocked = false;
        } else {
            System.out.println("STATE: " + state + " perm?" + permanent);
            permanentlyBlocked = permanent;
            System.out.println("WOW PERM" + isPermanent());
        }
    }

    public boolean getStatus() {
        return enabled;
    }

    public Line2D toLine() {
        return new Line2D.Double(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public boolean isPermanent() {
        return permanentlyBlocked && (!enabled);
    }
}
