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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationSymbolProperties.ObservationType;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.sql.GraphicPoint;
import org.applied_geodesy.jag3d.ui.graphic.sql.ObservableMeasurement;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlight;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightType;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class ObservationLayer extends Layer implements HighlightableLayer {
	// size of point symbol of point layers 
	private DoubleProperty pointSymbolSize = new SimpleDoubleProperty(SymbolBuilder.DEFAULT_SIZE);
	private List<ObservableMeasurement> observableMeasurements = FXCollections.observableArrayList();
	private Map<ObservationType, ObservationSymbolProperties> symbolPropertiesMap = new HashMap<ObservationType, ObservationSymbolProperties>(ObservationType.values().length);

	private ObjectProperty<Color> highlightColor = new SimpleObjectProperty<Color>(Color.ORANGERED); //#FF4500
	private DoubleProperty highlightLineWidth    = new SimpleDoubleProperty(2.5);
	private ObjectProperty<TableRowHighlightType> highlightType = new SimpleObjectProperty<TableRowHighlightType>(TableRowHighlightType.NONE);
	private Set<ObservationType> projectObservationTypes = new LinkedHashSet<ObservationType>(10); // Store all given ObservationType of the project
	
	ObservationLayer(LayerType layerType) {
		super(layerType);
		TableRowHighlightType highlightType;
		Color color, highlightColor;
		double symbolSize = -1, lineWidth = -1, highlightLineWidth = -1;
		boolean visible = Boolean.TRUE;

		switch(layerType) {
		case OBSERVATION_APRIORI:
			try {
				color = Color.web(PROPERTIES.getProperty("OBSERVATION_APRIORI_COLOR", "#b0c4de"));
			} catch (Exception e) {
				color = Color.web("#b0c4de");
			}

			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("OBSERVATION_APRIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			try { lineWidth  = Double.parseDouble(PROPERTIES.getProperty("OBSERVATION_APRIORI_LINE_WIDTH")); } catch (Exception e) {}
			try { visible    = PROPERTIES.getProperty("OBSERVATION_APRIORI_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}

			break;

		case OBSERVATION_APOSTERIORI:
			try {
				highlightType = TableRowHighlightType.valueOf(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_HIGHLIGHT_TYPE", "NONE"));
				if (highlightType == null)
					highlightType = TableRowHighlightType.NONE;
			} catch (Exception e) {
				highlightType = TableRowHighlightType.NONE;
			}
			
			try {
				highlightColor = Color.web(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_HIGHLIGHT_COLOR", "#FF4500"));
			} catch (Exception e) {
				highlightColor = Color.web("#FF4500");
			}
			
			try {
				color = Color.web(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_COLOR", "#778899"));
			} catch (Exception e) {
				color = Color.web("#778899");
			}

			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			try { lineWidth  = Double.parseDouble(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_LINE_WIDTH")); } catch (Exception e) {}
			try { visible    = PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}
			try { highlightLineWidth = Double.parseDouble(PROPERTIES.getProperty("OBSERVATION_APOSTERIORI_HIGHLIGHT_LINE_WIDTH")); } catch (Exception e) {}
			this.setHighlightLineWidth(highlightLineWidth >= 0 ? highlightLineWidth : 2.5);
			this.setHighlightColor(highlightColor);
			this.setHighlightType(highlightType);
			
			break;
		default:
			throw new IllegalArgumentException("Error, unsupported layer type " + layerType);
		}

		symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;
		lineWidth = lineWidth >= 0 ? lineWidth : 1.0;
				
		this.setLineWidth(lineWidth);
		this.setSymbolSize(symbolSize);
		this.setColor(color);
		
		this.setVisible(visible);
		
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

			this.symbolPropertiesMap.put(observationType, properties);
		}
	}

	@Override
	public void draw(GraphicsContext graphicsContext, GraphicExtent graphicExtent) {
		if (!this.isVisible() || this.observableMeasurements.isEmpty())
			return;

		double symbolSize = this.getSymbolSize();
		graphicsContext.setLineCap(StrokeLineCap.BUTT);
		graphicsContext.setLineWidth(this.getLineWidth());

		final double maxLength = 125;
		double pointSymbolSize = this.getPointSymbolSize();
		
		double width  = graphicExtent.getDrawingBoardWidth();
		double height = graphicExtent.getDrawingBoardHeight();

		for (ObservableMeasurement observableLink : this.observableMeasurements) {
			GraphicPoint startPoint = observableLink.getStartPoint();
			GraphicPoint endPoint   = observableLink.getEndPoint();

			if (!startPoint.isVisible() || !endPoint.isVisible())
				continue;

			PixelCoordinate pixelCoordinateStartPoint = GraphicExtent.toPixelCoordinate(startPoint.getCoordinate(), graphicExtent);
			PixelCoordinate pixelCoordinateEndPoint   = GraphicExtent.toPixelCoordinate(endPoint.getCoordinate(), graphicExtent);
			
			if (!this.contains(graphicExtent, pixelCoordinateStartPoint) && !this.contains(graphicExtent, pixelCoordinateEndPoint))
				continue;
			
			if (!this.contains(graphicExtent, pixelCoordinateStartPoint) && !this.contains(graphicExtent, pixelCoordinateEndPoint))
				continue;

			double xs = pixelCoordinateStartPoint.getX();
			double ys = pixelCoordinateStartPoint.getY();

			double xe = pixelCoordinateEndPoint.getX();
			double ye = pixelCoordinateEndPoint.getY();

			double distance = Math.hypot(xe-xs, ye-ys);
			distance = distance > 0 ? distance : 1;
			double dx = (xe-xs)/distance;
			double dy = (ye-ys)/distance;
			
			Color color      = this.getColor();
			double lineWidth = this.getLineWidth();
			
			TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
			double leftBoundary  = tableRowHighlight.getLeftBoundary(this.getHighlightType());
			double rightBoundary = tableRowHighlight.getRightBoundary(this.getHighlightType());

			switch(this.getHighlightType()) {
			case INFLUENCE_ON_POSITION:
				if (observableLink.getInfluenceOnPosition() > rightBoundary) {
					color     = this.getHighlightColor();
					lineWidth = this.getHighlightLineWidth();
				}
				break;
			case P_PRIO_VALUE:
				if (observableLink.getPprio() < Math.log(leftBoundary / 100.0)) {
					color     = this.getHighlightColor();
					lineWidth = this.getHighlightLineWidth();
				}
				break;
			case REDUNDANCY:
				if (observableLink.getRedundancy() < leftBoundary) {
					color     = this.getHighlightColor();
					lineWidth = this.getHighlightLineWidth();
				}
				break;
			case TEST_STATISTIC:
				if (observableLink.isSignificant()) {
					color     = this.getHighlightColor();
					lineWidth = this.getHighlightLineWidth();
				}
				break;
			case GROSS_ERROR:
				if (observableLink.isGrossErrorExceeded()) {
					color     = this.getHighlightColor();
					lineWidth = this.getHighlightLineWidth();
				}
				break;
			case NONE: // DEFAULT
				color     = this.getColor();
				lineWidth = this.getLineWidth();
				break;
			}
			
			graphicsContext.setStroke(color);
			graphicsContext.setLineWidth(lineWidth);
			graphicsContext.setLineDashes(null);

			// clipping line, if one of the points is outside
			double layerDiagoal = Math.hypot(width, height);
			if (distance > 1.005 * layerDiagoal) {
				distance = 1.005 * layerDiagoal;

				if (!this.contains(graphicExtent, pixelCoordinateStartPoint)) {
					xs = xe - distance * dx;
					ys = ye - distance * dy;
				}

				else if (!this.contains(graphicExtent, pixelCoordinateEndPoint)) {
					xe = xs + distance * dx;
					ye = ys + distance * dy;
				}
			}

			double si = 0.0, ei = 0.0;
			if (this.contains(graphicExtent, pixelCoordinateStartPoint) && !observableLink.getStartPointObservationType().isEmpty()) {
				si = 1.0;
				graphicsContext.strokeLine(
						xs,
						ys,
						xs + Math.min(0.35*distance, maxLength) * dx,
						ys + Math.min(0.35*distance, maxLength) * dy
						);
			}

			if (this.contains(graphicExtent, pixelCoordinateEndPoint) && !observableLink.getEndPointObservationType().isEmpty()) {
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
		this.projectObservationTypes.clear();
		if (observableMeasurements != null) {
			for (ObservableMeasurement observableMeasurement : observableMeasurements) {
				GraphicPoint startPoint = observableMeasurement.getStartPoint();
				GraphicPoint endPoint   = observableMeasurement.getEndPoint();

				graphicExtent.merge(startPoint.getCoordinate());
				graphicExtent.merge(endPoint.getCoordinate());
				
				this.projectObservationTypes.addAll(observableMeasurement.getStartPointObservationType());
				this.projectObservationTypes.addAll(observableMeasurement.getEndPointObservationType());
				
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
		this.observableMeasurements.clear();
		this.projectObservationTypes.clear();
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

	@Override
	public final ObjectProperty<Color> highlightColorProperty() {
		return this.highlightColor;
	}
	
	@Override
	public final Color getHighlightColor() {
		return this.highlightColorProperty().get();
	}
	
	@Override
	public final void setHighlightColor(final Color highlightColor) {
		this.highlightColorProperty().set(highlightColor);
	}

	@Override
	public final DoubleProperty highlightLineWidthProperty() {
		return this.highlightLineWidth;
	}

	@Override
	public final double getHighlightLineWidth() {
		return this.highlightLineWidthProperty().get();
	}

	@Override
	public final void setHighlightLineWidth(final double highlightLineWidth) {
		this.highlightLineWidthProperty().set(highlightLineWidth);
	}
	
	@Override
	public ObjectProperty<TableRowHighlightType> highlightTypeProperty() {
		return this.highlightType;
	}
	
	@Override
	public TableRowHighlightType getHighlightType() {
		return this.highlightTypeProperty().get();
	}
	
	@Override
	public void setHighlightType(final TableRowHighlightType highlightType) {
		this.highlightTypeProperty().set(highlightType);
	}
	
	@Override
	public void drawLegendSymbol(GraphicsContext graphicsContext, GraphicExtent graphicExtent, PixelCoordinate pixelCoordinateStartPoint, double symbolHeight, double symbolWidth) {
		PixelCoordinate pixelCoordinateEndPoint = new PixelCoordinate(pixelCoordinateStartPoint.getX() + 70, pixelCoordinateStartPoint.getY());
		
		if (!this.contains(graphicExtent, pixelCoordinateStartPoint) && !this.contains(graphicExtent, pixelCoordinateEndPoint))
			return;
		
		Set<ObservationType> observationTypes = new LinkedHashSet<ObservationType>(10);
		
		for (ObservableMeasurement observableLink : this.observableMeasurements) {
			GraphicPoint startPoint = observableLink.getStartPoint();
			GraphicPoint endPoint   = observableLink.getEndPoint();

			if (!startPoint.isVisible() || !endPoint.isVisible())
				continue;

			observationTypes.addAll(observableLink.getStartPointObservationType());
			observationTypes.addAll(observableLink.getEndPointObservationType());

			if (observationTypes.size() == this.projectObservationTypes.size()) {
				boolean containsAll = true;
				for (ObservationType type : observationTypes) {
					if (!this.projectObservationTypes.contains(type)) {
						containsAll = false;
						break;
					}
				}
				if (containsAll)
					break;
			}
		}
		
		if (observationTypes.isEmpty())
			return;

		graphicsContext.setLineWidth(this.getLineWidth());
		graphicsContext.setStroke(this.getColor());
		graphicsContext.setLineDashes(null);
		graphicsContext.strokeLine(
				pixelCoordinateStartPoint.getX(),
				pixelCoordinateStartPoint.getY(),
				pixelCoordinateStartPoint.getX() + symbolWidth,
				pixelCoordinateStartPoint.getY()
				);

		PixelCoordinate coordinate = new PixelCoordinate(pixelCoordinateStartPoint.getX() + 0.5*(symbolWidth - symbolHeight), pixelCoordinateStartPoint.getY() - 0.5*symbolHeight);
		SymbolBuilder.drawSymbol(graphicsContext, coordinate, this.symbolPropertiesMap, observationTypes, symbolHeight);		
	}

	@Override
	public boolean hasContent() {
		return this.observableMeasurements != null && !this.observableMeasurements.isEmpty();
	}
}
