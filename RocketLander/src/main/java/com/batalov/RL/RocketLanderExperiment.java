package com.batalov.RL;

import com.batalov.RL.QLearningOrchestrator.*;

import java.util.*;
import java.util.logging.Logger;

public class RocketLanderExperiment {

	private static final Logger LOG = Logger.getLogger(RocketLanderExperiment.class.getName());

	private static boolean getBurnEngines(final RocketLander lander) {
		return lander.getBurnLeft(); // assume both engines are always activated together, so no need to check #getBurnRight().
	}

	/*
	 * Used for experiments where episodes end when the ship flies above a certain boundary height.
	 */
	private static final float CEILING_HEIGHT = 60.0f;
	private static boolean isTooHigh(final RocketLander lander) {
		return lander.getPosition().getY() > CEILING_HEIGHT;
	}

	/* 
	 * determines end of episode when rocket (crash)lands or flies above the boundary height.
	 */
	private static boolean isEpisodeEnd(final RocketLander lander) {
		return lander.isLandedOrCrashed() || isTooHigh(lander);
	}

	// penalize crash (or out of bounds) heavily, otherwise penalize for time wasted
	private static Double reinforcement1(final RocketLander oldState, final String actionName, final RocketLander lander) {
		return lander.crashed() || isTooHigh(lander) ? -1000.0 : -1.0;
	}

	private static String resultAsString(final RocketLander lander) {
		if (lander.crashed()) {
			return "CRASH";
		}
		else if (isTooHigh(lander)) {
			return "^^^^^";
		}
		else {
			return "LAND!";
		}
	}

	private static final String DESC_NAME_HEIGHT 	= "height";
	private static final String DESC_NAME_VELOCITY 	= "velocity";
	private static final String DESC_NAME_ENGINES 	= "engines";
	
	private static final int PRECISION_HEIGHT 	= 0;
	private static final int PRECISION_VELOCITY = 0;
	
	private static Map<String, InputDescriptor> defineInputDescriptors() {
		final Map<String, InputDescriptor> inputDescriptors = new HashMap<String, InputDescriptor>();
		inputDescriptors.put(DESC_NAME_HEIGHT,	    FixedPointInputDescriptor.newWith(-1.0f, 99.0f, PRECISION_HEIGHT));
		inputDescriptors.put(DESC_NAME_VELOCITY,	FixedPointInputDescriptor.newWith(-99f, +99f, PRECISION_VELOCITY));
		inputDescriptors.put(DESC_NAME_ENGINES, 	BooleanInputDescriptor.INSTANCE);
		return inputDescriptors;
	}
	
	private static Map<String, Input> getStateFrom(final RocketLander lander) {
		final Map<String, Input> result = new HashMap<String, Input>();
		result.put(DESC_NAME_HEIGHT,   new FixedPointInput((float)lander.getPosition().getY(), PRECISION_HEIGHT));
		result.put(DESC_NAME_VELOCITY, new FixedPointInput((float)lander.getVelocity().getY(), PRECISION_VELOCITY));
		result.put(DESC_NAME_ENGINES,  new BooleanInput(getBurnEngines(lander)));
		return result;
	}

	public static void sleep(final long millis) {
	    try {
	        Thread.sleep(millis);
        }
        catch (final InterruptedException e) {
	        LOG.warning("Unexpected Exception" + e);
        }
    }

    private static final long TICK_DURATION_MILLIS = 100;

