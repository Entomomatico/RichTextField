/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package richtextfield.utils;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class ScreenCapture extends JFrame implements MouseListener, MouseMotionListener {
  
    private Point startPt;
    private Point endPt;

    private Rectangle imageRect = null;
    private BufferedImage buff = null;

    private boolean flag = false;

    private int X = 0, Y = 0, WIDTH = 0, HEIGHT = 0;
    private Color selectionColor;

    public ScreenCapture() {
        this(Color.RED);
    }

    public ScreenCapture(Color selectionColor) {

        this.selectionColor = selectionColor;

        startPt = new Point();
        endPt = new Point();

        // setting full frame size;
//        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        Rectangle screenRect;
        double minX = 0, minY = 0, maxX = 0, maxY = 0;
        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            screenRect = gd.getDefaultConfiguration().getBounds();
            minX = Math.min(minX, screenRect.getMinX());
            minY = Math.min(minY, screenRect.getMinY());
            maxX = Math.max(maxX, screenRect.getMinX() + gd.getDisplayMode().getWidth());
            maxY = Math.max(maxY, screenRect.getMinY() + gd.getDisplayMode().getHeight());
        }
        
        this.setLocation(BigDecimal.valueOf(minX).intValue(), BigDecimal.valueOf(minY).intValue());
        this.setSize(BigDecimal.valueOf(maxX - minX).intValue(), BigDecimal.valueOf(maxY - minY).intValue());

        // Mouse Listener and Mouse Motion listener;
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        // Blurring the frame;
        this.setUndecorated(true);
        this.setOpacity(0.2f);
    }

    public void captureImage() {

        flag = false;
        this.setVisible(true);

        while (!flag) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
        }
    }

    // coloring selected region;
    @Override
    public void paint(Graphics g) {

        super.paint(g);

        setValues();

        g.setColor(selectionColor);
        g.fillRect(X, Y, WIDTH, HEIGHT);
    }

    // calculating start point and width & height;
    private void setValues() {
        X = Math.min(startPt.x, endPt.x);
        Y = Math.min(startPt.y, endPt.y);
        WIDTH = Math.abs(startPt.x - endPt.x);
        HEIGHT = Math.abs(startPt.y - endPt.y);
    }

    public void setSelectionColor(Color color) {
        this.selectionColor = color;
    }

    // starting point of selected region;
    @Override
    public void mousePressed(MouseEvent e) {

        startPt.x = e.getX();
        startPt.y = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        // if no any region is selected;
        if (WIDTH == 0 || HEIGHT == 0) {
            flag = true;
            this.dispose();
            return;
        }

        // hiding frame to capture clear image;
        this.setVisible(false);

        // imageRect is selected region;
        Point location = this.getLocation();
        imageRect = new Rectangle(X + location.x, Y + location.y, WIDTH, HEIGHT);
        try {
            // capturing image of selected region;
            buff = new Robot().createScreenCapture(imageRect);
        } catch (AWTException ex) {
        }

        flag = true;
        this.dispose();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        endPt.x = e.getX();
        endPt.y = e.getY();
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    public boolean isImageCaptured() {
        return (buff != null);
    }

    public BufferedImage getImage() {
        return buff;
    }

    public ImageIcon getImageIcon() {
        return new ImageIcon(buff);
    }
}
  

