package org.applied_geodesy.jag3d.ui.graphic.layer;

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
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class LegendLayer extends Layer implements FontLayer {
	private ObservableList<Layer> layers;
	
	private DoubleProperty fontSize   = new SimpleDoubleProperty(10);
	private StringProperty fontFamily = new SimpleStringProperty(Font.getDefault().getFamily());
	private ObjectProperty<Color> fontColor = new SimpleObjectProperty<Color>(Color.SLATEGREY);

	LegendLayer(LayerType layerType, ObservableList<Layer> layers) {
		super(layerType);
		if (layerType != LayerType.LEGEND)
			throw new IllegalArgumentException("Error, unsupported layer type " + layerType);
		this.layers = layers;
	}
	
	@Override
	public void draw(GraphicsContext graphicsContext, GraphicExtent graphicExtent) {
		Color fontColor   = this.getFontColor();
		double fontSize   = this.getFontSize();
		String fontFamily = this.getFontFamily();
		Font font = Font.font(fontFamily, FontWeight.NORMAL, FontPosture.REGULAR, fontSize);
		
		double sketchWidth  = graphicExtent.getDrawingBoardWidth();
		double sketchHeight = graphicExtent.getDrawingBoardHeight();

		double xMargin = 10, yMargin = 10;
		double xPadding = 5, yPadding = 5;
		
		int cnt = 0;
		double textColumnWidth   = 0;
		double symbolColumnWidth = 3 * 0.7 * fontSize;
		double textHeight = 0;

		for (Layer layer : this.layers) {
			if (!layer.isVisible() || !layer.hasContent() || layer == this)
				continue;

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
		graphicsContext.setLineWidth(this.getLineWidth());
		graphicsContext.setStroke(new Color(1, 1, 1, 0.7));
		graphicsContext.setFill(new Color(1, 1, 1, 0.7));
		// x, y, w, h, arcWidth, arcHeight
		graphicsContext.fillRoundRect(sketchWidth - (legendWidth + xMargin), yMargin, legendWidth, legendHeight, 5, 5);

		// Draw legend box border		
		graphicsContext.setStroke(fontColor);
		graphicsContext.setFill(fontColor);
		graphicsContext.strokeRoundRect(sketchWidth - (legendWidth + xMargin), yMargin, legendWidth, legendHeight, 5, 5);

		double x = sketchWidth - legendWidth;
		double y = yMargin + textHeight; //  + this.yPadding
		// draw items
		int length = this.layers.size();
		for (int i = length - 1; i >= 0; i--) {
			Layer layer = this.layers.get(i); 
			if (!layer.isVisible() || !layer.hasContent() || layer == this)
				continue;

			x = sketchWidth - (legendWidth + xMargin) + xPadding;

			layer.drawLegendSymbol(graphicsContext, graphicExtent, new PixelCoordinate(x, y - 0.3 * textHeight), 0.7 * fontSize, symbolColumnWidth);

			x += symbolColumnWidth + xPadding;

			graphicsContext.setStroke(fontColor);
			graphicsContext.setFill(fontColor);
			graphicsContext.setFont(font);
			graphicsContext.fillText(layer.toString(), x, y);
			
			y += textHeight;
		}
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
