package nl.tue.robots.drones.simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class RealWall extends RealObject {

    private static final int WALL_SIZE = 6;
    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;

    public RealWall(int floor, int x1, int y1, int x2, int y2) {
        super(floor);
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public void drawObject(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(WALL_SIZE));
        g.drawLine(x1, y1, x2, y2);
    }
}
