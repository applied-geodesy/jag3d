package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterS13 extends StrainParameter {

	public StrainParameterS13() {
		super(0.0);
	}
	
	public StrainParameterS13(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_S13;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}
