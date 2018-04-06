package com.batalov.RL;

/**
 * Subclasses implement domain-specific (for a particular world and experiment) actions that need to be hashable. Actions need to be {@link Comparable} to maintain a fixed order.
 * @author denisb
 */
public abstract class QAction implements Comparable<QAction> {
	public abstract int compareTo(QAction action);
	public abstract int hashCode();
	public abstract boolean equals(Object obj);
}
