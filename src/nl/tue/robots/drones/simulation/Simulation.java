package nl.tue.robots.drones.simulation;

import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.fileIO.GraphIO;
import nl.tue.robots.drones.fileIO.MalformedWallFileException;
import nl.tue.robots.drones.gui.GUI;
import nl.tue.robots.drones.model.Model;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

public class Simulation {

    private static final int NUM_DRONES = 10;
    private RealBuilding building;
    private Model model;
    private static final int MULTIPLIER = GUI.MULTIPLIER;

    private static final int FLOORS = 3;
    private static final int FLOORS_OFFSET = 3;
    private static final int PADDING = 20;

    //Number of Floors to fit on a single row (NOTE: The sideview also counts as one!)
    //Should be 4 but can be another number during debugging
    private static final int NUM_FLOORS_PER_ROW = FLOORS + 1;


    private static int counter = 0;

    private int from = 0;
    private boolean drawModel = true;
    private boolean paused = false;

    public Simulation(File file) throws FileNotFoundException, MalformedWallFileException {
        //This is where a real application would open the file.
        model = new Model(this, GraphIO.readBuilding(file), NUM_DRONES);
        File wallsFile = new File(file.getParent(), file.getName().replace(".csv", ".walls"));
        building = GraphIO.readWalls(this, wallsFile);

        Node start = model.getStartingNode();

        for (int i = 0; i < NUM_DRONES; i++) {
            building.addObject(new RealDrone(this, i, start.getZ(), start.getX(), start.getY()));
        }

        for (int i = 0; i < 2; i++) {
            building.addObject(new RealHuman(10, 11 * i + 40, 0));
        }

        model.addOrder(Arrays.asList(start, model.getNode(144), start, model.getNode(332), start));
        model.addOrder(Arrays.asList(start, model.getNode(332), start));
        model.addOrder(Arrays.asList(start, model.getNode(8), start));
        model.addOrder(Arrays.asList(start, model.getNode(134), start));
        /*for(int i = 0; i < 100; i++) {
            int id;
            do {
                id = (int) (Math.random() * 300);
            } while (!model.hasNode(id));
            Node node = model.getNode(id);
            model.addOrder(Arrays.asList(start, node, start));
        }*/

    }

    public void draw(Graphics2D g, int width, int height) {
        g.translate(0, PADDING);
        building.renderSideView(g);

        int floorWidth = (building.getWidth() + FLOORS_OFFSET) * MULTIPLIER;
        g.translate(floorWidth, 0);

        int to = Math.min(from + FLOORS, building.getFloors() + 1);
        for (int floor = from; floor < to; floor++) {
            building.drawBackground(g, floor);
            if (drawModel) {
                model.drawFloor(g, floor);
            }
            building.drawForeground(g, floor);
            g.translate(floorWidth, 0);
        }

        counter++;
    }

    public void togglePause() {
        paused = !paused;
        System.out.println((paused ? "Pausing" : "Unpause"));
    }

    public void update() {
        if (!paused) {
            building.update();
            model.update();
        }
    }

    /**
     * Converts screen x & y values to building coordinates.
     *
     * @param x the x value of the point on screen
     * @param y the y value of the point on screen
     * @return an array containing the coordinates in the order {x, y, z}
     */
    public int[] screenToCoords(int x, int y) {
        int floor = (int) (x / ((building.getWidth() + FLOORS_OFFSET - 0.5) * MULTIPLIER) - 1);
        if (floor < 0 || floor >= FLOORS) {
            return new int[]{-1, -1, -1};
        }
        int xF = ((x + (MULTIPLIER / 2)) / MULTIPLIER) % (building.getWidth() + FLOORS_OFFSET);
        int yF = (y - PADDING) / MULTIPLIER;
        return new int[]{xF, yF, from + floor};
    }

    /**
     * Checks if given coordinates fall within the GUI building limits
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     * @return True if (x,y,z) is within the building
     */
    public boolean isWithinBuilding(int x, int y, int z) {
        return x >= 0 && x <= getBuilding().getWidth() &&
                y >= 0 && y <= getBuilding().getDepth() &&
                z >= 0 && z <= getBuilding().getFloors();
    }

    public RealBuilding getBuilding() {
        return building;
    }

    public Dimension getSize() {
        return new Dimension(
                (building.getWidth() * NUM_FLOORS_PER_ROW + (FLOORS + 1) * FLOORS_OFFSET) *
                        MULTIPLIER,
                building.getDepth() * MULTIPLIER + PADDING * 2);
    }

    public void sendObstacle(int id, boolean permanent) {
        if (permanent) {
            System.out.println("SENDING PERM TO MODEL");
        }
        model.droneBlocked(id, permanent);
    }

    public void droneInstruction(int id, List<Node> next) {
        RealDrone d = building.getDrone(id);
        d.addDestinations(next);
    }

    public void droneSetAlarm(int id, boolean enable) {
        RealDrone d = building.getDrone(id);
        d.setAlarm(enable);
    }

    public Node clearInstruction(int id, boolean everything) {
        RealDrone d = building.getDrone(id);
        return d.clear(everything);
    }

    public boolean travelsThrough(int id, Transition t) {
        RealDrone d = building.getDrone(id);
        return d.passes(t.getFrom(), t.getTo());
    }

    public void sendArrived(int id, Node node) {
        model.droneArrived(id, node);
    }

    public boolean sendToTransition(int id, Transition transition) {
        return model.droneTransition(id, transition);
    }

    public static int getCounter() {
        return counter;
    }

    public static int getHalfSecond() {
        return (counter % 60) / 30;
    }

    public void addOrder(int x, int y, int z) {
        Node node = model.toNode(x, y, z);
        if (node != null) {
            System.out.println("Sending to " + node);
            model.addOrderTo(node);
        } else {
            System.out.println("No node close enough");
        }
    }

    public void setDrawModel(boolean drawModel) {
        this.drawModel = drawModel;
    }

    public boolean getDrawModel() {
        return drawModel;
    }

    public void addNewWallObject(RealWall object) {
        model.getBuilding().getAllTransitions().stream()
                .filter(t -> t.getFrom().getZ() == object.getFloor() &&
                        object.toLine().intersectsLine(t.toLine())).forEach(
                object::addUndetected);
        System.out.println("INTERSECT" + object.undetected.size());
        building.addObject(object);
    }

    public void floorUp() {
        if (FLOORS + from <= (building.getFloors() + 1)) {
            from += FLOORS;
        } else {
            System.out.println("NO FLOORS" + from + "+" + FLOORS + ":" + building.getFloors());
        }
    }

    public void floorDown() {
        if (from > 0) {
            from -= FLOORS;
            from = Math.max(0, from);
        }
    }

}
