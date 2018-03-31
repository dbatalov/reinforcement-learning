package com.batalov.RL;

public interface Reinforcement {
	public double function(final QState oldState, final QAction action, final QState newState);
}
