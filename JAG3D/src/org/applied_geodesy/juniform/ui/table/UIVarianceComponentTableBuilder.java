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

import org.applied_geodesy.adjustment.geometry.VarianceComponent;
import org.applied_geodesy.adjustment.geometry.VarianceComponentType;
import org.applied_geodesy.ui.table.ColumnTooltipHeader;
import org.applied_geodesy.ui.table.ColumnType;
import org.applied_geodesy.util.CellValueType;

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

public class UIVarianceComponentTableBuilder extends UITableBuilder<VarianceComponent> {
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

		TableColumn<VarianceComponent, VarianceComponentType> varianceComponentTypeColumn = null;
		TableColumn<VarianceComponent, Boolean> booleanColumn = null;
		TableColumn<VarianceComponent, Double> doubleColumn   = null;
		TableColumn<VarianceComponent, Integer> integerColumn = null; 

		TableView<VarianceComponent> table = this.createTable();
		
		double columWidth = 85;
		// type of component
		int columnIndex    = table.getColumns().size(); 
		String labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.type.label", "Component");
		String tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.type.tooltip", "Type of estimated variance component");
		CellValueType cellValueType = CellValueType.STRING;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		varianceComponentTypeColumn = this.<VarianceComponentType>getColumn(header, VarianceComponent::varianceComponentTypeProperty, getVarianceComponentTypeCallback(), ColumnType.VISIBLE, columnIndex, false); 
		varianceComponentTypeColumn.setMinWidth(150);
		varianceComponentTypeColumn.setSortable(false);
		table.getColumns().add(varianceComponentTypeColumn);

		// number of observations
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.number_of_observations.label", "n");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.number_of_observations.tooltip", "Number of equations");
		cellValueType = CellValueType.INTEGER;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		integerColumn = this.<Integer>getColumn(header, VarianceComponent::numberOfObservationsProperty, getIntegerCallback(), ColumnType.VISIBLE, columnIndex, false); 
		integerColumn.setPrefWidth(columWidth);
		integerColumn.setSortable(false);
		table.getColumns().add(integerColumn);

		// redundnacy
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.redundancy.label", "r");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.redundancy.tooltip", "Redundancy");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, VarianceComponent::redundancyProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false); 
		doubleColumn.setPrefWidth(columWidth);
		doubleColumn.setSortable(false);
		table.getColumns().add(doubleColumn);

		// Omega
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.omega.label", "\u03A9");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.omega.tooltip", "Squared weigthed residuals");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, VarianceComponent::unitOmegaProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false); 
		doubleColumn.setPrefWidth(columWidth);
		doubleColumn.setSortable(false);
		table.getColumns().add(doubleColumn);

		// Sigma a-posteriori
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIVarianceComponentTableBuilder.tableheader.variance.label", "1 : \u03C3\u00B2");
		tooltipText = i18n.getString("UIVarianceComponentTableBuilder.tableheader.variance.tooltip", "A-posteriori variance factor w.r.t. a-priori variance");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, VarianceComponent::unitVarianceProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false); 
		doubleColumn.setPrefWidth(columWidth);
		doubleColumn.setSortable(false);
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
		booleanColumn.setSortable(false);
		table.getColumns().add(booleanColumn);

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		this.table = table;
		this.isInitialize = true;
	}
	
	private static Callback<TableColumn<VarianceComponent, VarianceComponentType>, TableCell<VarianceComponent, VarianceComponentType>> getVarianceComponentTypeCallback() {
		return new Callback<TableColumn<VarianceComponent, VarianceComponentType>, TableCell<VarianceComponent, VarianceComponentType>>() {
			@Override
			public TableCell<VarianceComponent, VarianceComponentType> call(TableColumn<VarianceComponent, VarianceComponentType> cell) {
				TableCell<VarianceComponent, VarianceComponentType> tableCell = new TextFieldTableCell<VarianceComponent, VarianceComponentType>(
						new StringConverter<VarianceComponentType>() {

							@Override
							public String toString(VarianceComponentType type) {
								if (type == null)
									return null;

								return getVarianceComponentTypeLabel(type);
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
	
	public static final String getVarianceComponentTypeLabel(VarianceComponentType type) {
		switch (type) {
		case GLOBAL:
			return i18n.getString("UIVarianceComponentTableBuilder.type.global.label", "Global adjustment");
		}
		return null;
	}
	
	@Override
	void setValue(VarianceComponent row, int columnIndex, Object oldValue, Object newValue) {}
}
