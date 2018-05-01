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

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dnd.CongruenceAnalysisRowDnD;
import org.applied_geodesy.jag3d.ui.table.row.CongruenceAnalysisRow;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlight;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightRangeType;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightType;
import org.applied_geodesy.jag3d.ui.tree.CongruenceAnalysisTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.EditableMenuCheckBoxTreeCell;
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

public class UICongruenceAnalysisTableBuilder extends UIEditableTableBuilder<CongruenceAnalysisRow> {	
	private CongruenceAnalysisTreeItemValue congruenceAnalysisItemValue;
	private int dimension;
	private Map<Integer, TableView<CongruenceAnalysisRow>> tables = new HashMap<Integer, TableView<CongruenceAnalysisRow>>();

	private static UICongruenceAnalysisTableBuilder tableBuilder = new UICongruenceAnalysisTableBuilder();
	private UICongruenceAnalysisTableBuilder() {
		super();
	}

	public static UICongruenceAnalysisTableBuilder getInstance() {
		return tableBuilder;
	}

	public TableView<CongruenceAnalysisRow> getTable(CongruenceAnalysisTreeItemValue congruenceAnalysisItemValue) {
		this.congruenceAnalysisItemValue = congruenceAnalysisItemValue;
		this.dimension = congruenceAnalysisItemValue.getDimension();
		this.init();
		return this.table;
	}

