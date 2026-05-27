package com.batalov.RL;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.batalov.RL.FlappyBird.BirdState;

public class FlappyBirdView extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3380798215766079366L;

	private Image background;
	private Image ground;
	private Image birdWingDown;
	private Image birdWingUp;
	private Image birdWingMid;
	private Image birdFalling;
	private Image rip;
	private boolean realTime = true;

	private volatile BirdState bird;
	private volatile boolean birdFlap = false;

	public FlappyBirdView(final BirdState state) {
		this.bird = state;
		try {
			this.background = ImageIO.read(FlappyBirdView.class.getResource("/background.png"));
			this.background = this.scaleDownBy(this.background, 4);
			this.ground = ImageIO.read(FlappyBirdView.class.getResource("/ground.png"));
			this.ground = this.scaleDownBy(this.ground, 4);
			this.birdWingDown = ImageIO.read(FlappyBirdView.class.getResource("/bird-wing-down.png"));
			this.birdWingDown = this.scaleDownBy(this.birdWingDown, 4);
			this.birdWingUp = ImageIO.read(FlappyBirdView.class.getResource("/bird-wing-up.png"));
			this.birdWingUp = this.scaleDownBy(this.birdWingUp, 4);
			this.birdWingMid = ImageIO.read(FlappyBirdView.class.getResource("/bird-wing-mid.png"));
			this.birdWingMid = this.scaleDownBy(this.birdWingMid, 4);
			this.birdFalling = ImageIO.read(FlappyBirdView.class.getResource("/bird-falling.png"));
			this.birdFalling = this.scaleDownBy(this.birdFalling, 4);

			this.rip = ImageIO.read(FlappyBirdView.class.getResource("/RIP-Flappy-Bird.png"));
			this.rip = this.scaleDownBy(this.rip, 2);
		} catch (final IOException ioe) {
			ioe.printStackTrace();
		}
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(final MouseEvent e) {
				final int transformedY = FlappyBirdView.this.transformY(e.getY());
				if (transformedY > 0) {
					FlappyBirdView.this.getBird().setTargetHeight(pixelsToMeters(transformedY));
					FlappyBirdView.this.getBird().getPipe().setYOpening(pixelsToMeters(transformedY));
					FlappyBirdView.this.updateView();
				} else {
					FlappyBirdView.this.switchRealTime();
				}
			}
		});
	}

	public BirdState getBird() {
		return this.bird;
	}

	public void setBird(final BirdState state) {
		this.bird = state;
	}

	public synchronized void switchRealTime() {
		this.realTime = !this.realTime;
		System.out.println("real-time = " + this.realTime);
	}

	public synchronized boolean isRealTime() {
		return this.realTime;
	}

	private Image scaleDownBy(final Image img, final int factor) {
		return img.getScaledInstance(img.getWidth(this) / factor, img.getHeight(this) / factor, Image.SCALE_DEFAULT);
	}

	public Dimension getPreferredSize() {
		return new Dimension(this.background.getWidth(this), this.background.getHeight(this));
	}

	public float getTargetHeight() {
		return this.bird.getTargetHeight();
	}

	private int transformY(final int y) {
		return Math.round(this.background.getHeight(this) * 9.0f / 10) - y;
	}

	private static int metersToPixels(final float meters) {
		return Math.round(meters * 100);
	}

	private static float pixelsToMeters(final int pixels) {
		return pixels / 100f;
	}

	public void paintComponent(final Graphics g) {

		// paint background
		if (this.background != null) {
			g.drawImage(this.background, 0, 0, this);
		}

		// draw ground
		if (this.ground != null) {
			final float fOffset = this.getBird().getGroundOffset() / 0.125f;
			final int pOffset = metersToPixels((fOffset - (float)Math.floor(fOffset)) * 0.125f);
			g.drawImage(this.ground, 0, 362, this.background.getWidth(this), 370, pOffset, 0, this.background.getWidth(this)+pOffset-1, this.ground.getHeight(this)-1, this);
		}
 		
		// draw ceiling line
		g.setColor(Color.BLACK);
		g.drawLine(0, this.transformY(metersToPixels(this.bird.getCeiling())), this.background.getWidth(this), this.transformY(metersToPixels(this.bird.getCeiling())));

		// draw pipe
		g.setColor(Color.decode("#6BBB30"));
		final int xLeft = metersToPixels(this.getBird().getPipe().getXPosition() - this.getBird().getGroundOffset() - BirdState.PIPE_WIDTH/2);
		final int yOpeningTop = this.transformY(metersToPixels(this.getBird().getPipe().getYOpening() + BirdState.PIPE_OPENING_HEIGHT/2));
		final int yOpeningBottom = this.transformY(metersToPixels(this.getBird().getPipe().getYOpening() - BirdState.PIPE_OPENING_HEIGHT/2));
		g.fillRect(xLeft, 0, metersToPixels(BirdState.PIPE_WIDTH), yOpeningTop);
		g.fillRect(xLeft, yOpeningBottom, metersToPixels(BirdState.PIPE_WIDTH), this.transformY(0)-yOpeningBottom);
		
		// draw bird with(out) tombstone
		final Image birdToDraw;
		if (this.bird.getVelocity() < -2.0f) {
			birdToDraw = this.birdFalling;
		} else if (this.birdFlap) {
			birdToDraw = this.birdWingUp;
		} else {
			birdToDraw = this.birdWingDown;
		}

		if (birdToDraw != null) {
			final int birdX = metersToPixels(BirdState.BIRD_OFFSET) - birdToDraw.getWidth(this)/2;
			final int birdY = this.transformY(Math.round(metersToPixels(this.bird.getHeight()) + birdToDraw.getHeight(this)/2));
			g.drawImage(birdToDraw, birdX, birdY, this);

			// RIP tombstone
			final int tombstoneLeftEdge = birdX + birdToDraw.getWidth(this) + 10; 
			if (this.bird.isDead() && this.rip != null) {
				g.drawImage(this.rip, tombstoneLeftEdge, this.transformY(this.rip.getHeight(this)), this);
			}
		}

		// draw target height mark
		g.setColor(Color.BLACK);
		g.fillRect(this.background.getWidth(this) - 6, this.transformY(Math.round(metersToPixels(this.bird.getTargetHeight())) + 3), 6, 6);
	}

	public void updateView() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				FlappyBirdView.this.repaint();
			}
		});
	}

	public void updateView(final BirdState state) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				FlappyBirdView.this.setBird(state);
				FlappyBirdView.this.repaint();
			}
		});
	}

	public void updateView(final boolean birdFlap) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				FlappyBirdView.this.birdFlap = birdFlap;
				FlappyBirdView.this.repaint();
			}
		});
	}

	public void updateView(final BirdState state, final boolean birdFlap) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				FlappyBirdView.this.setBird(state);
				FlappyBirdView.this.birdFlap = birdFlap;
				FlappyBirdView.this.repaint();
			}
		});
	}
}