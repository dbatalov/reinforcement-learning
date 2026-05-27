package com.batalov.RL;

import java.util.Map;

/**
 * Defines the shape of an action selection policy used in the {@link QLearningAlgorithm#selectAction(QState, QActionSelectionPolicy)} method.
 * @see QAction
 * @author denisb
 */
public interface QActionSelectionPolicy {
	public QAction select(final Map<QAction, Double> actionValues);
}
