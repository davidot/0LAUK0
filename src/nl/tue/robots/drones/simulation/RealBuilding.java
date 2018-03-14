package nl.tue.robots.drones.simulation;

import nl.tue.robots.drones.gui.GUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RealBuilding {

    private final int floors;
    private final int maxWidth;
    private final int maxDepth;

    private final ArrayList<RealObject> objects = new ArrayList<>();

    public RealBuilding(int floors, int maxWidth, int maxDepth) {
        this.floors = floors;
        this.maxWidth = maxWidth;
        this.maxDepth = maxDepth;
    }

    public List<RealObject> getObjectsOnFloor(int floor) {
        return objects.stream().filter(obj -> obj.getFloor() == floor).collect(Collectors.toList());
    }

    public List<RealWall> getAllWalls() {
        return objects.stream().filter(obj -> (obj instanceof RealWall)).map(obj -> (RealWall)obj).collect(Collectors.toList());
    }

    public void addWalls(List<RealWall> walls) {
        walls.removeAll(objects); // prevent adding duplicate walls
        objects.addAll(walls);
    }

    public int getFloors() {
        return floors;
    }

    public int getWidth() {
        return maxWidth;
    }

    public int getDepth() {
        return maxDepth;
    }

    public void addObject(RealObject object) {
        if (!getObjectsOnFloor(object.getFloor()).contains(object)) {
            objects.add(object);
        }
    }


    public void render(Graphics2D g, int floors, int from, int perColumn) {
        if (floors % perColumn != 0) {
            return;
        }
        int rows = floors / perColumn;

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));

        int w = maxWidth * GUI.MULTIPLIER;
        int d = maxDepth * GUI.MULTIPLIER;

        for(int i = 0; i < rows; i++) {
            g.drawLine(0, d * (i + 1), w * perColumn, d * (i + 1));
        }

        for(int i = 0; i < perColumn; i++) {
            g.drawLine(w * (i + 1), 0,  w * (i + 1), d * rows);
        }

        AffineTransform transform = g.getTransform();

        for(int floor = from; floor < from + floors; floor++) {
            g.translate((floor % perColumn) * w, (floor / perColumn) * d);
            g.scale(GUI.MULTIPLIER, GUI.MULTIPLIER);
            for (RealObject obj : getObjectsOnFloor(floor)) {
                obj.drawObject(g);
            }
            g.setTransform(transform);
        }

    }

    public void update() {

    }


}
