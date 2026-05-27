package com.batalov.RL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.batalov.RL.FlappyBird.BirdAction;
import com.batalov.RL.FlappyBird.BirdAction.BirdActionMarshaller;
import com.batalov.RL.FlappyBird.BirdReinforcementSimple;
import com.batalov.RL.FlappyBird.BirdState;
import com.batalov.RL.FlappyBird.BirdState.BirdStateMarshaller;
import com.batalov.RL.FlappyBird.BirdReinforcementTargetHeight;
import com.batalov.RL.QLearningOrchestrator.EnvironmentConfiguration;
import com.batalov.RL.QLearningOrchestrator.FixedPointInputDescriptor;
import com.batalov.RL.QLearningOrchestrator.Input;
import com.batalov.RL.QLearningOrchestrator.InputDescriptor;
import com.batalov.RL.QLearningOrchestrator.FixedPointInput;

public class FlappyBirdTargetHeightExperiment {

	static final Logger log = Logger.getLogger(FlappyBirdTargetHeightExperiment.class.getName());
	
	private static Map<String, Input> birdStateToServiceInputs(final BirdState state) {
		final Map<String, Input> result = new HashMap<String, Input>();
		result.put("height", new FixedPointInput(state.getHeight(), 1));
		result.put("velocity", new FixedPointInput(state.getVelocity(), 1));
		result.put("targetHeight", new FixedPointInput(state.getTargetHeight(), 1));
		return result;
	}

	public static void main(String[] args) {
		// step 1. get experiment orchestrator
		final QLearningOrchestrator qlo = QLearningOrchestrator.getOrchestrator();
		log.info("step 1. created QLearningOrchestrator");

		// step 2.1. prepare input descriptors and other env params
		final Map<String, InputDescriptor> inputDescriptors = new HashMap<String, InputDescriptor>();
		inputDescriptors.put("height", FixedPointInputDescriptor.newWith(-1.0f, +4.0f, 1));
		inputDescriptors.put("velocity", FixedPointInputDescriptor.newWith(-9.9f, +9.9f, 1));
		inputDescriptors.put("targetHeight", FixedPointInputDescriptor.newWith(0.0f, +4.0f, 1));

		// TODO need another qualifier, e.g. if the bird dies the episode ends, but in theory the bird could fly forever
		final boolean isEpisodic = false; // TODO check discount factor. if not episodic, the discount factor of 1 will make the algo diverge.

		// step 2.2. specify the environment configuration that will later be used to create models
		final EnvironmentConfiguration envConfig = new EnvironmentConfiguration(inputDescriptors, isEpisodic);
		final String envConfigId = qlo.configureEnvironment(envConfig);
		log.info("step 2. created environment configuration");

		// step 3. create a new model using the environment configuration
		final String modelId = qlo.createModel(envConfigId);

		// step 4. configure the model
		qlo.setAlgorithmLearningRate(modelId, 1.0);
		qlo.setAlgorithmDiscountFactor(modelId, 0.1);
		log.info("steps 3,4. created & configured the model");

		// ===== STEP 5. initialize real environment
		final BirdStateMarshaller stateMarshaller = new BirdStateMarshaller();
		final BirdActionMarshaller actionMarshaller = new BirdActionMarshaller();

		final BirdState start = new BirdState();
		final FlappyBirdFrame f = new FlappyBirdFrame(new BirdState(start));
		// ===== END OF STEP 5

		// ===== STEP 6. Build a list of actions
		// in most general case this list will change from state to state
		final List<? extends QAction> allActions = start.getAvailableActions();
		// build the list of available actions to pass to the orchestrator - it does not change from state to state
		final List<String> actionNames = new ArrayList<String>();
		for (final QAction action: allActions) {
			final String actionName = actionMarshaller.actionToString(action);
			actionNames.add(actionName);
		}
		log.info("Available actions: " + actionNames);
		// ===== END OF STEP 6

		// ===== STEP 7. start the interaction/learning loop
		final BirdReinforcementTargetHeight reinforcementFunction = new BirdReinforcementTargetHeight(f.getFlappyBirdView().getTargetHeight());
//		final BirdReinforcementSimple reinforcementFunction = new BirdReinforcementSimple();
		
		int xSession = 0;
		final List<Long> runTimes = new LinkedList<Long>();
		final List<Integer> stepsSinceLastChange = new LinkedList<Integer>();
//		while (!didConverge(runTimes, stepsSinceLastChange)) {
		while (true) {
			xSession++;
			final BirdState state = f.getFlappyBirdView().getBird();
			state.resetFrom(start);
			Map<String, Input> serviceInputs = birdStateToServiceInputs(state);
			final String sessionId = qlo.startSession(modelId, serviceInputs, actionNames);
			state.newTimeOfLastUpdate();
			if (f.getFlappyBirdView().isRealTime()) {
				FlappyBird.sleepTimeStep();
			}
			f.getFlappyBirdView().updateView(false);

// 			System.out.println("Table at start of episode:\n" + qlo.getModel(modelId).getAlgorithm().getTable().toString());
			Double reinforcement = null;
			while (!state.isDead()) {
				final BirdState oldState = new BirdState(state);
				// repeatedly choose new action and provide state transitions and reinforcement
				final String actionName = qlo.chooseAction(sessionId);

				final BirdAction action = actionMarshaller.actionFromString(actionName);
				state.applyAction(action, f.getFlappyBirdView().isRealTime());
				f.getFlappyBirdView().updateView(action.isFlap());

				reinforcement = reinforcementFunction.reinforcement(oldState, action, state);
				serviceInputs = birdStateToServiceInputs(state);
				final long time = qlo.newState(sessionId, serviceInputs, actionNames, reinforcement);

				System.out.println("t = " + time + ": s = " + stateMarshaller.stateToString(oldState) + ", a = " + actionName + ", r = " + reinforcement + ", s' = " + stateMarshaller.stateToString(state));
	 			System.out.println();
//	 			System.out.println(qlo.getModel(modelId).getAlgorithm().getTable().toString());

				reinforcementFunction.setTargetHeight(state.getTargetHeight());

				if (f.getFlappyBirdView().isRealTime()) {
					FlappyBird.sleepTimeStep();
				}
			}
			
//			start.setTargetHeight(state.getTargetHeight());
			final long finalTime = qlo.getSession(sessionId).getTicks();
			System.out.println("session = " + xSession + ", time = " + finalTime);
			runTimes.add(finalTime);
			stepsSinceLastChange.add(qlo.getSession(sessionId).getStepsSinceLastChange());
			System.out.println();
			qlo.endSession(sessionId);
			if (f.getFlappyBirdView().isRealTime()) {
				FlappyBird.sleep(1000);
			}
		}
	}
}
