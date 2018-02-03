package org.applied_geodesy.jag3d.ui.textfield;

import org.applied_geodesy.jag3d.ui.table.CellValueType;

public class LengthUnceraintySquareRootTextField extends UncertaintyTextField {
	private final boolean dividedBySquareRootLengthUnit;
	
	public LengthUnceraintySquareRootTextField(Double value) {
		this(value, false);
	}
	
	public LengthUnceraintySquareRootTextField(Double value, boolean displayUnit) {
		this(value, displayUnit, ValueSupport.GREATER_THAN_OR_EQUAL_TO_ZERO, false);
	}
	
	public LengthUnceraintySquareRootTextField(Double value, boolean displayUnit, boolean dividedBySquareRootLengthUnit) {
		this(value, displayUnit, ValueSupport.GREATER_THAN_OR_EQUAL_TO_ZERO, dividedBySquareRootLengthUnit);
	}
	
	public LengthUnceraintySquareRootTextField(Double value, boolean displayUnit, ValueSupport valueSupport, boolean dividedBySquareRootLengthUnit) {
		super(value, CellValueType.LENGTH_UNCERTAINTY, displayUnit, valueSupport);
		this.dividedBySquareRootLengthUnit = dividedBySquareRootLengthUnit;
		this.setText(this.getRendererFormat(value));
	}
	
	@Override
	String getRendererFormat(Double value) {
		if (!this.check(value))
			return null;

		return this.dividedBySquareRootLengthUnit ? 
				options.toLengthUncertaintyDividedBySquareRootLengthFormat(this.getNumber().doubleValue(), this.isDisplayUnit()) :
				options.toSquareRootLengthUncertaintyFormat(this.getNumber().doubleValue(), this.isDisplayUnit());
	}
}
