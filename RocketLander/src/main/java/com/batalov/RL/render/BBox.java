package com.batalov.RL.render;

import javafx.geometry.Point2D;

public class BBox {
    Point2D topLeft;
    Point2D bottomRight;

    public BBox(Point2D topLeft, Point2D bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    public BBox(double x0, double y0, double x1, double y1) {
        assert (x0 <= x1);
        assert (y0 <= y1);
        this.topLeft = new Point2D(x0, y0);
        this.bottomRight = new Point2D(x1, y1);
    }

    public double width() {
        return bottomRight.getX() - topLeft.getX();
    }

    public double height() {
        return bottomRight.getY() - topLeft.getY();
    }

    public boolean intersects(Point2D p) {
        return p.getX() >= topLeft.getX()
                && p.getY() >= topLeft.getY()
                && p.getX() <= bottomRight.getX()
                && p.getY() <= bottomRight.getY();
    }

    public boolean intersects(BBox bBox) {
        return intersects(bBox.topLeft) || intersects(bBox.bottomRight);
    }
}
