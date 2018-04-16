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

import java.util.Locale;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.coordinate.WorldCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;
import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.util.FormatterOptions;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

class MouseNavigationCanvas extends ResizableCanvas {

	private class ScrollEventHandler implements EventHandler<ScrollEvent> {
		@Override
		public void handle(ScrollEvent event) {
			double x = event.getX();
			double y = event.getY();

			GraphicExtent graphicExtent = getCurrentGraphicExtent();
			WorldCoordinate mouseWorldCoord = GraphicExtent.toWorldCoordinate(new PixelCoordinate(x, y), getCurrentGraphicExtent());

			double mouseX = mouseWorldCoord.getX();
			double mouseY = mouseWorldCoord.getY();

			double minX = graphicExtent.getMinX();
			double maxX = graphicExtent.getMaxX();
			double minY = graphicExtent.getMinY();
			double maxY = graphicExtent.getMaxY();

			double extentWidth  = graphicExtent.getExtentWidth();
			double extentHeight = graphicExtent.getExtentHeight();


			// Verteilung der Strecken
			double fxMin = Math.abs(minX - mouseX) / extentWidth;
			double fxMax = Math.abs(maxX - mouseX) / extentWidth;

			double fyMin = Math.abs(minY - mouseY) / extentHeight;
			double fyMax = Math.abs(maxY - mouseY) / extentHeight;

			if (event.getDeltaY() < 0) {
				extentWidth  *= 1.15;
				extentHeight *= 1.15;
			}
			else if (event.getDeltaY() > 0) {
				extentWidth  /= 1.15;
				extentHeight /= 1.15;
			}
			else 
				return;

			double newMinX = mouseX - fxMin * extentWidth;
			double newMaxX = mouseX + fxMax * extentWidth;

			double newMinY = mouseY - fyMin * extentHeight;
			double newMaxY = mouseY + fyMax * extentHeight;

			getCurrentGraphicExtent().set(newMinX, newMinY, newMaxX, newMaxY);
			
			layerManager.draw();
		}
	}

	private class RubberbandingAndPanEventHandler implements EventHandler<MouseEvent> {
		private double xStart = -1, yStart = -1, xEnd = 0, yEnd = 0;
		@Override
		public void handle(MouseEvent event) {
			MouseButton mouseButton = event.getButton();
			this.xEnd = event.getX();
			this.yEnd = event.getY();
			setCurrentCoordinate(new PixelCoordinate(this.xEnd, this.yEnd));

			if (toolbarType == null || toolbarType == ToolbarType.NONE)
				return;
			
			// expand plot
			if(mouseButton == MouseButton.PRIMARY && event.getClickCount() >= 2) {
				layerManager.expand();
	        }

			// zoom by window
			else if (toolbarType == ToolbarType.WINDOW_ZOOM && event.getEventType() == MouseEvent.MOUSE_DRAGGED && mouseButton == MouseButton.PRIMARY && event.isPrimaryButtonDown() && this.xStart >= 0 && this.yStart >= 0) {
				drawRect(
						Math.min(this.xStart,this.xEnd), 
						Math.min(this.yStart,this.yEnd), 
						Math.abs(this.xStart-this.xEnd), 
						Math.abs(this.yStart-this.yEnd)
						);
			}

			// move map
			else if ((mouseButton == MouseButton.SECONDARY || toolbarType == ToolbarType.MOVE) && event.getEventType() == MouseEvent.MOUSE_DRAGGED && this.xStart >= 0 && this.yStart >= 0) {

				GraphicExtent currentExtent = getCurrentGraphicExtent();

				WorldCoordinate startWorldCoordinate = GraphicExtent.toWorldCoordinate(new PixelCoordinate(this.xStart, this.yStart), getCurrentGraphicExtent());
				WorldCoordinate endWorldCoordinate   = GraphicExtent.toWorldCoordinate(new PixelCoordinate(this.xEnd, this.yEnd), getCurrentGraphicExtent());


				double startX = startWorldCoordinate.getX();
				double startY = startWorldCoordinate.getY();

				double endX = endWorldCoordinate.getX();
				double endY = endWorldCoordinate.getY();


				double minX = currentExtent.getMinX();
				double minY = currentExtent.getMinY();
				double maxX = currentExtent.getMaxX();
				double maxY = currentExtent.getMaxY();

				currentExtent.set(
						minX - (endX-startX), 
						minY - (endY-startY), 
						maxX - (endX-startX), 
						maxY - (endY-startY)
						);

				layerManager.draw();
				
				this.xStart = event.getX();
				this.yStart = event.getY();	
			}

			else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				this.xStart = event.getX();
				this.yStart = event.getY();
			}

