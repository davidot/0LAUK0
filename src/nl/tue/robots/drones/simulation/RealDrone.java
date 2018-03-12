package nl.tue.robots.drones.simulation;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;


public class RealDrone extends RealObject {
    //where the drones are on the screen
    private int x;
    private int y;
    
    //Movement
    private int speedX = 0;
    private int speedY = 0;
    private int destinationX = 0;
    private int destinationY = 0;
    
    //Image and rendering
    private BufferedImage[] imageSequence;
    private int frame = 0;
    private int counter = 0;
    private final int COUNTER_FRAME_SWITCH = 10;
    private final String[] DEFAULT_IMAGE_SEQUENCE = new String[] {"drone32_frame1.png", "drone32_frame2.png"};
    
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
                BufferedImage img = ImageIO.read(new File("0LAUK0-master/res/" + imgs[i]));
                imgSequence[i] = img;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgSequence;
    }
    
    //basic getter
    public int getX(){
        return x;
    }
    
    //basic getter
    public int getY(){
        return y;
    }
    
    //basic setter
    public void setXY(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    //basic getter
    public int getDestinationX(){
        return destinationX;
    }
    
    //basic getter
    public int getDestinationY(){
        return destinationY;
    }
    
    //basic setter
    public void setDestination(int x, int y){
        this.destinationX = x;
        this.destinationY = y;
    }
    
    //basic getter
    public int getSpeedX(){
        return speedX;
    }
    
    //basic getter
    public int getSpeedY(){
        return speedY;
    }
    
    //basic setter
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
    public void goTo(int x, int y, int speed){
        this.setDestination(x, y);
        this.setSpeed(speed, speed);
    }

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
     * @return Whether this drone is currently moving to a destination
     */
    public boolean isHasDestination(){
        return speedX > 0 || speedY > 0;
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
    }
}
