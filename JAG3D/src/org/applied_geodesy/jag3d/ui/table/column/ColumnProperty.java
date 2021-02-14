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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class ColumnProperty {
	public final static double PREFERRED_WIDTH = 125;
	public final static double MIN_WIDTH       = 50;
	
	private DoubleProperty prefWidth    = new SimpleDoubleProperty(this, "prefWidth", PREFERRED_WIDTH);
	
	private final ColumnContentType columnContentType;
	
	ColumnProperty(ColumnContentType columnContentType) {
		this.columnContentType = columnContentType;
		double prefWidth = DefaultColumnProperty.getPrefWidth(this.columnContentType);
		this.setPrefWidth(prefWidth);
	}
	
	public ColumnContentType getColumnContentType() {
		return this.columnContentType;
	}
	
	public DoubleProperty prefWidthProperty() {
		return this.prefWidth;
	}
	
	public Double getPrefWidth() {
		return this.prefWidth.get();
	}
	
	public void setPrefWidth(double prefWidth) {
		this.prefWidth.set(Math.max(prefWidth, MIN_WIDTH));
	}
	
	@Override
	public String toString() {
		return "ColumnProperty [columnContentType="	+ columnContentType + ", prefWidth=" + prefWidth + "]";
	}
}
