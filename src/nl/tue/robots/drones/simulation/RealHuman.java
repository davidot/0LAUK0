/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.robots.drones.simulation;

import nl.tue.robots.drones.gui.GUI;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * @author sowdiyeah
 */
public class RealHuman extends RealObstacle {

    private BufferedImage humanIcon;
    private LinkedList<Point2D> destinations;
    private LinkedList<Point2D> path;

    private static final int VISION_RANGE = 1;

    public RealHuman(int x, int y, int floor) {
        super(floor, x - 1, y - 1, x + 1, y + 1);
        try {
            humanIcon = ImageIO.read(new File("res/construction-worker.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        destinations = new LinkedList<>();
        path = new LinkedList<>();
    }

    @Override
    public void renderObject(Graphics2D g) {
        g.drawImage(humanIcon
                        .getScaledInstance(getXSize() * GUI.MULTIPLIER, getYSize() * GUI.MULTIPLIER,
                                BufferedImage.SCALE_SMOOTH), (GUI.MULTIPLIER * (getX() - 1)),
                (GUI.MULTIPLIER * (getY() - 1)), null);
    }

    @Override
    public void renderSide(Graphics2D g) {
        g.drawImage(humanIcon, 0, 0, RealBuilding.DRAW_SIZE, RealBuilding.DRAW_SIZE, null);
    }

    @Override
    public String toString() {
        return String.format("[Human at (%d,%d)]", getX(), getY());
    }

    /**
     * Tell the worker to move to a location.
     * @param destination the point the worker should move to
     */
    public void moveTo(Point2D destination) {
        path = calcPath(getX(),getY(), destination);
        if (path.size() == 0) {
            System.out.printf("Worker at (%d,%d) cannot reach destination (%d,%d)%n",
                    getX(), getY(), (int)destination.getX(), (int)destination.getY());
        } else {
            this.destinations.add(destination);
        }
    }

    private int c = 0;
    public void update() {
        if (c <= 0) {
            c = 2;
        } else {
            c--;
            return;
        }
        if (destinations.size() > 0 && path.size() > 0) {
            int x = getX();
            int y = getY();
            Point2D nextStep = path.poll();

            //Get the approaching direction
            int dirX = (int) nextStep.getX() - x;
            int dirY = (int) nextStep.getY() - y;//Integer.compare(destinationY, y);

            // Check for nearby obstacles in the direction we're moving to
            RealObject obstacle =
                    getRealBuilding().pathObstructionInRange(new Point2D.Double(x, y),
                            new Point2D.Double(x + (2 * dirX), y + (2 * dirY)), getFloor(), VISION_RANGE);

            if (obstacle != null && !(obstacle instanceof RealHuman)) {
                path = calcPath(x,y,destinations.peek()); // recalculate path
                if (path.size() == 0) {
                    System.out.printf("Worker at (%d,%d) cannot reach destination (%d,%d)%n",
                            getX(), getY(), (int)destinations.peek().getX(), (int)destinations.peek().getY());
                    destinations.removeFirst();
                }
                return;
            }

            //Move
            move(x + dirX, y + dirY);

        } else if (destinations.size() > 0){
            destinations.removeFirst();
            if (destinations.size() > 0) {
                path = calcPath(getX(),getY(), destinations.peek());
                if (path.size() == 0) {
                    System.out.printf("Worker at (%d,%d) cannot reach destination (%d,%d)%n",
                            getX(), getY(), (int)destinations.peek().getX(), (int)destinations.peek().getY());
                    destinations.removeFirst();
                }
            }
        }
    }

    /**
     * Performs a double breadth first search to arrive at a path over the grid for the human.
     * @param x the X coordinate of the current location
     * @param y the Y coordinate of the current location
     * @param destination the destination for this worker
     * @return a list of points to move to in order to reach the destination or an empty list if none exists
     */
    private LinkedList<Point2D> calcPath(int x, int y, Point2D destination) {
        LinkedList<Point2D> newPath = new LinkedList<>();
        if (!(x < 0 || x > getRealBuilding().getWidth() || y < 0 || y > getRealBuilding().getDepth() ||
                destination.getX() < 0 || destination.getX() > getRealBuilding().getWidth() ||
                destination.getY() < 0 || destination.getY() > getRealBuilding().getDepth())) {

            // both point are inside the building
            PriorityQueue<ExpandedGridPoint> queueA = new PriorityQueue<>();
            PriorityQueue<ExpandedGridPoint>  queueB = new PriorityQueue<>();
            HashMap<Point2D, Point2D> childParentMapA = new HashMap<>();
            HashMap<Point2D, Point2D> childParentMapB = new HashMap<>();

            // set up queues and maps with location and destination
            Point2D location = new Point2D.Double(x,y);
            queueA.add(new ExpandedGridPoint(location, 0, location.distance(destination)));
            queueB.add(new ExpandedGridPoint(destination, 0, destination.distance(location)));
            childParentMapA.put(location, null);
            childParentMapB.put(destination, null);

            // create 2 BFS trees until a common node is found or we find one queue empty (the start of that queue is isolated)
            Point2D commonPoint = null;
            while (commonPoint == null && (queueA.size() > 0 && queueB.size() > 0)) {
                ExpandedGridPoint A = queueA.poll();
                ExpandedGridPoint B = queueB.poll();

                // expand frontier A
                //noinspection Duplicates - surpress warning since loops are similar but cannot be a separate function
                for (Point2D n : getSurroundingGridPoints(A.gridPoint)) {
                    if (!childParentMapA.containsKey(n)) {
                        // unseen point
                        childParentMapA.put(n, A.gridPoint); // mark A as parent of n
                        RealObject obstacle = getRealBuilding().destinationObstructed(n, getFloor(), false);
                        if (obstacle == null || obstacle instanceof RealHuman) {
                            // the point is not obstructed so it can be in our path since we can move there
                            if (childParentMapB.containsKey(n)) {
                                // we found our shared node
                                commonPoint = n;
                            } else {
                                ExpandedGridPoint newPoint = new ExpandedGridPoint(
                                        n, A.travel + n.distance(A.gridPoint), n.distance(destination));
                                queueA.add(newPoint);
                            }
                        }
                    }
                }
                // expand frontier B
                //noinspection Duplicates - surpress warning since loops are similar but cannot be a separate function
                for (Point2D n : getSurroundingGridPoints(B.gridPoint)) {
                    if (!childParentMapB.containsKey(n)) {
                        // unseen point
                        childParentMapB.put(n, B.gridPoint); // mark B as parent of n
                        RealObject obstacle = getRealBuilding().destinationObstructed(n, getFloor(), false);
                        if (obstacle == null) {
                            // the point is not obstructed so it can be in our path since we can move there
                            if (childParentMapA.containsKey(n)) {
                                // we found our shared node
                                commonPoint = n;
                            } else {
                                ExpandedGridPoint newPoint = new ExpandedGridPoint(
                                        n, B.travel + n.distance(B.gridPoint), n.distance(location));
                                queueB.add(newPoint);
                            }
                        }
                    }
                }
            }

            if (commonPoint != null) {
                newPath = new LinkedList<>(); // we have a path so instantiate our variable

                // trace path both ways
                Point2D p = commonPoint;
                // trace from halfway point to start and add all nodes in between to the path
                while (p != null) {
                    newPath.addFirst(p);
                    p = childParentMapA.get(p);
                }
                // common point is already in path, start with its parent in the other direction
                p = childParentMapB.get(commonPoint);
                // trace from halfway point to end and add all nodes in between to the path
                while (p != null) {
                    newPath.add(p);
                    p = childParentMapB.get(p);
                }
                // path is constructed, return it
            }
        }

        return newPath;
    }

    /**
     * Finds the surrounding grid points that lie are part of the building.
     * @param p the point whose neighbours to find
     * @return a list of points which neighbour {@code p} or an empty list if {@code p} is not in building
     */
    private LinkedList<Point2D> getSurroundingGridPoints(Point2D p) {
        LinkedList<Point2D> neighbours = new LinkedList<>();

        if (!(p.getX() < 0 || p.getX() > getRealBuilding().getWidth() ||
                p.getY() < 0 || p.getY() > getRealBuilding().getDepth())) {
            // point is inside building

            int x = (int) p.getX();
            int y = (int) p.getY();

            // whether to find the neighbours at those directions
            boolean north = y > 0;
            boolean south = y < getRealBuilding().getDepth();
            boolean west = x > 0;
            boolean east = x < getRealBuilding().getWidth();

            if (north) {
                neighbours.add(new Point2D.Double(x, y - 1)); // north
                if (west) {
                    neighbours.add(new Point2D.Double(x - 1, y - 1)); //north west
                }
                if (east) {
                    neighbours.add(new Point2D.Double(x + 1, y - 1)); // north east
                }
            }
            if (south) {
                neighbours.add(new Point2D.Double(x, y + 1)); // south
                if (west) {
                    neighbours.add(new Point2D.Double(x - 1, y + 1)); // south west
                }
                if (east) {
                    neighbours.add(new Point2D.Double(x + 1, y + 1)); // south east
                }
            }
            if (west) {
                neighbours.add(new Point2D.Double(x - 1, y)); // west
            }
            if (east) {
                neighbours.add(new Point2D.Double(x + 1, y)); // east
            }
        }

        return neighbours;
    }

    public boolean isWalking() {
        return !destinations.isEmpty();
    }

    private class ExpandedGridPoint implements Comparable<ExpandedGridPoint> {
        final Point2D gridPoint;
        final double travel;
        final double heur;

        ExpandedGridPoint(Point2D p, double traveled, double heuristic) {
            this.gridPoint = p;
            this.travel = traveled;
            this.heur = heuristic;
        }


        @Override
        public int compareTo(ExpandedGridPoint o) {
            if (this.travel == o.travel) {
                return Double.compare(this.heur, o.heur);
            } else {
                return Double.compare(this.travel, o.travel);
            }
        }
    }
}
