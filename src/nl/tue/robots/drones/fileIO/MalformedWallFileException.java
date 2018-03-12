package nl.tue.robots.drones.fileIO;

/**
 * Indicates that something is wrong in the file describing the walls of a building
 */
public class MalformedWallFileException extends Exception {

    private final int line;

    MalformedWallFileException(int lineNr, String message) {
        super(message);
        this.line = lineNr;
    }

    int getLine() {
        return line;
    }
}
