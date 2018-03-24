package nl.tue.robots.drones.gui;

import nl.tue.robots.drones.simulation.Simulation;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardListener implements KeyListener {

    private GUI gui;

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
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
