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
	// size of point symbol of point layers 
	private DoubleProperty pointSymbolSize = new SimpleDoubleProperty(SymbolBuilder.DEFAULT_SIZE);
	private List<ObservableMeasurement> observableMeasurements = FXCollections.observableArrayList();
	private Map<ObservationType, ObservationSymbolProperties> symbolPropertiesMap = new HashMap<ObservationType, ObservationSymbolProperties>(ObservationType.values().length);

	ObservationLayer(LayerType layerType, GraphicExtent currentGraphicExtent) {
		super(layerType, currentGraphicExtent);

		Color color;
		double symbolSize = -1;
		double lineWidth = -1;

		switch(layerType) {
		case OBSERVATION_APRIORI:
			try {
				color = Color.web(PROPERTIES.getProperty("OBSERVATION_APRIORI_COLOR", "#b0c4de"));
			}
			catch (Exception e) {
				color = Color.web("#b0c4de"); //Color.LIGHTSTEELBLUE;
			}

			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("OBSERVATION_APRIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;


			try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("OBSERVATION_APRIORI_LINE_WIDTH")); } catch (Exception e) {}
			lineWidth = lineWidth >= 0 ? lineWidth : 1.0;

			break;

		case OBSERVATION_APOSTERIORI:
			try {
				color = Color.web(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_COLOR", "#778899"));
			}
			catch (Exception e) {
				color = Color.web("#778899"); //color = Color.LIGHTSLATEGRAY;
			}

			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;


			try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_LINE_WIDTH")); } catch (Exception e) {}
			lineWidth = lineWidth >= 0 ? lineWidth : 1.0;

			break;
		default:
			throw new IllegalArgumentException("Error, unsupported layer type " + layerType);
		}

		this.setLineWidth(lineWidth);
		this.setSymbolSize(symbolSize);
		this.setColor(color);
		this.initSymbolProperties();
	}

	private void initSymbolProperties() {
		ObservationType observationTypes[] = ObservationType.values();
		ObservationSymbolProperties properties = null;
		for (ObservationType observationType : observationTypes) {
			properties = new ObservationSymbolProperties(observationType);
			properties.setVisible(Boolean.TRUE);
			Color color = Color.BLACK;
			switch (this.getLayerType()) {
			case OBSERVATION_APRIORI:
				switch(observationType) {
				case LEVELING:
					try {
						color = Color.web(PROPERTIES.getProperty("OBSERVATION_APRIORI_COLOR_LEVELING", "#c195c1"));
					}
					catch (Exception e) {
						color = Color.web("#c195c1");
					}
					break;

				case DIRECTION:
					try {
						color = Color.web(PROPERTIES.getProperty("OBSERVATION_APRIORI_COLOR_DIRECTION", "#ffff7d"));
					}
					catch (Exception e) {
						color = Color.web("#ffff7d");
					}
					break;

				case DISTANCE:
					try {
						color = Color.web(PROPERTIES.getProperty("OBSERVATION_APRIORI_COLOR_DISTANCE", "#6d93ff"));
					}
					catch (Exception e) {
						color = Color.web("#6d93ff");
					}
					break;

				case ZENITH_ANGLE:
					try {
						color = Color.web(PROPERTIES.getProperty("OBSERVATION_APRIORI_COLOR_ZENITH_ANGLE", "#f28282"));
					}
					catch (Exception e) {
						color = Color.web("#f28282");
					}
					break;

				case GNSS:
					try {
						color = Color.web(PROPERTIES.getProperty("OBSERVATION_APRIORI_COLOR_GNSS", "#70b770"));
					}
					catch (Exception e) {
						color = Color.web("#70b770");
					}
					break;
				}

				break;

			case OBSERVATION_APOSTERIORI:
				switch(observationType) {
				case LEVELING:
					try {
						color = Color.web(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_COLOR_LEVELING", "#875187"));
					}
					catch (Exception e) {
						color = Color.web("#875187");
					}
					break;

				case DIRECTION:
					try {
						color = Color.web(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_COLOR_DIRECTION", "#d7d722"));
					}
					catch (Exception e) {
						color = Color.web("#d7d722");
					}
					break;

				case DISTANCE:
					try {
						color = Color.web(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_COLOR_DISTANCE", "#4872e9"));
					}
					catch (Exception e) {
						color = Color.web("#4872e9");
					}
					break;	

				case ZENITH_ANGLE:
					try {
						color = Color.web(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_COLOR_ZENITH_ANGLE", "#d23737"));
					}
					catch (Exception e) {
						color = Color.web("#d23737");
					}
					break;

				case GNSS:
					try {
						color = Color.web(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_COLOR_GNSS", "#32a332"));
					}
					catch (Exception e) {
						color = Color.web("#32a332");
					}
					break;	

				}
				break;

			default:
				break;
			}

			properties.setColor(color);
			this.addLayerPropertyChangeListener(properties.visibleProperty());
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
