package com.batalov.RL;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.StringJoiner;

/**
 * A dumbed-down version of the <A href="https://en.wikipedia.org/wiki/Lights_Out_(game)">Lights Out puzzle</A>, where only the pressed light flips its state, and none of the neighbors.
 * Solving a puzzle means effectively pressing on all the lights exactly once. :) Additionally, the objective is to turn all lights on, not to turn them off.
 * 
 * @author denisb
 */
public class LightsWorld {

	/**
	 * LightsOut-specific implementation of {@link QState}
	 * @author denisb
	 */
	public static class LightsState extends QState {
		private final int numRows;
		private final int numCols;
		// TODO starting to think that BitSet is a bad representation for this class
		private final BitSet board;
	
		public static class LightsStateMarshaller implements QStateMarshaller {
	
			@Override
			public String stateToString(final QState state) {
				return state.toString();
			}
	
			@Override
			public LightsState stateFromString(final String strState) {
				final String[] rows = strState.split(",");
				int numRows = rows.length;
				int numCols = rows[0].length();
				
				final boolean[][] board = new boolean[numRows][numCols];
				for (int i = 0; i < numRows; i++) {
					final char[] chars = rows[i].toCharArray();
					for (int j = 0; j < chars.length; j++) {
						board[i][j] = chars[j] != '0';
					}
				}
				
				return new LightsState(board);
			}
			
		}

		/**
		 * Convenience method for naming consistency.
		 * @return Factory method to create a state with all lights off. 
		 */
		public static LightsState allUnLit(final int numRows, final int numCols) {
			return new LightsState(numRows, numCols);
		}

		/**
		 * @return Factory method to create a state with all lights lit. 
		 */
		public static LightsState allLit(final int numRows, final int numCols) {
			final LightsState result = allUnLit(numRows, numCols);
			result.board.flip(0, result.numRows*result.numCols); // flip all bits
			return result;
		}
	
		/**
		 * Lights puzzle with specified number of rows and columns. All lights are off.
		 * @param numRows 
		 * @param numCols
		 */
		public LightsState(final int numRows, final int numCols) {
			this.numRows = numRows;
			this.numCols = numCols;
			this.board = new BitSet(this.numRows*this.numCols); // all bits false by default
		}
		
		/**
		 * Copy constructor.
		 */
		public LightsState(final LightsState toCopy) {
			this(toCopy.getBoard());
		}
		
		/**
		 * Initialize the lights with a specific configuration.
		 * @param board 2D array of light states.
		 */
		public LightsState(final boolean[][] board) {
			this.numRows = board.length;
			this.numCols = board[0].length;
			
			this.board = new BitSet(this.numRows*this.numCols);
			int index = 0;
			for (final boolean[] row: board) {
				for (final boolean light: row) {
					if (light)
						this.board.set(index);
					else
						this.board.clear(index);
					index++;
				}
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((board == null) ? 0 : board.hashCode());
			result = prime * result + numCols;
			result = prime * result + numRows;
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
			LightsState other = (LightsState) obj;
			if (board == null) {
				if (other.board != null)
					return false;
			} else if (!board.equals(other.board))
				return false;
			if (numCols != other.numCols)
				return false;
			if (numRows != other.numRows)
				return false;
			return true;
		}
		
		public boolean[][] getBoard() {
			final boolean[][] result = new boolean[this.numRows][this.numCols];
			int index = 0;
			for (int i = 0; i < this.numRows; i++) {
				for (int j = 0; j < this.numCols; j++) {
					result[i][j] = this.board.get(index++);
				}
			}
			return result;
		}

		/**
		 * @return true if a particular light is lit
		 */
		public boolean isLit(final int row, final int col) {
			return this.board.get(row*this.numCols + col);
		}

		/**
		 * @return true if all lights are lit
		 */
		public boolean isAllLit() {
			return this.board.cardinality() == this.numRows*this.numCols;
		}

		/**
		 * Apply the specified action, i.e. press on a particular light.
		 */
		public void applyAction(final LightsWorld.LightsAction action) {
			this.board.flip(action.index());
		}
		
		public String toString() {
			final StringJoiner sj = new StringJoiner(",");
			int index = 0;
			for (int i = 0; i < this.numRows; i++) {
				final StringBuilder sb = new StringBuilder();
				for (int j = 0; j < this.numCols; j++) {
					sb.append(this.board.get(index++) ? '1' : '0');
				}
				sj.add(sb);
			}
			return sj.toString();
		}
	
		@Override
		public List<? extends QAction> getAvailableActions() {
			final List<LightsWorld.LightsAction> actions = new ArrayList<LightsWorld.LightsAction>();
			for (int i = 0; i < this.numRows; i++) {
				for (int j = 0; j < this.numCols; j++) {
					actions.add(new LightsWorld.LightsAction(this.numRows, this.numCols, i, j));
				}
			}
			return actions;
		}
	}

