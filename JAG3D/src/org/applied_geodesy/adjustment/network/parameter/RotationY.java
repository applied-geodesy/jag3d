package org.applied_geodesy.adjustment.network.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class RotationY extends AdditionalUnknownParameter {

	public RotationY() {
		super(0.0);
	}

	public RotationY(double r) {
		super(r);
	}
	
	@Override
	public ParameterType getParameterType() {
		return ParameterType.ROTATION_Y;
	}

	@Override
	public double getExpectationValue() {
		return 0.0; 
	}
}
