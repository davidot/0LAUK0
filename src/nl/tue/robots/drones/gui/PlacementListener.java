package nl.tue.robots.drones.gui;

import nl.tue.robots.drones.simulation.*;

import java.awt.*;
import java.awt.event.*;

public class PlacementListener extends MouseAdapter {

    int x;
    int y;
    int z;
    int[] startObject;
    boolean placingWall;
    boolean placingObstacle;
    Menu contextMenu;

    final Simulation sim;

    PlacementListener(Simulation s) {
        this.placingWall = false;
        this.placingObstacle = false;
        this.sim = s;

        // Context menu for placing things
        contextMenu = new Menu();
        MenuItem objectMenuItem = new MenuItem("Place Obstacle");
        MenuItem humanMenuItem = new MenuItem("Place Worker");
        MenuItem wallMenuItem = new MenuItem("Draw Wall");
        objectMenuItem.setActionCommand("obstacle");
        humanMenuItem.setActionCommand("human");
        wallMenuItem.setActionCommand("wall");
        ActionListener menuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("object")) {
                    startObject = new int[] {x,y,z};
                     placingObstacle = true;
                } else if (e.getActionCommand().equals("human")) {
                    sim.getBuilding().addObject(new RealHuman(x,y,z));
                } else if (e.getActionCommand().equals("wall")) {
                    startObject = new int[] {x,y,z};
                    placingWall = true;
                }
            }
        };
        objectMenuItem.addActionListener(menuListener);
        humanMenuItem.addActionListener(menuListener);
        wallMenuItem.addActionListener(menuListener);
        contextMenu.add(objectMenuItem);
        contextMenu.add(humanMenuItem);
        contextMenu.add(wallMenuItem);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);

        int[] coords = sim.screenToCoords(e.getX(), e.getY());
        x = coords[0];
        y = coords[1];
        z = coords[2];

        if (placingWall) {
            if (z == startObject[2]) {
                sim.getBuilding().addObject(new RealWall(z, startObject[0], startObject[1], x, y, false));
            }
        } else if (placingObstacle) {
            if (z == startObject[2]) {
                int diffX = Math.abs(x - startObject[0]);
                int diffY = Math.abs(y - startObject[1]);
                int obsX = (startObject[0] > x ? x + diffX : startObject[0] + diffX);
                int obsY = (startObject[1] > y ? y + diffY : startObject[1] + diffY);
                int obsSize = (diffX > diffY ? diffX / 2 : diffY / 2); // size of obstacle is 0.5 times smallest of differences
                sim.getBuilding().addObject(new RealObstacle(obsX, obsY, z, obsSize));
            }
        } else if (e.isPopupTrigger()) {

        }
    }

}
