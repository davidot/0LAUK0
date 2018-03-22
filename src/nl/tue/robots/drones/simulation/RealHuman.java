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
        super(x, y, floor, 2, 2);
        try {
            humanIcon = ImageIO.read(new File("res/construction-worker.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawObject(Graphics2D g) {
        g.drawImage(humanIcon.getScaledInstance(sizeX * GUI.MULTIPLIER, sizeY * GUI.MULTIPLIER,
                BufferedImage.SCALE_SMOOTH), (int) (GUI.MULTIPLIER * (x - 1)),
                (int) (GUI.MULTIPLIER * (y - 1)), null);
    }

    @Override
    public void drawSide(Graphics2D g) {
        g.drawImage(humanIcon, 0, 0, RealBuilding.DRAW_SIZE, RealBuilding.DRAW_SIZE, null);
    }
}
