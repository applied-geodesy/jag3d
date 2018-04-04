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

import org.applied_geodesy.jag3d.ui.table.row.AdditionalParameterRow;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterOptions;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;

class AdditionalParameterDoubleCell extends TableCell<AdditionalParameterRow, Double> implements FormatterChangedListener {
	private DisplayCellFormatType displayFormatType;
	private FormatterOptions options = FormatterOptions.getInstance();
	Double item;
	AdditionalParameterDoubleCell(DisplayCellFormatType displayFormatType) {
		this.displayFormatType = displayFormatType;
		this.setAlignment(Pos.CENTER_RIGHT);
		this.options.addFormatterChangedListener(this);
	}
		
	@Override
    protected void updateItem(Double value, boolean empty) {
		int currentIndex = indexProperty().getValue();

		if (!empty && currentIndex >= 0 && currentIndex < this.getTableView().getItems().size()) {
			AdditionalParameterRow paramRow = this.getTableView().getItems().get(currentIndex);
			this.item = value;
			this.toFormattedString(paramRow, this.item);
		}
		else
			setText(value == null ? null : value.toString());
	}
	
	// https://stackoverflow.com/questions/27281370/javafx-tableview-format-one-cell-based-on-the-value-of-another-in-the-row
	private void toFormattedString(AdditionalParameterRow paramRow, Double value) {
		if (value != null && paramRow != null && paramRow.getParameterType() != null) {
			switch(paramRow.getParameterType()) {
			case ZERO_POINT_OFFSET:
			case STRAIN_TRANSLATION_X:
			case STRAIN_TRANSLATION_Y:
			case STRAIN_TRANSLATION_Z:
				if (this.displayFormatType == DisplayCellFormatType.NORMAL)
					this.setText(options.toLengthFormat(value.doubleValue(), true));
				else if (this.displayFormatType == DisplayCellFormatType.UNCERTAINTY)
					this.setText(options.toLengthUncertaintyFormat(value.doubleValue(), true));
				else if (this.displayFormatType == DisplayCellFormatType.RESIDUAL)
					this.setText(options.toLengthResidualFormat(value.doubleValue(), true));
				
				break;
			case SCALE:
			case STRAIN_SCALE_X:
			case STRAIN_SCALE_Y:
			case STRAIN_SCALE_Z:
				
				if (this.displayFormatType == DisplayCellFormatType.NORMAL)
					this.setText(options.toScaleFormat(value.doubleValue(), true));
				else if (this.displayFormatType == DisplayCellFormatType.UNCERTAINTY)
					this.setText(options.toScaleUncertaintyFormat(value.doubleValue(), true));
				else if (this.displayFormatType == DisplayCellFormatType.RESIDUAL)
					this.setText(options.toScaleResidualFormat(value.doubleValue(), true));

				break;
			case ORIENTATION:
			case ROTATION_X:
			case ROTATION_Y:
			case ROTATION_Z:
			case STRAIN_ROTATION_X:
			case STRAIN_ROTATION_Y:
			case STRAIN_ROTATION_Z:
			case STRAIN_SHEAR_X:
			case STRAIN_SHEAR_Y:
			case STRAIN_SHEAR_Z:
				if (this.displayFormatType == DisplayCellFormatType.NORMAL)
					this.setText(options.toAngleFormat(value.doubleValue(), true));
				else if (this.displayFormatType == DisplayCellFormatType.UNCERTAINTY)
					this.setText(options.toAngleUncertaintyFormat(value.doubleValue(), true));
				else if (this.displayFormatType == DisplayCellFormatType.RESIDUAL)
					this.setText(options.toAngleResidualFormat(value.doubleValue(), true));

				break;
			case REFRACTION_INDEX:
				this.setText(options.toStatisticFormat(value.doubleValue()));
				break;
				
			default:
				System.err.println(UIAdditionalParameterTableBuilder.class.getSimpleName() + " : Error, unknown parameter type " + paramRow.getParameterType());
				setText(null);
				break;
				
			}
		}
		else 
			setText(null);
	}
	
	@Override
	public void formatterChanged(FormatterEvent evt) {
		int idx = this.getTableRow().getIndex();
		int itemSize = this.getTableView().getItems().size();
		if (idx >= 0 && idx < itemSize) {
			AdditionalParameterRow paramRow = this.getTableView().getItems().get(idx);
			this.toFormattedString(paramRow, this.item);
		}
	}
}
