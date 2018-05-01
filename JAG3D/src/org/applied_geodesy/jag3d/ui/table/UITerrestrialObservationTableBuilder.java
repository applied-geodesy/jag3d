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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dnd.TerrestrialObservationRowDnD;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlight;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightRangeType;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightType;
import org.applied_geodesy.jag3d.ui.tree.EditableMenuCheckBoxTreeCell;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
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
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.leveling.tooltip", "A-priori height difference");
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
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.direction.tooltip", "A-priori direction");
			cellValueType = CellValueType.ANGLE;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.direction.label", "\u03C3t0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.direction.tooltip", "A-priori uncertainty of direction");
			cellValueType = CellValueType.ANGLE_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case HORIZONTAL_DISTANCE:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.horizontal_distance.label", "sh0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.horizontal_distance.tooltip", "A-priori horizontal distance");
			cellValueType = CellValueType.LENGTH;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.horizontal_distance.label", "\u03C3sh0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.horizontal_distance.tooltip", "A-priori uncertainty of horizontal distance");
			cellValueType = CellValueType.LENGTH_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case SLOPE_DISTANCE:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.slope_distance.label", "s0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.slope_distance.tooltip", "A-priori slope distance");
			cellValueType = CellValueType.LENGTH;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.slope_distance.label", "\u03C3s0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.slope_distance.tooltip", "A-priori uncertainty of slope distance");
			cellValueType = CellValueType.LENGTH_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case ZENITH_ANGLE:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.zenith_angle.label", "v0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value0.zenith_angle.tooltip", "A-priori zenith angle");
			cellValueType = CellValueType.ANGLE;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.zenith_angle.label", "\u03C3v0");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty0.zenith_angle.tooltip", "A-priori uncertainty of zenith angle");
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
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.leveling.tooltip", "A-posteriori height difference");
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
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.direction.tooltip", "A-posteriori direction");
			cellValueType = CellValueType.ANGLE;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.direction.label", "\u03C3t");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.direction.tooltip", "A-posteriori uncertainty of direction");
			cellValueType = CellValueType.ANGLE_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case HORIZONTAL_DISTANCE:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.horizontal_distance.label", "sh");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.horizontal_distance.tooltip", "A-posteriori horizontal distance");
			cellValueType = CellValueType.LENGTH;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.horizontal_distance.label", "\u03C3sh");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.horizontal_distance.tooltip", "A-posteriori uncertainty of horizontal distance");
			cellValueType = CellValueType.LENGTH_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case SLOPE_DISTANCE:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.slope_distance.label", "s");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.slope_distance.tooltip", "A-posteriori slope distance");
			cellValueType = CellValueType.LENGTH;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.slope_distance.label", "\u03C3s");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.slope_distance.tooltip", "A-posteriori uncertainty of slope distance");
			cellValueType = CellValueType.LENGTH_UNCERTAINTY;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::sigmaAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);

			break;
		case ZENITH_ANGLE:
			// Value
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.zenith_angle.label", "v");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.value.zenith_angle.tooltip", "A-posteriori zenith angle");
			cellValueType = CellValueType.ANGLE;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::valueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			table.getColumns().add(doubleColumn);

			// Uncertainty
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.zenith_angle.label", "\u03C3v");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.uncertainty.zenith_angle.tooltip", "A-posteriori uncertainty of zenith angle");
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
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.redundancy.tooltip", "Redundancy");
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
			// Residual
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.residual.label", "\u03B5");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.residual.tooltip", "Residual");
			cellValueType = CellValueType.LENGTH_RESIDUAL;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::residualProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);
						
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
			// Residual
			columnIndex = table.getColumns().size(); 
			labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.residual.label", "\u03B5");
			tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.residual.tooltip", "Residual");
			cellValueType = CellValueType.ANGLE_RESIDUAL;
			header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
			doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::residualProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
			doubleColumn.setComparator(new AbsoluteValueComparator());
			table.getColumns().add(doubleColumn);
						
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
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.influenceonposition.label", "EP");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.influenceonposition.tooltip", "Influence on point position due to an undetected gross-error");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::influenceOnPointPositionProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Influence on network distortion
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.influenceonnetworkdistortion.label", "EF\u00B7SPmax");
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.influenceonnetworkdistortion.tooltip", "Maximal influence on network distortion");
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
		labelText   = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.teststatistic.aposteriori.label", "Tpost");
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
		tooltipText = i18n.getString("UITerrestrialObservationTableBuilder.tableheader.testdecision.tooltip", "Checked, if null-hypothesis is rejected");
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
	void setValue(TerrestrialObservationRow rowData, int columnIndex, Object oldValue, Object newValue) {
		boolean valid = (oldValue == null || oldValue.toString().trim().isEmpty()) && (newValue == null || newValue.toString().trim().isEmpty());
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
			// Set observation group id, if not exists
			if (rowData.getGroupId() < 0)
				rowData.setGroupId(this.observationItemValue.getGroupId());
			
			try {
				SQLManager.getInstance().saveItem(rowData);
			} catch (Exception e) {
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
				valid = false;
				raiseErrorMessageSaveValue(e);
				e.printStackTrace();
			}
		}
