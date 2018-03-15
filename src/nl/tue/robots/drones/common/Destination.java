
package nl.tue.robots.drones.common;

/**
 * A class consisting of x y z coordinates, where z represents the floor
 * @since 15 MAR 2018
 */
public class Destination {
    private final int x;
    private final int y;
    private final int z;

    public Destination(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    public int getZ(){
        return this.z;
    }
    
    /**
     * 
     * @param dest The first destination
     * @param dest2 The second destination
     * @return Whether dest and dest2 have the same coordinates
     */
    public static boolean destinationsEqual(Destination dest, Destination dest2){
        return dest.x == dest2.x && dest.y == dest2.y && dest.z == dest2.z;
    }
}
