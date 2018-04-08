package com.batalov.RL.render;


import com.batalov.RL.RocketLander;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.net.URL;
import java.util.stream.IntStream;

public class RocketLanderView extends Canvas {

    private Image stars;
    private Image ground;
    private RocketLander rocketLander;
    public static final int numStars = 40;
    private ViewPort viewPort;
    private Background background;
    public static final double pix_per_m = 10.;

    public RocketLanderView(final RocketLander rocketLander, double W_px, double H_px) {
        super(W_px, H_px);
        this.rocketLander = rocketLander;
        ClassLoader cl = this.getClass().getClassLoader();
        URL p = cl.getResource("stars2.png");
        this.stars = new Image(cl.getResource("stars2.png").toString());
        this.ground = new Image(cl.getResource("surface.png").toString());
        viewPort = new ViewPort(0., 0., W_px, H_px, pix_per_m);
        background = new Background(numStars);
        viewPort.move(-40,-30);


        //this.starGraphics = this.setScaleX(this.starGraphics, 2);
        //this.ground = this.scaleDownBy(this.ground, 2)
    }

    public void update(double time) {

    }

    public void render() {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0, getWidth(), getHeight());
        background.render(gc, viewPort);
        /*
        Star starGraphic = new Star(100, 100, 5, 10, 30);
        Point2D[] poly = starGraphic.polygon();
        double[] xs = new double[poly.length*2];
        IntStream.range(0, poly.length).forEachOrdered(i -> {
            xs[2*i] = poly[i].getX();
            xs[2*i+i] = poly[i].getY();
        });

        Node root = this.getScene().getRoot();
        Group g = new Group();
        Polygon polygon = new Polygon(xs);
        root.s
        */
        //gc.drawImage(this.stars,0, 0);
        //starGraphic.render(gc);
    }

    public void move(double x, double y) {
        viewPort.move(x,y);
    }

}
