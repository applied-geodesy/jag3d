package org.applied_geodesy.adjustment.network.observation;

import org.applied_geodesy.adjustment.point.Point;

public class GNSSBaselineDeltaX3D extends GNSSBaseline3D {
	
//	fx = m*(( Math.cos(ry)*Math.cos(rz))*dX + (Math.cos(rx)*Math.sin(rz)+Math.sin(rx)*Math.sin(ry)*Math.cos(rz))*dY + (Math.sin(rx)*Math.sin(rz)-Math.cos(rx)*Math.sin(ry)*Math.cos(rz))*dZ)
//	fy = m*((-Math.cos(ry)*Math.sin(rz))*dX + (Math.cos(rx)*Math.cos(rz)-Math.sin(rx)*Math.sin(ry)*Math.sin(rz))*dY + (Math.sin(rx)*Math.cos(rz)+Math.cos(rx)*Math.sin(ry)*Math.sin(rz))*dZ)
//	fz = m*(  Math.sin(ry)*dX - Math.sin(rx)*Math.cos(ry)*dY + Math.cos(rx)*Math.cos(ry)*dZ)

	public GNSSBaselineDeltaX3D(int id, Point startPoint, Point endPoint, double observation, double sigma) {
		super(id, startPoint, endPoint, observation, sigma);
	}
	
	@Override
	public double diffXs() {
		double m   = this.getScale().getValue();
		double ry  = this.getRotationY().getValue();
		double rz  = this.getRotationZ().getValue();
		
		return -m*Math.cos(ry)*Math.cos(rz);
	}
	
	@Override
	public double diffYs() {
		double m   = this.getScale().getValue();
		double rx  = this.getRotationX().getValue();
		double ry  = this.getRotationY().getValue();
		double rz  = this.getRotationZ().getValue();

		return m*(-Math.cos(rx)*Math.sin(rz)-Math.sin(rx)*Math.sin(ry)*Math.cos(rz));
	}
	
	@Override
	public double diffZs() {
		double m   = this.getScale().getValue();
		double rx  = this.getRotationX().getValue();
		double ry  = this.getRotationY().getValue();
		double rz  = this.getRotationZ().getValue();
		
		return m*(-Math.sin(rx)*Math.sin(rz)+Math.cos(rx)*Math.sin(ry)*Math.cos(rz));
	}
	
	@Override
	public double diffRotX() {
		double m   = this.getScale().getValue();
		double rx  = this.getRotationX().getValue();
		double ry  = this.getRotationY().getValue();
		double rz  = this.getRotationZ().getValue();
		
		double dY = this.getEndPoint().getY() - this.getStartPoint().getY();
		double dZ = this.getEndPoint().getZ() + this.getEndPointHeight() - this.getStartPoint().getZ() - this.getStartPointHeight();
		
		return m*((-Math.sin(rx)*Math.sin(rz)+Math.cos(rx)*Math.sin(ry)*Math.cos(rz))*dY+(Math.cos(rx)*Math.sin(rz)+Math.sin(rx)*Math.sin(ry)*Math.cos(rz))*dZ);
	}
	
	@Override
	public double diffRotY() {
		double m   = this.getScale().getValue();
		double rx  = this.getRotationX().getValue();
		double ry  = this.getRotationY().getValue();
		double rz  = this.getRotationZ().getValue();
		
		double dX = this.getEndPoint().getX() - this.getStartPoint().getX();
		double dY = this.getEndPoint().getY() - this.getStartPoint().getY();
		double dZ = this.getEndPoint().getZ() + this.getEndPointHeight() - this.getStartPoint().getZ() - this.getStartPointHeight();
		
		return m*(-Math.sin(ry)*Math.cos(rz)*dX+Math.sin(rx)*Math.cos(ry)*Math.cos(rz)*dY-Math.cos(rx)*Math.cos(ry)*Math.cos(rz)*dZ);
	}
	
	@Override
	public double diffRotZ() {
		double m   = this.getScale().getValue();
		double rx  = this.getRotationX().getValue();
		double ry  = this.getRotationY().getValue();
		double rz  = this.getRotationZ().getValue();
		
		double dX = this.getEndPoint().getX() - this.getStartPoint().getX();
		double dY = this.getEndPoint().getY() - this.getStartPoint().getY();
		double dZ = this.getEndPoint().getZ() + this.getEndPointHeight() - this.getStartPoint().getZ() - this.getStartPointHeight();
		
		return m*(-Math.cos(ry)*Math.sin(rz)*dX+(Math.cos(rx)*Math.cos(rz)-Math.sin(rx)*Math.sin(ry)*Math.sin(rz))*dY+(Math.sin(rx)*Math.cos(rz)+Math.cos(rx)*Math.sin(ry)*Math.sin(rz))*dZ);
	}

	@Override
	public double diffScale() {
		double rx  = this.getRotationX().getValue();
		double ry  = this.getRotationY().getValue();
		double rz  = this.getRotationZ().getValue();
		
		double dX = this.getEndPoint().getX() - this.getStartPoint().getX();
		double dY = this.getEndPoint().getY() - this.getStartPoint().getY();
		double dZ = this.getEndPoint().getZ() + this.getEndPointHeight() - this.getStartPoint().getZ() - this.getStartPointHeight();
		
		return Math.cos(ry)*Math.cos(rz)*dX + (Math.cos(rx)*Math.sin(rz)+Math.sin(rx)*Math.sin(ry)*Math.cos(rz))*dY + (Math.sin(rx)*Math.sin(rz)-Math.cos(rx)*Math.sin(ry)*Math.cos(rz))*dZ;
	}
	 
	@Override
	public double getValueAposteriori() {
		double m   = this.getScale().getValue();
		double rx  = this.getRotationX().getValue();
		double ry  = this.getRotationY().getValue();
		double rz  = this.getRotationZ().getValue();
		
		double dX = this.getEndPoint().getX() - this.getStartPoint().getX();
		double dY = this.getEndPoint().getY() - this.getStartPoint().getY();
		double dZ = this.getEndPoint().getZ() + this.getEndPointHeight() - this.getStartPoint().getZ() - this.getStartPointHeight();
		
		return m*( Math.cos(ry)*Math.cos(rz)*dX + (Math.cos(rx)*Math.sin(rz)+Math.sin(rx)*Math.sin(ry)*Math.cos(rz))*dY + (Math.sin(rx)*Math.sin(rz)-Math.cos(rx)*Math.sin(ry)*Math.cos(rz))*dZ);
	}

	@Override
	public ComponentType getComponent() {
		return ComponentType.X;
	}
}
