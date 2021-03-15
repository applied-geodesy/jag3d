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

package org.applied_geodesy.jag3d.ui.table.column;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class ColumnPropertiesManager implements Iterable<Map.Entry<Pair<TableContentType, ColumnContentType>, ColumnProperty>>  {
	private static ColumnPropertiesManager columnPropertiesManager = new ColumnPropertiesManager();
	private Map<Pair<TableContentType, ColumnContentType>, ColumnProperty> properties = new HashMap<Pair<TableContentType, ColumnContentType>, ColumnProperty>();
	private Map<TableContentType, ObservableList<ColumnContentType>> tableSortOrder = new HashMap<TableContentType, ObservableList<ColumnContentType>>();
	private Map<TableContentType, ObservableList<ColumnContentType>> tableColumnsOrder = new HashMap<TableContentType, ObservableList<ColumnContentType>>();
	
	private ColumnPropertiesManager() {}
	
	public static ColumnPropertiesManager getInstance() {
		return columnPropertiesManager;
	}
	
	public ColumnProperty getProperty(TableContentType tableType, ColumnContentType columnType) {
		Pair<TableContentType, ColumnContentType> key = new Pair<TableContentType, ColumnContentType>(tableType, columnType);
		
		if (!this.properties.containsKey(key)) {
			ColumnProperty property = new ColumnProperty(columnType);
			this.properties.put(key, property);
		}

		return this.properties.get(key);
	}

	@Override
	public Iterator<Map.Entry<Pair<TableContentType, ColumnContentType>, ColumnProperty>> iterator() {
		return this.properties.entrySet().iterator();
	}
	
	public void clearOrder() {
		for (ObservableList<ColumnContentType> columnTypes : this.tableColumnsOrder.values())
			columnTypes.clear();
		for (ObservableList<ColumnContentType> columnTypes : this.tableSortOrder.values())
			columnTypes.clear();
	}
	
	public ObservableList<ColumnContentType> getSortOrder(TableContentType tableType) {
		if (!this.tableSortOrder.containsKey(tableType)) {
			ObservableList<ColumnContentType> columnTypes = FXCollections.observableArrayList();
			this.tableSortOrder.put(tableType, columnTypes);
		}
		return this.tableSortOrder.get(tableType);
	}
	
	public ObservableList<ColumnContentType> getColumnsOrder(TableContentType tableType) {
		if (!this.tableColumnsOrder.containsKey(tableType)) {
			ObservableList<ColumnContentType> columnTypes = FXCollections.observableArrayList();
			this.tableColumnsOrder.put(tableType, columnTypes);
		}
		return this.tableColumnsOrder.get(tableType);
	}
}
