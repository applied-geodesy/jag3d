package org.applied_geodesy.adjustment.network.observation;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.point.Point;

public abstract class GNSSBaseline2D extends GNSSBaseline {

	public GNSSBaseline2D(int id, Point startPoint, Point endPoint, double observation, double sigma) {
		super(id, startPoint, endPoint, 0, 0, observation, sigma);
	}
	
	@Override
	public int getDimension() {
		return 2;
	}
	
	@Override
	public ObservationType getObservationType() {
		return ObservationType.GNSS2D;
	}	
}
