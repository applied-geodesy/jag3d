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
