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

import java.util.function.Predicate;

import org.applied_geodesy.adjustment.transformation.Transformation;
import org.applied_geodesy.adjustment.transformation.TransformationChangeListener;
import org.applied_geodesy.adjustment.transformation.TransformationEvent;
import org.applied_geodesy.adjustment.transformation.TransformationEvent.TransformationEventType;
import org.applied_geodesy.adjustment.transformation.parameter.ParameterType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.ui.table.ColumnTooltipHeader;
import org.applied_geodesy.ui.table.ColumnType;
import org.applied_geodesy.ui.table.DisplayCellFormatType;
import org.applied_geodesy.ui.table.NaturalOrderComparator;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.ObservableUniqueList;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class UIParameterTableBuilder extends UIEditableTableBuilder<UnknownParameter> implements TransformationChangeListener {
	private static UIParameterTableBuilder tableBuilder = new UIParameterTableBuilder();
	private boolean isInitialize = false;
	private ObservableUniqueList<UnknownParameter> unknownTransformationParameters;
	private Predicate<UnknownParameter> visiblePredicate = new Predicate<UnknownParameter>() {
		@Override
		public boolean test(UnknownParameter unknownParameter) {
			return unknownParameter.isVisible();
		}
	};
	private UIParameterTableBuilder() {
		super();
	}
	
	public static UIParameterTableBuilder getInstance() {
		tableBuilder.init();
		return tableBuilder;
	}
	
	public TableView<UnknownParameter> getTable() {
		return this.getTable(null);
	}
	
	public TableView<UnknownParameter> getTable(Transformation transformation) {
		// show all items
		if (transformation == null) {
			if (this.unknownTransformationParameters != null) {
				FilteredList<UnknownParameter> filteredFeatureParameters = new FilteredList<UnknownParameter>(this.unknownTransformationParameters, this.visiblePredicate);
				this.table.setItems(filteredFeatureParameters);
			}
		}
		else 
			this.table.setItems(FXCollections.observableArrayList(transformation.getUnknownParameters()));

		return this.table;
	}

	private void init() {
		if (this.isInitialize)
			return;
		
		TableColumn<UnknownParameter, ParameterType> parameterTypeColumn = null;
		TableColumn<UnknownParameter, Double> doubleColumn   = null; 
		TableColumn<UnknownParameter, Boolean> booleanColumn = null;

		TableView<UnknownParameter> table = this.createTable();
		
		// Parameter type
		int columnIndex = table.getColumns().size(); 
		String labelText   = i18n.getString("UIParameterTableBuilder.tableheader.type.label", "Type");
		String tooltipText = i18n.getString("UIParameterTableBuilder.tableheader.type.tooltip", "Type of the model parameter");
		ColumnTooltipHeader header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		parameterTypeColumn = this.<ParameterType>getColumn(header, UnknownParameter::parameterTypeProperty, getParameterTypeCallback(), ColumnType.VISIBLE, columnIndex, false); 
		parameterTypeColumn.setComparator(new NaturalOrderComparator<ParameterType>());
		table.getColumns().add(parameterTypeColumn);
			
		///////////////// A-POSTERIORI VALUES /////////////////////////////
		// Parameter value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIParameterTableBuilder.tableheader.value.aposteriori.label", "Estimated value");
		tooltipText = i18n.getString("UIParameterTableBuilder.tableheader.value.aposteriori.tooltip", "Estimated value of model parameter");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, UnknownParameter::valueProperty, getDoubleValueWithUnitCallback(DisplayCellFormatType.NORMAL), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		// Uncertainty
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIParameterTableBuilder.tableheader.uncertainty.label", "Uncertainty");
		tooltipText = i18n.getString("UIParameterTableBuilder.tableheader.uncertainty.tooltip", "A-posteriori uncertainty of parameter");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, UnknownParameter::uncertaintyProperty, getDoubleValueWithUnitCallback(DisplayCellFormatType.UNCERTAINTY), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		// Decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexSignificant = columnIndex;
		labelText   = i18n.getString("UIParameterTableBuilder.tableheader.testdecision.label", "Significant");
		tooltipText = i18n.getString("UIParameterTableBuilder.tableheader.testdecision.tooltip", "Checked, if null-hypothesis is rejected");
		header = new ColumnTooltipHeader(CellValueType.BOOLEAN, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, UnknownParameter::significantProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, false);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<UnknownParameter, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<UnknownParameter, Boolean> param) {
				final TableCellChangeListener<Boolean> significantChangeListener = new TableCellChangeListener<Boolean>(columnIndexSignificant, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isSignificant());
				booleanProp.addListener(significantChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);
		
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table = table;
		this.isInitialize = true;
	}
	
	private static Callback<TableColumn<UnknownParameter, ParameterType>, TableCell<UnknownParameter, ParameterType>> getParameterTypeCallback() {
        return new Callback<TableColumn<UnknownParameter, ParameterType>, TableCell<UnknownParameter, ParameterType>>() {
            @Override
            public TableCell<UnknownParameter, ParameterType> call(TableColumn<UnknownParameter, ParameterType> cell) {
            	TableCell<UnknownParameter, ParameterType> tableCell = new TextFieldTableCell<UnknownParameter, ParameterType>(
            			new StringConverter<ParameterType>() {

            				@Override
            				public String toString(ParameterType parameterType) {
            					return getParameterTypeLabel(parameterType);
            				}

            				@Override
            				public ParameterType fromString(String string) {
            					return ParameterType.valueOf(string);
            				}
            			});
            	tableCell.setAlignment(Pos.CENTER_LEFT);
            	return tableCell;	
            }
        };
    }
	
	private static Callback<TableColumn<UnknownParameter, Double>, TableCell<UnknownParameter, Double>> getDoubleValueWithUnitCallback(DisplayCellFormatType displayFormatType) {
		return new Callback<TableColumn<UnknownParameter, Double>, TableCell<UnknownParameter, Double>>() {
			@Override
			public TableCell<UnknownParameter, Double> call(TableColumn<UnknownParameter, Double> cell) {
				return new ParameterDoubleCell(displayFormatType);
			}
		};
    }
		
	private void setTransformation(Transformation transformation) {
		if (transformation == null)
			this.unknownTransformationParameters = new ObservableUniqueList<>(0);
		else
			this.unknownTransformationParameters = transformation.getUnknownParameters();
	}

	@Override
	void setValue(UnknownParameter unknownParameter, int columnIndex, Object oldValue, Object newValue) {
		boolean valid = (oldValue == null || oldValue.toString().trim().isEmpty()) && (newValue == null || newValue.toString().trim().isEmpty());
		switch (columnIndex) {
		case 0:
			if (newValue != null && !newValue.toString().trim().isEmpty()) {
				unknownParameter.setName(newValue.toString().trim());
				valid = true;
			}
			else
				unknownParameter.setName(oldValue == null ? null : oldValue.toString().trim());
			break;
			
		case 4:
			if (newValue != null && !newValue.toString().trim().isEmpty()) {
				unknownParameter.setDescription(newValue.toString().trim());
				valid = true;
			}
			else
				unknownParameter.setDescription(oldValue == null ? null : oldValue.toString().trim());
			break;
			
		default:
			System.err.println(this.getClass().getSimpleName() + " : Editable column exceed " + columnIndex);
			valid = false;
			break;
		}
		
		if (!valid) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					table.refresh();
					table.requestFocus();
					table.getSelectionModel().clearSelection();
					table.getSelectionModel().select(unknownParameter);
				}
			});
		}
	}
	
	public static String getParameterTypeLabel(ParameterType parameterType) {
		if (parameterType == null)
			return null;

		switch (parameterType) {
		case SHIFT_X:
			return i18n.getString("UIParameterTableBuilder.parameter.type.shift.x", "Shift tx");
		case SHIFT_Y:
			return i18n.getString("UIParameterTableBuilder.parameter.type.shift.y", "Shift ty");
		case SHIFT_Z:
			return i18n.getString("UIParameterTableBuilder.parameter.type.shift.z", "Shift tz");
			
		case QUATERNION_Q0:
			return i18n.getString("UIParameterTableBuilder.parameter.type.quaternion.q0", "Quaternion q0");
		case QUATERNION_Q1:
			return i18n.getString("UIParameterTableBuilder.parameter.type.quaternion.q1", "Quaternion q1");
		case QUATERNION_Q2:
			return i18n.getString("UIParameterTableBuilder.parameter.type.quaternion.q2", "Quaternion q2");
		case QUATERNION_Q3:
			return i18n.getString("UIParameterTableBuilder.parameter.type.quaternion.q3", "Quaternion q3");
			
		case VECTOR_LENGTH:
			return i18n.getString("UIParameterTableBuilder.parameter.type.vector.norm", "Norm of vector \u2016n\u2016");
			
		case SCALE_X:
			return i18n.getString("UIParameterTableBuilder.parameter.type.scale.x", "Scale mx");
		case SCALE_Y:
			return i18n.getString("UIParameterTableBuilder.parameter.type.scale.y", "Scale my");
		case SCALE_Z:
			return i18n.getString("UIParameterTableBuilder.parameter.type.scale.z", "Scale mz");
			
		case SHEAR_X:
			return i18n.getString("UIParameterTableBuilder.parameter.type.shear.x", "Shear sx");
		case SHEAR_Y:
			return i18n.getString("UIParameterTableBuilder.parameter.type.shear.y", "Shear sy");
		case SHEAR_Z:
			return i18n.getString("UIParameterTableBuilder.parameter.type.shear.z", "Shear sz");
			
		case EULER_ANGLE_X:
			return i18n.getString("UIParameterTableBuilder.parameter.type.angle.x", "Euler angle rx");
		case EULER_ANGLE_Y:
			return i18n.getString("UIParameterTableBuilder.parameter.type.angle.y", "Euler angle ry");
		case EULER_ANGLE_Z:
			return i18n.getString("UIParameterTableBuilder.parameter.type.angle.x", "Euler angle rz");
			
		case AUXILIARY_ELEMENT_11:
			return i18n.getString("UIParameterTableBuilder.parameter.type.auxiliary.element11", "Auxiliary quantity e11");
		case AUXILIARY_ELEMENT_12:
			return i18n.getString("UIParameterTableBuilder.parameter.type.auxiliary.element12", "Auxiliary quantity e12");
		case AUXILIARY_ELEMENT_13:
			return i18n.getString("UIParameterTableBuilder.parameter.type.auxiliary.element13", "Auxiliary quantity e13");
		case AUXILIARY_ELEMENT_21:
			return i18n.getString("UIParameterTableBuilder.parameter.type.auxiliary.element22", "Auxiliary quantity e21");
		case AUXILIARY_ELEMENT_22:
			return i18n.getString("UIParameterTableBuilder.parameter.type.auxiliary.element22", "Auxiliary quantity e22");
		case AUXILIARY_ELEMENT_23:
			return i18n.getString("UIParameterTableBuilder.parameter.type.auxiliary.element23", "Auxiliary quantity e23");
		case AUXILIARY_ELEMENT_33:
			return i18n.getString("UIParameterTableBuilder.parameter.type.auxiliary.element33", "Auxiliary quantity e33");
		case CONSTANT:
			return i18n.getString("UIParameterTableBuilder.parameter.type.constant", "Constant");
		}
		return null;
	}
	
	@Override
	public void transformationChanged(TransformationEvent evt) {
		if (evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_ADDED)
			this.setTransformation(evt.getSource());
		else if (evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_REMOVED)
			this.setTransformation(null);
	}
}