			else if (mouseButton == MouseButton.PRIMARY && event.getEventType() == MouseEvent.MOUSE_RELEASED && this.xStart >= 0 && this.yStart >= 0) {
				if (toolbarType == ToolbarType.WINDOW_ZOOM && this.xStart != this.xEnd && this.yStart != this.yEnd) {
					WorldCoordinate startWorldCoord = GraphicExtent.toWorldCoordinate(new PixelCoordinate(this.xStart, this.yStart), getCurrentGraphicExtent());
					WorldCoordinate endWorldCoord   = GraphicExtent.toWorldCoordinate(new PixelCoordinate(this.xEnd, this.yEnd), getCurrentGraphicExtent());
					getCurrentGraphicExtent().set(startWorldCoord, endWorldCoord);
				}
				
				layerManager.draw();
				
				this.xStart = -1;
				this.yStart = -1;
			}
		}
	}

	private FormatterOptions options = FormatterOptions.getInstance();
	private ToolbarType toolbarType = null;
	private Label coordinatePanel;
	private RubberbandingAndPanEventHandler rubberbandingAndPanEventHandler = new RubberbandingAndPanEventHandler();
	private LayerManager layerManager;
	private ObjectProperty<Color> color     = new SimpleObjectProperty<Color>(Color.DARKBLUE);
	private DoubleProperty lineWidth        = new SimpleDoubleProperty(1.0);
	private ScrollEventHandler scrollEventHandler = new ScrollEventHandler();

	MouseNavigationCanvas(LayerManager layerManager) {
		super(layerManager.getCurrentGraphicExtent());
		
		this.layerManager = layerManager;
		this.coordinatePanel = layerManager.getCoordinateLabel();

		this.addEventHandler(MouseEvent.MOUSE_MOVED,    this.rubberbandingAndPanEventHandler);
		this.addEventHandler(MouseEvent.MOUSE_PRESSED,  this.rubberbandingAndPanEventHandler);
		this.addEventHandler(MouseEvent.MOUSE_DRAGGED,  this.rubberbandingAndPanEventHandler);
		this.addEventHandler(MouseEvent.MOUSE_RELEASED, this.rubberbandingAndPanEventHandler);

		this.addEventHandler(ScrollEvent.SCROLL_STARTED, this.scrollEventHandler);
		this.addEventHandler(ScrollEvent.SCROLL, this.scrollEventHandler);
		this.addEventHandler(ScrollEvent.SCROLL_FINISHED, this.scrollEventHandler);

		this.setColor(Color.BLACK);
		this.setLineWidth(1.25);
	}

	@Override
	public void draw() {
		super.draw();
		this.drawScaleBar();		
	}

	private void drawRect(double x, double y, double w, double h) {
		this.clear();

		GraphicsContext gc = this.getGraphicsContext2D();

		// set layer color
		gc.setStroke(this.getColor());

		// line properties
		gc.setLineWidth(this.getLineWidth());
		gc.setLineDashes(3,5,3,5);
		gc.strokeRect(x, y, w, h);
	}
	
	private void drawScaleBar() {
		GraphicsContext gc = this.getGraphicsContext2D();
		
		// set layer color
		gc.setStroke(Color.BLACK);
		
		// set font 
		gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, FontPosture.REGULAR, 10));

		// line properties
		gc.setLineWidth(0.5);
		gc.setLineDashes(null);

		double extentScale = this.getCurrentGraphicExtent().getScale();
		
		if (Double.isInfinite(extentScale) || Double.isNaN(extentScale) || extentScale <= 0)
			return;

		int scaleSegments     =  3;
		double scaleBarWidth  = 50;
		double scaleBarHeight =  5;
		double padding        = 25;
		
		double x = this.getWidth() - padding;
		double y = this.getHeight() - padding;

		double worldScale = options.convertLengthToView(extentScale * scaleBarWidth);
		int exponent = (int)Math.log10(worldScale);
		double magnitude = Math.pow(10, exponent);
		double ratio = Math.ceil(worldScale/magnitude);
		
		if ((worldScale/magnitude) < 0.5) { // exponent <= 0 && 
			exponent--;
			magnitude = Math.pow(10, exponent);
			ratio = Math.ceil(worldScale/magnitude);
		}
		
		scaleBarWidth = ratio*magnitude/extentScale;
		for (int i = 1; i <= scaleSegments; i++) {
			double xi = x - scaleBarWidth * i;
			gc.setFill(i % 2 == 0 ? Color.WHITE : Color.DARKGRAY);
			gc.fillRect(xi, y, scaleBarWidth, scaleBarHeight);
			gc.strokeRect(xi, y, scaleBarWidth, scaleBarHeight);
		}
		
		String format = "%f %s";
		if (exponent <= -6 || exponent >= 6)
			format = "%.3e %s";
		else if (exponent >= 0)
			format = "%.0f %s";
		else if (exponent < 0)
			format = "%." + Math.abs(exponent) + "f %s";
		
		// estimate text size
		Text scaleLabel = new Text(String.format(Locale.ENGLISH, format, scaleSegments*ratio*magnitude, options.getFormatterOptions().get(CellValueType.LENGTH).getUnit().getAbbreviation()));
		scaleLabel.setFont(gc.getFont());
		double scaleLabelWidth = scaleLabel.getBoundsInLocal().getWidth();

		gc.setFill(Color.BLACK);
		gc.fillText("0", x - scaleBarWidth * scaleSegments, y - scaleBarHeight);
		gc.fillText(scaleLabel.getText(), x - scaleLabelWidth, y - scaleBarHeight);
	}

	public void setToolbarType(ToolbarType toolbarType) {
		this.toolbarType = toolbarType;
	}

	public ToolbarType getToolbarType() {
		return this.toolbarType;
	}

	private void setCurrentCoordinate(PixelCoordinate coordinate) {
		WorldCoordinate worldCoordinate = GraphicExtent.toWorldCoordinate(coordinate, this.getCurrentGraphicExtent()); 
		if (Double.isNaN(worldCoordinate.getX()) || Double.isNaN(worldCoordinate.getY()))
			this.coordinatePanel.setText(null);
		else {
			String x = options.toLengthFormat(worldCoordinate.getX(), true);
			String y = options.toLengthFormat(worldCoordinate.getY(), true);

			this.coordinatePanel.setText(String.format("[x = %s / y = %s]", x, y));
		}
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
	
	public DoubleProperty lineWidthProperty() {
		return this.lineWidth;
	}
	
	public double getLineWidth() {
		return this.lineWidthProperty().get();
	}
	
	public void setLineWidth(final double lineWidth) {
		this.lineWidthProperty().set(lineWidth);
	}
}
