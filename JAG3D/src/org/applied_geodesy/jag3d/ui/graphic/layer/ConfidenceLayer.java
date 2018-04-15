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

import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

public abstract class ConfidenceLayer<T extends Layer> extends Layer {

	private ObjectProperty<Color> strokeColor = new SimpleObjectProperty<Color>(Color.BLACK);
	private List<T> referenceLayers = FXCollections.observableArrayList();

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

	@Override
	public void clearLayer() {} // no clearing --> use data from reference layer

	@Override
	public GraphicExtent getMaximumGraphicExtent() {
		GraphicExtent extent = new GraphicExtent();
		extent.reset();
		return extent;
	}
}