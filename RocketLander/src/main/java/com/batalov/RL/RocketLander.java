package com.batalov.RL;


import com.batalov.RL.RNG;
import javafx.geometry.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Rocket Lander world provides 2D physics simulation of a rocket with 2 engines flying in the gravity force of a celestial body. The objective is to land the rocket ship safely.
 *
 * @author denisb
 */
public class RocketLander {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(RocketLander.class.getName());

    //	private static final Point2D GRAVITY = new Point2D.Float(0, -9.81); // Earth, m/s^2
    private static final Point2D GRAVITY = new Point2D(0, 1.625); // Moon, m/s^2
    private static final Point2D LANDED = new Point2D(0, 0); // when landed, the gravity is compensated by ground resistance

    private static final double ENGINE_ACCELERATION = -10.; // m/s^2, per single engine

    private static final double ROTATION_PER_SEC = Math.toRadians(10);

    private Point2D position = new Point2D(0, 0); // meters along X and Y from "origin"
    private Point2D velocity = new Point2D(0, 0); // vector, m/s
    private double rotation; // radians, 0 - means fully vertical, pi/2 rad (90 deg) - facing left, -pi/2 rad (-90 deg) - facing right, pi or -pi (180/-180 deg) - facing down
    private boolean burnLeft;
    private boolean burnRight;
    private boolean crashed = false;

	/*
	// to be modeled later
	private double angularVelocity;
	private final double emptyWeightKg = 100;
	private double fuel;
	*/

    public RocketLander() {
        this.reset(this.position.getX());
    }

    private void reset(final double x) {
        this.position = new Point2D(x, 0);
        this.velocity = new Point2D(0, 0);
        this.rotation = 0;
        this.setBurnLeft(false);
        this.setBurnRight(false);
    }

    public Point2D getPosition() {
        return this.position;
    }

    public Point2D getVelocity() {
        return this.velocity;
    }

    /**
     * @return rotation in radians
     */
    public double getRotation() {
        return this.rotation;
    }

    public boolean getBurnLeft() {
        return this.burnLeft;
    }

    public boolean getBurnRight() {
        return this.burnRight;
    }

    public void setBurnLeft(final boolean burnLeft) {
        this.burnLeft = burnLeft;
    }

    public void setBurnRight(final boolean burnRight) {
        this.burnRight = burnRight;
    }

    /**
     * Advances time in the simulation - computes new state of the rocket ship. If the lander crashes, the position of the rocket does not change. If it lands safely, it can take off.
     *
     * @param timeDeltaSec number of seconds (or fraction of a second) to advance.
     */
    public void tick(final double timeDeltaSec) {
        //log.info("RL pos: {}", this.position);
        //log.info(this.toString());
        if (this.crashed()) { // has side effects
            return; // state does not change after a crash
        }

        final Point2D acceleration;
        if (this.burnLeft && this.burnRight) {
            final double dualEngineAcceleration = 2 * ENGINE_ACCELERATION;
            final Point2D thrust = thrustVector(this.rotation, dualEngineAcceleration);
            acceleration = add(GRAVITY, thrust);
        } else {
            if (!this.isOnGround()) {
                acceleration = GRAVITY;
            } else {
                acceleration = LANDED;
            }
        }
        this.position = positionAfter(timeDeltaSec, this.position, this.velocity, acceleration);
        this.velocity = velocityAfter(timeDeltaSec, this.velocity, acceleration);
        this.rotation += ((burnLeft ? -1 : 0) + (burnRight ? 1 : 0)) * ROTATION_PER_SEC * timeDeltaSec;
        this.checkLanded(); // has side effects
        //log.info("*RL pos: {}", this.position);
    }

    public boolean isOnGround() {
        return this.position.getY() == 0;
    }

    public boolean checkLanded() {
        if (!this.crashed() && this.position.getY() >= 0.) {
            // straighten position and stop
            this.reset(this.position.getX());
            return true;
        } else {
            return false;
        }
    }

    public boolean crashed() {
        if (this.crashed) {
            return true;
        } else if (this.position.getY() > 0.5 &&
                (Math.abs(this.velocity.getX()) > 1.
                        || this.velocity.getY() < 3.
                        || Math.abs(this.rotation) > Math.toRadians(10))) {

            this.crashed = true;
            // crash should not result in going into ground too deep
            if (this.position.getY() > 1.) {
                this.position = new Point2D(this.position.getX(), 1); // 1 meter deep
            }
            // stop the rocket
            this.velocity = new Point2D(0, 0);
            // turn off engines
            this.setBurnLeft(false);
            this.setBurnRight(false);
            return true;
        }
        return false;
    }

    static Point2D positionAfter(final double timeDeltaSec, final Point2D position, final Point2D velocity, final Point2D acceleration) {
        final double newX = (position.getX() + velocity.getX() * timeDeltaSec + acceleration.getX() * timeDeltaSec * timeDeltaSec / 2);
        final double newY = (position.getY() + velocity.getY() * timeDeltaSec + acceleration.getY() * timeDeltaSec * timeDeltaSec / 2);
        return new Point2D(newX, newY);
    }

    static Point2D velocityAfter(final double timeDeltaSec, final Point2D velocity, final Point2D acceleration) {
        final double newX = (velocity.getX() + acceleration.getX() * timeDeltaSec);
        final double newY = (velocity.getY() + acceleration.getY() * timeDeltaSec);
        return new Point2D(newX, newY);
    }

    static Point2D add(final Point2D vec1, final Point2D vec2) {
        return new Point2D(vec1.getX() + vec2.getX(), vec1.getY() + vec2.getY());
    }

    static Point2D thrustVector(final double rotation, final double thurstAcceleration) {
        return new Point2D(thurstAcceleration * Math.sin(rotation), thurstAcceleration * Math.cos(rotation));
    }

    public String toString() {
        return String.format("< %s -- %s >, p = [%f, %f], v = [%f, %f], r = %f", (this.getBurnLeft() ? "*" : "."), (this.getBurnRight() ? "*" : "."), this.position.getX(), this.position.getY(), this.velocity.getX(), this.velocity.getY(), this.rotation);
    }

    /**
     * Simple test simulation, with random firing of engines.
     */
    public static void main(final String[] args) {
        final RocketLander lander = new RocketLander();
        for (int t = 0; t < 10000; t++) {
            System.out.println("Time t = " + t);
            lander.setBurnLeft(RNG.getRandom().nextBoolean());
            lander.setBurnRight(RNG.getRandom().nextBoolean());
            lander.tick(0.1f);
            System.out.println(lander);
            if (lander.crashed()) {
                System.out.println("CRASH!");
                break;
            }
        }
        System.out.println(lander);
    }
}
