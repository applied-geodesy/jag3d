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
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.PointSymbolType;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.sql.GraphicPoint;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class PointLayer extends Layer {
	private DoubleProperty fontSize   = new SimpleDoubleProperty(10);
	private StringProperty fontFamily = new SimpleStringProperty(Font.getDefault().getFamily());
	private ObjectProperty<Color> fontColor = new SimpleObjectProperty<Color>(Color.DIMGREY);
	private ObjectProperty<PointSymbolType> pointSymbolType = new SimpleObjectProperty<PointSymbolType>(PointSymbolType.STROKED_CIRCLE);
	private List<GraphicPoint> points = FXCollections.observableArrayList();
	
	private BooleanProperty point1DVisible = new SimpleBooleanProperty(Boolean.FALSE);
	private BooleanProperty point2DVisible = new SimpleBooleanProperty(Boolean.TRUE);
	private BooleanProperty point3DVisible = new SimpleBooleanProperty(Boolean.TRUE);
	
	PointLayer(LayerType layerType, GraphicExtent currentGraphicExtent) {
		super(layerType, currentGraphicExtent);
		
		Color color;
		PointSymbolType pointSymbolType;
		double symbolSize = -1, lineWidth = -1;
		
		switch(layerType) {			
		case REFERENCE_POINT_APRIORI:
			try {
				color = Color.web(PROPERTIES.getProperty("REFERENCE_POINT_APRIORI_COLOR", "#90ee90"));
			}
			catch (Exception e) {
				color = Color.web("#90ee90"); //Color.LIGHTGREEN;
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("REFERENCE_POINT_APRIORI_SYMBOL_TYPE", "STROKED_SQUARE"));
			}catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("REFERENCE_POINT_APRIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;
			
			try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("REFERENCE_POINT_APRIORI_LINE_WIDTH")); } catch (Exception e) {}
			lineWidth = lineWidth >= 0 ? lineWidth : 1.5;

			break;
			
		case REFERENCE_POINT_APOSTERIORI:
			try {
				color = Color.web(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_COLOR", "#006400"));
			}
			catch (Exception e) {
				color = Color.web("#006400"); //Color.DARKGREEN;
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_SYMBOL_TYPE", "STROKED_SQUARE"));
			}catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;
			
			try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_LINE_WIDTH")); } catch (Exception e) {}
			lineWidth = lineWidth >= 0 ? lineWidth : 1.5;

			break;
			
		case STOCHASTIC_POINT_APRIORI:
			try {
				color = Color.web(PROPERTIES.getProperty("STOCHASTIC_POINT_APRIORI_COLOR", "#ffd700"));
			}
			catch (Exception e) {
				color = Color.web("#ffd700"); //Color.GOLD;
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("STOCHASTIC_POINT_APRIORI_SYMBOL_TYPE", "STROKED_UPRIGHT_TRIANGLE"));
			}catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("STOCHASTIC_POINT_APRIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;
			
			try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("STOCHASTIC_POINT_APRIORI_LINE_WIDTH")); } catch (Exception e) {}
			lineWidth = lineWidth >= 0 ? lineWidth : 1.5;

			break;
			
		case STOCHASTIC_POINT_APOSTERIORI:
			try {
				color = Color.web(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_COLOR", "#daa520"));
			}
			catch (Exception e) {
				color = Color.web("#daa520"); //Color.GOLDENROD;
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_SYMBOL_TYPE", "STROKED_UPRIGHT_TRIANGLE"));
			}catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;
			
			try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_LINE_WIDTH")); } catch (Exception e) {}
			lineWidth = lineWidth >= 0 ? lineWidth : 1.5;

			break;
			
		case DATUM_POINT_APRIORI:
			try {
				color = Color.web(PROPERTIES.getProperty("DATUM_POINT_APRIORI_COLOR", "#87ceeb"));
			}
			catch (Exception e) {
				color = Color.web("#87ceeb"); //Color.SKYBLUE;
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("DATUM_POINT_APRIORI_SYMBOL_TYPE", "STROKED_HEXAGON"));
			}catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("DATUM_POINT_APRIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;
			
			try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("DATUM_POINT_APRIORI_LINE_WIDTH")); } catch (Exception e) {}
			lineWidth = lineWidth >= 0 ? lineWidth : 1.5;

			break;
			
		case DATUM_POINT_APOSTERIORI:
			try {
				color = Color.web(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_COLOR", "#1e90ff"));
			}
			catch (Exception e) {
				color = Color.web("#1e90ff"); //Color.DODGERBLUE;
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_SYMBOL_TYPE", "STROKED_HEXAGON"));
			}catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}
			
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;
			
			try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_LINE_WIDTH")); } catch (Exception e) {}
			lineWidth = lineWidth >= 0 ? lineWidth : 1.5;

			break;
			
		case NEW_POINT_APRIORI:
			try {
				color = Color.web(PROPERTIES.getProperty("NEW_POINT_APRIORI_COLOR", "#c480c4"));
			}
			catch (Exception e) {
				color = Color.web("#c480c4");
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("NEW_POINT_APRIORI_SYMBOL_TYPE", "STROKED_CIRCLE"));
			}catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}
			
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("NEW_POINT_APRIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;
	
			try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("NEW_POINT_APRIORI_LINE_WIDTH")); } catch (Exception e) {}
			lineWidth = lineWidth >= 0 ? lineWidth : 1.5;

			break;
			
		case NEW_POINT_APOSTERIORI:
			try {
				color = Color.web(PROPERTIES.getProperty("NEW_POINT_APOSTERIORI_COLOR", "#dda0dd"));
			}
			catch (Exception e) {
				color = Color.web("#dda0dd"); //Color.PLUM;
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("NEW_POINT_APOSTERIORI_SYMBOL_TYPE", "STROKED_CIRCLE"));
			}catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}
			
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("NEW_POINT_APOSTERIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;
			
			try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("NEW_POINT_APOSTERIORI_LINE_WIDTH")); } catch (Exception e) {}
			lineWidth = lineWidth >= 0 ? lineWidth : 1.5;

			break;

		default:
			throw new IllegalArgumentException("Error, unsupported layer type " + layerType);		
		}
		
		this.setSymbolType(pointSymbolType);
		this.setColor(color);
		this.setLineWidth(1.5);
		this.setSymbolSize(symbolSize);
		
		this.addLayerPropertyChangeListener(this.fontColorProperty());
		this.addLayerPropertyChangeListener(this.fontFamilyProperty());
		this.addLayerPropertyChangeListener(this.fontSizeProperty());
		this.addLayerPropertyChangeListener(this.pointSymbolTypeProperty());

	}
	
	public void setPoints(List<GraphicPoint> points) {
		// clear and unbind
		for (GraphicPoint point : this.points)
			point.visibleProperty().unbind();
		this.points.clear();
		
		if (points != null) {
			for (GraphicPoint point : points) {
				switch(point.getDimension()) {
				case 1:
					point.visibleProperty().bind(this.point1DVisibleProperty().and(this.visibleProperty()));
					break;
				case 2:
					point.visibleProperty().bind(this.point2DVisibleProperty().and(this.visibleProperty()));
					break;
				case 3:
					point.visibleProperty().bind(this.point3DVisibleProperty().and(this.visibleProperty()));
					break;
				default:
					continue; // wrong dimension
				}
			}
			this.points.addAll(points);
		}
	}
	
	List<GraphicPoint> getPoints() {
		return this.points;
	}
	
	public void draw(GraphicExtent graphicExtent) {
		this.clearDrawingBoard();
		
		if (!this.isVisible() || this.points.isEmpty())
			return;

		GraphicsContext graphicsContext = this.getGraphicsContext2D();
		graphicsContext.setLineCap(StrokeLineCap.BUTT);

		PointSymbolType symbolType = this.getPointSymbolType();
		double symbolSize = this.getSymbolSize();
		double fontSize   = this.getFontSize();
		double lineWidth  = this.getLineWidth();
		String fontFamily = this.getFontFamily();
		graphicsContext.setLineWidth(lineWidth);
		
		// draw points
		for (GraphicPoint point : this.points) {
			if (!point.isVisible())
				continue;
			
			PixelCoordinate pixelCoordinate = GraphicExtent.toPixelCoordinate(point.getCoordinate(), graphicExtent);

			if (this.contains(pixelCoordinate)) {
				// set layer color
				graphicsContext.setStroke(this.getColor());
				graphicsContext.setFill(this.getColor());
				SymbolBuilder.drawSymbol(graphicsContext, pixelCoordinate, symbolType, symbolSize);
				
				// set text color
				graphicsContext.setStroke(this.getFontColor());
				graphicsContext.setFill(this.getFontColor());
				graphicsContext.setFont(Font.font(fontFamily, FontWeight.NORMAL, FontPosture.REGULAR, fontSize));		
				graphicsContext.fillText(point.getName().trim(), 
						pixelCoordinate.getX() + 0.5 * (symbolSize + lineWidth + 1), 
						pixelCoordinate.getY() + 0.5 * (symbolSize + fontSize + lineWidth + 1));
			}
		}
	}
	
	public ObjectProperty<PointSymbolType> pointSymbolTypeProperty() {
		return this.pointSymbolType;
	}

	public PointSymbolType getPointSymbolType() {
		return this.pointSymbolTypeProperty().get();
	}

	public void setSymbolType(final PointSymbolType pointSymbolType) {
		this.pointSymbolTypeProperty().set(pointSymbolType);
	}

	public final ObjectProperty<Color> fontColorProperty() {
		return this.fontColor;
	}

	public final Color getFontColor() {
		return this.fontColorProperty().get();
	}

	public final void setFontColor(final Color textColor) {
		this.fontColorProperty().set(textColor);
	}

	public final DoubleProperty fontSizeProperty() {
		return this.fontSize;
	}

	public final double getFontSize() {
		return this.fontSizeProperty().get();
	}

	public final void setFontSize(final double fontSize) {
		this.fontSizeProperty().set(fontSize);
	}
	
	public StringProperty fontFamilyProperty() {
		return this.fontFamily;
	}

	public String getFontFamily() {
		return this.fontFamilyProperty().get();
	}

	public void setFontFamily(final String fontFamily) {
		this.fontFamilyProperty().set(fontFamily);
	}
	
	@Override
	public String toString() {
		switch(this.getLayerType()) {
		case DATUM_POINT_APOSTERIORI:
			return i18n.getString("PointAprioriLayer.type.datum.aposteriori", "Datum points (a-posteriori)");
		case DATUM_POINT_APRIORI:
			return i18n.getString("PointAprioriLayer.type.datum.apriori", "Datum points (a-priori)");
		case NEW_POINT_APOSTERIORI:
			return i18n.getString("PointAprioriLayer.type.new.aposteriori", "New points (a-posteriori)");
		case NEW_POINT_APRIORI:
			return i18n.getString("PointAprioriLayer.type.new.apriori", "New points (a-priori)");
		case REFERENCE_POINT_APOSTERIORI:
			return i18n.getString("PointAprioriLayer.type.reference.aposteriori", "Reference points (a-posteriori)");
		case REFERENCE_POINT_APRIORI:
			return i18n.getString("PointAprioriLayer.type.reference.apriori", "Reference points (a-priori)");
		case STOCHASTIC_POINT_APOSTERIORI:
			return i18n.getString("PointAprioriLayer.type.stochastic.aposteriori", "Stochastic points (a-posteriori)");
		case STOCHASTIC_POINT_APRIORI:
			return i18n.getString("PointAprioriLayer.type.stochastic.apriori", "Stochastic points (a-priori)");
		default:
			return "";		
		}
	}

	public BooleanProperty point1DVisibleProperty() {
		return this.point1DVisible;
	}

	public boolean isPoint1DVisible() {
		return this.point1DVisibleProperty().get();
	}

	public void setPoint1DVisible(final boolean point1DVisible) {
		this.point1DVisibleProperty().set(point1DVisible);
	}

	public BooleanProperty point2DVisibleProperty() {
		return this.point2DVisible;
	}

	public boolean isPoint2DVisible() {
		return this.point2DVisibleProperty().get();
	}

	public void setPoint2DVisible(final boolean point2DVisible) {
		this.point2DVisibleProperty().set(point2DVisible);
	}

	public BooleanProperty point3DVisibleProperty() {
		return this.point3DVisible;
	}

	public boolean isPoint3DVisible() {
		return this.point3DVisibleProperty().get();
	}

	public void setPoint3DVisible(final boolean point3DVisible) {
		this.point3DVisibleProperty().set(point3DVisible);
	}

	@Override
	public void clearLayer() {
		this.clearDrawingBoard();
		this.points.clear();
	}

	@Override
	public GraphicExtent getMaximumGraphicExtent() {
		GraphicExtent graphicExtent = new GraphicExtent();
		graphicExtent.reset();
		if (this.points != null) {
			for (GraphicPoint point : this.points) 
				if (point.isVisible())
					graphicExtent.merge(point.getCoordinate());
		}
		return graphicExtent;
	}
}
