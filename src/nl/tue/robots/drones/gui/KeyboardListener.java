package nl.tue.robots.drones.gui;

import nl.tue.robots.drones.simulation.Simulation;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardListener implements KeyListener {

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
                simulation.setDrawModel(!simulation.getDrawModel());
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
                gui.getSimulation().togglePause();
                break;
            case KeyEvent.VK_UP:
                gui.getSimulation().floorDown();
                break;
            case KeyEvent.VK_DOWN:
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
                gui.getMenuPanel().activate(GUIMenuPanel.MenuPanelButton.REMOVE);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
