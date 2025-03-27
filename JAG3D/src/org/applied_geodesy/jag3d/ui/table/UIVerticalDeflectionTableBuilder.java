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

import org.applied_geodesy.adjustment.network.VerticalDeflectionType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dnd.VerticalDeflectionRowDnD;
import org.applied_geodesy.jag3d.ui.table.column.ColumnContentType;
import org.applied_geodesy.jag3d.ui.table.column.TableContentType;
import org.applied_geodesy.jag3d.ui.table.row.VerticalDeflectionRow;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlight;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightRangeType;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightType;
import org.applied_geodesy.jag3d.ui.tree.EditableMenuCheckBoxTreeCell;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.jag3d.ui.tree.VerticalDeflectionTreeItemValue;
import org.applied_geodesy.ui.table.AbsoluteValueComparator;
import org.applied_geodesy.ui.table.ColumnTooltipHeader;
import org.applied_geodesy.ui.table.ColumnType;
import org.applied_geodesy.ui.table.NaturalOrderTableColumnComparator;
import org.applied_geodesy.util.CellValueType;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.MultipleSelectionModel;
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

public class UIVerticalDeflectionTableBuilder extends UIEditableTableBuilder<VerticalDeflectionRow> {	
	private VerticalDeflectionTreeItemValue verticalDeflectionItemValue;
	private VerticalDeflectionType type;
	private Map<VerticalDeflectionType, TableView<VerticalDeflectionRow>> tables = new HashMap<VerticalDeflectionType, TableView<VerticalDeflectionRow>>();
	
	
	private static UIVerticalDeflectionTableBuilder tableBuilder = new UIVerticalDeflectionTableBuilder();
	private UIVerticalDeflectionTableBuilder() {
		super();
	}

	public static UIVerticalDeflectionTableBuilder getInstance() {
		return tableBuilder;
	}

	public TableView<VerticalDeflectionRow> getTable(VerticalDeflectionTreeItemValue verticalDeflectionItemValue) {
		this.verticalDeflectionItemValue = verticalDeflectionItemValue;
		this.type = TreeItemType.getVerticalDeflectionTypeByTreeItemType(verticalDeflectionItemValue.getItemType());
		switch(this.type) {
		case REFERENCE_VERTICAL_DEFLECTION:
		case STOCHASTIC_VERTICAL_DEFLECTION:
		case UNKNOWN_VERTICAL_DEFLECTION:
			this.init();
			return this.table;
		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, unsuported observation type " + type);
		}
	}
	
	private TableContentType getTableContentType() {
		switch(this.type) {
		case REFERENCE_VERTICAL_DEFLECTION:
			return TableContentType.REFERENCE_DEFLECTION;
		case STOCHASTIC_VERTICAL_DEFLECTION:
			return TableContentType.STOCHASTIC_DEFLECTION;	
		default:
			return TableContentType.UNKNOWN_DEFLECTION;	
		}
	}

