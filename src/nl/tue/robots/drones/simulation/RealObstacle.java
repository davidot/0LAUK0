/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.robots.drones.simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import nl.tue.robots.drones.gui.GUI;

/**
 *
 * @author sowdiyeah
 */
public class RealObstacle extends RealObject {
    protected int x, y;    
    protected int size;
    
    public RealObstacle(int x, int y, int floor, int size) {
        super(floor);
        this.x = x;
        this.y = y;
        this.size = size;
    }
    
    public int getX(){
        return this.x;
    }
    
    public int getY(){
        return this.x;
    }
        
    @Override
    public void drawObject(Graphics2D g) {
        g.setColor(Color.RED);
        g.fillOval(GUI.MULTIPLIER * x - size / 2, GUI.MULTIPLIER * y - size / 2, size, size);
    }
}