	public static void main(String[] args) {
		// step 1. create experiment orchestrator
		final QLearningOrchestrator qlo = QLearningOrchestrator.getOrchestrator();
		LOG.info("step 1. created QLearningOrchestrator endpoint");

		// step 2.1. prepare input descriptors and other env params
		final Map<String, InputDescriptor> inputDescriptors = defineInputDescriptors();

		// TODO need another qualifier, because some episodes could end with premature "game over", but with correct behavior the episode should last forever
		final boolean isEpisodic = false; // TODO check discount factor. if not episodic, the discount factor of 1 will make the algo diverge.

		// step 2.2. specify the environment configuration that will later be used to create models
		final EnvironmentConfiguration envConfig = new EnvironmentConfiguration(inputDescriptors, isEpisodic);
		final String envConfigId = qlo.configureEnvironment(envConfig);
		LOG.info("step 2. created environment configuration");

		// step 3. create a new model using the environment configuration
		final String modelId = qlo.createModel(envConfigId);

		// step 4. configure the model
		qlo.setAlgorithmLearningRate(modelId, 1);
		qlo.setAlgorithmDiscountFactor(modelId, 1);
		LOG.info("steps 3,4. created & configured the model");

		// ===== STEP 5. initialize real environment
		// define the state of the environment at the beginning of each learning session
		final RocketLander startingLander = new RocketLander();
		startingLander.getPosition().setLocation(0, 50.0);
		// now create the actual environment
		final RocketLander lander = new RocketLander();
		// create the lander UI
		final RocketLanderFrame f = new RocketLanderFrame(lander);
		sleep(5000); // wait for the UI to initialize, TODO this is a hack - add proper waiting
		// ===== END OF STEP 5

		// ===== STEP 6. Build a list of actions
		// build the list of available actions to pass to the orchestrator - it does not change from state to state for RocketLander
		// in most general case this list will change from state to state
		final List<String> actionNames = new ArrayList<String>(Arrays.asList("off", "burn"));
		LOG.info("Available actions: " + actionNames);
		// ===== END OF STEP 6

		// ===== STEP 7. start the interaction/learning loop
		int xSession = 0;
/* commented out mechanism for stopping training after apparent convergence
		final List<Long> runTimes = new LinkedList<Long>();
		final List<Integer> stepsSinceLastChange = new LinkedList<Integer>();
		while (!didConverge(runTimes, stepsSinceLastChange)) {
*/
		while (true) {
			xSession++;
			lander.setFrom(startingLander); // reinitialize from starting state
			Map<String, Input> inputs = getStateFrom(lander);
			final String sessionId = qlo.startSession(modelId, inputs, actionNames);

//			lander.newTimeOfLastUpdate();
			// TODO add real-time vs simulated time replicable mechanism
			if (f.getRocketLanderView().isRealTime()) {
				sleep(TICK_DURATION_MILLIS);
			}

			f.getRocketLanderView().updateView();

// 			System.out.println("Table at start of episode:\n" + qlo.getModel(modelId).getAlgorithm().getTable().toString());
			Double reinforcement = null;
			while (!isEpisodeEnd(lander)) {
				final RocketLander oldState = new RocketLander(lander);
				// repeatedly choose new action and provide state transitions and reinforcement
				final String actionName = qlo.chooseAction(sessionId);

				final boolean burn = actionName.equals("burn");
//				System.out.println("burn action = " + burn);
				lander.setBurnLeft(burn);
				lander.setBurnRight(burn);
				lander.tick(TICK_DURATION_MILLIS/1000f);
				f.getRocketLanderView().updateView();

				reinforcement = reinforcement1(oldState, actionName, lander);
				inputs = getStateFrom(lander);
				final long time = qlo.newState(sessionId, inputs, actionNames, reinforcement);
				if (f.getRocketLanderView().isRealTime()) {
					System.out.println("episode = " + xSession + ", time = " + time);
					System.out.println("s  : " + oldState);
					System.out.println("a = " + actionName + ", r = " + reinforcement);
					System.out.println("s' : " + lander);
		 			System.out.println();
		 			sleep(TICK_DURATION_MILLIS);
				}

//	 			System.out.println(qlo.getModel(modelId).getAlgorithm().getTable().toString());
			}
			
			final long finalTime = qlo.getSession(sessionId).getTicks();
			final int sslc = qlo.getSession(sessionId).getStepsSinceLastChange();
			System.out.println("episode = " + xSession + ", time = " + finalTime + ", sslc = " + sslc + ", result = " + resultAsString(lander));
//          part of commented out mechanism for covergence testing, see comments above
//			runTimes.add(finalTime);
//			stepsSinceLastChange.add(sslc);
			qlo.endSession(sessionId);

			if (f.getRocketLanderView().isRealTime()) {
				sleep(1000);
			}
		}
	}
}
