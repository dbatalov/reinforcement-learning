package com.batalov.RL;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ActionSelectionPolicyTest {

	static class TestQAction extends QAction {

		private final int val;

		public TestQAction(final int val) {
			this.val = val;
		}

		@Override
		public int compareTo(QAction action) {
			return this.val - ((TestQAction) action).val;
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
				return this.val == ((TestQAction) obj).val;
			}
			return false;
		}

		public String toString() {
			return "<" + this.val + ">";
		}
	}

	private static final Map<QAction, Double> SAME_ACTION_VALUES;
	private static final List<TestQAction> SAME_ACTION_LIST;

	static {
		SAME_ACTION_VALUES = new HashMap<QAction, Double>();
		SAME_ACTION_LIST = new ArrayList<TestQAction>();
		for (int i = 0; i < 10; i++) {
			final TestQAction action = new TestQAction(i);
			SAME_ACTION_LIST.add(action); // remembering the order of objects
			SAME_ACTION_VALUES.put(action, new Double(0.0));
		}
	}

	@Test
	public void testBestActionNonRandom() {
		final BestActionSelectionPolicy policy = BestActionSelectionPolicy.NON_RANDOM_INSTANCE;

		final QAction action1 = policy.select(SAME_ACTION_VALUES);
		final QAction action2 = policy.select(SAME_ACTION_VALUES);
		final QAction action3 = policy.select(SAME_ACTION_VALUES);
		assertEquals(action1, SAME_ACTION_LIST.get(0));
		assertEquals(action2, SAME_ACTION_LIST.get(0));
		assertEquals(action3, SAME_ACTION_LIST.get(0));
		assertTrue("does not appear deterministric", action1.equals(action2) && action1.equals(action3));

		System.out.println("Deterministic Action Selection");
		System.out.println("selected " + action1);
		System.out.println("selected " + action2);
		System.out.println("selected " + action3);
	}

	@Test
	public void testBestActionRandom() {
		final BestActionSelectionPolicy policy = BestActionSelectionPolicy.RANDOMIZED_INSTANCE;

		final QAction action1 = policy.select(SAME_ACTION_VALUES);
		final QAction action2 = policy.select(SAME_ACTION_VALUES);
		final QAction action3 = policy.select(SAME_ACTION_VALUES);
		assertFalse("does not appear random", action1.equals(action2) && action1.equals(action3));

		System.out.println("Random Action Selection");
		System.out.println("selected " + action1);
		System.out.println("selected " + action2);
		System.out.println("selected " + action3);
	}

    private static final Map<QAction, Double> SOFTMAX_ACTION_VALUES;
    private static final List<TestQAction> SOFTMAX_ACTION_LIST;

    static {
        SOFTMAX_ACTION_VALUES = new HashMap<QAction, Double>();
        SOFTMAX_ACTION_LIST = new ArrayList<TestQAction>();

        final double[] values = new double[] {3, 1, 1, 1};
        for (int i = 0; i < values.length; i++) {
            final TestQAction action = new TestQAction(i);
            SOFTMAX_ACTION_LIST.add(action);
            SOFTMAX_ACTION_VALUES.put(action, values[i]);
        }
    }

    @Test
	public void testSoftmax() {
		final QActionSelectionPolicy qasp = SoftmaxActionSelectionPolicy.DEFAULT_INSTANCE;

		final int[] histogram = new int[SOFTMAX_ACTION_LIST.size()];

		double total = 0;
		for (int n = 0; n < 10000; n++) {
		    final TestQAction selectedAction = (TestQAction)qasp.select(SOFTMAX_ACTION_VALUES);
            histogram[selectedAction.val]++;
            total++;
        }

        System.out.println("Observed action distribution:");
		for (final Map.Entry<QAction, Double> entry: SOFTMAX_ACTION_VALUES.entrySet()) {
		    System.out.println(entry + " = " + histogram[((TestQAction)entry.getKey()).val]/total);
        }

        assertEquals(0.711, histogram[0]/total, 0.05);
        assertEquals(0.096, histogram[1]/total, 0.05);
        assertEquals(0.096, histogram[2]/total, 0.05);
        assertEquals(0.096, histogram[3]/total, 0.05);
	}
}