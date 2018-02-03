package org.applied_geodesy.adjustment.network.parameter;

import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.point.Point;

/**
 * Klasse ist eine Huelle fuer die Y-Komponente. Die statistischen groessen werden 
 * in der X-Komponente abgespeichert, da beide (X und Y) als ein Objekt zu interpretieren sind.
 *
 */
public class DeflectionY extends Deflection {
	
	public DeflectionY(Point point) {
		super(point);
	}
	
	public DeflectionY(Point point, double value) {
		super(point, value);
	}
	
	public DeflectionY(Point point, double value, double std) {
		super(point, value, std);
	}
	
	@Override
	public ParameterType getParameterType() {
		return ParameterType.DEFLECTION_Y;
	}
}
