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

package org.applied_geodesy.adjustment.network.approximation.bundle.point;

import java.util.ArrayList;
import java.util.List;

import org.applied_geodesy.adjustment.network.approximation.bundle.PointBundle;
import org.applied_geodesy.adjustment.network.approximation.bundle.transformation.Transformation;
import org.applied_geodesy.adjustment.network.approximation.bundle.transformation.TransformationParameterType;

public abstract class Point {
    // Punktnummer
	private String name;
    // Spalte in Designmatrix; -1 entspricht nicht gesetzt
	private int colInJacobiMatrix = -1;
    // Zeile in Designmatrix; -1 entspricht nicht gesetzt
	private int rowInJacobiMatrix = -1;
	private boolean isOutlier = false;

	private List<PointBundle> bundles = new ArrayList<PointBundle>();

	private double coordinates[] = new double[this.getDimension()];
	private double weigths[] = new double[this.getDimension()];

	public Point(String name) throws IllegalArgumentException {
		if (name == null || name.trim().length()==0)
			throw new IllegalArgumentException(this.getClass() + " Punktnummer ungueltig!");
		this.name = name.trim();
		for (int i=0; i<this.getDimension(); i++) 
			this.weigths[i] = 1.0;
	}

	public String getName() {
		return this.name;
	}

	public abstract int getDimension();

	public void setWeightedX(double px) {
		this.weigths[0] = px;
	}

	public void setWeightedY(double py) {
		this.weigths[1] = py;
	}

	public void setWeightedZ(double pz) {
		this.weigths[this.getDimension()-1] = pz;
	}

	public double getWeightedX() {
		return this.weigths[0];
	}

	public double getWeightedY() {
		return this.weigths[1];
	}

	public double getWeightedZ() {
		return this.weigths[this.getDimension()-1];
	}

	public void setX(double x) {
		this.coordinates[0] = x;
	}

	public void setY(double y) {
		this.coordinates[1] = y;
	}

	public void setZ(double z) {
		this.coordinates[this.getDimension()-1] = z;
	}

	public double getX() {
		return this.coordinates[0];
	}

	public double getY() {
		return this.coordinates[1];
	}

	public double getZ() {
		return this.coordinates[this.getDimension()-1];
	}

	public void join(Point p) {
		if (this.name.equals(p.getName()) && this.getDimension() == p.getDimension()) {
			if (this.getDimension() == 1) {
				this.setZ( 0.5*(this.getZ() + p.getZ()) );
			}
			else if (this.getDimension() == 2) {
				this.setX( 0.5*(this.getX() + p.getX()) );
				this.setY( 0.5*(this.getY() + p.getY()) );
			}
			else if (this.getDimension() == 3) {
				this.setX( 0.5*(this.getX() + p.getX()) );
				this.setY( 0.5*(this.getY() + p.getY()) );
				this.setZ( 0.5*(this.getZ() + p.getZ()) );
			}
		}
	}

	public double getDistance3D(Point p) {
	if (this.getDimension() + p.getDimension() < 6)
		throw new IllegalArgumentException("Raumstrecke nur zwischen 3D Punkten bestimmbar " +
			this.name +" " + this.getDimension() + "D und " +
			p.getName() + " " + p.getDimension() + "D");
	
		return Math.sqrt( Math.pow(this.getX()-p.getX(),2)
						+ Math.pow(this.getY()-p.getY(),2)
						+ Math.pow(this.getZ()-p.getZ(),2));
	}

	public double getDistance2D(Point p){
	if (this.getDimension() == 1 || p.getDimension() == 1)
		throw new IllegalArgumentException("Horizontalstrecke nicht mit 1D Punkte(n) bestimmbar " +
			this.name +" " + this.getDimension() + "D und " +
			p.getName() + " " + p.getDimension() + "D");
		return Math.sqrt( Math.pow(this.getX()-p.getX(),2)
						+ Math.pow(this.getY()-p.getY(),2));
	}

