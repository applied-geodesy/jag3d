package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterS33 extends StrainParameter {

	public StrainParameterS33() {
		super(1.0);
	}
	
	public StrainParameterS33(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_S33;
	}
	
	@Override
	public double getExpectationValue() {
		return 1;
	}
}
