package nl.tue.robots.drones.simulation;

import java.awt.Graphics2D;

/**
 * 
 * @since 15 MAR 2018
 */

public abstract class RealObject {

    private int floor;

    public RealObject(int floor) {
        this.floor = floor;
    }

    public int getFloor() {
        return floor;
    }

    public abstract void drawObject(Graphics2D g);


}
