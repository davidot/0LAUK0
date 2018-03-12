package nl.tue.robots.drones.gui;

import nl.tue.robots.drones.algorithm.Algorithm;
import nl.tue.robots.drones.common.Node;
import nl.tue.robots.drones.common.Transition;
import nl.tue.robots.drones.fileIO.GraphIO;
import nl.tue.robots.drones.model.Building;

import javax.swing.*;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class GUI extends Canvas implements Runnable {

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
    private static final int NODE_RADIUS = 5;
    private static final int FLOOR = 100;

    //size of screen in tiles

    /**
     * Default width of the frame
     */
    private static int RENDER_WIDTH = 1024;
    /**
     * Default height of the frame
     */
    private static int RENDER_HEIGHT = 640;

    private static JFileChooser fileBrowser;
    private static JFrame frame;
    //needed for threads
    private boolean isRunning;
    //one time objects

    //thread security
    private boolean running;
    private Thread mainThread;


    //Drone model variables
    private boolean checked = false;
    private Building building;
    private ArrayList<Transition> path;
    private Map<Integer, Node> nodes;
    private ArrayList<Transition> transitions;

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

    public static GUI startDefault() {

        boolean fullscreen = false;

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
        //frame.setResizable(false);
        if(fullscreen) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);
            setPixelSize(Toolkit.getDefaultToolkit().getScreenSize().width,
                    Toolkit.getDefaultToolkit().getScreenSize().height);
        } else {
            GUI.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    resizeFrame(e.getComponent().getSize());
                }
            });
        }
        //Make sure the frame is packed
        try {
            SwingUtilities.invokeAndWait(frame::pack);
        } catch(InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }

        fileBrowser = new JFileChooser();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //Hack for a way to display the fps on the frame
        GUI.start();

        return GUI;
    }

    private static void setPixelSize(int width, int height) {
        RENDER_WIDTH = width;
        RENDER_HEIGHT = height;
    }

    private static void resizeFrame(Dimension d) {
        setPixelSize(d.width, d.height);
    }

    //private init since it should only be called once
    private void init() {
        try {
            File f = null;
            int returnVal = fileBrowser.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileBrowser.getSelectedFile();
                //This is where a real application would open the file.
                building = GraphIO.readBuilding(file);
            } else {
                System.out.println("User canceled opening a file");
            }

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        if (building != null) {
            Node from = building.getNode(0);
            Node to = building.getNode(0);

            if (from == null || to == null) {
                System.exit(-1);
            }

            path = new Algorithm().findPath(from, to);
            nodes = building.getAllNodesWithId();
            transitions = new ArrayList<>(nodes.size());
            building.getAllNodes().forEach(node -> transitions.addAll(node.getTransitions()));
        }
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

        g.translate(20, 20);
        int multiplier = 50;

        for (Map.Entry<Integer, Node> entry:nodes.entrySet()) {
            int num = entry.getKey();
            Node node = entry.getValue();
            g.setColor(Color.BLACK);
            g.fillOval(node.getX()*multiplier - NODE_RADIUS + node.getZ() * FLOOR, node.getY()*multiplier - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            g.setColor(Color.RED);
            g.drawString("n:" + num, node.getX()*multiplier+node.getZ() * FLOOR, node.getY()*multiplier - NODE_RADIUS * 2);
        }

        g.setStroke(new BasicStroke(3));

        for (Transition transition: transitions) {
            Node from = transition.getFrom();
            Node to = transition.getTo();
            if (from.getZ() != to.getZ()) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.BLACK);
            }
            g.drawLine(from.getX() * multiplier+from.getZ() * FLOOR, from.getY() * multiplier,
                    to.getX() * multiplier+to.getZ() * FLOOR, to.getY() * multiplier);
        }

        g.setColor(Color.BLUE);

        for (Transition transition: path) {
            Node from = transition.getFrom();
            Node to = transition.getTo();
            g.drawLine(from.getX() * multiplier + from.getZ() * FLOOR, from.getY() * multiplier,
                    to.getX() * multiplier + to.getZ() * FLOOR, to.getY() * multiplier);
        }




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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    mainThread.join();
                    System.exit(0);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
    }


}
