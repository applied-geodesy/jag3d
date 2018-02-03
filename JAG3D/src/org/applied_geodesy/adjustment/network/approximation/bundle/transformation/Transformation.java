package org.applied_geodesy.adjustment.network.approximation.bundle.transformation;

import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point;

public interface Transformation {

	public boolean transformL2Norm();

	public boolean transformLMS();

	public double getOmega();

	public void setFixedParameter(TransformationParameterType type, boolean fixed);

	public int getDimension();

	public int numberOfIdenticalPoints();

	public int numberOfRequiredPoints();
	
	public Point transformPoint2TargetSystem(Point point);

	public Point transformPoint2SourceSystem(Point point);

	public PointBundle getTransformdPoints();

	public TransformationParameterSet getTransformationParameterSet();
}
