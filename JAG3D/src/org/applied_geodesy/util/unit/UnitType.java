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

package org.applied_geodesy.util.unit;

public enum UnitType {
	METER (10),
	MILLIMETER(11),
	MICROMETER(12),
	INCH(13),
	
	UNITLESS(20),
	PARTS_PER_MILLION_WRT_ONE(21),
	PARTS_PER_MILLION_WRT_ZERO(22),
	PERCENT(23),
	
	RADIAN(30),
	DEGREE(31),
	GRADIAN(32),
	MILLIRADIAN(33),
	ARCSECOND(34),
	MILLIGRADIAN(35),
	MIL6400(36),
	DEGREE_SEXAGESIMAL(37),
	
	;

	private int id;
	private UnitType(int id) {
		this.id = id;
	}

	public final int getId() {
		return this.id;
	}
	
	public static UnitType getEnumByValue(int value) {
		for(UnitType element : UnitType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}
