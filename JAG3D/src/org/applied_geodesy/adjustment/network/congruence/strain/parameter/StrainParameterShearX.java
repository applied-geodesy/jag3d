package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterShearX extends StrainParameter {

	public StrainParameterShearX() {
		super(0.0);
	}
	
	public StrainParameterShearX(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_SHEAR_X;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}
