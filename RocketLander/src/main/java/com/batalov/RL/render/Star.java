package com.batalov.RL.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Star {
    Point2D center;
    int spikes;
    double innerRadius;
    double outerRadius;

    public Star(Point2D center, int spikes, double innerRadius, double outerRadius) {
        this.center = center;
        this.spikes = spikes;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
    }
    public Star(Star star) {
        center = star.center;
        spikes = star.spikes;
    }

    public Point2D[] polygon() {
        /*
        We build a star by using alternate points between an inner and outer circle
         */
        double cx = center.getX();
        double cy = center.getY();
        double angle0 = 2 * Math.PI / spikes;
        int npoints = this.spikes * 2;
        Point2D[] poly = new Point2D[npoints];
        IntStream.range(0, spikes).forEachOrdered(i -> {
            double outAngle = angle0 * i;
            double inAngle = angle0 / 2 + angle0 * i;
            Point2D outerPoint = new Point2D(cx + outerRadius * Math.sin(outAngle), cy + outerRadius * Math.cos(outAngle));
            Point2D innerPoint = new Point2D(cx + innerRadius * Math.sin(inAngle), cy + innerRadius * Math.cos(inAngle));
            poly[2 * i] = outerPoint;
            poly[2 * i + 1] = innerPoint;
        });
        return poly;
    }



    public void render(GraphicsContext gc) {
        Point2D[] poly = this.polygon();
        double[] xs = new double[poly.length];
        double[] ys = new double[poly.length];
        IntStream.range(0, poly.length).forEachOrdered(i -> {
            xs[i] = poly[i].getX();
            ys[i] = poly[i].getY();
        });
        gc.setFill(Color.WHITE);
        gc.fillPolygon(xs, ys, poly.length);
    }
}
