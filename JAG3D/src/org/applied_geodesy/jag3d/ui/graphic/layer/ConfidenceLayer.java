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

import org.applied_geodesy.adjustment.DefaultValue;
import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class ConfidenceLayer<T extends Layer> extends Layer {

	private ObjectProperty<Color> strokeColor = new SimpleObjectProperty<Color>(Color.BLACK);
	private List<T> referenceLayers = FXCollections.observableArrayList();
	private DoubleProperty confidenceLevel = null;

	ConfidenceLayer(LayerType layerType) {
		super(layerType);
		this.setSymbolSize(0);
		this.setLineWidth(0.5);
	}
	
	public void add(T layer) {
		this.referenceLayers.add(layer);
	}

	public void addAll(List<T> layers) {
		for (T layer : layers)
			this.add(layer);
	}
	
	List<T> getReferenceLayers() {
		return this.referenceLayers;
	}

	public ObjectProperty<Color> strokeColorProperty() {
		return this.strokeColor;
	}

	public Color getStrokeColor() {
		return this.strokeColorProperty().get();
	}

	public void setStrokeColor(final Color borderColor) {
		this.strokeColorProperty().set(borderColor);
	}
	
	public DoubleProperty confidenceLevelProperty() {
		if (this.confidenceLevel == null)
			this.confidenceLevel = new SimpleDoubleProperty(DefaultValue.getConfidenceLevel());
		return this.confidenceLevel;
	}

	public double getConfidenceLevel() {
		return this.confidenceLevelProperty().get();
	}

	public void setConfidenceLevel(final double confidenceLevel) {
		this.confidenceLevelProperty().set(confidenceLevel);
	}

	@Override
	public void clearLayer() {} // no clearing --> use data from reference layer

	@Override
	public GraphicExtent getMaximumGraphicExtent() {
		GraphicExtent extent = new GraphicExtent();
		extent.reset();
		return extent;
	}
	
	@Override
	public void drawLegendSymbol(GraphicsContext graphicsContext, GraphicExtent graphicExtent, PixelCoordinate pixelCoordinate, double symbolHeight, double symbolWidth) {
		if (this.contains(graphicExtent, pixelCoordinate) && Math.min(symbolHeight, symbolWidth) > 0) {
			double lineWidth = this.getLineWidth();
			double symbolSize = symbolHeight;
			
			graphicsContext.setLineWidth(lineWidth);
			graphicsContext.setStroke(this.getStrokeColor());
			graphicsContext.setFill(this.getColor());
			graphicsContext.setLineDashes(null);
			
			double majorAxis = 0.75 * symbolSize;
			double minorAxis = 0.7  * majorAxis;
			double angle     = 0;

			SymbolBuilder.drawEllipse(graphicsContext, new PixelCoordinate(pixelCoordinate.getX() + 0.5 * symbolWidth, pixelCoordinate.getY()), majorAxis, minorAxis, angle);
		}
	}
}