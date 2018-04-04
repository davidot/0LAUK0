package nl.tue.robots.drones.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;

import static nl.tue.robots.drones.gui.GUIMenuPanel.MenuPanelButton.DESTINATION;
import static nl.tue.robots.drones.gui.GUIMenuPanel.MenuPanelButton.MOVEMENT;
import static nl.tue.robots.drones.gui.GUIMenuPanel.MenuPanelButton.OBSTACLE;
import static nl.tue.robots.drones.gui.GUIMenuPanel.MenuPanelButton.REMOVE;
import static nl.tue.robots.drones.gui.GUIMenuPanel.MenuPanelButton.WALL;
import static nl.tue.robots.drones.gui.GUIMenuPanel.MenuPanelButton.WORKER;

public class GUIMenuPanel extends Canvas {

    public enum MenuPanelButton {DESTINATION, WORKER, WALL, OBSTACLE, MOVEMENT, REMOVE}

    public static final int BUTTON_HEIGHT = 25;
    public static final int BUTTON_WIDTH = 100;
    public static final int BUTTON_MARGIN = 5;
    public static final int INNER_MARGIN = 5;

    private final Rectangle2D destination = new Rectangle2D.Double();
    private boolean destinationActive = false;
    private final Rectangle2D worker = new Rectangle2D.Double();
    private boolean workerActive = false;
    private final Rectangle2D wall = new Rectangle2D.Double();
    private boolean wallActive = false;
    private final Rectangle2D obstacle = new Rectangle2D.Double();
    private boolean obstacleActive = false;
    private final Rectangle2D movement = new Rectangle2D.Double();
    private boolean movementActive = false;
    private final Rectangle2D remove = new Rectangle2D.Double();
    private boolean removeActive = false;
    private String status = "";
    private Color statusColor = Color.BLACK;

    private MouseClickListener mcl;

