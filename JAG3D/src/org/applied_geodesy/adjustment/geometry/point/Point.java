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

package org.applied_geodesy.adjustment.geometry.point;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

public class Point {
	private static int ID_CNT = 0;
	private ReadOnlyObjectProperty<Integer> id;
	private ObjectProperty<String> name = new SimpleObjectProperty<String>(this, "name", "");
	private ObjectProperty<Double> x0 = new SimpleObjectProperty<Double>(this, "x0", 0.0);
	private ObjectProperty<Double> y0 = new SimpleObjectProperty<Double>(this, "y0", 0.0);
	private ObjectProperty<Double> z0 = new SimpleObjectProperty<Double>(this, "z0", 0.0);
	private ReadOnlyObjectProperty<Integer> dimension;
	
	public Point(Point point) {
		this(point.getName(), point.getX0(), point.getY0(), point.getZ0(), point.getDimension());
	}

	public Point(String name, double x0, double y0) {
		this(name, x0, y0, 0.0, 2);
	}
	
	public Point(String name, double x0, double y0, double z0) {
		this(name, x0, y0, z0, 3);
	}
	
	private Point(String name, double x0, double y0, double z0, int dimension) {
		this.id        = new ReadOnlyObjectWrapper<Integer>(this, "id", ID_CNT++);
		this.dimension = new ReadOnlyObjectWrapper<Integer>(this, "dimension", dimension);
		this.setName(name);
		this.setX0(x0);
		this.setY0(y0);
		this.setZ0(z0);
	}
		
	public final int getId() {
		return this.id.get();
	}
	
	public ReadOnlyObjectProperty<Integer> idProperty() {
		return this.id;
	}
	
	public String getName() {
		return this.name.get();
	}
	
	public void setName(String name) {
		this.name.set(name);
	}
	
	public ObjectProperty<String> nameProperty() {
		return this.name;
	}
	
	public int getDimension() {
		return this.dimension.get();
	}
	
	public ReadOnlyObjectProperty<Integer> dimensionProperty() {
		return this.dimension;
	}
	
	public double getX0() {
		return this.x0.get();
	}
	
	public void setX0(double x0) {
		this.x0.set(x0);
	}
	
	public ObjectProperty<Double> x0Property() {
		return this.x0;
	}
	
	public double getY0() {
		return this.y0.get();
	}
	
	public void setY0(double y0) {
		this.y0.set(y0);
	}
	
	public ObjectProperty<Double> y0Property() {
		return this.y0;
	}
	
	public double getZ0() {
		return this.z0.get();
	}
	
	public void setZ0(double z0) {
		this.z0.set(z0);
	}
	
	public ObjectProperty<Double> z0Property() {
		return this.z0;
	}
	
	@Override
	public String toString() {
		return "Point#" + id.get() + " [name=" + name.get() + ", x0=" + x0.get() + ", y0=" + y0.get() + ", z0=" + z0.get() + ", dimension=" + dimension.get() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dimension == null) ? 0 : dimension.get().hashCode());
		result = prime * result + ((id == null) ? 0 : id.get().hashCode());
		result = prime * result + ((x0 == null) ? 0 : x0.get().hashCode());
		result = prime * result + ((y0 == null) ? 0 : y0.get().hashCode());
		result = prime * result + ((z0 == null) ? 0 : z0.get().hashCode());
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
		Point other = (Point) obj;
		if (this.getId() != other.getId())
			return false;
		if (!this.getName().equals(other.getName()))
			return false;
		return this.equalsCoordinateComponents(other);
	}
	
	public boolean equalsCoordinateComponents(Point point) {
		if (this == point)
			return true;
		if (point == null)
			return false;
		if (this.getDimension() != point.getDimension())
			return false;
		if (this.getX0() != point.getX0())
			return false;
		if (this.getY0() != point.getY0())
			return false;
		if (this.getZ0() != point.getZ0())
			return false;
		return true;
	}
}
