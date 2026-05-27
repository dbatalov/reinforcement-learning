package com.batalov.RL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.batalov.RL.FlappyBird.BirdAction.BirdActionMarshaller;
import com.batalov.RL.FlappyBird.BirdState.BirdStateMarshaller;

public class FlappyBird {

	static final Logger log = Logger.getLogger(FlappyBird.class.getName());
	
	// TODO this class no longer really needs to extend QState after introduction of QLearningOrchestrator
	public static class BirdState extends QState {
		
		private static final float FLAP_VELOCITY = 0.2f;
		private static final float FLAP_JUMP = 0.3f;
		private static final float X_VELOCITY = 0.5f;
		private static final float PIPE_OFFSET = 2.5f;
		public static final float PIPE_WIDTH = 0.3f;
		public static final float PIPE_OPENING_HEIGHT = 0.7f;
		public static final float BIRD_SIZE = 0.21f; // based on widest dimension of bird image
		public static final float BIRD_OFFSET = 0.5f; // assuming bird has a square bounding box (not quite correct)

		// Earth
		private static float G =
			 -9.81f; // Earth
//			 -1.63f; // Moon
//			-24.79f; // Jupiter

		private float height;
		private float velocity;
		private float targetHeight;
		private long timeOfLastUpdate;
		private final float ceiling;
		private final Pipe pipe;
		private float groundOffset = 0.0f;

		public static class Pipe implements Cloneable {
			private float xPosition;
			private float yOpening;

			public Pipe(final float xPosition, final float yOpening) {
				this.xPosition = xPosition;
				this.yOpening = yOpening;
			}

			float getXPosition() {
				return this.xPosition;
			}

			float getYOpening() {
				return this.yOpening;
			}
			
			void setXPosition(final float xPosition) {
				this.xPosition = xPosition;
			}

			void setYOpening(final float yOpening) {
				this.yOpening = yOpening;
			}

			public Pipe clone() {
				try {
					return (Pipe)super.clone();
				}
				catch (final CloneNotSupportedException e) {
					throw new AssertionError(e);
				}
			}
		}

		public static class BirdStateMarshaller implements QStateMarshaller {

			@Override
			public String stateToString(final QState state) {
				return state.toString();
			}

			@Override
			public BirdState stateFromString(final String strState) {
				final String[] data = strState.split(",");
				float height = Float.parseFloat(data[0]);
				float velocity = Float.parseFloat(data[1]);
				float targetHeight = Float.parseFloat(data[2]);
				return new BirdState(height, velocity, targetHeight);
			}
			
		}

		public BirdState() {
			this(3f, 0f, 1.5f);
		}

		public BirdState(final float height, final float velocity, final float targetHeight) {
			// view width is 200 pixels, corresponding to position of 2.0f, so starting with 2.5f just off the screen
			this(height, velocity, targetHeight, new Pipe(PIPE_OFFSET, targetHeight));
		}

		public BirdState(final float height, final float velocity, final float targetHeight, final Pipe pipe) {
			this.height = height;
			this.velocity = velocity;
			this.targetHeight = targetHeight;
			this.ceiling = 3.6f; // or = height; 3.6 is just the top of the window
			this.pipe = pipe;
			this.newTimeOfLastUpdate();
		}

		public BirdState(final BirdState toCopy) {
			this(toCopy.getHeight(), toCopy.getVelocity(), toCopy.getTargetHeight(), toCopy.getPipe().clone());
			this.groundOffset = toCopy.groundOffset;
			this.nextIndex = toCopy.nextIndex;
			this.timeOfLastUpdate = toCopy.timeOfLastUpdate;
		}
		
		public void resetFrom(final BirdState toCopy) {
			this.height = toCopy.height;
			this.velocity = toCopy.velocity;
			this.targetHeight = toCopy.targetHeight;
			this.pipe.setXPosition(toCopy.getPipe().getXPosition());
			this.pipe.setYOpening(toCopy.getPipe().getYOpening());
			this.groundOffset = toCopy.groundOffset;
			this.nextIndex = toCopy.nextIndex;
			this.newTimeOfLastUpdate();
		}

		public Pipe getPipe() {
			return this.pipe;
		}

