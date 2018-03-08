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
     * @param fileName the name of the file to read.
     * @return The Building created from the contents of the file
     */
    public static Building readBuilding(String fileName) throws FileNotFoundException {
        Building build = new Building();
        File buildFile = new File(fileName);

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
                throw new FileNotFoundException("Could not find file" + fileName);
            } else {
                System.err.println("Could not read file" + fileName);
            }
        }

        return build;
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
            Building plan1 = readBuilding("tests/Floorplan 1.csv");
            System.out.println(reportBuildingGraph(plan1));

        } catch (FileNotFoundException e) {
            System.err.println(e);
        }
    }

}
