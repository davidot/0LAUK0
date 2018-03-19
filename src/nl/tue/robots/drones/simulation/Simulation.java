package nl.tue.robots.drones.simulation;

import nl.tue.robots.drones.fileIO.GraphIO;
import nl.tue.robots.drones.fileIO.MalformedWallFileException;
import nl.tue.robots.drones.gui.GUI;
import nl.tue.robots.drones.model.Model;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;

public class Simulation {

    private static final int NUM_DRONES = 5;
    private RealBuilding building;
    private Model model;
    private static final int MULTIPLIER = GUI.MULTIPLIER;

    private static final int FLOORS = 3;
    private static final int FLOORS_OFFSET = 3;
    private static final int PADDING = 20;

    private int from = 0;

    public Simulation(File file) throws FileNotFoundException, MalformedWallFileException {
        //This is where a real application would open the file.
        model = new Model(GraphIO.readBuilding(file), NUM_DRONES);
        building = GraphIO.readWalls(new File(file.getParent(), file.getName().replace(".csv", ".walls")));
        for(int i = 0; i < 10; i++) {
            building.addObject(new RealHuman(10, 11 * i, 0));
        }
    }


    public void draw(Graphics2D g, int width, int height) {
        g.translate(0, PADDING);
        building.renderSideView(g);

        int floorWidth = width / 4;
        g.translate(floorWidth, 0);

        for (int floor = from; floor < from + FLOORS; floor++) {
            building.drawFloor(g, floor);
            g.translate(-MULTIPLIER, -MULTIPLIER);
            model.drawFloor(g, floor);
            g.translate(MULTIPLIER, MULTIPLIER);
            g.translate(floorWidth, 0);
        }
    }

    /**
     * Converts screen x & y values to building coordinates.
     * @param x the x value of the point on screen
     * @param y the y value of the point on screen
     * @return an array containing the coordinates in the order {x, y, z}
     */
    public int[] screenToCoords(int x, int y) {
        //TODO implement
        return new int[] {0,0,0};
    }

    public Dimension getSize() {
        return new Dimension((building.getWidth() * 4 + FLOORS * FLOORS_OFFSET) * MULTIPLIER, building.getDepth() * MULTIPLIER + PADDING * 2);
    }
}
