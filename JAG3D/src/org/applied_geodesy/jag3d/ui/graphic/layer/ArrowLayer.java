package org.applied_geodesy.jag3d.ui.graphic.layer;

import java.util.List;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.ArrowSymbolType;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.sql.GraphicPoint;
import org.applied_geodesy.jag3d.ui.graphic.sql.RelativeConfidence;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ArrowLayer extends Layer {
	private DoubleProperty vectorScale = new SimpleDoubleProperty(1.0);
	private List<RelativeConfidence> relativeConfidences = FXCollections.observableArrayList();
	private ObjectProperty<ArrowSymbolType> arrowSymbolType = new SimpleObjectProperty<ArrowSymbolType>(ArrowSymbolType.FILLED_TETRAGON_ARROW);

	ArrowLayer(LayerType layerType, GraphicExtent currentGraphicExtent) {
		super(layerType, currentGraphicExtent);
		this.setColor(Color.DARKORANGE);
		this.setLineWidth(1.0);
		//this.addLayerPropertyChangeListener(this.vectorScaleProperty());
		this.addLayerPropertyChangeListener(this.arrowSymbolTypeProperty());
	}
	
	@Override
	public void draw(GraphicExtent graphicExtent) {
		this.clearDrawingBoard();

		if (!this.isVisible() || this.relativeConfidences.isEmpty())
			return;

		GraphicsContext graphicsContext = this.getGraphicsContext2D();
		ArrowSymbolType arrowSymbolType = this.getArrowSymbolType();
		double scale      = this.getVectorScale();
		double symbolSize = this.getSymbolSize();
		graphicsContext.setLineWidth(this.getLineWidth());

		for (RelativeConfidence relativeConfidence : relativeConfidences) {
			GraphicPoint startPoint = relativeConfidence.getStartPoint();
			GraphicPoint endPoint   = relativeConfidence.getEndPoint();
		
			if (!startPoint.isVisible() || !endPoint.isVisible())
				continue;
			
			PixelCoordinate pixelCoordinateStartPoint = GraphicExtent.toPixelCoordinate(startPoint.getCoordinate(), graphicExtent);
			PixelCoordinate pixelCoordinateEndPoint   = GraphicExtent.toPixelCoordinate(endPoint.getCoordinate(), graphicExtent);
			
			if (!this.contains(pixelCoordinateStartPoint) && !this.contains(pixelCoordinateEndPoint))
				continue;

			double xs = pixelCoordinateStartPoint.getX();
			double ys = pixelCoordinateStartPoint.getY();
			
			double xe = pixelCoordinateEndPoint.getX();
			double ye = pixelCoordinateEndPoint.getY();
			
			double distance = Math.hypot(xe-xs, ye-ys);
			distance = distance > 0 ? distance : 1;
			double dx = (xe-xs)/distance;
			double dy = (ye-ys)/distance;
			
			double avgX = 0.5 * (xs + xe);
			double avgY = 0.5 * (ys + ye);
			
			double angle = Math.atan2(dy, dx);

			PixelCoordinate vectorStartCoordinate = new PixelCoordinate(avgX, avgY);
			
			if (!this.contains(vectorStartCoordinate))
				continue;
			
			distance = distance * scale;
			
			// clipping line, if one of the points is outside
			double layerDiagoal = Math.hypot(this.getWidth(), this.getHeight());
			if (distance > 1.05 * layerDiagoal) {
				distance = 1.05 * layerDiagoal;
			}
			
			PixelCoordinate vectorEndCoordinate = new PixelCoordinate(avgX + distance * dx, avgY + distance * dy);

			graphicsContext.setStroke(this.getColor());
			graphicsContext.setFill(this.getColor());
			graphicsContext.setLineWidth(this.getLineWidth());
			graphicsContext.setLineDashes(null);
			
			graphicsContext.strokeLine(
					avgX,
					avgY,
					avgX + distance * dx,
					avgY + distance * dy
					);

			SymbolBuilder.drawSymbol(graphicsContext, vectorEndCoordinate, arrowSymbolType, symbolSize, angle);
		}
	}
	
	public void setRelativeConfidences(List<RelativeConfidence> relativeConfidences) {
		GraphicExtent graphicExtent = this.getMaximumGraphicExtent();
		graphicExtent.reset();
		this.relativeConfidences.clear();
		if (relativeConfidences != null) {
			for (RelativeConfidence relativeConfidence : relativeConfidences) {
				GraphicPoint startPoint = relativeConfidence.getStartPoint();
				GraphicPoint endPoint   = relativeConfidence.getEndPoint();
				
				graphicExtent.merge(startPoint.getCoordinate());
				graphicExtent.merge(endPoint.getCoordinate());
			}
			this.relativeConfidences.addAll(relativeConfidences);
		}
	}
	
	List<RelativeConfidence> getRelativeConfidences() {
		return this.relativeConfidences;
	}

	public DoubleProperty vectorScaleProperty() {
		return this.vectorScale;
	}

	public double getVectorScale() {
		return this.vectorScaleProperty().get();
	}

	public void setVectorScale(final double vectorScale) {
		this.vectorScaleProperty().set(vectorScale);
	}
	
	@Override
	public String toString() {
		return i18n.getString("ArrowLayer.type", "Displacements (Congruence analysis)");
	}

	public ObjectProperty<ArrowSymbolType> arrowSymbolTypeProperty() {
		return this.arrowSymbolType;
	}

	public ArrowSymbolType getArrowSymbolType() {
		return this.arrowSymbolTypeProperty().get();
	}

	public void setArrowSymbolType(final ArrowSymbolType arrowSymbolType) {
		this.arrowSymbolTypeProperty().set(arrowSymbolType);
	}

	@Override
	public void clearLayer() {
		this.clearDrawingBoard();
		this.relativeConfidences.clear();
	}

	@Override
	public GraphicExtent getMaximumGraphicExtent() {
		GraphicExtent graphicExtent = new GraphicExtent();
		graphicExtent.reset();
		if (this.relativeConfidences != null) {
			for (RelativeConfidence relativeConfidence : this.relativeConfidences) {
				GraphicPoint startPoint = relativeConfidence.getStartPoint();
				GraphicPoint endPoint   = relativeConfidence.getEndPoint();
				
				if (startPoint.isVisible())
					graphicExtent.merge(startPoint.getCoordinate());
				if (endPoint.isVisible())
					graphicExtent.merge(endPoint.getCoordinate());
			}
		}
		return graphicExtent;
	}
}
