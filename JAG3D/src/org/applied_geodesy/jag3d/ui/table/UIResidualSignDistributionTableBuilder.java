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

import org.applied_geodesy.adjustment.network.VarianceComponentType;
import org.applied_geodesy.jag3d.ui.table.row.ResidualSignDistributionRow;
import org.applied_geodesy.ui.table.ColumnTooltipHeader;
import org.applied_geodesy.ui.table.ColumnType;
import org.applied_geodesy.ui.table.NaturalOrderComparator;
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

public class UIResidualSignDistributionTableBuilder extends UITableBuilder<ResidualSignDistributionRow> {
	
	private static UIResidualSignDistributionTableBuilder tableBuilder = new UIResidualSignDistributionTableBuilder();
	private boolean isInitialize = false;
	private UIResidualSignDistributionTableBuilder() {
		super();
	}
	
	public static UIResidualSignDistributionTableBuilder getInstance() {
		tableBuilder.init();
		return tableBuilder;
	}

	private void init() {
		if (this.isInitialize)
			return;

		TableColumn<ResidualSignDistributionRow, VarianceComponentType> varianceComponentTypeColumn = null;
		TableColumn<ResidualSignDistributionRow, Boolean> booleanColumn = null;
		TableColumn<ResidualSignDistributionRow, Integer> integerColumn = null; 
		TableColumn<ResidualSignDistributionRow, Double>  doubleColumn  = null; 

		CellValueType cellValueTypeInteger = CellValueType.INTEGER;
		
		TableView<ResidualSignDistributionRow> table = this.createTable();

		// component type
		int columnIndex = table.getColumns().size(); 
		String labelText   = i18n.getString("UIResidualSignDistributionTableBuilder.tableheader.type.label", "Component");
		String tooltipText = i18n.getString("UIResidualSignDistributionTableBuilder.tableheader.type.tooltip", "Type of estimated variance component");
		CellValueType cellValueType = CellValueType.STRING;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		varianceComponentTypeColumn = this.<VarianceComponentType>getColumn(header, ResidualSignDistributionRow::varianceComponentTypeProperty, getVarianceComponentTypeCallback(), ColumnType.VISIBLE, columnIndex, false); 
		varianceComponentTypeColumn.setMinWidth(150);
		varianceComponentTypeColumn.setComparator(new NaturalOrderComparator<VarianceComponentType>());
		table.getColumns().add(varianceComponentTypeColumn);

		// number of observations
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIResidualSignDistributionTableBuilder.tableheader.number_of_observations.label", "n");
		tooltipText = i18n.getString("UIResidualSignDistributionTableBuilder.tableheader.number_of_observations.tooltip", "Number of observations");
		header = new ColumnTooltipHeader(cellValueTypeInteger, labelText, tooltipText);
		integerColumn = this.<Integer>getColumn(header, ResidualSignDistributionRow::numberOfObservationsProperty, getIntegerCallback(), ColumnType.VISIBLE, columnIndex, false);  
		table.getColumns().add(integerColumn);
		
		// redundnacy
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIResidualSignDistributionTableBuilder.tableheader.redundancy.label", "r");
		tooltipText = i18n.getString("UIResidualSignDistributionTableBuilder.tableheader.redundancy.tooltip", "Redundancy");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, ResidualSignDistributionRow::redundancyProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false); 
		table.getColumns().add(doubleColumn);
		
		// number of effective observations
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIResidualSignDistributionTableBuilder.tableheader.number_of_effective_observations.label", "n(r \u003E 0)");
		tooltipText = i18n.getString("UIResidualSignDistributionTableBuilder.tableheader.number_of_effective_observations.tooltip", "Number of effective observations");
		header = new ColumnTooltipHeader(cellValueTypeInteger, labelText, tooltipText);
		integerColumn = this.<Integer>getColumn(header, ResidualSignDistributionRow::numberOfEffectiveObservationsProperty, getIntegerCallback(), ColumnType.VISIBLE, columnIndex, false);  
		table.getColumns().add(integerColumn);
		
		// number of negative residuals
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIResidualSignDistributionTableBuilder.tableheader.number_of_negative_residuals.label", "n(\u03B5 \u003C 0)");
		tooltipText = i18n.getString("UIResidualSignDistributionTableBuilder.tableheader.number_of_negative_residuals.tooltip", "Number of negative residuals");
		header = new ColumnTooltipHeader(cellValueTypeInteger, labelText, tooltipText);
		integerColumn = this.<Integer>getColumn(header, ResidualSignDistributionRow::numberOfNegativeResidualsProperty, getIntegerCallback(), ColumnType.VISIBLE, columnIndex, false);  
		table.getColumns().add(integerColumn);

		// decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexOutlier = columnIndex;
		labelText   = i18n.getString("UIResidualSignDistributionTableBuilder.tableheader.significant.label", "Significant");
		tooltipText = i18n.getString("UIResidualSignDistributionTableBuilder.tableheader.significant.tooltip", "Checked, if number of negative signs is significant w.r.t. positive signs");
		cellValueType = CellValueType.BOOLEAN;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, ResidualSignDistributionRow::significantProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, false); 
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<ResidualSignDistributionRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<ResidualSignDistributionRow, Boolean> param) {
				final TableCellChangeListener<Boolean> significantChangeListener = new TableCellChangeListener<Boolean>(columnIndexOutlier, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isSignificant());
				booleanProp.addListener(significantChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table = table;
		this.isInitialize = true;
	}
	
	private static Callback<TableColumn<ResidualSignDistributionRow, VarianceComponentType>, TableCell<ResidualSignDistributionRow, VarianceComponentType>> getVarianceComponentTypeCallback() {
		return new Callback<TableColumn<ResidualSignDistributionRow, VarianceComponentType>, TableCell<ResidualSignDistributionRow, VarianceComponentType>>() {
			@Override
			public TableCell<ResidualSignDistributionRow, VarianceComponentType> call(TableColumn<ResidualSignDistributionRow, VarianceComponentType> cell) {
				TableCell<ResidualSignDistributionRow, VarianceComponentType> tableCell = new TextFieldTableCell<ResidualSignDistributionRow, VarianceComponentType>(
						new StringConverter<VarianceComponentType>() {

							@Override
							public String toString(VarianceComponentType type) {
								if (type == null)
									return null;

								return UIVarianceComponentTableBuilder.getVarianceComponentTypeLabel(type);
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
	
	@Override
	void setValue(ResidualSignDistributionRow row, int columnIndex, Object oldValue, Object newValue) {}
	
	@Override
	public ResidualSignDistributionRow getEmptyRow() {
		return new ResidualSignDistributionRow();
	}
}
