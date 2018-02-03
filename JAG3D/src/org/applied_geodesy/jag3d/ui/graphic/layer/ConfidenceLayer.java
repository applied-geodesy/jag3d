package org.applied_geodesy.jag3d.ui.graphic.layer;

import java.util.List;

import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

public abstract class ConfidenceLayer<T extends Layer> extends Layer {

	private ObjectProperty<Color> strokeColor = new SimpleObjectProperty<Color>(Color.BLACK);
	private List<T> referenceLayers = FXCollections.observableArrayList();

	ConfidenceLayer(LayerType layerType, GraphicExtent currentGraphicExtent) {
		super(layerType, currentGraphicExtent);
		this.setSymbolSize(0);
		this.setLineWidth(0.5);
		this.addLayerPropertyChangeListener(this.strokeColorProperty());
	}
	
	public void add(T layer) {
		this.referenceLayers.add(layer);
	}

	public void addAll(List<T> layers) {
		for (T layer : layers)
			this.add(layer);
	}
	
	List<T> getReferenceLayers() {
		return this.referenceLayers;
	}

	public ObjectProperty<Color> strokeColorProperty() {
		return this.strokeColor;
	}

	public Color getStrokeColor() {
		return this.strokeColorProperty().get();
	}

	public void setStrokeColor(final Color borderColor) {
		this.strokeColorProperty().set(borderColor);
	}

	@Override
	public String toString() {
		return i18n.getString("ConfidenceLayer.type", "Confidence");
	}

	@Override
	public void clearLayer() {
		this.clearDrawingBoard();
	}

	@Override
	public GraphicExtent getMaximumGraphicExtent() {
		GraphicExtent extent = new GraphicExtent();
		extent.reset();
		return extent;
	}
}

