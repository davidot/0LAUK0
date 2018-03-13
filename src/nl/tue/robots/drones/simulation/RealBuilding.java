package nl.tue.robots.drones.simulation;

import java.awt.Graphics2D;
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

    public void addObject(RealObject object) {
        if (!getObjectsOnFloor(object.getFloor()).contains(object)) {
            objects.add(object);
        }
    }


    public void render(Graphics2D g, int floors, int from, int perColumn) {
        if (floors % perColumn != 0) {
            System.out.println("NOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOSDfasdhfkasdhfklasdhfkljasdhf");
            return;
        }
        int rows = floors / perColumn;

        for(int i = 0; i < rows; i++) {
            g.drawLine(0, maxDepth * (i + 1), maxWidth * perColumn, maxDepth * (i + 1));
        }

        for(int i = 0; i < perColumn; i++) {
            g.drawLine(maxWidth * (i + 1), 0,  maxWidth * (i + 1), maxDepth * rows);
        }

        for(int floor = from; floor < from + floors; floor++) {
            //todo draw the full floor
        }

    }

    public void update() {

    }


}
