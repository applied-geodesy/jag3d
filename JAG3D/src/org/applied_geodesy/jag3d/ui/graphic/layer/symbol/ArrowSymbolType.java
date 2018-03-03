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

package org.applied_geodesy.jag3d.ui.graphic.layer.symbol;

public enum ArrowSymbolType {
	STROKED_TRIANGLE_ARROW(11),
	STROKED_TETRAGON_ARROW(12),
	
	FILLED_TRIANGLE_ARROW(21),
	FILLED_TETRAGON_ARROW(22),
	
	;
	
	private int id;
	private ArrowSymbolType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static ArrowSymbolType getEnumByValue(int value) {
		for(ArrowSymbolType element : ArrowSymbolType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}

