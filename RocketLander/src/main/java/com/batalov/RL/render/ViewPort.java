package com.batalov.RL.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

public class ViewPort {
    public double x;
    public double y;

    public double w_pix;
    public double h_pix;

    private final double PIXELS_PER_METER = 5.;
    public BBox extents;
    private BBox environment;

    public ViewPort(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w_pix = w;
        this.h_pix = h;
        extents = new BBox(
                x - w / (2 * PIXELS_PER_METER), y - h / (2 * PIXELS_PER_METER),
                x + w / (2 * PIXELS_PER_METER), y + h / (2 * PIXELS_PER_METER));
    }


    /**
     * Move the viewport in world coordinates
     */
    public void move(double x, double y) {
        this.x += x;
        this.y += y;
    }

    public void render(GraphicsContext gc) {

    }

    public Point2D toPixels(Point2D p) {
        return new Point2D(Math.round(p.getX() * PIXELS_PER_METER), Math.round(p.getY() * PIXELS_PER_METER));
    }
    public double toPixels(double x) {
        return Math.round(x*PIXELS_PER_METER);
    }
}
