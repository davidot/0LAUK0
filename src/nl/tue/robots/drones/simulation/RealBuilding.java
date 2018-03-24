package nl.tue.robots.drones.simulation;

import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.gui.GUI;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RealBuilding {

    public static final int MULTI = GUI.MULTIPLIER;
    private final int floors;
    private final int maxWidth;
    private final int maxDepth;

    private final ArrayList<RealObject> objects = new ArrayList<>();
    private final Simulation simulation;

    public RealBuilding(Simulation simulation, int floors,
                        int maxWidth, int maxDepth) {
        this.simulation = simulation;
        this.floors = floors;
        this.maxWidth = maxWidth;
        this.maxDepth = maxDepth;
    }

    public List<RealObject> getObjectsOnFloor(int floor) {
        return objects.stream().filter(obj -> obj.getFloor() == floor).collect(Collectors.toList());
    }

    public List<RealWall> getAllWalls() {
        return objects.stream().filter(obj -> (obj instanceof RealWall)).map(obj -> (RealWall) obj)
                .collect(Collectors.toList());
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
        object.setRealBuilding(this);
        if (!getObjectsOnFloor(object.getFloor()).contains(object)) {
            object.setRealBuilding(this);
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

        for (int floor = from; floor < from + floors; floor++) {
            g.translate((floor % perColumn) * (w + 3 * MULTI), (floor / perColumn) * d);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, maxWidth * MULTI, maxDepth * MULTI);
            for (RealObject obj : getObjectsOnFloor(floor)) {
                obj.drawObject(g);
            }
            g.setTransform(transform);
        }

        g.setColor(Color.BLACK);

        for (int i = 1; i < rows; i++) {
            g.drawLine(0, d * (i + 1), w * perColumn, d * (i + 1));
        }

        for (int i = 0; i < perColumn; i++) {
            int x = ((w + (3 * MULTI)) * (i + 1)) - (int) (MULTI * 1.5);
            g.drawLine(x, 0, x, d * rows);
        }
    }

    public void drawBackground(Graphics2D g, int floor) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, maxWidth * MULTI, maxDepth * MULTI);

        for (RealObject obj : getObjectsOnFloor(floor)) {
            if (obj instanceof RealWall) {
                obj.drawObject(g);
            }
        }
    }

    public void drawForeground(Graphics2D g, int floor) {
        for (RealObject obj : getObjectsOnFloor(floor)) {
            if (!(obj instanceof RealWall)) {
                obj.drawObject(g);
            }
        }
    }

    private static final int BASE_OFFSET = 5;
    public static final int DRAW_SIZE = 25;
    private static final int DISTANCE_BETWEEN = BASE_OFFSET + DRAW_SIZE;


    public void renderSideView(Graphics2D g) {
        int total = (getDepth() * MULTI) / (getFloors() + 1);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        int quad = getWidth() * MULTI / 8;
        g.drawLine(quad, 0, quad, getDepth() * MULTI);
        int qqaud = 7 * quad;
        g.drawLine(qqaud, 0, qqaud, getDepth() * MULTI);
        for (int i = 0; i <= getFloors() + 1; i++) {
            g.setColor(Color.BLACK);
            g.drawLine(quad, i * total, qqaud, i * total);

            if (i > getFloors()) {
                break;
            }

            g.translate(quad + BASE_OFFSET, i * total + BASE_OFFSET);

            int floor = getFloors() - i;

            AffineTransform transform = g.getTransform();
            int y = 0;
            int x = BASE_OFFSET;
            for (RealObject obj : getObjectsOnFloor(floor)) {
                if (obj.drawsSide()) {
                    obj.drawSide(g);
                    g.translate(DISTANCE_BETWEEN, 0);
                    x += DISTANCE_BETWEEN;
                    if (x > qqaud - DISTANCE_BETWEEN * 2) {
                        g.setTransform(transform);
                        y += DISTANCE_BETWEEN;
                        g.translate(0, y);
                        x = BASE_OFFSET;
                    }
                }
            }

            g.setTransform(transform);
            g.translate(-quad - BASE_OFFSET, -i * total - BASE_OFFSET);
        }
    }

    /**
     * Finds walls and obstacles around the given location on the path to the destination.
     * @param location The current location
     * @param destination The destination
     * @param floor The floor number of the location & destination
     * @param range The (horizontal) range to search for objects
     * @return the nearest Wall or Obstacle or {@code null} if none detected
     */
    public RealObject pathObstructionInRange(Point2D location, Point2D destination, int floor, double range) {
        RealObject obstruction = null;

        Line2D path = new Line2D.Double(location, destination);

        // get walls in range crossing the path
        List<RealWall> blockingWalls = getAllWalls().stream().filter(
                w -> w.getFloor() == floor && (w.toLine().ptLineDist(location) <= range) && w.toLine().intersectsLine(path))
                .collect(Collectors.toList());

        // assign the closest (or only) wall or obstruction to the result
        if (blockingWalls.size() > 1) {
            // multiple walls, get closest one
            obstruction = blockingWalls.stream().sorted(
                    (o1, o2) -> (int) Math.signum(o1.toLine().ptLineDist(location) - o2.toLine().ptLineDist(location)))
                    .findFirst().orElse(null);
        } else if (blockingWalls.size() > 0) {
            // only one wall
            obstruction = blockingWalls.get(0);
        } else {
            // no walls in the path, so find obstacles

            // list all obstacles on floor
            List<RealObstacle> allObstacles = getObjectsOnFloor(floor).stream()
                    .filter(o -> o instanceof RealObstacle).map(o -> (RealObstacle) o).collect(Collectors.toList());
            // filter obstacles to only contain those in range and in path
            List<RealObstacle> relevantObstacles = allObstacles.stream()
                    .filter(obs -> {
                        // get all edges
                        Line2D topEdge = new Line2D.Double(obs.getTopLeft(), obs.getTopRight());
                        Line2D bottomEdge = new Line2D.Double(obs.getBottomLeft(), obs.getBottomRight());
                        Line2D leftEdge = new Line2D.Double(obs.getTopLeft(), obs.getBottomLeft());
                        Line2D rightEdge = new Line2D.Double(obs.getTopRight(), obs.getBottomRight());
                        // get both diagonals
                        Line2D diagonal1 = new Line2D.Double(obs.getTopRight(), obs.getBottomRight());
                        Line2D diagonal2 = new Line2D.Double(obs.getBottomLeft(), obs.getTopRight());

                        /* return (at least 1 edge in range) && (at least 1 diagonal crosses path)
                         * Works because if an edge is in range the obstacle is visible and to cross the obstacle means
                         * going over a diagonal. It also means crossing the edges but diagonals are used for this
                         * rather than edges since it saves 2 lineIntersect() calls.
                         */
                        return ((topEdge.ptLineDist(location) <= range) || (bottomEdge.ptLineDist(location) <= range) ||
                                    (leftEdge.ptLineDist(location) <= range) || (rightEdge.ptLineDist(location) <= range))
                                && (diagonal1.intersectsLine(path) || diagonal2.intersectsLine(path));
                    }).collect(Collectors.toList());

            if (relevantObstacles.size() > 0) {
                // if at least one obstacle
                obstruction = relevantObstacles.get(0);
            } // else nothing found in range in path, obstruction remains null
        }

        return obstruction;
    }

    public RealObject obstaclesOnPath(int x, int y, int lx, int ly, int rx, int ry, int floor,
                                      int range, Transition transition) {
        List<RealObstacle> obstacles = getObjectsOnFloor(floor).stream()
                .filter(obj -> obj.getFloor() == floor && obj instanceof RealObstacle)
                .map(obj -> (RealObstacle) obj).collect(Collectors.toList());
        for (RealObstacle obstacle : obstacles) {
            if ((((Math.pow(obstacle.getX() - x, 2) + Math.pow(obstacle.getY() - y, 2)) <
                    range * range))
                    || (lx <= obstacle.getX() && ly <= obstacle.getY()) &&
                    (obstacle.getX() <= rx && obstacle.getY() <= ry)) {
                return obstacle;
            }
        }
        // Line2D droneLine = new Line2D.Double(lx, ly, rx, ry);
        return getAllWalls().stream().filter(w -> w.getFloor() == floor && w.hasUndetected() &&
                w.hasUndetected(transition)).findFirst().orElse(null);
    }

    public RealDrone getDrone(int id) {
        return objects.stream().filter(d -> d instanceof RealDrone).map(RealDrone.class::cast)
                .filter(d -> d.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalStateException("WOWOWO"));
    }

    public void update() {
        objects.stream().filter(d -> d instanceof RealDrone).map(RealDrone.class::cast).forEach(RealDrone::update);
    }

    public void removeObstacle(int x, int y, int floor) {
        // Get all obstacles on this floor
        List<RealObstacle> localObstacles = getObjectsOnFloor(floor).stream().
                filter(obj -> (obj instanceof RealObstacle)).map(obj -> (RealObstacle) obj).collect(Collectors.toList());

        RealObstacle removable = null;
        // get all obstacles at x,y
        List<RealObstacle> candidates = localObstacles.stream().filter(obj -> obj.bounds.contains(x,y)).collect(Collectors.toList());

//        List<RealObstacle> candidates = localObstacles.stream().filter(obj -> (
//                (obj.getX() - obj.getXSize() / 2 <= x && obj.getX() + obj.getYSize() / 2 >= x) &&
//                    (obj.getY() - obj.getYSize() / 2 <= y && obj.getY() + obj.getYSize() / 2 >= y))).collect(Collectors.toList());

        // pick the first as option
        if (candidates.size() > 0) {
            removable = candidates.get(0);
        }

        if (removable != null) {
            // we found a object to remove
            objects.remove(removable);
        }
    }
}
