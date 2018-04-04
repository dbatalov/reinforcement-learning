package com.batalov.RL;

import java.util.Random;

/**
 * Singleton random number generator needed to ensure repeatable experiments.
 * 
 * @author denisb
 */
public class RNG {
	private static final Random rng;
	static {
		rng = new Random(123);
	}
	
	public static Random getRandom() {
		return rng;
	}
}
