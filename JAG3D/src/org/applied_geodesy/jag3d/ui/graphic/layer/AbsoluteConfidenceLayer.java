package org.applied_geodesy.jag3d.ui.graphic.layer;

import java.util.List;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.sql.GraphicPoint;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class AbsoluteConfidenceLayer extends ConfidenceLayer<PointLayer> {
	private class VisiblePropertyChangeListener implements ChangeListener<Boolean> {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			draw(getCurrentGraphicExtent());
		}
	}
	
	private VisiblePropertyChangeListener visiblePropertyChangeListener = new VisiblePropertyChangeListener();
	private DoubleProperty confidenceScale = null;
	
	AbsoluteConfidenceLayer(LayerType layerType, GraphicExtent currentGraphicExtent) {
		super(layerType, currentGraphicExtent);
		this.setColor(Color.gray(0.8));
		this.addLayerPropertyChangeListener(this.confidenceScaleProperty());
	}
	
	@Override
	public void add(PointLayer layer) {
		layer.visibleProperty().addListener(this.visiblePropertyChangeListener);
		super.add(layer);
	}

	public void addAll(PointLayer ...layers) {
		for (PointLayer layer : layers)
			this.add(layer);
	}

	@Override
	public void draw(GraphicExtent graphicExtent) {
		this.clearDrawingBoard();
		
		if (!this.isVisible())
			return;

		GraphicsContext graphicsContext = this.getGraphicsContext2D();	
		List<PointLayer> referenceLayers = this.getReferenceLayers();
		double scale = this.getCurrentGraphicExtent().getScale();
		double ellipseScale = this.getConfidenceScale()/scale;
		double lineWidth  = this.getLineWidth();
		graphicsContext.setLineWidth(lineWidth);
		graphicsContext.setStroke(this.getStrokeColor());
		graphicsContext.setFill(this.getColor());
		for (PointLayer layer : referenceLayers) {
			if (layer.isVisible()) {
				// draw points
				for (GraphicPoint point : layer.getPoints()) {
					if (!point.isVisible())
						continue;
					
					PixelCoordinate pixelCoordinate = GraphicExtent.toPixelCoordinate(point.getCoordinate(), graphicExtent);
					if (this.contains(pixelCoordinate) && point.getMajorAxis() > 0) {
						double majorAxis = ellipseScale*point.getMajorAxis();
						double minorAxis = ellipseScale*point.getMinorAxis();
						double angle     = point.getAngle();

						SymbolBuilder.drawEllipse(graphicsContext, pixelCoordinate, majorAxis, minorAxis, angle);
					}
				}
			}
		}
	}
	
	public DoubleProperty confidenceScaleProperty() {
		if (this.confidenceScale == null)
			this.confidenceScale = new SimpleDoubleProperty(1.0);
		return this.confidenceScale;
	}

	public double getConfidenceScale() {
		return this.confidenceScaleProperty().get();
	}

	public void setConfidenceScale(final double confidenceScale) {
		this.confidenceScaleProperty().set(confidenceScale);
	}
	
	@Override
	public String toString() {
		return i18n.getString("AbsoluteConfidenceLayer.type", "Point confidences");
	}
}
