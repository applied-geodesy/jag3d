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

package org.applied_geodesy.coordtrans.ui.table;

import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.ui.table.DisplayCellFormatType;
import org.applied_geodesy.ui.table.EditableCell;
import org.applied_geodesy.ui.table.EditableDoubleCellConverter;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterOptions;

import javafx.geometry.Pos;
import javafx.scene.control.TableRow;

class ParameterDoubleCell extends EditableCell<UnknownParameter, Double> implements FormatterChangedListener {
	private DisplayCellFormatType displayFormatType;
	private EditableDoubleCellConverter editableDoubleCellConverter;
	private FormatterOptions options = FormatterOptions.getInstance();

	ParameterDoubleCell(DisplayCellFormatType displayFormatType) {
		super(new EditableDoubleCellConverter(CellValueType.LENGTH, true));
		this.editableDoubleCellConverter = (EditableDoubleCellConverter)this.getEditableCellConverter();
		this.displayFormatType = displayFormatType;
		this.setAlignment(Pos.CENTER_RIGHT);
		this.options.addFormatterChangedListener(this);
	}
		
	@Override
    protected void updateItem(Double value, boolean empty) {
		int currentIndex = indexProperty().getValue();
		if (!empty && currentIndex >= 0 && currentIndex < this.getTableView().getItems().size()) {
			UnknownParameter paramRow = this.getTableView().getItems().get(currentIndex);
			this.setCellValueTypeOfUnknownParameter(paramRow);
		}
		super.updateItem(value, empty);
	}
	
	// https://stackoverflow.com/questions/27281370/javafx-tableview-format-one-cell-based-on-the-value-of-another-in-the-row
	private void setCellValueTypeOfUnknownParameter(UnknownParameter paramRow) {
		if (paramRow != null && paramRow.getParameterType() != null) {
			switch(paramRow.getParameterType()) {
			case SHIFT_X:
			case SHIFT_Y:
			case SHIFT_Z:
			
				if (this.displayFormatType == DisplayCellFormatType.NORMAL)
					this.editableDoubleCellConverter.setCellValueType(CellValueType.LENGTH);
				else if (this.displayFormatType == DisplayCellFormatType.UNCERTAINTY) 
					this.editableDoubleCellConverter.setCellValueType(CellValueType.LENGTH_UNCERTAINTY);
				else if (this.displayFormatType == DisplayCellFormatType.RESIDUAL) 
					this.editableDoubleCellConverter.setCellValueType(CellValueType.LENGTH_RESIDUAL);
				
				break;
			
			case QUATERNION_Q0:
			case QUATERNION_Q1:
			case QUATERNION_Q2:
			case QUATERNION_Q3:
			case VECTOR_LENGTH:

				if (this.displayFormatType == DisplayCellFormatType.NORMAL)
					this.editableDoubleCellConverter.setCellValueType(CellValueType.VECTOR);
				else if (this.displayFormatType == DisplayCellFormatType.UNCERTAINTY) 
					this.editableDoubleCellConverter.setCellValueType(CellValueType.VECTOR_UNCERTAINTY);
				else if (this.displayFormatType == DisplayCellFormatType.RESIDUAL) 
					this.editableDoubleCellConverter.setCellValueType(CellValueType.VECTOR_RESIDUAL);

				break;
				
			case EULER_ANGLE_X:
			case EULER_ANGLE_Y:
			case EULER_ANGLE_Z:
			case SHEAR_X:
			case SHEAR_Y:
			case SHEAR_Z:
				
				if (this.displayFormatType == DisplayCellFormatType.NORMAL)
					this.editableDoubleCellConverter.setCellValueType(CellValueType.ANGLE);
				else if (this.displayFormatType == DisplayCellFormatType.UNCERTAINTY) 
					this.editableDoubleCellConverter.setCellValueType(CellValueType.ANGLE_UNCERTAINTY);
				else if (this.displayFormatType == DisplayCellFormatType.RESIDUAL) 
					this.editableDoubleCellConverter.setCellValueType(CellValueType.ANGLE_RESIDUAL);

				break;
				
			case SCALE_X:
			case SCALE_Y:
			case SCALE_Z:
				
				if (this.displayFormatType == DisplayCellFormatType.NORMAL)
					this.editableDoubleCellConverter.setCellValueType(CellValueType.SCALE);
				else if (this.displayFormatType == DisplayCellFormatType.UNCERTAINTY) 
					this.editableDoubleCellConverter.setCellValueType(CellValueType.SCALE_UNCERTAINTY);
				else if (this.displayFormatType == DisplayCellFormatType.RESIDUAL) 
					this.editableDoubleCellConverter.setCellValueType(CellValueType.SCALE_RESIDUAL);

				break;
				
			case SCALE_SHEAR_COMPONENT_S11:
			case SCALE_SHEAR_COMPONENT_S12:
			case SCALE_SHEAR_COMPONENT_S13:
			case SCALE_SHEAR_COMPONENT_S22:
			case SCALE_SHEAR_COMPONENT_S23:
			case SCALE_SHEAR_COMPONENT_S33:
			case CONSTANT:
				
				this.editableDoubleCellConverter.setCellValueType(CellValueType.DOUBLE);
				break;

			default:
				throw new IllegalArgumentException("Error, unknown type of parameter " + paramRow.getParameterType());
			}
		}
	}
	
	@Override
	public void formatterChanged(FormatterEvent evt) {
//		int idx = this.getTableRow().getIndex();
//		int itemSize = this.getTableView().getItems().size();
//		if (idx >= 0 && idx < itemSize) {
//			UnknownParameter paramRow = this.getTableView().getItems().get(idx);
//			this.setCellValueTypeOfUnknownParameter(paramRow);
//		}
		
		TableRow<UnknownParameter> tableRow = this.getTableRow();
		if (tableRow == null || tableRow.isEmpty() || this.getTableRow().getItem() == null)
			return;
		
		UnknownParameter paramRow = this.getTableRow().getItem();
		this.setCellValueTypeOfUnknownParameter(paramRow);
	}
}
