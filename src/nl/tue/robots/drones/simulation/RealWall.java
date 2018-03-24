package nl.tue.robots.drones.simulation;

import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.gui.GUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class RealWall extends RealObject {
    private static final boolean DRAW_WALL_COORDINATES = false;
    
    private static final int MULTI = GUI.MULTIPLIER;
    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;
    private final boolean outer;
    public List<Transition> undetected = new ArrayList<>();

    public RealWall(int floor, int x1, int y1, int x2, int y2, boolean outerWall) {
        super(floor);
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.outer = outerWall;
    }

    @Override
    public void drawObject(Graphics2D g) {
        //draw the walls
        g.setStroke(new BasicStroke(MULTI/2));
        g.setColor(undetected.isEmpty() ? Color.GRAY : Color.RED);
        g.drawLine(x1 * MULTI, y1 * MULTI, x2 * MULTI, y2 * MULTI);
        g.setStroke(new BasicStroke(1));
        
        if (DRAW_WALL_COORDINATES){
            g.setColor(Color.YELLOW);
            g.drawString("(" + x1 + "," + y1 + "," + x2 + "," + y2 + ")",
                        (x1+x2)/2*MULTI-38, (y1+y2)/2*MULTI+4);
        }
    }

    @Override
    public void drawSide(Graphics2D g) {
        //don't
    }

    @Override
    public boolean drawsSide() {
        return false;
    }

    /**
     * @return an int array containing the coordinates of the wall in the order [x1,y1,x2,y2]
     */
    public int[] getCoords() {
        return new int[]{x1, y1, x2, y2};
    }

    public boolean isOuterWall() {
        return outer;
    }

    public boolean hasUndetected() {
        return !undetected.isEmpty();
    }

    public Line2D toLine() {
        return new Line2D.Double(x1, y1, x2, y2);
    }

    public void addUndetected(Transition t) {
        undetected.add(t);
    }

    public boolean hasUndetected(Transition transition) {
        if (undetected.contains(transition)) {
            undetected.remove(transition);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("[Wall on floor %d from (%d,%d) to (%d,%d)]", getFloor(),x1,y1,x2,y2);
    }
}
