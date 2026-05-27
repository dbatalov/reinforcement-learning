package com.batalov.RL;

import javax.swing.JFrame;

import com.batalov.RL.FlappyBird.BirdState;

public class FlappyBirdFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8206226511237504195L;
	private final FlappyBirdView flappyBirdView;
	
	public FlappyBirdFrame(final BirdState state) {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.flappyBirdView = new FlappyBirdView(state);
		this.add(this.flappyBirdView);
		this.pack();
		this.setVisible(true);
	}
	
	public FlappyBirdView getFlappyBirdView() {
		return this.flappyBirdView;
	}

	public static void main(final String[] args) {
		new FlappyBirdFrame(new BirdState());
	}
}
