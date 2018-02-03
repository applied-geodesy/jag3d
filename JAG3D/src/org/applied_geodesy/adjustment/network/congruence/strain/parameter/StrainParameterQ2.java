package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterQ2 extends StrainParameter {

	public StrainParameterQ2() {
		super(0.0);
	}
	
	public StrainParameterQ2(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_Q2;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}
