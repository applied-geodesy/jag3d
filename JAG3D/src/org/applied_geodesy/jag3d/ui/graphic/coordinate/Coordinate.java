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

package org.applied_geodesy.jag3d.ui.graphic.coordinate;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public abstract class Coordinate {
	private DoubleProperty x = new SimpleDoubleProperty(0.0);
	private DoubleProperty y = new SimpleDoubleProperty(0.0);
	
	Coordinate(double x, double y) {
		this.setX(x);
		this.setY(y);
	}

	public DoubleProperty xProperty() {
		return this.x;
	}

	public double getX() {
		return this.xProperty().get();
	}

	public void setX(final double x) {
		this.xProperty().set(x);
	}
	

	public DoubleProperty yProperty() {
		return this.y;
	}

	public double getY() {
		return this.yProperty().get();
	}

	public void setY(final double y) {
		this.yProperty().set(y);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": [ " + this.getX() + " / " + this.getY() + " ]";
	}
}
