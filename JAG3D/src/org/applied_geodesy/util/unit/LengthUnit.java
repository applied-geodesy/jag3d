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

public class LengthUnit extends Unit {
	public static LengthUnit METER      = new LengthUnit(UnitType.METER);
	public static LengthUnit MILLIMETER = new LengthUnit(UnitType.MILLIMETER);
	public static LengthUnit MICROMETER = new LengthUnit(UnitType.MICROMETER);
	public static LengthUnit NANOMETER  = new LengthUnit(UnitType.NANOMETER);
	public static LengthUnit INCH       = new LengthUnit(UnitType.INCH);
	public static LengthUnit FOOT       = new LengthUnit(UnitType.FOOT);
	
	public static final Map<UnitType, LengthUnit> UNITS;
	static {
		UNITS = new LinkedHashMap<UnitType, LengthUnit>();
		UNITS.put(UnitType.METER,      METER);
		UNITS.put(UnitType.MILLIMETER, MILLIMETER);
		UNITS.put(UnitType.MICROMETER, MICROMETER);
		UNITS.put(UnitType.NANOMETER,  NANOMETER);
		UNITS.put(UnitType.INCH,       INCH);
		UNITS.put(UnitType.FOOT,       FOOT);
	}
		
	private double conversionFactorToMeter = 1.0;
	
	private LengthUnit(UnitType type) {
		super(type);
		switch (type) {
		case METER:
			this.name         = i18n.getString("Unit.meter.name", "Meter");
			this.abbreviation = i18n.getString("Unit.meter.abbreviation", "m");
			this.conversionFactorToMeter = 1.0;
			break;
		case MILLIMETER:
			this.name         = i18n.getString("Unit.millimeter.name", "Millimeter");
			this.abbreviation = i18n.getString("Unit.millimeter.abbreviation", "mm");
			this.conversionFactorToMeter = 1E-3;
			break;
		case MICROMETER:
			this.name         = i18n.getString("Unit.micrometer.name", "Micrometer");
			this.abbreviation = i18n.getString("Unit.micrometer.abbreviation", "\u03BCm");
			this.conversionFactorToMeter = 1E-6;
			break;
		case NANOMETER:
			this.name         = i18n.getString("Unit.nanometer.name", "Nanometer");
			this.abbreviation = i18n.getString("Unit.nanometer.abbreviation", "nm");
			this.conversionFactorToMeter = 1E-9;
			break;
		case INCH:
			this.name         = i18n.getString("Unit.inch.name", "Inch");
			this.abbreviation = i18n.getString("Unit.inch.abbreviation", "in");
			this.conversionFactorToMeter = 0.0254;
			break;
		case FOOT:
			this.name         = i18n.getString("Unit.foot.name", "Foot");
			this.abbreviation = i18n.getString("Unit.foot.abbreviation", "ft");
			this.conversionFactorToMeter = 0.3048;
			break;
		default:
			break;
		}
	}
	
	public final double toMeter(double d) {
		return d * this.conversionFactorToMeter;
	}
	
	public final double fromMeter(double d) {
		return d / this.conversionFactorToMeter;
	}
	
	public static LengthUnit getUnit(UnitType type) {
		return UNITS.get(type);
	}
}
