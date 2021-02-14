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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;

public class ContentColumn<S, T> extends TableColumn<S, T> {
	private class WidthChangListener implements ChangeListener<Number> {
		@Override
		public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
			if (newValue != null && columnProperty.getColumnContentType() != ColumnContentType.DEFAULT) {
				columnProperty.setPrefWidth(newValue.doubleValue());
			}
		}
	}
	
	private ColumnProperty columnProperty;
	
	public ContentColumn(ColumnProperty columnProperty) {
		this.columnProperty = columnProperty;
		this.setMinWidth(ColumnProperty.MIN_WIDTH);
		this.setPrefWidth(columnProperty.getPrefWidth());
		
		if (columnProperty.getColumnContentType() != ColumnContentType.DEFAULT) {
			this.prefWidthProperty().bindBidirectional(columnProperty.prefWidthProperty());
			this.widthProperty().addListener(new WidthChangListener());
		}
	}

	public ColumnProperty getColumnProperty() {
		return this.columnProperty;
	}
}
