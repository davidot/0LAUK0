/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.robots.drones.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.io.*;
import javax.imageio.*;

/**
 *
 * @author sowdiyeah
 */
public class RealHuman extends RealObstacle {
    private BufferedImage humanIcon;
    
    public RealHuman(int x, int y, int floor) {
        super(x, y, floor, 2);
        try {
            humanIcon = ImageIO.read(new File("res/human.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
        @Override
    public void drawObject(Graphics2D g) {
        g.drawImage(humanIcon, x - 1, y - 1, null);
    }
}
