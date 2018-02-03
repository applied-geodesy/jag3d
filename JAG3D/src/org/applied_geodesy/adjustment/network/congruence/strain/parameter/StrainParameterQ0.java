package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterQ0 extends StrainParameter {

	public StrainParameterQ0() {
		super(1.0);
	}
	
	public StrainParameterQ0(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_Q0;
	}
	
	@Override
	public double getExpectationValue() {
		return 1;
	}
}
