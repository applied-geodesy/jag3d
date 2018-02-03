package org.applied_geodesy.adjustment.network.observation;

import org.applied_geodesy.adjustment.point.Point;

public class GNSSBaselineDeltaY2D extends GNSSBaseline2D {
	
	public GNSSBaselineDeltaY2D(int id, Point startPoint, Point endPoint,	double observation, double sigma) {
		super(id, startPoint, endPoint, observation, sigma);
	}

	@Override
	public double diffXs() {
		double m   = this.getScale().getValue();
		double phi = this.getRotationZ().getValue();
		return m*Math.sin(phi);
	}
	
	@Override
	public double diffYs() {
		double m   = this.getScale().getValue();
		double phi = this.getRotationZ().getValue();
		return -m*Math.cos(phi);
	}
	
	@Override
	public double diffScale() {
		double phi = this.getRotationZ().getValue();
		double dX = this.getEndPoint().getX() - this.getStartPoint().getX();
		double dY = this.getEndPoint().getY() - this.getStartPoint().getY();
		
		return Math.cos(phi)*dY - Math.sin(phi)*dX;
	}

	@Override
	public double diffRotZ() {
		double m   = this.getScale().getValue();
		double phi = this.getRotationZ().getValue();
		double dX = this.getEndPoint().getX() - this.getStartPoint().getX();
		double dY = this.getEndPoint().getY() - this.getStartPoint().getY();

		return m*(-Math.sin(phi)*dY-Math.cos(phi)*dX);
	}
	 
	@Override
	public double getValueAposteriori() {
		double m   = this.getScale().getValue();
		double phi = this.getRotationZ().getValue();
		double dX = this.getEndPoint().getX() - this.getStartPoint().getX();
		double dY = this.getEndPoint().getY() - this.getStartPoint().getY();

	    return m * (Math.cos(phi)*dY - Math.sin(phi)*dX);
	}

	@Override
	public double diffZs() {
		return 0;
	}

	@Override
	public ComponentType getComponent() {
		return ComponentType.Y;
	}
}