	public int getRowInJacobiMatrix() {
		return this.rowInJacobiMatrix;
	}

	public void setRowInJacobiMatrix(int row) {
		this.rowInJacobiMatrix = row;
	}

	public int getColInJacobiMatrix() {
		return this.colInJacobiMatrix;
	}

	public void setColInJacobiMatrix(int col) {
		this.colInJacobiMatrix = col;
	}

	public abstract Transformation getTransformation(PointBundle b1, PointBundle b2);
	
	public PointBundle getCurrentBundle() {
		if (this.bundles.size() > 0)
			return this.bundles.get(this.bundles.size()-1);
		this.addBundle();
		return this.getCurrentBundle();
	}

	public List<PointBundle> getPointBundles() {
		return this.bundles;
	}

	public void addBundle() {
		this.addBundle(false);
	}

	public void addBundle(boolean isInsersectionBundle) {
		this.bundles.add(new PointBundle(this, isInsersectionBundle));
	}

	public void removeEmptyBundles() {
		List<PointBundle> newBundles = new ArrayList<PointBundle>();
		for (int i=0; i<this.bundles.size(); i++) {
			PointBundle bundle = this.bundles.get(i);
			if (bundle.size() > 0 && !(bundle.size() == 1 && bundle.get(0) == this))
				newBundles.add(bundle);
		}
		this.bundles = newBundles;
	}

	public boolean containsPointInBundle(String pointId) {
		for (PointBundle bundle : this.bundles)
			if (bundle.contains(pointId))
				return true;
		return false;
	}

	public void joinBundles() {
		this.removeEmptyBundles();
		if (this.bundles.size() > 1) {
			Transformation trans = null;
			do {
				trans = null;
				PointBundle bundle1 = null, bundle2 = null;
				int numIdentPoints = -1;
				// permutiere ueber alle Bundle um das Optimale == meisten identischen Punkte zu ermitteln
				for (int i=0; i<this.bundles.size(); i++) {
					PointBundle b1 = this.bundles.get(i);
					for (int j=0; j<this.bundles.size(); j++) {
						if (i==j)
							continue;
						PointBundle b2 = this.bundles.get(j);
						Transformation t = this.getTransformation(b1, b2);
						
						if (t != null && t.numberOfIdenticalPoints() >= t.numberOfRequiredPoints() && t.numberOfIdenticalPoints() > numIdentPoints) {
							numIdentPoints = t.numberOfIdenticalPoints();
							bundle1 = b1;
							bundle2 = b2;
						}
					}
				}
				if (bundle1 != null && bundle2 != null) {
					if (bundle1.isIntersection() && !bundle2.isIntersection())
						trans = this.getTransformation(bundle1, bundle2);
					else if (!bundle1.isIntersection() && bundle2.isIntersection())
						trans = this.getTransformation(bundle2, bundle1);
					else if (bundle1.size() < bundle2.size())
						trans = this.getTransformation(bundle1, bundle2);
					else
						trans = this.getTransformation(bundle2, bundle1);
				}
				else
					trans = null;
				
				if (trans != null && trans.numberOfIdenticalPoints() >= trans.numberOfRequiredPoints()) {
					//trans.setFixedParameter(TransformationParameterSet.SCALE, true);
					trans.setFixedParameter(TransformationParameterType.SCALE, !bundle1.isIntersection() && !bundle2.isIntersection());
					if (trans.transformLMS()) {
						PointBundle joinedBundle = trans.getTransformdPoints();
						if (joinedBundle != null) {
							joinedBundle.setIntersection(bundle1.isIntersection() && bundle2.isIntersection());
							this.bundles.remove(bundle1);
							this.bundles.remove(bundle2);
							this.bundles.add(joinedBundle);
						}
						else
							trans = null;
					}
					else
						trans = null;
				}
				else
					trans = null;
			}
			while (trans != null);
		}
	}
	public void isOutlier(boolean isOutlier) {
		this.isOutlier = isOutlier;
	}
	public boolean isOutlier() {
		return isOutlier;
	}
}