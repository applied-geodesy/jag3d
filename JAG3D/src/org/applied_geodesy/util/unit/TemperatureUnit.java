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

public class TemperatureUnit extends Unit {
	public static TemperatureUnit DEGREE_CELSIUS  = new TemperatureUnit(UnitType.DEGREE_CELSIUS);
	public static TemperatureUnit KELVIN = new TemperatureUnit(UnitType.KELVIN);
	public static TemperatureUnit DEGREE_FAHRENHEIT  = new TemperatureUnit(UnitType.DEGREE_FAHRENHEIT);
	
	public static final Map<UnitType, TemperatureUnit> UNITS;
	static {
		UNITS = new LinkedHashMap<UnitType, TemperatureUnit>();
		UNITS.put(UnitType.DEGREE_CELSIUS,     DEGREE_CELSIUS);
		UNITS.put(UnitType.KELVIN,             KELVIN);
		UNITS.put(UnitType.DEGREE_FAHRENHEIT,  DEGREE_FAHRENHEIT);
	}

	private double zeroPointOffsetToDegreeCelsius  = 0.0;
	private double conversionFactorToDegreeCelsius = 1.0;
	
	private TemperatureUnit(UnitType type) {
		super(type);
		switch (type) {
		case DEGREE_CELSIUS:
			this.name         = i18n.getString("Unit.degree_celsius.name", "Degree Celsius");
			this.abbreviation = i18n.getString("Unit.degree_celsius.abbreviation", "\u2103");
			this.zeroPointOffsetToDegreeCelsius  = 0.0;
			this.conversionFactorToDegreeCelsius = 1.0;
		break;
		case DEGREE_FAHRENHEIT:
			this.name         = i18n.getString("Unit.degree_fahrenheit.name", "Degree Fahrenheit");
			this.abbreviation = i18n.getString("Unit.degree_fahrenheit.abbreviation", "\u2109");
			this.zeroPointOffsetToDegreeCelsius  = 32.0;
			this.conversionFactorToDegreeCelsius = 5.0/9.0;
		break;
		case KELVIN:
			this.name         = i18n.getString("Unit.kelvin.name", "Kelvin");
			this.abbreviation = i18n.getString("Unit.kelvin.abbreviation", "K");
			this.zeroPointOffsetToDegreeCelsius  = 273.15;
			this.conversionFactorToDegreeCelsius = 1.0;
		break;
		
		default:
			break;
		}
	}
	
	public final double toDegreeCelsius(double d) {
		return (d - this.zeroPointOffsetToDegreeCelsius) * this.conversionFactorToDegreeCelsius;
	}
	
	public final double fromDegreeCelsius(double d) {
		return d / this.conversionFactorToDegreeCelsius + this.zeroPointOffsetToDegreeCelsius;
	}
	
	public static TemperatureUnit getUnit(UnitType type) {
		return UNITS.get(type);
	}
}