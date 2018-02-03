package org.applied_geodesy.jag3d.ui.textfield;

import org.applied_geodesy.jag3d.ui.table.CellValueType;

public class UncertaintyTextField extends DoubleTextField {
	
	public UncertaintyTextField(Double value, CellValueType type) {
		this(value, type, false);
	}

	public UncertaintyTextField(Double value, CellValueType type, boolean displayUnit) {
		this(value, type, displayUnit, ValueSupport.GREATER_THAN_OR_EQUAL_TO_ZERO);
	}
	
	public UncertaintyTextField(Double value, CellValueType type, boolean displayUnit, ValueSupport valueSupport) {
		super(value, type, displayUnit, valueSupport);
		
		if (valueSupport != ValueSupport.GREATER_THAN_OR_EQUAL_TO_ZERO && valueSupport != ValueSupport.GREATER_THAN_ZERO)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Error, uncertainty value must be greater than or equal to zeros, or greater than zero " + valueSupport);
	}
}

