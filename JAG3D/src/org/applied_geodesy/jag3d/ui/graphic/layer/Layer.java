package org.applied_geodesy.jag3d.ui.graphic.layer;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;
import org.applied_geodesy.util.i18.I18N;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Layer extends Canvas {
	private class ResizingListener implements ChangeListener<Number> {
		private Orientation orientation;
		
		private ResizingListener(Orientation orientation) {
			this.orientation = orientation;
		}
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			double extentWidth  = currentGraphicExtent.getExtentWidth();
			double extentHeight = currentGraphicExtent.getExtentHeight();
			
			double drawingBoardHeight = currentGraphicExtent.getDrawingBoardHeight();
			double drawingBoardWidth  = currentGraphicExtent.getDrawingBoardWidth();

			switch(this.orientation) {
			case HORIZONTAL:
				drawingBoardWidth = oldValue.doubleValue() > newValue.doubleValue() ? oldValue.doubleValue() : newValue.doubleValue();
				break;
			case VERTICAL:
				drawingBoardHeight = oldValue.doubleValue() > newValue.doubleValue() ? oldValue.doubleValue() : newValue.doubleValue();
				break;			
			}
			currentGraphicExtent.setScale(GraphicExtent.getScale(drawingBoardHeight, drawingBoardWidth, extentHeight, extentWidth));
			draw(currentGraphicExtent);
		}
	}
	
	private class LayerPropertyChangeListener implements ChangeListener<Object> {
		@Override
		public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
			draw(getCurrentGraphicExtent());
		}
	}

	private final LayerType layerType;
	private StringProperty name             = new SimpleStringProperty();
	private ObjectProperty<Color> color     = new SimpleObjectProperty<Color>(Color.DARKBLUE);
	private DoubleProperty lineWidth        = new SimpleDoubleProperty(1.0);
	private DoubleProperty symbolSize       = new SimpleDoubleProperty(SymbolBuilder.DEFAULT_SIZE);
	private final GraphicExtent currentGraphicExtent;
	private LayerPropertyChangeListener layerPropertyChangeListener = new LayerPropertyChangeListener();
	static I18N i18n = I18N.getInstance();
	
	Layer(LayerType layerType, GraphicExtent currentGraphicExtent) {
		this.layerType = layerType;
		this.widthProperty().addListener(new ResizingListener(Orientation.HORIZONTAL));
		this.heightProperty().addListener(new ResizingListener(Orientation.VERTICAL));
		
		this.addLayerPropertyChangeListener(this.visibleProperty());
		this.addLayerPropertyChangeListener(this.colorProperty());
		this.addLayerPropertyChangeListener(this.lineWidthProperty());
		this.addLayerPropertyChangeListener(this.symbolSizeProperty());

		this.currentGraphicExtent = currentGraphicExtent;
	}
	
	public abstract void clearLayer();
	
	public LayerType getLayerType() {
		return this.layerType;
	}
	
	public abstract void draw(GraphicExtent graphicExtent);

	public abstract GraphicExtent getMaximumGraphicExtent();
	
	public GraphicExtent getCurrentGraphicExtent() {
		return this.currentGraphicExtent;
	}

	public void clearDrawingBoard() {
		double width  = this.getWidth();
		double height = this.getHeight();

		GraphicsContext gc = this.getGraphicsContext2D();
		// remove old Graphic
		gc.clearRect(0, 0, width, height);
	}
	
	void addLayerPropertyChangeListener(Property<?> property) {
		property.addListener(this.layerPropertyChangeListener);
	}

	public ObjectProperty<Color> colorProperty() {
		return this.color;
	}
	
	public Color getColor() {
		return this.colorProperty().get();
	}
	
	public void setColor(final Color color) {
		this.colorProperty().set(color);
	}
		
	public StringProperty nameProperty() {
		return this.name;
	}
	
	public String getName() {
		return this.nameProperty().get();
	}
	
	public void setName(final String name) {
		this.nameProperty().set(name);
	}

	public DoubleProperty lineWidthProperty() {
		return this.lineWidth;
	}
	
	public double getLineWidth() {
		return this.lineWidthProperty().get();
	}
	
	public void setLineWidth(final double lineWidth) {
		this.lineWidthProperty().set(lineWidth);
	}
	
	public boolean contains(PixelCoordinate pixelCoordinate) {
		return pixelCoordinate.getX() >= 0 && pixelCoordinate.getX() <= this.getWidth() && 
				pixelCoordinate.getY() >= 0 && pixelCoordinate.getY() <= this.getHeight();
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public double prefWidth(double height) {
		return getWidth();
	}

	@Override
	public double prefHeight(double width) {
		return getHeight();
	}

	public final DoubleProperty symbolSizeProperty() {
		return this.symbolSize;
	}

	public final double getSymbolSize() {
		return this.symbolSizeProperty().get();
	}

	public final void setSymbolSize(final double symbolSize) {
		this.symbolSizeProperty().set(symbolSize);
	}
}