    protected GUIMenuPanel() {
        super();
        int xOffset = BUTTON_MARGIN;
        destination.setFrame(xOffset, BUTTON_MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT);
        xOffset += BUTTON_WIDTH + BUTTON_MARGIN;
        worker.setFrame(xOffset, BUTTON_MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT);
        xOffset += BUTTON_WIDTH + BUTTON_MARGIN;
        wall.setFrame(xOffset, BUTTON_MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT);
        xOffset += BUTTON_WIDTH + BUTTON_MARGIN;
        obstacle.setFrame(xOffset, BUTTON_MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT);
        xOffset += BUTTON_WIDTH + BUTTON_MARGIN;
        movement.setFrame(xOffset, BUTTON_MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT);
        xOffset += BUTTON_WIDTH + BUTTON_MARGIN;
        remove.setFrame(xOffset, BUTTON_MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (destination.contains(x, y)) {
                    activate(DESTINATION);
                } else if (worker.contains(x, y)) {
                    activate(WORKER);
                } else if (wall.contains(x, y)) {
                    activate(WALL);
                } else if (obstacle.contains(x, y)) {
                    activate(OBSTACLE);
                } else if (movement.contains(x, y)) {
                    activate(MOVEMENT);
                } else if (remove.contains(x, y)) {
                    activate(REMOVE);
                }
                e.getComponent().repaint();
            }
        });
    }

    /**
     * Deactivates all actions
     */
    public void deactivate() {
        destinationActive = false;
        workerActive = false;
        wallActive = false;
        obstacleActive = false;
        movementActive = false;
        removeActive = false;
        repaint();
        setStatus("");
    }

    /**
     * Sets the given button as active.
     *
     * @param b the button to set as active
     */
    protected void activate(MenuPanelButton b) {
        mcl.cancelAction();
        deactivate();
        switch(b) {
            case DESTINATION:
                destinationActive = true;
                setStatus("Picking destination");
                mcl.startDestinationPick();
                break;
            case WORKER:
                workerActive = true;
                mcl.startHumanPlacement();
                setStatus("Placing worker");
                break;
            case WALL:
                wallActive = true;
                mcl.startWallPlacement();
                setStatus("Picking first wall point");
                break;
            case OBSTACLE:
                obstacleActive = true;
                mcl.startObstaclePlacement();
                setStatus("Picking first obstacle point");
                break;
            case MOVEMENT:
                movementActive = true;
                mcl.startWorkerMovement();
                setStatus("Picking worker to move");
                break;
            case REMOVE:
                removeActive = true;
                mcl.startRemoval();
                setStatus("Removing obstacle / worker");
                break;
        }
        repaint();
    }

    public void setMouseClickListener(MouseClickListener listener) {
        this.mcl = listener;
    }

    /**
     * Puts the specified text in the status bar.
     *
     * @param s the text to display
     */
    public void setStatus(String s) {
        setStatus(s, Color.BLACK);
    }

    /**
     * Puts the specified text in the status bar with the specified colour.
     *
     * @param s the text to display
     * @param c the colour in which to show the text
     */
    public void setStatus(String s, Color c) {
        status = s;
        statusColor = c;
        repaint();
    }

    public void render() {
        BufferStrategy buffer = getBufferStrategy();
        if (buffer == null) {
            this.createBufferStrategy(2);
            return;
        }
        Graphics2D g = (Graphics2D) buffer.getDrawGraphics();

        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        // draw buttons
        g.setColor(Color.BLACK);
        g.drawRect((int) destination.getX(), (int) destination.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        g.drawRect((int) worker.getX(), (int) worker.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        g.drawRect((int) wall.getX(), (int) wall.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        g.drawRect((int) obstacle.getX(), (int) obstacle.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        g.drawRect((int) movement.getX(), (int) movement.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        g.drawRect((int) remove.getX(), (int) remove.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);

        fillButtons(g);
        drawButtonTexts(g);

        // draw status bar
        g.setColor(Color.BLACK);
        g.drawRect((int) remove.getX() + BUTTON_WIDTH + BUTTON_MARGIN, BUTTON_MARGIN,
                3 * BUTTON_WIDTH, BUTTON_HEIGHT);
        if (!status.equals("")) {
            g.setColor(statusColor);
            g.drawString(status, (int) remove.getX() + BUTTON_WIDTH + BUTTON_MARGIN + INNER_MARGIN,
                    BUTTON_HEIGHT + BUTTON_MARGIN - INNER_MARGIN);
        }

        g.dispose();
        buffer.show();
    }

    private void fillButtons(Graphics g) {
        // fill all as inactive
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect((int) destination.getX(), (int) destination.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        g.fillRect((int) worker.getX(), (int) worker.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        g.fillRect((int) wall.getX(), (int) wall.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        g.fillRect((int) obstacle.getX(), (int) obstacle.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        g.fillRect((int) movement.getX(), (int) movement.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        g.fillRect((int) remove.getX(), (int) remove.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
        // give active one a darker shade
        if (destinationActive || workerActive || wallActive || obstacleActive || movementActive ||
                removeActive) {
            g.setColor(Color.GRAY);
            if (destinationActive) {
                g.fillRect((int) destination.getX(), (int) destination.getY(), BUTTON_WIDTH,
                        BUTTON_HEIGHT);
            }
            if (workerActive) {
                g.fillRect((int) worker.getX(), (int) worker.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
            }
            if (wallActive) {
                g.fillRect((int) wall.getX(), (int) wall.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
            }
            if (obstacleActive) {
                g.fillRect((int) obstacle.getX(), (int) obstacle.getY(), BUTTON_WIDTH,
                        BUTTON_HEIGHT);
            }
            if (movementActive) {
                g.fillRect((int) movement.getX(), (int) movement.getY(), BUTTON_WIDTH,
                        BUTTON_HEIGHT);
            }
            if (removeActive) {
                g.fillRect((int) remove.getX(), (int) remove.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
            }
        }
    }

    private void drawButtonTexts(Graphics g) {
        // put text on buttons
        g.setColor(Color.BLACK);
        g.drawString(MouseClickListener.DRONE_TEXT, (int) destination.getX() + INNER_MARGIN,
                (int) destination.getMaxY() - INNER_MARGIN);
        g.drawString(MouseClickListener.HUMAN_TEXT, (int) worker.getX() + INNER_MARGIN,
                (int) worker.getMaxY() - INNER_MARGIN);
        g.drawString(MouseClickListener.WALL_TEXT, (int) wall.getX() + INNER_MARGIN,
                (int) wall.getMaxY() - INNER_MARGIN);
        g.drawString(MouseClickListener.OBSTACLE_TEXT, (int) obstacle.getX() + INNER_MARGIN,
                (int) obstacle.getMaxY() - INNER_MARGIN);
        g.drawString(MouseClickListener.MOVEMENT_TEXT, (int) movement.getX() + INNER_MARGIN,
                (int) movement.getMaxY() - INNER_MARGIN);
        g.drawString(MouseClickListener.REMOVE_TEXT, (int) remove.getX() + INNER_MARGIN,
                (int) remove.getMaxY() - INNER_MARGIN);
    }
}
