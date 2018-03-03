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

import java.util.Map;

public class ScaleUnit extends Unit {
	public static ScaleUnit UNITLESS  = new ScaleUnit(UnitType.UNITLESS);
	public static ScaleUnit PARTS_PER_MILLION_WRT_ZERO = new ScaleUnit(UnitType.PARTS_PER_MILLION_WRT_ZERO);
	public static ScaleUnit PARTS_PER_MILLION_WRT_ONE  = new ScaleUnit(UnitType.PARTS_PER_MILLION_WRT_ONE);

	public static final Map<UnitType, ScaleUnit> UNITS = Map.of(
			UnitType.UNITLESS,                   UNITLESS,
			UnitType.PARTS_PER_MILLION_WRT_ZERO, PARTS_PER_MILLION_WRT_ZERO,
			UnitType.PARTS_PER_MILLION_WRT_ONE,  PARTS_PER_MILLION_WRT_ONE
	);
	
	private double a = 0.0, s = 1.0;

	private ScaleUnit(UnitType type) {
		super(type);
		switch (type) {
		case UNITLESS:
			this.name         = i18n.getString("Unit.unitless.name", "Unitless");
			this.abbreviation = i18n.getString("Unit.unitless.abbreviation", "\u2014");
			this.a = 0.0;
			this.s = 1.0;
			break;
		case PARTS_PER_MILLION_WRT_ZERO:
			this.name         = i18n.getString("Unit.ppm.one.name", "Parts per million");
			this.abbreviation = i18n.getString("Unit.ppm.one.abbreviation", "ppm");
			this.a = 0.0; //1.0;
			this.s = 1E-6;
			break;
		case PARTS_PER_MILLION_WRT_ONE:
			this.name         = i18n.getString("Unit.ppm.zero.name", "Parts per million");
			this.abbreviation = i18n.getString("Unit.ppm.zero.abbreviation", "ppm");
			this.a = 1.0;
			this.s = 1E-6;
			break;
		default:
			break;
		}
	}
	
	public final double toUnitless(double d) {
		return this.a + d * this.s;
	}
	
	public final double fromUnitless(double d) {
		return (d - this.a) / this.s;
	}
	
	public static ScaleUnit getUnit(UnitType type) {
		return UNITS.get(type);
	}
}