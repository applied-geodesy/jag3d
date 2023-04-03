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

import org.applied_geodesy.adjustment.geometry.FeatureChangeListener;
import org.applied_geodesy.adjustment.geometry.FeatureEvent;
import org.applied_geodesy.adjustment.geometry.VarianceComponent;
import org.applied_geodesy.ui.table.ColumnTooltipHeader;
import org.applied_geodesy.ui.table.ColumnType;
import org.applied_geodesy.util.CellValueType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

public class UIVarianceComponentTableBuilder extends UIEditableTableBuilder<VarianceComponent> implements FeatureChangeListener {
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

		TableColumn<VarianceComponent, Boolean> booleanColumn = null;
		TableColumn<VarianceComponent, Double> doubleColumn   = null;
		TableColumn<VarianceComponent, Integer> integerColumn = null; 

		TableView<VarianceComponent> table = this.createTable();

		// number of observations
		int columnIndex = table.getColumns().size(); 
		String labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.number_of_observations.label", "n");
		String tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.number_of_observations.tooltip", "Number of observations");
		CellValueType cellValueType = CellValueType.INTEGER;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		integerColumn = this.<Integer>getColumn(header, VarianceComponent::numberOfModelEquationsProperty, getIntegerCallback(), ColumnType.VISIBLE, columnIndex, false); 
		table.getColumns().add(integerColumn);

		// redundnacy
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.redundancy.label", "r");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.redundancy.tooltip", "Redundancy");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, VarianceComponent::redundancyProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false); 
		table.getColumns().add(doubleColumn);

		// Omega
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.omega.label", "\u03A9");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.omega.tooltip", "Squared weigthed residuals");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, VarianceComponent::omegaProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false); 
		table.getColumns().add(doubleColumn);

		// Sigma a-posteriori
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.variance.label", "1 : \u03C3\u00B2");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.variance.tooltip", "A-posteriori variance factor w.r.t. a-priori variance");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, VarianceComponent::varianceProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false); 
		table.getColumns().add(doubleColumn);

		// Decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexOutlier = columnIndex;
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.significant.label", "Significant");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.significant.tooltip", "Checked, if a-posteriori variance is significant w.r.t. a-priori variance");
		cellValueType = CellValueType.BOOLEAN;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, VarianceComponent::significantProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, false);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<VarianceComponent, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<VarianceComponent, Boolean> param) {
				final TableCellChangeListener<Boolean> significantChangeListener = new TableCellChangeListener<Boolean>(columnIndexOutlier, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isSignificant());
				booleanProp.addListener(significantChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		this.table = table;

	}
	
	@Override
	void setValue(VarianceComponent row, int columnIndex, Object oldValue, Object newValue) {}

	@Override
	public void featureChanged(FeatureEvent evt) {
		// TODO Auto-generated method stub
		
	}
}
