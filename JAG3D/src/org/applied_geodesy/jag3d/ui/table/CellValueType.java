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

public enum CellValueType {
	OBJECT(0),
	BOOLEAN(1),
	INTEGER(2),
	DOUBLE(3),
	STRING(4),
	DATE(5),
	IMAGE(6),

	ANGLE(10),
	ANGLE_UNCERTAINTY(11),
	ANGLE_RESIDUAL(12),
	
	LENGTH(20),
	LENGTH_UNCERTAINTY(21),
	LENGTH_RESIDUAL(22),
	
	SCALE(30),
	SCALE_UNCERTAINTY(31),
	SCALE_RESIDUAL(32),

	VECTOR(40),
	VECTOR_UNCERTAINTY(41),
	
	STATISTIC(50),

	;

	private int id;
	private CellValueType(int id) {
		this.id = id;
	}

	public final int getId() {
		return this.id;
	}
	
	public static CellValueType getEnumByValue(int value) {
		for(CellValueType element : CellValueType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}
