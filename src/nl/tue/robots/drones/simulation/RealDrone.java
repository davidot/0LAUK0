package nl.tue.robots.drones.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.awt.image.*;
import java.io.*;
import java.util.LinkedList;
import javax.imageio.*;

import java.util.List;
import java.util.NoSuchElementException;
import nl.tue.robots.drones.common.Destination;

/**
 * 
 * @since 15 MAR 2018
 */


public class RealDrone extends RealObject {

    private static int colorId = 0;
    private static final Color[] colors = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
            Color.DARK_GRAY, Color.GRAY, Color.PINK, Color.YELLOW, Color.MAGENTA, Color.CYAN,
            new Color(63, 68, 143)};

    //where the drones are on the screen
    private int x;
    private int y;

    //Movement
    private int speedX = 0;
    private int speedY = 0;
    private LinkedList<Destination> destinations;

    //Image and rendering
    private BufferedImage[] imageSequence;
    private int frame = 0;
    private int counter = 0;
    private static final int COUNTER_FRAME_SWITCH = 10;
    private static final String[] DEFAULT_IMAGE_SEQUENCE =
                        {"drone_frame1.png", "drone_frame2.png"};

    //constructor
    public RealDrone(int floor, int x, int y) {
        this(floor, x, y, null);
    }

    //constructor with non-default image
    public RealDrone(int floor, int x, int y, BufferedImage[] imageSequence) {
        super(floor);
        this.x = x;
        this.y = y;
        if (imageSequence != null){
            this.imageSequence = imageSequence;
        }else{
            this.imageSequence = getImageSequence(DEFAULT_IMAGE_SEQUENCE);
        }
        
        destinations = new LinkedList<>();
    }

    /**
     *
     * @param imgs An array of strings of the filenames containing each frame of
     *             the animation. Must be ordered by ascending frame
     * @pre {@code imgs != null && imgs.length > 0}
     * @post (\forall i; imgs.has(i); imgSequence[i] = FileWithString(i))
     *
     * @return An array consisting of the images.
     */
    public static BufferedImage[] getImageSequence(String[] imgs){
        BufferedImage[] imgSequence = new BufferedImage[imgs.length];
        try {
            /*
            //Prints all files and folders in the current directory
            File file = new File(".");
            for(String fileNames : file.list()) System.out.println(fileNames);
            */

            //Open up all the frames and store them
            for (int i = 0; i < imgs.length; i++){
                BufferedImage img = ImageIO.read(new File("res/" + imgs[i]));
                imgSequence[i] = img;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgSequence;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public void setXY(int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     * Adds a destination to the list of destinations to fly to, if that
     * destination is not already the last one in the list
     * @param x
     * @param y
     * @param z 
     */
    public void addDestination(int x, int y, int z){
        Destination lastDest = this.getFinalDestination();
        Destination newDest = new Destination(x, y, z);
        if (lastDest == null || !Destination.destinationsEqual(lastDest, newDest)){ 
            System.out.println("new dest added");
            
            if (!isHasDestination()){
                setSpeed(4, 4);
            }
            
            destinations.add(newDest);
        }
    }
    
    /**
     * Removes and returns the next destination from the list
     * @pre destinations.size() > 0
     * 
     * @return the next destination
     * 
     * @throws NoSuchElementException if there is no next destination
     */
    public Destination removeNextDestination(){
        if (!isHasDestination()){
            throw new NoSuchElementException("RealDrone.removeNextDestination.pre violated: No next destination");
        }
        System.out.println("dest removed");
        Destination firstDest = destinations.removeFirst();
        
        if (isHasDestination()){
            setSpeed(4, 4);
        }
        return firstDest;
    }
    
    /**
     * 
     * @return Whether this drone has any destination
     */
    public boolean isHasDestination(){
        return destinations.size() > 0;
    }
    
    /**
     * Gets the next destination
     * @return The next destination, or null if no destinations
     */
    public Destination getNextDestination(){
        if (!isHasDestination()){
            return null;
        }
        return destinations.getFirst();
    }
    
    /**
     * Gets the final destination
     * @return The final destination, or null if no destinations
     */
    public Destination getFinalDestination(){
        if (destinations.size() <= 0){
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


    public int getSpeedX(){
        return speedX;
    }


    public int getSpeedY(){
        return speedY;
    }


    public void setSpeed(int speedX, int speedY){
        this.speedX = speedX;
        this.speedY = speedY;
    }

    /**
     *
     * @param x X coordinate of the destination
     * @param y Y coordinate of the destination
     * @param speed the speed with which the drone moves to the destination
     */
    /*
    public void goTo(int x, int y, int speed){
        this.setDestination(x, y);
        this.setSpeed(speed, speed);
    }
    */

    @Override
    public void drawObject(Graphics2D g) {

        //handles the subimages of the drone image
        counter++;
        if (counter >= COUNTER_FRAME_SWITCH){
            //timeout reached; reset counter and increment frame
            counter = 0;
            frame += 1;
            if (frame >= imageSequence.length){
                //reached past the final frame; reset the frame counter
                frame = 0;
            }
        }
        //Draw the image centered around its XY-coordinates, rather than them
        //being at the topleft of the image
        g.drawImage(imageSequence[frame], x - imageSequence[frame].getWidth()/2, y - imageSequence[frame].getHeight()/2, null);

        //TODO: This should be moved somewhere else
        update();
    }

    /**
     * Moves the drone on the screen based on its speed, destination, and
     * current location
     * @pre true
     * @post distance(x, y, destinationX, destinationY) <=
     *       \old(distance(x, y, destinationX, destinationY))
     */
    public void update(){
        //TODO: Diagonal movement is faster now: with a speed of x you will...
        //..move x horizontaly AND vertically, and not x towards the direction of the goal
        //Solve this with trigonometry
        
        Destination destination = getNextDestination();
        if (destination == null){
            return;
        }
        
        int destinationX = destination.getX();
        int destinationY = destination.getY();
        
        //Get the approaching direction (1 when moving along an axis; -1 otherwise)
        int dirX = x > destinationX ? -1 : 1;
        int dirY = y > destinationY ? -1 : 1;

        //Move
        x += dirX*speedX;
        y += dirY*speedY;

        //if we moved over the goal, move onto the goal instead
        if ((dirX == 1 && x >= destinationX) || (dirX == -1 && x <= destinationX)){
            speedX = 0;
            x = destinationX;
        }

        if ((dirY == 1 && y >= destinationY) || (dirY == -1 && y <= destinationY)){
            speedY = 0;
            y = destinationY;
        }
        
        if (x == destinationX && y == destinationY){
            removeNextDestination();
        }
    }
}