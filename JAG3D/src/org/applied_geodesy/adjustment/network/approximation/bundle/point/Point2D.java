package org.applied_geodesy.adjustment.network.approximation.bundle.point;

import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.transformation.Transformation2D;

public class Point2D extends Point {

	public Point2D (String id, double x, double y) {
		super(id);
		this.setX(x);
		this.setY(y);
	}

	@Override
	public final int getDimension() {
		return 2;
	}

	public void addObservedPoint(String id, double dir, double dist2d) {
		if (id.equals(this.getName())) {
			System.err.println(this.getClass().getSimpleName()+" Fehler, Punkt kann sich nicht selbst beobachten! " + this.getName() + "  "+ id);
			return;
		}
		PointBundle currentBundle = this.getCurrentBundle();
		Point2D p = ClassicGeodeticComputation.POLAR(this, id, dir, dist2d);
		currentBundle.addPoint(p);
	}

	public void addObservedPoint(String id, double dir, double dist3d, double ihDist, double thDist, double zenith, double ihZenith, double thZenith) {
		if (id.equals(this.getName())) {
			System.err.println(this.getClass().getSimpleName()+" Fehler, Punkt kann sich nicht selbst beobachten! " + this.getName() + "  "+ id);
			return;
		}
		double slopeDist = ClassicGeodeticComputation.SLOPEDISTANCE(dist3d, ihDist, thDist, zenith, ihZenith, thZenith);
		PointBundle currentBundle = this.getCurrentBundle();
		Point2D p = ClassicGeodeticComputation.POLAR(this, id, dir, slopeDist, zenith);
		currentBundle.addPoint(p);
	}

	@Override
	public Transformation2D getTransformation(PointBundle b1, PointBundle b2) {
		return new Transformation2D(b1, b2);
	}

	@Override
	public String toString() {
		return this.getName()+"  "+this.getX()+"    "+this.getY()+" ";
	}
}

