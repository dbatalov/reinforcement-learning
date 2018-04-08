package com.batalov.RL.render;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class ViewPort {
    double x_min;
    double y_min;
    double w;
    double h;
    double pix_per_m;

    public ViewPort(double x_min_px, double y_min_px, double w_px, double h_px, double pix_per_m) {
        this.pix_per_m = pix_per_m;
        this.x_min = toMeters(x_min_px);
        this.y_min = toMeters(y_min_px);
        this.w = toMeters(w_px);
        this.h = toMeters(h_px);
    }

    public Rectangle2D bbox() {
        return new Rectangle2D(x_min, y_min, w, h);
    }

    public void move(double x, double y) {
        this.x_min += x;
        this.y_min += y;
    }

    public Point2D toPixelCoordinates(Point2D p) {
        return new Point2D(toPixels(p.getX()-x_min), toPixels(p.getY()-y_min));
    }

    public double toPixels(double x) {
        return Math.round(x * pix_per_m);
    }

    public double toMeters(double x) {
        return x / pix_per_m;
    }
}
