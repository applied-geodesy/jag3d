package org.applied_geodesy.jag3d.ui.graphic.coordinate;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public abstract class Coordinate {
	private DoubleProperty x = new SimpleDoubleProperty(0.0);
	private DoubleProperty y = new SimpleDoubleProperty(0.0);
	
	Coordinate(double x, double y) {
		this.setX(x);
		this.setY(y);
	}

	public DoubleProperty xProperty() {
		return this.x;
	}

	public double getX() {
		return this.xProperty().get();
	}

	public void setX(final double x) {
		this.xProperty().set(x);
	}
	

	public DoubleProperty yProperty() {
		return this.y;
	}

	public double getY() {
		return this.yProperty().get();
	}

	public void setY(final double y) {
		this.yProperty().set(y);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": [ " + this.getX() + " / " + this.getY() + " ]";
	}
}
