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

package org.applied_geodesy.adjustment.geometry;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.point.Point;
import org.applied_geodesy.adjustment.geometry.restriction.Restriction;
import org.applied_geodesy.util.ObservableUniqueList;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperSymmPackMatrix;

public abstract class GeometricPrimitive implements Iterable<FeaturePoint>, Geometrizable {
	
	private class FeaturePointListChangeListener implements ListChangeListener<FeaturePoint> {
		@Override
		public void onChanged(Change<? extends FeaturePoint> change) {
			while (change.next()) {
				if (change.wasRemoved()) {
					for (FeaturePoint featurePoint : change.getRemoved()) {
						featurePoint.remove(GeometricPrimitive.this);
						featurePoint.reset();
					}
				}
				else if (change.wasAdded()) {
					for (FeaturePoint featurePoint : change.getAddedSubList()) {
						featurePoint.add(GeometricPrimitive.this);
						featurePoint.reset();
					}
				}
			}
		}
	}

	//private ObservableList<FeaturePoint> featurePointList = FXCollections.observableArrayList();
	private ObservableUniqueList<FeaturePoint> featurePointList = new ObservableUniqueList<FeaturePoint>();
	private Point centerOfMass;
	private ObjectProperty<String> name = new SimpleObjectProperty<String>(this, "name");
	private static int ID_CNT = 0;
	private ReadOnlyObjectProperty<Integer> id;
	
	public abstract Collection<UnknownParameter> getUnknownParameters();
	
	public abstract PrimitiveType getPrimitiveType();
	
	public abstract UnknownParameter getUnknownParameter(ParameterType parameterType);
	
	public boolean contains(Object object) {
		return this.getUnknownParameters().contains(object);
	}
	
	public abstract void reverseCenterOfMass(UpperSymmPackMatrix Dp);
	
	public abstract void jacobianElements(FeaturePoint point, Matrix Jx, Matrix Jv, int rowIndex);
	
	public abstract double getMisclosure(FeaturePoint point);
	
	public abstract int getDimension();
	
	public GeometricPrimitive() {
		this.id = new ReadOnlyObjectWrapper<Integer>(this, "id", ID_CNT++);
		this.featurePointList.addListener(new FeaturePointListChangeListener());
	}
	
	public final int getId() {
		return this.id.get();
	}
	
	public ReadOnlyObjectProperty<Integer> idProperty() {
		return this.id;
	}
	
	public ObservableUniqueList<FeaturePoint> getFeaturePoints() {
		return this.featurePointList;
	}
		
	public void setCenterOfMass(Point centerOfMass) {
		this.centerOfMass = centerOfMass;
	}
		
	public Point getCenterOfMass() {
		if (this.centerOfMass == null) {
			if (this.getDimension() == 2)
				this.centerOfMass = new Point("CENTER_OF_MASS", 0, 0);
			else if (this.getDimension() == 3)
				this.centerOfMass = new Point("CENTER_OF_MASS", 0, 0, 0);
		}	
		return this.centerOfMass;
	}
	
	public Collection<Restriction> getRestrictions() {
		return Collections.<Restriction>emptySet();
	}
	
	public int getNumberOfPoints() {
		return this.featurePointList.size();
	}
		
	@Override
	public Iterator<FeaturePoint> iterator() {
		return this.featurePointList.iterator();
	}
	
	public ObjectProperty<String> nameProperty() {
		return this.name;
	}
	
	public String getName() {
		return this.nameProperty().get();
	}
	
	public void setName(String name) {
		this.nameProperty().set(name);
	}
	
	public abstract String toLaTex();
	
	@Override
	public String toString() {
		return this.getName();
	}
}
