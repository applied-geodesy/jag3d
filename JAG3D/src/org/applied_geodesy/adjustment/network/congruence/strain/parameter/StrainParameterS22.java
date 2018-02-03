package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterS22 extends StrainParameter {

	public StrainParameterS22() {
		super(1.0);
	}
	
	public StrainParameterS22(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_S22;
	}
	
	@Override
	public double getExpectationValue() {
		return 1;
	}
}
