package com.batalov.RL;

/**
 * Implements translation of {@link QAction} to and from a string.
 * @author denisb
 */
public interface QActionMarshaller {
	public String actionToString(final QAction state);
	public QAction actionFromString(final String str);
}
