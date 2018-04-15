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

import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.ArrowSymbolType;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

public abstract class ArrowLayer extends Layer {
	private DoubleProperty vectorScale = new SimpleDoubleProperty(1.0);
	private ObjectProperty<ArrowSymbolType> symbolType = new SimpleObjectProperty<ArrowSymbolType>(ArrowSymbolType.FILLED_TETRAGON_ARROW);

	ArrowLayer(LayerType layerType) {
		super(layerType);
		this.setSymbolType(ArrowSymbolType.FILLED_TETRAGON_ARROW);
		this.setColor(Color.DARKORANGE);
	}

	public DoubleProperty vectorScaleProperty() {
		return this.vectorScale;
	}

	public double getVectorScale() {
		return this.vectorScaleProperty().get();
	}

	public void setVectorScale(final double vectorScale) {
		this.vectorScaleProperty().set(vectorScale);
	}
	
	public ObjectProperty<ArrowSymbolType> symbolTypeProperty() {
		return this.symbolType;
	}

	public ArrowSymbolType getSymbolType() {
		return this.symbolTypeProperty().get();
	}

	public void setSymbolType(final ArrowSymbolType arrowSymbolType) {
		this.symbolTypeProperty().set(arrowSymbolType);
	}
}
