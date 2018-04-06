package com.batalov.RL.render;

public class Star {
    public double x;
    public double y;
    public double innerRadius;
    public double outerRadius;
    public int spikes;
    public Star(double x, double y, double innerRadius, double outerRadius, int spikes) {
        this.x = x;
        this.y = y;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.spikes = spikes;
    }

    public BBox bbox() {
        return new BBox(x - outerRadius / 2, y - outerRadius / 2, x + outerRadius / 2, y + outerRadius / 2);
    }

    public void move(double x, double y) {
        this.x += x;
        this.y += y;
    }
}
