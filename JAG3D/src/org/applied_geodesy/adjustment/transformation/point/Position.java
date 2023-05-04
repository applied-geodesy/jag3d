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

package org.applied_geodesy.adjustment.transformation.point;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;

public class Position implements Positionable {
	private static int ID_CNT = 0;
	private ReadOnlyObjectProperty<Integer> id;
	private ObjectProperty<Double> x = new SimpleObjectProperty<Double>(this, "x", 0.0);
	private ObjectProperty<Double> y = new SimpleObjectProperty<Double>(this, "y", 0.0);
	private ObjectProperty<Double> z = new SimpleObjectProperty<Double>(this, "z", 0.0);
	private ReadOnlyObjectProperty<Integer> dimension;
	
	Position(Position position) {
		this(position.getX(), position.getY(), position.getZ(), position.getDimension());
	}
	
	Position(double z) {
		this(0.0, 0.0, z, 1);
	}

	Position(double x, double y) {
		this(x, y, 0.0, 2);
	}
	
	Position(double x, double y, double z) {
		this(x, y, z, 3);
	}
	
	private Position(double x, double y, double z, int dimension) {
		this.id        = new ReadOnlyObjectWrapper<Integer>(this, "id", ID_CNT++);
		this.dimension = new ReadOnlyObjectWrapper<Integer>(this, "dimension", dimension);
		this.setX(x);
		this.setY(y);
		this.setZ(z);
	}
		
	public final int getId() {
		return this.id.get();
	}
	
	public ReadOnlyObjectProperty<Integer> idProperty() {
		return this.id;
	}
	

	public int getDimension() {
		return this.dimension.get();
	}
	
	public ReadOnlyObjectProperty<Integer> dimensionProperty() {
		return this.dimension;
	}
	
	public double getX() {
		return this.x.get();
	}
	
	public void setX(double x) {
		this.x.set(x);
	}
	
	public ObservableObjectValue<Double> xProperty() {
		return this.x;
	}
	
	public double getY() {
		return this.y.get();
	}
	
	public void setY(double y) {
		this.y.set(y);
	}
	
	public ObservableObjectValue<Double> yProperty() {
		return this.y;
	}
	
	public double getZ() {
		return this.z.get();
	}
	
	public void setZ(double z) {
		this.z.set(z);
	}
	
	public ObservableObjectValue<Double> zProperty() {
		return this.z;
	}
	
	public void reset() {}
	
	@Override
	public String toString() {
		return "Point#" + id.get() + " [x=" + x.get() + ", y=" + y.get() + ", z=" + z.get() + ", dimension=" + dimension.get() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dimension == null) ? 0 : dimension.get().hashCode());
		result = prime * result + ((id == null) ? 0 : id.get().hashCode());
		result = prime * result + ((x == null) ? 0 : x.get().hashCode());
		result = prime * result + ((y == null) ? 0 : y.get().hashCode());
		result = prime * result + ((z == null) ? 0 : z.get().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (this.getId() != other.getId())
			return false;
		return this.equalsPosition(other);
	}
}
