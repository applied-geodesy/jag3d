package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterRotationZ extends StrainParameter {

	public StrainParameterRotationZ() {
		super(0.0);
	}
	
	public StrainParameterRotationZ(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_ROTATION_Z;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}