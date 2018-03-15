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
import nl.tue.robots.drones.gui.GUI;

/**
 *
 * @author sowdiyeah
 */
public class RealHuman extends RealObstacle {
    private double x, y;
    private int initialX, initialY;
    private BufferedImage humanIcon;
    private int angle = 0;
    
    public RealHuman(int x, int y, int floor) {
        super(x, y, floor, 2);
        initialX = x;
        initialY = y;
        try {
            humanIcon = ImageIO.read(new File("res/construction-worker.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
        @Override
    public void drawObject(Graphics2D g) {
        angle += 1;
        
        x = 3 * Math.cos(angle * Math.PI / 180) + initialX;
        y = 4 * Math.sin(angle * Math.PI / 180) + initialY;
        
        g.drawImage(humanIcon.getScaledInstance(size * GUI.MULTIPLIER, size * GUI.MULTIPLIER, BufferedImage.SCALE_SMOOTH), (int)(GUI.MULTIPLIER * (x - 1)), (int)(GUI.MULTIPLIER * (y - 1)), null);
    }
}
