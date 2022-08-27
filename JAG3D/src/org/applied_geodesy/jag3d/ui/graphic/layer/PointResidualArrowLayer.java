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
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.collections.FXCollections;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PointResidualArrowLayer extends ArrowLayer {
	private List<PointLayer> referenceLayers = FXCollections.observableArrayList();

	PointResidualArrowLayer(LayerType layerType) {
		super(layerType);
		
		Color color;
		ArrowSymbolType arrowSymbolType;
		double symbolSize = -1, lineWidth = -1;
		boolean visible = Boolean.FALSE;
		
		switch(layerType) {			
		case POINT_RESIDUAL_HORIZONTAL:
			try {
				color = Color.web(PROPERTIES.getProperty("POINT_RESIDUAL_HORIZONTAL_ARROW_COLOR", "#334db3"));
			} catch (Exception e) {
				color = Color.web("#334db3");
			}

			try {
				arrowSymbolType = ArrowSymbolType.valueOf(PROPERTIES.getProperty("POINT_RESIDUAL_HORIZONTAL_ARROW_SYMBOL_TYPE", "STROKED_ARROW"));
			} catch (Exception e) {
				arrowSymbolType = ArrowSymbolType.STROKED_ARROW;
			}
			
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("POINT_RESIDUAL_HORIZONTAL_ARROW_SYMBOL_SIZE")); } catch (Exception e) {}
			try { lineWidth  = Double.parseDouble(PROPERTIES.getProperty("POINT_RESIDUAL_HORIZONTAL_ARROW_LINE_WIDTH")); } catch (Exception e) {}
			try { visible    = PROPERTIES.getProperty("POINT_RESIDUAL_HORIZONTAL_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}
			
			break;
			
		case POINT_RESIDUAL_VERTICAL:
			try {
				color = Color.web(PROPERTIES.getProperty("POINT_RESIDUAL_VERTICAL_ARROW_COLOR", "#6680e6"));
			} catch (Exception e) {
				color = Color.web("#6680e6");
			}

			try {
				arrowSymbolType = ArrowSymbolType.valueOf(PROPERTIES.getProperty("POINT_RESIDUAL_VERTICAL_ARROW_SYMBOL_TYPE", "STROKED_ARROW"));
			} catch (Exception e) {
				arrowSymbolType = ArrowSymbolType.STROKED_ARROW;
			}
			
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("POINT_RESIDUAL_VERTICAL_ARROW_SYMBOL_SIZE")); } catch (Exception e) {}
			try { lineWidth  = Double.parseDouble(PROPERTIES.getProperty("POINT_RESIDUAL_VERTICAL_ARROW_LINE_WIDTH")); } catch (Exception e) {}
			try { visible    = PROPERTIES.getProperty("POINT_RESIDUAL_VERTICAL_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}
			
			break;
		
		default:
			throw new IllegalArgumentException("Error, unsupported layer type " + layerType);		
		}

		symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;
		lineWidth = lineWidth >= 0 ? lineWidth : 1.0;

		this.setVisible(visible);
		this.setSymbolType(arrowSymbolType);
		this.setColor(color);
		this.setSymbolSize(symbolSize);
		this.setLineWidth(lineWidth);
	}
	
	public void add(PointLayer layer) {
		this.referenceLayers.add(layer);
	}

	public void addAll(PointLayer ...layers) {
		for (PointLayer layer : layers)
			this.add(layer);
	}
	
	List<PointLayer> getReferenceLayers() {
		return this.referenceLayers;
	}
	
	@Override
	public void draw(GraphicsContext graphicsContext, GraphicExtent graphicExtent) {
		if (!this.isVisible() || this.referenceLayers == null || this.referenceLayers.isEmpty())
			return;
		
		LayerType layerType = this.getLayerType();
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

		for (PointLayer layer : this.referenceLayers) {
			if (layer.isVisible()) {
				// draw points
				for (GraphicPoint startPoint : layer.getPoints()) {
					if (!startPoint.isVisible())
						continue;
					
					WorldCoordinate endPoint;

					switch(layerType) {
					case POINT_RESIDUAL_HORIZONTAL:
						endPoint = new WorldCoordinate(
								startPoint.getCoordinate().getX() + startPoint.getResidualX(), 
								startPoint.getCoordinate().getY() + startPoint.getResidualY());
						break;
						
					case POINT_RESIDUAL_VERTICAL:
						endPoint = new WorldCoordinate(
								startPoint.getCoordinate().getX() + 0, 
								startPoint.getCoordinate().getY() + startPoint.getResidualZ());
						break;
					default:
						continue;
					}
					
					PixelCoordinate pixelCoordinateStartPoint = GraphicExtent.toPixelCoordinate(startPoint.getCoordinate(), graphicExtent);
					PixelCoordinate pixelCoordinateEndPoint   = GraphicExtent.toPixelCoordinate(endPoint, graphicExtent);
	
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
					
					double angle = Math.atan2(dy, dx);

					if (!this.contains(graphicExtent, pixelCoordinateStartPoint))
						continue;
					
					distance = distance * scale;
					
					// clipping line, if one of the points is outside
					double layerDiagoal = Math.hypot(width, height);
					if (distance > 1.05 * layerDiagoal) {
						distance = 1.05 * layerDiagoal;
					}
					
					PixelCoordinate vectorEndCoordinate = new PixelCoordinate(xs + distance * dx, ys + distance * dy);

//					graphicsContext.setStroke(this.getColor());
//					graphicsContext.setFill(this.getColor());
//					graphicsContext.setLineWidth(lineWidth);
//					graphicsContext.setLineDashes(null);
					
					graphicsContext.strokeLine(
							xs,
							ys,
							xs + distance * dx,
							ys + distance * dy
							);

					SymbolBuilder.drawSymbol(graphicsContext, vectorEndCoordinate, arrowSymbolType, symbolSize, angle);
				}
			}
		}
	}

	@Override
	public String toString() {
		switch(this.getLayerType()) {
		case POINT_RESIDUAL_HORIZONTAL:
			return i18n.getString("PointResidualArrowLayer.type.horizontal", "Horizontal point residuals");
		case POINT_RESIDUAL_VERTICAL:
			return i18n.getString("PointResidualArrowLayer.type.vertical", "Vertical point residuals");
		default:
			return "";
		}
	}

	@Override
	public GraphicExtent getMaximumGraphicExtent() {
		GraphicExtent extent = new GraphicExtent();
		extent.reset();
		return extent;
	}

	@Override
	public void clearLayer() {} // no clearing --> use data from reference layer

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
		
		for (PointLayer layer : this.referenceLayers) {
			if (layer.isVisible() && layer.hasContent()) {
				for (GraphicPoint startPoint : layer.getPoints()) {
					if (startPoint.isVisible())
						if ((layerType == LayerType.POINT_RESIDUAL_HORIZONTAL && startPoint.getDimension() != 1) ||
								(layerType == LayerType.POINT_RESIDUAL_VERTICAL && startPoint.getDimension() != 2))
							return true;
				}
			}
		}
		
		return false;
	}
}
