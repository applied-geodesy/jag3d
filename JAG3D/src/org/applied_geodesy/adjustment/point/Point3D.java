package org.applied_geodesy.adjustment.point;

import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.point.Point;

public class Point3D extends Point{

	public Point3D(String id, double x, double y, double z) {
		super(id);
		this.setX(x);
		this.setY(y);
		this.setZ(z);
		
		this.coordinates0[0] = x;
		this.coordinates0[1] = y;
		this.coordinates0[2] = z;
	}

	public Point3D(String id, double x, double y, double z, double sX, double sY, double sZ) {
		this(id, x, y, z);
		
		this.setStdX(sX);
		this.setStdY(sY);
		this.setStdZ(sZ);
	}
	
	public final int getDimension() {
		return 3; 
	}

	@Override
	public String toString() {
		return new String(this.getClass() + " " + this.getName() + ": " + this.getX() + "/" + this.getY() + "/" + this.getZ());
	}
	
	@Override
	public ParameterType getParameterType() {
		return ParameterType.POINT3D;
	}
}