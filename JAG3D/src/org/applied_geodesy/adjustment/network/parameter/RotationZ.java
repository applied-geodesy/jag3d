package org.applied_geodesy.adjustment.network.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class RotationZ extends AdditionalUnknownParameter {

	public RotationZ() {
		super(0.0);
	}

	public RotationZ(double r) {
		super(r);
	}
	
	@Override
	public ParameterType getParameterType() {
		return ParameterType.ROTATION_Z;
	}

	@Override
	public double getExpectationValue() {
		return 0.0; 
	}
}
