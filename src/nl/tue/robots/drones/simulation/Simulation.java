package nl.tue.robots.drones.simulation;

import nl.tue.robots.drones.fileIO.GraphIO;
import nl.tue.robots.drones.fileIO.MalformedWallFileException;
import nl.tue.robots.drones.model.Model;

import java.io.File;
import java.io.FileNotFoundException;

public class Simulation {

    private static final int NUM_DRONES = 5;
    private RealBuilding building;
    private Model model;

    public Simulation(File file) {
        //This is where a real application would open the file.
        try {
            model = new Model(GraphIO.readBuilding(file), NUM_DRONES);
            building = GraphIO.readWalls(new File(file.getParent(), file.getName().replace(".csv", ".walls")));
        } catch(FileNotFoundException | MalformedWallFileException e) {
            e.printStackTrace();
        }
    }

}
