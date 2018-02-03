package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterTranslationY extends StrainParameter {

	public StrainParameterTranslationY() {
		super(0.0);
	}
	
	public StrainParameterTranslationY(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_TRANSLATION_Y;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}