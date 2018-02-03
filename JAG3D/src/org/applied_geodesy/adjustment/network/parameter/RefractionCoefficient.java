package org.applied_geodesy.adjustment.network.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class RefractionCoefficient extends AdditionalUnknownParameter {

	public RefractionCoefficient() {
		super(0.0);
	}

	public RefractionCoefficient(double refCoef) {
		super(refCoef);
	}
	
	@Override
	public ParameterType getParameterType() {
		return ParameterType.REFRACTION_INDEX;
	}
	
	@Override
	public double getExpectationValue() {
		return 0.0; 
	}
}