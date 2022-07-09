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

import java.io.BufferedInputStream;
import java.util.Properties;

import org.applied_geodesy.util.unit.AngleUnit;
import org.applied_geodesy.util.unit.LengthUnit;
import org.applied_geodesy.util.unit.PercentUnit;
import org.applied_geodesy.util.unit.PressureUnit;
import org.applied_geodesy.util.unit.ScaleUnit;
import org.applied_geodesy.util.unit.TemperatureUnit;
import org.applied_geodesy.util.unit.UnitType;

public class DefaultFormatterOption {

	private static final LengthUnit LENGTH_UNIT = LengthUnit.METER;
	private static final AngleUnit  ANGLE_UNIT  = AngleUnit.GRADIAN;
	private static final ScaleUnit  SCALE_UNIT  = ScaleUnit.PARTS_PER_MILLION_WRT_ONE;
	private static final LengthUnit VECTOR_UNIT = LengthUnit.METER;
	
	private static final LengthUnit LENGTH_UNCERTAINTY_UNIT = LengthUnit.MILLIMETER;
	private static final AngleUnit  ANGLE_UNCERTAINTY_UNIT  = AngleUnit.MILLIGRADIAN;
	private static final ScaleUnit  SCALE_UNCERTAINTY_UNIT  = ScaleUnit.PARTS_PER_MILLION_WRT_ZERO;
	private static final LengthUnit VECTOR_UNCERTAINTY_UNIT = LengthUnit.MILLIMETER;
	
	private static final LengthUnit LENGTH_RESIDUAL_UNIT = LengthUnit.MILLIMETER;
	private static final AngleUnit  ANGLE_RESIDUAL_UNIT  = AngleUnit.MILLIGRADIAN;
	private static final ScaleUnit  SCALE_RESIDUAL_UNIT  = ScaleUnit.PARTS_PER_MILLION_WRT_ZERO;
	private static final LengthUnit VECTOR_RESIDUAL_UNIT = LengthUnit.MILLIMETER;
	
	private static final TemperatureUnit TEMPERATURE_UNIT = TemperatureUnit.DEGREE_CELSIUS;
	private static final PressureUnit PRESSURE_UNIT       = PressureUnit.HECTOPASCAL;
	private static final PercentUnit PERCENT_UNIT         = PercentUnit.PERCENT;

	
	private static final int LENGTH_FRACTION_DIGITS = 4;
	private static final int ANGLE_FRACTION_DIGITS  = 5;
	private static final int SCALE_FRACTION_DIGITS  = 2;
	private static final int VECTOR_FRACTION_DIGITS = 7;
	
	private static final int LENGTH_UNCERTAINTY_FRACTION_DIGITS = 1;
	private static final int ANGLE_UNCERTAINTY_FRACTION_DIGITS  = 2;
	private static final int SCALE_UNCERTAINTY_FRACTION_DIGITS  = 1;
	private static final int VECTOR_UNCERTAINTY_FRACTION_DIGITS = 1;
	
	private static final int LENGTH_RESIDUAL_FRACTION_DIGITS = 1;
	private static final int ANGLE_RESIDUAL_FRACTION_DIGITS  = 2;
	private static final int SCALE_RESIDUAL_FRACTION_DIGITS  = 2;
	private static final int VECTOR_RESIDUAL_FRACTION_DIGITS = 2;
	
	private static final int TEMPERATURE_FRACTION_DIGITS = 1;
	private static final int PRESSURE_FRACTION_DIGITS    = 2;
	private static final int PERCENT_FRACTION_DIGITS     = 2;
	
	private static final int STATISTIC_FRACTION_DIGITS = 2;
	private static final int DOUBLE_FRACTION_DIGITS    = 5;
	
	private final static Properties PROPERTIES = new Properties();
	
