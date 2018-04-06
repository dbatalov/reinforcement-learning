package com.batalov.RL.render;

import javafx.scene.canvas.GraphicsContext;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class Background {
    private int numStars;
    Star[] stars;
    private double innerRadius = 3;
    private double outerRadius = 6;
    private Random random;
    private ViewPort viewPort;
    private BBox sky;

    private int spikes = 5;

    public Background(ViewPort viewPort, int numStars) {
        this.viewPort = viewPort;
        stars = new Star[numStars];
        random = new Random();
        this.numStars = numStars;
        BBox ext = viewPort.extents;

        sky = new BBox(ext.topLeft.subtract(ext.width(), ext.height()),
                ext.bottomRight.add(ext.width(), ext.height()));

        IntStream.range(0, numStars).forEachOrdered(i -> {
            stars[i] = new Star(
                    sky.topLeft.getX() + sky.width() * random.nextDouble(),
                    sky.topLeft.getY() + sky.height() * random.nextDouble(),
                    innerRadius,
                    outerRadius,
                    spikes
            );
        });
    }

    public void render(GraphicsContext gc) {
        for(int i=0; i < numStars; ++i) {
            Star star = stars[i];
            StarGraphic starGraphic = new StarGraphic(
                    viewPort.toPixels(star.x),
                    viewPort.toPixels(star.y),
                    spikes,
                    viewPort.toPixels(innerRadius),
                    viewPort.toPixels(outerRadius));
            starGraphic.render(gc);
        }

    }

    public void move(double x, double y) {
        Arrays.stream(stars).forEach(star -> {
            star.move(x, y);
        });
        ArrayList<Integer> clipped = new ArrayList<Integer>();
        for (int i = 0; i < numStars; ++i) {
            if (!stars[i].bbox().intersects(sky)) {
                clipped.add(i);
            }
        }
        clipped.forEach(i -> {
            while (true) {
                Star star = new Star(
                        sky.topLeft.getX() + sky.width() * random.nextDouble(),
                        sky.topLeft.getY() + sky.height() * random.nextDouble(),
                        innerRadius,
                        outerRadius,
                        5);
                if (! star.bbox().intersects(viewPort.extents))
                    break;
            }
        });
    }
}