		public float getVelocity() {
			return this.velocity;
		}

		public void setVelocity(final float velocity) {
			this.velocity = velocity;
		}

		public float getHeight() {
			return this.height;
		}

		public void setHeight(final float height) {
			this.height = height;
		}
		
		public float getTargetHeight() {
			return this.targetHeight;
		}

		public void setTargetHeight(final float targetHeight) {
			this.targetHeight = targetHeight;
			this.getPipe().setYOpening(targetHeight);
		}

		public float getCeiling() {
			return this.ceiling;
		}

		public float getGroundOffset() {
			return this.groundOffset;
		}

		public boolean isDead() {
			return this.getHeight() < 0f || this.isHitPipe();
		}

		public boolean isHitPipe() {
			return this.isBirdInPipe() && !this.isBirdInOpening();
		}

		public boolean isBirdInOpening() {
			final float birdBottom = this.getHeight() - BIRD_SIZE/2; 
			final float birdTop = birdBottom + BIRD_SIZE;
			final float openingBottom = this.getPipe().getYOpening() - PIPE_OPENING_HEIGHT/2;
			final float openingTop = openingBottom + PIPE_OPENING_HEIGHT;
//			log.info("ob = " + openingBottom + ", ot = " + openingTop + ", bb = " + birdBottom + ", bt = " + birdTop);
			return birdTop < openingTop && birdBottom > openingBottom;
		}
		
		public boolean isBirdInPipe() {
			final float pipeLeftEdge = this.getPipe().getXPosition() - this.getGroundOffset() - PIPE_WIDTH/2;
			final float pipeRightEdge = pipeLeftEdge + PIPE_WIDTH;
			final float birdLeftEdge = BIRD_OFFSET - BIRD_SIZE/2;
			final float birdRightEdge = birdLeftEdge + BIRD_SIZE;
//			log.info("pl = " + pipeLeftEdge + ", pr = " + pipeRightEdge + ", bl = " + birdLeftEdge + ", br = " + birdRightEdge);
			return birdRightEdge >= pipeLeftEdge && birdLeftEdge <= pipeRightEdge;
		}
/*
		public long getTimeOfLastUpdate() {
			return this.timeOfLastUpdate;
		}
*/
		public float newTimeOfLastUpdate() {
			final long timeNow = System.currentTimeMillis();
			final float timeDelta = (timeNow - this.timeOfLastUpdate)/1000f;
			this.timeOfLastUpdate = timeNow;
			return timeDelta;
		}

		private static final float TIME_STEP = 0.1f;

		public void applyAction(final BirdAction action, final boolean isRealTime) {
			float timeDelta = this.newTimeOfLastUpdate();
			if (isRealTime && timeDelta < TIME_STEP) {
				FlappyBird.sleep(Math.round(TIME_STEP - timeDelta));
			}
			timeDelta = TIME_STEP;
			this.height += this.velocity*timeDelta + G*timeDelta*timeDelta/2f;
			this.velocity += G*timeDelta;
			if (action.isFlap()) {
				this.velocity = FLAP_VELOCITY;
				this.height += FLAP_JUMP;
			}
			if (this.height > this.ceiling) {
				this.height = this.ceiling; 
				this.velocity = 0.0f;
			}

			this.updateGround(timeDelta);
//			System.out.println("applyAction: " + action.toString() + ", tDelta = " + timeDelta + ", h = " + this.height + ", v = " + this.velocity);
		}
		
		private void updateGround(final float timeDelta) {
			float newOffset = this.groundOffset + X_VELOCITY*timeDelta;
			if (newOffset > 3.0f) {
				newOffset -= 3.0f;
				this.setTargetHeight(this.nextTargetHeight());
			}
			this.groundOffset = newOffset;
		}

//		private static final List<Float> TARGET_HEIGHTS = Arrays.asList(0.5f, 1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f);
		private static final List<Float> TARGET_HEIGHTS = Arrays.asList(0.5f, 1.5f, 2.5f, 3.5f);
		private int nextIndex = 2;
		private float nextTargetHeight() {
			if (nextIndex >= TARGET_HEIGHTS.size()) {
				nextIndex = 0;
			}
			return TARGET_HEIGHTS.get(nextIndex++);
		}
		
