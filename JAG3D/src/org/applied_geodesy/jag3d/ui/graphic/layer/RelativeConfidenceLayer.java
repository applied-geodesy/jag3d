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

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.sql.GraphicPoint;
import org.applied_geodesy.jag3d.ui.graphic.sql.RelativeConfidence;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RelativeConfidenceLayer extends ConfidenceLayer<PointShiftArrowLayer> {

	RelativeConfidenceLayer(LayerType layerType) {
		super(layerType);
		
		Color fillColor, strokeColor;
		double lineWidth = -1;
		
		try {
			fillColor = Color.web(PROPERTIES.getProperty("RELATIVE_CONFIDENCE_FILL_COLOR", "#ffffe0"));
		} catch (Exception e) {
			fillColor = Color.web("#ffffe0");
		}
		
		try {
			strokeColor = Color.web(PROPERTIES.getProperty("RELATIVE_CONFIDENCE_STROKE_COLOR", "#000000"));
		} catch (Exception e) {
			strokeColor = Color.web("#999999");
		}

		try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("RELATIVE_CONFIDENCE_LINE_WIDTH")); } catch (Exception e) {}
		lineWidth = lineWidth >= 0 ? lineWidth : 0.5;
		
		this.setStrokeColor(strokeColor);
		this.setColor(fillColor);
		this.setLineWidth(lineWidth);
	}

	@Override
	public void draw(GraphicsContext graphicsContext, GraphicExtent graphicExtent) {
		if (!this.isVisible() || graphicExtent.getScale() <= 0)
			return;

		List<PointShiftArrowLayer> referenceLayers = this.getReferenceLayers();
		double scale = graphicExtent.getScale();
		double lineWidth  = this.getLineWidth();
		graphicsContext.setLineWidth(lineWidth);
		graphicsContext.setStroke(this.getStrokeColor());
		graphicsContext.setFill(this.getColor());
		graphicsContext.setLineDashes(null);

		for (PointShiftArrowLayer layer : referenceLayers) {
			if (layer.isVisible()) {
				double ellipseScale = layer.getVectorScale()/scale;
				for (RelativeConfidence relativeConfidence : layer.getRelativeConfidences()) { 
					GraphicPoint startPoint = relativeConfidence.getStartPoint();
					GraphicPoint endPoint   = relativeConfidence.getEndPoint();
					
					if (!startPoint.isVisible() || !endPoint.isVisible())
						continue;
					
					PixelCoordinate pixelCoordinateStartPoint = GraphicExtent.toPixelCoordinate(startPoint.getCoordinate(), graphicExtent);
					PixelCoordinate pixelCoordinateEndPoint   = GraphicExtent.toPixelCoordinate(endPoint.getCoordinate(), graphicExtent);
					
					if (!this.contains(graphicExtent, pixelCoordinateStartPoint) && !this.contains(graphicExtent, pixelCoordinateEndPoint))
						continue;

					double xs = pixelCoordinateStartPoint.getX();
					double ys = pixelCoordinateStartPoint.getY();
					
					double xe = pixelCoordinateEndPoint.getX();
					double ye = pixelCoordinateEndPoint.getY();
					
					double avgX = 0.5 * (xs + xe);
					double avgY = 0.5 * (ys + ye);

					PixelCoordinate vectorStartCoordinate = new PixelCoordinate(avgX, avgY);
					
					if (this.contains(graphicExtent, vectorStartCoordinate) && relativeConfidence.getMajorAxis() > 0) {
						double majorAxis = ellipseScale*relativeConfidence.getMajorAxis();
						double minorAxis = ellipseScale*relativeConfidence.getMinorAxis();
						double angle     = relativeConfidence.getAngle();

						SymbolBuilder.drawEllipse(graphicsContext, vectorStartCoordinate, majorAxis, minorAxis, angle);
					}
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return i18n.getString("RelativeConfidenceLayer.type", "Relative confidences");
	}
}
