package com.batalov.RL;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

/**
 * Implementation of the Q-learning algorithm.
 * @author denisb
 */
public class QLearningAlgorithm {
	private static final Logger LOG = Logger.getLogger(QLearningAlgorithm.class.getName());

	public static final double DEFAULT_LEARNING_RATE = 1.0; // TODO is this a reasonable default?
	public static final double DEFAULT_DISCOUNT_FACTOR = 0.1;
	
	private final QTable qTable;
	private double learningRate;
	private double discountFactor;

	/**
	 * This class supports different implementations of the Q table, hence need to provide one.
	 * 
	 * @param qTable
	 */
	public QLearningAlgorithm(final QTable qTable) {
		this(qTable, DEFAULT_LEARNING_RATE, DEFAULT_DISCOUNT_FACTOR);
	}

	public QLearningAlgorithm(final QTable qTable, final double learningRate, final double discountFactor) {
		this.qTable = qTable;
		this.learningRate = learningRate;
		this.discountFactor = discountFactor;
	}
	
	/**
	 * Update Q table after receiving the reinforcement.
	 * @param oldState the state from which the action was performed
	 * @param action the performed action
	 * @param newState the new observed state
	 * @param reinforcement the reinforcement received
	 * @return true if the Q table was actually updated. No update indicates that the value has likely converged.
	 */
	public boolean update(final QState oldState, final QAction action, final QState newState, final double reinforcement) {
		final double oldValue = this.qTable.getValue(oldState, action);
		final double learnedValue = reinforcement + this.getDiscountFactor() * this.qTable.getBestValue(newState);
		final double delta = this.getLearningRate()*(learnedValue - oldValue);
		final double newValue = oldValue + delta;
		this.qTable.setValue(oldState, action, newValue);
		final double updateMagnitude = Math.abs(delta);
		final double runningMean = this.meanUpdateMagnitude(updateMagnitude);
		LOG.finest("update mean magnitude = " + runningMean); // lowering mean is an indication of convergence....
		return newValue != oldValue;
	}

	public double getLearningRate() {
		return this.learningRate;
	}

	public void setLearningRate(final double learningRate) {
		this.learningRate = learningRate;
	}

	public double getDiscountFactor() {
		return this.discountFactor;
	}

	public void setDiscountFactor(final double discountFactor) {
		this.discountFactor = discountFactor;
	}

	/**
	 * Choose the next action to perform based on the current state and action selection policy. TODO should include policy as member rather than passing as parameter?
	 * @param state current state
	 * @param policy action selection policy
	 * @return chosen action
	 */
	public QAction selectAction(final QState state, final QActionSelectionPolicy policy) {
		final Map<QAction, Double> actionValues = this.qTable.getActionValues(state);
		LOG.finest("AV = " + this.qTable.getActionValuesAsString(state));
		return policy.select(actionValues);
	}
	
	QTable getTable() {
		return this.qTable;
	}

	/*
	 * Experimental mechanism for estimating convergence of Q values. Maintains running average over Q value deltas.
	 */
	private final Queue<Double> lastUpdateMagnitudes = new ArrayBlockingQueue<Double>(100);
	private double meanUpdateMagnitude(final double magnitude) {
		if (!this.lastUpdateMagnitudes.offer(magnitude)) {
			this.lastUpdateMagnitudes.poll();
			this.lastUpdateMagnitudes.add(magnitude);
		}
		return this.lastUpdateMagnitudes.stream().mapToDouble(a -> a).average().getAsDouble(); // TODO inefficient but readable :)
	}
}
