package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterA11 extends StrainParameter {

	public StrainParameterA11() {
		super(1.0);
	}
	
	public StrainParameterA11(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_A11;
	}

	@Override
	public double getExpectationValue() {
		return 1;
	}
}
