package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterQ3 extends StrainParameter {

	public StrainParameterQ3() {
		super(0.0);
	}
	
	public StrainParameterQ3(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_Q3;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}
