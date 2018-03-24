package nl.tue.robots.drones.gui;

import nl.tue.robots.drones.simulation.RealHuman;
import nl.tue.robots.drones.simulation.RealObstacle;
import nl.tue.robots.drones.simulation.RealWall;
import nl.tue.robots.drones.simulation.Simulation;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseClickListener extends MouseAdapter {

    private static final String DRONE_ACTION = "drone";
    private static final String WALL_ACTION = "wall";
    private static final String HUMAN_ACTION = "human";
    private static final String OBSTACLE_ACTION = "obstacle";
    private static final String REMOVE_ACTION = "remove";

    protected int guiX;
    protected int guiY;
    int x;
    int y;
    int z;
    protected int[] startObject;
    protected boolean placingWall;
    protected boolean placingObstacle;
    JPopupMenu contextMenu;

    final GUI gui;

    MouseClickListener(GUI g) {
        this.placingWall = false;
        this.placingObstacle = false;
        this.gui = g;

        // Context menu for placing things
        contextMenu = new JPopupMenu();
        JMenuItem objectMenuItem = new JMenuItem("Draw Obstacle");
        JMenuItem humanMenuItem = new JMenuItem("Place Worker");
        JMenuItem wallMenuItem = new JMenuItem("Draw Wall");
        JMenuItem droneMenuItem = new JMenuItem("Send drone");
        JMenuItem removeMenuItem = new JMenuItem("Remove Obstacle/Worker");

        objectMenuItem.setActionCommand(OBSTACLE_ACTION);
        humanMenuItem.setActionCommand(HUMAN_ACTION);
        wallMenuItem.setActionCommand(WALL_ACTION);
        droneMenuItem.setActionCommand(DRONE_ACTION);
        removeMenuItem.setActionCommand(REMOVE_ACTION);

        ActionListener menuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals(OBSTACLE_ACTION)) {
                    startObject = new int[]{x, y, z};
                    placingObstacle = true;
                } else if (e.getActionCommand().equals(HUMAN_ACTION)) {
                    sim().getBuilding().addObject(new RealHuman(x, y, z));
                } else if (e.getActionCommand().equals(WALL_ACTION)) {
                    startObject = new int[]{x, y, z};
                    placingWall = true;
                } else if (e.getActionCommand().equals(DRONE_ACTION)) {
                    sim().addOrder(x, y, z);
                } else if (e.getActionCommand().equals(REMOVE_ACTION)) {
                    sim().getBuilding().removeObstacle(x,y,z);
                }
            }
        };
        objectMenuItem.addActionListener(menuListener);
        humanMenuItem.addActionListener(menuListener);
        wallMenuItem.addActionListener(menuListener);
        droneMenuItem.addActionListener(menuListener);
        removeMenuItem.addActionListener(menuListener);

        contextMenu.add(objectMenuItem);
        contextMenu.add(humanMenuItem);
        contextMenu.add(wallMenuItem);
        contextMenu.add(droneMenuItem);
        contextMenu.add(removeMenuItem);
    }
    
    protected void cancelObjectPlacement(){
        placingWall = false;
        placingObstacle = false;
    }

    private Simulation sim() {
        return gui.getSimulation();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        guiToBuildingCoords(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {   
        guiX = e.getX();
        guiY = e.getY();
        if (e.getButton() == MouseEvent.BUTTON3) {
            if (x >= 0 && y >= 0 && z >= 0) {
                contextMenu.show(gui, e.getX(), e.getY());
                cancelObjectPlacement();
            }
        } else if (placingWall) {
            guiToBuildingCoords(e.getX(),e.getY());
            if (z == startObject[2]) {
                sim().addNewWallObject(new RealWall(z, startObject[0], startObject[1], x, y, false));
            }
            placingWall = false;
        } else if (placingObstacle) {
            guiToBuildingCoords(e.getX(),e.getY());
            if (z == startObject[2]) {
                int diffX = Math.abs(x - startObject[0]);
                int diffY = Math.abs(y - startObject[1]);
                int obsX = (startObject[0] > x ? x + diffX / 2 : startObject[0] + diffX / 2);
                int obsY = (startObject[1] > y ? y + diffY / 2 : startObject[1] + diffY / 2);
                System.out.println("Adding ob: (" + obsX + "," + obsY + ") of " + diffX + " by " + diffY);
                sim().getBuilding().addObject(new RealObstacle(obsX, obsY, z, diffX, diffY));
            }
            placingObstacle = false;
        }
    }

    /**
     * Converts the GUI coordinates to building coordinates and stores them in the x, y and z fields.
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
}
