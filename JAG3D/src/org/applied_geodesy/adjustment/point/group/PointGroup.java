package org.applied_geodesy.adjustment.point.group;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.adjustment.DefaultUncertainty;
import org.applied_geodesy.adjustment.point.Point;
import org.applied_geodesy.adjustment.point.Point1D;
import org.applied_geodesy.adjustment.point.Point2D;
import org.applied_geodesy.adjustment.point.Point3D;

public class PointGroup {
	private int id; 
	private int dimension = -1; // Keine Dimension, wenn -1

	// doppelte Speicherung, um Index- und Punktnummerabfrage 
	// einfach zu realisieren
	private Map<String,Point> pointHashMap = new LinkedHashMap<String,Point>();
	private List<Point> pointArrayList = new ArrayList<Point>();

	public PointGroup(int id) {
		this.id = id;
	}

	public final int getId() {
		return this.id;
	}

	public boolean add(Point point) {
		String name = point.getName();
		int pointDim = point.getDimension();

		if (this.dimension<0)
			this.dimension = pointDim;

		if (this.dimension != pointDim || this.pointHashMap.containsKey( name ))
			return false;

		if (this.dimension > 1) {
			point.setStdX( point.getStdX() > 0 ? point.getStdX() : DefaultUncertainty.getUncertaintyX() );
			point.setStdY( point.getStdY() > 0 ? point.getStdY() : DefaultUncertainty.getUncertaintyY() );
		}
		if (this.dimension != 2) {
			point.setStdZ( point.getStdZ() > 0 ? point.getStdZ() : DefaultUncertainty.getUncertaintyZ() );
		}

		point.getDeflectionX().setStd( point.getDeflectionX().getStd() > 0 ? point.getDeflectionX().getStd() : DefaultUncertainty.getUncertaintyDeflectionX() );
		point.getDeflectionY().setStd( point.getDeflectionY().getStd() > 0 ? point.getDeflectionY().getStd() : DefaultUncertainty.getUncertaintyDeflectionY() );

		this.pointHashMap.put( name, point );
		this.pointArrayList.add( point );

		return true;
	}

	public Point get( int index ) throws ArrayIndexOutOfBoundsException{
		return this.pointArrayList.get( index );
	}

	public Point get( String pointId ) {
		return this.pointHashMap.get( pointId );
	}

	public int size() {
		return this.pointArrayList.size();
	}

	public int getDimension() {
		return this.dimension;
	}

	@Override
	public String toString() {
		return new String(this.getClass() + " " + this.id + " Points in Group: " + this.size());
	}

	public Point getCenterPoint() {
		double x = 0.0, 
				y = 0.0, 
				z = 0.0;

		for (int i=0; i<this.size(); i++) {
			if (this.getDimension() > 1) {
				x += this.get(i).getX();
				y += this.get(i).getY();
			}
			if (this.getDimension() != 2) {
				z += this.get(i).getZ();
			}
		}

		if (this.getDimension() == 1)
			return new Point1D("c", z/this.size());

		else if (this.getDimension() == 2)
			return new Point2D("c", x/this.size(), y/this.size());

		else if (this.getDimension() == 3)
			return new Point3D("c", x/this.size(), y/this.size(), z/this.size());

		return null;
	}

}
