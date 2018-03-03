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

package org.applied_geodesy.util;

import java.util.EventObject;

import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.util.unit.Unit;

public class FormatterEvent extends EventObject {
	private static final long serialVersionUID = 7098232117005493587L;

	private final CellValueType cellType;
	private final FormatterEventType type;
	private final Unit oldUnit, newUnit;
	private final int oldRes, newRes; 
	
	public FormatterEvent(FormatterOptions formatterOptions, FormatterEventType type, CellValueType cellType, Unit oldUnit, Unit newUnit, int res) {
		super(formatterOptions);
		this.cellType = cellType;
		this.type     = type;
		this.oldUnit  = oldUnit;
		this.newUnit  = newUnit;
		this.oldRes   = res;
		this.newRes   = res;
	}
	
	public FormatterEvent(FormatterOptions formatterOptions, FormatterEventType type, CellValueType cellType, Unit unit, int oldRes, int newRes) {
		super(formatterOptions);
		this.cellType = cellType;
		this.type     = type;
		this.oldUnit  = unit;
		this.newUnit  = unit;
		this.oldRes   = oldRes;
		this.newRes   = newRes;
	}
	
	@Override
	public FormatterOptions getSource() {
		return (FormatterOptions)super.getSource();
	}

	public Unit getOldUnit() {
		return this.oldUnit;
	}
	
	public Unit getNewUnit() {
		return this.newUnit;
	}
	
	public int getOldResultion() {
		return this.oldRes;
	}
	
	public int getNewResultion() {
		return this.newRes;
	}
	
	public FormatterEventType getEventType() {
		return this.type;
	}
	
	public CellValueType getCellType() {
		return this.cellType;
	}

	@Override
	public String toString() {
		return "FormatterEvent [cellType=" + cellType + ", type=" + type + ", oldUnit="
				+ oldUnit + ", newUnit=" + newUnit + ", oldRes=" + oldRes
				+ ", newRes=" + newRes + "]";
	}
}
