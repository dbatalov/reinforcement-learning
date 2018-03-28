package com.batalov.RL;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Essentially argmax() if action is viewed as an index into values
 * @author denisb
 *
 */
public class BestActionSelectionPolicy implements QActionSelectionPolicy {
	/**
	 * breaks ties randomly
	 */
	public static final BestActionSelectionPolicy RANDOMIZED_INSTANCE = new BestActionSelectionPolicy(true);
	/**
	 * does not break ties, chooses the first 
	 */
	public static final BestActionSelectionPolicy NON_RANDOM_INSTANCE = new BestActionSelectionPolicy(false);
//	public static final BestActionSelectionPolicy DEFAULT_INSTANCE = RANDOMIZED_INSTANCE;
	public static final BestActionSelectionPolicy DEFAULT_INSTANCE = NON_RANDOM_INSTANCE;

	private final boolean randomized;

	private BestActionSelectionPolicy(final boolean randomized) {
		this.randomized = randomized;
	}

	@Override
	public QAction select(final Map<QAction, Double> actionValues) {
		if (this.randomized) {
			final Double maxValue = Collections.max(actionValues.values());
			final List<QAction> bestActionList = new LinkedList<QAction>();
			for (final Map.Entry<QAction, Double> entry: actionValues.entrySet()) {
				if (entry.getValue().equals(maxValue)) {
					bestActionList.add(entry.getKey());
				}
			}
			return bestActionList.get(RNG.getRandom().nextInt(bestActionList.size()));
		}
		else {
			return Collections.max(actionValues.entrySet(), (entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).getKey();
		}
	}
}