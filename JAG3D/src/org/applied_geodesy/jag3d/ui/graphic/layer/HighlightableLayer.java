package org.applied_geodesy.jag3d.ui.graphic.layer;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;

public interface HighlightableLayer {
	public LayerType getLayerType();
	
	public ObjectProperty<Color> highlightColorProperty();
	public Color getHighlightColor();
	public void setHighlightColor(final Color highlightColor);
	
	public DoubleProperty highlightLineWidthProperty();
	public double getHighlightLineWidth();
	public void setHighlightLineWidth(final double highlightLineWidth);
}
