package com.batalov.RL.render;


import com.batalov.RL.RocketLander;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class RocketLanderView extends Canvas {
    private static final Logger log = LoggerFactory.getLogger(RocketLander.class.getName());

    private Image stars;
    private Image ground;
    private Image lander;
    private Image landerCrashed;
    private Image landerFire;
    private RocketLander rocketLander;
    public static final int numStars = 40;
    private ViewPort viewPort;
    private Background background;
    public static final double pix_per_m = 10.;
    GraphicsContext gc = this.getGraphicsContext2D();

    public RocketLanderView(final RocketLander rocketLander, double W_px, double H_px) {
        super(W_px, H_px);
        this.rocketLander = rocketLander;
        ClassLoader cl = this.getClass().getClassLoader();
        URL p = cl.getResource("stars2.png");
        //this.stars = new Image(cl.getResource("stars2.png").toString());
        this.ground = new Image(cl.getResource("surface.png").toString());
        this.lander = new Image(cl.getResource("lander.png").toString());
        this.landerCrashed = new Image(cl.getResource("lander_crash.png").toString());
        this.landerFire = new Image(cl.getResource("lander_fire.png").toString());
        viewPort = new ViewPort(0., 0., W_px, H_px, pix_per_m);
        background = new Background(numStars);
        viewPort.move(-40, -30);


        //this.starGraphics = this.setScaleX(this.starGraphics, 2);
        //this.ground = this.scaleDownBy(this.ground, 2)
    }

    public void update(double time) {

    }

    public void renderGround() {
        gc.save();
        Rectangle2D bbox = viewPort.bbox();
        double w = viewPort.toPixels(bbox.getWidth());
        double h = viewPort.toPixels(1000);
        double x = viewPort.toPixels(0);
        double y = viewPort.toPixelsY(0) + lander.getHeight()/2;
        gc.setFill(Color.GRAY);
        gc.fillRect(x,y,w,h);
        Point2D topLeft = new Point2D(bbox.getMinX(), bbox.getMinY());
        Point2D botRight = new Point2D(bbox.getMaxX(),bbox.getMaxY());
        Point2D topLeft_px = viewPort.toPixelCoordinates(topLeft);
        Point2D botRight_px = viewPort.toPixelCoordinates(botRight);
        if (botRight_px.getY() > 0) {
            gc.strokeRect(botRight_px.getX(), viewPort.toPixelsY(0), topLeft_px.getX() - botRight_px.getX(), 10);
        }
        //gc.strokeRect();
        gc.restore();
    }

    public void renderLander() {
        gc.save();
        Affine tx = gc.getTransform();
        viewPort.setCenter(rocketLander.getPosition().getX(), rocketLander.getPosition().getY());
        Point2D position_ = viewPort.toPixelCoordinates(rocketLander.getPosition());
        Point2D position = new Point2D(position_.getX(), position_.getY());
        //log.info("position {}", position);
        tx.appendRotation(Math.toDegrees(-rocketLander.getRotation()), position);
        tx.appendTranslation(-this.lander.getWidth()/2, -this.lander.getHeight()/2);
        gc.setTransform(tx);
        //gc.drawImage(this.lander, position.getX(), position.getY());
        if (rocketLander.crashed())
            gc.drawImage(this.landerCrashed, position.getX(), position.getY());
        else
            gc.drawImage(this.lander, position.getX(), position.getY());

        if (rocketLander.getBurnRight()) {
            gc.save();
            Affine trans = gc.getTransform();
            trans.appendTranslation(12, 0);
            gc.setTransform(trans);
            gc.drawImage(this.landerFire, position.getX(), position.getY());
            gc.restore();
        }

        if (rocketLander.getBurnLeft()) {
            gc.save();
            Affine trans = gc.getTransform();
            trans.appendTranslation(-12, 0);
            gc.setTransform(trans);
            gc.drawImage(this.landerFire, position.getX(), position.getY());
            gc.restore();
        }

        gc.restore();
    }

    public void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());
        renderLander();
        background.render(gc, viewPort);
        renderGround();
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
        //starGraphic.render(gc);
    }


    public void move(double x, double y) {
        viewPort.move(x, y);
        //rocketLander.move()
    }

}
