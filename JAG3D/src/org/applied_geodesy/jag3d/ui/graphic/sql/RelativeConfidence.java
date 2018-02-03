package org.applied_geodesy.jag3d.ui.graphic.sql;

public class RelativeConfidence extends PointPair {
	private double minorAxis = 0.0, majorAxis = 0.0, angle = 0.0;
	
	public RelativeConfidence(GraphicPoint startPoint, GraphicPoint endPoint, double majorAxis, double minorAxis, double angle, boolean significant) {
		super(startPoint, endPoint);
		this.setSignificant(significant);	
		this.majorAxis = majorAxis;
		this.minorAxis = minorAxis;
		this.angle = angle;
	}

	public double getMinorAxis() {
		return minorAxis;
	}

	public void setMinorAxis(double minorAxis) {
		this.minorAxis = minorAxis;
	}

	public double getMajorAxis() {
		return majorAxis;
	}

	public void setMajorAxis(double majorAxis) {
		this.majorAxis = majorAxis;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}
}
