package com.batalov.RL;

import javafx.geometry.Point2D;
import org.junit.Test;

import java.util.Arrays;

public class StarTest {
    @Test
    public void polygonTest() {
        Star star = new Star(0,0, 5, 4, 8);
        Point2D[] poly = star.polygon();
        //System.out.println(Arrays.toString(poly));
        System.out.println(Arrays.toString(poly));
    }
}
