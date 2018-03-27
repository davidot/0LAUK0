package nl.tue.robots.drones.gui;

import nl.tue.robots.drones.simulation.Simulation;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class KeyboardListener implements KeyListener, MouseWheelListener {

    private final GUI gui;

    public KeyboardListener(GUI gui) {
        this.gui = gui;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_M:
                Simulation simulation = gui.getSimulation();
                simulation.setRenderModel(!simulation.getRenderModel());
                break;
            case KeyEvent.VK_R:
                gui.reload();
                break;
            case KeyEvent.VK_ESCAPE:
                //Pressing escape while placing a wall/obstacle allows you to
                //not place it instead
                gui.getMenuPanel().deactivate();
                gui.placeListener.cancelAction();
                break;
            case KeyEvent.VK_P:
                // toggle the pause status and show the new status in the UI
                if (gui.getSimulation().togglePause()) {
                    gui.getMenuPanel().setStatus("Simulation paused");
                } else {
                    gui.getMenuPanel().setStatus("Simulation Unpaused");
                }
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_LEFT:
                gui.getSimulation().floorDown();
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_RIGHT:
                gui.getSimulation().floorUp();
                break;
            case KeyEvent.VK_1:
                gui.getMenuPanel().activate(GUIMenuPanel.MenuPanelButton.DESTINATION);
                break;
            case KeyEvent.VK_2:
                gui.getMenuPanel().activate(GUIMenuPanel.MenuPanelButton.WORKER);
                break;
            case KeyEvent.VK_3:
                gui.getMenuPanel().activate(GUIMenuPanel.MenuPanelButton.WALL);
                break;
            case KeyEvent.VK_4:
                gui.getMenuPanel().activate(GUIMenuPanel.MenuPanelButton.OBSTACLE);
                break;
            case KeyEvent.VK_5:
                gui.getMenuPanel().activate(GUIMenuPanel.MenuPanelButton.MOVEMENT);
                break;
            case KeyEvent.VK_6:
                gui.getMenuPanel().activate(GUIMenuPanel.MenuPanelButton.REMOVE);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {
            gui.getSimulation().floorDown();
        } else if (e.getWheelRotation() > 0) {
            gui.getSimulation().floorUp();
        }
        gui.placeListener.cancelAction();
    }
}