	private void init() {
		if (this.tables.containsKey(this.dimension)) {
			this.table = this.tables.get(this.dimension);
			return;
		}
		TableColumn<CongruenceAnalysisRow, Boolean> booleanColumn = null;
		TableColumn<CongruenceAnalysisRow, String> stringColumn   = null;
		TableColumn<CongruenceAnalysisRow, Double> doubleColumn   = null;

		TableView<CongruenceAnalysisRow> table = this.createTable();
		///////////////// A-PRIORI VALUES /////////////////////////////
		// Enable/Disable
		int columnIndex = table.getColumns().size(); 
		final int columnIndexEnable = columnIndex;
		String labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.enable.label", "Enable");
		String tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.enable.tooltip", "State of the point nexus");
		CellValueType cellValueType = CellValueType.BOOLEAN;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType,labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, CongruenceAnalysisRow::enableProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, true);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<CongruenceAnalysisRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<CongruenceAnalysisRow, Boolean> param) {
				final TableCellChangeListener<Boolean> enableChangeListener = new TableCellChangeListener<Boolean>(columnIndexEnable, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isEnable());
				booleanProp.addListener(enableChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		// Station-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.reference.epoch.name.label", "Point-Id (Reference epoch)");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.reference.epoch.name.tooltip", "Id of the point w.r.t. reference epoch");
		cellValueType = CellValueType.STRING;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, CongruenceAnalysisRow::nameInReferenceEpochProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true); 
		stringColumn.setPrefWidth(175);
		table.getColumns().add(stringColumn);

		// Target-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.control.epoch.name.label", "Point-Id (Control epoch)");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.control.epoch.name.tooltip", "Id of the point w.r.t. control epoch");
		cellValueType = CellValueType.STRING;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, CongruenceAnalysisRow::nameInControlEpochProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true);
		stringColumn.setPrefWidth(175);
		table.getColumns().add(stringColumn);

		///////////////// A-POSTERIORI VALUES /////////////////////////////
		// A-posteriori Components

		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.y.label", "y");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.y.tooltip", "A-posteriori y-component of displacement vector");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::yAposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.x.label", "x");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.x.tooltip", "A-posteriori x-component of displacement vector");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::xAposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.z.label", "z");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.z.tooltip", "A-posteriori z-component of displacement vector");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::zAposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 2 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);


		// A-posteriori Uncertainties
		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.sigma.y.label", "\u03C3y");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.sigma.y.tooltip", "A-posteriori uncertainty of y-component of displacement vector");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::sigmaYaposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.sigma.x.label", "\u03C3x");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.sigma.x.tooltip", "A-posteriori uncertainty of x-component of displacement vector");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::sigmaXaposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.sigma.z.label", "\u03C3z");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.sigma.z.tooltip", "A-posteriori uncertainty of z-component of displacement vector");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::sigmaZaposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 2 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Confidence
		// A
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.semiaxis.major.label", "a");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.semiaxis.major.tooltip", "Major semi axis");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::confidenceAProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT_CONGRUENCE, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// B
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.semiaxis.middle.label", "b");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.semiaxis.middle.tooltip", "Middle semi axis");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::confidenceBProperty, getDoubleCallback(cellValueType), this.dimension == 3 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// C
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.semiaxis.minor.label", "c");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.semiaxis.minor.tooltip", "Minor semi axis");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::confidenceCProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// alpha
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.semiaxisrotation.alpha.label", "\u03B1");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.semiaxisrotation.alpha.tooltip", "Rotation angle of confidence region");
		cellValueType = CellValueType.ANGLE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::confidenceAlphaProperty, getDoubleCallback(cellValueType), this.dimension == 3 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// beta
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.semiaxisrotation.beta.label", "\u03B2");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.semiaxisrotation.beta.tooltip", "Rotation angle of confidence region");
		cellValueType = CellValueType.ANGLE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::confidenceBetaProperty, getDoubleCallback(cellValueType), this.dimension == 3 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// gamma
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.semiaxisrotation.gamma.label", "\u03B3");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.semiaxisrotation.gamma.tooltip", "Rotation angle of confidence region");
		cellValueType = CellValueType.ANGLE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::confidenceGammaProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// Gross-Error
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.grosserror.y.label", "\u2207y");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.grosserror.y.tooltip", "Gross-error in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::grossErrorYProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.grosserror.x.label", "\u2207x");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.grosserror.x.tooltip", "Gross-error in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::grossErrorXProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.grosserror.z.label", "\u2207z");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.grosserror.z.tooltip", "Gross-error in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::grossErrorZProperty, getDoubleCallback(cellValueType), this.dimension != 2 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);


		// MDB
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.minimaldetectablebias.y.label", "\u2207y(\u03B1,\u03B2)");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.minimaldetectablebias.y.tooltip", "Minimal detectable bias in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::minimalDetectableBiasYProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.minimaldetectablebias.x.label", "\u2207x(\u03B1,\u03B2)");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.minimaldetectablebias.x.tooltip", "Minimal detectable bias in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::minimalDetectableBiasXProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.minimaldetectablebias.z.label", "\u2207z(\u03B1,\u03B2)");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.minimaldetectablebias.z.tooltip", "Minimal detectable bias in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::minimalDetectableBiasZProperty, getDoubleCallback(cellValueType), this.dimension != 2 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);


		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.pvalue.apriori.label", "log(Pprio)");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.pvalue.apriori.tooltip", "A-priori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::pValueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT_CONGRUENCE, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.pvalue.aposteriori.label", "log(Ppost)");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.pvalue.aposteriori.tooltip", "A-posteriori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::pValueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT_CONGRUENCE, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Tprio
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.teststatistic.apriori.label", "Tprio");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.teststatistic.apriori.tooltip", "A-priori test statistic");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::testStatisticAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT_CONGRUENCE, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Tpost
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.teststatistic.aposteriori.label", "Tpost");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.teststatistic.aposteriori.tooltip", "A-posteriori test statistic");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::testStatisticAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT_CONGRUENCE, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexOutlier = columnIndex;
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.testdecision.label", "Significant");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.testdecision.tooltip", "Checked, if null-hypothesis is rejected");
		cellValueType = CellValueType.BOOLEAN;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, CongruenceAnalysisRow::significantProperty, getBooleanCallback(), ColumnType.APOSTERIORI_POINT_CONGRUENCE, columnIndex, false);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<CongruenceAnalysisRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<CongruenceAnalysisRow, Boolean> param) {
				final TableCellChangeListener<Boolean> significantChangeListener = new TableCellChangeListener<Boolean>(columnIndexOutlier, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isSignificant());
				booleanProp.addListener(significantChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		this.addContextMenu(table, this.createContextMenu(false));
		this.addDynamicRowAdder(table);

		this.tables.put(this.dimension, table);
		this.table = table;
	}

	
	@Override
	public CongruenceAnalysisRow getEmptyRow() {
		return new CongruenceAnalysisRow();
	}

	@Override
	void setValue(CongruenceAnalysisRow rowData, int columnIndex, Object oldValue, Object newValue) {
		boolean valid = (oldValue == null || oldValue.toString().trim().isEmpty()) && (newValue == null || newValue.toString().trim().isEmpty());
		switch (columnIndex) {
		case 0:
			rowData.setEnable(newValue != null && newValue instanceof Boolean && (Boolean)newValue);
			valid = true;
			break;
		case 1:
			if (newValue != null && !newValue.toString().trim().isEmpty() && ((rowData.getNameInControlEpoch() == null || !rowData.getNameInControlEpoch().equals(newValue.toString().trim())))) {
				rowData.setNameInReferenceEpoch(newValue.toString().trim());
				valid = true;
			}
			else
				rowData.setNameInReferenceEpoch(oldValue == null ? null : oldValue.toString().trim());
			break;
		case 2:
			if (newValue != null && !newValue.toString().trim().isEmpty() && ((rowData.getNameInReferenceEpoch() == null || !rowData.getNameInReferenceEpoch().equals(newValue.toString().trim())))) {
				rowData.setNameInControlEpoch(newValue.toString().trim());
				valid = true;
			}
			else
				rowData.setNameInControlEpoch(oldValue == null ? null : oldValue.toString().trim());
			break;
		default:
			System.err.println(this.getClass().getSimpleName() + " : Editable column exceed " + columnIndex);
			valid = false;
			break;
		}
		
		if (valid && this.isComplete(rowData)) {
			// Set observation group id, if not exists
			if (rowData.getGroupId() < 0)
				rowData.setGroupId(this.congruenceAnalysisItemValue.getGroupId());

			try {
				SQLManager.getInstance().saveItem(rowData);
			} catch (Exception e) {
				switch (columnIndex) {
				case 1:
					rowData.setNameInReferenceEpoch(oldValue == null ? null : oldValue.toString().trim());
					break;
				case 2:
					rowData.setNameInControlEpoch(oldValue == null ? null : oldValue.toString().trim());
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
	
	private boolean isComplete(CongruenceAnalysisRow row) {
		return row.getNameInReferenceEpoch() != null && !row.getNameInReferenceEpoch().trim().isEmpty() &&
				row.getNameInControlEpoch() != null  && !row.getNameInControlEpoch().trim().isEmpty() &&
				!row.getNameInReferenceEpoch().equals(row.getNameInControlEpoch());
	}
	
	@Override
	void enableDragSupport() {
		this.table.setOnDragDetected(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent event) {
		    	List<CongruenceAnalysisRow> selectedRows = new ArrayList<CongruenceAnalysisRow>(table.getSelectionModel().getSelectedItems());
		    	if (selectedRows != null && !selectedRows.isEmpty()) {

		    		List<CongruenceAnalysisRowDnD> rowsDnD = new ArrayList<CongruenceAnalysisRowDnD>(selectedRows.size());
		    		for (CongruenceAnalysisRow selectedRow : selectedRows) {
		    			CongruenceAnalysisRowDnD rowDnD = null;
		    			if (isComplete(selectedRow) && (rowDnD = CongruenceAnalysisRowDnD.fromCongruenceAnalysisRow(selectedRow)) != null) {
		    				rowsDnD.add(rowDnD);
		    			}
		    		}

		    		if (!rowsDnD.isEmpty()) {
		    			Dragboard db = table.startDragAndDrop(TransferMode.MOVE);
		    			ClipboardContent content = new ClipboardContent();
		    			content.put(EditableMenuCheckBoxTreeCell.TREE_ITEM_TYPE_DATA_FORMAT, congruenceAnalysisItemValue.getItemType());
		    			content.put(EditableMenuCheckBoxTreeCell.GROUP_ID_DATA_FORMAT,       congruenceAnalysisItemValue.getGroupId());
		    			content.put(EditableMenuCheckBoxTreeCell.DIMENSION_DATA_FORMAT,      dimension);
		    			content.put(EditableMenuCheckBoxTreeCell.CONGRUENCE_ANALYSIS_ROWS_DATA_FORMAT, rowsDnD);

		    			db.setContent(content);
		    		}
		    	}
		        event.consume();
		    }
		});
	}
	
	@Override
	void duplicateRows() {
		List<CongruenceAnalysisRow> selectedRows = new ArrayList<CongruenceAnalysisRow>(this.table.getSelectionModel().getSelectedItems());
		if (selectedRows == null || selectedRows.isEmpty())
			return;

		List<CongruenceAnalysisRow> clonedRows = new ArrayList<CongruenceAnalysisRow>(selectedRows.size());
		for (CongruenceAnalysisRow row : selectedRows) {
			CongruenceAnalysisRow clonedRow = CongruenceAnalysisRow.cloneRowApriori(row);
			
			if (this.isComplete(clonedRow)) {
				try {
					// Generate next unique point names
					String[] names = SQLManager.getInstance().getNextValidPointNexusNames(this.congruenceAnalysisItemValue.getGroupId(), row.getNameInReferenceEpoch(), row.getNameInControlEpoch());
					clonedRow.setNameInReferenceEpoch(names[0]);
					clonedRow.setNameInControlEpoch(names[1]);
					SQLManager.getInstance().saveItem(clonedRow);
				} 
				catch (Exception e) {
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
			for (CongruenceAnalysisRow clonedRow : clonedRows)
				this.table.getSelectionModel().select(clonedRow);
			this.table.scrollTo(clonedRows.get(0));
		}
	}
	
	@Override
	void removeRows() {
		List<CongruenceAnalysisRow> selectedRows = new ArrayList<CongruenceAnalysisRow>(this.table.getSelectionModel().getSelectedItems());
		if (selectedRows == null || selectedRows.isEmpty())
			return;
		
		List<CongruenceAnalysisRow> removedRows = new ArrayList<CongruenceAnalysisRow>(selectedRows.size());
		for (CongruenceAnalysisRow row : selectedRows) {
			if (this.isComplete(row)) {
				try {
					SQLManager.getInstance().remove(row);
				} 
				catch (Exception e) {
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
				
		List<CongruenceAnalysisRow> selectedRows = new ArrayList<CongruenceAnalysisRow>(this.table.getSelectionModel().getSelectedItems());
		if (selectedRows == null || selectedRows.isEmpty())
			return;
		
		TreeItemType parentType = TreeItemType.getDirectoryByLeafType(this.congruenceAnalysisItemValue.getItemType());
		TreeItem<TreeItemValue> newTreeItem = UITreeBuilder.getInstance().addItem(parentType, false);
		try {
			SQLManager.getInstance().saveGroup((CongruenceAnalysisTreeItemValue)newTreeItem.getValue());
		} 
		catch (Exception e) {
			raiseErrorMessage(type, e);
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			return;
		}
		
		try {
			int groupId = ((CongruenceAnalysisTreeItemValue)newTreeItem.getValue()).getGroupId();
			for (CongruenceAnalysisRow row : selectedRows) {
				row.setGroupId(groupId);
				SQLManager.getInstance().saveItem(row);
			}
		} 
		catch (Exception e) {
			raiseErrorMessage(type, e);
			e.printStackTrace();
			return;
		}
		
		UITreeBuilder.getInstance().getTree().getSelectionModel().select(newTreeItem);
	}
	
	public void export(File file, boolean aprioriValues) throws IOException {
		List<CongruenceAnalysisRow> rows = this.table.getItems();
		
		String exportFormatString = "%15s \t";
		//String exportFormatDouble = "%+15.6f \t";
		String exportFormatDouble = "%20s \t";
		
		PrintWriter writer = null;

		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter( file )));

			for (CongruenceAnalysisRow row : rows) {
				if (!row.isEnable())
					continue;

				String nameInReferenceEpoch = row.getNameInReferenceEpoch();
				String nameInControlEpoch   = row.getNameInControlEpoch();

				if (nameInReferenceEpoch == null || nameInReferenceEpoch.trim().isEmpty() || 
						nameInControlEpoch == null || nameInControlEpoch.trim().isEmpty())
					continue;

				Double y = aprioriValues ? null : row.getYAposteriori();
				Double x = aprioriValues ? null : row.getXAposteriori();
				Double z = aprioriValues ? null : row.getZAposteriori();

				if (!aprioriValues && this.dimension != 2 && z == null)
					continue;
				
				if (!aprioriValues && this.dimension != 1 && (y == null || x == null))
					continue;

				Double sigmaY = aprioriValues ? null : row.getSigmaYaposteriori();
				Double sigmaX = aprioriValues ? null : row.getSigmaXaposteriori();
				Double sigmaZ = aprioriValues ? null : row.getSigmaZaposteriori();

				if (!aprioriValues && (sigmaY == null || sigmaX == null || sigmaZ == null))
					continue;
				
				String yValue = this.dimension != 1 && y != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(y, false)) : "";
				String xValue = this.dimension != 1 && x != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(x, false)) : "";
				String zValue = this.dimension != 2 && z != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(z, false)) : "";

				String sigmaYvalue = this.dimension != 1 && sigmaY != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(sigmaY, false)) : "";
				String sigmaXvalue = this.dimension != 1 && sigmaX != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(sigmaX, false)) : "";
				String sigmaZvalue = this.dimension != 2 && sigmaZ != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(sigmaZ, false)) : "";

				writer.println(
						String.format(exportFormatString, nameInReferenceEpoch) +
						String.format(exportFormatString, nameInControlEpoch) +
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
	void highlightTableRow(TableRow<CongruenceAnalysisRow> row) {
		if (row == null)
			return;
		
		TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
		TableRowHighlightType tableRowHighlightType = tableRowHighlight.getTableRowHighlightType(); 
		double leftBoundary  = tableRowHighlight.getLeftBoundary(); 
		double rightBoundary = tableRowHighlight.getRightBoundary();
		
		CongruenceAnalysisRow item = row.getItem();

		if (!row.isSelected() && item != null) {
			switch(tableRowHighlightType) {
			case TEST_STATISTIC:
				this.setTableRowHighlight(row, item.isSignificant() ? TableRowHighlightRangeType.INADEQUATE : TableRowHighlightRangeType.EXCELLENT);
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
				
			default:
				this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				
				break;
			}
		} 
		else {
			this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
		}
	}
}