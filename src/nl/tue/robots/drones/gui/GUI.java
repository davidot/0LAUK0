package nl.tue.robots.drones.gui;

import nl.tue.robots.drones.fileIO.MalformedWallFileException;
import nl.tue.robots.drones.simulation.Simulation;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;


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

    //Should be 10, but can be any other number due to debugging
    public static final int MULTIPLIER = 7;

    private Simulation simulation;
    private boolean reload = false;

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
        } catch (Exception e) {
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
        } catch (InvocationTargetException | InterruptedException e) {
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
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
    }

    //private init since it should only be called once
    private void init() {
        try {
            // JFileChooser fileBrowser = new JFileChooser(new File("."));
            // int returnVal = fileBrowser.showOpenDialog(this);

            // if (returnVal == JFileChooser.APPROVE_OPTION) {
            //     File file = fileBrowser.getSelectedFile();
            simulation = new Simulation(new File("tests/Floorplan 9.csv"));
            internalResize(simulation.getSize());
            // } else {
            //     System.out.println("User canceled opening a file");
            //     JOptionPane.showConfirmDialog(this, "Did not select anything, shutting down",
            //             "Stopping", JOptionPane.DEFAULT_OPTION);
            //     System.exit(0);
            // }

        } catch (FileNotFoundException | MalformedWallFileException e) {
            e.printStackTrace();
            JOptionPane.showConfirmDialog(this, "Error opening files",
                    "Stopping", JOptionPane.DEFAULT_OPTION);
            System.exit(0);
        }

        MouseClickListener placeListener = new MouseClickListener(this);
        this.addMouseListener(placeListener);
        this.addKeyListener(new KeyboardListener(this));
//        this.add(placeListener.getContextMenu());
    }

    @Override
    public void run() {
        if (running) {
            return;
        }
        init();
        running = true;
        long lastTime = System.nanoTime();
        double unprocessed = 0;
        int frames = 0;
        int ticks = 0;
        long lastTimer = System.currentTimeMillis();

        while (isRunning) {
            boolean shouldRender = false;
            long now = System.nanoTime();
            unprocessed += (now - lastTime) / NS_TICKS;
            lastTime = now;
            if (unprocessed > TARGET_TICKS * 5) {
                double ticksLeft = TARGET_TICKS * 5;
                System.out
                        .println(TICK_OVER_PRE + (int) (unprocessed - ticksLeft) + TICK_OVER_POST);
                unprocessed = ticksLeft;
            }
            while (unprocessed >= 1) {
                tick();
                ticks++;
                unprocessed--;
                shouldRender = true;
            }


            if (shouldRender) {
                frames++;
                render();
            }

            try {
                Thread.sleep(SLEEPTIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (System.currentTimeMillis() - lastTimer > 1000) {
                lastTimer += 1000;
                if (frame != null) {
                    frame.setTitle(TITLE + " | " + frames + " fps | " + ticks + " ticks");
                }
                frames = 0;
                ticks = 0;
            }
        }
    }


    //private to make sure the amount of ticks stays on target
    private void tick() {
        if (reload) {
            try {
                simulation = new Simulation(new File("tests/Floorplan 9.csv"));
            } catch (FileNotFoundException | MalformedWallFileException e) {
                e.printStackTrace();
            }
            reload = false;
        }
        simulation.update();
    }

    private void render() {
        BufferStrategy buffer = getBufferStrategy();
        if (buffer == null) {
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

        simulation.draw(g, width, height);

        //stop drawing here
        g.dispose();
        buffer.show();
    }

    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        mainThread = new Thread(this, "Drone model");
        mainThread.start();
    }

    public void reload() {
        reload = true;
    }

    public Simulation getSimulation() {
        return simulation;
    }
}