	static {
		BufferedInputStream bis = null;
		final String path = "properties/formatteroptions.default";
		try {
			if (DefaultFormatterOption.class.getClassLoader().getResourceAsStream(path) != null) {
				bis = new BufferedInputStream(DefaultFormatterOption.class.getClassLoader().getResourceAsStream(path));
				PROPERTIES.load(bis);
			}  
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (bis != null)
					bis.close();  
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private DefaultFormatterOption() {}
	
	public static int getLengthFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("LENGTH_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : LENGTH_FRACTION_DIGITS;
	}
	
	public static int getAngleFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("ANGLE_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : ANGLE_FRACTION_DIGITS;
	}
	
	public static int getScaleFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("SCALE_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : SCALE_FRACTION_DIGITS;
	}
	
	public static int getVectorFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("VECTOR_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : VECTOR_FRACTION_DIGITS;
	}
	
	public static int getLengthUncertaintyFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("LENGTH_UNCERTAINTY_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : LENGTH_UNCERTAINTY_FRACTION_DIGITS;
	}
	
	public static int getAngleUncertaintyFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("ANGLE_UNCERTAINTY_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : ANGLE_UNCERTAINTY_FRACTION_DIGITS;
	}
	
	public static int getScaleUncertaintyFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("SCALE_UNCERTAINTY_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : SCALE_UNCERTAINTY_FRACTION_DIGITS;
	}
	
	public static int getVectorUncertaintyFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("VECTOR_UNCERTAINTY_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : VECTOR_UNCERTAINTY_FRACTION_DIGITS;
	}
	
	public static int getLengthResidualFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("LENGTH_RESIDUAL_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : LENGTH_RESIDUAL_FRACTION_DIGITS;
	}
	
	public static int getAngleResidualFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("ANGLE_RESIDUAL_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : ANGLE_RESIDUAL_FRACTION_DIGITS;
	}
	
	public static int getScaleResidualFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("SCALE_RESIDUAL_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : SCALE_RESIDUAL_FRACTION_DIGITS;
	}
	
	public static int getVectorResidualFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("VECTOR_RESIDUAL_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : VECTOR_RESIDUAL_FRACTION_DIGITS;
	}
	
	public static int getTemperatureFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("TEMPERATURE_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : TEMPERATURE_FRACTION_DIGITS;
	}
	
	public static int getPressureFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("PRESSURE_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : PRESSURE_FRACTION_DIGITS;
	}
	
	public static int getPercentFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("PERCENT_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : PERCENT_FRACTION_DIGITS;
	}
	
	public static int getStatisticFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("STATISTIC_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : STATISTIC_FRACTION_DIGITS;
	}
	
	public static int getDoubleFractionDigits() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("DOUBLE_FRACTION_DIGITS")); } catch (Exception e) {}
		return value >= 0 ? value : DOUBLE_FRACTION_DIGITS;
	}
	
	public static LengthUnit getLengthUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("LENGTH_UNIT"));
			if (unitType != null && LengthUnit.UNITS.containsKey(unitType))
				return LengthUnit.getUnit(unitType);
		} catch (Exception e) {}
		return LENGTH_UNIT;
	}
	
	public static AngleUnit getAngleUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("ANGLE_UNIT"));
			if (unitType != null && AngleUnit.UNITS.containsKey(unitType))
				return AngleUnit.getUnit(unitType);
		} catch (Exception e) {}
		return ANGLE_UNIT;
	}
	
	public static ScaleUnit getScaleUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("SCALE_UNIT"));
			if (unitType != null && ScaleUnit.UNITS.containsKey(unitType))
				return ScaleUnit.getUnit(unitType);
		} catch (Exception e) {}
		return SCALE_UNIT;
	}
	
	public static LengthUnit getVectorUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("VECTOR_UNIT"));
			if (unitType != null && LengthUnit.UNITS.containsKey(unitType))
				return LengthUnit.getUnit(unitType);
		} catch (Exception e) {}
		return VECTOR_UNIT;
	}

	public static LengthUnit getLengthUncertaintyUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("LENGTH_UNCERTAINTY_UNIT"));
			if (unitType != null && LengthUnit.UNITS.containsKey(unitType))
				return LengthUnit.getUnit(unitType);
		} catch (Exception e) {}
		return LENGTH_UNCERTAINTY_UNIT;
	}
	
	public static AngleUnit getAngleUncertaintyUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("ANGLE_UNCERTAINTY_UNIT"));
			if (unitType != null && AngleUnit.UNITS.containsKey(unitType))
				return AngleUnit.getUnit(unitType);
		} catch (Exception e) {}
		return ANGLE_UNCERTAINTY_UNIT;
	}
	
	public static ScaleUnit getScaleUncertaintyUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("SCALE_UNCERTAINTY_UNIT"));
			if (unitType != null && ScaleUnit.UNITS.containsKey(unitType))
				return ScaleUnit.getUnit(unitType);
		} catch (Exception e) {}
		return SCALE_UNCERTAINTY_UNIT;
	}
	
	public static LengthUnit getVectorUncertaintyUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("VECTOR_UNCERTAINTY_UNIT"));
			if (unitType != null && LengthUnit.UNITS.containsKey(unitType))
				return LengthUnit.getUnit(unitType);
		} catch (Exception e) {}
		return VECTOR_UNCERTAINTY_UNIT;
	}
	
	public static LengthUnit getLengthResidualUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("LENGTH_RESIDUAL_UNIT"));
			if (unitType != null && LengthUnit.UNITS.containsKey(unitType))
				return LengthUnit.getUnit(unitType);
		} catch (Exception e) {}
		return LENGTH_RESIDUAL_UNIT;
	}
	
	public static AngleUnit getAngleResidualUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("ANGLE_RESIDUAL_UNIT"));
			if (unitType != null && AngleUnit.UNITS.containsKey(unitType))
				return AngleUnit.getUnit(unitType);
		} catch (Exception e) {}
		return ANGLE_RESIDUAL_UNIT;
	}
	
	public static ScaleUnit getScaleResidualUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("SCALE_RESIDUAL_UNIT"));
			if (unitType != null && ScaleUnit.UNITS.containsKey(unitType))
				return ScaleUnit.getUnit(unitType);
		} catch (Exception e) {}
		return SCALE_RESIDUAL_UNIT;
	}
	
	public static LengthUnit getVectorResidualUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("VECTOR_RESIDUAL_UNIT"));
			if (unitType != null && LengthUnit.UNITS.containsKey(unitType))
				return LengthUnit.getUnit(unitType);
		} catch (Exception e) {}
		return VECTOR_RESIDUAL_UNIT;
	}
	
	public static TemperatureUnit getTemperatureUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("TEMPERATURE_UNIT"));
			if (unitType != null && TemperatureUnit.UNITS.containsKey(unitType))
				return TemperatureUnit.getUnit(unitType);
		} catch (Exception e) {}
		return TEMPERATURE_UNIT;
	}
	
	public static PressureUnit getPressureUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("PRESSURE_UNIT"));
			if (unitType != null && PressureUnit.UNITS.containsKey(unitType))
				return PressureUnit.getUnit(unitType);
		} catch (Exception e) {}
		return PRESSURE_UNIT;
	}

	public static PercentUnit getPercentUnit() {
		try { 
			UnitType unitType = UnitType.valueOf(PROPERTIES.getProperty("PERCENT_UNIT"));
			if (unitType != null && PercentUnit.UNITS.containsKey(unitType))
				return PercentUnit.getUnit(unitType);
		} catch (Exception e) {}
		return PERCENT_UNIT;
	}	
}
