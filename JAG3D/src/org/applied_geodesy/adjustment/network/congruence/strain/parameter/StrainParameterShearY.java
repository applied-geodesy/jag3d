package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterShearY extends StrainParameter {

	public StrainParameterShearY() {
		super(0.0);
	}
	
	public StrainParameterShearY(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_SHEAR_Y;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}