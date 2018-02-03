package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterScaleZ extends StrainParameter {

	public StrainParameterScaleZ() {
		super(1.0);
	}
	
	public StrainParameterScaleZ(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_SCALE_Z;
	}
	
	@Override
	public double getExpectationValue() {
		return 1;
	}
}