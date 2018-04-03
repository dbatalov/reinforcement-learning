package com.batalov.RL;

import java.awt.geom.Point2D;

import org.junit.Assert;
import org.junit.Test;

public class RocketLanderTest extends Assert {

	@Test
	public void testThrustVector() {
		final float delta = 0.0001f;
		Point2D vec;

		vec = RocketLander.thrustVector(0, 5);
		assertEquals(vec.getX(), 0f, delta);
		assertEquals(vec.getY(), 5f, delta);

		vec = RocketLander.thrustVector((float)Math.toRadians(180), 5);
		assertEquals(vec.getX(), 0f, delta);
		assertEquals(vec.getY(), -5f, delta);
		
		vec = RocketLander.thrustVector((float)Math.toRadians(-180), 5);
		assertEquals(vec.getX(), 0f, delta);
		assertEquals(vec.getY(), -5f, delta);

		vec = RocketLander.thrustVector((float)Math.toRadians(-10), 5);
		assertEquals(vec.getX(), 0.8682409f, delta);
		assertEquals(vec.getY(), 4.924039f, delta);

		vec = RocketLander.thrustVector((float)Math.toRadians(10), 5);
		assertEquals(vec.getX(), -0.8682409f, delta);
		assertEquals(vec.getY(), 4.924039f, delta);

		vec = RocketLander.thrustVector((float)Math.toRadians(100), 5);
		assertEquals(vec.getX(), -4.924039f, delta);
		assertEquals(vec.getY(), -0.86824095f, delta);

		vec = RocketLander.thrustVector((float)Math.toRadians(80), 5);
		assertEquals(vec.getX(), -4.924039f, delta);
		assertEquals(vec.getY(), 0.86824095f, delta);

		vec = RocketLander.thrustVector((float)Math.toRadians(-100), 5);
		assertEquals(vec.getX(), 4.924039f, delta);
		assertEquals(vec.getY(), -0.86824095f, delta);

		vec = RocketLander.thrustVector((float)Math.toRadians(-80), 5);
		assertEquals(vec.getX(), 4.924039f, delta);
		assertEquals(vec.getY(), 0.86824095f, delta);
	}

}
