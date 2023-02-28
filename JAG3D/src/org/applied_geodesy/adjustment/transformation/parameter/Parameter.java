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

package org.applied_geodesy.adjustment.transformation.parameter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Parameter {
	private ObjectProperty<ParameterType> parameterType = new SimpleObjectProperty<ParameterType>(this, "parameterType");
	private ObjectProperty<Double> value0 = new SimpleObjectProperty<Double>(this, "value0", 0.0);
	private ObjectProperty<String> description = new SimpleObjectProperty<String>(this, "description", "");
	private ObjectProperty<String> name = new SimpleObjectProperty<String>(this, "name", "");
	private ObjectProperty<Boolean> visible = new SimpleObjectProperty<Boolean>(this, "visible", Boolean.TRUE);
	
	public Parameter(ParameterType parameterType) {
		this(parameterType, Boolean.TRUE);
	}
	
	public Parameter(ParameterType parameterType, boolean visible) {
		this.setParameterType(parameterType);
		this.setVisible(visible);
	}
	
	public ObjectProperty<ParameterType> parameterTypeProperty() {
		return this.parameterType;
	}
	
	public void setParameterType(ParameterType parameterType) {
		this.parameterType.set(parameterType);
	}
	
	public ParameterType getParameterType() {
		return this.parameterType.get();
	}
	
	public void setVisible(boolean visible) {
		this.visible.set(visible);
	}
	
	public boolean isVisible() {
		return this.visible.get();
	}
	
	public ObjectProperty<Boolean> visibleProperty() {
		return this.visible;
	}

	public void setValue0(double value0) {
		this.value0.set(value0);
	}
	
	public double getValue0() {
		return this.value0.get();
	}
	
	public ObjectProperty<Double> value0Property() {
		return this.value0;
	}
	
	public void setDescription(String description) {
		this.description.set(description);
	}
	
	public String getDescription() {
		return this.description.get();
	}
	
	public ObjectProperty<String> descriptionProperty() {
		return this.description;
	}
	
	public void setName(String name) {
		this.name.set(name);
	}
	
	public String getName() {
		return this.name.get();
	}
	
	public ObjectProperty<String> nameProperty() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name.get();
	}
}
