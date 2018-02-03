package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterS23 extends StrainParameter {

	public StrainParameterS23() {
		super(0.0);
	}
	
	public StrainParameterS23(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_S23;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}
