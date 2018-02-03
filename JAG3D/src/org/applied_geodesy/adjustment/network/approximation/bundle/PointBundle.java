package org.applied_geodesy.adjustment.network.approximation.bundle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point1D;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point2D;
import org.applied_geodesy.adjustment.network.approximation.bundle.point.Point3D;
import org.applied_geodesy.adjustment.network.approximation.bundle.transformation.TransformationParameterSet;

public class PointBundle {
	private List<Point> pointArrayList = new ArrayList<Point>();
	private Map<String,Point> pointHashMap = new LinkedHashMap<String,Point>();
	private TransformationParameterSet transParameter = new TransformationParameterSet();
	private int dim = -1;
	private boolean isIntersection = false;
	
	public PointBundle(int dim) {
		this(dim, false);
	}
	
	public PointBundle(Point p) {
		this(p, false);
	}
	  
	public PointBundle(int dim, boolean isIntersection) {
		this.dim = dim;
		this.isIntersection = isIntersection;
	}
	
	public PointBundle(Point p, boolean isIntersection) {
		this.addPoint(p);
		this.isIntersection = isIntersection;
	}
	
	public int size() {
		return this.pointArrayList.size();
	}
	
	public boolean addPoint(Point p) {
		String pointId = p.getName();
		int dim = p.getDimension();
		if (this.dim < 0)
			this.dim = dim;

		if (this.dim != dim)
			return false;

		if (this.pointHashMap.containsKey( pointId )) {
			Point pointInGroup = this.pointHashMap.get( pointId );
			pointInGroup.join(p);
		}
		else {
			this.add(p);
		}
		return true;
	}
	
	public void removePoint(Point p) {
		this.pointHashMap.remove(p.getName());
		this.pointArrayList.remove(p);
	}
	
	private void add(Point p) {
		this.pointHashMap.put(p.getName(), p);
		this.pointArrayList.add(p);
	}

	public Point get( int index ) throws ArrayIndexOutOfBoundsException{
		return this.pointArrayList.get( index );
	}

	public Point get( String pointId ) {
		return this.pointHashMap.get( pointId );
	}
	
	public final int getDimension() {
		return this.dim;
	}
	
	public TransformationParameterSet getTransformationParameterSet() {
		return this.transParameter;
	}
	
	public void setTransformationParameterSet(TransformationParameterSet transParameter) {
		this.transParameter = transParameter;
	}
	
	public Point getCenterPoint() {
		double 	x = 0.0,
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
	
	public void setIntersection(boolean isIntersection) {
		this.isIntersection = this.dim > 1 && isIntersection;
	}
	
	public boolean isIntersection() {
		return this.dim > 1 && this.isIntersection;
	}
	
	public boolean contains(String pointId) {
		return this.pointHashMap.containsKey(pointId);
	}
}