		public String toString() {
			return String.format("%+4d,%+06.2f,%03d", (int)(this.getHeight()*100), this.getVelocity(), (int)(this.getTargetHeight()*100));
		}

		@Override
		public List<? extends QAction> getAvailableActions() {
			final List<BirdAction> actions = new ArrayList<BirdAction>();
			actions.add(BirdAction.FLAP);
			actions.add(BirdAction.DROP);
			return actions;
		}

		@Override
		public int hashCode() {
			return this.toString().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return this.toString().equals(obj.toString());
		}
	}

	public static class BirdAction extends QAction {
		public static final BirdAction DROP = new BirdAction(false);
		public static final BirdAction FLAP = new BirdAction(true);
		
		private final boolean flap;

		public static class BirdActionMarshaller implements QActionMarshaller {

			@Override
			public String actionToString(final QAction action) {
				return action.toString();
			}

			@Override
			public BirdAction actionFromString(final String strAction) {
				return strAction.equals(DROP.toString()) ? DROP : FLAP;
			}
			
		}

		private BirdAction(final boolean flap) {
			this.flap = flap;
		}
				
		public String toString() {
			return this.isFlap() ? "flap" : "drop";
		}

		public boolean isFlap() {
			return this.flap;
		}

		@Override
		public int compareTo(QAction action) {
			if (this.equals(action)) {
				return 0;
			}
			else {
				return this.isFlap() ? 1 : -1;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (flap ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BirdAction other = (BirdAction) obj;
			if (flap != other.flap)
				return false;
			return true;
		}
	}

	public static class BirdReinforcementSimple {
		public double reinforcement(final BirdState oldState, final BirdAction action, final BirdState newState) {
			return newState.isDead() ? -100000.0 : 0.0;
		}
	}

	public static class BirdReinforcementTargetHeight {
		private float targetHeight;
		public BirdReinforcementTargetHeight() {
			this(1f);
		}
		public BirdReinforcementTargetHeight(final float targetHeight) {
			this.targetHeight = targetHeight;
		}
		public float getTargetHeight() {
			return this.targetHeight;
		}
		public void setTargetHeight(final float targetHeight) {
			this.targetHeight = targetHeight;
		}
		/*
		 * TODO: do we need to use discretized height here to remove hidden states?
		 */
		public double reinforcement(final BirdState oldState, final BirdAction action, final BirdState newState) {
//			System.out.println("reinforcement(): target = " + this.getTargetHeight() + ", height = " + newState.getHeight());
			if (newState.isDead()) {
				return -100.0; // heavy penalty assuming the bird ceiling is set to 3.
			}
			else {
				return -Math.abs(newState.getHeight() - this.getTargetHeight());
			}
		}
	}

	@SuppressWarnings("unused")
	private static final boolean RESET_QTABLE = false;
	private static final boolean LEARN = true;
	private static final float INITIAL_TARGET_HEIGHT = 3f;

	// TODO move this sleep to some utilities class
	public static void sleep(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void sleepTimeStep() {
		sleep(Math.round(1000*BirdState.TIME_STEP));
	}

	@SuppressWarnings("unused")
	public static void main(final String[] args) {
		final BirdStateMarshaller stateMarshaller = new BirdState.BirdStateMarshaller();
		final BirdActionMarshaller actionMarshaller = new BirdAction.BirdActionMarshaller();
		
		final BirdState start = new BirdState();
		final FlappyBirdFrame f = new FlappyBirdFrame(new BirdState(start));
		final QActionSelectionPolicy policy = BestActionSelectionPolicy.DEFAULT_INSTANCE;
		final QTable qTable = new HashMapQTable();
//		final QTable qTable = new DynamoDBQTable(RESET_QTABLE, stateMarshaller, actionMarshaller);
		final QLearningAlgorithm ql = new QLearningAlgorithm(qTable, 1.0, 0.1);
//		final BirdReinforcementSimple reinforcementFunction = new BirdReinforcementSimple();
		final BirdReinforcementTargetHeight reinforcementFunction = new BirdReinforcementTargetHeight(Math.round(start.getTargetHeight()));
/*
		// target height in centimeters
		for (int targetH = 100; targetH <= 200; targetH++) {
			final float targetHMeters = targetH/100f;
			f.getFlappyBirdView().updateView(targetHMeters);
			start.setTargetHeight(targetHMeters);
			reinforcementFunction.setTargetHeight(Math.round(INITIAL_TARGET_HEIGHT));
			int time;
			do {
				time = 0;
				final BirdState state = new BirdState(start);
				state.newTimeOfLastUpdate();
				if (f.getFlappyBirdView().isRealTime()) {
					sleepTimeStep();
				}
				f.getFlappyBirdView().updateView(state.getHeight(), state.getVelocity(), false);
				while (time < 2000 && !state.isDead()) {
	//				System.out.println("state at t = " + time + ": " + stateMarshaller.stateToString(state));
					final BirdState oldState = new BirdState(state);
					final long t0 = System.currentTimeMillis();
					final BirdAction action = (BirdAction)ql.selectAction(oldState, policy);
					final long actionSelectionTime = System.currentTimeMillis() - t0;
					state.applyAction(action, f.getFlappyBirdView().isRealTime());
					f.getFlappyBirdView().updateView(state.getHeight(), state.getVelocity(), action.isFlap());
					final double reinforcement = reinforcementFunction.reinforcement(oldState, action, state);
	//				System.out.println("a = " + actionMarshaller.actionToString(action) + ", r = " + reinforcement + ", action selection time = " + actionSelectionTime);
	//	 			System.out.println();
					if (LEARN) {
						final long t1 = System.currentTimeMillis();
						ql.update(oldState, action, state, reinforcement);
						final long updateTime = System.currentTimeMillis() - t1;
	//					System.out.println("update time = " + updateTime);
					}
					final float newTargetHeight = f.getFlappyBirdView().getTargetHeight();
					state.setTargetHeight(newTargetHeight);
					reinforcementFunction.setTargetHeight(newTargetHeight);
					time++;
					if (f.getFlappyBirdView().isRealTime()) {
						sleepTimeStep();
					}
				}
				if (f.getFlappyBirdView().isRealTime()) {
					sleep(1000);
				}
	//			sleep(100);
				
				System.out.println("target = " + f.getFlappyBirdView().getTargetHeight() + ", time = " + time);
			} while (time < 2000);
		}
*/
		while (true) {
			int time = 0;
			final BirdState state = f.getFlappyBirdView().getBird();
			state.resetFrom(start);
			state.newTimeOfLastUpdate();
			if (f.getFlappyBirdView().isRealTime()) {
				sleepTimeStep();
			}
			f.getFlappyBirdView().updateView(false);
			while (!state.isDead()) {
				System.out.println("state at t = " + time + ": " + stateMarshaller.stateToString(state));
				final BirdState oldState = new BirdState(state);
				final long t0 = System.currentTimeMillis();
				final BirdAction action = (BirdAction)ql.selectAction(oldState, policy);
				final long actionSelectionTime = System.currentTimeMillis() - t0;
				state.applyAction(action, f.getFlappyBirdView().isRealTime());
				f.getFlappyBirdView().updateView(action.isFlap());
				final double reinforcement = reinforcementFunction.reinforcement(oldState, action, state);
				System.out.println("a = " + actionMarshaller.actionToString(action) + ", r = " + reinforcement + ", action selection time = " + actionSelectionTime);
	 			System.out.println();

				if (LEARN) {
					final long t1 = System.currentTimeMillis();
					ql.update(oldState, action, state, reinforcement);
					final long updateTime = System.currentTimeMillis() - t1;
					System.out.println("update time = " + updateTime);
				}

				reinforcementFunction.setTargetHeight(f.getFlappyBirdView().getTargetHeight());
				time++;
				if (f.getFlappyBirdView().isRealTime()) {
					sleepTimeStep();
				}
			}
			start.setTargetHeight(state.getTargetHeight());
			if (f.getFlappyBirdView().isRealTime()) {
				sleep(1000);
			}
//			sleep(100);
			
			System.out.println("target = " + f.getFlappyBirdView().getTargetHeight() + ", time = " + time);
		}
	
	}

}
