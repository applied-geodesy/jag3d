package org.applied_geodesy.jag3d.ui.table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dnd.TerrestrialObservationRowDnD;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.tree.EditableMenuCheckBoxTreeCell;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;

public class UITerrestrialObservationTableBuilder extends UIEditableTableBuilder<TerrestrialObservationRow> {
	private ObservationTreeItemValue observationItemValue;
	private ObservationType type;
	private Map<ObservationType, TableView<TerrestrialObservationRow>> tables = new HashMap<ObservationType, TableView<TerrestrialObservationRow>>();

	private static UITerrestrialObservationTableBuilder tableBuilder = new UITerrestrialObservationTableBuilder();
	private UITerrestrialObservationTableBuilder() {
		super();
	}

	public static UITerrestrialObservationTableBuilder getInstance() {
		return tableBuilder;
	}

	public TableView<TerrestrialObservationRow> getTable(ObservationTreeItemValue observationItemValue) {
		this.observationItemValue = observationItemValue;
		this.type = observationItemValue.getObservationType();
		switch(type) {
		case LEVELING:
		case DIRECTION:
		case HORIZONTAL_DISTANCE:
		case SLOPE_DISTANCE:
		case ZENITH_ANGLE:
			this.init();
			return this.table;
		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, unsuported observation type " + type);
		}
	}

