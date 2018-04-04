package com.batalov.RL;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * {@link HashMap} based implementation of {@link QTable}.
 * @author denisb
 *
 */
public class HashMapQTable extends AbstractQTable {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(HashMapQTable.class.getName());
	
	private final Map<QState, Map<QAction, Double>> table;
	
	public HashMapQTable()	{
		this.table = new HashMap<QState, Map<QAction, Double>>();
	}

	/* (non-Javadoc)
	 * @see com.batalov.RL.QTableInterface#getValue(com.batalov.RL.QState, com.batalov.RL.QAction)
	 */
	@Override
	public double getValue(final QState state, final QAction action) {
		final Map<QAction, Double> actionValues = this.table.get(state);
		if (actionValues != null) {
			final Double value = actionValues.get(action);
			if (value != null) {
				return value;
			}
		}
		return DEFAULT_VALUE;
	}

	/* (non-Javadoc)
	 * @see com.batalov.RL.QTableInterface#setValue(com.batalov.RL.QState, com.batalov.RL.QAction, double)
	 */
	@Override
	public void setValue(final QState state, final QAction action, final double value) {
		final Map<QAction, Double> actionValues;
		if (this.table.containsKey(state)) {
			actionValues = this.table.get(state);
		}
		else {
			actionValues = new HashMap<QAction, Double>();
			this.table.put(state, actionValues);
		}	
		actionValues.put(action, value);
	}

	
	/* (non-Javadoc)
	 * @see com.batalov.RL.QTableInterface#getStoredActionValues(com.batalov.RL.QState)
	 */
	@Override
	public Map<QAction, Double> getStoredActionValues(final QState state) {
		final Map<QAction, Double> actionValuesStored = this.table.get(state);
		if (actionValuesStored == null)
			return new HashMap<QAction, Double>();
		else
			return new HashMap<QAction, Double>(actionValuesStored);
	}

	/*
	 * (non-Javadoc)
	 * For debugging purposes
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (final Map.Entry<QState, Map<QAction, Double>> entry: this.table.entrySet()) {
			sb.append(entry.getKey());
			sb.append("->[ ");
			for (final Map.Entry<QAction, Double> actionValue: entry.getValue().entrySet()) {
				sb.append(actionValue.getKey());
				sb.append(":");
				sb.append(actionValue.getValue());
				sb.append(" ");
			}
			sb.append("]\n");
		}
		return sb.toString();
	}

}
