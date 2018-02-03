package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterTranslationX extends StrainParameter {

	public StrainParameterTranslationX() {
		super(0.0);
	}
	
	public StrainParameterTranslationX(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_TRANSLATION_X;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}