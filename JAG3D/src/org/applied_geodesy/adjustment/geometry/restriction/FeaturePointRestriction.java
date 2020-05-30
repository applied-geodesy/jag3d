package org.applied_geodesy.adjustment.geometry.restriction;

import java.util.Collection;

import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;

public class FeaturePointRestriction extends Restriction {
	private ObjectProperty<FeaturePoint> featurePoint = new SimpleObjectProperty<FeaturePoint>(this, "featurePoint");
	private ObjectProperty<GeometricPrimitive> geometricPrimitive = new SimpleObjectProperty<GeometricPrimitive>(this, "geometricPrimitive");
	
	public FeaturePointRestriction() {
		this(false, null, null);
	}
	
	public FeaturePointRestriction(boolean indispensable, GeometricPrimitive geometricPrimitive, FeaturePoint featurePoint) {
		super(RestrictionType.FEATURE_POINT, indispensable);

		this.setGeometricPrimitive(geometricPrimitive);
		this.setFeaturePoint(featurePoint);
	}
	
	public void setGeometricPrimitive(GeometricPrimitive geometricPrimitive) {
		this.geometricPrimitive.set(geometricPrimitive);
	}
	
	public GeometricPrimitive getGeometricPrimitive() {
		return this.geometricPrimitive.get();
	}
	
	public ObjectProperty<GeometricPrimitive> geometricPrimitiveProperty() {
		return this.geometricPrimitive;
	}
	
	public void setFeaturePoint(FeaturePoint featurePoint) {
		this.featurePoint.set(featurePoint);
	}
	
	public FeaturePoint getFeaturePoint() {
		return this.featurePoint.get();
	}
	
	public ObjectProperty<FeaturePoint> featurePointProperty() {
		return this.featurePoint;
	}

	@Override
	public double getMisclosure() {
		return this.getGeometricPrimitive().getMisclosure(this.getFeaturePoint());
	}

	@Override
	public void transposedJacobianElements(Matrix JrT) {
		Collection<UnknownParameter> unknownParameters = this.getGeometricPrimitive().getUnknownParameters();

		int rowIndex = this.getRow();
		// store values temp. in a one row matrix
		DenseMatrix jx = new DenseMatrix(1, JrT.numRows());
		this.getGeometricPrimitive().jacobianElements(this.getFeaturePoint(), jx, null, 0);
		
		// copy jx values to restriction matrix
		for (UnknownParameter unknownParameter : unknownParameters) {
			int column = unknownParameter.getColumn();
			if (column < 0)
				continue;
			JrT.set(column, rowIndex, jx.get(0, column));
		}	
	}

	@Override
	public boolean contains(Object object) {
		if (object == null)
			return false;
		return this.getFeaturePoint() == object || this.getGeometricPrimitive() == object;
	}

	@Override
	public String toLaTex() {
		// TODO Auto-generated method stub
		return "$f_{geo}\\left(x, p\\right) = 0$";
	}
}
