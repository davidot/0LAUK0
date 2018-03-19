package nl.tue.robots.drones.gui;

import nl.tue.robots.drones.simulation.*;

import javax.swing.*;
import java.awt.event.*;

public class PlacementListener extends MouseAdapter {

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
        objectMenuItem.setLabel("Draw Obstacle");
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
        int[] coords = sim.screenToCoords(e.getX(), e.getY());
        x = coords[0];
        y = coords[1];
        z = coords[2];

        super.mouseClicked(e);

        if (placingWall) {
            if (z == startObject[2]) {
                sim.getBuilding().addObject(new RealWall(z, startObject[0], startObject[1], x, y, false));
                placingObstacle = false;
            }
        } else if (placingObstacle) {
            if (z == startObject[2]) {
                int diffX = Math.abs(x - startObject[0]);
                int diffY = Math.abs(y - startObject[1]);
                int obsX = (startObject[0] > x ? x + diffX : startObject[0] + diffX);
                int obsY = (startObject[1] > y ? y + diffY : startObject[1] + diffY);
                int obsSize = (diffX > diffY ? diffX / 2 : diffY / 2); // size of obstacle is 0.5 times smallest of differences
                sim.getBuilding().addObject(new RealObstacle(obsX, obsY, z, obsSize));
                placingWall = false;
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            System.out.println(e);
            contextMenu.show(gui, e.getX(), e.getY());
        }
    }

}
