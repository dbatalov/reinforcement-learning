package com.batalov.RL;

import java.util.*;

/**
 * Randomly chooses an action, such that actions with higher values are more likely to be chosen, based on <a href="https://en.wikipedia.org/wiki/Softmax_function">softmax distribution</a>.
 * @author denisb
 */
public class SoftmaxActionSelectionPolicy implements QActionSelectionPolicy {

	public static final SoftmaxActionSelectionPolicy DEFAULT_INSTANCE = new SoftmaxActionSelectionPolicy();

	@Override
	public QAction select(final Map<QAction, Double> actionValues) {
	    double sumWeights = 0.0;
		final TreeMap<Double, QAction> softmaxWeightedActions = new TreeMap<Double, QAction>();
		for (final Map.Entry<QAction, Double> entry: actionValues.entrySet()) {
            softmaxWeightedActions.put(sumWeights, entry.getKey());
		    sumWeights += Math.exp(entry.getValue());;
        }

        final double weightedRandom = RNG.getRandom().nextDouble()*sumWeights;
		return softmaxWeightedActions.floorEntry(weightedRandom).getValue();
	}
}
