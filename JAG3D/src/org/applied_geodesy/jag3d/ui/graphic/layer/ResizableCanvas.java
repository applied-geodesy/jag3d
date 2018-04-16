package org.applied_geodesy.jag3d.ui.graphic.layer;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

class ResizableCanvas extends Canvas {
	
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
			draw();
		}
	}

	private final GraphicExtent currentGraphicExtent;
	private ObservableList<Layer> layers = FXCollections.observableArrayList();

	ResizableCanvas(GraphicExtent currentGraphicExtent) {
		this.currentGraphicExtent = currentGraphicExtent;
		
		this.widthProperty().bind(this.currentGraphicExtent.drawingBoardWidthProperty());
		this.heightProperty().bind(this.currentGraphicExtent.drawingBoardHeightProperty());
		
		this.widthProperty().addListener(new ResizingListener(Orientation.HORIZONTAL));
		this.heightProperty().addListener(new ResizingListener(Orientation.VERTICAL));
	}
	
	public ObservableList<Layer> getLayers() {
		return this.layers;
	}

	void draw() {
		this.clear();

		GraphicsContext graphicsContext = getGraphicsContext2D();
		
		for (Layer layer : this.layers) {
			if (!layer.isVisible())
				continue;
			layer.draw(graphicsContext, this.getCurrentGraphicExtent());
		}
	}
	
	void clear() {
		double width  = this.getWidth();
		double height = this.getHeight();

		GraphicsContext gc = this.getGraphicsContext2D();
		// remove old Graphic
		gc.clearRect(0, 0, width, height);
	}
	
	public GraphicExtent getCurrentGraphicExtent() {
		return this.currentGraphicExtent;
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
	
	public boolean contains(PixelCoordinate pixelCoordinate) {
		return pixelCoordinate.getX() >= 0 && pixelCoordinate.getX() <= this.getWidth() && 
				pixelCoordinate.getY() >= 0 && pixelCoordinate.getY() <= this.getHeight();
	}
}
