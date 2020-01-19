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
import org.applied_geodesy.jag3d.ui.graphic.coordinate.WorldCoordinate;
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
		ArrowSymbolType arrowSymbolType;
		double symbolSize = -1, lineWidth = -1;
		
		switch(layerType) {			
		case POINT_SHIFT_HORIZONTAL:
			try {
				color = Color.web(PROPERTIES.getProperty("POINT_SHIFT_HORIZONTAL_ARROW_COLOR", "#ff8c00"));
			} catch (Exception e) {
				color = Color.web("#ff8c00");
			}

			try {
				arrowSymbolType = ArrowSymbolType.valueOf(PROPERTIES.getProperty("POINT_SHIFT_HORIZONTAL_ARROW_SYMBOL_TYPE", "FILLED_TETRAGON_ARROW"));
			} catch (Exception e) {
				arrowSymbolType = ArrowSymbolType.FILLED_TETRAGON_ARROW;
			}

			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("POINT_SHIFT_HORIZONTAL_ARROW_SYMBOL_SIZE")); } catch (Exception e) {}
			try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("POINT_SHIFT_HORIZONTAL_ARROW_LINE_WIDTH")); } catch (Exception e) {}

			break;

		case POINT_SHIFT_VERTICAL:
			try {
				color = Color.web(PROPERTIES.getProperty("POINT_SHIFT_VERTICAL_ARROW_COLOR", "#ffaf00"));
			} catch (Exception e) {
				color = Color.web("#ff8c00");
			}

			try {
				arrowSymbolType = ArrowSymbolType.valueOf(PROPERTIES.getProperty("POINT_SHIFT_VERTICAL_ARROW_SYMBOL_TYPE", "FILLED_TETRAGON_ARROW"));
			} catch (Exception e) {
				arrowSymbolType = ArrowSymbolType.FILLED_TETRAGON_ARROW;
			}

			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("POINT_SHIFT_VERTICAL_ARROW_SYMBOL_SIZE")); } catch (Exception e) {}
			try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("POINT_SHIFT_VERTICAL_ARROW_LINE_WIDTH")); } catch (Exception e) {}
			
			break;
			
		default:
			throw new IllegalArgumentException("Error, unsupported layer type " + layerType);		
		}

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

		LayerType layerType = this.getLayerType();
		ArrowSymbolType arrowSymbolType = this.getSymbolType();
		double scale      = this.getVectorScale();
		double symbolSize = this.getSymbolSize();
		double lineWidth  = this.getLineWidth();
		
		double width  = graphicExtent.getDrawingBoardWidth();
		double height = graphicExtent.getDrawingBoardHeight();
		double layerDiagonal = Math.hypot(width, height);
		
		graphicsContext.setStroke(this.getColor());
		graphicsContext.setFill(this.getColor());
		graphicsContext.setLineWidth(lineWidth);
		graphicsContext.setLineDashes(null);

		for (RelativeConfidence relativeConfidence : this.relativeConfidences) {
			GraphicPoint startPoint = relativeConfidence.getStartPoint();
			GraphicPoint endPoint   = relativeConfidence.getEndPoint();
			double deltaHeight      = relativeConfidence.getDeltaHeight();
				
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
			
			double pxDistance = Math.hypot(xe-xs, ye-ys);
			
			double avgX = 0.5 * (xs + xe);
			double avgY = 0.5 * (ys + ye);
			
			PixelCoordinate vectorStartCoordinate = new PixelCoordinate(avgX, avgY);
			if (!this.contains(graphicExtent, vectorStartCoordinate))
				continue;
			
			switch(layerType) {
			// Draw horizontal components
			case POINT_SHIFT_HORIZONTAL:
				// skip, if symbol size larger than component
				if (startPoint.getDimension() != 1 && endPoint.getDimension() != 1 && pxDistance > 0 && pxDistance * scale >= symbolSize) {

					pxDistance = pxDistance > 0 ? pxDistance : 1;
					double dx = (xe-xs)/pxDistance;
					double dy = (ye-ys)/pxDistance;

					double angle = Math.atan2(dy, dx);

					pxDistance = pxDistance * scale;

					// clipping line, if one of the points is outside
					if (pxDistance > 1.05 * layerDiagonal) {
						pxDistance = 1.05 * layerDiagonal;
					}

					PixelCoordinate vectorEndCoordinate = new PixelCoordinate(avgX + pxDistance * dx, avgY + pxDistance * dy);

					graphicsContext.strokeLine(
							avgX,
							avgY,
							avgX + pxDistance * dx,
							avgY + pxDistance * dy
							);

					SymbolBuilder.drawSymbol(graphicsContext, vectorEndCoordinate, arrowSymbolType, symbolSize, angle);
				}
				
				break;
			
			// Draw vertical component
			case POINT_SHIFT_VERTICAL:
				PixelCoordinate pixelCoordinateHeightComponent = GraphicExtent.toPixelCoordinate(new WorldCoordinate(startPoint.getCoordinate().getX(), startPoint.getCoordinate().getY() + deltaHeight), graphicExtent);
				double pxHeightComponent = pixelCoordinateHeightComponent.getY() - pixelCoordinateStartPoint.getY();

				// skip, if symbol size larger than component
				if (startPoint.getDimension() != 2 && endPoint.getDimension() != 2 && Math.abs(pxHeightComponent) > 0 && Math.abs(pxHeightComponent) * scale >= symbolSize) {
					// North or south direction
					double angle = 0.5 * Math.signum(pxHeightComponent) * Math.PI;

					pxHeightComponent = pxHeightComponent * scale;

					// clipping line, if one of the points is outside
					if (Math.abs(pxHeightComponent) > 1.05 * layerDiagonal) {
						pxHeightComponent = 1.05 * Math.signum(pxHeightComponent) * layerDiagonal;
					}

					PixelCoordinate vectorEndCoordinate = new PixelCoordinate(avgX, avgY + pxHeightComponent);

					graphicsContext.strokeLine(
							avgX,
							avgY,
							avgX,
							avgY + pxHeightComponent
							);

					SymbolBuilder.drawSymbol(graphicsContext, vectorEndCoordinate, arrowSymbolType, symbolSize, angle);
				}

				break;
				
			default:
				continue;
			}
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
		switch(this.getLayerType()) {
		case POINT_SHIFT_HORIZONTAL:
			return i18n.getString("PointShiftArrowLayer.type.horizontal", "Horizontal point shift");
		case POINT_SHIFT_VERTICAL:
			return i18n.getString("PointShiftArrowLayer.type.vertical", "Vertical point shift");
		default:
			return "";
		}
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

	@Override
	public void drawLegendSymbol(GraphicsContext graphicsContext, GraphicExtent graphicExtent, PixelCoordinate pixelCoordinate, double symbolHeight, double symbolWidth) {
		ArrowSymbolType arrowSymbolType = this.getSymbolType();
		double lineWidth  = this.getLineWidth();
		double symbolSize = symbolHeight;
		graphicsContext.setStroke(this.getColor());
		graphicsContext.setFill(this.getColor());
		graphicsContext.setLineWidth(lineWidth);
		graphicsContext.setLineDashes(null);
		
		graphicsContext.strokeLine(
				pixelCoordinate.getX(),
				pixelCoordinate.getY(),
				pixelCoordinate.getX() + symbolWidth,
				pixelCoordinate.getY() + 0
		);

		SymbolBuilder.drawSymbol(graphicsContext, pixelCoordinate, arrowSymbolType, symbolSize, Math.PI);
	}

	@Override
	public boolean hasContent() {
		LayerType layerType = this.getLayerType();
		for (RelativeConfidence relativeConfidence : this.relativeConfidences) {
			GraphicPoint startPoint = relativeConfidence.getStartPoint();
			GraphicPoint endPoint   = relativeConfidence.getEndPoint();
			
			if (startPoint.isVisible() && endPoint.isVisible()) {
				if ((layerType == LayerType.POINT_SHIFT_HORIZONTAL && startPoint.getDimension() != 1 && endPoint.getDimension() != 1) ||
						(layerType == LayerType.POINT_SHIFT_VERTICAL && startPoint.getDimension() != 2 && endPoint.getDimension() != 2))
					return true;
			}
		}
		return false;
	}
}
