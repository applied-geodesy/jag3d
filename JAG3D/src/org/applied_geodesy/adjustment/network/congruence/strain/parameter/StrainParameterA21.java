package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterA21 extends StrainParameter {

	public StrainParameterA21() {
		super(0.0);
	}
	
	public StrainParameterA21(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_A21;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}
