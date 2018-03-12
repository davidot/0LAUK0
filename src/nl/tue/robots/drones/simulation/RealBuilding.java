package nl.tue.robots.drones.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RealBuilding {

    private final int floors;
    private final int width;
    private final int depth;

    private final ArrayList<RealObject> objects = new ArrayList<>();

    public RealBuilding(int floors, int width, int depth) {
        this.floors = floors;
        this.width = width;
        this.depth = depth;
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
        return width;
    }

    public int getDepth() {
        return depth;
    }
}
