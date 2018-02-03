package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterScaleY extends StrainParameter {

	public StrainParameterScaleY() {
		super(1.0);
	}
	
	public StrainParameterScaleY(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_SCALE_Y;
	}
	
	@Override
	public double getExpectationValue() {
		return 1;
	}
}