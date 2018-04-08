package com.batalov.RL.render;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Random;

public class Background {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private int numStars;
    private double radiusMin = 1;
    private double radiusMax = 8;

    public Background(int numStars) {
        this.numStars = numStars;
    }

    public void render(GraphicsContext gc, ViewPort vp) {
        Rectangle2D vp_box = vp.bbox();
        //log.info("Background render {} {} {}", vp.x_min, vp.y_min, vp_box);
        double w = vp_box.getWidth();
        double h = vp_box.getHeight();
        int x_i_min = (int) Math.floor(vp_box.getMinX() / w);
        int x_i_max = (int) Math.floor(vp_box.getMaxX() / w);

        int y_i_min = (int) Math.floor(vp_box.getMinY() / h);
        int y_i_max = (int) Math.floor(vp_box.getMaxY() / h);

        ArrayList<Star> stars = new ArrayList<>(numStars*4);
        for (int x_i = x_i_min; x_i <= x_i_max; ++x_i) {
            for (int y_i = y_i_min; y_i <= y_i_max; ++y_i) {
                long hash = y_i << 32 + x_i;
                double x_min = x_i * w;
                double y_min = y_i * h;
                Random random = new Random(hash);
                Random radiusRandom = new Random(hash);
                Random angleRandom = new Random(hash);
                for (int i = 0; i < numStars; ++i) {
                    double outerRadius = radiusMin + radiusRandom.nextDouble() * (radiusMax - radiusMin);
                    double innerRadius = outerRadius / 2;
                    Point2D pt = new Point2D(x_min + w * random.nextDouble(), y_min + w * random.nextDouble());
                    stars.add(new Star(vp.toPixelCoordinates(pt), 5, innerRadius, outerRadius, angleRandom.nextDouble()));
                }
            }
        }
        stars.forEach(star -> {
            star.render(gc);
        });
    }
}
