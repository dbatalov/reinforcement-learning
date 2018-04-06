package com.batalov.RL;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Star {
    int cx;
    int cy;
    int spikes;
    int innerRadius;
    int outerRadius;

    public Star(int cx, int cy, int spikes, int innerRadius, int outerRadius) {
        this.cx = cx;
        this.cy = cy;
        this.spikes = spikes;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
    }

    public Point2D[] polygon() {
        /*
        We build a star by using alternate points between an inner and outer circle
         */
        Point2D center = new Point2D(cx, cy);
        Point2D topTip = new Point2D(cx + outerRadius, cy);
        double angle0 = 2*Math.PI / spikes;
        int npoints = this.spikes * 2;
        Point2D[] poly = new Point2D[npoints];
        IntStream.range(0, spikes).forEachOrdered(i -> {
            double outAngle = angle0 * i;
            double inAngle = angle0/2 + angle0 * i;
            Point2D outerPoint = new Point2D(cx + outerRadius * Math.sin(outAngle), cy + outerRadius * Math.cos(outAngle));
            Point2D innerPoint = new Point2D(cx + innerRadius * Math.sin(inAngle), cy + innerRadius * Math.cos(inAngle));
            poly[2*i] = outerPoint;
            poly[2*i+1] = innerPoint;
        });
        return poly;
    }

    public void draw(GraphicsContext gc) {
        Point2D[] poly = this.polygon();
        double[] xs = new double[poly.length];
        double[] ys = new double[poly.length];
        IntStream.range(0, poly.length).forEachOrdered(i -> {
            xs[i] = poly[i].getX();
            ys[i] = poly[i].getY();
        });
        gc.strokePolygon(xs, ys, poly.length);
    }
}
