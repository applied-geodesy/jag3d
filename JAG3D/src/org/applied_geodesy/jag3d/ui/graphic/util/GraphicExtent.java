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

package org.applied_geodesy.jag3d.ui.graphic.util;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.PixelCoordinate;
import org.applied_geodesy.jag3d.ui.graphic.coordinate.WorldCoordinate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class GraphicExtent {
	private DoubleProperty minX = new SimpleDoubleProperty(0.0);
	private DoubleProperty minY = new SimpleDoubleProperty(0.0);
	private DoubleProperty maxX = new SimpleDoubleProperty(0.0);
	private DoubleProperty maxY = new SimpleDoubleProperty(0.0);
	
	private BooleanProperty extended = new SimpleBooleanProperty(Boolean.FALSE);

	private DoubleProperty drawingBoardWidth  = new SimpleDoubleProperty(0.0);
	private DoubleProperty drawingBoardHeight = new SimpleDoubleProperty(0.0);

	public GraphicExtent() {
		this.reset();
	}

	public GraphicExtent merge(GraphicExtent graphicExtent) {
		boolean isExtended = false;
		
		if (this.getMinX() > graphicExtent.getMinX()) {
			this.setMinX(graphicExtent.getMinX());
			isExtended = true;	
		}
		
		if (this.getMinY() > graphicExtent.getMinY()) {
			this.setMinY(graphicExtent.getMinY());
			isExtended = true;
		}
		
		if (this.getMaxX() < graphicExtent.getMaxX()) {
			this.setMaxX(graphicExtent.getMaxX());
			isExtended = true;
		}
		
		if (this.getMaxY() < graphicExtent.getMaxY()) {
			this.setMaxY(graphicExtent.getMaxY());
			isExtended = true;
		}

		if (isExtended)
			this.extended();
		
		return this;
	}
	
	public void reset() {
		boolean isExtended = false;

		if (this.getMinX() != Double.MAX_VALUE) {
			this.setMinX(Double.MAX_VALUE);
			isExtended = true;
		}

		if (this.getMinY() != Double.MAX_VALUE) {
			this.setMinY(Double.MAX_VALUE);
			isExtended = true;
		}

		if (this.getMaxX() != Double.MIN_VALUE) {
			this.setMaxX(Double.MIN_VALUE);
			isExtended = true;
		}

		if (this.getMaxY() != Double.MIN_VALUE) {
			this.setMaxY(Double.MIN_VALUE);
			isExtended = true;
		}

		if (isExtended)
			this.extended();
	}
	
	public double getCentreX() {
		return this.getMinX() + 0.5 * this.getExtentWidth();
	}
	
	public double getCentreY() {
		return this.getMinY() + 0.5 * this.getExtentHeight();
	}
	
	public GraphicExtent merge(WorldCoordinate worldCoordinate) {
		return this.merge(worldCoordinate.getX(), worldCoordinate.getY());
	}

	public GraphicExtent merge(double x, double y) {
		boolean isExtended = false;
		
		if (this.getMinX() > x) {
			this.setMinX(x);
			isExtended = true;
		}

		if (this.getMaxX() < x) {
			this.setMaxX(x);
			isExtended = true;
		}

		if (this.getMinY() > y) {
			this.setMinY(y);
			isExtended = true;
		}

		if (this.getMaxY() < y) {
			this.setMaxY(y);
			isExtended = true;
		}
		
		if (isExtended)
			this.extended();

		return this;
	}
	
	public void set(GraphicExtent graphicExtent) {
		double xmin = graphicExtent.getMinX(); 
		double ymin = graphicExtent.getMinY(); 
		double xmax = graphicExtent.getMaxX(); 
		double ymax = graphicExtent.getMaxY(); 
		this.set(xmin, ymin, xmax, ymax);
	}
	
	public void set(WorldCoordinate minCoordinate, WorldCoordinate maxCoordinate) {
		double xmin = minCoordinate.getX(); 
		double ymin = minCoordinate.getY();  
		double xmax = maxCoordinate.getX(); 
		double ymax = maxCoordinate.getY(); 
		this.set(xmin, ymin, xmax, ymax);
	}

	public void set(double xmin, double ymin, double xmax, double ymax) {
		boolean isExtended = false;
		
		if (xmin > xmax) {
			isExtended = this.getMinX() != xmax || this.getMaxX() != xmin;
			this.setMinX(xmax);
			this.setMaxX(xmin);
			
		} 
		else {
			isExtended = this.getMinX() != xmin || this.getMaxX() != xmax;
			this.setMinX(xmin);
			this.setMaxX(xmax);
		}

		if (ymin > ymax) {
			isExtended = this.getMinY() != ymax || this.getMaxY() != ymin;
			this.setMinY(ymax);
			this.setMaxY(ymin);
		} 
		else {
			isExtended = this.getMinY() != ymin || this.getMaxY() != ymax;
			this.setMinY(ymin);
			this.setMaxY(ymax);
		}
		
		if (isExtended)
			this.extended();
	}
	
	public double getScale() {
		return getScale(this.getDrawingBoardHeight(), this.getDrawingBoardWidth(), this.getExtentHeight(), this.getExtentWidth());
	}
	
	public void setScale(double scale) {
		double pixelCentreX = 0.5 * this.getDrawingBoardWidth();
		double pixelCentreY = 0.5 * this.getDrawingBoardHeight();
		
		double newMinX = pixelCentreX - 0.5 * this.getDrawingBoardWidth();
		double newMaxX = pixelCentreX + 0.5 * this.getDrawingBoardWidth();
		
		double newMinY = pixelCentreY + 0.5 * this.getDrawingBoardHeight();
		double newMaxY = pixelCentreY - 0.5 * this.getDrawingBoardHeight();
		
		WorldCoordinate minWorldCoord = toWorldCoordinate(new PixelCoordinate(newMinX, newMinY), this, scale);
		WorldCoordinate maxWorldCoord = toWorldCoordinate(new PixelCoordinate(newMaxX, newMaxY), this, scale);

		this.set(minWorldCoord, maxWorldCoord);
	}
	
	public boolean contains(double x, double y) {
		return x >= this.getMinX() && x <= this.getMaxX() && y >= this.getMinY() && y <= this.getMaxY();
	}

	public double getExtentWidth() {
		return Math.abs(this.getMaxX() - this.getMinX());
	}

	public double getExtentHeight() {
		return Math.abs(this.getMaxY() - this.getMinY());
	}

	@Override
	public String toString() {
		return "GraphicExtent [Min=(" + this.getMinX() + ", " + this.getMinY() + ") Max=(" + this.getMaxX() + ", " + this.getMaxY() + ")]";
	}

	public DoubleProperty minXProperty() {
		return this.minX;
	}

	public double getMinX() {
		return this.minXProperty().get();
	}

	public void setMinX(final double minX) {
		this.minXProperty().set(minX);
	}

	public DoubleProperty minYProperty() {
		return this.minY;
	}

	public double getMinY() {
		return this.minYProperty().get();
	}

	public void setMinY(final double minY) {
		this.minYProperty().set(minY);
	}

	public DoubleProperty maxXProperty() {
		return this.maxX;
	}

	public double getMaxX() {
		return this.maxXProperty().get();
	}

	public void setMaxX(final double maxX) {
		this.maxXProperty().set(maxX);
	}

	public DoubleProperty maxYProperty() {
		return this.maxY;
	}

	public double getMaxY() {
		return this.maxYProperty().get();
	}

	public void setMaxY(final double maxY) {
		this.maxYProperty().set(maxY);
	}
	
	public DoubleProperty drawingBoardHeightProperty() {
		return this.drawingBoardHeight;
	}

	public double getDrawingBoardHeight() {
		return this.drawingBoardHeightProperty().get();
	}

	public void setDrawingBoardHeight(final double height) {
		this.drawingBoardHeightProperty().set(height);
	}

	public DoubleProperty drawingBoardWidthProperty() {
		return this.drawingBoardWidth;
	}

	public double getDrawingBoardWidth() {
		return this.drawingBoardWidthProperty().get();
	}

	public void setDrawingBoardWidth(final double width) {
		this.drawingBoardWidthProperty().set(width);
	}

	public static PixelCoordinate toPixelCoordinate(WorldCoordinate coordinate, GraphicExtent graphicExtent) {
		return toPixelCoordinate(coordinate, graphicExtent, graphicExtent.getScale());
	}
	
	public static WorldCoordinate toWorldCoordinate(PixelCoordinate coordinate, GraphicExtent graphicExtent) {
		return toWorldCoordinate(coordinate, graphicExtent, graphicExtent.getScale());
	}
	
	public static WorldCoordinate toWorldCoordinate(PixelCoordinate coordinate, GraphicExtent graphicExtent, double scale) {
		double x = coordinate.getX();
		double y = coordinate.getY();
		
		return new WorldCoordinate(
				GraphicUtils.toWorldCoordinate(x - 0.5*graphicExtent.getDrawingBoardWidth(),  graphicExtent.getCentreX(), scale),
				GraphicUtils.toWorldCoordinate(0.5*graphicExtent.getDrawingBoardHeight() - y, graphicExtent.getCentreY(), scale)
		);
	}
	
	public static PixelCoordinate toPixelCoordinate(WorldCoordinate coordinate, GraphicExtent graphicExtent, double scale) {
		double x = coordinate.getX();
		double y = coordinate.getY();

		return new PixelCoordinate(
				0.5*graphicExtent.getDrawingBoardWidth()  + GraphicUtils.toPixelCoordinate(x, graphicExtent.getCentreX(), scale),
				0.5*graphicExtent.getDrawingBoardHeight() - GraphicUtils.toPixelCoordinate(y, graphicExtent.getCentreY(), scale)
		);
	}
	
	public static double getScale(double drawingBoardHeight, double drawingBoardWidth, double extentHeight, double extentWidth) {
		double scaleHeight = drawingBoardHeight > 0 ? extentHeight / drawingBoardHeight : -1;
	    double scaleWidth  = drawingBoardWidth  > 0 ? extentWidth  / drawingBoardWidth  : -1;
	    return Math.max(scaleHeight, scaleWidth);
	}

	public final BooleanProperty extendedProperty() {
		return this.extended;
	}

	private final boolean isExtended() {
		return this.extendedProperty().get();
	}

	private final void setExtended(final boolean extended) {
		this.extendedProperty().set(extended);
	}
	
	private void extended() {
		this.setExtended(!this.isExtended());
	}
}
