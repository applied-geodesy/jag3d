package org.applied_geodesy.adjustment.network.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class RotationX extends AdditionalUnknownParameter {

	public RotationX() {
		super(0.0);
	}

	public RotationX(double r) {
		super(r);
	}
	
	@Override
	public ParameterType getParameterType() {
		return ParameterType.ROTATION_X;
	}

	@Override
	public double getExpectationValue() {
		return 0.0; 
	}
}
