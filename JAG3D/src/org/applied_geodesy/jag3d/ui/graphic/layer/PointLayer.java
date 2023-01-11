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
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlight;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightType;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.PointSymbolType;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.sql.GraphicPoint;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class PointLayer extends Layer implements HighlightableLayer, FontLayer {
	private DoubleProperty fontSize   = new SimpleDoubleProperty(10);
	private StringProperty fontFamily = new SimpleStringProperty(Font.getDefault().getFamily());
	private ObjectProperty<Color> fontColor = new SimpleObjectProperty<Color>(Color.DIMGREY);
	private ObjectProperty<Color> fontBackgroundColor = new SimpleObjectProperty<Color>(Color.rgb(255, 255, 255, 0.25));
	private ObjectProperty<PointSymbolType> pointSymbolType = new SimpleObjectProperty<PointSymbolType>(PointSymbolType.STROKED_CIRCLE);
	private List<GraphicPoint> points = FXCollections.observableArrayList();

	private ObjectProperty<Color> highlightColor = new SimpleObjectProperty<Color>(Color.ORANGERED); //#FF4500
	private DoubleProperty highlightLineWidth    = new SimpleDoubleProperty(2.5);
	private ObjectProperty<TableRowHighlightType> highlightType = new SimpleObjectProperty<TableRowHighlightType>(TableRowHighlightType.NONE);

	private BooleanProperty point1DVisible = new SimpleBooleanProperty(Boolean.FALSE);
	private BooleanProperty point2DVisible = new SimpleBooleanProperty(Boolean.TRUE);
	private BooleanProperty point3DVisible = new SimpleBooleanProperty(Boolean.TRUE);

	PointLayer(LayerType layerType) {
		super(layerType);

		Color symbolColor, fontColor, highlightColor;
		TableRowHighlightType highlightType;
		PointSymbolType pointSymbolType;
		String fontFamily = null;
		double symbolSize = -1, lineWidth = -1, fontSize = -1, highlightLineWidth = -1;
		boolean visible = Boolean.TRUE;

		switch(layerType) {			
		case REFERENCE_POINT_APRIORI:
			fontFamily = PROPERTIES.getProperty("REFERENCE_POINT_APRIORI_FONT_FAMILY", "System");

			try {
				fontColor = Color.web(PROPERTIES.getProperty("REFERENCE_POINT_APRIORI_FONT_COLOR", "#696969"));
			} catch (Exception e) {
				fontColor = Color.web("#696969");
			}

			try {
				symbolColor = Color.web(PROPERTIES.getProperty("REFERENCE_POINT_APRIORI_COLOR", "#90ee90"));
			} catch (Exception e) {
				symbolColor = Color.web("#90ee90");
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("REFERENCE_POINT_APRIORI_SYMBOL_TYPE", "STROKED_SQUARE"));
			} catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { fontSize   = Double.parseDouble(PROPERTIES.getProperty("REFERENCE_POINT_APRIORI_FONT_SIZE")); } catch (Exception e) {}
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("REFERENCE_POINT_APRIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			try { lineWidth  = Double.parseDouble(PROPERTIES.getProperty("REFERENCE_POINT_APRIORI_LINE_WIDTH")); } catch (Exception e) {}
			try { visible    = PROPERTIES.getProperty("REFERENCE_POINT_APRIORI_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}

			break;

		case REFERENCE_POINT_APOSTERIORI:
			fontFamily = PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_FONT_FAMILY", "System");

			try {
				fontColor = Color.web(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_FONT_COLOR", "#696969"));
			} catch (Exception e) {
				fontColor = Color.web("#696969");
			}

			try {
				symbolColor = Color.web(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_COLOR", "#006400"));
			} catch (Exception e) {
				symbolColor = Color.web("#006400");
			}
			
			try {
				highlightType = TableRowHighlightType.valueOf(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_HIGHLIGHT_TYPE", "NONE"));
				if (highlightType == null)
					highlightType = TableRowHighlightType.NONE;
			} catch (Exception e) {
				highlightType = TableRowHighlightType.NONE;
			}

			try {
				highlightColor = Color.web(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_HIGHLIGHT_COLOR", "#FF4500"));
			} catch (Exception e) {
				highlightColor = Color.web("#FF4500");
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_SYMBOL_TYPE", "STROKED_SQUARE"));
			} catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { fontSize   = Double.parseDouble(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_FONT_SIZE")); } catch (Exception e) {}
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			try { lineWidth  = Double.parseDouble(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_LINE_WIDTH")); } catch (Exception e) {}
			try { visible    = PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}
			
			try { highlightLineWidth = Double.parseDouble(PROPERTIES.getProperty("REFERENCE_POINT_APOSTERIORI_HIGHLIGHT_LINE_WIDTH")); } catch (Exception e) {}
			this.setHighlightLineWidth(highlightLineWidth >= 0 ? highlightLineWidth : 2.5);
			this.setHighlightColor(highlightColor);
			this.setHighlightType(highlightType);

			break;

		case STOCHASTIC_POINT_APRIORI:
			fontFamily = PROPERTIES.getProperty("STOCHASTIC_POINT_APRIORI_FONT_FAMILY", "System");

			try {
				fontColor = Color.web(PROPERTIES.getProperty("STOCHASTIC_POINT_APRIORI_FONT_COLOR", "#696969"));
			} catch (Exception e) {
				fontColor = Color.web("#696969");
			}

			try {
				symbolColor = Color.web(PROPERTIES.getProperty("STOCHASTIC_POINT_APRIORI_COLOR", "#ffd700"));
			} catch (Exception e) {
				symbolColor = Color.web("#ffd700");
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("STOCHASTIC_POINT_APRIORI_SYMBOL_TYPE", "STROKED_UPRIGHT_TRIANGLE"));
			} catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { fontSize   = Double.parseDouble(PROPERTIES.getProperty("STOCHASTIC_POINT_APRIORI_FONT_SIZE")); } catch (Exception e) {}
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("STOCHASTIC_POINT_APRIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			try { lineWidth  = Double.parseDouble(PROPERTIES.getProperty("STOCHASTIC_POINT_APRIORI_LINE_WIDTH")); } catch (Exception e) {}
			try { visible    = PROPERTIES.getProperty("STOCHASTIC_POINT_APRIORI_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}

			break;

		case STOCHASTIC_POINT_APOSTERIORI:
			fontFamily = PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_FONT_FAMILY", "System");

			try {
				fontColor = Color.web(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_FONT_COLOR", "#696969"));
			} catch (Exception e) {
				fontColor = Color.web("#696969");
			}

			try {
				symbolColor = Color.web(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_COLOR", "#daa520"));
			} catch (Exception e) {
				symbolColor = Color.web("#daa520");
			}
			
			try {
				highlightType = TableRowHighlightType.valueOf(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_HIGHLIGHT_TYPE", "NONE"));
				if (highlightType == null)
					highlightType = TableRowHighlightType.NONE;
			} catch (Exception e) {
				highlightType = TableRowHighlightType.NONE;
			}

			try {
				highlightColor = Color.web(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_HIGHLIGHT_COLOR", "#FF4500"));
			} catch (Exception e) {
				highlightColor = Color.web("#FF4500");
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_SYMBOL_TYPE", "STROKED_UPRIGHT_TRIANGLE"));
			} catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { fontSize   = Double.parseDouble(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_FONT_SIZE")); } catch (Exception e) {}
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			try { lineWidth  = Double.parseDouble(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_LINE_WIDTH")); } catch (Exception e) {}
			try { visible    = PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}
			
			try { highlightLineWidth = Double.parseDouble(PROPERTIES.getProperty("STOCHASTIC_POINT_APOSTERIORI_HIGHLIGHT_LINE_WIDTH")); } catch (Exception e) {}
			this.setHighlightLineWidth(highlightLineWidth >= 0 ? highlightLineWidth : 2.5);
			this.setHighlightColor(highlightColor);
			this.setHighlightType(highlightType);

			break;

		case DATUM_POINT_APRIORI:
			fontFamily = PROPERTIES.getProperty("DATUM_POINT_APRIORI_FONT_FAMILY", "System");

			try {
				fontColor = Color.web(PROPERTIES.getProperty("DATUM_POINT_APRIORI_FONT_COLOR", "#696969"));
			} catch (Exception e) {
				fontColor = Color.web("#696969");
			}

			try {
				symbolColor = Color.web(PROPERTIES.getProperty("DATUM_POINT_APRIORI_COLOR", "#87ceeb"));
			} catch (Exception e) {
				symbolColor = Color.web("#87ceeb");
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("DATUM_POINT_APRIORI_SYMBOL_TYPE", "STROKED_HEXAGON"));
			} catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { fontSize   = Double.parseDouble(PROPERTIES.getProperty("DATUM_POINT_APRIORI_FONT_SIZE")); } catch (Exception e) {}
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("DATUM_POINT_APRIORI_SYMBOL_SIZE")); } catch (Exception e) {}
			try { lineWidth  = Double.parseDouble(PROPERTIES.getProperty("DATUM_POINT_APRIORI_LINE_WIDTH")); } catch (Exception e) {}
			try { visible    = PROPERTIES.getProperty("DATUM_POINT_APRIORI_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}

			break;

		case DATUM_POINT_APOSTERIORI:
			fontFamily = PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_FONT_FAMILY", "System");

			try {
				fontColor = Color.web(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_FONT_COLOR", "#696969"));
			} catch (Exception e) {
				fontColor = Color.web("#696969");
			}

			try {
				symbolColor = Color.web(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_COLOR", "#1e90ff"));
			} catch (Exception e) {
				symbolColor = Color.web("#1e90ff");
			}
			
			try {
				highlightType = TableRowHighlightType.valueOf(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_HIGHLIGHT_TYPE", "NONE"));
				if (highlightType == null)
					highlightType = TableRowHighlightType.NONE;
			} catch (Exception e) {
				highlightType = TableRowHighlightType.NONE;
			}

			try {
				highlightColor = Color.web(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_HIGHLIGHT_COLOR", "#FF4500"));
			} catch (Exception e) {
				highlightColor = Color.web("#FF4500");
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_SYMBOL_TYPE", "STROKED_HEXAGON"));
			} catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { fontSize   = Double.parseDouble(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_FONT_SIZE")); } catch (Exception e) {}
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_SYMBOL_SIZE")); } catch (Exception e) {}			
			try { lineWidth  = Double.parseDouble(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_LINE_WIDTH")); } catch (Exception e) {}
			try { visible    = PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}
			
			try { highlightLineWidth = Double.parseDouble(PROPERTIES.getProperty("DATUM_POINT_APOSTERIORI_HIGHLIGHT_LINE_WIDTH")); } catch (Exception e) {}
			this.setHighlightLineWidth(highlightLineWidth >= 0 ? highlightLineWidth : 2.5);
			this.setHighlightColor(highlightColor);
			this.setHighlightType(highlightType);

			break;

		case NEW_POINT_APRIORI:
			fontFamily = PROPERTIES.getProperty("NEW_POINT_APRIORI_FONT_FAMILY", "System");

			try {
				fontColor = Color.web(PROPERTIES.getProperty("NEW_POINT_APRIORI_FONT_COLOR", "#696969"));
			} catch (Exception e) {
				fontColor = Color.web("#696969");
			}

			try {
				symbolColor = Color.web(PROPERTIES.getProperty("NEW_POINT_APRIORI_COLOR", "#c480c4"));
			} catch (Exception e) {
				symbolColor = Color.web("#c480c4");
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("NEW_POINT_APRIORI_SYMBOL_TYPE", "STROKED_CIRCLE"));
			} catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { fontSize   = Double.parseDouble(PROPERTIES.getProperty("NEW_POINT_APRIORI_FONT_SIZE")); } catch (Exception e) {}
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("NEW_POINT_APRIORI_SYMBOL_SIZE")); } catch (Exception e) {}	
			try { lineWidth  = Double.parseDouble(PROPERTIES.getProperty("NEW_POINT_APRIORI_LINE_WIDTH")); } catch (Exception e) {}
			try { visible    = PROPERTIES.getProperty("NEW_POINT_APRIORI_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}

			break;

		case NEW_POINT_APOSTERIORI:
			fontFamily = PROPERTIES.getProperty("NEW_POINT_APOSTERIORI_FONT_FAMILY", "System");

			try {
				fontColor = Color.web(PROPERTIES.getProperty("NEW_POINT_APOSTERIORI_FONT_COLOR", "#696969"));
			} catch (Exception e) {
				fontColor = Color.web("#696969");
			}

			try {
				symbolColor = Color.web(PROPERTIES.getProperty("NEW_POINT_APOSTERIORI_COLOR", "#dda0dd"));
			} catch (Exception e) {
				symbolColor = Color.web("#dda0dd");
			}

			try {
				pointSymbolType = PointSymbolType.valueOf(PROPERTIES.getProperty("NEW_POINT_APOSTERIORI_SYMBOL_TYPE", "STROKED_CIRCLE"));
			} catch (Exception e) {
				pointSymbolType = PointSymbolType.STROKED_SQUARE;
			}

			try { fontSize   = Double.parseDouble(PROPERTIES.getProperty("NEW_POINT_APOSTERIORI_FONT_SIZE")); } catch (Exception e) {}
			try { symbolSize = Double.parseDouble(PROPERTIES.getProperty("NEW_POINT_APOSTERIORI_SYMBOL_SIZE")); } catch (Exception e) {}			
			try { lineWidth  = Double.parseDouble(PROPERTIES.getProperty("NEW_POINT_APOSTERIORI_LINE_WIDTH")); } catch (Exception e) {}
			try { visible    = PROPERTIES.getProperty("NEW_POINT_APOSTERIORI_VISIBLE").equalsIgnoreCase("TRUE"); } catch (Exception e) {}

			break;

		default:
			throw new IllegalArgumentException("Error, unsupported layer type " + layerType);		
		}

		symbolSize = symbolSize >= 0 ? symbolSize : SymbolBuilder.DEFAULT_SIZE;
		lineWidth = lineWidth >= 0 ? lineWidth : 1.5;
		fontSize = fontSize >= 0 ? fontSize : 10.0;
		fontFamily = fontFamily != null ? fontFamily : "System";

		this.setSymbolType(pointSymbolType);
		this.setColor(symbolColor);
		this.setLineWidth(lineWidth);
		this.setSymbolSize(symbolSize);

		this.setFontSize(fontSize);
		this.setFontFamily(fontFamily);
		this.setFontColor(fontColor);
		
		this.setVisible(visible);
	}

	@Override
	public void draw(GraphicsContext graphicsContext, GraphicExtent graphicExtent) {
		if (!this.isVisible() || this.points.isEmpty())
			return;

//		graphicsContext.setLineCap(StrokeLineCap.BUTT);
//		graphicsContext.setLineDashes(null);

		PointSymbolType symbolType = this.getPointSymbolType();
		double symbolSize = this.getSymbolSize();
		double fontSize   = this.getFontSize();
		String fontFamily = this.getFontFamily();
		Color fontBackgroundColor = this.getFontBackgroundColor();

		// draw points
		for (GraphicPoint point : this.points) {
			if (!point.isVisible())
				continue;

			PixelCoordinate pixelCoordinate = GraphicExtent.toPixelCoordinate(point.getCoordinate(), graphicExtent);

			if (this.contains(graphicExtent, pixelCoordinate)) {
				// set layer color and line width properties
				Color symbolColor = this.getColor();
				Color fontColor   = this.getFontColor();
				double lineWidth  = this.getLineWidth();
				
				TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
				double leftBoundary  = tableRowHighlight.getLeftBoundary(this.getHighlightType());
				double rightBoundary = tableRowHighlight.getRightBoundary(this.getHighlightType());

				switch(this.getHighlightType()) {
				case INFLUENCE_ON_POSITION:
					if (point.getMaxInfluenceOnPosition() > rightBoundary) {
						symbolColor = this.getHighlightColor();
						fontColor   = this.getHighlightColor();
						lineWidth   = this.getHighlightLineWidth();
					}
					break;
				case P_PRIO_VALUE:
					if (point.getPprio() < Math.log(leftBoundary / 100.0)) {
						symbolColor = this.getHighlightColor();
						fontColor   = this.getHighlightColor();
						lineWidth   = this.getHighlightLineWidth();
					}
					break;
				case REDUNDANCY:
					if (point.getMinRedundancy() < leftBoundary) {
						symbolColor = this.getHighlightColor();
						fontColor   = this.getHighlightColor();
						lineWidth   = this.getHighlightLineWidth();
					}
					break;
				case TEST_STATISTIC:
					if (point.isSignificant()) {
						symbolColor = this.getHighlightColor();
						fontColor   = this.getHighlightColor();
						lineWidth   = this.getHighlightLineWidth();
					}
					break;
				case GROSS_ERROR:
					if (point.isGrossErrorExceeded()) {
						symbolColor = this.getHighlightColor();
						fontColor   = this.getHighlightColor();
						lineWidth   = this.getHighlightLineWidth();
					}
					break;
				case NONE: // DEFAULT
					symbolColor = this.getColor();
					fontColor   = this.getFontColor();
					lineWidth   = this.getLineWidth();
					break;
				}

				this.drawPointSymbol(graphicsContext, pixelCoordinate, symbolColor, symbolType, symbolSize, lineWidth);
				this.drawPointText(graphicsContext, pixelCoordinate, point.getName().trim(), fontColor, fontBackgroundColor, fontFamily, symbolSize, lineWidth, fontSize);
			}
		}
	}
	
	private void drawPointSymbol(GraphicsContext graphicsContext, PixelCoordinate pixelCoordinate, Color color, PointSymbolType symbolType, double symbolSize, double lineWidth) {
		graphicsContext.setLineCap(StrokeLineCap.BUTT);
		graphicsContext.setLineDashes(null);
		
		graphicsContext.setLineWidth(lineWidth);
		graphicsContext.setStroke(color);
		graphicsContext.setFill(color);

		SymbolBuilder.drawSymbol(graphicsContext, pixelCoordinate, symbolType, symbolSize);
	}
	
	private void drawPointText(GraphicsContext graphicsContext, PixelCoordinate pixelCoordinate, String name, Color color, Color backgroundColor, String fontFamily, double symbolSize, double lineWidth, double fontSize) {
		// estimate text size
		Font font = Font.font(fontFamily, FontWeight.NORMAL, FontPosture.REGULAR, fontSize);
		Text text = new Text(name);
		text.setFont(font);
		text.setWrappingWidth(0);
	    text.setLineSpacing(0);
//	    Bounds textBounds = text.getBoundsInLocal();
//	    Rectangle stencil = new Rectangle(textBounds.getMinX(), textBounds.getMinY(), textBounds.getWidth(), textBounds.getHeight());
//	    Shape intersection = Shape.intersect(text, stencil);
//	    textBounds = intersection.getBoundsInLocal();
//		
//		double textWidth  = textBounds.getWidth() + 4; // text.getLayoutBounds().getWidth();
//		double textHeight = textBounds.getHeight()+ 4; // text.getLayoutBounds().getHeight();
		
	    // faster approach
		double textWidth  = text.getLayoutBounds().getWidth();
		double textHeight = text.getLayoutBounds().getHeight();
		
		double x0 = pixelCoordinate.getX() + 0.5 * (symbolSize + lineWidth);
		double y0 = pixelCoordinate.getY() + 0.5 * (symbolSize + lineWidth);
		
		graphicsContext.setLineCap(StrokeLineCap.BUTT);
		graphicsContext.setLineDashes(null);
		graphicsContext.setStroke(backgroundColor);
		graphicsContext.setFill(backgroundColor);
        graphicsContext.fillRoundRect(x0, y0, textWidth, textHeight, 2, 2);

		graphicsContext.setStroke(color);
		graphicsContext.setFill(color);
		graphicsContext.setFont(font);
		graphicsContext.setTextBaseline(VPos.CENTER);
		graphicsContext.setTextAlign(TextAlignment.CENTER);
		graphicsContext.fillText(name, 
				x0 + 0.5 * textWidth, 
				y0 + 0.5 * textHeight);
	}

	@Override
	public void clearLayer() {
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

	public void setPoints(List<GraphicPoint> points) {
		this.points.clear();
		if (points != null) {
			for (GraphicPoint point : points) {
				int dimension = point.getDimension();
				switch(dimension) {
				case 1:
					point.setVisible(this.isPoint1DVisible());
					break;
				case 2:
					point.setVisible(this.isPoint2DVisible());
					break;
				case 3:
					point.setVisible(this.isPoint3DVisible());
					break;
				default:
					continue;
				}
				this.points.add(point);
			}
		}
	}

	public List<GraphicPoint> getPoints() {
		return this.points;
	}

	private void setPointVisible(int dimension, boolean visible) {
		for (GraphicPoint point : this.points) {
			if (point.getDimension() == dimension)
				point.setVisible(visible);
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
		switch(this.getLayerType()) {
		case DATUM_POINT_APOSTERIORI:
			return i18n.getString("PointLayer.type.datum.aposteriori", "Datum points (a-posteriori)");
		case DATUM_POINT_APRIORI:
			return i18n.getString("PointLayer.type.datum.apriori", "Datum points (a-priori)");
		case NEW_POINT_APOSTERIORI:
			return i18n.getString("PointLayer.type.new.aposteriori", "New points (a-posteriori)");
		case NEW_POINT_APRIORI:
			return i18n.getString("PointLayer.type.new.apriori", "New points (a-priori)");
		case REFERENCE_POINT_APOSTERIORI:
			return i18n.getString("PointLayer.type.reference.aposteriori", "Reference points (a-posteriori)");
		case REFERENCE_POINT_APRIORI:
			return i18n.getString("PointLayer.type.reference.apriori", "Reference points (a-priori)");
		case STOCHASTIC_POINT_APOSTERIORI:
			return i18n.getString("PointLayer.type.stochastic.aposteriori", "Stochastic points (a-posteriori)");
		case STOCHASTIC_POINT_APRIORI:
			return i18n.getString("PointLayer.type.stochastic.apriori", "Stochastic points (a-priori)");
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
		this.setPointVisible(1, point1DVisible);
		this.point1DVisibleProperty().set(point1DVisible);
	}

	public BooleanProperty point2DVisibleProperty() {
		return this.point2DVisible;
	}

	public boolean isPoint2DVisible() {
		return this.point2DVisibleProperty().get();
	}

	public void setPoint2DVisible(final boolean point2DVisible) {
		this.setPointVisible(2, point2DVisible);
		this.point2DVisibleProperty().set(point2DVisible);
	}

	public BooleanProperty point3DVisibleProperty() {
		return this.point3DVisible;
	}

	public boolean isPoint3DVisible() {
		return this.point3DVisibleProperty().get();
	}

	public void setPoint3DVisible(final boolean point3DVisible) {
		this.setPointVisible(3, point3DVisible);
		this.point3DVisibleProperty().set(point3DVisible);
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
	public void drawLegendSymbol(GraphicsContext graphicsContext, GraphicExtent graphicExtent, PixelCoordinate pixelCoordinate, double symbolHeight, double symbolWidth) {
		if (this.contains(graphicExtent, pixelCoordinate)) {
			PointSymbolType symbolType = this.getPointSymbolType();
			Color symbolColor = this.getColor();
			double lineWidth  = this.getLineWidth();
			double symbolSize = symbolHeight;
			
			this.drawPointSymbol(graphicsContext, new PixelCoordinate(pixelCoordinate.getX() + 0.5 * symbolWidth, pixelCoordinate.getY()), symbolColor, symbolType, symbolSize, lineWidth);
		}
	}

	@Override
	public boolean hasContent() {
		return super.hasContent() && this.points != null && !this.points.isEmpty();
	}
}
