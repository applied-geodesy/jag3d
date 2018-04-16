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

import java.io.BufferedInputStream;
import java.util.Properties;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.SymbolBuilder;
import org.applied_geodesy.util.i18.I18N;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Layer {
	final static Properties PROPERTIES = new Properties();
	static {
		BufferedInputStream bis = null;
		final String path = "/properties/layers.default";
		try {
			if (Layer.class.getResource(path) != null) {
				bis = new BufferedInputStream(Layer.class.getResourceAsStream(path));
				PROPERTIES.load(bis);
			}  
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (bis != null)
					bis.close();  
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private final LayerType layerType;
	private StringProperty name             = new SimpleStringProperty("Layer");
	private ObjectProperty<Color> color     = new SimpleObjectProperty<Color>(Color.DARKBLUE);
	private DoubleProperty lineWidth        = new SimpleDoubleProperty(1.0);
	private DoubleProperty symbolSize       = new SimpleDoubleProperty(SymbolBuilder.DEFAULT_SIZE);
	private BooleanProperty visible         = new SimpleBooleanProperty(Boolean.TRUE);

	I18N i18n = I18N.getInstance();
	
	Layer(LayerType layerType) {
		this.layerType = layerType;
	}
	
	public abstract void draw(GraphicsContext graphicsContext, GraphicExtent graphicExtent);

	public abstract GraphicExtent getMaximumGraphicExtent();

	public abstract void clearLayer();
	
	public LayerType getLayerType() {
		return this.layerType;
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
	
	public final DoubleProperty symbolSizeProperty() {
		return this.symbolSize;
	}

	public final double getSymbolSize() {
		return this.symbolSizeProperty().get();
	}

	public final void setSymbolSize(final double symbolSize) {
		this.symbolSizeProperty().set(symbolSize);
	}
	
	public boolean contains(GraphicExtent graphicExtent, PixelCoordinate pixelCoordinate) {
		return pixelCoordinate.getX() >= 0 && pixelCoordinate.getX() <= graphicExtent.getDrawingBoardWidth() && 
				pixelCoordinate.getY() >= 0 && pixelCoordinate.getY() <= graphicExtent.getDrawingBoardHeight();
	}

	public final BooleanProperty visibleProperty() {
		return this.visible;
	}

	public final boolean isVisible() {
		return this.visibleProperty().get();
	}

	public final void setVisible(final boolean visible) {
		this.visibleProperty().set(visible);
	}
}