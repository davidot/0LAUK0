package nl.tue.robots.drones.fileIO;

import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.model.Building;

import java.io.*;
import java.util.Scanner;
import java.util.regex.Pattern;

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

                if (line.startsWith("#")) {
                    // line is a comment skip it
                } else if (line.startsWith("___")) {
                    // done with nodes, break to start with edges
                    break;
                } else {
                    // read node
                    Scanner scan = new Scanner(line);
                    scan.useDelimiter(";");

                    int id = scan.nextInt();
                    int x = scan.nextInt();
                    int y = scan.nextInt();
                    int z = scan.nextInt();

                    Node n = new Node(x,y,z);
                    System.out.println("Node " + id + ": " + n);
                }

            }

            while (fileReader.ready()) {
                // start reading edges
                String line = fileReader.readLine();

                if (line.startsWith("#")) {
                    // line is a comment skip it
                } else {
                    // read edge

                }
            }


        } catch (IOException ex) {
            if (ex instanceof FileNotFoundException) {
                throw new FileNotFoundException("Could not find file" + fileName);
            }
        }


        return build;
    }

    public static void main(String[] args) {
        try {readBuilding("test5.csv");} catch (Exception e) {System.err.println(e);}
    }
}
