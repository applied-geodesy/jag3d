package org.applied_geodesy.jag3d.ui.graphic.sql;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.WorldCoordinate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class GraphicPoint {
	private WorldCoordinate coordinate;
	private double minorAxis = 0.0, majorAxis = 0.0, angle = 0.0;
	private String name;
	private int dimension;
	private boolean significant;
	private BooleanProperty visible = new SimpleBooleanProperty(Boolean.TRUE);
	
	public GraphicPoint(String name, int dimension, double x, double y) {
		this(name, dimension, x, y, 0, 0, 0, false);
	}
	
	public GraphicPoint(String name, int dimension, double x, double y, double majorAxis, double minorAxis, double angle, boolean significant) {
		this.coordinate = new WorldCoordinate(x, y);
		this.name = name;
		this.dimension = dimension;
		this.majorAxis = Math.max(majorAxis, minorAxis);
		this.minorAxis = Math.min(majorAxis, minorAxis);
		this.angle = angle;
		this.significant = significant;
	}
	
	public WorldCoordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(WorldCoordinate coordinate) {
		this.coordinate = coordinate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSignificant() {
		return significant;
	}
	
	public void setSignificant(boolean significant) {
		this.significant = significant;
	}

	public double getMinorAxis() {
		return minorAxis;
	}

	public void setMinorAxis(double minorAxis) {
		this.minorAxis = minorAxis;
	}

	public double getMajorAxis() {
		return majorAxis;
	}

	public void setMajorAxis(double majorAxis) {
		this.majorAxis = majorAxis;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public BooleanProperty visibleProperty() {
		return this.visible;
	}

	public boolean isVisible() {
		return this.visibleProperty().get();
	}

	public void setVisible(final boolean visible) {
		this.visibleProperty().set(visible);
	}

	public int getDimension() {
		return dimension;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
