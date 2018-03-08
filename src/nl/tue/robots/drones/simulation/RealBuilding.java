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

}