	private void init() {
		if (this.tables.containsKey(this.type)) {
			this.table = this.tables.get(this.type);
			return;
		}
		TableColumn<TerrestrialObservationRow, Boolean> booleanColumn = null;
		TableColumn<TerrestrialObservationRow, String> stringColumn   = null;
		TableColumn<TerrestrialObservationRow, Double> doubleColumn   = null; 

		TableView<TerrestrialObservationRow> table = this.createTable();
		///////////////// A-PRIORI VALUES /////////////////////////////

		// Enable/Disable
		int columnIndex = table.getColumns().size(); 
		final int columnIndexEnable = columnIndex;
		String labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.enable.label", "Enable");
		String tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.enable.tooltip", "State of the observation");
		CellValueType cellValueType = CellValueType.BOOLEAN;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, TerrestrialObservationRow::enableProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, true);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<TerrestrialObservationRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<TerrestrialObservationRow, Boolean> param) {
				final TableCellChangeListener<Boolean> enableChangeListener = new TableCellChangeListener<Boolean>(columnIndexEnable, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isEnable());
				booleanProp.addListener(enableChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		// Station-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.station.name.label", "Station-Id");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.station.name.tooltip", "Id of station");
		cellValueType = CellValueType.STRING;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, TerrestrialObservationRow::startPointNameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true); 
		table.getColumns().add(stringColumn);

		// Target-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.target.name.label", "Target-Id");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.target.name.tooltip", "Id of target point");
		cellValueType = CellValueType.STRING;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, TerrestrialObservationRow::endPointNameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true);
		table.getColumns().add(stringColumn);

		// Station height
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.station.height.label", "ih");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.station.height.tooltip", "Instrument height");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::instrumentHeightProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// Target height
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.target.height.label", "th");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.target.height.tooltip", "Target height");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::reflectorHeightProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// Terrestrial Observation a-priori and uncertainties
		switch (this.type) {
		case LEVELING:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.leveling.label", "\u03B4h0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.leveling.tooltip", "Height difference");
			cellValueType = CellValueType.LENGTH;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.leveling.label", "\u03C3h0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.leveling.tooltip", "A-priori uncertainty of height difference");
			cellValueType = CellValueType.LENGTH_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case DIRECTION:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.direction.label", "t0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.direction.tooltip", "Direction");
			cellValueType = CellValueType.ANGLE;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.slopedistance.label", "\u03C3t0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.slopedistance.tooltip", "A-priori uncertainty of direction");
			cellValueType = CellValueType.ANGLE_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case HORIZONTAL_DISTANCE:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.horizontaldistance.label", "sh0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.horizontaldistance.tooltip", "Horizontal distance");
			cellValueType = CellValueType.LENGTH;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.horizontaldistance.label", "\u03C3sh0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.horizontaldistance.tooltip", "A-priori uncertainty of horizontal distance");
			cellValueType = CellValueType.LENGTH_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case SLOPE_DISTANCE:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.slopedistance.label", "s0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.slopedistance.tooltip", "Slope distance");
			cellValueType = CellValueType.LENGTH;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.slopedistance.label", "\u03C3s0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.slopedistance.tooltip", "A-priori uncertainty of slope distance");
			cellValueType = CellValueType.LENGTH_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case ZENITH_ANGLE:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.zenith.label", "v0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.zenith.tooltip", "Zenith angle");
			cellValueType = CellValueType.ANGLE;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.slopedistance.label", "\u03C3v0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.slopedistance.tooltip", "A-priori uncertainty of zenith angle");
			cellValueType = CellValueType.ANGLE_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		default:
			System.err.println(this.getClass().getSimpleName() + " : Unsupported observation type " + type);
			break;
		}

		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.distance.label", "d0");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.distance.tooltip", "Length approximation for distance dependent uncertainty calculation");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::distanceAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
		table.getColumns().add(doubleColumn);

		//////////////////// ADJUSTED VALUES //////////////////////////

		// Terrestrial Observation a-posteriori and uncertainties; Gross error and minimal detectable bias
		switch (type) {
		case LEVELING:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.leveling.label", "\u03B4h");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.leveling.tooltip", "Height difference");
			cellValueType = CellValueType.LENGTH;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.leveling.label", "\u03C3h");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.leveling.tooltip", "A-posteriori uncertainty of height difference");
			cellValueType = CellValueType.LENGTH_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case DIRECTION:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.direction.label", "t");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.direction.tooltip", "Direction");
			cellValueType = CellValueType.ANGLE;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.slopedistance.label", "\u03C3t");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.slopedistance.tooltip", "A-posteriori uncertainty of direction");
			cellValueType = CellValueType.ANGLE_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case HORIZONTAL_DISTANCE:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.horizontaldistance.label", "sh");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.horizontaldistance.tooltip", "Horizontal distance");
			cellValueType = CellValueType.LENGTH;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.horizontaldistance.label", "\u03C3sh");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.horizontaldistance.tooltip", "A-posteriori uncertainty of horizontal distance");
			cellValueType = CellValueType.LENGTH_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case SLOPE_DISTANCE:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.slopedistance.label", "s");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.slopedistance.tooltip", "Slope distance");
			cellValueType = CellValueType.LENGTH;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.slopedistance.label", "\u03C3s");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.slopedistance.tooltip", "A-posteriori uncertainty of slope distance");
			cellValueType = CellValueType.LENGTH_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case ZENITH_ANGLE:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.zenith.label", "v");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.zenith.tooltip", "Zenith angle");
			cellValueType = CellValueType.ANGLE;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.slopedistance.label", "\u03C3v");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.slopedistance.tooltip", "A-posteriori uncertainty of zenith angle");
			cellValueType = CellValueType.ANGLE_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		default:
			System.err.println(this.getClass().getSimpleName() + " : Unsupported observation type " + type);
			break;
		}

		// Redundance
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.redundancy.label", "r");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.redundancy.tooltip", "Part of redundancy");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::redundancyProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Estimated gross error and minimal detectable bias
		switch (type) {
		case LEVELING:
		case HORIZONTAL_DISTANCE:
		case SLOPE_DISTANCE:
			// Gross error
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.grosserror.label", "\u2207");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.grosserror.tooltip", "Estimated gross error");
			cellValueType = CellValueType.LENGTH_RESIDUAL;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::grossErrorProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			// Minimal detectable bias
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.minimaldetectablebias.label", "\u2207(\u03b1,\u03b2)");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.minimaldetectablebias.tooltip", "Minimal detectable bias");
			cellValueType = CellValueType.LENGTH_RESIDUAL;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::minimalDetectableBiasProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case DIRECTION:
		case ZENITH_ANGLE:
			// Gross error
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.grosserror.label", "\u2207");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.grosserror.tooltip", "Estimated gross error");
			cellValueType = CellValueType.ANGLE_RESIDUAL;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::grossErrorProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			// Minimal detectable bias
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.minimaldetectablebias.label", "\u2207(\u03b1,\u03b2)");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.minimaldetectablebias.tooltip", "Minimal detectable bias");
			cellValueType = CellValueType.ANGLE_RESIDUAL;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::minimalDetectableBiasProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		default:
			System.err.println(this.getClass().getSimpleName() + " : Unsupported observation type " + type);
			break;
		}

		// Influence on point position
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.ep.label", "EP");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.ep.tooltip", "Influence on point position due to an undetected gross-error");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::influenceOnPointPositionProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Influence on network distortion
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.efsp.label", "EF\u00B7SPmax");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.efsp.tooltip", "Maximal influence on network distortion");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::influenceOnNetworkDistortionProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Omega
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.omega.label", "\u03A9");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.omega.tooltip", "Weighted squares of residual");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::omegaProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// A-priori log(p)
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.pvalue.apriori.label", "log(Pprio)");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.pvalue.apriori.tooltip", "A-priori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::pValueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// A-priori log(p)
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.pvalue.aposteriori.label", "log(Ppost)");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.pvalue.aposteriori.tooltip", "A-posteriori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::pValueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// A-priori test statistic
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.teststatistic.apriori.label", "Tprio");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.teststatistic.apriori.tooltip", "A-priori test statistic");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::testStatisticAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// A-posteriori test statistic
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.teststatistic.aposteriori.label", "Tprio");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.teststatistic.aposteriori.tooltip", "A-posteriori test statistic");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::testStatisticAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexOutlier = columnIndex;
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.testdecision.label", "Significant");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.testdecision.tooltip", "Decision of test statistic");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, TerrestrialObservationRow::significantProperty, getBooleanCallback(), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<TerrestrialObservationRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<TerrestrialObservationRow, Boolean> param) {
				final TableCellChangeListener<Boolean> significantChangeListener = new TableCellChangeListener<Boolean>(columnIndexOutlier, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isSignificant());
				booleanProp.addListener(significantChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);
		
		this.addContextMenu(table, this.createContextMenu(false));
		this.addDynamicRowAdder(table);

		this.tables.put(this.type, table);
		this.table = table;
	}

	@Override
	void setValue(TerrestrialObservationRow rowData, int columnIndex, int rowIndex, Object oldValue, Object newValue) {
		boolean valid = false;
		switch (columnIndex) {
		case 0:
			rowData.setEnable(newValue != null && newValue instanceof Boolean && (Boolean)newValue);
			valid = true;
			break;
		case 1:
			if (newValue != null && !newValue.toString().trim().isEmpty() && ((rowData.getEndPointName() == null || !rowData.getEndPointName().equals(newValue.toString().trim())))) {
				rowData.setStartPointName(newValue.toString().trim());
				valid = true;
			}
			else
				rowData.setStartPointName(oldValue == null ? null : oldValue.toString().trim());
			break;
		case 2:
			if (newValue != null && !newValue.toString().trim().isEmpty() && ((rowData.getStartPointName() == null || !rowData.getStartPointName().equals(newValue.toString().trim())))) {
				rowData.setEndPointName(newValue.toString().trim());
				valid = true;
			}
			else
				rowData.setStartPointName(oldValue == null ? null : oldValue.toString().trim());
			break;
		case 3:
			if (newValue != null && newValue instanceof Double) {
				rowData.setInstrumentHeight((Double)newValue);
				valid = true;
			}
			else
				rowData.setInstrumentHeight(0.0);
			break;
		case 4:
			if (newValue != null && newValue instanceof Double) {
				rowData.setReflectorHeight((Double)newValue);
				valid = true;
			}
			else
				rowData.setInstrumentHeight(0.0);
			break;
		case 5:
			if (newValue != null && newValue instanceof Double) {
				rowData.setValueApriori((Double)newValue);
				valid = true;
			}
			else
				rowData.setValueApriori(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 6:
			rowData.setSigmaApriori(newValue == null ? null : newValue instanceof Double && (Double)newValue > 0 ? (Double)newValue : null);
			valid = true;
			break;
		case 7:
			rowData.setDistanceApriori(newValue == null ? null : newValue instanceof Double && (Double)newValue > 0 ? (Double)newValue : null);
			valid = true;
			break;
		default:
			System.err.println(this.getClass().getSimpleName() + " : Editable column exceed " + columnIndex);
			valid = false;
			break;
		}

		if (valid && this.isComplete(rowData)) {
			try {
				SQLManager.getInstance().saveItem(this.observationItemValue.getGroupId(), rowData);
			} catch (SQLException e) {
				
				switch (columnIndex) {
				case 1:
					rowData.setStartPointName(oldValue == null ? null : oldValue.toString().trim());
					break;
				case 2:
					rowData.setEndPointName(oldValue == null ? null : oldValue.toString().trim());
					break;
				default:
					break;
				}
				raiseErrorMessageSaveValue(e);
				e.printStackTrace();
			}
		}
		table.refresh();
	}
	
	private boolean isComplete(TerrestrialObservationRow row) {
		return row.getStartPointName() != null && !row.getStartPointName().trim().isEmpty() &&
				row.getEndPointName() != null  && !row.getEndPointName().trim().isEmpty() &&
				!row.getStartPointName().equals(row.getEndPointName()) &&
				row.getInstrumentHeight() != null &&
				row.getReflectorHeight() != null &&
				row.getValueApriori() != null;
	}
	
	@Override
	public TerrestrialObservationRow getEmptyRow() {
		return new TerrestrialObservationRow();
	}
	
	@Override
	void enableDragSupport() {
		this.table.setOnDragDetected(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent event) {
		    	List<TerrestrialObservationRow> selectedRows = new ArrayList<TerrestrialObservationRow>(table.getSelectionModel().getSelectedItems());
		    	if (selectedRows != null && !selectedRows.isEmpty()) {

		    		List<TerrestrialObservationRowDnD> rowsDnD = new ArrayList<TerrestrialObservationRowDnD>(selectedRows.size());
		    		for (TerrestrialObservationRow selectedRow : selectedRows) {
		    			TerrestrialObservationRowDnD rowDnD = null;
		    			if (isComplete(selectedRow) && (rowDnD = TerrestrialObservationRowDnD.fromTerrestrialObservationRow(selectedRow)) != null) {
		    				rowsDnD.add(rowDnD);
		    			}
		    		}

		    		if (!rowsDnD.isEmpty()) {
		    			Dragboard db = table.startDragAndDrop(TransferMode.MOVE);
		    			ClipboardContent content = new ClipboardContent();
		    			content.put(EditableMenuCheckBoxTreeCell.TREE_ITEM_TYPE_DATA_FORMAT, observationItemValue.getItemType());
		    			content.put(EditableMenuCheckBoxTreeCell.GROUP_ID_DATA_FORMAT,       observationItemValue.getGroupId());
		    			content.put(EditableMenuCheckBoxTreeCell.DIMENSION_DATA_FORMAT,      observationItemValue.getDimension());
		    			content.put(EditableMenuCheckBoxTreeCell.TERRESTRIAL_OBSERVATION_ROWS_DATA_FORMAT, rowsDnD);

		    			db.setContent(content);
		    		}
		    	}
		        event.consume();
		    }
		});
	}
	
	@Override
	void duplicateRows() {
		List<TerrestrialObservationRow> selectedRows = new ArrayList<TerrestrialObservationRow>(this.table.getSelectionModel().getSelectedItems());
		if (selectedRows == null || selectedRows.isEmpty())
			return;

		List<TerrestrialObservationRow> clonedRows = new ArrayList<TerrestrialObservationRow>(selectedRows.size());
		for (TerrestrialObservationRow row : selectedRows) {
			TerrestrialObservationRow clonedRow = TerrestrialObservationRow.cloneRowApriori(row);
			if (this.isComplete(clonedRow)) {
				try {
					SQLManager.getInstance().saveItem(this.observationItemValue.getGroupId(), clonedRow);
				} catch (SQLException e) {
					raiseErrorMessage(ContextMenuType.DUPLICATE, e);
					e.printStackTrace();
					break;
				}
			}
			clonedRows.add(clonedRow);
		}

		if (clonedRows != null && !clonedRows.isEmpty()) {
			this.table.getItems().addAll(clonedRows);
			this.table.getSelectionModel().clearSelection();
			for (TerrestrialObservationRow clonedRow : clonedRows)
				this.table.getSelectionModel().select(clonedRow);
			this.table.scrollTo(clonedRows.get(0));
		}
	}
	
	@Override
	void removeRows() {
		List<TerrestrialObservationRow> selectedRows = new ArrayList<TerrestrialObservationRow>(this.table.getSelectionModel().getSelectedItems());
		if (selectedRows == null || selectedRows.isEmpty())
			return;
		
		List<TerrestrialObservationRow> removedRows = new ArrayList<TerrestrialObservationRow>(selectedRows.size());
		for (TerrestrialObservationRow row : selectedRows) {
			if (this.isComplete(row)) {
				try {
					SQLManager.getInstance().remove(row);
				} catch (SQLException e) {
					raiseErrorMessage(ContextMenuType.REMOVE, e);
					e.printStackTrace();
					break;
				}
			}
			removedRows.add(row);
		}
		if (removedRows != null && !removedRows.isEmpty()) {
			this.table.getSelectionModel().clearSelection();
			this.table.getItems().removeAll(removedRows);
			if (this.table.getItems().isEmpty())
				this.table.getItems().setAll(getEmptyRow());
		}
	}
	
	@Override
	void moveRows(ContextMenuType type) {
		if (type != ContextMenuType.MOVETO)
			return;

		List<TerrestrialObservationRow> selectedRows = new ArrayList<TerrestrialObservationRow>(this.table.getSelectionModel().getSelectedItems());

		if (selectedRows == null || selectedRows.isEmpty())
			return;

		TreeItemType parentType = TreeItemType.getDirectoryByLeafType(this.observationItemValue.getItemType());
		TreeItem<TreeItemValue> newTreeItem = UITreeBuilder.getInstance().addItem(parentType, false);

		try {
			SQLManager.getInstance().saveGroup((ObservationTreeItemValue)newTreeItem.getValue());
		} catch (SQLException e) {
			raiseErrorMessage(type, e);
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			return;
		}
		
		try {
			int groupId = ((ObservationTreeItemValue)newTreeItem.getValue()).getGroupId();
			for (TerrestrialObservationRow row : selectedRows) {
				SQLManager.getInstance().saveItem(groupId, row);
			}
			
		} catch (SQLException e) {
			raiseErrorMessage(type, e);
			e.printStackTrace();
			return;
		}
		UITreeBuilder.getInstance().getTree().getSelectionModel().select(newTreeItem);
	}
}
