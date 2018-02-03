package org.applied_geodesy.adjustment.network.approximation.bundle.point;

import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.transformation.Transformation1D;

public class Point1D extends Point {

	public Point1D (String id, double z) {
		super(id);
		this.setZ(z);
	}

	@Override
	public final int getDimension() {
		return 1;
	}

	@Override
	public Transformation1D getTransformation(PointBundle b1, PointBundle b2) {
		return new Transformation1D(b1, b2);
	}

	public void addObservedPoint(String id, double dist3d, double ihDist, double thDist, double zenith, double ihZenith, double thZenith) {
		if (id.equals(this.getName())) {
			System.err.println(this.getClass().getSimpleName()+" Fehler, Punkt kann sich nicht selbst beobachten! " + this.getName() + "  "+ id);
		    return;
		}
		double slopeDist = ClassicGeodeticComputation.SLOPEDISTANCE(dist3d, ihDist, thDist, zenith, ihZenith, thZenith);
		PointBundle currentBundle = this.getCurrentBundle();
		Point1D p = ClassicGeodeticComputation.TRIGO_HEIGHT_3D(this, id, slopeDist, zenith, ihZenith, thZenith);
		currentBundle.addPoint(p);
	}
	
	public void addObservedPoint(String id, double dist2d, double zenith, double ihZenith, double thZenith) {
		if (id.equals(this.getName())) {
			System.err.println(this.getClass().getSimpleName()+" Fehler, Punkt kann sich nicht selbst beobachten! " + this.getName() + "  "+ id);
		    return;
		}
		PointBundle currentBundle = this.getCurrentBundle();
		Point1D p = ClassicGeodeticComputation.TRIGO_HEIGHT_2D(this, id, dist2d, zenith, ihZenith, thZenith);
		currentBundle.addPoint(p);
	}

	public void addObservedPoint(String id, double deltaH, double ih, double th) {
		if (id.equals(this.getName())) {
			System.err.println(this.getClass().getSimpleName()+" Fehler, Punkt kann sich nicht selbst beobachten! " + this.getName() + "  "+ id);
			return;
		}
		PointBundle currentBundle = this.getCurrentBundle();
		Point1D p = ClassicGeodeticComputation.ORTHO_HEIGHT(this, id, deltaH, ih, th);
		currentBundle.addPoint(p);
	}
	
	public void addObservedPoint(String id, double deltaH) {
		if (id.equals(this.getName())) {
			System.err.println(this.getClass().getSimpleName()+" Fehler, Punkt kann sich nicht selbst beobachten! " + this.getName() + "  "+ id);
			return;
		}
		PointBundle currentBundle = this.getCurrentBundle();
		Point1D p = new Point1D(id, this.getZ() + deltaH);
		currentBundle.addPoint(p);
	}

	@Override
	public String toString() {
		return this.getName()+"  [ z = "+this.getZ() +" ]";
	}
}

