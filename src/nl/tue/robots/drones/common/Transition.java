package nl.tue.robots.drones.common;

import nl.tue.robots.drones.gui.GUI;

import java.awt.geom.Line2D;

public class Transition {

    private static final int OUTSIDE_FACTOR = 20;
    private static final double OUTSIDE_MULTIPLIER = 2.0;
    private final Node from;
    private final Node to;
    private final boolean outside;
    private final int distance;
    private final boolean opposite;
    private Transition otherDirection;

    public static final int TEMP_TIMEOUT = GUI.TARGET_TICKS * 15;

    private int timeLocked = 0;
    private boolean permanentlyBlocked;

    public Transition(Node from, Node to, boolean outside, Transition otherDirection) {
        this.from = from;
        this.to = to;
        this.outside = outside;

        if (this.outside) {
            this.distance = (int) (OUTSIDE_FACTOR + OUTSIDE_MULTIPLIER * from.distanceTo(to));
        } else {
            this.distance = from.distanceTo(to);
        }

        this.otherDirection = otherDirection;
        otherDirection.otherDirection = this;
        opposite = true;
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
        opposite = false;
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
        timeLocked = state ? 0 : TEMP_TIMEOUT;
        permanentlyBlocked = !state && permanent;
    }

    public boolean getStatus() {
        return timeLocked <= 0;
    }

    public int getTimeLocked() {
        return timeLocked;
    }

    public Line2D toLine() {
        return new Line2D.Double(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public boolean isPermanent() {
        return permanentlyBlocked;
    }

    public void update() {
        if (!permanentlyBlocked && timeLocked > 0) {
            timeLocked--;
        }
        if (timeLocked <= 0) {
            getOpposite().timeLocked = 0;
        }
    }

    public boolean shouldRender() {
        return !opposite;
    }

    public boolean open() {
        return getStatus() && !permanentlyBlocked;
    }
}
