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

import org.applied_geodesy.jag3d.ui.table.row.PrincipalComponentRow;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class UIPrincipalComponentTableBuilder extends UITableBuilder<PrincipalComponentRow> {
	private static UIPrincipalComponentTableBuilder tableBuilder = new UIPrincipalComponentTableBuilder();
	private boolean isInitialize = false;
	private UIPrincipalComponentTableBuilder() {
		super();
	}
	
	public static UIPrincipalComponentTableBuilder getInstance() {
		tableBuilder.init();
		return tableBuilder;
	}

	private void init() {
		if (this.isInitialize)
			return;
		
		TableView<PrincipalComponentRow> table = this.createTable();
		
		TableColumn<PrincipalComponentRow, Integer> integerColumn = null; 
		TableColumn<PrincipalComponentRow, Double>  doubleColumn  = null; 
		
		double minColumnWidth  =  50;
		double prefColumnWidth = 100;
		
		// Index of eigenvalue
		CellValueType cellValueType = CellValueType.INTEGER;
		int columnIndex = table.getColumns().size(); 
		String labelText   = i18n.getString("UIPrincipalComponentTableBuilder.tableheader.index.label", "k");
		String tooltipText = i18n.getString("UIPrincipalComponentTableBuilder.tableheader.index.tooltip", "k-te index of eigenvalue");
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		integerColumn = this.<Integer>getColumn(header, PrincipalComponentRow::indexProperty, getIntegerCallback(), ColumnType.VISIBLE, columnIndex, false); 
		integerColumn.setMinWidth(minColumnWidth);
		integerColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(integerColumn);

		// Eigenvalue
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPrincipalComponentTableBuilder.tableheader.square_root_eigenvalue.label", "\u221A\u03BB(k)");
		tooltipText = i18n.getString("UIPrincipalComponentTableBuilder.tableheader.square_root_eigenvalue.tooltip", "Square-root eigenvalue of covariance matrix");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, PrincipalComponentRow::valueProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false);
		doubleColumn.setMinWidth(minColumnWidth);
		doubleColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(doubleColumn);
	
		// ratio eigenvalue vs. trace(Cxx)
		cellValueType = CellValueType.STATISTIC;
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPrincipalComponentTableBuilder.tableheader.ratio.label", "\u03BB(k)/trace(Cxx)");
		tooltipText = i18n.getString("UIPrincipalComponentTableBuilder.tableheader.ratio.tooltip", "Ratio of eigenvalue \u03BB(k) w.r.t. trace of variance-covariance-matrix");
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, PrincipalComponentRow::ratioProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false);
		doubleColumn.setMinWidth(minColumnWidth);
		doubleColumn.setPrefWidth(prefColumnWidth);
		table.getColumns().add(doubleColumn);
		
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table = table;
		this.isInitialize = true;
	}

	@Override
	void setValue(PrincipalComponentRow row, int columnIndex, Object oldValue, Object newValue) {}
	
	@Override
	public PrincipalComponentRow getEmptyRow() {
		return new PrincipalComponentRow();
	}
}
