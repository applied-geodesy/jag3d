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

package org.applied_geodesy.jag3d.ui.table.row;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class PrincipalComponentRow extends Row {

	private ObjectProperty<Integer> index = new SimpleObjectProperty<Integer>(this, "index");
	private ObjectProperty<Double> value  = new SimpleObjectProperty<Double>(this, "value");
	private ObjectProperty<Double> ratio  = new SimpleObjectProperty<Double>(this, "ratio");
	public ObjectProperty<Integer> indexProperty() {
		return this.index;
	}
	
	public Integer getIndex() {
		return this.indexProperty().get();
	}
	
	public void setIndex(final Integer index) {
		this.indexProperty().set(index);
	}
	
	public ObjectProperty<Double> valueProperty() {
		return this.value;
	}
	
	public Double getValue() {
		return this.valueProperty().get();
	}
	
	public void setValue(final Double value) {
		this.valueProperty().set(value);
	}
	
	public ObjectProperty<Double> ratioProperty() {
		return this.ratio;
	}
	
	public Double getRatio() {
		return this.ratioProperty().get();
	}
	
	public void setRatio(final Double ratio) {
		this.ratioProperty().set(ratio);
	}
}
