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

import java.util.HashMap;
import java.util.Map;

import org.applied_geodesy.adjustment.network.VarianceComponentType;
import org.applied_geodesy.jag3d.ui.table.column.ColumnContentType;
import org.applied_geodesy.jag3d.ui.table.column.TableContentType;
import org.applied_geodesy.jag3d.ui.table.row.VarianceComponentRow;
import org.applied_geodesy.ui.table.ColumnTooltipHeader;
import org.applied_geodesy.ui.table.ColumnType;
import org.applied_geodesy.ui.table.NaturalOrderComparator;
import org.applied_geodesy.util.CellValueType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class UIVarianceComponentTableBuilder extends UITableBuilder<VarianceComponentRow> {
	public enum VarianceComponentDisplayType {
		OVERALL_COMPONENTS,
		SELECTED_GROUP_COMPONENTS
	}

	private static UIVarianceComponentTableBuilder tableBuilder = new UIVarianceComponentTableBuilder();
	private VarianceComponentDisplayType type;
	private Map<VarianceComponentDisplayType, TableView<VarianceComponentRow>> tables = new HashMap<VarianceComponentDisplayType, TableView<VarianceComponentRow>>();
	private UIVarianceComponentTableBuilder() {
		super();
	}

	public static UIVarianceComponentTableBuilder getInstance() {
		return tableBuilder;
	}
	
	public TableView<VarianceComponentRow> getTable(VarianceComponentDisplayType varianceComponentDisplayType) {
		this.type = varianceComponentDisplayType;
		this.init();
		return this.table;
	}
	
	private TableContentType getTableContentType() {
		switch(this.type) {
		case SELECTED_GROUP_COMPONENTS:
			return TableContentType.SELECTED_GROUP_VARIANCE_COMPONENTS;
		default:
			return TableContentType.UNSPECIFIC;
		}
	}

	private void init() {
		if (this.tables.containsKey(this.type)) {
			this.table = this.tables.get(this.type);
			return;
		}

		TableColumn<VarianceComponentRow, VarianceComponentType> varianceComponentTypeColumn = null;
		TableColumn<VarianceComponentRow, String> stringColumn   = null;
		TableColumn<VarianceComponentRow, Boolean> booleanColumn = null;
		TableColumn<VarianceComponentRow, Double> doubleColumn   = null;
		TableColumn<VarianceComponentRow, Integer> integerColumn = null; 
		
		TableContentType tableContentType = this.getTableContentType();

		TableView<VarianceComponentRow> table = this.createTable();

		// Overall component type
		int columnIndex = table.getColumns().size(); 
		String labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.type.label", "Component");
		String tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.type.tooltip", "Type of estimated variance component");
		CellValueType cellValueType = CellValueType.STRING;
		ColumnContentType columnContentType = this.type == VarianceComponentDisplayType.OVERALL_COMPONENTS ? ColumnContentType.DEFAULT : ColumnContentType.VARIANCE_COMPONENT_TYPE;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		varianceComponentTypeColumn = this.<VarianceComponentType>getColumn(tableContentType, columnContentType, header, VarianceComponentRow::varianceComponentTypeProperty, getVarianceComponentTypeCallback(), this.type == VarianceComponentDisplayType.OVERALL_COMPONENTS ? ColumnType.VISIBLE : ColumnType.HIDDEN, columnIndex, false, this.type == VarianceComponentDisplayType.SELECTED_GROUP_COMPONENTS); 
		if (this.type == VarianceComponentDisplayType.OVERALL_COMPONENTS) {
			varianceComponentTypeColumn.setMinWidth(150);
		}
		varianceComponentTypeColumn.setComparator(new NaturalOrderComparator<VarianceComponentType>());
		table.getColumns().add(varianceComponentTypeColumn);
		
		// Selected component type
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.name.label", "Name");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.name.tooltip", "Name of variance component");
		cellValueType = CellValueType.STRING;
		columnContentType = this.type == VarianceComponentDisplayType.OVERALL_COMPONENTS ? ColumnContentType.DEFAULT : ColumnContentType.VARIANCE_COMPONENT_NAME;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(tableContentType, columnContentType, header, VarianceComponentRow::nameProperty, getStringCallback(), this.type == VarianceComponentDisplayType.SELECTED_GROUP_COMPONENTS ? ColumnType.VISIBLE : ColumnType.HIDDEN, columnIndex, false, this.type == VarianceComponentDisplayType.SELECTED_GROUP_COMPONENTS); 
		stringColumn.setComparator(new NaturalOrderComparator<String>());
		table.getColumns().add(stringColumn);

		// number of observations
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.number_of_observations.label", "n");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.number_of_observations.tooltip", "Number of observations");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		cellValueType = CellValueType.INTEGER;
		columnContentType = this.type == VarianceComponentDisplayType.OVERALL_COMPONENTS ? ColumnContentType.DEFAULT : ColumnContentType.NUMBER_OF_OBSERVATION;
		integerColumn = this.<Integer>getColumn(tableContentType, columnContentType, header, VarianceComponentRow::numberOfObservationsProperty, getIntegerCallback(), ColumnType.VISIBLE, columnIndex, false, this.type == VarianceComponentDisplayType.SELECTED_GROUP_COMPONENTS); 
		table.getColumns().add(integerColumn);

		// redundnacy
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.redundancy.label", "r");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.redundancy.tooltip", "Redundancy");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = this.type == VarianceComponentDisplayType.OVERALL_COMPONENTS ? ColumnContentType.DEFAULT : ColumnContentType.REDUNDANCY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VarianceComponentRow::redundancyProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false, this.type == VarianceComponentDisplayType.SELECTED_GROUP_COMPONENTS); 
		table.getColumns().add(doubleColumn);

		// Omega
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.omega.label", "\u03A9");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.omega.tooltip", "Squared weigthed residuals");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = this.type == VarianceComponentDisplayType.OVERALL_COMPONENTS ? ColumnContentType.DEFAULT : ColumnContentType.OMEGA;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VarianceComponentRow::omegaProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false, this.type == VarianceComponentDisplayType.SELECTED_GROUP_COMPONENTS); 
		table.getColumns().add(doubleColumn);

		// Sigma a-posteriori
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.variance.label", "1 : \u03C3\u00B2");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.variance.tooltip", "A-posteriori variance factor w.r.t. a-priori variance");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = this.type == VarianceComponentDisplayType.OVERALL_COMPONENTS ? ColumnContentType.DEFAULT : ColumnContentType.VARIANCE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VarianceComponentRow::sigma2aposterioriProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false, this.type == VarianceComponentDisplayType.SELECTED_GROUP_COMPONENTS); 
		table.getColumns().add(doubleColumn);

		// Decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexOutlier = columnIndex;
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.significant.label", "Significant");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.significant.tooltip", "Checked, if a-posteriori variance is significant w.r.t. a-priori variance");
		cellValueType = CellValueType.BOOLEAN;
		columnContentType = this.type == VarianceComponentDisplayType.OVERALL_COMPONENTS ? ColumnContentType.DEFAULT : ColumnContentType.SIGNIFICANT;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(tableContentType, columnContentType, header, VarianceComponentRow::significantProperty, getBooleanCallback(), this.type == VarianceComponentDisplayType.OVERALL_COMPONENTS ? ColumnType.VISIBLE : ColumnType.HIDDEN, columnIndex, false, this.type == VarianceComponentDisplayType.SELECTED_GROUP_COMPONENTS);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<VarianceComponentRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<VarianceComponentRow, Boolean> param) {
				final TableCellChangeListener<Boolean> significantChangeListener = new TableCellChangeListener<Boolean>(columnIndexOutlier, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isSignificant());
				booleanProp.addListener(significantChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);
		if (this.type == VarianceComponentDisplayType.SELECTED_GROUP_COMPONENTS)
			this.addColumnOrderSequenceListeners(tableContentType, table);
		
		if (this.type == VarianceComponentDisplayType.OVERALL_COMPONENTS)
			table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		this.tables.put(this.type, table);
		this.table = table;
	}

	private static Callback<TableColumn<VarianceComponentRow, VarianceComponentType>, TableCell<VarianceComponentRow, VarianceComponentType>> getVarianceComponentTypeCallback() {
		return new Callback<TableColumn<VarianceComponentRow, VarianceComponentType>, TableCell<VarianceComponentRow, VarianceComponentType>>() {
			@Override
			public TableCell<VarianceComponentRow, VarianceComponentType> call(TableColumn<VarianceComponentRow, VarianceComponentType> cell) {
				TableCell<VarianceComponentRow, VarianceComponentType> tableCell = new TextFieldTableCell<VarianceComponentRow, VarianceComponentType>(
						new StringConverter<VarianceComponentType>() {

							@Override
							public String toString(VarianceComponentType type) {
								if (type == null)
									return null;

								return getVarianceComponentTypeLabel(type);
							}

							@Override
							public VarianceComponentType fromString(String string) {
								return VarianceComponentType.valueOf(string);
							}
						});
				tableCell.setAlignment(Pos.CENTER_LEFT);
				return tableCell;	
			}
		};
	}
	
	public static final String getVarianceComponentTypeLabel(VarianceComponentType type) {
		switch (type) {
		case GLOBAL:
			return i18n.getString("UIVarianceComponentTableBuilder.type.global.label", "Global adjustment");
			
		case LEVELING_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.leveling.label", "Leveling");
		case LEVELING_ZERO_POINT_OFFSET_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.leveling.zero_point_offset.label", "Leveling \u03C3a");
		case LEVELING_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.leveling.square_root_distance_dependent.label", "Leveling \u03C3b");
		case LEVELING_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.leveling.distance_dependent.label", "Leveling \u03C3c");						
			
		case DIRECTION_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.direction.label", "Direction");
		case DIRECTION_ZERO_POINT_OFFSET_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.direction.zero_point_offset.label", "Direction \u03C3a");
		case DIRECTION_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.direction.square_root_distance_dependent.label", "Direction \u03C3b");
		case DIRECTION_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.direction.distance_dependent.label", "Direction \u03C3c");
		
		case HORIZONTAL_DISTANCE_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.horizontal_distance.label", "Horizontal distance");
		case HORIZONTAL_DISTANCE_ZERO_POINT_OFFSET_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.horizontal_distance.zero_point_offset.label", "Horizontal distance \u03C3a");
		case HORIZONTAL_DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.horizontal_distance.square_root_distance_dependent.label", "Horizontal distance \u03C3b");
		case HORIZONTAL_DISTANCE_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.horizontal_distance.distance_dependent.label", "Horizontal distance \u03C3c");
		
		case SLOPE_DISTANCE_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.slope_distance.label", "Slope distance");
		case SLOPE_DISTANCE_ZERO_POINT_OFFSET_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.slope_distance.zero_point_offset.label", "Slope distance \u03C3a");
		case SLOPE_DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.slope_distance.square_root_distance_dependent.label", "Slope distance \u03C3b");
		case SLOPE_DISTANCE_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.slope_distance.distance_dependent.label", "Slope distance \u03C3c");
			
		case ZENITH_ANGLE_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.zenith_angle.label", "Zenith angle");
		case ZENITH_ANGLE_ZERO_POINT_OFFSET_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.zenith_angle.zero_point_offset.label", "Zenith angle \u03C3a");
		case ZENITH_ANGLE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.zenith_angle.square_root_distance_dependent.label", "Zenith angle \u03C3b");
		case ZENITH_ANGLE_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.zenith_angle.distance_dependent.label", "Zenith angle \u03C3c");
			
		case GNSS1D_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.1d.label", "GNSS baseline 1D");
		case GNSS1D_ZERO_POINT_OFFSET_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.1d.zero_point_offset.label", "GNSS baseline 1D \u03C3a");
		case GNSS1D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.1d.square_root_distance_dependent.label", "GNSS baseline 1D \u03C3b");
		case GNSS1D_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.1d.distance_dependent.label", "GNSS baseline 1D \u03C3c");

		case GNSS2D_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.2d.label", "GNSS baseline 2D");
		case GNSS2D_ZERO_POINT_OFFSET_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.2d.zero_point_offset.label", "GNSS baseline 2D \u03C3a");
		case GNSS2D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.2d.square_root_distance_dependent.label", "GNSS baseline 2D \u03C3b");
		case GNSS2D_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.2d.distance_dependent.label", "GNSS baseline 2D \u03C3c");
			
		case GNSS3D_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.3d.label", "GNSS baseline 3D");
		case GNSS3D_ZERO_POINT_OFFSET_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.3d.zero_point_offset.label", "GNSS baseline 3D \u03C3a");
		case GNSS3D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.3d.square_root_distance_dependent.label", "GNSS baseline 3D \u03C3b");
		case GNSS3D_DISTANCE_DEPENDENT_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.3d.distance_dependent.label", "GNSS baseline 3D \u03C3c");

		case STOCHASTIC_POINT_1D_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.point.1d.label", "Stochastic point 1D");
		case STOCHASTIC_POINT_2D_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.point.2d.label", "Stochastic point 2D");
		case STOCHASTIC_POINT_3D_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.point.3d.label", "Stochastic point 3D");
			
		case STOCHASTIC_DEFLECTION_COMPONENT:
			return i18n.getString("UIVarianceComponentTableBuilder.type.deflection.label", "Deflection of the vertical");
		}
		return null;
	}

	@Override
	void setValue(VarianceComponentRow row, int columnIndex, Object oldValue, Object newValue) {}

	@Override
	public VarianceComponentRow getEmptyRow() {
		return new VarianceComponentRow();
	}	
}