	public static class LightsAction extends QAction {
		private final int numRows;
		private final int numCols;
		private final int row;
		private final int col;
	
		public static class LightsActionMarshaller implements QActionMarshaller {
	
			@Override
			public String actionToString(final QAction action) {
				return action.toString();
			}
	
			@Override
			public LightsAction actionFromString(final String strAction) {
				final String[] data = strAction.replaceAll("[<>]", "").split("[x:,]");
				return new LightsAction(Integer.parseInt(data[0].trim()), Integer.parseInt(data[1].trim()), Integer.parseInt(data[2].trim()), Integer.parseInt(data[3].trim()));
			}
			
		}
	
		public LightsAction(final int numRows, final int numCols, final int row, final int col) {
			this.numRows = numRows;
			this.numCols = numCols;
			this.row = row;
			this.col = col;
		}
		
		public int getRows() {
			return this.numRows;
		}
		
		public int getCols() {
			return this.numCols;
		}
		
		public int getRow() {
			return this.row;
		}
		
		public int getCol() {
			return this.col;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + col;
			result = prime * result + numCols;
			result = prime * result + numRows;
			result = prime * result + row;
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
			LightsAction other = (LightsAction) obj;
			if (col != other.col)
				return false;
			if (numCols != other.numCols)
				return false;
			if (numRows != other.numRows)
				return false;
			if (row != other.row)
				return false;
			return true;
		}
		
		private int index() {
			return row*numCols+col;
		}
		
		public String toString() {
			return "<" + this.numRows + "x" + this.numCols + ": " + this.row + "," + this.col + ">";
		}
	
		@Override
		public int compareTo(QAction action) {
			if (this.equals(action)) {
				return 0;
			}
			else {
				return this.index() - ((LightsAction)action).index();
			}
		}
	}

	/**
	 * Container for several reinforcement functions related to Lights world.
	 * @author denisb
	 *
	 */
	public static class LightsReinforcement {
		/**
		 * Penalize every action that does not result in the success state - all lights lit. The intention is to find the shortest path to solution, wasted moves result in increased penalty.
		 */
		public static double timeWastedExceptLast(final LightsState oldState, final LightsAction action, final LightsState newState) {
			return newState.isAllLit() ? 0.0 : -1.0;
		}
		/**
		 * Same as {@link #timeWastedExceptLast(LightsState, LightsAction, LightsState)} except the success state is not distinguished in any way.
		 */
		public static double timeWasted(final LightsState oldState, final LightsAction action, final LightsState newState) {
			return -1.0;
		}
		/**
		 * Only penalize actions that turn off lights, actions that turn them on lead to the solution. Such reinforcement effectively gives away the solution with strong hints.  
		 */
		public static double turnedOffLights(final LightsState oldState, final LightsAction action, final LightsState newState) {
			return newState.isLit(action.getRow(), action.getCol()) ? 0.0 : -1.0;
		}
	}

}
