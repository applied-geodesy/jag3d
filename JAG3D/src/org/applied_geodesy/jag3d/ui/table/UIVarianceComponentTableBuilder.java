package org.applied_geodesy.jag3d.ui.table;

import org.applied_geodesy.adjustment.network.VarianceComponentType;
import org.applied_geodesy.jag3d.ui.table.row.VarianceComponentRow;

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

	private static UIVarianceComponentTableBuilder tableBuilder = new UIVarianceComponentTableBuilder();
	private boolean isInitialize = false;
	private UIVarianceComponentTableBuilder() {
		super();
	}

	public static UIVarianceComponentTableBuilder getInstance() {
		tableBuilder.init();
		return tableBuilder;
	}

	private void init() {
		if (this.isInitialize)
			return;
		
		double minColumnWidth = 50;
		double prefColumnWidth = 100;

		TableColumn<VarianceComponentRow, VarianceComponentType> varianceComponentTypeColumn = null;
		TableColumn<VarianceComponentRow, Boolean> booleanColumn = null;
		TableColumn<VarianceComponentRow, Double> doubleColumn   = null;
		TableColumn<VarianceComponentRow, Integer> integerColumn = null; 

		TableView<VarianceComponentRow> table = this.createTable();

		// Component type
		CellValueType cellValueType = CellValueType.STRING;
		int columnIndex = table.getColumns().size(); 
		String labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.type.label", "Parameter type");
		String tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.type.tooltip", "Type of the additional parameter");
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		varianceComponentTypeColumn = this.<VarianceComponentType>getColumn(header, VarianceComponentRow::varianceComponentTypeProperty, getVarianceComponentTypeCallback(), ColumnType.VISIBLE, columnIndex, false); 
		varianceComponentTypeColumn.setPrefWidth(175);
		varianceComponentTypeColumn.setMinWidth(150);
		varianceComponentTypeColumn.setMaxWidth(250);
		table.getColumns().add(varianceComponentTypeColumn);

		// number of observations
		cellValueType = CellValueType.INTEGER;
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.number_of_observations.label", "n");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.number_of_observations.tooltip", "Number of observations");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		integerColumn = this.<Integer>getColumn(header, VarianceComponentRow::numberOfObservationsProperty, getIntegerCallback(), ColumnType.VISIBLE, columnIndex, false); 
		integerColumn.setMinWidth(minColumnWidth);
		integerColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(integerColumn);

		// redundnacy
		cellValueType = CellValueType.STATISTIC;
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.redundancy.label", "r");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.redundancy.tooltip", "Redundancy");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, VarianceComponentRow::redundancyProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false); 
		doubleColumn.setMinWidth(minColumnWidth);
		doubleColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(doubleColumn);

		// Omega
		cellValueType = CellValueType.STATISTIC;
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.omega.label", "\u03A9");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.omega.tooltip", "Squared weigthed residuals");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, VarianceComponentRow::omegaProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false); 
		doubleColumn.setMinWidth(minColumnWidth);
		doubleColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(doubleColumn);

		// Sigma a-posteriori
		cellValueType = CellValueType.STATISTIC;
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.variance.label", "1 : \u03C3\u00B2");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.variance.tooltip", "A-posteriori variance factor w.r.t. a-priori variance");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, VarianceComponentRow::sigma2aposterioriProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false); 
		doubleColumn.setMinWidth(minColumnWidth);
		doubleColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(doubleColumn);

		// Decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexOutlier = columnIndex;
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.significant.label", "Significant");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.significant.tooltip", "Checked, if a-posteriori variance is significant w.r.t. a-priori variance");
		cellValueType = CellValueType.BOOLEAN;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, VarianceComponentRow::significantProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, false);
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

		this.table = table;
		this.isInitialize = true;
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
									return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.1d.label", "GNSS 1D baseline");
								case GNSS1D_ZERO_POINT_OFFSET_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.1d.zero_point_offset.label", "GNSS 1D baseline \u03C3a");
								case GNSS1D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.1d.square_root_distance_dependent.label", "GNSS 1D baseline \u03C3b");
								case GNSS1D_DISTANCE_DEPENDENT_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.1d.distance_dependent.label", "GNSS 1D baseline \u03C3c");

								case GNSS2D_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.2d.label", "GNSS 2D baseline");
								case GNSS2D_ZERO_POINT_OFFSET_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.2d.zero_point_offset.label", "GNSS 2D baseline \u03C3a");
								case GNSS2D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.2d.square_root_distance_dependent.label", "GNSS 2D baseline \u03C3b");
								case GNSS2D_DISTANCE_DEPENDENT_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.2d.distance_dependent.label", "GNSS 2D baseline \u03C3c");
									
								case GNSS3D_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.3d.label", "GNSS 3D baseline");
								case GNSS3D_ZERO_POINT_OFFSET_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.3d.zero_point_offset.label", "GNSS 3D baseline \u03C3a");
								case GNSS3D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.3d.square_root_distance_dependent.label", "GNSS 3D baseline \u03C3b");
								case GNSS3D_DISTANCE_DEPENDENT_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.gnss.3d.distance_dependent.label", "GNSS 3D baseline \u03C3c");

								case STOCHASTIC_POINT_1D_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.point.1d.label", "Stochastic point 1D");
								case STOCHASTIC_POINT_2D_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.point.1d.label", "Stochastic point 2D");
								case STOCHASTIC_POINT_3D_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.point.1d.label", "Stochastic point 3D");
								case STOCHASTIC_POINT_DEFLECTION_COMPONENT:
									return i18n.getString("UIVarianceComponentTableBuilder.type.point.1d.label", "Deflection of vertical");
								}
								return null;
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

	@Override
	void setValue(VarianceComponentRow row, int columnIndex, int rowIndex, Object oldValue, Object newValue) {}

	@Override
	public VarianceComponentRow getEmptyRow() {
		return new VarianceComponentRow();
	}	
}
