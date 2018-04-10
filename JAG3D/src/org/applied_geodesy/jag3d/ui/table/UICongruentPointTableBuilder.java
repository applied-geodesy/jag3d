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

import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class UICongruentPointTableBuilder extends UITableBuilder<TerrestrialObservationRow> {

	private static UICongruentPointTableBuilder tableBuilder = new UICongruentPointTableBuilder();
	private boolean isInitialize = false;
	
	private UICongruentPointTableBuilder() {
		super();
	}

	public static UICongruentPointTableBuilder getInstance() {
		tableBuilder.init();
		return tableBuilder;
	}

	private void init() {
		if (this.isInitialize)
			return;

		TableColumn<TerrestrialObservationRow, String> stringColumn = null;
		TableColumn<TerrestrialObservationRow, Double> doubleColumn = null; 

		TableView<TerrestrialObservationRow> table = this.createTable();

		// Point-A
		int columnIndex = table.getColumns().size(); 
		String labelText   = i18n.getString("UICongruentPointTableBuilder.tableheader.point1.name.label", "Point A");
		String tooltipText = i18n.getString("UICongruentPointTableBuilder.tableheader.point1.name.tooltip", "Id of first point A");
		CellValueType cellValueType = CellValueType.STRING;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, TerrestrialObservationRow::startPointNameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true); 
		table.getColumns().add(stringColumn);

		// Point B
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruentPointTableBuilder.tableheader.point2.name.label", "Point B");
		tooltipText = i18n.getString("UICongruentPointTableBuilder.tableheader.point2.name.tooltip", "Id of second point B");
		cellValueType = CellValueType.STRING;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, TerrestrialObservationRow::endPointNameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true);
		table.getColumns().add(stringColumn);

		// Distance between A-B
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UICongruentPointTableBuilder.tableheader.distance.label", "Distance");
		tooltipText = i18n.getString("UICongruentPointTableBuilder.tableheader.distance.tooltip", "Estimated distance between point A and B");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, TerrestrialObservationRow::distanceAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_TERRESTRIAL_OBSERVATION, columnIndex, true);
		table.getColumns().add(doubleColumn);

		this.table = table;
		this.isInitialize = true;
	}

	@Override
	public TerrestrialObservationRow getEmptyRow() {
		return new TerrestrialObservationRow();
	}

	@Override
	void setValue(TerrestrialObservationRow row, int columnIndex, Object oldValue, Object newValue) {}
	
}
