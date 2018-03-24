/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.robots.drones.simulation;

import nl.tue.robots.drones.gui.GUI;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author sowdiyeah
 */
public class RealHuman extends RealObstacle {

    private BufferedImage humanIcon;

    public RealHuman(int x, int y, int floor) {
        super(floor, x-1 , y-1, x+1, y+1);
        try {
            humanIcon = ImageIO.read(new File("res/construction-worker.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawObject(Graphics2D g) {
        g.drawImage(humanIcon.getScaledInstance(getXSize() * GUI.MULTIPLIER, getYSize() * GUI.MULTIPLIER,
                BufferedImage.SCALE_SMOOTH), (GUI.MULTIPLIER * (getX() - 1)),
                (GUI.MULTIPLIER * (getY() - 1)), null);
    }

    @Override
    public void drawSide(Graphics2D g) {
        g.drawImage(humanIcon, 0, 0, RealBuilding.DRAW_SIZE, RealBuilding.DRAW_SIZE, null);
    }

    @Override
    public String toString() {
        return String.format("[Human at (%d,%d)]", getX(),getY());
    }
}
