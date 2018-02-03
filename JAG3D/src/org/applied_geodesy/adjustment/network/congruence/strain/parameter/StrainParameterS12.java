package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterS12 extends StrainParameter {

	public StrainParameterS12() {
		super(0.0);
	}
	
	public StrainParameterS12(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_S12;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}
