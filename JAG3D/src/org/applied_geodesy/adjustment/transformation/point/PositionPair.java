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

public abstract class PositionPair<T1 extends Positionable, T2 extends Positionable> implements Pairable<T1,T2> {
	private ObjectProperty<String> name    = new SimpleObjectProperty<String>(this, "name", "");
	private ObjectProperty<Boolean> enable = new SimpleObjectProperty<Boolean>(this, "enable", Boolean.TRUE);
	
	private ReadOnlyObjectProperty<T1> sourcePosition;
	private ReadOnlyObjectProperty<T2> targetPosition;
		
	PositionPair(String name, T1 sourcePosition, T2 targetPosition) {
		if (sourcePosition.getDimension() != targetPosition.getDimension())
			throw new IllegalArgumentException("Error, cannot create point pair from "
					+ "points having different dimensions " + sourcePosition.getDimension() + " vs. " + targetPosition.getDimension());
		
		this.sourcePosition = new ReadOnlyObjectWrapper<T1>(this, "sourcePosition", sourcePosition);
		this.targetPosition = new ReadOnlyObjectWrapper<T2>(this, "targetPosition", targetPosition);
		this.setName(name);
	}
	
	public ObservableObjectValue<T1> sourceSystemPositionProperty() {
		return this.sourcePosition;
	}
	
	public ObservableObjectValue<T2> targetSystemPositionProperty() {
		return this.targetPosition;
	}
	
	public T1 getSourceSystemPosition() {
		return this.sourcePosition.get();
	}
	
	public T2 getTargetSystemPosition() {
		return this.targetPosition.get();
	}
	
	public ObservableObjectValue<Double> sourceXProperty() {
		return this.sourcePosition.get().xProperty();
	}
	
	public ObservableObjectValue<Double> sourceYProperty() {
		return this.sourcePosition.get().yProperty();
	}
	
	public ObservableObjectValue<Double> sourceZProperty() {
		return this.sourcePosition.get().zProperty();
	}
	
	public ObservableObjectValue<Double> targetXProperty() {
		return this.targetPosition.get().xProperty();
	}
	
	public ObservableObjectValue<Double> targetYProperty() {
		return this.targetPosition.get().yProperty();
	}
	
	public ObservableObjectValue<Double> targetZProperty() {
		return this.targetPosition.get().zProperty();
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
	
	public boolean equalsCoordinateComponents(PositionPair<?,?> positionPair) {
		return this.getSourceSystemPosition().equalsPosition(positionPair.getSourceSystemPosition()) && 
		this.getTargetSystemPosition().equalsPosition(positionPair.getTargetSystemPosition());
	}

	public boolean isEnable() {
		return this.enable.get();
	}
	
	public void setEnable(boolean enable) {
		this.enable.set(enable);
	}
	
	public ObjectProperty<Boolean> enableProperty() {
		return this.enable;
	}
	
	public void reset() {}
	
	@Override
	public String toString() {
		return "PositionPair {\n"
				+ "\t[ " + this.sourcePosition.get().getX() + " / " + this.sourcePosition.get().getY() + " / " + this.sourcePosition.get().getZ() + " ]"
				+ "\t[ " + this.targetPosition.get().getX() + " / " + this.targetPosition.get().getY() + " / " + this.targetPosition.get().getZ() + " ]"
				+ "}";
	}
}
