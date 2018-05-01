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
import org.applied_geodesy.jag3d.ui.dnd.GNSSObservationRowDnD;
import org.applied_geodesy.jag3d.ui.table.row.GNSSObservationRow;
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
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

public class UIGNSSObservationTableBuilder extends UIEditableTableBuilder<GNSSObservationRow> {
	private ObservationTreeItemValue observationItemValue;
	private ObservationType type;
	private Map<ObservationType, TableView<GNSSObservationRow>> tables = new HashMap<ObservationType, TableView<GNSSObservationRow>>();

	private static UIGNSSObservationTableBuilder tableBuilder = new UIGNSSObservationTableBuilder();
	private UIGNSSObservationTableBuilder() {
		super();
	}

	public static UIGNSSObservationTableBuilder getInstance() {
		return tableBuilder;
	}

	public TableView<GNSSObservationRow> getTable(ObservationTreeItemValue observationItemValue) {
		this.observationItemValue = observationItemValue;
		this.type = observationItemValue.getObservationType();
		switch(type) {
		case GNSS1D:
		case GNSS2D:
		case GNSS3D:
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
		TableColumn<GNSSObservationRow, Boolean> booleanColumn = null;
		TableColumn<GNSSObservationRow, String> stringColumn   = null;
		TableColumn<GNSSObservationRow, Double> doubleColumn   = null; 
		
		TableView<GNSSObservationRow> table = this.createTable();
		///////////////// A-PRIORI VALUES /////////////////////////////

		// Enable/Disable
		int columnIndex = table.getColumns().size(); 
		final int columnIndexEnable = columnIndex;
		String labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.enable.label", "Enable");
		String tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.enable.tooltip", "State of the baseline");
		CellValueType cellValueType = CellValueType.BOOLEAN;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, GNSSObservationRow::enableProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, true);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<GNSSObservationRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<GNSSObservationRow, Boolean> param) {
				final TableCellChangeListener<Boolean> enableChangeListener = new TableCellChangeListener<Boolean>(columnIndexEnable, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isEnable());
				booleanProp.addListener(enableChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		// Station-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.station.name.label", "Station-Id");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.station.name.tooltip", "Id of station");
		cellValueType = CellValueType.STRING;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, GNSSObservationRow::startPointNameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true); 
		table.getColumns().add(stringColumn);

		// Target-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.target.name.label", "Target-Id");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.target.name.tooltip", "Id of target point");
		cellValueType = CellValueType.STRING;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, GNSSObservationRow::endPointNameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true);
		table.getColumns().add(stringColumn);
		
		
		// A-priori Components
		// Y0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.y0.label", "y0");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.y0.tooltip", "A-priori y-component of the baseline");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::yAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_GNSS_OBSERVATION, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// X0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.x0.label", "x0");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.x0.tooltip", "A-priori x-component of the baseline");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::xAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_GNSS_OBSERVATION, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// Z0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.z0.label", "z0");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.z0.tooltip", "A-priori z-component of the baseline");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::zAprioriProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS2D ? ColumnType.APRIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, true);
		table.getColumns().add(doubleColumn);
		
