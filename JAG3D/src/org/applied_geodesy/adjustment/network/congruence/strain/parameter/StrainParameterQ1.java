package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterQ1 extends StrainParameter {

	public StrainParameterQ1() {
		super(0.0);
	}
	
	public StrainParameterQ1(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_Q1;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}
