package com.batalov.RL;

/**
 * Implements translation of {@link QState} to and from a string.
 * @author denisb
 */
public interface QStateMarshaller {
	public String stateToString(final QState state);
	public QState stateFromString(final String str);
}
