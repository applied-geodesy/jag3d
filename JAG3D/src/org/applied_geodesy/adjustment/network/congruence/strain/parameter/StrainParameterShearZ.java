package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterShearZ extends StrainParameter {

	public StrainParameterShearZ() {
		super(0.0);
	}
	
	public StrainParameterShearZ(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_SHEAR_Z;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}