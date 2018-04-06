package com.batalov.RL;


import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.net.URL;
import javax.imageio.ImageIO;

public class RocketLanderViewFX extends Canvas {

    private Image stars;
    private Image ground;
    private RocketLander rocketLander;

    public RocketLanderViewFX(final RocketLander rocketLander, int W, int H) {
        super(W, H);
        this.rocketLander = rocketLander;
        ClassLoader cl = this.getClass().getClassLoader();
        URL p = cl.getResource("stars2.png");
        this.stars = new Image(cl.getResource("stars2.png").toString());
        this.ground = new Image(cl.getResource("surface.png").toString());
        //this.stars = this.setScaleX(this.stars, 2);
        //this.ground = this.scaleDownBy(this.ground, 2)
    }

    public void update(double time) {

    }
    public void renderSky() {
        //Instream.
    }

    public void render() {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.drawImage(this.stars,0, 0);
        Star star = new Star(100, 100, 5, 10, 30);
        star.draw(gc);
    }

}
