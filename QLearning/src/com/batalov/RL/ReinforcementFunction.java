package com.batalov.RL;

/**
 * Defines the shape of the reinforcement function which needs to be implemented for a specific world. Several functions for different experiments may exist.
 * @author denisb
 */
public interface ReinforcementFunction {
	/**
	 * @return the reinforcement for transitioning from oldState to newState after performing the specified action.
	 */
	public double reinforcement(final QState oldState, final QAction action, final QState newState);
}
