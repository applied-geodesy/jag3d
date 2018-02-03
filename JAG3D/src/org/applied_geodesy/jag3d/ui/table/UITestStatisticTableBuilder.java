package org.applied_geodesy.jag3d.ui.table;

import org.applied_geodesy.jag3d.ui.table.row.TestStatisticRow;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class UITestStatisticTableBuilder extends UITableBuilder<TestStatisticRow> {

	private static UITestStatisticTableBuilder tableBuilder = new UITestStatisticTableBuilder();
	private boolean isInitialize = false;
	private UITestStatisticTableBuilder() {
		super();
	}
	
	public static UITestStatisticTableBuilder getInstance() {
		tableBuilder.init();
		return tableBuilder;
	}

	private void init() {
		if (this.isInitialize)
			return;
		
		TableView<TestStatisticRow> table = this.createTable();
		
		TableColumn<TestStatisticRow, Double> doubleColumn = null; 
		CellValueType cellValueType = CellValueType.STATISTIC;
		
		double minColumnWidth = 50;
		double prefColumnWidth = 100;
		
		// Degree of freedom numerator
		int columnIndex = table.getColumns().size(); 
		String labelText   = i18n.getString("UITestStatisticTableBuilder.tableheader.degree_of_freedom_numerator.label", "d1");
		String tooltipText = i18n.getString("UITestStatisticTableBuilder.tableheader.degree_of_freedom_numerator.tooltip", "Numerator degree of freedom");
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, TestStatisticRow::numeratorDegreeOfFreedomProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false);
		doubleColumn.setMinWidth(minColumnWidth);
		doubleColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(doubleColumn);

		// Degree of freedom denominator
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITestStatisticTableBuilder.tableheader.degree_of_freedom_denominator.label", "d2");
		tooltipText = i18n.getString("UITestStatisticTableBuilder.tableheader.degree_of_freedom_denominator.tooltip", "Denominator degree of freedom");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, TestStatisticRow::denominatorDegreeOfFreedomProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false);
		doubleColumn.setMinWidth(minColumnWidth);
		doubleColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(doubleColumn);
	
		// Probability value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITestStatisticTableBuilder.tableheader.probability_value.label", "\u03B1 [%]");
		tooltipText = i18n.getString("UITestStatisticTableBuilder.tableheader.probability_value.tooltip", "Probability value (type I-error)");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, TestStatisticRow::probabilityValueProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false);
		doubleColumn.setMinWidth(minColumnWidth);
		doubleColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(doubleColumn);

		// test power
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITestStatisticTableBuilder.tableheader.power_of_test.label", "\u03B2 [%]");
		tooltipText = i18n.getString("UITestStatisticTableBuilder.tableheader.power_of_test.tooltip", "Power of test (type II-error)");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, TestStatisticRow::powerOfTestProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false);
		doubleColumn.setMinWidth(minColumnWidth);
		doubleColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(doubleColumn);

		// noncentrality parameter
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITestStatisticTableBuilder.tableheader.noncentrality_parameter.label", "\u03BB(\u03B1, \u03B2)");
		tooltipText = i18n.getString("UITestStatisticTableBuilder.tableheader.noncentrality_parameter.tooltip", "Noncentrality Parameter");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, TestStatisticRow::noncentralityParameterProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false);
		doubleColumn.setMinWidth(minColumnWidth);
		doubleColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(doubleColumn);

		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITestStatisticTableBuilder.tableheader.p_value.label", "log(p)");
		tooltipText = i18n.getString("UITestStatisticTableBuilder.tableheader.p_value.tooltip", "p-value (logarithmic representation)");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, TestStatisticRow::pValueProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false);
		doubleColumn.setMinWidth(minColumnWidth);
		doubleColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(doubleColumn);

		// Quantile
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITestStatisticTableBuilder.tableheader.quantile.label", "q");
		tooltipText = i18n.getString("UITestStatisticTableBuilder.tableheader.quantile.tooltip", "Quantile of F-distribution");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, TestStatisticRow::quantileProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false);
		doubleColumn.setMinWidth(minColumnWidth);
		doubleColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(doubleColumn);
		
		this.table = table;
		this.isInitialize = true;
	}

	@Override
	void setValue(TestStatisticRow row, int columnIndex, int rowIndex, Object oldValue, Object newValue) {}
	
	@Override
	public TestStatisticRow getEmptyRow() {
		return new TestStatisticRow();
	}
}
