T3

	// penalize crash (or too high) heavily, otherwise penalize for time wasted when engines are burning
	private static Double reinforcement2(final RocketLander oldState, final String actionName, final RocketLander lander) {
		if (lander.crashed() || isTooHigh(lander)) {
			return -1000.0;
		}
		else if (actionName.equals("burn")) {
			return  -1.0;
		}
		else {
			return 0.0;
		}
	}

T4

	// penalize crash (or too high) heavily, otherwise penalize for burning engines that cause the rocket to go up
	private static Double reinforcement3(final RocketLander oldState, final String actionName, final RocketLander lander) {
		if (lander.crashed() || isTooHigh(lander)) {
			return -1000.0;
		}
		else if (actionName.equals("burn") && lander.getVelocity().getY() > 0) {
			return  -1.0;
		}
		else {
			return 0.0;
		}
	}
