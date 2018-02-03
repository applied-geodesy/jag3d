package org.applied_geodesy.jag3d.ui.table;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterOptions;

public class EditableDoubleCellConverter extends EditableCellConverter<Double> implements FormatterChangedListener {
	private final CellValueType cellValueType;
	private final FormatterOptions options = FormatterOptions.getInstance();
	private final NumberFormat editorNumberFormat = NumberFormat.getInstance(Locale.ENGLISH);
	private boolean displayUnit = false;
	private final static int EXTRA_DIGITS_ON_EDIT = 7;
	
	EditableDoubleCellConverter(CellValueType cellValueType) {
		this(cellValueType, false);
	}
	
	EditableDoubleCellConverter(CellValueType cellValueType, boolean displayUnit) {
		this.cellValueType = cellValueType;
		this.options.addFormatterChangedListener(this);
		this.displayUnit = displayUnit;
		
		int fracDigits = options.getFormatterOptions().get(this.cellValueType).getFractionDigits();
		this.editorNumberFormat.setGroupingUsed(false);
		this.editorNumberFormat.setMaximumFractionDigits(fracDigits + EXTRA_DIGITS_ON_EDIT);
		this.editorNumberFormat.setMinimumFractionDigits(fracDigits);
	}
	
	@Override
	public String toEditorString(Double value) {
		try {
			if (value == null)
				return "";

			switch(this.cellValueType) {
			case ANGLE:
				return this.editorNumberFormat.format(options.convertAngleToView(value.doubleValue()));
			case ANGLE_RESIDUAL:
				return this.editorNumberFormat.format(options.convertAngleResidualToView(value.doubleValue()));
			case ANGLE_UNCERTAINTY:
				return this.editorNumberFormat.format(options.convertAngleUncertaintyToView(value.doubleValue()));
			case LENGTH:
				return this.editorNumberFormat.format(options.convertLengthToView(value.doubleValue()));
			case LENGTH_RESIDUAL:
				return this.editorNumberFormat.format(options.convertLengthResidualToView(value.doubleValue()));
			case LENGTH_UNCERTAINTY:
				return this.editorNumberFormat.format(options.convertLengthUncertaintyToView(value.doubleValue()));
			case SCALE:
				return this.editorNumberFormat.format(options.convertScaleToView(value.doubleValue()));
			case SCALE_UNCERTAINTY:
				return this.editorNumberFormat.format(options.convertScaleUncertaintyToView(value.doubleValue()));
			case STATISTIC:
				return this.editorNumberFormat.format(value.doubleValue());
			case VECTOR:
				return this.editorNumberFormat.format(options.convertVectorToView(value.doubleValue()));
			case VECTOR_UNCERTAINTY:
				return this.editorNumberFormat.format(options.convertVectorUncertaintyToView(value.doubleValue()));
			default:
				System.err.println(this.getClass().getSimpleName() + " : Unsupported cell value type " + this.cellValueType);
				return String.valueOf(value);
			}
		}
		catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		}
		return "";
	}

	@Override
	public String toString(Double value) {
		if (value == null)
			return "";

		switch(this.cellValueType) {
		case ANGLE:
			return options.toAngleFormat(value.doubleValue(), displayUnit);
		case ANGLE_RESIDUAL:
			return options.toAngleResidualFormat(value.doubleValue(), displayUnit);
		case ANGLE_UNCERTAINTY:
			return options.toAngleUncertaintyFormat(value.doubleValue(), displayUnit);
		case LENGTH:
			return options.toLengthFormat(value.doubleValue(), displayUnit);
		case LENGTH_RESIDUAL:
			return options.toLengthResidualFormat(value.doubleValue(), displayUnit);
		case LENGTH_UNCERTAINTY:
			return options.toLengthUncertaintyFormat(value.doubleValue(), displayUnit);
		case SCALE:
			return options.toScaleFormat(value.doubleValue(), displayUnit);
		case SCALE_UNCERTAINTY:
			return options.toScaleUncertaintyFormat(value.doubleValue(), displayUnit);
		case STATISTIC:
			return options.toTestStatisticFormat(value.doubleValue());
		case VECTOR:
			return options.toVectorFormat(value.doubleValue(), displayUnit);
		case VECTOR_UNCERTAINTY:
			return options.toVectorUncertaintyFormat(value.doubleValue(), displayUnit);
		default:
			System.err.println(this.getClass().getSimpleName() + " : Unsupported cell value type " + this.cellValueType);
			return String.valueOf(value);
		}
	}

	@Override
	public Double fromString(String string) {
		if (string != null && !string.trim().isEmpty()) {
			try {
				double value = options.getFormatterOptions().get(this.cellValueType).parse(string.trim()).doubleValue();
				switch(this.cellValueType) {
				case ANGLE:
					return options.convertAngleToModel(value);
				case ANGLE_RESIDUAL:
					return options.convertAngleResidualToModel(value);
				case ANGLE_UNCERTAINTY:
					return options.convertAngleUncertaintyToModel(value);
				case LENGTH:
					return options.convertLengthToModel(value);
				case LENGTH_RESIDUAL:
					return options.convertLengthResidualToModel(value);
				case LENGTH_UNCERTAINTY:
					return options.convertLengthUncertaintyToModel(value);
				case SCALE:
					return options.convertScaleToModel(value);
				case SCALE_UNCERTAINTY:
					return options.convertScaleUncertaintyToModel(value);
				case STATISTIC:
					return value;
				case VECTOR:
					return options.convertVectorToModel(value);
				case VECTOR_UNCERTAINTY:
					return options.convertVectorUncertaintyToModel(value);
				default:
					System.err.println(this.getClass().getSimpleName() + " : Unsupported cell value type " + this.cellValueType);
					return value;
				}

			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void formatterChanged(FormatterEvent evt) {
		switch(evt.getEventType()) {
		case RESOLUTION_CHANGED:
			this.editorNumberFormat.setMinimumFractionDigits(evt.getNewResultion());
			this.editorNumberFormat.setMaximumFractionDigits(evt.getNewResultion() + EXTRA_DIGITS_ON_EDIT);
			break;
		case UNIT_CHANGED:
			break;
		}

		this.tableCell.setText(this.tableCell == null ? null :  this.toString((Double)this.tableCell.getItem()));
	}
}
