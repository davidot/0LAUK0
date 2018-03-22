package nl.tue.robots.drones.simulation;

import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.fileIO.Images;
import nl.tue.robots.drones.gui.GUI;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @since 15 MAR 2018
 */


public class RealDrone extends RealObject {

    private static final int SPEED = 1;

    private static final Color[] colors = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.PINK, Color.MAGENTA, Color.CYAN, Color.ORANGE,
            new Color(150, 200, 55),};
    private static final int FLOOR_STEPS = 30;
    public static final int DRONE_DRAW_SIZE = 1;
    public static final int DOUBLE_DRAW_SIZE = 2 * DRONE_DRAW_SIZE;

    //where the drones are on the screen
    private int x;
    private int y;

    //Movement
    private int speedX = 0;
    private int speedY = 0;
    private LinkedList<Node> destinations;

    //Image and rendering
    private BufferedImage[] imageSequence;
    private static final String[] DEFAULT_IMAGE_SEQUENCE =
            {"drone_frame1.png", "drone_frame2.png"};

    private static final BufferedImage[] frames = new BufferedImage[DEFAULT_IMAGE_SEQUENCE.length];

    private static final int width;
    private static final int height;
    private RealObject lastObstacle;
    private int lastFloor;
    private int floorStep;

    public static int getImgWidth() {
        return width;
    }

    public static int getImgHeight() {
        return height;
    }

    static {
        int nWidth = 50;
        int nHeight = 50;
        try {

            //Open up all the frames and store them
            for(int i = 0; i < frames.length; i++) {
                frames[i] = ImageIO.read(new File("res/" + DEFAULT_IMAGE_SEQUENCE[i]));
                if(i == 0) {
                    nWidth = frames[i].getWidth();
                    nHeight = frames[i].getHeight();
                }
            }

        } catch(IOException e) {
            e.printStackTrace();

        }
        width = nWidth;
        height = nHeight;
    }

    private final int id;
    private Simulation simulation;
    private Node arrived;
    private Transition currentTransition;

    //constructor
    public RealDrone(Simulation simulation, int id, int floor, int x, int y) {
        this(simulation, id, floor, x, y, null);
    }

    //constructor with non-default image
    public RealDrone(Simulation simulation, int id, int floor, int x, int y,
                     BufferedImage[] imageSequence) {
        super(floor);
        lastFloor = floor;
        this.simulation = simulation;
        this.x = x;
        this.y = y;
        if(imageSequence != null) {
            this.imageSequence = imageSequence;
        } else {
            this.imageSequence = getImageSequence(id, -1);
        }
        this.id = id;
        destinations = new LinkedList<>();
    }

    /**
     * @param id the id of the color
     * @return An array consisting of the images.
     *
     * @pre {@code imgs != null && imgs.length > 0}
     * @post (\ forall i ; imgs.has ( i); imgSequence[i] = FileWithString(i))
     */
    public static BufferedImage[] getImageSequence(int id, int alpha) {
        BufferedImage[] imgSequence = new BufferedImage[frames.length];
        Color color = colors[id];
        for(int i = 0; i < frames.length; i++) {
            imgSequence[i] = Images.convertImageColor(frames[i], color, alpha);
        }
        return imgSequence;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addDestination(Node node) {
        // addDestination(node.getX(), node.getY(), node.getZ());
        Node lastDest = this.getFinalDestination();
        if(lastDest == null || lastDest == node) {

            if(!isHasDestination()) {
                setSpeed(SPEED, SPEED);
            }

            destinations.add(node);
        }
    }

    /**
     * Removes and returns the next destination from the list
     *
     * @return the next destination
     *
     * @throws NoSuchElementException if there is no next destination
     * @pre destinations.size() > 0
     */
    public Node removeNextDestination() {
        if(!isHasDestination()) {
            throw new NoSuchElementException(
                    "RealDrone.removeNextDestination.pre violated: No next destination");
        }
        Node firstDest = destinations.removeFirst();

        arrived = firstDest;

        if(isHasDestination()) {
            setSpeed(SPEED, SPEED);
        }
        return firstDest;
    }

    /**
     * @return Whether this drone has any destination
     */
    public boolean isHasDestination() {
        return destinations.size() > 0;
    }

    /**
     * Gets the next destination
     *
     * @return The next destination, or null if no destinations
     */
    public Node getNextDestination() {
        if(!isHasDestination()) {
            return null;
        }
        return destinations.getFirst();
    }

    /**
     * Gets the final destination
     *
     * @return The final destination, or null if no destinations
     */
    public Node getFinalDestination() {
        if(destinations.size() <= 0) {
            return null;
        }
        return destinations.getLast();
    }


    /*
    public int getDestinationX(){
        return destinationX;
    }


    public int getDestinationY(){
        return destinationY;
    }


    public void setDestination(int x, int y){
        this.destinationX = x;
        this.destinationY = y;
    }
    */


    public int getSpeedX() {
        return speedX;
    }


    public int getSpeedY() {
        return speedY;
    }


    public void setSpeed(int speedX, int speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
    }

    @Override
    public void drawObject(Graphics2D g) {

        //Draw the image centered around its XY-coordinates, rather than them
        //being at the topleft of the image
        Composite composite = g.getComposite();
        AlphaComposite ac = null;
        int sWidth = width;
        int sHeight = height;
        if (floorStep > 0) {
            int toZ = getNextDestination().getZ();
            boolean onLast = getFloor() == lastFloor;
            double scale;
            int step = floorStep / (FLOOR_STEPS / 8);

            float alpha = step * 25.0f / 256.0f;
            ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    onLast ? 1 - alpha : alpha);
            if(toZ >= lastFloor) {
                //going up
                scale = onLast ? 1.0 : 0.5 + step * 0.05;
            } else {
                //going down
                scale = onLast ? 1.0 : 1.4 - step * 0.05;
            }
            sWidth = (int) (sWidth * scale);
            sHeight = (int) (sHeight * scale);
        }

        int x = (this.x - 1) * GUI.MULTIPLIER - sWidth / DOUBLE_DRAW_SIZE;
        int y = (this.y - 1) * GUI.MULTIPLIER - sHeight / DOUBLE_DRAW_SIZE;
        if (ac != null) {
            g.setComposite(ac);
        }
        g.drawImage(imageSequence[Simulation.getHalfSecond()], x, y, sWidth / DRONE_DRAW_SIZE,
                sHeight / DRONE_DRAW_SIZE, null);
        g.setComposite(composite);
        update();
    }

    @Override
    public void drawSide(Graphics2D g) {
        g.drawImage(imageSequence[Simulation.getHalfSecond()], 0, 0, RealBuilding.DRAW_SIZE,
                RealBuilding.DRAW_SIZE, null);
    }

    private int c = 0;

    /**
     * Moves the drone on the screen based on its speed, destination, and
     * current location
     *
     * @pre true
     * @post distance(x, y, destinationX, destinationY) <=
     * \old(distance(x, y, destinationX, destinationY))
     */
    public void update() {
        if(c <= 0) {
            c = 1;
        } else {
            c--;
            return;
        }
        //TODO: Diagonal movement is faster now: with a speed of x you will...
        //..move x horizontaly AND vertically, and not x towards the direction of the goal
        //Solve this with trigonometry

        Node destination = getNextDestination();
        if(destination == null) {
            return;
        }

        if(arrived != null) {
            currentTransition = arrived.getTransition(destination);
            if(currentTransition != null) {
                if(simulation.sendToTransition(id, currentTransition)) {
                    arrived = null;
                } else {
                    // System.out.println("Cant move yet transistion blocked");
                    //don't move
                    return;
                }
            }
        }

        if(destination.getZ() != getFloor() || lastFloor != destination.getZ()) {

            if(destination.getX() != getX() || destination.getY() != getY()) {
                System.out.println("Should not happen");
            }
            floorStep += 1;
            if(floorStep > FLOOR_STEPS / 2) {
                setFloor(destination.getZ());
                // just in case the coords are not the same
                setXY(destination.getX(), destination.getY());
            }
            if(floorStep >= FLOOR_STEPS) {
                floorStep = 0;
                lastFloor = destination.getZ();
            }
            return;
        }


        int destinationX = destination.getX();
        int destinationY = destination.getY();


        // Check for nearby obstacles
        int range = 3;
        int lx = getX();
        int ly = getY();
        int rx = destinationX;
        int ry = destinationY;
        if(destinationX == getX()) {
            if(destinationY > getY()) {
                lx += range;
                rx -= range;
            } else {
                lx -= range;
                rx += range;
            }
        } else {
            if(destinationX > getX()) {
                ly += range;
                ry -= range;
            } else {
                ly -= range;
                ry += range;
            }
        }

        RealObject obstacle =
                getRealBuilding().obstaclesOnPath(x, y, lx, ly, rx, ry, getFloor(), range);
        if(obstacle != null && obstacle != lastObstacle) {
            // tell simulation that an obstacle is in the way for this drone
            lastObstacle = obstacle;
            simulation.sendObstacle(id, !(obstacle instanceof RealObstacle));
            return;
        }

        //Get the approaching direction (1 when moving along an axis; -1 otherwise)
        int dirX = x > destinationX ? -1 : 1;
        int dirY = y > destinationY ? -1 : 1;

        //Move
        x += dirX * speedX;
        y += dirY * speedY;

        //if we moved over the goal, move onto the goal instead
        if((dirX == 1 && x >= destinationX) || (dirX == -1 && x <= destinationX)) {
            speedX = 0;
            x = destinationX;
        }

        if((dirY == 1 && y >= destinationY) || (dirY == -1 && y <= destinationY)) {
            speedY = 0;
            y = destinationY;
        }

        if(x == destinationX && y == destinationY) {
            simulation.sendArrived(id, removeNextDestination());
        }
    }

    public int getId() {
        return id;
    }

    public void addDestinations(List<Node> next) {
        for(Node n : next) {
            addDestination(n);
        }
    }

    public Node clear(boolean everything) {
        Node first = null;
        if(!everything) {
            first = getNextDestination();
        }
        destinations.clear();
        arrived = null;
        if(first != null) {
            addDestination(first);
        }
        return first;
    }

    public boolean passes(Node... nodes) {
        for(Node n : nodes) {
            if(destinations.contains(n)) {
                return true;
            }
        }
        return false;
    }
}