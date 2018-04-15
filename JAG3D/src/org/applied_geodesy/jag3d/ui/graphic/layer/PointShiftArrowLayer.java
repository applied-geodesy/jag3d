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
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.ArrowSymbolType;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.sql.GraphicPoint;
import org.applied_geodesy.jag3d.ui.graphic.sql.RelativeConfidence;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.collections.FXCollections;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PointShiftArrowLayer extends ArrowLayer {
	private List<RelativeConfidence> relativeConfidences = FXCollections.observableArrayList();

	PointShiftArrowLayer(LayerType layerType) {
		super(layerType);
		
		Color color;
		double symbolSize = -1, lineWidth = -1;
		
		try {
			color = Color.web(PROPERTIES.getProperty("POINT_SHIFT_ARROW_COLOR", "#ff8c00"));
		} catch (Exception e) {
			color = Color.web("#ff8c00");
		}
		
		ArrowSymbolType arrowSymbolType;
		try {
			arrowSymbolType = ArrowSymbolType.valueOf(PROPERTIES.getProperty("POINT_SHIFT_ARROW_SYMBOL_TYPE", "FILLED_TETRAGON_ARROW"));
		} catch (Exception e) {
			arrowSymbolType = ArrowSymbolType.FILLED_TETRAGON_ARROW;
		}

		try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("POINT_SHIFT_ARROW_SYMBOL_SIZE")); } catch (Exception e) {}
		try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("POINT_SHIFT_ARROW_LINE_WIDTH")); } catch (Exception e) {}

		symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;
		lineWidth = lineWidth >= 0 ? lineWidth : 1.0;
		
		this.setSymbolType(arrowSymbolType);
		this.setColor(color);
		this.setSymbolSize(symbolSize);
		this.setLineWidth(lineWidth);
	}
	
	@Override
	public void draw(GraphicsContext graphicsContext, GraphicExtent graphicExtent) {
		if (!this.isVisible() || this.relativeConfidences == null || this.relativeConfidences.isEmpty())
			return;

		ArrowSymbolType arrowSymbolType = this.getSymbolType();
		double scale      = this.getVectorScale();
		double symbolSize = this.getSymbolSize();
		double lineWidth  = this.getLineWidth();
		
		double width  = graphicExtent.getDrawingBoardWidth();
		double height = graphicExtent.getDrawingBoardHeight();
		
		graphicsContext.setStroke(this.getColor());
		graphicsContext.setFill(this.getColor());
		graphicsContext.setLineWidth(lineWidth);
		graphicsContext.setLineDashes(null);

		for (RelativeConfidence relativeConfidence : relativeConfidences) {
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
			
			double distance = Math.hypot(xe-xs, ye-ys);
			
			// skip, if symbol size larger than component
			if (distance == 0 || distance * scale < symbolSize)
				continue;
			
			distance = distance > 0 ? distance : 1;
			double dx = (xe-xs)/distance;
			double dy = (ye-ys)/distance;
			
			double avgX = 0.5 * (xs + xe);
			double avgY = 0.5 * (ys + ye);
			
			double angle = Math.atan2(dy, dx);

			PixelCoordinate vectorStartCoordinate = new PixelCoordinate(avgX, avgY);
			
			if (!this.contains(graphicExtent, vectorStartCoordinate))
				continue;
			
			distance = distance * scale;
			
			// clipping line, if one of the points is outside
			double layerDiagoal = Math.hypot(width, height);
			if (distance > 1.05 * layerDiagoal) {
				distance = 1.05 * layerDiagoal;
			}
			
			PixelCoordinate vectorEndCoordinate = new PixelCoordinate(avgX + distance * dx, avgY + distance * dy);

//			graphicsContext.setStroke(this.getColor());
//			graphicsContext.setFill(this.getColor());
//			graphicsContext.setLineWidth(lineWidth);
//			graphicsContext.setLineDashes(null);
			
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
	
	@Override
	public String toString() {
		return i18n.getString("PointShiftArrowLayer.type", "Point Shift (Congruence analysis)");
	}

	@Override
	public void clearLayer() {
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
