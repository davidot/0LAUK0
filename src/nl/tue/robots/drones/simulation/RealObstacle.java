package nl.tue.robots.drones.simulation;

import nl.tue.robots.drones.gui.GUI;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * @author sowdiyeah
 */
public class RealObstacle extends RealObject {
    protected int x, y;
    protected int size;

    public RealObstacle(int x, int y, int floor, int size) {
        super(floor);
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public void drawObject(Graphics2D g) {
        g.setColor(Color.RED);
        g.fillOval(GUI.MULTIPLIER * x - size / 2, GUI.MULTIPLIER * y - size / 2, size, size);
    }

    @Override
    public void drawSide(Graphics2D g) {
        g.setColor(Color.RED);
        g.fillOval(0, 0, RealBuilding.DRAW_SIZE, RealBuilding.DRAW_SIZE);
    }
}
