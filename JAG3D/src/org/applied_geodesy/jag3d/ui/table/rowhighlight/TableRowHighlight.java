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

package org.applied_geodesy.jag3d.ui.table.rowhighlight;

import org.applied_geodesy.jag3d.ui.table.UICongruenceAnalysisTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIGNSSObservationTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIPointTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UITerrestrialObservationTableBuilder;

import javafx.scene.paint.Color;

public class TableRowHighlight {

	private static TableRowHighlight tableRowHighlight = new TableRowHighlight();
	private Color excellentColor    = DefaultTableRowHighlightValue.getExcellentColor();
	private Color satisfactoryColor = DefaultTableRowHighlightValue.getSatisfactoryColor();
	private Color inadequateColor   = DefaultTableRowHighlightValue.getInadequateColor();
	
	private double leftBoundary = Double.NaN;
	private double rightBoundary = Double.NaN;
	
	private TableRowHighlightType tableRowHighlightType = TableRowHighlightType.NONE;
	
	private TableRowHighlight() {}
	
	public static TableRowHighlight getInstance() {
		return tableRowHighlight;
	}
	
	public void setTableRowHighlightType(TableRowHighlightType tableRowHighlightType) {
		this.tableRowHighlightType = tableRowHighlightType;
	}
	
	public void setRange(double leftBoundary, double rightBoundary) {
		this.leftBoundary  = Math.min(leftBoundary, rightBoundary);
		this.rightBoundary = Math.max(leftBoundary, rightBoundary);
	}
	
	public double getLeftBoundary() {
		return this.leftBoundary;
	}

	public double getRightBoundary() {
		return this.rightBoundary;
	}
	
	public TableRowHighlightType getTableRowHighlightType() {
		return this.tableRowHighlightType;
	}
	
	public void setColor(TableRowHighlightRangeType type, Color color) {
		switch(type) {
		case EXCELLENT:
			this.excellentColor = color;
			break;
			
		case SATISFACTORY:
			this.satisfactoryColor = color;
			break;
			
		case INADEQUATE:
			this.inadequateColor = color;
			break;
			
		case NONE:
			break;
		}
	}
	
	public Color getColor(TableRowHighlightRangeType type) {
		switch(type) {
		case EXCELLENT:
			return this.excellentColor;
			
		case SATISFACTORY:
			return this.satisfactoryColor;

		case INADEQUATE:
			return this.inadequateColor;

		case NONE:
			return Color.TRANSPARENT;
		}
		
		return Color.TRANSPARENT;
	}
	
	public void refreshTables() {
		// Update tables
		UITerrestrialObservationTableBuilder.getInstance().refreshTable();
		UIGNSSObservationTableBuilder.getInstance().refreshTable();
		UIPointTableBuilder.getInstance().refreshTable();
		UICongruenceAnalysisTableBuilder.getInstance().refreshTable();
	}
	
	@Override
	public String toString() {
		return "TableRowHighlight [excellentColor=" + excellentColor + ", satisfactoryColor=" + satisfactoryColor
				+ ", inadequateColor=" + inadequateColor + ", leftBoundary=" + leftBoundary + ", rightBoundary="
				+ rightBoundary + ", tableRowHighlightType=" + tableRowHighlightType + "]";
	}
}
