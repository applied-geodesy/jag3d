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

package org.applied_geodesy.juniform.ui.table;

import java.util.function.Predicate;

import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.FeatureChangeListener;
import org.applied_geodesy.adjustment.geometry.FeatureEvent;
import org.applied_geodesy.adjustment.geometry.FeatureEvent.FeatureEventType;
import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.juniform.ui.dialog.UnknownParameterTypeDialog;
import org.applied_geodesy.ui.table.ColumnTooltipHeader;
import org.applied_geodesy.ui.table.ColumnType;
import org.applied_geodesy.ui.table.DisplayCellFormatType;
import org.applied_geodesy.ui.table.NaturalOrderComparator;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.ObservableUniqueList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class UIParameterTableBuilder extends UIEditableTableBuilder<UnknownParameter> implements FeatureChangeListener {
	private static UIParameterTableBuilder tableBuilder = new UIParameterTableBuilder();
	private boolean isInitialize = false;
	private ObservableUniqueList<UnknownParameter> unknownFeatureParameters;
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
	
	public TableView<UnknownParameter> getTable(GeometricPrimitive geometry) {
		// show all items
		if (geometry == null) {
			if (this.unknownFeatureParameters != null) {
				FilteredList<UnknownParameter> filteredFeatureParameters = new FilteredList<UnknownParameter>(this.unknownFeatureParameters, this.visiblePredicate);
				this.table.setItems(filteredFeatureParameters);
			}
		}
		else 
			this.table.setItems(FXCollections.observableArrayList(geometry.getUnknownParameters()));

		return this.table;
	}

	private void init() {
		if (this.isInitialize)
			return;
		
		TableColumn<UnknownParameter, ParameterType> parameterTypeColumn = null;
//		TableColumn<UnknownParameter, ProcessingType> processingTypeColumn = null;
		TableColumn<UnknownParameter, Double> doubleColumn   = null; 
		TableColumn<UnknownParameter, String> stringColumn   = null; 

		TableView<UnknownParameter> table = this.createTable();
		
		// Parameter name
		int columnIndex = table.getColumns().size(); 
		String labelText   = i18n.getString("UIParameterTableBuilder.tableheader.name.label", "Name");
		String tooltipText = i18n.getString("UIParameterTableBuilder.tableheader.name.tooltip", "Name of the model parameter");
		ColumnTooltipHeader header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, UnknownParameter::nameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true); 
		stringColumn.setComparator(new NaturalOrderComparator<String>());
		table.getColumns().add(stringColumn);
		
		// Parameter type
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIParameterTableBuilder.tableheader.type.label", "Type");
		tooltipText = i18n.getString("UIParameterTableBuilder.tableheader.type.tooltip", "Type of the model parameter");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
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
		labelText   = i18n.getString("UIParameterTableBuilder.tableheader.uncertainty.label", "\u03C3");
		tooltipText = i18n.getString("UIParameterTableBuilder.tableheader.uncertainty.tooltip", "A-posteriori uncertainty of parameter");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, UnknownParameter::uncertaintyProperty, getDoubleValueWithUnitCallback(DisplayCellFormatType.UNCERTAINTY), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		// description text
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIParameterTableBuilder.tableheader.description.label", "Description");
		tooltipText = i18n.getString("UIParameterTableBuilder.tableheader.description.tooltip", "User-defined description of the parameter");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText); 
		stringColumn = this.<String>getColumn(header, UnknownParameter::descriptionProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true); 
		stringColumn.setComparator(new NaturalOrderComparator<String>());
		table.getColumns().add(stringColumn);
		
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
            					return UnknownParameterTypeDialog.getParameterTypeLabel(parameterType);
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
		
	private void setFeature(Feature feature) {
		if (feature == null)
			this.unknownFeatureParameters = new ObservableUniqueList<>(0);
		else
			this.unknownFeatureParameters = feature.getUnknownParameters();
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
	
	@Override
	public void featureChanged(FeatureEvent evt) {
		if (evt.getEventType() == FeatureEventType.FEATURE_ADDED)
			this.setFeature(evt.getSource());
		else if (evt.getEventType() == FeatureEventType.FEATURE_REMOVED)
			this.setFeature(null);
	}

}
