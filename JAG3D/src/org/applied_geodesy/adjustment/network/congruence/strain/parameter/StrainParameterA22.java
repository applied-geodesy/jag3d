package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterA22 extends StrainParameter {

	public StrainParameterA22() {
		super(1.0);
	}
	
	public StrainParameterA22(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_A22;
	}
	
	@Override
	public double getExpectationValue() {
		return 1;
	}
}
