package org.applied_geodesy.adjustment;

public class DefaultValue {
	private final static int MAXIMAL_ITERATIONS = 5000;
	private final static double PROBABILITY_VALUE = 0.1;
	private final static double POWER_OF_TEST = 80.0;
	private final static double ROBUST_ESTIMATION_LIMIT = 3.5;
	
	private DefaultValue() {}
	
	public static int getMaximalNumberOfIterations() {
		return MAXIMAL_ITERATIONS;
	}
	public static double getProbabilityValue() {
		return PROBABILITY_VALUE;
	}
	public static double getPowerOfTest() {
		return POWER_OF_TEST;
	}
	public static double getRobustEstimationLimit() {
		return ROBUST_ESTIMATION_LIMIT;
	}
}
