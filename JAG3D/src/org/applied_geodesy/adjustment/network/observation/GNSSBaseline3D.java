package org.applied_geodesy.adjustment.network.observation;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.parameter.RotationX;
import org.applied_geodesy.adjustment.network.parameter.RotationY;
import org.applied_geodesy.adjustment.point.Point;

public abstract class GNSSBaseline3D extends GNSSBaseline {
	private RotationX rx = new RotationX();
	private RotationY ry = new RotationY();

	public GNSSBaseline3D(int id, Point startPoint, Point endPoint, double observation, double sigma) {
		super(id, startPoint, endPoint, 0, 0, observation, sigma);
	}

	public void setRotationX(RotationX r) {
		this.rx = r;
		this.rx.setObservation( this );
	}

	public void setRotationY(RotationY r) {
		this.ry = r;
		this.ry.setObservation( this );
	}

	public RotationX getRotationX() {
		return this.rx;
	}

	public RotationY getRotationY() {
		return this.ry;
	}

	@Override
	public int getColInJacobiMatrixFromRotationX() {
		return this.rx.getColInJacobiMatrix();
	}
	
	@Override
	public int getColInJacobiMatrixFromRotationY() {
		return this.ry.getColInJacobiMatrix();
	}
	
	@Override
	public int getDimension() {
		return 3;
	}
	
	@Override
	public ObservationType getObservationType() {
		return ObservationType.GNSS3D;
	}	
}