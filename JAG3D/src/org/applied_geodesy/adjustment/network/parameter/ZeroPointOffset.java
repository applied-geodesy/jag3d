package org.applied_geodesy.adjustment.network.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class ZeroPointOffset extends AdditionalUnknownParameter {

	public ZeroPointOffset() {
		this(0.0);
	}

	public ZeroPointOffset(double add) {
		super(add);
	}

	@Override
	public double getExpectationValue() {
		return 0.0; 
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.ZERO_POINT_OFFSET;
	}
}