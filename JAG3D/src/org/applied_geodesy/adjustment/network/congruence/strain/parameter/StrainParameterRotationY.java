package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterRotationY extends StrainParameter {

	public StrainParameterRotationY() {
		super(0.0);
	}
	
	public StrainParameterRotationY(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_ROTATION_Y;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}