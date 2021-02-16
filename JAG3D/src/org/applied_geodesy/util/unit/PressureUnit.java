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

import java.util.LinkedHashMap;
import java.util.Map;

public class PressureUnit extends Unit {
	public static PressureUnit PASCAL      = new PressureUnit(UnitType.PASCAL);
	public static PressureUnit HECTOPASCAL = new PressureUnit(UnitType.HECTOPASCAL);
	public static PressureUnit KILOPASCAL  = new PressureUnit(UnitType.KILOPASCAL);
	
	public static PressureUnit BAR = new PressureUnit(UnitType.BAR);
	public static PressureUnit MILLIBAR = new PressureUnit(UnitType.MILLIBAR);
	public static PressureUnit TORR = new PressureUnit(UnitType.TORR);
	
	public static final Map<UnitType, PressureUnit> UNITS;
	static {
		UNITS = new LinkedHashMap<UnitType, PressureUnit>();
		UNITS.put(UnitType.PASCAL,      PASCAL);
		UNITS.put(UnitType.HECTOPASCAL, HECTOPASCAL);
		UNITS.put(UnitType.KILOPASCAL,  KILOPASCAL);
		UNITS.put(UnitType.BAR,         BAR);
		UNITS.put(UnitType.MILLIBAR,    MILLIBAR);
		UNITS.put(UnitType.TORR,        TORR);
	}

	private double conversionFactorToHectopascal = 1.0;
	
	private PressureUnit(UnitType type) {
		super(type);
		switch (type) {
		case PASCAL:
			this.name         = i18n.getString("Unit.pascal.name", "Pascal");
			this.abbreviation = i18n.getString("Unit.pascal.abbreviation", "Pa");
			this.conversionFactorToHectopascal = 100.0;
		break;
		case KILOPASCAL:
			this.name         = i18n.getString("Unit.kilopascal.name", "Kilopascal");
			this.abbreviation = i18n.getString("Unit.kilopascal.abbreviation", "kPa");
			this.conversionFactorToHectopascal = 0.1;
		break;
		case HECTOPASCAL:
			this.name         = i18n.getString("Unit.hectopascal.name", "Hectopascal");
			this.abbreviation = i18n.getString("Unit.hectopascal.abbreviation", "hPa");
			this.conversionFactorToHectopascal = 1.0;
		break;
		case BAR:
			this.name         = i18n.getString("Unit.bar.name", "Bar");
			this.abbreviation = i18n.getString("Unit.bar.abbreviation", "bar");
			this.conversionFactorToHectopascal = 1000.0;
		break;
		case MILLIBAR:
			this.name         = i18n.getString("Unit.millibar.name", "Millibar");
			this.abbreviation = i18n.getString("Unit.millibar.abbreviation", "mbar");
			this.conversionFactorToHectopascal = 1.0;
		break;
		case TORR:
			this.name         = i18n.getString("Unit.torr.name", "Torr");
			this.abbreviation = i18n.getString("Unit.torr.abbreviation", "Torr");
			this.conversionFactorToHectopascal = 1013.25/760.0;
		break;
		
		default:
			break;
		}
	}
	
	public final double toHectopascal(double d) {
		return d * this.conversionFactorToHectopascal;
	}
	
	public final double fromHectopascal(double d) {
		return d / this.conversionFactorToHectopascal;
	}
	
	public static PressureUnit getUnit(UnitType type) {
		return UNITS.get(type);
	}
}
