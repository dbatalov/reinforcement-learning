package com.batalov.RL;

import java.util.List;

/**
 * Subclasses implement domain-specific (for a particular world and experiment) states that need to be hashable. Each also carries the domain-specific information about which actions are possible in this state.
 * @author denisb
 */
public abstract class QState {
	public abstract List<? extends QAction> getAvailableActions();
	public abstract int hashCode();
	public abstract boolean equals(final Object state);
}
