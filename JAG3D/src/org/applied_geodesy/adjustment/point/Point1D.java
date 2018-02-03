package org.applied_geodesy.adjustment.point;

import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.point.Point;

public class Point1D extends Point{

	public Point1D(Point3D p3d) {
		this(p3d.getName(), p3d.getZ());
		if (p3d.getStdZ() > 0.0)
			this.setStdZ( p3d.getStdZ() );
	}

	public Point1D(String id, double z, double sZ) {
		this(id, z);
		this.setStdZ(sZ);
	}

	public Point1D(String id, double z) {
		this(id, 0.0, 0.0, z);
	}

	public Point1D(String id, double x, double y, double z) {
		this(id, x, y, z, 0.0);
	}

	public Point1D(String id, double x, double y, double z, double sZ) {
		super(id);
		this.setX(x);
		this.setY(y);
		this.setZ(z);
		this.coordinates0[0] = x;
		this.coordinates0[1] = y;
		this.coordinates0[2] = z;
		this.setStdZ(sZ);
	}

	@Override
	public String toString() {
		return new String(this.getClass() + " " + this.getName() + ": " + this.getX() + "/" + this.getY() + "/" + this.getZ());
	}
	
	public final int getDimension() {
		return 1; 
	}

	@Override
	public ParameterType getParameterType() {
		return ParameterType.POINT1D;
	}
}
