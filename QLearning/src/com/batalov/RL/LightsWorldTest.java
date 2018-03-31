package com.batalov.RL;

import java.util.List;

import com.batalov.RL.LightsWorld.LightsAction.LightsActionMarshaller;
import com.batalov.RL.LightsWorld.LightsState.LightsStateMarshaller;

/**
 * Example of executing the Q-Learning algorithm for LightsWorld with a fixed number of attempts at solving the puzzle (runs).
 * @author denisb
 *
 */
public class LightsWorldTest {

	public static void main(final String[] args) {
		final int runs = 10;
		final LightsStateMarshaller stateMarshaller = new LightsWorld.LightsState.LightsStateMarshaller();
		final LightsActionMarshaller actionMarshaller = new LightsWorld.LightsAction.LightsActionMarshaller();
//		final LightsState start = stateMarshaller.stateFromString("000,000,000"); // 3x3 board
		final LightsWorld.LightsState start = stateMarshaller.stateFromString("00,00"); // 2x2 board
		System.out.println("Start State = " + start + " < " + (!start.isAllLit() ? "not " : "") + "all lit");
		final List<? extends QAction> allActions = start.getAvailableActions();
		for (final QAction action: allActions) {
			System.out.println("Action = " + actionMarshaller.actionToString(action));
		}
		final QActionSelectionPolicy policy = BestActionSelectionPolicy.DEFAULT_INSTANCE;
		final ReinforcementFunction rf = new LightsWorld.LightsReinforcementTimeWasted();
		final QTable qTable = new HashMapQTable();
		final QLearningAlgorithm ql = new QLearningAlgorithm(qTable, 1.0, 0.1);
		for (int i = 0; i < runs; i++) {
			final LightsWorld.LightsState state = new LightsWorld.LightsState(start);
			int time = 0;
			while (!state.isAllLit()) {
				System.out.println("state at t = " + time + ": " + stateMarshaller.stateToString(state));
				final LightsWorld.LightsState oldState = new LightsWorld.LightsState(state);
				final LightsWorld.LightsAction action = (LightsWorld.LightsAction)ql.selectAction(oldState, policy);
				state.applyAction(action);
				final double reinforcement = rf.reinforcement(oldState, action, state);
				System.out.println("a = " + actionMarshaller.actionToString(action) + ", r = " + reinforcement);
	 			System.out.println();
				ql.update(oldState, action, state, reinforcement);
				// examine the state of the Q table
	 			System.out.println(qTable);
				time++;
			}
			System.out.println("run = " + i + ", time = " + time);
			System.out.println();
		}
	}
}