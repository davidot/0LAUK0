package nl.tue.robots.drones.gui;

import nl.tue.robots.drones.algorithm.Algorithm;
import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.fileIO.GraphIO;
import nl.tue.robots.drones.fileIO.MalformedWallFileException;
import nl.tue.robots.drones.model.Building;

import nl.tue.robots.drones.simulation.RealBuilding;
import nl.tue.robots.drones.simulation.RealDrone;

import javax.swing.*;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import nl.tue.robots.drones.simulation.RealHuman;
import nl.tue.robots.drones.simulation.RealObstacle;


public class GUI extends Canvas implements Runnable {

    //TODO: remove hardcoded drone
    private RealDrone d;
    private RealDrone e;

    /**
     * Title of the frame of the main {@link GUI}
     */
    public static final String TITLE = "Drone model";
    /**
     * The charset used to read and write data
     */
    public static final String CHARSET_NAME = "UTF-8";

    public static final int SLEEPTIME = 2;
    /**
     * The target amount of ticks per second
     */
    public static final int TARGET_TICKS = 60;
    /**
     * The amount of nanoseconds one tick can take
     */
    public static final double NS_TICKS = 1000000000.0 / TARGET_TICKS;

    //Strings
    private static final String TICK_OVER_PRE = "Skipping ";
    private static final String TICK_OVER_POST = " ticks is the system overloaded?";
    public static final char LINE_SEPARATOR_CHAR = '\n';
    private static final int NODE_RADIUS = 4;

    //size of screen in tiles

    /**
     * Default width of the frame
     */
    private static int RENDER_WIDTH = 1024;
    /**
     * Default height of the frame
     */
    private static int RENDER_HEIGHT = 640;

    private static JFrame frame;
    //needed for threads
    private boolean isRunning;
    //one time objects

    //thread security
    private boolean running;
    private Thread mainThread;


    private Building building;
    private RealBuilding realBuilding;
    private ArrayList<Transition> path;
    private Map<Integer, Node> nodes;
    private ArrayList<Transition> transitions;
    public static final int MULTIPLIER = 10;

    private GUI() {

    }


    /**
     * @return width of the frame
     */
    public static int getRenderWidth() {
        return RENDER_WIDTH;
    }

    /**
     * @return height of the frame
     */
    public static int getRenderHeight() {
        return RENDER_HEIGHT;
    }

    public static void main(String[] args) {
        startDefault();
    }

