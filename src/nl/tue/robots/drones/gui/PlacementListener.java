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
import java.util.Arrays;

public class PlacementListener extends MouseAdapter {

    private static final String DRONE_ACTION = "drone";
    private static final String WALL_ACTION = "wall";
    private static final String HUMAN_ACTION = "human";
    private static final String OBSTACLE_ACTION = "obstacle";
    int x;
    int y;
    int z;
    int[] startObject;
    boolean placingWall;
    boolean placingObstacle;
    JPopupMenu contextMenu;

    final Simulation sim;
    final GUI gui;

    PlacementListener(Simulation s, GUI g) {
        this.placingWall = false;
        this.placingObstacle = false;
        this.sim = s;
        this.gui = g;

        // Context menu for placing things
        contextMenu = new JPopupMenu();
        JMenuItem objectMenuItem = new JMenuItem("Draw Obstacle");
        JMenuItem humanMenuItem = new JMenuItem("Place Worker");
        JMenuItem wallMenuItem = new JMenuItem("Draw Wall");
        JMenuItem droneMenuItem = new JMenuItem("Send drone");
        objectMenuItem.setActionCommand(OBSTACLE_ACTION);
        humanMenuItem.setActionCommand(HUMAN_ACTION);
        wallMenuItem.setActionCommand(WALL_ACTION);
        droneMenuItem.setActionCommand(DRONE_ACTION);
        ActionListener menuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getActionCommand().equals(OBSTACLE_ACTION)) {
                    startObject = new int[]{x, y, z};
                    placingObstacle = true;
                } else if(e.getActionCommand().equals(HUMAN_ACTION)) {
                    sim.getBuilding().addObject(new RealHuman(x, y, z));
                } else if(e.getActionCommand().equals(WALL_ACTION)) {
                    startObject = new int[]{x, y, z};
                    placingWall = true;
                } else if(e.getActionCommand().equals(DRONE_ACTION)) {
                    sim.addOrder(x, y, z);
                }
            }
        };
        objectMenuItem.addActionListener(menuListener);
        humanMenuItem.addActionListener(menuListener);
        wallMenuItem.addActionListener(menuListener);
        droneMenuItem.addActionListener(menuListener);
        contextMenu.add(objectMenuItem);
        contextMenu.add(humanMenuItem);
        contextMenu.add(wallMenuItem);
        contextMenu.add(droneMenuItem);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int[] coords = sim.screenToCoords(e.getX(), e.getY());
        x = coords[0];
        y = coords[1];
        z = coords[2];

        System.out
                .println("Made " + Arrays.toString(coords) + " from " + e.getX() + ", " + e.getY());

        // super.mouseClicked(e);

        if(placingWall) {
            if(z == startObject[2]) {
                RealWall object = new RealWall(z, startObject[0], startObject[1], x, y, false);
                object.setDetected(false);
                sim.getBuilding().addObject(
                        object);
                placingWall = false;
            }
        } else if(placingObstacle) {
            if(z == startObject[2]) {
                int diffX = Math.abs(x - startObject[0]);
                int diffY = Math.abs(y - startObject[1]);
                int obsX = (startObject[0] > x ? x + diffX : startObject[0] + diffX);
                int obsY = (startObject[1] > y ? y + diffY : startObject[1] + diffY);
                int obsSize = (diffX > diffY ? diffX / 2 :
                        diffY / 2); // size of obstacle is 0.5 times smallest of differences
                System.out.println("Addin ob: " + obsX + "," + obsY + "," + obsSize);
                sim.getBuilding().addObject(new RealObstacle(obsX, obsY, z, obsSize));
                placingObstacle = false;
            }
        } else if(e.getButton() == MouseEvent.BUTTON3) {
            System.out.println(e);
            contextMenu.show(gui, e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if(e.isPopupTrigger()) {
            contextMenu.show(gui, e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        if(e.isPopupTrigger()) {
            contextMenu.show(gui, e.getX(), e.getY());
        }
    }
}
