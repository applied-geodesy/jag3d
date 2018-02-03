package org.applied_geodesy.adjustment.network.congruence.strain.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class StrainParameterTranslationZ extends StrainParameter {

	public StrainParameterTranslationZ() {
		super(0.0);
	}
	
	public StrainParameterTranslationZ(double value0) {
		super(value0);
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.STRAIN_TRANSLATION_Z;
	}
	
	@Override
	public double getExpectationValue() {
		return 0;
	}
}