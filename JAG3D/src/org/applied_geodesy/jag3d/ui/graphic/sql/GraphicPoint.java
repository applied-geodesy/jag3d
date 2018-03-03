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

package org.applied_geodesy.jag3d.ui.graphic.sql;

import org.applied_geodesy.jag3d.ui.graphic.coordinate.WorldCoordinate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class GraphicPoint {
	private WorldCoordinate coordinate;
	private double minorAxis = 0.0, majorAxis = 0.0, angle = 0.0, principalComponentX = 0.0, principalComponentY = 0.0, principalComponentZ = 0.0;
	private String name;
	private int dimension;
	private boolean significant;
	private BooleanProperty visible = new SimpleBooleanProperty(Boolean.TRUE);
	
	public GraphicPoint(String name, int dimension, double x, double y) {
		this(name, dimension, x, y, 0, 0, 0, false);
	}
	
	public GraphicPoint(String name, int dimension, double x, double y, double majorAxis, double minorAxis, double angle, boolean significant) {
		this(name, dimension, x, y, 0, 0, 0, 0, 0, 0, false);
	}
	
	public GraphicPoint(String name, int dimension, double x, double y, double majorAxis, double minorAxis, double angle, double principalComponentX, double principalComponentY, double principalComponentZ, boolean significant) {
		this.coordinate = new WorldCoordinate(x, y);
		this.name = name;
		this.dimension = dimension;
		this.majorAxis = Math.max(majorAxis, minorAxis);
		this.minorAxis = Math.min(majorAxis, minorAxis);
		this.principalComponentX = principalComponentX;
		this.principalComponentY = principalComponentY;
		this.principalComponentZ = principalComponentZ;
		this.angle = angle;
		this.significant = significant;
	}
	
	public WorldCoordinate getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(WorldCoordinate coordinate) {
		this.coordinate = coordinate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSignificant() {
		return significant;
	}
	
	public void setSignificant(boolean significant) {
		this.significant = significant;
	}

	public double getMinorAxis() {
		return minorAxis;
	}

	public void setMinorAxis(double minorAxis) {
		this.minorAxis = minorAxis;
	}

	public double getMajorAxis() {
		return majorAxis;
	}

	public void setMajorAxis(double majorAxis) {
		this.majorAxis = majorAxis;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public BooleanProperty visibleProperty() {
		return this.visible;
	}

	public boolean isVisible() {
		return this.visibleProperty().get();
	}

	public void setVisible(final boolean visible) {
		this.visibleProperty().set(visible);
	}

	public int getDimension() {
		return dimension;
	}

	public double getPrincipalComponentX() {
		return principalComponentX;
	}

	public double getPrincipalComponentY() {
		return principalComponentY;
	}
	
	public double getPrincipalComponentZ() {
		return principalComponentZ;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
