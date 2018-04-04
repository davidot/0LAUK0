package nl.tue.robots.drones.gui;

import nl.tue.robots.drones.simulation.RealHuman;
import nl.tue.robots.drones.simulation.RealObstacle;
import nl.tue.robots.drones.simulation.RealWall;
import nl.tue.robots.drones.simulation.Simulation;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class MouseClickListener extends MouseAdapter {

    private static final String DRONE_ACTION = "drone";
    protected static final String DRONE_TEXT = "Send drone";
    private static final String WALL_ACTION = "wall";
    protected static final String WALL_TEXT = "Draw Wall";
    private static final String HUMAN_ACTION = "human";
    protected static final String HUMAN_TEXT = "Place Worker";
    private static final String OBSTACLE_ACTION = "obstacle";
    protected static final String OBSTACLE_TEXT = "Draw Obstacle";
    private static final String MOVEMENT_ACTION = "movement";
    protected static final String MOVEMENT_TEXT = "Move Worker";
    private static final String REMOVE_ACTION = "remove";
    protected static final String REMOVE_TEXT = "Remove";
    protected static final String REMOVE_TEXT_LONG = "Remove Obstacle/Worker";

    protected int guiX;
    protected int guiY;
    int x;
    int y;
    int z;
    protected int[] startObject;
    protected boolean placingWall;
    protected boolean placingObstacle;
    protected boolean placingFirst;
    private boolean placingHuman;
    private boolean pickingDestination;
    private boolean pickingRemoval;
    private boolean movingWorker;
    final JPopupMenu contextMenu;

    final GUI gui;

    MouseClickListener(GUI g) {
        this.placingWall = false;
        this.placingObstacle = false;
        this.placingFirst = false;
        this.placingHuman = false;
        this.pickingDestination = false;
        this.pickingRemoval = false;

        this.gui = g;

        // Context menu for placing things
        contextMenu = new JPopupMenu();
        JMenuItem droneMenuItem = new JMenuItem(DRONE_TEXT);
        JMenuItem humanMenuItem = new JMenuItem(HUMAN_TEXT);
        JMenuItem wallMenuItem = new JMenuItem(WALL_TEXT);
        JMenuItem objectMenuItem = new JMenuItem(OBSTACLE_TEXT);
        JMenuItem movementMenuItem = new JMenuItem(MOVEMENT_TEXT);
        JMenuItem removeMenuItem = new JMenuItem(REMOVE_TEXT_LONG);

        droneMenuItem.setActionCommand(DRONE_ACTION);
        humanMenuItem.setActionCommand(HUMAN_ACTION);
        wallMenuItem.setActionCommand(WALL_ACTION);
        objectMenuItem.setActionCommand(OBSTACLE_ACTION);
        movementMenuItem.setActionCommand(MOVEMENT_ACTION);
        removeMenuItem.setActionCommand(REMOVE_ACTION);

        ActionListener menuListener = e -> {
            switch(e.getActionCommand()) {
                case DRONE_ACTION:
                    sim().addOrder(x, y, z);
                    break;
                case HUMAN_ACTION:
                    sim().getBuilding().addObject(new RealHuman(x, y, z));
                    break;
                case WALL_ACTION:
                    startObject = new int[]{x, y, z};
                    placingWall = true;
                    panel().setStatus("Picking second wall point");
                    break;
                case OBSTACLE_ACTION:
                    startObject = new int[]{x, y, z};
                    placingObstacle = true;
                    panel().setStatus("Picking second obstacle point");
                    break;
                case MOVEMENT_ACTION:
                    if (sim().getBuilding().hasWorkerAt(x, y, z)) {
                        // there is a worker at the specified location
                        startObject = new int[]{x, y, z};
                        movingWorker = true;
                        panel().setStatus("Picking worker destination");
                    } else {
                        panel().setStatus("No worker here");
                    }
                    break;
                case REMOVE_ACTION:
                    sim().getBuilding().removeObstacle(x, y, z);
                    break;
            }
        };
        droneMenuItem.addActionListener(menuListener);
        humanMenuItem.addActionListener(menuListener);
        wallMenuItem.addActionListener(menuListener);
        objectMenuItem.addActionListener(menuListener);
        movementMenuItem.addActionListener(menuListener);
        removeMenuItem.addActionListener(menuListener);

        contextMenu.add(droneMenuItem);
        contextMenu.add(humanMenuItem);
        contextMenu.add(wallMenuItem);
        contextMenu.add(objectMenuItem);
        contextMenu.add(movementMenuItem);
        contextMenu.add(removeMenuItem);
    }

    protected void cancelAction() {
        this.placingWall = false;
        this.placingObstacle = false;
        this.placingFirst = false;
        this.placingHuman = false;
        this.pickingDestination = false;
        this.pickingRemoval = false;
        gui.menuPanel.deactivate();
    }

    private Simulation sim() {
        return gui.getSimulation();
    }

    private GUIMenuPanel panel() {
        return gui.getMenuPanel();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        guiToBuildingCoords(e.getX(), e.getY());
        guiX = e.getX();
        guiY = e.getY();
        String errorMessage = "";
        if (e.getButton() == MouseEvent.BUTTON3) {
            if (x >= 0 && y >= 0 && z >= 0) {
                contextMenu.show(gui, e.getX(), e.getY());
                cancelAction();
            }
            panel().deactivate();
        } else if (placingHuman) {
            sim().addNewObject(new RealHuman(x, y, z));
            placingHuman = false;
            resetCoords();
            panel().deactivate();
        } else if (pickingDestination) {
            sim().addOrder(x, y, z);
            pickingDestination = false;
            resetCoords();
            panel().deactivate();
        } else if (pickingRemoval) {
            sim().removeObject(x, y, z);
            pickingRemoval = false;
            resetCoords();
            panel().deactivate();
        } else if (placingFirst) {
            // we just got our first location on the press so store it as start and reset boolean
            startObject = new int[]{x, y, z};
            placingFirst = false;
            if (placingWall) {
                panel().setStatus("Picking second wall point");
            } else if (placingObstacle) {
                panel().setStatus("Picking second obstacle point");
            } else if (movingWorker) {
                if (sim().getBuilding().hasWorkerAt(x, y, z)) {
                    // there is a worker at the specified location
                    panel().setStatus("Picking worker destination");
                } else {
                    placingFirst = true;
                    resetCoords();
                    panel().setStatus("Pick a worker to move");
                }
            }
        } else if (placingWall) {
            // we already have our first point and are placing a wall
            guiToBuildingCoords(e.getX(), e.getY());
            if (z == startObject[2]) {
                sim().addNewObject(
                        new RealWall(z, startObject[0], startObject[1], x, y, false));
            }
            placingWall = false;
            resetCoords();
            panel().deactivate();
        } else if (placingObstacle) {
            // we already have our first point and are placing an obstacle
            guiToBuildingCoords(e.getX(), e.getY());
            if (z == startObject[2]) {
                //Empty areas are not allowed
                x = (x - startObject[0] == 0) ? x + 1 : x;
                y = (y - startObject[1] == 0) ? y + 1 : y;

                RealObstacle ob = new RealObstacle(z, startObject[0], startObject[1], x, y);
                sim().addNewObject(ob);
                System.out.println(
                        "Adding ob: (" + ob.getX() + "," + ob.getY() + ") of " + ob.getXSize() +
                                " by " + ob.getYSize());
            }
            placingObstacle = false;
            resetCoords();
            panel().deactivate();
        } else if (movingWorker) {
            // we are moving a worker and already have their location
            guiToBuildingCoords(e.getX(), e.getY());
            if (z == startObject[2]) {
                sim().moveWorker(z, new Point2D.Double(startObject[0], startObject[1]),
                        new Point2D.Double(x, y));
                System.out.printf("Telling worker on floor %d at (%d,%d) to move to (%d,%d)%n",
                        z, startObject[0], startObject[1], x, y);
            } else {
                System.out.println("Worker told to move between floors");
                errorMessage = "Workers cannot move between floors in simulation";

            }
            movingWorker = false;
            resetCoords();
            panel().deactivate();
            // panel status was cleared by deactivate, set our error (or clear again if none exists)
            panel().setStatus(errorMessage, Color.RED);
        }
    }

    /**
     * Converts the GUI coordinates to building coordinates and stores them in the x, y and z fields.
     *
     * @param xCoord the GUI x coordinate
     * @param yCoord the GUI y coordinate
     */
    private void guiToBuildingCoords(int xCoord, int yCoord) {
        // building coordinates of the click
        int[] coords = sim().screenToCoords(xCoord, yCoord);
        if (sim().isWithinBuilding(coords[0], coords[1], coords[2])) {
            // if coordinates are withing the building, store them
            x = coords[0];
            y = coords[1];
            z = coords[2];
        } else {
            x = -1;
            y = -1;
            z = -1;
        }
    }

    public void startWallPlacement() {
        placingFirst = true;
        placingWall = true;
    }

    public void startObstaclePlacement() {
        placingFirst = true;
        placingObstacle = true;
    }

    public void startHumanPlacement() {
        placingHuman = true;
    }

    public void startDestinationPick() {
        pickingDestination = true;
    }

    public void startRemoval() {
        pickingRemoval = true;
    }

    public void startWorkerMovement() {
        placingFirst = true;
        movingWorker = true;
    }

    private void resetCoords() {
        x = -1;
        y = -1;
        z = -1;
    }
}
