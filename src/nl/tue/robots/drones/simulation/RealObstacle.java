package nl.tue.robots.drones.simulation;

import nl.tue.robots.drones.gui.GUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author sowdiyeah
 */
public class RealObstacle extends RealObject {

    protected final Rectangle2D bounds;

    public RealObstacle(int floor, int x1, int y1, int x2, int y2) {
        super(floor);
        bounds = new Rectangle2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2),
                Math.abs(y1 - y2));
    }

    public int getX() {
        return (int) this.bounds.getCenterX();
    }

    public int getY() {
        return (int) this.bounds.getCenterY();
    }

    public int getXSize() {
        return (int) this.bounds.getWidth();
    }

    public int getYSize() {
        return (int) this.bounds.getHeight();
    }

    public Point2D getTopLeft() {
        return new Point2D.Double(bounds.getX(), bounds.getY());
    }

    public Point2D getTopRight() {
        return new Point2D.Double(bounds.getMaxX(), bounds.getY());
    }

    public Point2D getBottomLeft() {
        return new Point2D.Double(bounds.getX(), bounds.getMaxY());
    }

    public Point2D getBottomRight() {
        return new Point2D.Double(bounds.getMaxX(), bounds.getMaxY());
    }

    /**
     * Whether the specified point lies underneath the obstacle.
     *
     * @param p the point to check
     * @return {@code true} if the point is underneath the obstacle or on its edges
     */
    public boolean covers(Point2D p) {
        return (bounds.getX() <= p.getX() && p.getX() <= bounds.getMaxX())
                && (bounds.getY() <= p.getY() && p.getY() <= bounds.getMaxY());
    }

    @Override
    public void renderObject(Graphics2D g) {
        g.setColor(Color.RED.darker());
        g.fillRect((int) (GUI.MULTIPLIER * bounds.getX()), (int) (GUI.MULTIPLIER * bounds.getY()),
                (int) (GUI.MULTIPLIER * bounds.getWidth()),
                (int) (GUI.MULTIPLIER * bounds.getHeight()));
    }

    @Override
    public void renderSide(Graphics2D g) {
        g.setColor(Color.RED);
        g.fillRect(0, 0, RealBuilding.DRAW_SIZE, RealBuilding.DRAW_SIZE);
    }

    @Override
    public String toString() {
        return String.format("[Obstacle at (%d,%d) with size %d by %d]", getX(), getY(), getXSize(),
                getYSize());
    }

    /**
     * Moves the obstacle to the given (x,y) point on the same floor
     *
     * @param x
     * @param y
     */
    public void move(int x, int y) {
        int dx = x - getX();
        int dy = y - getY();
        bounds.setFrame(bounds.getX() + dx, bounds.getY() + dy, bounds.getWidth(),
                bounds.getHeight());
    }
}
