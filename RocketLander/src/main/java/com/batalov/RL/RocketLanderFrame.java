package com.batalov.RL;

import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Top level window displaying the simulation. Run the main() method to "play" Rocket Lander by controlling the engines as described in {@link RocketLanderView}.
 * @author denisb
 */
public class RocketLanderFrame extends JFrame {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(RocketLanderFrame.class.getName());

	private static final long serialVersionUID = 4737877752995234336L;

	private final RocketLanderView landerView;
	
	/**
	 * @param lander the model that maintains the state of the "world", this JFrame acts as a view per the MVC pattern.
	 */
	public RocketLanderFrame(final RocketLander lander) {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.landerView = new RocketLanderView(lander);
		this.landerView.setFocusable(true);
		this.add(this.landerView);
		this.pack();
		this.setVisible(true);
		this.landerView.requestFocus();
	}
	
	public RocketLanderView getRocketLanderView() {
		return this.landerView;
	}

	public static void main(final String[] args) {
		final RocketLander rl = new RocketLander();
		final RocketLanderFrame f = new RocketLanderFrame(rl);
		final Thread simulation = new Thread() {
			public void run() {
				final float timeDeltaSec = 0.1f;
				long t = 0;
				while (!rl.crashed()) {
					System.out.println("Time t = " + t);
					try {
						Thread.sleep((long)(timeDeltaSec*1000));
					}
					catch (final Exception e) {
						e.printStackTrace();
						break;
					}
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							rl.tick(timeDeltaSec);
							System.out.println(rl);
							f.repaint();
						}
					});
					t++;
				}
				System.out.println("CRASH!");
				System.out.println(rl);
			}
		};
		simulation.start();
	}
}
