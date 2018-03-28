package com.batalov.RL;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Base implementation o methods that are common to all {@link QTable} implementations.
 * 
 * @author denisb
 */
public abstract class AbstractQTable implements QTable {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(AbstractQTable.class.getName());

	public AbstractQTable() {
		super();
	}

	@Override
	public double getBestValue(final QState state) {
		final Map<QAction, Double> actionValues = this.getActionValues(state);
		return Collections.max(actionValues.entrySet(), (entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).getValue();
	}

	@Override
	public Map<QAction, Double> getActionValues(final QState state) {
		final Map<QAction, Double> result = this.getStoredActionValues(state);
		final List<? extends QAction> availableActions = state.getAvailableActions();
		for (final QAction action: availableActions) {
			result.putIfAbsent(action, DEFAULT_VALUE);
		}
		return result;
	}

	// TODO this implementation does not use QActionMarshaller
	@Override
	public String getActionValuesAsString(final QState state) {
		final Map<QAction, Double> actionValues = this.getActionValues(state);
		final List<Map.Entry<QAction, Double>> entries = actionValues.entrySet().stream().sorted(Map.Entry.<QAction, Double>comparingByKey()).collect(Collectors.toList());
		final StringJoiner sj = new StringJoiner(", ", "[", "]");
		for (final Map.Entry<QAction, Double> entry: entries) {
			final StringBuilder sb = new StringBuilder();
			sb.append(entry.getKey());
			sb.append(":");
			sb.append(entry.getValue());
			sj.add(sb);
		}
		return sj.toString();
	}
}