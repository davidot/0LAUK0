package nl.tue.robots.drones.simulation;

import java.awt.Graphics2D;

/**
 * @since 15 MAR 2018
 */

public abstract class RealObject {

    private int floor;
    private RealBuilding realBuilding;

    public RealObject(int floor) {
        this.floor = floor;
    }

    public final void setRealBuilding(RealBuilding building) {
        this.realBuilding = building;
    }

    public final RealBuilding getRealBuilding() {
        return realBuilding;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public abstract void renderObject(Graphics2D g);

    public abstract void renderSide(Graphics2D g);

    public boolean onSide() {
        return true;
    }
}
