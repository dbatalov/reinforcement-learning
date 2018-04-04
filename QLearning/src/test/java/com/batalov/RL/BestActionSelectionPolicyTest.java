package com.batalov.RL;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class BestActionSelectionPolicyTest {

	static class TestQAction extends QAction {

		private final int val;

		public TestQAction(final int val) {
			this.val = val;
		}

		@Override
		public int compareTo(QAction action) {
			return this.val - ((TestQAction)action).val;
		}

		@Override
		public int hashCode() {
			return val;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof TestQAction) {
				return this.val == ((TestQAction)obj).val;
			}
			return false;
		}
		
		public String toString() {
			return "<" + this.val + ">";
		}
	}

	private static final Map<QAction, Double> ACTION_VALUES;
	private static final List<TestQAction> ACTION_LIST;
	static {
		ACTION_VALUES = new HashMap<QAction, Double>();
		ACTION_LIST = new ArrayList<TestQAction>();
		for (int i = 0; i < 10; i++) {
			final TestQAction action = new TestQAction(i);
			ACTION_LIST.add(action); // remembering the order of objects
			ACTION_VALUES.put(action, new Double(0.0));	
		};
	}

	@Test
	public void testNonRandom() {
		final BestActionSelectionPolicy policy = BestActionSelectionPolicy.NON_RANDOM_INSTANCE;

		final QAction action1 = policy.select(ACTION_VALUES);
		final QAction action2 = policy.select(ACTION_VALUES);
		final QAction action3 = policy.select(ACTION_VALUES);
		assertEquals(action1, ACTION_LIST.get(0));
		assertEquals(action2, ACTION_LIST.get(0));
		assertEquals(action3, ACTION_LIST.get(0));
		assertTrue("does not appear deterministric", action1.equals(action2) && action1.equals(action3));

		System.out.println("Deterministic Action Selection");
		System.out.println("selected " + action1);
		System.out.println("selected " + action2);
		System.out.println("selected " + action3);
	}

	@Test
	public void testRandom() {
		final BestActionSelectionPolicy policy = BestActionSelectionPolicy.RANDOMIZED_INSTANCE;

		final QAction action1 = policy.select(ACTION_VALUES);
		final QAction action2 = policy.select(ACTION_VALUES);
		final QAction action3 = policy.select(ACTION_VALUES);
		assertFalse("does not appear random", action1.equals(action2) && action1.equals(action3));

		System.out.println("Random Action Selection");
		System.out.println("selected " + action1);
		System.out.println("selected " + action2);
		System.out.println("selected " + action3);
	}
}