    private static void startDefault() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
            e.printStackTrace();
        }

        GUI GUI = new GUI();
        GUI.setMinimumSize(new Dimension(RENDER_WIDTH, RENDER_HEIGHT));
        GUI.setMaximumSize(new Dimension(RENDER_WIDTH, RENDER_HEIGHT));
        GUI.setPreferredSize(new Dimension(RENDER_WIDTH, RENDER_HEIGHT));
        GUI.setIgnoreRepaint(true);

        //Make the frame
        frame = new JFrame(TITLE);
        frame.setIgnoreRepaint(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(GUI, BorderLayout.CENTER);
        frame.setResizable(false);
        GUI.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeFrame(e.getComponent().getSize());
            }
        });
        //Make sure the frame is packed
        try {
            SwingUtilities.invokeAndWait(frame::pack);
        } catch(InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //Hack for a way to display the fps on the frame
        GUI.start();

    }

    private static void setPixelSize(int width, int height) {
        RENDER_WIDTH = width;
        RENDER_HEIGHT = height;
    }

    private static void resizeFrame(Dimension d) {
        setPixelSize(d.width, d.height);
    }

    private void internalResize(Dimension d) {
        frame.setResizable(true);
        setMinimumSize(d);
        setMaximumSize(d);
        setPreferredSize(d);
        try {
            SwingUtilities.invokeAndWait(frame::pack);
        } catch(InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
        frame.setResizable(false);
    }

    //private init since it should only be called once
    private void init() {
        try {
            JFileChooser fileBrowser = new JFileChooser(new File("."));
            int returnVal = fileBrowser.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileBrowser.getSelectedFile();
                //This is where a real application would open the file.
                building = GraphIO.readBuilding(file);
                realBuilding = GraphIO.readWalls(new File(file.getParent(), file.getName().replace(".csv", ".walls")));
                realBuilding.addObject(new RealHuman(5, 5, 0));
                internalResize(new Dimension(realBuilding.getWidth() * MULTIPLIER * 4 + 9 * MULTIPLIER, realBuilding.getDepth() * MULTIPLIER + 40));
            } else {
                System.out.println("User canceled opening a file");
                JOptionPane.showConfirmDialog(this, "Did not select anything, shutting down",
                        "Stopping", JOptionPane.DEFAULT_OPTION);
                System.exit(0);
            }

        } catch(FileNotFoundException | MalformedWallFileException e) {
            e.printStackTrace();
            JOptionPane.showConfirmDialog(this, "Error opening files",
                    "Stopping", JOptionPane.DEFAULT_OPTION);
            System.exit(0);
        }

        Node from = building.getNode(0);
        Node to = building.getNode(144);

        if (from == null || to == null) {
            System.exit(-1);
        }

        long now = System.currentTimeMillis();
        path = new Algorithm().findPath(from, to);
        System.out.println("Took: " + (System.currentTimeMillis() - now));
        nodes = building.getAllNodesWithId();
        transitions = new ArrayList<>(nodes.size());
        building.getAllNodes().forEach(node -> transitions.addAll(node.getTransitions()));
    }

    @Override
    public void run() {
        if(running) {
            return;
        }
        init();
        running = true;
        long lastTime = System.nanoTime();
        double unprocessed = 0;
        int frames = 0;
        int ticks = 0;
        long lastTimer = System.currentTimeMillis();

        while(isRunning) {
            boolean shouldRender = false;
            long now = System.nanoTime();
            unprocessed += (now - lastTime) / NS_TICKS;
            lastTime = now;
            if(unprocessed > TARGET_TICKS * 5) {
                double ticksLeft = TARGET_TICKS * 5;
                System.out
                        .println(TICK_OVER_PRE + (int) (unprocessed - ticksLeft) + TICK_OVER_POST);
                unprocessed = ticksLeft;
            }
            while(unprocessed >= 1) {
                tick();
                ticks++;
                unprocessed--;
                shouldRender = true;
            }


            if(shouldRender && building != null) {
                frames++;
                render();
            }

            try {
                Thread.sleep(SLEEPTIME);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            if(System.currentTimeMillis() - lastTimer > 1000) {
                lastTimer += 1000;
                if(frame != null) {
                    frame.setTitle(TITLE + " | " + frames + " fps | " + ticks + " ticks");
                }
                frames = 0;
                ticks = 0;
            }
        }
    }


    //private to make sure the amount of ticks stays on target
    private void tick() {
    }

    private void render() {
        BufferStrategy buffer = getBufferStrategy();
        if(buffer == null) {
            this.createBufferStrategy(2);
            requestFocus();
            return;
        }
        Graphics2D g = (Graphics2D) buffer.getDrawGraphics();

        //get the current size of the screen
        int width = getRenderWidth();
        int height = getRenderHeight();

        //clear the last frame
        g.setColor(getBackground());
        g.fillRect(0, 0, width, height);

        //start drawing here

        g.translate(width / 4, 20);
        realBuilding.render(g, 3, 0, 3);


        g.translate(-MULTIPLIER, -MULTIPLIER);

        //draw the nodes
        //todo hardcoded shit
        int floor = (realBuilding.getWidth() + 3) * MULTIPLIER;
        g.setStroke(new BasicStroke(2));

        //draw the transitions
        for (Transition transition: transitions) {
            Node from = transition.getFrom();
            Node to = transition.getTo();
            if (transition.isOutside()) {
                g.setColor(Color.RED);
            } else if (from.getZ() != to.getZ()) {
                g.setColor(Color.ORANGE);
                int x1 = from.getX() * MULTIPLIER + from.getZ() * floor;
                int x2 = to.getX() * MULTIPLIER + to.getZ() * floor;
                int y1 = from.getY() * MULTIPLIER;
                int y2 = to.getY() * MULTIPLIER;
                QuadCurve2D curve = new QuadCurve2D.Double(
                        x1, y1, (x1 + x2) / 2, (y1 + y2) / 2 - 75, x2, y2);
                g.draw(curve);
                continue;
            } else {
                g.setColor(Color.BLACK);
            }
            g.drawLine(from.getX() * MULTIPLIER +from.getZ() * floor, from.getY() * MULTIPLIER,
                    to.getX() * MULTIPLIER +to.getZ() * floor, to.getY() * MULTIPLIER);
        }

        g.setStroke(new BasicStroke(1));

        //nodes
        for (Map.Entry<Integer, Node> entry:nodes.entrySet()) {
            int num = entry.getKey();
            Node node = entry.getValue();
            g.setColor(Color.BLACK);
            g.fillOval(node.getX()* MULTIPLIER - NODE_RADIUS + node.getZ() * floor, node.getY()*
                    MULTIPLIER - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            g.setColor(Color.RED);
            g.drawString("n:" + num, node.getX()* MULTIPLIER +node.getZ() * floor, node.getY()*
                    MULTIPLIER - NODE_RADIUS * 2);
        }


        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(2));
        //path
        for (Transition transition: path) {
            Node from = transition.getFrom();
            Node to = transition.getTo();
            if (from.getZ() != to.getZ()) {
                g.setColor(Color.GREEN);
                int x1 = from.getX() * MULTIPLIER + from.getZ() * floor;
                int x2 = to.getX() * MULTIPLIER + to.getZ() * floor;
                int y1 = from.getY() * MULTIPLIER;
                int y2 = to.getY() * MULTIPLIER;
                QuadCurve2D curve = new QuadCurve2D.Double(
                        x1, y1, (x1 + x2) / 2, (y1 + y2) / 2 - 75, x2, y2);
                g.draw(curve);
                continue;
            } else {
                g.setColor(Color.BLUE);
            }
            g.drawLine(from.getX() * MULTIPLIER + from.getZ() * floor, from.getY() * MULTIPLIER,
                    to.getX() * MULTIPLIER + to.getZ() * floor, to.getY() * MULTIPLIER);
        }

        //draw the drones on the screen
        //hardcoded for now (and in the wrong place too probably)
        //Makes the drones move left to right; Ignore the spaghetti
        if (d == null){
            d = new RealDrone(0, 0, 0);
            d.addDestination(100, 200, 0);
            d.addDestination(10, 200, 0);
            d.addDestination(100, 150, 0);
            d.addDestination(150, 100, 0);
            d.addDestination(0, 0, 0);
        }else if (!d.isHasDestination()){
            d.setSpeed(4, 4);
            if (d.getX() < 100){
                d.addDestination(200, 200, 0);
            }else{
                d.addDestination(0, 200, 0);
            }
        }
        System.out.println(d.getSpeedX());
        System.out.println(d.getSpeedY());

        if (e == null){
            e = new RealDrone(0, 0, 0);
            e.addDestination(100, 0, 0);
        }else if (!e.isHasDestination()){
            if (e.getX() < 100){
                e.addDestination(200, 0, 0);
            }else{
                e.addDestination(0, 0, 0);
            }
        }

        d.drawObject(g);
        e.drawObject(g);

        g.translate(MULTIPLIER, MULTIPLIER);

        //stop drawing here
        g.dispose();
        buffer.show();
    }

    public void start() {
        if(isRunning) {
            return;
        }
        isRunning = true;
        System.out.println("Starting main thread");
        mainThread = new Thread(this, "Drone model");
        mainThread.start();
    }

    public void stop() {
        if(!isRunning) {
            return;
        }
        isRunning = false;
        SwingUtilities.invokeLater(() -> {
            try {
                mainThread.join();
                System.exit(0);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        });
    }


}
