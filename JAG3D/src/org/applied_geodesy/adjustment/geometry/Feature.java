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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.point.Point;
import org.applied_geodesy.adjustment.geometry.restriction.Restriction;
import org.applied_geodesy.util.ObservableUniqueList;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.NotConvergedException;

public abstract class Feature implements Iterable<GeometricPrimitive>, Geometrizable {
	private class GeometricPrimitiveListChangeListener implements ListChangeListener<GeometricPrimitive> {

		@Override
		public void onChanged(Change<? extends GeometricPrimitive> change) {
			if (unknownParameters == null)
				return;
			
			while (change.next()) {
				if (change.wasRemoved()) {
					for (GeometricPrimitive geometricPrimitive : change.getRemoved()) {
						if (unknownParameters != null)
							unknownParameters.removeAll(geometricPrimitive.getUnknownParameters());
						if (restrictions != null)
							restrictions.removeAll(geometricPrimitive.getRestrictions());
					}
				}
				else if (change.wasAdded()) {
					for (GeometricPrimitive geometricPrimitive : change.getAddedSubList()) {
						if (unknownParameters != null)
							unknownParameters.addAll(geometricPrimitive.getUnknownParameters());
						if (restrictions != null)
							restrictions.addAll(geometricPrimitive.getRestrictions());
					}
				}
			}
		}
	}
	
	private ReadOnlyObjectProperty<Boolean> immutable;
	private ObservableUniqueList<GeometricPrimitive> geometricPrimitives = new ObservableUniqueList<GeometricPrimitive>();
	private ObservableUniqueList<UnknownParameter> unknownParameters     = null;
	private ObservableUniqueList<Restriction> restrictions               = null;
	private ObservableUniqueList<Restriction> postprocessingCalculations = new ObservableUniqueList<Restriction>();
	private ObjectProperty<Boolean> estimateInitialGuess                 = new SimpleObjectProperty<Boolean>(this, "estimateInitialGuess", Boolean.TRUE);
	private ObjectProperty<Boolean> estimateCenterOfMass                 = new SimpleObjectProperty<Boolean>(this, "estimateCenterOfMass", Boolean.TRUE);
	private Point centerOfMass;
	
	Feature(boolean immutable) {
		this.immutable = new ReadOnlyObjectWrapper<Boolean>(this, "immutable", immutable);
		this.setCenterOfMass(this.getCenterOfMass());
		this.geometricPrimitives.addListener(new GeometricPrimitiveListChangeListener());
	}
	
	public void setCenterOfMass(Point centerOfMass) {
		for (GeometricPrimitive geometricPrimitive : this.geometricPrimitives)
			geometricPrimitive.setCenterOfMass(centerOfMass);
		this.centerOfMass = centerOfMass;
	}
	
	public abstract FeatureType getFeatureType();

	public final Point getCenterOfMass() {
		if (this.centerOfMass == null) {
			if (this.getFeatureType() == FeatureType.CURVE)
				this.centerOfMass = new Point("CENTER_OF_MASS", 0, 0);
			else
				this.centerOfMass = new Point("CENTER_OF_MASS", 0, 0, 0);
		}
		return this.centerOfMass;
	}
	
	public void prepareIteration() {}
	
	public final ObservableUniqueList<Restriction> getPostProcessingCalculations() {
		return this.postprocessingCalculations;
	}
	
	public ObjectProperty<Boolean> estimateCenterOfMassProperty() {
		return this.estimateCenterOfMass;
	}
	
	public void setEstimateCenterOfMass(boolean estimateCenterOfMass) {
		this.estimateCenterOfMass.set(estimateCenterOfMass);
	}
	
	public boolean isEstimateCenterOfMass() {
		return this.estimateCenterOfMass.get();
	}
	
	public ObjectProperty<Boolean> estimateInitialGuessProperty() {
		return this.estimateInitialGuess;
	}
	
	public void setEstimateInitialGuess(boolean estimateInitialGuess) {
		this.estimateInitialGuess.set(estimateInitialGuess);
	}
	
