package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterRotationX extends StrainParameter {

	public StrainParameterRotationX() {
		super(0.0);
	}
	
	public StrainParameterRotationX(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_ROTATION_X;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}