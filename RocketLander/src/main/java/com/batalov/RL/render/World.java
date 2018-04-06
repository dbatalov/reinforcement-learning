package com.batalov.RL.render;

public class World {
    private final double GROUND_BELOW = 0.;
    public ViewPort viewPort;
    public RocketLanderView rocketLanderView;
    private Background background;
    private int numStars = 15;

    public World(double W, double H, RocketLanderView rocketLanderView) {
        viewPort = new ViewPort(0,0, W, H);
        this.rocketLanderView = rocketLanderView;
        this.background = new Background(viewPort, numStars);
    }

}
