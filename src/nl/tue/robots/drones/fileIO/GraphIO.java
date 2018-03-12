package nl.tue.robots.drones.fileIO;

import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.model.Building;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class GraphIO {

    /**
     * Reads and parses the file and creates a Building out of it.
     * @return The Building created from the contents of the file
     */
    public static Building readBuilding(File buildFile) throws FileNotFoundException {
        Building build = new Building();

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(buildFile), "UTF-8"))){

            while (fileReader.ready()) {
                // start reading nodes
                String line = fileReader.readLine();

                if (line.startsWith("___")) {
                    // done with nodes, break to start with edges
                    break;
                } else if (!line.startsWith("#")){
                    // read node
                    Scanner scan = new Scanner(line);
                    scan.useDelimiter(";");

                    int id = scan.nextInt();
                    int x = scan.nextInt();
                    int y = scan.nextInt();
                    int z = scan.nextInt();

                    Node n = new Node(x,y,z);

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

                    // get mentioned nodes
                    Node a = build.getNode(nodeIDs[0]);
                    Node b = build.getNode(nodeIDs[1]);

                    // create transitions both ways
                    Transition transAB = new Transition(a, b);
                    Transition transBA = new Transition(b, a);

                    // add transitions to nodes
                    a.addTransistion(transAB);
                    b.addTransistion(transBA);

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
     * @param b The building to write to file
     * @param fileName The name of the file to be created
     * @throws IOException If something goes wrong during the writing process
     */
    public static void writeBuilding(Building b, String fileName) throws IOException {
        File buildFile = new File(fileName);
        if (!buildFile.createNewFile()) {
            throw new IllegalArgumentException("File " + fileName + " already exists");
        }

        try (BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(buildFile, false), "UTF-8"))){
            fileWriter.write("#ID;X;Y;Z");
            fileWriter.newLine();

            HashMap<Node, Integer> nodeToIDMap = new HashMap<>(); // map so we know node ids when writing edges

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
            throw e; // bounce exception
        }
    }

    public static String reportBuildingGraph(Building b) {
        String s = "";

        for (Node n : b.getAllNodes()) {
            s += "(" + n.getX() + "," + n.getY() + "," + n.getZ() + ") : \n";
            for (Transition t : n.getTransitions()) {
                s += "\t[(" + t.getTo().getX() + "," + t.getTo().getY() + "," + t.getTo().getZ() + ") : " + t.getDistance() + "]\n";
            }
        }
        return s;
    }

    public static void main(String[] args) {
        try {
            Building plan1 = readBuilding(new File("tests/Floorplan 1.csv"));
            System.out.println(reportBuildingGraph(plan1));

            writeBuilding(plan1, "tests/writeTest.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
