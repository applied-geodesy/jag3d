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
	private double minorAxis = 0.0, majorAxis = 0.0, angle = 0.0, pPrio = 0;
	private double principalComponentX = 0.0, principalComponentY = 0.0, principalComponentZ = 0.0;
	private double residualX = 0.0, residualY = 0.0, residualZ = 0.0;
	private double minRedundancy = 0.0;
	private double maxInfluenceOnPosition = 0.0;
	private String name;
	private int dimension;
	private boolean significant = false, grossErrorExceeded = false;
	private BooleanProperty visible = new SimpleBooleanProperty(Boolean.TRUE);
	
	public GraphicPoint(String name, int dimension, double x, double y) {
		this(name, dimension, x, y, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, false);
	}

	public GraphicPoint(String name, int dimension, 
			double x, double y, 
			double majorAxis, double minorAxis, double angle, 
			double residualX, double residualY, double residualZ,
			double principalComponentX, double principalComponentY, double principalComponentZ, 
			double minRedundancy, double maxInfluenceOnPosition,
			double pPrio, boolean grossErrorExceeded, boolean significant) {

		this.coordinate = new WorldCoordinate(x, y);
		this.name = name;
		this.dimension = dimension;
		this.majorAxis = Math.max(majorAxis, minorAxis);
		this.minorAxis = Math.min(majorAxis, minorAxis);
		this.principalComponentX = principalComponentX;
		this.principalComponentY = principalComponentY;
		this.principalComponentZ = principalComponentZ;
		this.residualX = residualX;
		this.residualY = residualY;
		this.residualZ = residualZ;
		this.minRedundancy = minRedundancy;
		this.maxInfluenceOnPosition = maxInfluenceOnPosition;
		this.angle = angle;
		this.pPrio = pPrio;
		this.grossErrorExceeded = grossErrorExceeded;
		this.significant = significant;
	}
	
	public WorldCoordinate getCoordinate() {
		return this.coordinate;
	}

	public void setCoordinate(WorldCoordinate coordinate) {
		this.coordinate = coordinate;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSignificant() {
		return this.significant;
	}
	
	public void setSignificant(boolean significant) {
		this.significant = significant;
	}
	
	public boolean isGrossErrorExceeded() {
		return this.grossErrorExceeded;
	}
	
	public void setGrossErrorExceeded(boolean grossErrorExceeded) {
		this.grossErrorExceeded = grossErrorExceeded;
	}

	public double getMinorAxis() {
		return this.minorAxis;
	}

	public void setMinorAxis(double minorAxis) {
		this.minorAxis = minorAxis;
	}

	public double getMajorAxis() {
		return this.majorAxis;
	}

	public void setMajorAxis(double majorAxis) {
		this.majorAxis = majorAxis;
	}

	public double getAngle() {
		return this.angle;
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
		return this.dimension;
	}

	public double getPrincipalComponentX() {
		return this.principalComponentX;
	}

	public double getPrincipalComponentY() {
		return this.principalComponentY;
	}
	
	public double getPrincipalComponentZ() {
		return this.principalComponentZ;
	}
	
	public double getResidualX() {
		return this.residualX;
	}

	public double getResidualY() {
		return this.residualY;
	}
	
	public double getResidualZ() {
		return this.residualZ;
	}
	
	public double getMinRedundancy() {
		return this.minRedundancy;
	}

	public double getMaxInfluenceOnPosition() {
		return this.maxInfluenceOnPosition;
	}
	
	public double getPprio() {
		return this.pPrio;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
