package org.applied_geodesy.adjustment.network.observation;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.point.Point;

public abstract class GNSSBaseline1D extends GNSSBaseline3D {

	public GNSSBaseline1D(int id, Point startPoint, Point endPoint, double observation, double sigma) {
		super(id, startPoint, endPoint, observation, sigma);
	}

	@Override
	public int getDimension() {
		return 1;
	}
	
	@Override
	public ObservationType getObservationType() {
		return ObservationType.GNSS1D;
	}	
}