	public boolean isEstimateInitialGuess() {
		return this.estimateInitialGuess.get();
	}
	
	public ReadOnlyObjectProperty<Boolean> immutableProperty() {
		return this.immutable;
	}
	
	public boolean isImmutable() {
		return this.immutable.get();
	}
	
	public final ObservableUniqueList<UnknownParameter> getUnknownParameters() {
		if (this.unknownParameters == null) {
			this.unknownParameters = new ObservableUniqueList<UnknownParameter>();
			for (GeometricPrimitive geometricPrimitive : this.geometricPrimitives)
				this.unknownParameters.addAll(geometricPrimitive.getUnknownParameters());
		}
		return this.unknownParameters;
	}
	
	public final ObservableUniqueList<Restriction> getRestrictions() {
		if (this.restrictions == null) {
			this.restrictions = new ObservableUniqueList<Restriction>();
			for (GeometricPrimitive geometricPrimitive : this.geometricPrimitives)
				this.restrictions.addAll(geometricPrimitive.getRestrictions());
		}
		return this.restrictions;
	}
	
	public void add(GeometricPrimitive geometricPrimitive) throws IllegalArgumentException {
		geometricPrimitive.setCenterOfMass(this.centerOfMass);
		this.geometricPrimitives.add(geometricPrimitive);
	}
	
	@Override
	public Iterator<GeometricPrimitive> iterator() {
		return this.geometricPrimitives.iterator();
	}
	
	public ObservableUniqueList<GeometricPrimitive> getGeometricPrimitives() {
		return this.geometricPrimitives;
	}
	
	public List<FeaturePoint> getFeaturePoints() {
		ObservableList<FeaturePoint> nonUniquePoints = FXCollections.<FeaturePoint>observableArrayList();
		for (GeometricPrimitive geometry : this.geometricPrimitives) {
			nonUniquePoints.addAll(geometry.getFeaturePoints());
		}
		FilteredList<FeaturePoint> enabledNonUniquePoints = new FilteredList<FeaturePoint>(nonUniquePoints);
		enabledNonUniquePoints.setPredicate(
				new Predicate<FeaturePoint>(){
					public boolean test(FeaturePoint featurePoint){
						return featurePoint.isEnable();
					}
				}
		);
					
		// Unique point list
		return new ArrayList<FeaturePoint>(new LinkedHashSet<FeaturePoint>(enabledNonUniquePoints));
	}
	
	public abstract void deriveInitialGuess() throws MatrixSingularException, IllegalArgumentException, NotConvergedException, UnsupportedOperationException;
	
	public void applyInitialGuess() {
		for (UnknownParameter unknownParameter : this.unknownParameters) {
			// set initial guess x <-- x0 because adjustment process works with x (not with x0)
			unknownParameter.setValue(unknownParameter.getValue0());
		}
	}
	
	public static Point deriveCenterOfMass(Collection<FeaturePoint> featurePoints) {
		int nop = 0, dim = -1;
		double x0 = 0, y0 = 0, z0 = 0;
		for (FeaturePoint featurePoint : featurePoints) {
			if (!featurePoint.isEnable())
				continue;
			
			nop++;
			x0 += featurePoint.getX0();
			y0 += featurePoint.getY0();
			z0 += featurePoint.getZ0();
			
			if (dim < 0)
				dim = featurePoint.getDimension();
			if (dim != featurePoint.getDimension())
				throw new IllegalArgumentException("Error, could not estimate center of mass because dimension of points is inconsistent, " + dim + " != " + featurePoint.getDimension());
		}
		
		if (nop == 0)
			throw new IllegalArgumentException("Error, could not estimate center of mass because of an empty point list!");
		
		x0 /= nop;
		y0 /= nop;
		z0 /= nop;
		
		if (dim == 2)
			return new Point("CENTER_OF_MASS", x0, y0);
		
		return new Point("CENTER_OF_MASS", x0, y0, z0);
	}
}
