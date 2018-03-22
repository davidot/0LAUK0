package nl.tue.robots.drones.gui;

import nl.tue.robots.drones.simulation.Simulation;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardListener implements KeyListener {
    private final Simulation simulation;
    private GUI gui;

    public KeyboardListener(Simulation simulation, GUI gui) {
        this.simulation = simulation;
        this.gui = gui;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_M:
                simulation.setDrawModel(!simulation.getDrawModel());
            case KeyEvent.VK_R:
                gui.reload();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
