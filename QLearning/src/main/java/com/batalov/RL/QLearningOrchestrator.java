package com.batalov.RL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.TreeMap;

public class QLearningOrchestrator {

	private static final Logger LOG = Logger.getLogger(QLearningOrchestrator.class.getName());

	public interface Input {
		public Object getValue();
	}

	public static class BooleanInput implements Input {

		private final boolean value;
		
		public BooleanInput(final boolean value) {
			this.value = value;
		}

		@Override
		public Object getValue() {
			return this.value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (this.value ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof BooleanInput)) {
				return false;
			}
			BooleanInput other = (BooleanInput) obj;
			return value == other.value;
		}

		@Override
		public String toString() {
			return this.value ? "1" : "0";
		}
	}

	public static class IntegerInput implements Input {
		
		private final int value;
		
		public IntegerInput(final int value) {
			this.value = value;
		}

		@Override
		public Object getValue() {
			return this.value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			IntegerInput other = (IntegerInput) obj;
			return this.value == other.value;
		}

		@Override
		public String toString() {
			return String.valueOf(this.value);
		}
	}

	public static class FixedPointInput extends IntegerInput {

		private final int precision;

		/**
		 * @param precision number of significant digits after the decimal point 
		 */
		public FixedPointInput(final float value, final int precision) {
			super(FixedPointInputDescriptor.floatToInt(value, precision));
			this.precision = precision;
		}

		public int getPrecision() {
			return this.precision;
		}

		@Override
		public Object getValue() {
			return Float.valueOf(this.toString());
		}

		@Override
		public String toString() {
			final String intAsStr = String.format("%0" + (this.getPrecision()+1) + "d", (int)super.getValue());
			return new StringBuilder(intAsStr).insert(intAsStr.length()-this.getPrecision(), '.').toString();
		}
	}

	public static interface InputDescriptor {
		public int getNumPossibleValues();
		public int getCode(final Object value); // TODO is int sufficient for a the space of the codes?
		public boolean matches(final Input input);
	}

	public static class BooleanInputDescriptor implements InputDescriptor {

		public static final BooleanInputDescriptor INSTANCE = new BooleanInputDescriptor();
		
		private BooleanInputDescriptor() {
		}

		@Override
		public int getNumPossibleValues() {
			return 2;
		}

		@Override
		public boolean matches(final Input input) {
			return input instanceof BooleanInput;
		}

		@Override
		public int getCode(Object value) {
			return ((Boolean)value) ? 1 : 0;
		}
	}

	public static class IntegerInputDescriptor implements InputDescriptor {
		private final int min;
		private final int max;

		public IntegerInputDescriptor() {
			this(0, Integer.MAX_VALUE-1);
		}

		// min and max must be such that the total number of possible values is not greater than Integer.MAX_VALUE
		public IntegerInputDescriptor(final int min, final int max) {
			assert min < max; // also ensures you have at least 2 possible values
			// this is different from above assertion for edge cases,
			// e.g. when min = 0 and max = Integer.MAX_VALUE 
			assert max-min > 0; 	 

			this.min = min;
			this.max = max;
		}

		public int getMin() {
			return this.min;
		}

		public int getMax() {
			return this.max;
		}

		@Override
		public int getNumPossibleValues() {
			return this.max-this.min+1;
		}

		@Override
		public boolean matches(final Input input) {
			final int value = (Integer)input.getValue();
			return input instanceof IntegerInput && value >= min && value <= max;
		}

		@Override
		public int getCode(Object value) {
			return (Integer)value;
		}
	}

	public static class FixedPointInputDescriptor extends IntegerInputDescriptor {

		private final int precision; // number of significant digits after the decimal point

		public FixedPointInputDescriptor() {
			super();
			this.precision = 0;
		}

		/**
		 * @param min smallest possible value
		 * @param max largest possible value
		 * @param precision number of significant digits after the decimal point 
		 */
		public static FixedPointInputDescriptor newWith(final float min, final float max, final int precision) {
			assert precision >= 0;
			return new FixedPointInputDescriptor(floatToInt(min, precision), floatToInt(max, precision), precision);
		}

		private static int floatToInt(final float f, final int precision) {
			final String fmt = String.format("%%.%df", precision);
			return Integer.valueOf(String.format(fmt, f).replace(".", ""));
		}

		private FixedPointInputDescriptor(final int min, final int max, final int precision) {
			super(min, max);
			this.precision = precision;
		}

		public int getPrecision() {
			return this.precision;
		}

		@Override
		public boolean matches(final Input input) {
			if (input instanceof FixedPointInput) {
				final FixedPointInput fpi = (FixedPointInput)input;
				final int value = floatToInt((float)input.getValue(), this.precision);
				return
					this.getPrecision() == fpi.getPrecision()
						&&
					value >= this.getMin()
						&&
					value <= this.getMax();
			}
			else {
				return false;
			}
		}

		@Override
		public int getCode(Object value) {
			return floatToInt((Float)value, this.precision);
		}
	}
	
	/**
	 * QState used inside the Orchestrator. Not exposed as part of the object interface. A combination of input descriptors, actual state (input) values and the list of available actions.
	 * @author denisb
	 *
	 */
	static class InputsQState extends QState {
		private final Map<String, ? extends InputDescriptor> inputDescriptors;
		private final SortedMap<String, ? extends Input> inputs;
		private final List<StringQAction> availableActions;

		public InputsQState(final Map<String, ? extends InputDescriptor> inputDescriptors, final Map<String, ? extends Input> inputs, final List<StringQAction> availableActions) {
			this.inputDescriptors = inputDescriptors;
			this.inputs = new TreeMap<String, Input>(inputs);
			this.availableActions = availableActions;
		}

		@Override
		public List<? extends QAction> getAvailableActions() {
			return Collections.unmodifiableList(new ArrayList<>(this.availableActions));
		}

		@Override
		public int hashCode() {
			int result = 0;
			int multiplier = 1;
			for (final Entry<String, ? extends Input> entry: this.inputs.entrySet()) {
				final InputDescriptor desc = this.inputDescriptors.get(entry.getKey());
				final int code = desc.getCode(entry.getValue().getValue());
				result += multiplier * code;
				multiplier *= desc.getNumPossibleValues();
			}
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof InputsQState)) {
				return false;
			}
			final InputsQState other = (InputsQState)obj;
			return
					this.inputDescriptors.equals(other.inputDescriptors)
						&&
					this.inputs.equals(other.inputs)
						&&
					this.availableActions.equals(other.availableActions);
		}

		@Override
		public String toString() {
			final StringJoiner sj = new StringJoiner(",", "[", "]");
			for (final Entry<String, ? extends Input> entry: this.inputs.entrySet()) {
				sj.add(entry.getKey() + ":" + entry.getValue().toString());
			}
			return sj.toString();
		}
	}
	
	/**
	 * QAction used inside the Orchestrator implementation. Not exposed as part of the object interface.
	 * @author denisb
	 */
	static class StringQAction extends QAction {

		private final String name;

		public StringQAction(final String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}

		@Override
		public int compareTo(QAction action) {
			return this.getName().compareTo(((StringQAction)action).getName());
		}

		@Override
		public int hashCode() {
			return this.getName().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return this.getName().equals(((StringQAction)obj).getName());
		}
		
		public String toString() {
			return this.getName();
		}
	}

	/**
	 * Used to define inputs (observable state values) and outputs (actions) for the environment (aka world) in RL.
	 * @author denisb
	 */
	public static class EnvironmentConfiguration {

		private final Map<String, InputDescriptor> inputDescriptors;
		private final boolean isEpisodic;
		
		/**
		 * 
		 * @param isEpisodic true if the RL interaction is episodic, i.e. has a natural end.
		 */
		public EnvironmentConfiguration(final Map<String, InputDescriptor> inputDescriptors, final boolean isEpisodic) {
			this.inputDescriptors = inputDescriptors;
			this.isEpisodic = isEpisodic;
		}

		public final Map<String, InputDescriptor> getInputDescriptors() {
			return this.inputDescriptors;
		}

		public final boolean isEpisodic() {
			return this.isEpisodic;
		}
	}
	
	static class Model {
		private final EnvironmentConfiguration envConfig;
		private final QLearningAlgorithm qAlgo;
		private final QActionSelectionPolicy policy;

		private Model(final EnvironmentConfiguration envConfig, final QLearningAlgorithm qAlgo) {
			this.envConfig = envConfig;
			this.qAlgo = qAlgo;
			this.policy = BestActionSelectionPolicy.DEFAULT_INSTANCE;
		}

		public QLearningAlgorithm getAlgorithm() {
			return this.qAlgo;
		}

		public EnvironmentConfiguration getEnvironmentConfiguration() {
			return this.envConfig;
		}

		public QActionSelectionPolicy getActionSelectionPolicy() {
			return this.policy;
		}
	}

	static class Session {
		private final Model model;
		private long ticks = 0;
		private InputsQState lastState = null;
		private StringQAction lastAction = null;
		// number of times update() was invoked on the algorithm without resulting in Q table change, large value indicates conversion
		private volatile int stepsSinceLastChange = 0;

		synchronized int getStepsSinceLastChange() {
			return this.stepsSinceLastChange;
		}

		Session(final Model model) {
			this.model = model;
		}

		Model getModel() {
			return this.model;
		}

		public InputsQState getLastState() {
			return this.lastState;
		}

		public void setLastState(final InputsQState lastState) {
			this.lastState = lastState;
		}

		public StringQAction getLastAction() {
			return this.lastAction;
		}

		public void setLastAction(final StringQAction lastAction) {
			this.lastAction = lastAction;
		}

		public long getTicks() {
			return this.ticks;
		}

		/**
		 * 
		 * @param newState
		 * @param resetStepsSinceLastChange
		 * @return new session ticks
		 */
		public synchronized long tick(InputsQState newState, boolean resetStepsSinceLastChange) {
			this.setLastState(newState);
			this.stepsSinceLastChange = resetStepsSinceLastChange ? 0 : this.stepsSinceLastChange + 1;
			return ++this.ticks;
		}
	}

	/**
	 * @return
	 */
	public static QLearningOrchestrator getOrchestrator() {
		return new QLearningOrchestrator();
	}
	
	private final Map<String, EnvironmentConfiguration> envConfigs = new HashMap<String, EnvironmentConfiguration>();
	private final Map<String, Model> models = new HashMap<String, Model>();
	private final Map<String, Session> sessions = new HashMap<String, Session>();

	/**
	 * @param envConfig
	 * @return
	 */
	public synchronized String configureEnvironment(final EnvironmentConfiguration envConfig) {
		final String id = "EC-" + new StringBuilder(this.envConfigs.size()).reverse(); 
		this.envConfigs.put(id, envConfig);
		return id;
	}

	/**
	 * @param envConfigId
	 * @return
	 */
	public synchronized String createModel(final String envConfigId) {

		final QTable qTable = new HashMapQTable();
		final QLearningAlgorithm qAlgo = new QLearningAlgorithm(qTable);
		final Model model = new Model(this.getEnvironmentConfiguration(envConfigId), qAlgo);

		final String id = "M-" + new StringBuilder(this.models.size()).reverse();
		this.models.put(id, model);
		return id;
	}

	private EnvironmentConfiguration getEnvironmentConfiguration(final String envConfigId) {
		return this.envConfigs.get(envConfigId);
	}

	Model getModel(final String modelId) {
		return this.models.get(modelId);
	}

	Session getSession(final String sessionId) {
		return this.sessions.get(sessionId);
	}

	/**
	 * @param modelId
	 * @param learningRate
	 */
	public void setAlgorithmLearningRate(final String modelId, final double learningRate) {
		this.getModel(modelId).getAlgorithm().setLearningRate(learningRate);
	}

	/**
	 * @param modelId
	 * @param discountFactor
	 */
	public void setAlgorithmDiscountFactor(final String modelId, final double discountFactor) {
		this.getModel(modelId).getAlgorithm().setDiscountFactor(discountFactor);
	}

	/**
	 * @param modelId
	 * @param discountFactor
	 */
	public String startSession(final String modelId, final Map<String, ? extends Input> startStateInputs, final List<String> availableActionNames) {
		final Model model = this.getModel(modelId);
		final List<StringQAction> availableActions = actionsFromNames(availableActionNames);
		final InputsQState startState = stateFromInputs(startStateInputs, model.getEnvironmentConfiguration().getInputDescriptors(), availableActions);

		final Session session = new Session(model);
		session.setLastState(startState);

		final String id = "S-" + new StringBuilder(this.sessions.size()).reverse();
		this.sessions.put(id, session);
		return id;
	}

	/**
	 * @param modelId
	 * @param discountFactor
	 */
	public void endSession(final String sessionId) {
		this.sessions.remove(sessionId);
	}

	/**
	 * @param modelId
	 * @param discountFactor
	 */
	public String chooseAction(final String sessionId) {
		final Session session = this.getSession(sessionId);
		final Model model = session.getModel();
		final StringQAction action = (StringQAction)model.getAlgorithm().selectAction(session.getLastState(), model.getActionSelectionPolicy());
		session.setLastAction(action);
		return action.getName();
	}

	/**
	 * 
	 * @param sessionId
	 * @param newStateInputs
	 * @param availableActionNames
	 * @param reinforcement
	 * @return new session ticks
	 */
	public long newState(final String sessionId, final Map<String, ? extends Input> newStateInputs, final List<String> availableActionNames, final double reinforcement) {
		final Session session = this.getSession(sessionId);
		final Model model = session.getModel();

		final List<StringQAction> availableActions = actionsFromNames(availableActionNames);
		final InputsQState newState = stateFromInputs(newStateInputs, model.getEnvironmentConfiguration().getInputDescriptors(), availableActions);
		LOG.finest("qlo.st = " + newState.toString());
		
		boolean resetStepsSinceLastChange = false;
		if (session.getLastState() != null && session.getLastAction() != null) {
			final boolean policyChanged = model.getAlgorithm().update(session.getLastState(), session.getLastAction(), newState, reinforcement);
			resetStepsSinceLastChange = policyChanged;
		}

		return session.tick(newState, resetStepsSinceLastChange);
	}
	
	private List<StringQAction> actionsFromNames(final List<String> availableActionNames) {
		final List<StringQAction> result = new ArrayList<StringQAction>(availableActionNames.size());
		for (final String name: availableActionNames) {
			result.add(new StringQAction(name));
		}
		return result;
	}

	private static void verifyInputsMatchDescriptors(final Map<String, ? extends Input> inputs, final Map<String, InputDescriptor> descriptors) {
		for (final Entry<String, ? extends Input> inputEntry: inputs.entrySet()) {
			final InputDescriptor desc = descriptors.get(inputEntry.getKey());
			if (desc == null) {
				throw new RuntimeException("Input '" + inputEntry.getKey() + "' has no corresponding descriptor");
			}
			if (!desc.matches(inputEntry.getValue())) {
				throw new RuntimeException(inputEntry.getValue() + " <- input '" + inputEntry.getKey() + "' of type '" + inputEntry.getValue().getClass() + "' does not match descriptor '" + desc.getClass() + "'");
			}
		}
	}

	private static InputsQState stateFromInputs(
			final Map<String, ? extends Input> stateInputs,
			final Map<String, InputDescriptor> inputDescriptors,
			final List<StringQAction> availableActions) {

		// verify inputs match descriptors
		verifyInputsMatchDescriptors(stateInputs, inputDescriptors);
		return new InputsQState(inputDescriptors, stateInputs, availableActions);
	}
}