		// A-priori Uncertainties
		// Y0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.sigma.y0.label", "\u03C3y0");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.sigma.y0.tooltip", "A-priori uncertainty of y-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::sigmaYaprioriProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APRIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// X0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.sigma.x0.label", "\u03C3x0");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.sigma.x0.tooltip", "A-priori uncertainty of x-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::sigmaXaprioriProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APRIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Z0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.sigma.z0.label", "\u03C3z0");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.sigma.z0.tooltip", "A-priori uncertainty of z-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::sigmaZaprioriProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS2D ? ColumnType.APRIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		///////////////// A-POSTERIORI VALUES /////////////////////////////
		// A-posteriori Components

		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.y.label", "y");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.y.tooltip", "A-posteriori y-component of the baseline");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::yAposterioriProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.x.label", "x");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.x.tooltip", "A-posteriori x-component of the baseline");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::xAposterioriProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.z.label", "z");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.z.tooltip", "A-posteriori z-component of the baseline");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::zAposterioriProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS2D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);


		// A-posteriori Uncertainties
		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.sigma.y.label", "\u03C3y");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.sigma.y.tooltip", "A-posteriori uncertainty of y-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::sigmaYaposterioriProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.sigma.x.label", "\u03C3x");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.sigma.x.tooltip", "A-posteriori uncertainty of x-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::sigmaXaposterioriProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.sigma.z.label", "\u03C3z");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.sigma.z.tooltip", "A-posteriori uncertainty of z-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::sigmaZaposterioriProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS2D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Redundancy
		// ry
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.redundancy.y.label", "ry");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.redundancy.y.tooltip", "Redundancy of y-component");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::redundancyYProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// rx
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.redundancy.x.label", "rx");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.redundancy.x.tooltip", "Redundancy of x-component");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::redundancyXProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// rz
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.redundancy.z.label", "rz");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.redundancy.z.tooltip", "Redundancy of z-component");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::redundancyZProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS2D ?ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// Residual
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.residual.y.label", "\u03B5y");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.residual.y.tooltip", "Residual of y-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::residualYProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.residual.x.label", "\u03B5x");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.residual.x.tooltip", "Residual of x-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::residualXProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.residual.z.label", "\u03B5z");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.residual.z.tooltip", "Residual of z-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::residualZProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS2D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Gross-Error
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.grosserror.y.label", "\u2207y");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.grosserror.y.tooltip", "Gross-error in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::grossErrorYProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.grosserror.x.label", "\u2207x");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.grosserror.x.tooltip", "Gross-error in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::grossErrorXProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.grosserror.z.label", "\u2207z");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.grosserror.z.tooltip", "Gross-error in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::grossErrorZProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS2D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// MDB
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.minimaldetectablebias.y.label", "\u2207y(\u03B1,\u03B2)");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.minimaldetectablebias.y.tooltip", "Minimal detectable bias in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::minimalDetectableBiasYProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.minimaldetectablebias.x.label", "\u2207x(\u03B1,\u03B2)");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.minimaldetectablebias.x.tooltip", "Minimal detectable bias in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::minimalDetectableBiasXProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.minimaldetectablebias.z.label", "\u2207z(\u03B1,\u03B2)");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.minimaldetectablebias.z.tooltip", "Minimal detectable bias in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::minimalDetectableBiasZProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS2D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Influence on point position (EP)
		// EP-y
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.influenceonposition.y.label", "EPy");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.influenceonposition.y.tooltip", "Influence on point position due to an undetected gross-error in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::influenceOnPointPositionYProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// EP-x
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.influenceonposition.x.label", "EPx");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.influenceonposition.x.tooltip", "Influence on point position due to an undetected gross-error in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::influenceOnPointPositionXProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS1D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// EP-z
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.influenceonposition.z.label", "EPz");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.influenceonposition.z.tooltip", "Influence on point position due to an undetected gross-error in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::influenceOnPointPositionZProperty, getDoubleCallback(cellValueType), this.type != ObservationType.GNSS2D ? ColumnType.APOSTERIORI_GNSS_OBSERVATION : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// EFSP
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.influenceonnetworkdistortion.label", "EF\u00B7SPmax");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.influenceonnetworkdistortion.tooltip", "Maximal influence on network distortion");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::influenceOnNetworkDistortionProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_GNSS_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// vTPv
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.omega.label", "\u03A9");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.omega.tooltip", "Weighted squares of residual");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::omegaProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_GNSS_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);	

		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.pvalue.apriori.label", "log(Pprio)");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.pvalue.apriori.tooltip", "A-priori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::pValueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_GNSS_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.pvalue.aposteriori.label", "log(Ppost)");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.pvalue.aposteriori.tooltip", "A-posteriori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::pValueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_GNSS_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Tprio
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.teststatistic.apriori.label", "Tprio");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.teststatistic.apriori.tooltip", "A-priori test statistic");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::testStatisticAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_GNSS_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Tpost
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.teststatistic.aposteriori.label", "Tpost");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.teststatistic.aposteriori.tooltip", "A-posteriori test statistic");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, GNSSObservationRow::testStatisticAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_GNSS_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexOutlier = columnIndex;
		labelText   = i18n.getString("UIGNSSObservationTableBuilder.tableheader.testdecision.label", "Significant");
		tooltipText = i18n.getString("UIGNSSObservationTableBuilder.tableheader.testdecision.tooltip", "Checked, if null-hypothesis is rejected");
		cellValueType = CellValueType.BOOLEAN;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, GNSSObservationRow::significantProperty, getBooleanCallback(), ColumnType.APOSTERIORI_GNSS_OBSERVATION, columnIndex, false);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<GNSSObservationRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<GNSSObservationRow, Boolean> param) {
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
	void setValue(GNSSObservationRow rowData, int columnIndex, Object oldValue, Object newValue) {
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
				rowData.setEndPointName(oldValue == null ? null : oldValue.toString().trim());
			break;
		case 3:
			if (newValue != null && newValue instanceof Double) {
				rowData.setYApriori((Double)newValue);	
				valid = true;
			}
			else
				rowData.setYApriori(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 4:
			if (newValue != null && newValue instanceof Double) {
				rowData.setXApriori((Double)newValue);	
				valid = true;
			}
			else
				rowData.setXApriori(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 5:
			if (newValue != null && newValue instanceof Double) {
				rowData.setZApriori((Double)newValue);	
				valid = true;
			}
			else
				rowData.setZApriori(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 6:
			rowData.setSigmaYapriori(newValue == null ? null : newValue instanceof Double && (Double)newValue > 0 ? (Double)newValue : null);
			valid = true;
			break;
		case 7:
			rowData.setSigmaXapriori(newValue == null ? null : newValue instanceof Double && (Double)newValue > 0 ? (Double)newValue : null);
			valid = true;
			break;
		case 8:
			rowData.setSigmaZapriori(newValue == null ? null : newValue instanceof Double && (Double)newValue > 0 ? (Double)newValue : null);
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

	@Override
	public GNSSObservationRow getEmptyRow() {
		return new GNSSObservationRow();
	}
	
	private boolean isComplete(GNSSObservationRow row) {
		return row.getStartPointName() != null && !row.getStartPointName().trim().isEmpty() &&
				row.getEndPointName() != null  && !row.getEndPointName().trim().isEmpty() &&
				!row.getStartPointName().equals(row.getEndPointName()) &&
				row.getXApriori() != null &&
				row.getYApriori() != null &&
				row.getZApriori() != null;
	}

	@Override
	void enableDragSupport() {
		this.table.setOnDragDetected(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent event) {
		    	List<GNSSObservationRow> selectedRows = new ArrayList<GNSSObservationRow>(table.getSelectionModel().getSelectedItems());
		    	if (selectedRows != null && !selectedRows.isEmpty()) {

		    		List<GNSSObservationRowDnD> rowsDnD = new ArrayList<GNSSObservationRowDnD>(selectedRows.size());
		    		for (GNSSObservationRow selectedRow : selectedRows) {
		    			GNSSObservationRowDnD rowDnD = null;
		    			if (isComplete(selectedRow) && (rowDnD = GNSSObservationRowDnD.fromGNSSObservationRow(selectedRow)) != null) {
		    				rowsDnD.add(rowDnD);
		    			}
		    		}

		    		if (!rowsDnD.isEmpty()) {
		    			Dragboard db = table.startDragAndDrop(TransferMode.MOVE);
		    			ClipboardContent content = new ClipboardContent();
		    			content.put(EditableMenuCheckBoxTreeCell.TREE_ITEM_TYPE_DATA_FORMAT, observationItemValue.getItemType());
		    			content.put(EditableMenuCheckBoxTreeCell.GROUP_ID_DATA_FORMAT,       observationItemValue.getGroupId());
		    			content.put(EditableMenuCheckBoxTreeCell.DIMENSION_DATA_FORMAT,      observationItemValue.getDimension());
		    			content.put(EditableMenuCheckBoxTreeCell.GNSS_OBSERVATION_ROWS_DATA_FORMAT, rowsDnD);

		    			db.setContent(content);
		    		}
		    	}
		        event.consume();
		    }
		});
	}
	
	@Override
	void duplicateRows() {
		List<GNSSObservationRow> selectedRows = new ArrayList<GNSSObservationRow>(this.table.getSelectionModel().getSelectedItems());
		if (selectedRows == null || selectedRows.isEmpty())
			return;

		List<GNSSObservationRow> clonedRows = new ArrayList<GNSSObservationRow>(selectedRows.size());
		for (GNSSObservationRow row : selectedRows) {
			GNSSObservationRow clonedRow = GNSSObservationRow.cloneRowApriori(row);
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
			for (GNSSObservationRow clonedRow : clonedRows)
				this.table.getSelectionModel().select(clonedRow);
			this.table.scrollTo(clonedRows.get(0));
		}
	}
	
	@Override
	void removeRows() {
		List<GNSSObservationRow> selectedRows = new ArrayList<GNSSObservationRow>(this.table.getSelectionModel().getSelectedItems());
		if (selectedRows == null || selectedRows.isEmpty())
			return;
		
		List<GNSSObservationRow> removedRows = new ArrayList<GNSSObservationRow>(selectedRows.size());
		for (GNSSObservationRow row : selectedRows) {
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
		
		List<GNSSObservationRow> selectedRows = new ArrayList<GNSSObservationRow>(this.table.getSelectionModel().getSelectedItems());
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
			for (GNSSObservationRow row : selectedRows) {
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
		List<GNSSObservationRow> rows = this.table.getItems();
		
		String exportFormatString = "%15s \t";
		//String exportFormatDouble = "%+15.6f \t";
		String exportFormatDouble = "%20s \t";
		
		PrintWriter writer = null;

		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter( file )));

			for (GNSSObservationRow row : rows) {
				if (!row.isEnable())
					continue;

				String startPointName = row.getStartPointName();
				String endPointName   = row.getEndPointName();

				if (startPointName == null || startPointName.trim().isEmpty() || 
						endPointName == null || endPointName.trim().isEmpty())
					continue;

				Double y = aprioriValues ? row.getYApriori() : row.getYAposteriori();
				Double x = aprioriValues ? row.getXApriori() : row.getXAposteriori();
				Double z = aprioriValues ? row.getZApriori() : row.getZAposteriori();

				if (this.type != ObservationType.GNSS2D && z == null)
					continue;
				
				if (this.type != ObservationType.GNSS1D && (y == null || x == null))
					continue;

				Double sigmaY = aprioriValues ? row.getSigmaYapriori() : row.getSigmaYaposteriori();
				Double sigmaX = aprioriValues ? row.getSigmaXapriori() : row.getSigmaXaposteriori();
				Double sigmaZ = aprioriValues ? row.getSigmaZapriori() : row.getSigmaZaposteriori();

				if (!aprioriValues && (sigmaY == null || sigmaX == null || sigmaZ == null))
					continue;
				
				if (aprioriValues) {
					if (sigmaY != null && sigmaY <= 0)
						sigmaY = null;
					if (sigmaX != null && sigmaX <= 0)
						sigmaX = null;
					if (sigmaZ != null && sigmaZ <= 0)
						sigmaZ = null;
				}
				
				String yValue = this.type != ObservationType.GNSS1D && y != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(y, false)) : "";
				String xValue = this.type != ObservationType.GNSS1D && x != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(x, false)) : "";
				String zValue = this.type != ObservationType.GNSS2D && z != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(z, false)) : "";

				String sigmaYvalue = this.type != ObservationType.GNSS1D && sigmaY != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(sigmaY, false)) : "";
				String sigmaXvalue = this.type != ObservationType.GNSS1D && sigmaX != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(sigmaX, false)) : "";
				String sigmaZvalue = this.type != ObservationType.GNSS2D && sigmaZ != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(sigmaZ, false)) : "";

				writer.println(
						String.format(exportFormatString, startPointName) +
						String.format(exportFormatString, endPointName) +
						yValue + xValue + zValue + sigmaYvalue + sigmaXvalue + sigmaZvalue
						);
			}
		}
		finally {
			if (writer != null)
				writer.close();
		}
	}
	
	@Override
	void highlightTableRow(TableRow<GNSSObservationRow> row) {
		if (row == null)
			return;
		
		TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
		TableRowHighlightType tableRowHighlightType = tableRowHighlight.getTableRowHighlightType(); 
		double leftBoundary  = tableRowHighlight.getLeftBoundary(); 
		double rightBoundary = tableRowHighlight.getRightBoundary();

		GNSSObservationRow item = row.getItem();

		if (!row.isSelected() && item != null) {
			switch(tableRowHighlightType) {
			case TEST_STATISTIC:
				this.setTableRowHighlight(row, item.isSignificant() ? TableRowHighlightRangeType.INADEQUATE : TableRowHighlightRangeType.EXCELLENT);
				break;
				
			case REDUNDANCY:
				Double redundancyX = item.getRedundancyX();
				Double redundancyY = item.getRedundancyY();
				Double redundancyZ = item.getRedundancyZ();
				Double redundancy = null;
				
				if (this.type == ObservationType.GNSS1D && redundancyZ != null) 
					redundancy = redundancyZ;

				else if (this.type == ObservationType.GNSS2D && redundancyY != null && redundancyX != null) 
					redundancy = Math.min(redundancyY, redundancyX);
				
				else if (this.type == ObservationType.GNSS3D && redundancyY != null && redundancyX != null && redundancyZ != null)
					redundancy = Math.min(redundancyZ, Math.min(redundancyY, redundancyX));
				
				if (redundancy == null) 
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				else
					this.setTableRowHighlight(row, redundancy < leftBoundary ? TableRowHighlightRangeType.INADEQUATE : 
						redundancy <= rightBoundary ? TableRowHighlightRangeType.SATISFACTORY :
							TableRowHighlightRangeType.EXCELLENT);
				
				break;
				
			case INFLUENCE_ON_POSITION:
				Double influenceOnPositionX = item.getInfluenceOnPointPositionX();
				Double influenceOnPositionY = item.getInfluenceOnPointPositionY();
				Double influenceOnPositionZ = item.getInfluenceOnPointPositionZ();
				Double influenceOnPosition = null;
				
				if (this.type == ObservationType.GNSS1D && influenceOnPositionZ != null) 
					influenceOnPosition = influenceOnPositionZ;

				else if (this.type == ObservationType.GNSS2D && influenceOnPositionY != null && influenceOnPositionX != null) 
					influenceOnPosition = Math.max(influenceOnPositionY, influenceOnPositionX);
				
				else if (this.type == ObservationType.GNSS3D && influenceOnPositionY != null && influenceOnPositionX != null && influenceOnPositionZ != null)
					influenceOnPosition = Math.max(influenceOnPositionZ, Math.max(influenceOnPositionY, influenceOnPositionX));
				
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