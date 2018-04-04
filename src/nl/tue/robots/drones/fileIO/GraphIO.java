package nl.tue.robots.drones.fileIO;

import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.model.Building;
import nl.tue.robots.drones.simulation.RealBuilding;
import nl.tue.robots.drones.simulation.RealWall;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class GraphIO {

    /**
     * Reads and parses the file and creates a Building out of it.
     *
     * @param buildFile the file to read
     * @return The Building created from the contents of the file
     */
    public static Building readBuilding(File buildFile) throws FileNotFoundException {
        Building build = new Building();

        try(BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(buildFile), "UTF-8"))) {

            while (fileReader.ready()) {
                // start reading nodes
                String line = fileReader.readLine();

                if (line.startsWith("___")) {
                    // done with nodes, break to start with edges
                    break;
                } else if (!line.startsWith("#")) {
                    // read node
                    Scanner scan = new Scanner(line);
                    scan.useDelimiter(";");

                    int id = scan.nextInt();
                    int x = scan.nextInt();
                    int y = scan.nextInt();
                    int z = scan.nextInt();

                    Node n = new Node(x, y, z, id);

                    build.addNode(n, id);
                } // else line is a comment skip it

            }

            while (fileReader.ready()) {
                // start reading edges
                String line = fileReader.readLine();

                if (!line.startsWith("#")) {
                    // read edge

                    // get string to node IDs
                    String[] values = line.split(";");
                    int[] nodeIDs = {Integer.parseInt(values[0]), Integer.parseInt(values[1])};
                    boolean outside = values.length > 2 && values[2].equals("O");

                    // get mentioned nodes
                    build.addTransition(nodeIDs[0], nodeIDs[1], outside);

                } // else line is a comment skip it
            }

        } catch (IOException ex) {
            if (ex instanceof FileNotFoundException) {
                throw (FileNotFoundException) ex;
            } else {
                System.err.println("Could not read file" + buildFile.getName());
            }
        }

        return build;
    }

    /**
     * Writes the provided building to a <i>new</i> file with the specified name.
     *
     * @param b         The building to write to file
     * @param buildFile The file to save to
     * @throws IOException If something goes wrong during the writing process
     */
    public static void writeBuilding(Building b, File buildFile) throws IOException {
        if (!buildFile.createNewFile()) {
            throw new IOException("Can not create file" + buildFile.getAbsolutePath());
        }

        try(BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(buildFile, false), "UTF-8"))) {
            fileWriter.write("#ID;X;Y;Z");
            fileWriter.newLine();

            HashMap<Node, Integer> nodeToIDMap =
                    new HashMap<>(); // map so we know node ids when writing edges

            // Loop over all nodes to write them
            int buildSize = b.getAllNodes().size();
            for (int i = 0; i < buildSize; i++) {
                Node n = b.getNode(i);

                nodeToIDMap.put(n, i); // do bookkeeping

                // write node to file
                fileWriter.write(i + ";" + n.getX() + ";" + n.getY() + ";" + n.getZ());
                fileWriter.newLine();
            }

            // write separator between nodes and edges
            fileWriter.write("_______________");
            fileWriter.newLine();

            // loop over nodes to write their edges
            for (int i = 0; i < buildSize; i++) {
                Node n = b.getNode(i);
                int nodeID = nodeToIDMap.get(n);

                // write edges connected from this node
                for (Transition t : n.getTransitions()) {
                    int toID = nodeToIDMap.get(t.getTo());

                    if (toID < i) {
                        // this edge was written when visiting the other node, no need to write it again
                        continue; // so continue with next loop iteration
                    }

                    fileWriter.write(nodeID + ";" + toID);
                    fileWriter.newLine();
                }

            }

            fileWriter.flush(); // ensure buffer is fully written to file
        } catch (IOException e) {
            throw new IOException("Could not save building", e); // bounce exception
        }
    }

    /**
     * Reads the wall map from file and constructs a RealBuilding out of it.
     *
     * @param wallsFile The file from which to read
     * @return The constructed RealBuilding or {@code null} if the building could not be constructed.
     *
     * @throws FileNotFoundException      If the given file cannot be opened for reading.
     * @throws MalformedWallFileException If the given file does not properly specify the building.
     */
    public static RealBuilding readWalls(
            File wallsFile) throws FileNotFoundException, MalformedWallFileException {
        try(BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(wallsFile), "UTF-8"))) {
            ArrayList<RealWall> walls = new ArrayList<>();
            int maxFloor;
            int maxX;
            int maxY;

            // start reading
            String line = fileReader.readLine();

            // first line should specify dimensions
            if (line.startsWith("D;")) {

                String[] dimensions = line.split(";");
                if (dimensions.length < 4) {
                    throw new MalformedWallFileException(1, "Dimension specification incorrect");
                }
                maxFloor = Integer.parseInt(dimensions[1]);
                maxX = Integer.parseInt(dimensions[2]);
                maxY = Integer.parseInt(dimensions[3]);
            } else {
                throw new MalformedWallFileException(1,
                        "Dimensions should be specified on the first line");
            }

            int i = 2;
            while (fileReader.ready()) {
                // start reading walls
                line = fileReader.readLine();

                if (!line.startsWith("#")) {
                    if (!line.matches("(\\d+;){5}[IOio]?")) {
                        throw new MalformedWallFileException(i,
                                "Wall specification does not adhere to the format: " +
                                        "floor;x1;y1;x2;y2;I/O. Where I/O is either I for inside wall or O for outside wall");
                    }

                    // read wall
                    Scanner scan = new Scanner(line);
                    scan.useDelimiter(";");

                    int floor = scan.nextInt();
                    int x1 = scan.nextInt();
                    int y1 = scan.nextInt();
                    int x2 = scan.nextInt();
                    int y2 = scan.nextInt();
                    boolean outer = false;
                    if (scan.hasNext()) {
                        outer = scan.next().toLowerCase().equals("o");
                    }

                    scan.close(); // we got the values so out close the scanner

                    if (floor > maxFloor || x1 > maxX || x2 > maxX || y1 > maxY || y2 > maxY) {
                        throw new MalformedWallFileException(i,
                                "Wall falls outside dimensions of building: " +
                                        maxFloor + " floors, 0 <= x <= " + maxX + ", 0 <= y <= " +
                                        maxY);
                    }

                    // construct and add wall object
                    RealWall wall = new RealWall(floor, x1, y1, x2, y2, outer);
                    walls.add(wall);

                } // else line is a comment skip it
                i++;
            }

            // if we get here the file was fully read and nothing was wrong
            RealBuilding build = new RealBuilding(maxFloor, maxX, maxY);
            build.addWalls(walls);

            return build;
        } catch (IOException ex) {
            if (ex instanceof FileNotFoundException) {
                throw (FileNotFoundException) ex;
            } else {
                System.err.println("Could not read file" + wallsFile.getName());
            }
        }

        return null;
    }

    public static void writeWalls(RealBuilding b, File wallFile) throws IOException {
        if (!wallFile.createNewFile()) {
            throw new IOException("Can not create file" + wallFile.getAbsolutePath());
        }

        try(BufferedWriter fileWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(wallFile, false), "UTF-8"))) {
            fileWriter.write("D;" + b.getFloors() + ";" + b.getWidth() + ";" + b.getDepth());
            fileWriter.newLine();
            fileWriter.write("# Format: floor;x1;y1;x2;y2;I(nside)/O(utside)");
            fileWriter.newLine();

            // get all walls from building
            List<RealWall> walls = b.getAllWalls();
            for (RealWall w : walls) {
                int[] wallCoords = w.getCoords();
                fileWriter.write(w.getFloor() + ";" + wallCoords[0] + ";" + wallCoords[1] + ";" +
                        wallCoords[2] + ";" + wallCoords[3] + ";" + (w.isOuterWall() ? "O" : "I"));
                fileWriter.newLine();
            }

            fileWriter.flush(); // ensure buffer is fully written to file
        } catch (IOException e) {
            throw new IOException("Could not write wall", e); // bounce exception
        }
    }

}
