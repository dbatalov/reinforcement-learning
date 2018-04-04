package com.batalov.RL;

import java.util.Map;

/**
 * QTable stores the learned action {@link QAction} values for a particular state {@link QState}.
 *
 * @author denisb
 */
public interface QTable {

	double DEFAULT_VALUE = 0.0;

	public double getValue(final QState state, final QAction action);

	public double getBestValue(final QState state);

	public void setValue(final QState state, final QAction action, final double value);

	/**
	 * @param state
	 * @return the stored *and* implied action values for the specified state. Implied action values are not stored explicitly, and are typically a default of some sort.
	 */
	public Map<QAction, Double> getActionValues(final QState state);

	public String getActionValuesAsString(final QState state);

	/**
	 * @param state
	 * @return a copy of the stored actionValues map
	 */
	Map<QAction, Double> getStoredActionValues(final QState state);
}
