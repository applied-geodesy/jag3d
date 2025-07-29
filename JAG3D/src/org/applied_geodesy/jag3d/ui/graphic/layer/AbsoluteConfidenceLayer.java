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

import java.util.List;
import java.util.Locale;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.sql.GraphicPoint;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;
import org.applied_geodesy.util.FormatterOptions;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class AbsoluteConfidenceLayer extends ConfidenceLayer<PointLayer> {
	private DoubleProperty confidenceScale = null;
	private FormatterOptions options = FormatterOptions.getInstance();
	
	AbsoluteConfidenceLayer(LayerType layerType) {
		super(layerType);
		
		Color fillColor, strokeColor;
		double lineWidth = -1;
		boolean visible = Boolean.TRUE;
		
		try {
			fillColor = Color.web(PROPERTIES.getProperty("ABSOLUTE_CONFIDENCE_FILL_COLOR", "#cccccc"));
		} catch (Exception e) {
			fillColor = Color.web("#cccccc");
		}
		
		try {
			strokeColor = Color.web(PROPERTIES.getProperty("ABSOLUTE_CONFIDENCE_STROKE_COLOR", "#000000"));
		} catch (Exception e) {
			strokeColor = Color.web("#000000");
		}

		try { visible   = PROPERTIES.getProperty("ABSOLUTE_CONFIDENCE_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}
		try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("ABSOLUTE_CONFIDENCE_LINE_WIDTH")); } catch (Exception e) {}
		lineWidth = lineWidth >= 0 ? lineWidth : 0.5;
		
		this.setStrokeColor(strokeColor);
		this.setColor(fillColor);
		this.setLineWidth(lineWidth);
		
		this.setVisible(visible);
	}
	
	@Override
	public void add(PointLayer layer) {
		super.add(layer);
	}

	public void addAll(PointLayer ...layers) {
		for (PointLayer layer : layers)
			this.add(layer);
	}

	@Override
	public void draw(GraphicsContext graphicsContext, GraphicExtent graphicExtent) {
		if (!this.isVisible() || graphicExtent.getScale() <= 0)
			return;

		List<PointLayer> referenceLayers = this.getReferenceLayers();
		double scale = graphicExtent.getScale();
		double ellipseScale = this.getConfidenceScale()/scale;
		double lineWidth  = this.getLineWidth();
		graphicsContext.setLineWidth(lineWidth);
		graphicsContext.setStroke(this.getStrokeColor());
		graphicsContext.setFill(this.getColor());
		graphicsContext.setLineDashes(null);
		
		for (PointLayer layer : referenceLayers) {
			if (layer.isVisible()) {
				// draw points
				for (GraphicPoint point : layer.getPoints()) {
					if (!point.isVisible())
						continue;
					
					PixelCoordinate pixelCoordinate = GraphicExtent.toPixelCoordinate(point.getCoordinate(), graphicExtent);
					if (this.contains(graphicExtent, pixelCoordinate) && point.getMajorAxis() > 0) {
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
		double confidenceLevel = this.getConfidenceLevel();
		String confidenceLevelLabel = confidenceLevel > 0 && confidenceLevel < 1 ? this.options.toPercentFormat(this.getConfidenceLevel(), true) : "";
		return String.format(Locale.ENGLISH, i18n.getString("AbsoluteConfidenceLayer.type", "Point confidences"), confidenceLevelLabel);
	}
	
	@Override
	public boolean hasContent() {
		List<PointLayer> referenceLayers = this.getReferenceLayers();
		for (PointLayer layer : referenceLayers) {
			if (layer.isVisible() && layer.hasContent()) {
				for (GraphicPoint point : layer.getPoints()) {
					if (point.isVisible())
						return true;
				}
			}
		}
		return false;
	}
}
