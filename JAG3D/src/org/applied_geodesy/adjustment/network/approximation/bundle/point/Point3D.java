package org.applied_geodesy.adjustment.network.approximation.bundle.point;

import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.transformation.Transformation;

public class Point3D extends Point {

	public Point3D (String id, double x, double y, double z) {
		super(id);
		this.setX(x);
		this.setY(y);
		this.setZ(z);
	}

	@Override
	public final int getDimension() {
		return 2;
	}
	
	@Override
	public Transformation getTransformation(PointBundle b1, PointBundle b2) {
		return null;
	}
	
	@Override
	public String toString() {
		return this.getName()+"  [ x = "+this.getX()+" / y = "+this.getY()+" / z = " + this.getZ() + " ]";
	}
}