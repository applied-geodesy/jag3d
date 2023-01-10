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

import java.util.ArrayList;
import java.util.List;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class LegendLayer extends Layer implements FontLayer {
	private ObservableList<Layer> layers;
	
	private DoubleProperty fontSize   = new SimpleDoubleProperty(10);
	private StringProperty fontFamily = new SimpleStringProperty(Font.getDefault().getFamily());
	private ObjectProperty<Color> fontColor = new SimpleObjectProperty<Color>(Color.SLATEGREY);
	private ObjectProperty<Color> fontBackgroundColor = new SimpleObjectProperty<Color>(Color.rgb(255, 255, 255, 0.9));
	private ObjectProperty<LegendPositionType> legendPositionType = new SimpleObjectProperty<LegendPositionType>(LegendPositionType.NORTH_EAST);
	
	LegendLayer(LayerType layerType, ObservableList<Layer> layers) {
		super(layerType);
		if (layerType != LayerType.LEGEND)
			throw new IllegalArgumentException("Error, unsupported layer type " + layerType);
		this.layers = layers;
		
		Color symbolColor, fontColor;
		String fontFamily = null;
		LegendPositionType legendPositionType;
		double lineWidth = -1, fontSize = -1;
		boolean visible = Boolean.TRUE;

		fontFamily = PROPERTIES.getProperty("LEGEND_FONT_FAMILY", "System");
		try {
			fontColor = Color.web(PROPERTIES.getProperty("LEGEND_FONT_COLOR", "#2F4F4F"));
		} catch (Exception e) {
			fontColor = Color.web("#2F4F4F");
		}
		
		try {
			symbolColor = Color.web(PROPERTIES.getProperty("LEGEND_FONT_COLOR", "#778899"));
		} catch (Exception e) {
			symbolColor = Color.web("#778899");
		}
		
		try {
			legendPositionType = LegendPositionType.valueOf(PROPERTIES.getProperty("LEGEND_POSITION", "NORTH_EAST"));
		} catch (Exception e) {
			legendPositionType = LegendPositionType.NORTH_EAST;
		}
		
		try { visible   = PROPERTIES.getProperty("LEGEND_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}
		try { fontSize  = Double.parseDouble(PROPERTIES.getProperty("LEGEND_FONT_SIZE")); } catch (Exception e) {}
		try { lineWidth = Double.parseDouble(PROPERTIES.getProperty("LEGEND_LINE_WIDTH")); } catch (Exception e) {}

		lineWidth = lineWidth >= 0 ? lineWidth : 0.25;
		fontSize = fontSize >= 0 ? fontSize : 12.0;
		fontFamily = fontFamily != null ? fontFamily : "System";

		this.setColor(symbolColor);
		this.setLineWidth(lineWidth);
		this.setLegendPositionType(legendPositionType);

		this.setFontSize(fontSize);
		this.setFontFamily(fontFamily);
		this.setFontColor(fontColor);
		
		this.setVisible(visible);
	}
	
	@Override
	public void draw(GraphicsContext graphicsContext, GraphicExtent graphicExtent) {
		Color fontColor           = this.getFontColor();
		String fontFamily         = this.getFontFamily();
		double fontSize           = this.getFontSize();
		double lineWidth          = this.getLineWidth();
		Color lineColor           = this.getColor();
		Color fontBackgroundColor = this.getFontBackgroundColor();
		
		Font font = Font.font(fontFamily, FontWeight.NORMAL, FontPosture.REGULAR, fontSize);
		
		double sketchWidth  = graphicExtent.getDrawingBoardWidth();
		double sketchHeight = graphicExtent.getDrawingBoardHeight();

		double xMargin = 10, yMargin = 10;
		double xPadding = 5, yPadding = 5;
		
		int cnt = 0;
		double textColumnWidth   = 0;
		double symbolColumnWidth = 3 * 0.7 * fontSize;
		double textHeight = 0;
		
		List<Layer> legendLayers = new ArrayList<Layer>(this.layers.size());

		for (Layer layer : this.layers) {
			if (layer == this || layer.getLayerType() == LayerType.LEGEND || !layer.isVisible() || !layer.hasContent())
				continue;

			legendLayers.add(layer);
			double textSize[] = this.getTextSize(font, layer.toString());
			textColumnWidth   = Math.max(textColumnWidth,  textSize[0]);
			textHeight        = Math.max(textHeight, textSize[1]);
			cnt++;
		}
		
		textHeight = textHeight + 3;

		double legendHeight = cnt * textHeight + 2 * yPadding;
		double legendWidth  = textColumnWidth + symbolColumnWidth + 3 * xPadding;
		
		if (cnt == 0 || (legendWidth + xMargin) > sketchWidth || (legendHeight + yMargin) > sketchHeight)
			return;
		
		graphicsContext.setLineCap(StrokeLineCap.BUTT);
		graphicsContext.setLineDashes(null);
		
		// Draw legend box
		graphicsContext.setLineWidth(lineWidth);
		graphicsContext.setStroke(fontBackgroundColor);
		graphicsContext.setFill(fontBackgroundColor);
		
		PixelCoordinate leftCorner = this.getLeftUpperCorner(graphicExtent, legendWidth, legendHeight, xMargin, yMargin);
		double xLeftUpperCorner = leftCorner.getX(); 
		double yLeftUpperCorner = leftCorner.getY();
		
		// x, y, w, h, arcWidth, arcHeight
		graphicsContext.fillRoundRect(xLeftUpperCorner, yLeftUpperCorner, legendWidth, legendHeight, 5, 5);

		// Draw legend box border		
		graphicsContext.setStroke(lineColor);
		graphicsContext.setFill(lineColor);
		graphicsContext.strokeRoundRect(xLeftUpperCorner, yLeftUpperCorner, legendWidth, legendHeight, 5, 5);

		double x = sketchWidth - legendWidth;
		double y = yLeftUpperCorner + textHeight; //  + this.yPadding
		// draw visible legend items
		int length = legendLayers.size();
		for (int i = length - 1; i >= 0; i--) {
			Layer layer = legendLayers.get(i); 

			x = xLeftUpperCorner + xPadding;

			layer.drawLegendSymbol(graphicsContext, graphicExtent, new PixelCoordinate(x, y - 0.3 * textHeight), 0.7 * fontSize, symbolColumnWidth);

			x += symbolColumnWidth + xPadding;

			graphicsContext.setStroke(fontColor);
			graphicsContext.setFill(fontColor);
			graphicsContext.setFont(font);
			graphicsContext.setTextBaseline(VPos.BASELINE);
			graphicsContext.setTextAlign(TextAlignment.LEFT);
			graphicsContext.fillText(layer.toString(), x, y);
			
			y += textHeight;
		}
	}
	
	private PixelCoordinate getLeftUpperCorner(GraphicExtent graphicExtent, double legendWidth, double legendHeight, double xMargin, double yMargin) {
		double sketchWidth  = graphicExtent.getDrawingBoardWidth();
		double sketchHeight = graphicExtent.getDrawingBoardHeight();
		
		double xLeftUpperCorner = 0, yLeftUpperCorner = 0;
		
		switch (this.legendPositionType.get()) {
		case NORTH:
			xLeftUpperCorner = 0.5 * (sketchWidth - legendWidth);
			yLeftUpperCorner = yMargin;
			break;
			
		case SOUTH:
			xLeftUpperCorner = 0.5 * sketchWidth - 0.5 * legendWidth;
			yLeftUpperCorner = sketchHeight - (legendHeight + 5*yMargin);
			break;
			
		case EAST:
			xLeftUpperCorner = sketchWidth - (legendWidth + xMargin);
			yLeftUpperCorner = 0.5 * (sketchHeight - legendHeight);
			break;
			
		case WEST:
			xLeftUpperCorner = xMargin;
			yLeftUpperCorner = 0.5 * (sketchHeight - legendHeight);
			break;
		
		
		case NORTH_EAST:
			xLeftUpperCorner = sketchWidth - (legendWidth + xMargin);
			yLeftUpperCorner = yMargin;
			break;
			
		case NORTH_WEST:
			xLeftUpperCorner = xMargin;
			yLeftUpperCorner = yMargin;
			break;

		case SOUTH_EAST:
			xLeftUpperCorner = sketchWidth - (legendWidth + xMargin);
			yLeftUpperCorner = sketchHeight - (legendHeight + 5*yMargin);
			break;
			
		case SOUTH_WEST:
			xLeftUpperCorner = xMargin;
			yLeftUpperCorner = sketchHeight - (legendHeight + 5*yMargin);
			break;
		}
		
		return new PixelCoordinate(xLeftUpperCorner, yLeftUpperCorner);
	}

	@Override
	public GraphicExtent getMaximumGraphicExtent() {
		return new GraphicExtent();
	}

	@Override
	public void clearLayer() {}
	
	@Override
	public boolean hasContent() {
		for (Layer layer : this.layers) {
			if (layer != this && layer.isVisible() && layer.hasContent())
				return true;
		}
		return false;
	}
	
	@Override
	public void drawLegendSymbol(GraphicsContext graphicsContext, GraphicExtent graphicExtent, PixelCoordinate pixelCoordinate, double symbolHeight, double symbolWidth) {}
	
	private double[] getTextSize(Font font, String str) {
		// https://stackoverflow.com/questions/32237048/javafx-fontmetrics
	    Text text = new Text(str);
	    text.setFont(font);
	    text.setWrappingWidth(0);
	    text.setLineSpacing(0);
	    Bounds textBounds = text.getBoundsInLocal();
	    Rectangle stencil = new Rectangle(textBounds.getMinX(), textBounds.getMinY(), textBounds.getWidth(), textBounds.getHeight());
	    Shape intersection = Shape.intersect(text, stencil);
	    textBounds = intersection.getBoundsInLocal();
	    return new double[] {
	    		textBounds.getWidth(), 
	    		textBounds.getHeight()
	    };
	}

	public final ObjectProperty<LegendPositionType> legendPositionTypeProperty() {
		return this.legendPositionType;
	}

	public final LegendPositionType getLegendPositionType() {
		return this.legendPositionTypeProperty().get();
	}

	public final void setLegendPositionType(final LegendPositionType legendPositionType) {
		this.legendPositionTypeProperty().set(legendPositionType);
	}
	
	@Override
	public final ObjectProperty<Color> fontColorProperty() {
		return this.fontColor;
	}

	@Override
	public final Color getFontColor() {
		return this.fontColorProperty().get();
	}

	@Override
	public final void setFontColor(final Color textColor) {
		this.fontColorProperty().set(textColor);
	}
	
	@Override
	public final ObjectProperty<Color> fontBackgroundColorProperty() {
		return this.fontBackgroundColor;
	}

	@Override
	public final Color getFontBackgroundColor() {
		return this.fontBackgroundColorProperty().get();
	}

	@Override
	public final void setFontBackgroundColor(final Color fontBackgroundColor) {
		this.fontBackgroundColorProperty().set(fontBackgroundColor);
	}

	@Override
	public final DoubleProperty fontSizeProperty() {
		return this.fontSize;
	}

	@Override
	public final double getFontSize() {
		return this.fontSizeProperty().get();
	}

	@Override
	public final void setFontSize(final double fontSize) {
		this.fontSizeProperty().set(fontSize);
	}

	@Override
	public StringProperty fontFamilyProperty() {
		return this.fontFamily;
	}

	@Override
	public String getFontFamily() {
		return this.fontFamilyProperty().get();
	}

	@Override
	public void setFontFamily(final String fontFamily) {
		this.fontFamilyProperty().set(fontFamily);
	}

	@Override
	public String toString() {
		return i18n.getString("LegendLayer.type", "Legende");
	}
}
