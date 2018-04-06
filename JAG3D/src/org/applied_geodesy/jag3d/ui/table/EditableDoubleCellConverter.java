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

package org.applied_geodesy.jag3d.ui.table;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.applied_geodesy.util.FormatterOptions;

public class EditableDoubleCellConverter extends EditableCellConverter<Double> {
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
			
			int fracDigits = options.getFormatterOptions().get(this.cellValueType).getFractionDigits();
			if (this.editorNumberFormat.getMinimumFractionDigits() != fracDigits) {
				this.editorNumberFormat.setMaximumFractionDigits(fracDigits + EXTRA_DIGITS_ON_EDIT);
				this.editorNumberFormat.setMinimumFractionDigits(fracDigits);
			}

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
			return options.toStatisticFormat(value.doubleValue());
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
				string = string.replaceAll(",", ".");
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

//	@Override
//	public void formatterChanged(FormatterEvent evt) {
//		switch(evt.getEventType()) {
//		case RESOLUTION_CHANGED:
//			this.editorNumberFormat.setMinimumFractionDigits(evt.getNewResultion());
//			this.editorNumberFormat.setMaximumFractionDigits(evt.getNewResultion() + EXTRA_DIGITS_ON_EDIT);
//			break;
//		case UNIT_CHANGED:
//			break;
//		}
//
//		if (this.tableCell != null)
//			this.tableCell.setText(this.toString((Double)this.tableCell.getItem()));
//	}
}
