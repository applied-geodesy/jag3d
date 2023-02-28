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
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

public class UnknownParameter extends Parameter {
	/** Value: -1 == not set, Integer.MIN_VALUE == fixed, else column in normal equation system **/
	private ObjectProperty<ProcessingType> processingType = new SimpleObjectProperty<ProcessingType>(ProcessingType.ADJUSTMENT);
	
	private ObjectProperty<Integer> column     = new SimpleObjectProperty<Integer>(this, "column", -1);
	private ObjectProperty<Double> value       = new SimpleObjectProperty<Double>(this, "value", 0.0);
	private ObjectProperty<Double> uncertainty = new SimpleObjectProperty<Double>(this, "uncertainty", 0.0);
	private ReadOnlyObjectProperty<Boolean> indispensable;
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable) {
		this(parameterType, indispensable, 0, Boolean.TRUE);
	}
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable, boolean visible) {
		this(parameterType, indispensable, 0, visible);
	}
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable, double value) {
		this(parameterType, indispensable, value, Boolean.TRUE);
	}
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable, double value, boolean visible) {
		this(parameterType, indispensable, value, visible, -1, ProcessingType.ADJUSTMENT);
	}
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable, double value, boolean visible, ProcessingType processingType) {
		this(parameterType, indispensable, value, visible, -1, processingType);
	}
	
	public UnknownParameter(ParameterType parameterType, boolean indispensable, double value, boolean visible, int column, ProcessingType processingType) {
		super(parameterType);
		this.setValue0(value);
		this.setColumn(column);
		this.setProcessingType(processingType);
		this.setVisible(visible);
		this.indispensable = new ReadOnlyObjectWrapper<Boolean>(this, "indispensable", indispensable);
	}
	
	public ReadOnlyObjectProperty<Boolean> indispensableProperty() {
		return this.indispensable;
	}
	
	public boolean isIndispensable() {
		return this.indispensable.get();
	}
	
	public void setColumn(int column) {
		this.column.set(column);
	}
	
	public int getColumn() {
		return this.column.get();
	}
	
	public ObjectProperty<Integer> columnProperty() {
		return this.column;
	}
	
	public ProcessingType getProcessingType() {
		return this.processingType.get();
	}
	
	public void setProcessingType(ProcessingType processingType) {
		this.processingType.set(processingType);
		if (processingType != ProcessingType.ADJUSTMENT)
			this.column.set(-1);
	}
	
	public ObjectProperty<ProcessingType> processingTypeProperty() {
		return this.processingType;
	}
	
	public void setValue(double value) {
		this.value.set(value);
	}
	
	public double getValue() {
		return this.value.get();
	}
	
	public ObjectProperty<Double> valueProperty() {
		return this.value;
	}
	
	public void setUncertainty(double uncertainty) {
		this.uncertainty.set(uncertainty);
	}
	
	public double getUncertainty() {
		return this.uncertainty.get();
	}
	
	public ObjectProperty<Double> uncertaintyProperty() {
		return this.uncertainty;
	}

	@Override
	public String toString() {
		return "UnknownParameter [Name=" + getName() + ", ParameterType=" + getParameterType()
			+ ", Value0=" + getValue0() + ", Column=" + getColumn()
			+ ", ProcessingType="	+ getProcessingType() + ", Value=" + getValue() + "]";
	}	
}
