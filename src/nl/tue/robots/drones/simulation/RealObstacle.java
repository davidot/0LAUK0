package nl.tue.robots.drones.simulation;

import nl.tue.robots.drones.gui.GUI;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * @author sowdiyeah
 */
public class RealObstacle extends RealObject {
    protected int x, y;
    protected int sizeX, sizeY;

    public RealObstacle(int x, int y, int floor, int sizeX, int sizeY) {
        super(floor);
        this.x = x;
        this.y = y;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
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
        g.fillRect(GUI.MULTIPLIER * (x - sizeX / 2), GUI.MULTIPLIER * (y - sizeY / 2), GUI.MULTIPLIER * sizeX, GUI.MULTIPLIER * sizeY);
    }

    @Override
    public void drawSide(Graphics2D g) {
        g.setColor(Color.RED);
        g.fillRect(0, 0, RealBuilding.DRAW_SIZE, RealBuilding.DRAW_SIZE);
    }
}
