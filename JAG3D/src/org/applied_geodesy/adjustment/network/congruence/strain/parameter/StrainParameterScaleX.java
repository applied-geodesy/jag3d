package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterScaleX extends StrainParameter {

	public StrainParameterScaleX() {
		super(1.0);
	}
	
	public StrainParameterScaleX(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_SCALE_X;
	}
	
	@Override
	public double getExpectationValue() {
		return 1;
	}
}