/***********************************************************************
* Copyright by Michael Loesler, https://software.applied-geodesy.org   *
*                                                                      *
* This program is free software; you can redistribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation; either version 3 of the License, or    *
* at your option any later version.                                    *
*                                                                      *
* This program is distributed in the hope that it will be useful,      *
* but WITHOUT ANY WARRANTY; without even the implied warranty of       *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
* GNU General Public License for more details.                         *
*                                                                      *
* You should have received a copy of the GNU General Public License    *
* along with this program; if not, see <http://www.gnu.org/licenses/>  *
* or write to the                                                      *
* Free Software Foundation, Inc.,                                      *
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.            *
*                                                                      *
***********************************************************************/

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
	@Override
	public String toString() {
		String str = "PointBundle = [";
		for (Point p : this.pointArrayList)
			str += p + ", ";
		str += "]";
		return str;
	}
}
