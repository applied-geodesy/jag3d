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

package org.applied_geodesy.jag3d.ui.graphic.layer;

public enum LegendPositionType {
	NORTH(1),
	SOUTH(2),
	EAST(3),
	WEST(4),
	NORTH_EAST(13),
	NORTH_WEST(14),
	SOUTH_EAST(23),
	SOUTH_WEST(24);
	
	private int id;
	private LegendPositionType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static LegendPositionType getEnumByValue(int value) {
		for(LegendPositionType element : LegendPositionType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}
