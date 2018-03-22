package nl.tue.robots.drones.gui;

import nl.tue.robots.drones.simulation.Simulation;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardListener implements KeyListener {
    private final Simulation simulation;

    public KeyboardListener(Simulation simulation) {
        this.simulation = simulation;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_M:
                simulation.setDrawModel(!simulation.getDrawModel());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
