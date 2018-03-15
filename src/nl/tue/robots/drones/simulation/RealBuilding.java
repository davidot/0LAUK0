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

    public static final int MULTI = GUI.MULTIPLIER;
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

        int w = maxWidth * MULTI;
        int d = maxDepth * MULTI;


        AffineTransform transform = g.getTransform();

        for(int floor = from; floor < from + floors; floor++) {
            g.translate((floor % perColumn) * (w + 3 * MULTI), (floor / perColumn) * d);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, maxWidth * MULTI, maxDepth * MULTI);
            for (RealObject obj : getObjectsOnFloor(floor)) {
                obj.drawObject(g);
            }
            g.setTransform(transform);
        }

        g.setColor(Color.BLACK);

        for(int i = 1; i < rows; i++) {
            g.drawLine(0, d * (i + 1), w * perColumn, d * (i + 1));
        }

        for(int i = 0; i < perColumn; i++) {
            int x = ((w + (3 * MULTI)) * (i + 1)) - (int) (MULTI * 1.5);
            g.drawLine(x, 0, x, d * rows);
        }
    }

    public void renderSideView(Graphics2D g) {
        int total = (getDepth() * MULTI) / (getFloors() + 1);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        int quad = getWidth() * MULTI / 8;
        g.drawLine(quad, 0, quad, getDepth() * MULTI);
        int qqaud = 7 * quad;
        g.drawLine(qqaud, 0, qqaud, getDepth() * MULTI);
        for(int i = 0; i <= getFloors() + 1; i++) {
            g.drawLine(quad, i * total, qqaud, i * total);
        }
    }

    public void update() {

    }


}
