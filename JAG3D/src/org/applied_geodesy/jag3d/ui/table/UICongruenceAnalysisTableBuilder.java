package org.applied_geodesy.jag3d.ui.table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dnd.CongruenceAnalysisRowDnD;
import org.applied_geodesy.jag3d.ui.table.row.CongruenceAnalysisRow;
import org.applied_geodesy.jag3d.ui.tree.CongruenceAnalysisTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.EditableMenuCheckBoxTreeCell;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
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
		table.getColumns().add(stringColumn);

		// Target-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.control.epoch.name.label", "Point-Id (Control epoch)");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.control.epoch.name.tooltip", "Id of the point w.r.t. control epoch");
		cellValueType = CellValueType.STRING;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, CongruenceAnalysisRow::nameInControlEpochProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true);
		table.getColumns().add(stringColumn);

		///////////////// A-POSTERIORI VALUES /////////////////////////////
		// A-posteriori Components

		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.y.label", "y");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.y.tooltip", "A-posteriori y-component of the displacement vector");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::yAposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.x.label", "x");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.x.tooltip", "A-posteriori x-component of the displacement vector");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::xAposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.z.label", "z");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.z.tooltip", "A-posteriori z-component of the displacement vector");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::zAposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 2 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);


		// A-posteriori Uncertainties
		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.sigmay.label", "\u03C3y");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.sigmay.tooltip", "A-posteriori uncertainty of y-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::sigmaYaposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.sigmax.label", "\u03C3x");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.sigmax.tooltip", "A-posteriori uncertainty of x-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::sigmaXaposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.sigmaz.label", "\u03C3z");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.sigmaz.tooltip", "A-posteriori uncertainty of z-component");
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
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.grosserror.y.tooltip", "Gross-error of y-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::grossErrorYProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.grosserror.x.label", "\u2207x");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.grosserror.x.tooltip", "Gross-error of x-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::grossErrorXProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.grosserror.z.label", "\u2207z");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.grosserror.z.tooltip", "Gross-error of z-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::grossErrorZProperty, getDoubleCallback(cellValueType), this.dimension != 2 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);


		// MDB
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.minimaldetectablebias.y.label", "\u2207y(\u03B1,\u03B2)");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.minimaldetectablebias.y.tooltip", "Minimal detectable bias of y-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::minimalDetectableBiasYProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.minimaldetectablebias.x.label", "\u2207x(\u03B1,\u03B2)");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.minimaldetectablebias.x.tooltip", "Minimal detectable bias of x-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, CongruenceAnalysisRow::minimalDetectableBiasXProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.minimaldetectablebias.z.label", "\u2207z(\u03B1,\u03B2)");
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.minimaldetectablebias.z.tooltip", "Minimal detectable bias of z-component");
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
		tooltipText = i18n.getString("UICongruenceAnalysisTableBuilder.tableheader.testdecision.tooltip", "Decision of test statistic");
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
	void setValue(CongruenceAnalysisRow rowData, int columnIndex, int rowIndex, Object oldValue, Object newValue) {
		boolean valid = false;
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
			try {
				SQLManager.getInstance().saveItem(this.congruenceAnalysisItemValue.getGroupId(), rowData);
			} catch (SQLException e) {
				
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
				raiseErrorMessageSaveValue(e);
				e.printStackTrace();
			}
		}
		table.refresh();
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
					SQLManager.getInstance().saveItem(this.congruenceAnalysisItemValue.getGroupId(), clonedRow);
				} 
				catch (SQLException e) {
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
				catch (SQLException e) {
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
		catch (SQLException e) {
			raiseErrorMessage(type, e);
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			return;
		}
		
		try {
			int groupId = ((CongruenceAnalysisTreeItemValue)newTreeItem.getValue()).getGroupId();
			for (CongruenceAnalysisRow row : selectedRows)
				SQLManager.getInstance().saveItem(groupId, row);
			
		} 
		catch (SQLException e) {
			raiseErrorMessage(type, e);
			e.printStackTrace();
			return;
		}
		
		UITreeBuilder.getInstance().getTree().getSelectionModel().select(newTreeItem);
	}
}