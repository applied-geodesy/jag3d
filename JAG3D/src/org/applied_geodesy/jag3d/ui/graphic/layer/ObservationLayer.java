package org.applied_geodesy.jag3d.ui.graphic.layer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationSymbolProperties.ObservationType;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.sql.GraphicPoint;
import org.applied_geodesy.jag3d.ui.graphic.sql.ObservableMeasurement;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class ObservationLayer extends Layer {
	
	private DoubleProperty pointSymbolSize = new SimpleDoubleProperty(SymbolBuilder.DEFAULT_SIZE);
	private List<ObservableMeasurement> observableMeasurements = FXCollections.observableArrayList();
	private Map<ObservationType, ObservationSymbolProperties> symbolPropertiesMap = new HashMap<ObservationType, ObservationSymbolProperties>(ObservationType.values().length);
	
	ObservationLayer(LayerType layerType, GraphicExtent currentGraphicExtent) {
		super(layerType, currentGraphicExtent);
		
		Color color = Color.BLACK;
		switch(layerType) {
		case OBSERVATION_APRIORI:
			color = Color.LIGHTSTEELBLUE;
			break;

		case OBSERVATION_APOSTERIORI:
			color = Color.LIGHTSLATEGRAY;
			break;
		default:
			throw new IllegalArgumentException("Error, unsupported layer type " + layerType);
		}
		
		this.setColor(color);
		this.initSymbolProperties();
	}
	
	private void initSymbolProperties() {
		ObservationType observationTypes[] = ObservationType.values();
		ObservationSymbolProperties properties = null;
		for (ObservationType observationType : observationTypes) {
			properties = new ObservationSymbolProperties(observationType);
			properties.setEnable(Boolean.TRUE);
			
			switch(observationType) {
			case LEVELING:
				properties.setColor(Color.VIOLET);
				break;
				
			case DIRECTION:
				properties.setColor(Color.YELLOW);
				break;
			
			case DISTANCE:	
				properties.setColor(Color.BLUE);
				break;
				
			case ZENITH_ANGLE:
				properties.setColor(Color.RED);
				break;
				
			case GNSS:
				properties.setColor(Color.GREEN);
				break;
			}
			
			this.addLayerPropertyChangeListener(properties.enableProperty());
			this.addLayerPropertyChangeListener(properties.colorProperty());
			
			this.symbolPropertiesMap.put(observationType, properties);
		}
	}

	@Override
	public void draw(GraphicExtent graphicExtent) {
		this.clearDrawingBoard();
		
		if (!this.isVisible() || this.observableMeasurements.isEmpty())
			return;

		GraphicsContext graphicsContext = this.getGraphicsContext2D();
		graphicsContext.setLineCap(StrokeLineCap.BUTT);

		double symbolSize = this.getSymbolSize();
		graphicsContext.setLineWidth(this.getLineWidth());

		final double maxLength = 125;
		double pointSymbolSize = this.getPointSymbolSize();

		for (ObservableMeasurement observableLink : this.observableMeasurements) {
			GraphicPoint startPoint = observableLink.getStartPoint();
			GraphicPoint endPoint   = observableLink.getEndPoint();
			
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
			
			graphicsContext.setStroke(this.getColor());
			graphicsContext.setLineWidth(this.getLineWidth());
			graphicsContext.setLineDashes(null);
			
			// clipping line, if one of the points is outside
			double layerDiagoal = Math.hypot(this.getWidth(), this.getHeight());
			if (distance > 1.005 * layerDiagoal) {
				distance = 1.005 * layerDiagoal;
				
				if (!this.contains(pixelCoordinateStartPoint)) {
					xs = xe - distance * dx;
					ys = ye - distance * dy;
				}
				
				else if (!this.contains(pixelCoordinateEndPoint)) {
					xe = xs + distance * dx;
					ye = ys + distance * dy;
				}
			}
			
			double si = 0.0, ei = 0.0;
			if (this.contains(pixelCoordinateStartPoint) && !observableLink.getStartPointObservationType().isEmpty()) {
				si = 1.0;
				graphicsContext.strokeLine(
						xs,
						ys,
						xs + Math.min(0.35*distance, maxLength) * dx,
						ys + Math.min(0.35*distance, maxLength) * dy
						);
			}
			
			if (this.contains(pixelCoordinateEndPoint) && !observableLink.getEndPointObservationType().isEmpty()) {
				ei = 1.0;
				graphicsContext.strokeLine(
						xe,
						ye,
						xe - Math.min(0.35*distance, maxLength) * dx,
						ye - Math.min(0.35*distance, maxLength) * dy
						);
			}
			
			graphicsContext.setLineDashes(2.5, 3.5);
			graphicsContext.strokeLine(
					xs + si * (Math.min(0.35*distance, maxLength) * dx),
					ys + si * (Math.min(0.35*distance, maxLength) * dy),
					xe - ei * (Math.min(0.35*distance, maxLength) * dx),
					ye - ei * (Math.min(0.35*distance, maxLength) * dy)
					);

			// check, if drawable
			if (distance > 3.0*symbolSize) {
				double scale = distance > 4.0*pointSymbolSize + symbolSize ? 1.5*pointSymbolSize + 0.5*symbolSize : 0.5 * (distance - symbolSize);
				if (!observableLink.getStartPointObservationType().isEmpty()) {
					PixelCoordinate coordinate = new PixelCoordinate(xs - 0.5*symbolSize + scale * dx, ys - 0.5*symbolSize + scale * dy);
					SymbolBuilder.drawSymbol(graphicsContext, coordinate, this.symbolPropertiesMap, observableLink.getStartPointObservationType(), symbolSize);
				}
				
				if (!observableLink.getEndPointObservationType().isEmpty()) {
					PixelCoordinate coordinate = new PixelCoordinate(xe - 0.5*symbolSize - scale * dx, ye - 0.5*symbolSize - scale * dy);
					SymbolBuilder.drawSymbol(graphicsContext, coordinate, this.symbolPropertiesMap, observableLink.getEndPointObservationType(), symbolSize);
				}
			}
		}
	}
		
	public void setObservableMeasurements(List<ObservableMeasurement> observableMeasurements) {
		GraphicExtent graphicExtent = this.getMaximumGraphicExtent();
		graphicExtent.reset();
		this.observableMeasurements.clear();
		if (observableMeasurements != null) {
			for (ObservableMeasurement observableMeasurement : observableMeasurements) {
				GraphicPoint startPoint = observableMeasurement.getStartPoint();
				GraphicPoint endPoint   = observableMeasurement.getEndPoint();
				
				graphicExtent.merge(startPoint.getCoordinate());
				graphicExtent.merge(endPoint.getCoordinate());
			}
			this.observableMeasurements.addAll(observableMeasurements);
		}
	}

	public final DoubleProperty pointSymbolSizeProperty() {
		return this.pointSymbolSize;
	}

	public final double getPointSymbolSize() {
		return this.pointSymbolSizeProperty().get();
	}

	public final void setPointSymbolSize(final double pointSymbolSize) {
		this.pointSymbolSizeProperty().set(pointSymbolSize);
	}
	
	public ObservationSymbolProperties getObservationSymbolProperties(ObservationType observationType) {
		return this.symbolPropertiesMap.get(observationType);
	}
	
	@Override
	public String toString() {
		switch(this.getLayerType()) {
		case OBSERVATION_APOSTERIORI:
			return i18n.getString("ObservationAprioriLayer.type.aposteriori", "Observations (a-posteriori)");
		case OBSERVATION_APRIORI:
			return i18n.getString("ObservationAprioriLayer.type.apriori", "Observations (a-priori)");
		default:
			return "";
		}
	}

	@Override
	public void clearLayer() {
		this.clearDrawingBoard();
		this.observableMeasurements.clear();
	}

	@Override
	public GraphicExtent getMaximumGraphicExtent() {
		GraphicExtent graphicExtent = new GraphicExtent();
		graphicExtent.reset();
		if (this.observableMeasurements != null) {
			for (ObservableMeasurement observableMeasurement : this.observableMeasurements) {
				GraphicPoint startPoint = observableMeasurement.getStartPoint();
				GraphicPoint endPoint   = observableMeasurement.getEndPoint();
				
				if (startPoint.isVisible())
					graphicExtent.merge(startPoint.getCoordinate());
				if (endPoint.isVisible())
					graphicExtent.merge(endPoint.getCoordinate());
			}
		}
		return graphicExtent;
	}
}
