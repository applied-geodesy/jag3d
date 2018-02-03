package org.applied_geodesy.adjustment.network.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;

public class Scale extends AdditionalUnknownParameter {

	public Scale() {
		super(1.0);
	}

	public Scale(double scale) {
		super(scale);
	}
	
	@Override
	public ParameterType getParameterType() {
		return ParameterType.SCALE;
	}

	@Override
	public double getExpectationValue() {
		return 1.0; 
	}
}