	private void init() {
		if (this.tables.containsKey(this.type)) {
			this.table = this.tables.get(this.type);
			return;
		}
		
		TableColumn<VerticalDeflectionRow, Boolean> booleanColumn = null;
		TableColumn<VerticalDeflectionRow, String> stringColumn   = null;
		TableColumn<VerticalDeflectionRow, Double> doubleColumn   = null;

		TableContentType tableContentType = this.getTableContentType();
		
		TableView<VerticalDeflectionRow> table = this.createTable();
		
		///////////////// A-PRIORI VALUES /////////////////////////////
		// Enable/Disable
		int columnIndex = table.getColumns().size(); 
		final int columnIndexEnable = columnIndex;
		String labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.enable.label", "Enable");
		String tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.enable.tooltip", "State of the vertical deflection");
		CellValueType cellValueType = CellValueType.BOOLEAN;
		ColumnContentType columnContentType = ColumnContentType.ENABLE;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::enableProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, true, true);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<VerticalDeflectionRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<VerticalDeflectionRow, Boolean> param) {
				final TableCellChangeListener<Boolean> enableChangeListener = new TableCellChangeListener<Boolean>(columnIndexEnable, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isEnable());
				booleanProp.addListener(enableChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		// Point-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.station.name.label", "Point-Id");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.station.name.tooltip", "Id of the point");
		cellValueType = CellValueType.STRING;
		columnContentType = ColumnContentType.POINT_NAME;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::nameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true, true); 
		stringColumn.setComparator(new NaturalOrderTableColumnComparator<String>(stringColumn));
		table.getColumns().add(stringColumn);
		
		// A-priori Deflection params
		// Y0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.y0.label", "\u03B6y0");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.y0.tooltip", "A-priori y-component of point deflection");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.VALUE_Y_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::yAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_DEFLECTION, columnIndex, true, true);
		table.getColumns().add(doubleColumn);

		// X0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.x0.label", "\u03B6x0");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.x0.tooltip", "A-priori x-component of point deflection");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.VALUE_X_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::xAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_DEFLECTION, columnIndex, true, true);
		table.getColumns().add(doubleColumn);
		
		// A-priori Deflection uncertainties
		// Y0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.sigma.y0.label", "\u03C3y0");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.sigma.y0.tooltip", "A-priori uncertainty of y-component");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.UNCERTAINTY_Y_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::sigmaYaprioriProperty, getDoubleCallback(cellValueType), this.type == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION ? ColumnType.APRIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, true, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// X0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.sigma.x0.label", "\u03C3x0");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.sigma.x0.tooltip", "A-priori uncertainty of x-component");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.UNCERTAINTY_X_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::sigmaXaprioriProperty, getDoubleCallback(cellValueType), this.type == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION ? ColumnType.APRIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, true, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		
		
		////////////////////////A-POSTERIORI DEFLECTION ///////////////////////////////
		// y-comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.y.label", "\u03B6y");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.y.tooltip", "Y-component of deflection of the vertical");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.VALUE_Y_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::yAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_DEFLECTION, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// x-comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.x.label", "\u03B6x");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.x.tooltip", "X-component of deflection of the vertical");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.VALUE_X_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::xAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_DEFLECTION, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// Uncertainty
		// y-comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.sigma.y.label", "\u03c3y");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.sigma.y.tooltip", "A-posteriori uncertainty of y-component of deflection of the vertical");
		cellValueType = CellValueType.ANGLE_UNCERTAINTY;
		columnContentType = ColumnContentType.UNCERTAINTY_Y_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::sigmaYaposterioriProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// x-comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.sigma.x.label", "\u03c3x");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.sigma.x.tooltip", "A-posteriori uncertainty of x-component of deflection of the vertical");
		cellValueType = CellValueType.ANGLE_UNCERTAINTY;
		columnContentType = ColumnContentType.UNCERTAINTY_X_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::sigmaXaposterioriProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// Confidence
		// A
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.semiaxis.major.label", "a");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.semiaxis.major.tooltip", "Major semi axis");
		cellValueType = CellValueType.ANGLE_UNCERTAINTY;
		columnContentType = ColumnContentType.CONFIDENCE_A;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::confidenceAProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// C
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.semiaxis.minor.label", "c");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.semiaxis.minor.tooltip", "Minor semi axis");
		cellValueType = CellValueType.ANGLE_UNCERTAINTY;
		columnContentType = ColumnContentType.CONFIDENCE_C;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::confidenceCProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// Redundancy
		// ry
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.redundancy.y.label", "ry");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.redundancy.y.tooltip", "Redundancy of y-component of deflection of the vertical");
		cellValueType = CellValueType.PERCENTAGE;
		columnContentType = ColumnContentType.REDUNDANCY_Y;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::redundancyYProperty, getDoubleCallback(cellValueType), this.type == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// rx
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.redundancy.x.label", "rx");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.redundancy.x.tooltip", "Redundancy of x-component of deflection of the vertical");
		cellValueType = CellValueType.PERCENTAGE;
		columnContentType = ColumnContentType.REDUNDANCY_X;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::redundancyXProperty, getDoubleCallback(cellValueType), this.type == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// Residual
		// y-epsilon
		columnIndex = table.getColumns().size(); 
		labelText   = this.type == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION ? i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.residual.y.label", "\u03B5y")                                                                               : i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.deviation.y.label", "\u0394y");
		tooltipText = this.type == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION ? i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.residual.y.tooltip", "Residual of y-component of deflection of the vertical, i.e. computed minus observed") : i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.deviation.y.tooltip", "Deviation of y-component w.r.t. y0");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.RESIDUAL_Y;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::residualYProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// x-epsilon
		columnIndex = table.getColumns().size(); 
		labelText   = this.type == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION ? i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.residual.x.label", "\u03B5y")                                                                               : i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.deviation.x.label", "\u0394x");
		tooltipText = this.type == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION ? i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.residual.x.tooltip", "Residual of x-component of deflection of the vertical, i.e. computed minus observed") : i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.deviation.x.tooltip", "Deviation of x-component w.r.t. x0");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.RESIDUAL_X;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::residualXProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.REFERENCE_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// Gross-error
		// y-Nabla
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.grosserror.y.label", "\u2207y");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.grosserror.y.tooltip", "Gross-error of deflection of the vertical in y");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.GROSS_ERROR_Y;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::grossErrorYProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// x-Nabla
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.grosserror.x.label", "\u2207x");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.grosserror.x.tooltip", "Gross-error of deflection of the vertical in x");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.GROSS_ERROR_X;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::grossErrorXProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);
		
		// MTB
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.maximumtolerablebias.y.label", "\u2207x(1)");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.maximumtolerablebias.y.tooltip", "Maximum tolerable bias of deflection of the vertical in y");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.MAXIMUM_TOLERABLE_BIAS_Y;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::maximumTolerableBiasYProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.maximumtolerablebias.x.label", "\u2207y(1)");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.maximumtolerablebias.x.tooltip", "Maximum tolerable bias of deflection of the vertical in x");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.MAXIMUM_TOLERABLE_BIAS_X;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::maximumTolerableBiasXProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// MDB
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.minimaldetectablebias.y.label", "\u2207x(\u03BB)");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.minimaldetectablebias.y.tooltip", "Minimal detectable bias of deflection of the vertical in y");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.MINIMAL_DETECTABLE_BIAS_Y;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::minimalDetectableBiasYProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.minimaldetectablebias.x.label", "\u2207y(\u03BB)");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.minimaldetectablebias.x.tooltip", "Minimal detectable bias of deflection of the vertical in x");
		cellValueType = CellValueType.ANGLE_RESIDUAL;
		columnContentType = ColumnContentType.MINIMAL_DETECTABLE_BIAS_X;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::minimalDetectableBiasXProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// vTPv
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.omega.label", "\u03A9");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.omega.tooltip", "Weighted squares of residual");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = ColumnContentType.OMEGA;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::omegaProperty, getDoubleCallback(cellValueType), this.type == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.pvalue.apriori.label", "log(Pprio)");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.pvalue.apriori.tooltip", "A-priori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = ColumnContentType.P_VALUE_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::pValueAprioriProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.pvalue.aposteriori.label", "log(Ppost)");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.pvalue.aposteriori.tooltip", "A-posteriori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = ColumnContentType.P_VALUE_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::pValueAposterioriProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// Tprio
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.teststatistic.apriori.label", "Tprio");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.teststatistic.apriori.tooltip", "A-priori test statistic");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = ColumnContentType.TEST_STATISTIC_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::testStatisticAprioriProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// Tpost
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.teststatistic.aposteriori.label", "Tpost");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.teststatistic.aposteriori.tooltip", "A-posteriori test statistic");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = ColumnContentType.TEST_STATISTIC_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::testStatisticAposterioriProperty, getDoubleCallback(cellValueType), this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// Decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexOutlierDeflection = columnIndex;
		labelText   = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.testdecision.label", "Significant");
		tooltipText = i18n.getString("UIVerticalDeflectionTableBuilder.tableheader.testdecision.tooltip", "Checked, if null-hypothesis is rejected");
		cellValueType = CellValueType.BOOLEAN;
		columnContentType = ColumnContentType.SIGNIFICANT;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(tableContentType, columnContentType, header, VerticalDeflectionRow::significantProperty, getBooleanCallback(), this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION ? ColumnType.APOSTERIORI_DEFLECTION : ColumnType.HIDDEN, columnIndex, false, true);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<VerticalDeflectionRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<VerticalDeflectionRow, Boolean> param) {
				final TableCellChangeListener<Boolean> significantChangeListener = new TableCellChangeListener<Boolean>(columnIndexOutlierDeflection, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isSignificant());
				booleanProp.addListener(significantChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		this.addContextMenu(table, this.createContextMenu(ContextMenuType.DEFAULT));
		this.addDynamicRowAdder(table);
		this.addColumnOrderSequenceListeners(tableContentType, table);

		this.tables.put(this.type, table);
		this.table = table;
	}
	
	@Override
	public VerticalDeflectionRow getEmptyRow() {
		return new VerticalDeflectionRow();
	}
	
	@Override
	void setValue(VerticalDeflectionRow rowData, int columnIndex, Object oldValue, Object newValue) {
		boolean valid = (oldValue == null || oldValue.toString().trim().isEmpty()) && (newValue == null || newValue.toString().trim().isEmpty());
		switch (columnIndex) {

		case 0:
			rowData.setEnable(newValue != null && newValue instanceof Boolean && (Boolean)newValue);
			valid = true;
			break;
		case 1:
			if (newValue != null && !newValue.toString().trim().isEmpty()) {
				rowData.setName(newValue.toString().trim());
				valid = true;
			}
			else
				rowData.setName(oldValue == null ? null : oldValue.toString().trim());
			break;
		case 2:
			if (newValue != null && newValue instanceof Double) {
				rowData.setYApriori((Double)newValue);	
				valid = true;
			}
			else
				rowData.setYApriori(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 3:
			if (newValue != null && newValue instanceof Double) {
				rowData.setXApriori((Double)newValue);	
				valid = true;
			}
			else
				rowData.setXApriori(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 4:
			rowData.setSigmaYapriori(newValue == null ? null : newValue instanceof Double && (Double)newValue > 0 ? (Double)newValue : null);
			valid = true;
			break;
		case 5:
			rowData.setSigmaXapriori(newValue == null ? null : newValue instanceof Double && (Double)newValue > 0 ? (Double)newValue : null);
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
				rowData.setGroupId(this.verticalDeflectionItemValue.getGroupId());
			
			try {
				SQLManager.getInstance().saveItem(rowData);
			} catch (Exception e) {
				switch (columnIndex) {
				case 1:
					rowData.setName(oldValue == null ? null : oldValue.toString().trim());
					break;
				default:
					break;
				}
				valid = false;
				raiseErrorMessageSaveValue(e);
				e.printStackTrace();
			}
		}

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				table.refresh();
				table.requestFocus();
				table.getSelectionModel().clearSelection();
				table.getSelectionModel().select(rowData);
				table.sort();
			}
		});
	}
	
	private boolean isComplete(VerticalDeflectionRow row) {
		return row.getName() != null && !row.getName().isBlank() &&
				row.getXApriori() != null &&
				row.getYApriori() != null;
	}
	
	@Override
	void enableDragSupport() {
		this.table.setOnDragDetected(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent event) {
		    	List<VerticalDeflectionRow> selectedRows = new ArrayList<VerticalDeflectionRow>(table.getSelectionModel().getSelectedItems());
		    	if (selectedRows != null && !selectedRows.isEmpty()) {

		    		List<VerticalDeflectionRowDnD> rowsDnD = new ArrayList<VerticalDeflectionRowDnD>(selectedRows.size());
		    		for (VerticalDeflectionRow selectedRow : selectedRows) {
		    			VerticalDeflectionRowDnD rowDnD = null;
		    			if (isComplete(selectedRow) && (rowDnD = VerticalDeflectionRowDnD.fromVerticalDeflectionRow(selectedRow)) != null) {
		    				rowsDnD.add(rowDnD);
		    			}
		    		}

		    		if (!rowsDnD.isEmpty()) {
		    			Dragboard db = table.startDragAndDrop(TransferMode.MOVE);
		    			ClipboardContent content = new ClipboardContent();
		    			content.put(EditableMenuCheckBoxTreeCell.TREE_ITEM_TYPE_DATA_FORMAT,           verticalDeflectionItemValue.getItemType());
		    			content.put(EditableMenuCheckBoxTreeCell.GROUP_ID_DATA_FORMAT,                 verticalDeflectionItemValue.getGroupId());
		    			content.put(EditableMenuCheckBoxTreeCell.DIMENSION_DATA_FORMAT,                verticalDeflectionItemValue.getDimension());
		    			content.put(EditableMenuCheckBoxTreeCell.VERTICAL_DEFLECTION_ROWS_DATA_FORMAT, rowsDnD);

		    			db.setContent(content);
		    		}
		    	}
		        event.consume();
		    }
		});
	}
	
	@Override
	void duplicateRows() {
		List<VerticalDeflectionRow> selectedRows = new ArrayList<VerticalDeflectionRow>(this.table.getSelectionModel().getSelectedItems());
		if (selectedRows == null || selectedRows.isEmpty())
			return;

		List<VerticalDeflectionRow> clonedRows = new ArrayList<VerticalDeflectionRow>(selectedRows.size());
		for (VerticalDeflectionRow row : selectedRows) {
			VerticalDeflectionRow clonedRow = VerticalDeflectionRow.cloneRowApriori(row);
			if (this.isComplete(clonedRow)) {
				try {
					// Generate next unique point name
					clonedRow.setName(SQLManager.getInstance().getNextValidVerticalDeflectionName(row.getName()));
					SQLManager.getInstance().saveItem(clonedRow);
				} catch (Exception e) {
					raiseErrorMessage(ContextMenuItemType.DUPLICATE, e);
					e.printStackTrace();
					break;
				}
			}
			clonedRows.add(clonedRow);
		}

		if (clonedRows != null && !clonedRows.isEmpty()) {
			ObservableList<VerticalDeflectionRow> tableModel = this.getTableModel(this.table);
			tableModel.addAll(clonedRows);
			this.table.getSelectionModel().clearSelection();
			for (VerticalDeflectionRow clonedRow : clonedRows)
				this.table.getSelectionModel().select(clonedRow);
			this.table.scrollTo(clonedRows.get(0));
		}
	}
	
	@Override
	void removeRows() {
		List<VerticalDeflectionRow> selectedRows = new ArrayList<VerticalDeflectionRow>(this.table.getSelectionModel().getSelectedItems());
		if (selectedRows == null || selectedRows.isEmpty())
			return;
		
		List<VerticalDeflectionRow> removedRows = new ArrayList<VerticalDeflectionRow>(selectedRows.size());
		for (VerticalDeflectionRow row : selectedRows) {
			if (this.isComplete(row)) {
				try {
					SQLManager.getInstance().remove(row);
				} catch (Exception e) {
					raiseErrorMessage(ContextMenuItemType.REMOVE, e);
					e.printStackTrace();
					break;
				}
			}
			removedRows.add(row);
		}
		if (removedRows != null && !removedRows.isEmpty()) {
			ObservableList<VerticalDeflectionRow> tableModel = this.getTableModel(this.table);
			this.table.getSelectionModel().clearSelection();
			tableModel.removeAll(removedRows);
			if (tableModel.isEmpty())
				tableModel.setAll(getEmptyRow());
		}
	}
	
	@Override
	void moveRows(ContextMenuItemType type) {
		if (type != ContextMenuItemType.MOVETO)
			return;
		
		List<VerticalDeflectionRow> selectedRows = new ArrayList<VerticalDeflectionRow>(this.table.getSelectionModel().getSelectedItems());
		if (selectedRows == null || selectedRows.isEmpty())
			return;
		
		TreeItemType parentType = TreeItemType.getDirectoryByLeafType(this.verticalDeflectionItemValue.getItemType());
		TreeItem<TreeItemValue> newTreeItem = UITreeBuilder.getInstance().addItem(parentType, false);
		try {
			SQLManager.getInstance().saveGroup((VerticalDeflectionTreeItemValue)newTreeItem.getValue());		
		} catch (Exception e) {
			raiseErrorMessage(type, e);
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			return;
		}
		
		try {
			int groupId = ((VerticalDeflectionTreeItemValue)newTreeItem.getValue()).getGroupId();
			for (VerticalDeflectionRow row : selectedRows) {
				row.setGroupId(groupId);
				SQLManager.getInstance().saveItem(row);
			}
			
		} catch (Exception e) {
			raiseErrorMessage(type, e);
			e.printStackTrace();
			return;
		}
		
		MultipleSelectionModel<TreeItem<TreeItemValue>> selectionModel = UITreeBuilder.getInstance().getTree().getSelectionModel();
		selectionModel.clearSelection();
		selectionModel.select(newTreeItem);
	}
	
	public void export(File file, boolean aprioriValues) throws IOException {
		List<VerticalDeflectionRow> rows = this.table.getItems();
		
		String exportFormatString = "%15s \t";
		//String exportFormatDouble = "%+15.6f \t";
		String exportFormatDouble = "%20s \t";
		
		PrintWriter writer = null;

		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter( file )));

			for (VerticalDeflectionRow row : rows) {
				if (!row.isEnable())
					continue;

				String name = row.getName();

				if (name == null || name.trim().isEmpty())
					continue;

				Double y = aprioriValues ? row.getYApriori() : row.getYAposteriori();
				Double x = aprioriValues ? row.getXApriori() : row.getXAposteriori();

				Double sigmaY = aprioriValues ? row.getSigmaYapriori() : row.getSigmaYaposteriori();
				Double sigmaX = aprioriValues ? row.getSigmaXapriori() : row.getSigmaXaposteriori();

				if (!aprioriValues && (sigmaY == null || sigmaX == null))
					continue;
				
				if (aprioriValues) {
					if (sigmaY != null && sigmaY <= 0)
						sigmaY = null;
					if (sigmaX != null && sigmaX <= 0)
						sigmaX = null;
				}
				
				String yValue = String.format(Locale.ENGLISH, exportFormatDouble, options.toAngleFormat(y, false));
				String xValue = String.format(Locale.ENGLISH, exportFormatDouble, options.toAngleFormat(x, false));

				String sigmaYvalue = sigmaY != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toAngleFormat(sigmaY, false)) : "";
				String sigmaXvalue = sigmaX != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toAngleFormat(sigmaX, false)) : "";
				
				writer.println(
						String.format(exportFormatString, name) +
						yValue + xValue + sigmaYvalue + sigmaXvalue
						);
			}
		}
		finally {
			if (writer != null)
				writer.close();
		}
	}
	
	@Override
	void highlightTableRow(TableRow<VerticalDeflectionRow> row) {
		if (row == null)
			return;
		
		TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
		TableRowHighlightType tableRowHighlightType = tableRowHighlight.getSelectedTableRowHighlightType(); 
		double leftBoundary  = tableRowHighlight.getLeftBoundary(tableRowHighlightType); 
		double rightBoundary = tableRowHighlight.getRightBoundary(tableRowHighlightType);

		VerticalDeflectionRow item = row.getItem();

		if (!row.isSelected() && item != null) {
			switch(tableRowHighlightType) {
			case TEST_STATISTIC:
				if (this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION)
					this.setTableRowHighlight(row, item.isSignificant() ? TableRowHighlightRangeType.INADEQUATE : TableRowHighlightRangeType.EXCELLENT);
				else
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				break;
				
			case GROSS_ERROR:
				if (this.type != VerticalDeflectionType.UNKNOWN_VERTICAL_DEFLECTION) {
					Double grossErrorX = item.getGrossErrorX();
					Double grossErrorY = item.getGrossErrorY();

					Double mtbX = item.getMaximumTolerableBiasX();
					Double mtbY = item.getMaximumTolerableBiasY();

					Double mdbX = item.getMinimalDetectableBiasX();
					Double mdbY = item.getMinimalDetectableBiasY();

					double dMTB = Double.NaN;
					double dMDB = Double.NaN;

					if (grossErrorX != null && grossErrorY != null && mtbX != null && mtbY != null && mdbX != null && mdbY != null) {
						dMTB = Math.max(Math.abs(grossErrorX) - Math.abs(mtbX), Math.abs(grossErrorY) - Math.abs(mtbY));
						dMDB = Math.min(Math.abs(mdbX) - Math.abs(grossErrorX), Math.abs(mdbY) - Math.abs(grossErrorY));
						
						if (dMTB > 0 && dMDB >= 0) 
							this.setTableRowHighlight(row, TableRowHighlightRangeType.SATISFACTORY);

						else if (dMDB < 0) 
							this.setTableRowHighlight(row, TableRowHighlightRangeType.INADEQUATE);

						else // if (dMTB <= 0) 
							this.setTableRowHighlight(row, TableRowHighlightRangeType.EXCELLENT);
					}
					else
						this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				}
				else
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				
				break;
				
			case REDUNDANCY:
				if (this.type == VerticalDeflectionType.STOCHASTIC_VERTICAL_DEFLECTION) {
					Double redundancyX = item.getRedundancyX();
					Double redundancyY = item.getRedundancyY();
					Double redundancy = null;

					if (redundancyY != null && redundancyX != null) 
						redundancy = Math.min(redundancyY, redundancyX);

					if (redundancy == null) 
						this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
					else
						this.setTableRowHighlight(row, redundancy < leftBoundary ? TableRowHighlightRangeType.INADEQUATE : 
							redundancy <= rightBoundary ? TableRowHighlightRangeType.SATISFACTORY :
								TableRowHighlightRangeType.EXCELLENT);
				}
				else
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				
				break;
				
			case INFLUENCE_ON_POSITION:
				this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				
				break;
				
			case P_PRIO_VALUE:
				Double pValue = item.getPValueApriori();
				if (pValue == null) 
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				else
					this.setTableRowHighlight(row, pValue < Math.log(leftBoundary) ? TableRowHighlightRangeType.INADEQUATE : 
						pValue <= Math.log(rightBoundary) ? TableRowHighlightRangeType.SATISFACTORY :
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
