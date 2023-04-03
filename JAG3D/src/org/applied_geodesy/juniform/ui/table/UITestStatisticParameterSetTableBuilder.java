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

import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.FeatureChangeListener;
import org.applied_geodesy.adjustment.geometry.FeatureEvent;
import org.applied_geodesy.adjustment.geometry.FeatureEvent.FeatureEventType;
import org.applied_geodesy.adjustment.statistic.TestStatisticParameterSet;
import org.applied_geodesy.ui.table.ColumnTooltipHeader;
import org.applied_geodesy.ui.table.ColumnType;
import org.applied_geodesy.util.CellValueType;

import javafx.collections.ListChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;


public class UITestStatisticParameterSetTableBuilder extends UIEditableTableBuilder<TestStatisticParameterSet> implements FeatureChangeListener {

	private class TestStatisticParameterSetsListChangeListener implements ListChangeListener<TestStatisticParameterSet> {

		@Override
		public void onChanged(Change<? extends TestStatisticParameterSet> change) {
			try {
				while (change.next()) {
					getTableModel(table).removeAll(change.getRemoved());
					getTableModel(table).addAll(change.getAddedSubList());
				}
			} catch(Exception e) {
				getTableModel(table).clear();
			}
		}	
	}
	
	private static UITestStatisticParameterSetTableBuilder tableBuilder = new UITestStatisticParameterSetTableBuilder();
	private boolean isInitialize = false;
	private final TestStatisticParameterSetsListChangeListener listChangeListener = new TestStatisticParameterSetsListChangeListener();
	
	private UITestStatisticParameterSetTableBuilder() {
		super();
	}

	public static UITestStatisticParameterSetTableBuilder getInstance() {
		tableBuilder.init();
		return tableBuilder;
	}

	private void init() {
		if (this.isInitialize)
			return;
		
		CellValueType cellValueTypeStatistic = CellValueType.STATISTIC;
		CellValueType cellValueTypePercent   = CellValueType.PERCENTAGE;

		TableColumn<TestStatisticParameterSet, Double> doubleColumn   = null;
		TableView<TestStatisticParameterSet> table = this.createTable();

		// Degree of freedom numerator
		int columnIndex = table.getColumns().size(); 
		columnIndex = table.getColumns().size(); 
		String labelText   = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.degree_of_freedom_numerator.label", "d1");
		String tooltipText = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.degree_of_freedom_numerator.tooltip", "Numerator degrees of freedom of F distribution");
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueTypeStatistic, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, TestStatisticParameterSet::numeratorDofProperty, getDoubleCallback(cellValueTypeStatistic), ColumnType.VISIBLE, columnIndex, true);
		table.getColumns().add(doubleColumn);
		
		// Degree of freedom denominator
		columnIndex = table.getColumns().size(); 
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.degree_of_freedom_denominator.label", "d2");
		tooltipText = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.degree_of_freedom_denominator.tooltip", "Denominator degrees of freedom of F distribution");
		header = new ColumnTooltipHeader(cellValueTypeStatistic, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, TestStatisticParameterSet::denominatorDofProperty, getDoubleCallback(cellValueTypeStatistic), ColumnType.VISIBLE, columnIndex, true);
		table.getColumns().add(doubleColumn);
		
		// Probability value
		columnIndex = table.getColumns().size(); 
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.probability_value.label", "\u03B1");
		tooltipText = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.probability_value.tooltip", "Probability value (type I-error)");
		header = new ColumnTooltipHeader(cellValueTypePercent, labelText, tooltipText, options.getFormatterOptions().get(cellValueTypePercent).getUnit());
		doubleColumn = this.<Double>getColumn(header, TestStatisticParameterSet::probabilityValueProperty, getDoubleCallback(cellValueTypePercent), ColumnType.VISIBLE, columnIndex, true);
		table.getColumns().add(doubleColumn);
		
		// Test power
		columnIndex = table.getColumns().size(); 
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.power_of_test.label", "1 - \u03B2");
		tooltipText = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.power_of_test.tooltip", "Power of test (type II-error)");
		header = new ColumnTooltipHeader(cellValueTypePercent, labelText, tooltipText, options.getFormatterOptions().get(cellValueTypePercent).getUnit());
		doubleColumn = this.<Double>getColumn(header, TestStatisticParameterSet::powerOfTestProperty, getDoubleCallback(cellValueTypePercent), ColumnType.VISIBLE, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// noncentrality parameter
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.noncentrality_parameter.label", "\u03BB(\u03B1, \u03B2)");
		tooltipText = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.noncentrality_parameter.tooltip", "Noncentrality Parameter");
		header = new ColumnTooltipHeader(cellValueTypeStatistic, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, TestStatisticParameterSet::noncentralityParameterProperty, getDoubleCallback(cellValueTypeStatistic), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.p_value.label", "log(p)");
		tooltipText = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.p_value.tooltip", "p-value (logarithmic representation)");
		header = new ColumnTooltipHeader(cellValueTypeStatistic, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, TestStatisticParameterSet::logarithmicProbabilityValueProperty, getDoubleCallback(cellValueTypeStatistic), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// Quantile
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.quantile.label", "q");
		tooltipText = i18n.getString("UITestStatisticParameterSetTableBuilder.tableheader.quantile.tooltip", "Quantile of F-distribution");
		header = new ColumnTooltipHeader(cellValueTypeStatistic, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, TestStatisticParameterSet::quantileProperty, getDoubleCallback(cellValueTypeStatistic), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table = table;
		this.isInitialize = true;
	}

	@Override
	void setValue(TestStatisticParameterSet row, int columnIndex, Object oldValue, Object newValue) {}
	
	@Override
	public void featureChanged(FeatureEvent evt) {
		Feature feature = evt.getSource();
		if (feature != null) {
			if (evt.getEventType() == FeatureEventType.FEATURE_ADDED) {
				feature.getTestStatisticParameterSets().addListener(this.listChangeListener);
				this.getTableModel(this.table).setAll(feature.getTestStatisticParameterSets());
			}
			else if (evt.getEventType() == FeatureEventType.FEATURE_REMOVED) {
				feature.getTestStatisticParameterSets().removeListener(this.listChangeListener);
				this.getTableModel(this.table).clear();
			}
		}
	}

}
