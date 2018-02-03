package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterA12 extends StrainParameter {

	public StrainParameterA12() {
		super(0.0);
	}
	
	public StrainParameterA12(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_A12;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}