//		this.table.refresh();
//		this.table.requestFocus();
//		this.table.getSelectionModel().clearSelection();
//		this.table.getSelectionModel().select(rowData);
		
		if (!valid) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					table.refresh();
					table.requestFocus();
					table.getSelectionModel().clearSelection();
					table.getSelectionModel().select(rowData);
				}
			});
		}
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
					SQLManager.getInstance().saveItem(clonedRow);
				} catch (Exception e) {
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
				} catch (Exception e) {
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
		} catch (Exception e) {
			raiseErrorMessage(type, e);
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			return;
		}
		
		try {
			int groupId = ((ObservationTreeItemValue)newTreeItem.getValue()).getGroupId();
			for (TerrestrialObservationRow row : selectedRows) {
				row.setGroupId(groupId);
				SQLManager.getInstance().saveItem(row);
			}
			
		} catch (Exception e) {
			raiseErrorMessage(type, e);
			e.printStackTrace();
			return;
		}
		UITreeBuilder.getInstance().getTree().getSelectionModel().select(newTreeItem);
	}
	
	public void export(File file, boolean aprioriValues) throws IOException {
		List<TerrestrialObservationRow> rows = this.table.getItems();

		String exportFormatString = "%15s \t";
		//String exportFormatDouble = "%+15.6f \t";
		String exportFormatDouble = "%20s \t";
		
		PrintWriter writer = null;

		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter( file )));

			for (TerrestrialObservationRow row : rows) {
				if (!row.isEnable())
					continue;

				String startPointName = row.getStartPointName();
				String endPointName   = row.getEndPointName();

				if (startPointName == null || startPointName.trim().isEmpty() || 
						endPointName == null || endPointName.trim().isEmpty())
					continue;

				Double value    = aprioriValues ? row.getValueApriori() : row.getValueAposteriori();
				Double distance = aprioriValues ? row.getDistanceApriori() : null;
				Double sigma    = aprioriValues ? row.getSigmaApriori() : row.getSigmaAposteriori();
				Double ih       = row.getInstrumentHeight();
				Double th       = row.getReflectorHeight();
				
				if (value == null || (!aprioriValues && sigma == null))
					continue;

				String observationValue, sigmaValue, distanceValue;
				
				String instrumentHeight = ih != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(ih, false)) : "";
				String reflectorHeight  = th != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(th, false)) : "";
				
				if (aprioriValues) {
					if (sigma != null && sigma <= 0)
						sigma = null;
					if (sigma != null && sigma > 0)
						distance = null;
					if (distance != null && distance <= 0)
						distance = null;
				}
				
				switch(this.type) {
				case DIRECTION:
				case ZENITH_ANGLE:
					observationValue = value != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toAngleFormat(value, false)) : "";
					sigmaValue       = sigma != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toAngleFormat(sigma, false)) : "";
					break;

				default:
					observationValue = value != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(value, false)) : "";
					sigmaValue       = sigma != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(sigma, false)) : "";
					break;

				}
				distanceValue = distance != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(distance, false)) : "";
				
				writer.println(
						String.format(exportFormatString, startPointName) +
						String.format(exportFormatString, endPointName) +
						instrumentHeight + reflectorHeight + observationValue + sigmaValue + distanceValue
				);

			}
		}
		finally {
			if (writer != null)
				writer.close();
		}
	}
	
	@Override
	void highlightTableRow(TableRow<TerrestrialObservationRow> row) {
		if (row == null)
			return;

		TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
		TableRowHighlightType tableRowHighlightType = tableRowHighlight.getTableRowHighlightType(); 
		double leftBoundary  = tableRowHighlight.getLeftBoundary(); 
		double rightBoundary = tableRowHighlight.getRightBoundary();
		
		TerrestrialObservationRow item = row.getItem();

		if (!row.isSelected() && item != null) {
			switch(tableRowHighlightType) {
			case TEST_STATISTIC:
				this.setTableRowHighlight(row, item.isSignificant() ? TableRowHighlightRangeType.INADEQUATE : TableRowHighlightRangeType.EXCELLENT);
				break;
				
			case REDUNDANCY:
				Double redundancy = item.getRedundancy();
				if (redundancy == null) 
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				else
					this.setTableRowHighlight(row, redundancy < leftBoundary ? TableRowHighlightRangeType.INADEQUATE : 
						redundancy <= rightBoundary ? TableRowHighlightRangeType.SATISFACTORY :
							TableRowHighlightRangeType.EXCELLENT);
				
				break;
				
			case INFLUENCE_ON_POSITION:
				Double influenceOnPosition = item.getInfluenceOnPointPosition();
				if (influenceOnPosition == null) 
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				else
					this.setTableRowHighlight(row, Math.abs(influenceOnPosition) < leftBoundary ? TableRowHighlightRangeType.EXCELLENT : 
						Math.abs(influenceOnPosition) <= rightBoundary ? TableRowHighlightRangeType.SATISFACTORY :
							TableRowHighlightRangeType.INADEQUATE);
				
				break;
				
			case P_PRIO_VALUE:
				Double pValue = item.getPValueApriori();
				if (pValue == null) 
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				else
					this.setTableRowHighlight(row, pValue < Math.log(leftBoundary / 100.0) ? TableRowHighlightRangeType.INADEQUATE : 
						pValue <= Math.log(rightBoundary / 100.0) ? TableRowHighlightRangeType.SATISFACTORY :
							TableRowHighlightRangeType.EXCELLENT);
				
				break;
				
			case NONE:
				this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				
				break;
			}

		} 
		else {
			this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
		}
	}
}
