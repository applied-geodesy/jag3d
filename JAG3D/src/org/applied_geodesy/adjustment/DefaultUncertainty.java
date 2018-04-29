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

package org.applied_geodesy.adjustment;

import java.io.BufferedInputStream;
import java.util.Properties;

public final class DefaultUncertainty {
	private static final double ANGLE_ZERO_POINT_OFFSET                 = 0.0003 * Constant.RHO_GRAD2RAD;
	private static final double ANGLE_SQUARE_ROOT_DISTANCE_DEPENDENT    = 0.0;
	private static final double ANGLE_DISTANCE_DEPENDENT                = 0.0005;

	private static final double DISTANCE_ZERO_POINT_OFFSET              = 0.002;
	private static final double DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT = 0.0;
	private static final double DISTANCE_DISTANCE_DEPENDENT             = 0.000002;
	
	private static final double LEVELING_ZERO_POINT_OFFSET              = 0.0001;
	private static final double LEVELING_SQUARE_ROOT_DISTANCE_DEPENDENT = 0.001;
	private static final double LEVELING_DISTANCE_DEPENDENT             = 0.0;
	
	private static final double GNSS_ZERO_POINT_OFFSET                  = 0.025;
	private static final double GNSS_SQUARE_ROOT_DISTANCE_DEPENDENT     = 0.0;
	private static final double GNSS_DISTANCE_DEPENDENT                 = 0.000050;
	
	private static final double X            = 0.002;
	private static final double Y            = 0.002;
	private static final double Z            = 0.002;
	
	private static final double DEFLECTION_X = 0.001 * Constant.RHO_GRAD2RAD;
	private static final double DEFLECTION_Y = 0.001 * Constant.RHO_GRAD2RAD;
	
	private final static Properties PROPERTIES = new Properties();
	
	static {
		BufferedInputStream bis = null;
		final String path = "/properties/uncertainties.default";
		try {
			if (DefaultUncertainty.class.getResource(path) != null) {
				bis = new BufferedInputStream(DefaultUncertainty.class.getResourceAsStream(path));
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

	private DefaultUncertainty() {}

	public static double getUncertaintyAngleZeroPointOffset() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("ANGLE_ZERO_POINT_OFFSET")); } catch (Exception e) {}
		return value > 0 ? value : ANGLE_ZERO_POINT_OFFSET;
	}
	public static double getUncertaintyAngleSquareRootDistanceDependent() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("ANGLE_SQUARE_ROOT_DISTANCE_DEPENDENT")); } catch (Exception e) {}
		return value >= 0 ? value : ANGLE_SQUARE_ROOT_DISTANCE_DEPENDENT;
	}
	public static double getUncertaintyAngleDistanceDependent() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("ANGLE_DISTANCE_DEPENDENT")); } catch (Exception e) {}
		return value >= 0 ? value : ANGLE_DISTANCE_DEPENDENT;
	}
	public static double getUncertaintyDistanceZeroPointOffset() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("DISTANCE_ZERO_POINT_OFFSET")); } catch (Exception e) {}
		return value > 0 ? value : DISTANCE_ZERO_POINT_OFFSET;
	}
	public static double getUncertaintyDistanceSquareRootDistanceDependent() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT")); } catch (Exception e) {}
		return value >= 0 ? value : DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT;
	}
	public static double getUncertaintyDistanceDistanceDependent() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("DISTANCE_DISTANCE_DEPENDENT")); } catch (Exception e) {}
		return value >= 0 ? value : DISTANCE_DISTANCE_DEPENDENT;
	}
	public static double getUncertaintyLevelingZeroPointOffset() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("LEVELING_ZERO_POINT_OFFSET")); } catch (Exception e) {}
		return value > 0 ? value : LEVELING_ZERO_POINT_OFFSET;
	}
	public static double getUncertaintyLevelingSquareRootDistanceDependent() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("LEVELING_SQUARE_ROOT_DISTANCE_DEPENDENT")); } catch (Exception e) {}
		return value >= 0 ? value : LEVELING_SQUARE_ROOT_DISTANCE_DEPENDENT;
	}
	public static double getUncertaintyLevelingDistanceDependent() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("LEVELING_DISTANCE_DEPENDENT")); } catch (Exception e) {}
		return value >= 0 ? value : LEVELING_DISTANCE_DEPENDENT;
	}
	public static double getUncertaintyGNSSZeroPointOffset() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("GNSS_ZERO_POINT_OFFSET")); } catch (Exception e) {}
		return value > 0 ? value : GNSS_ZERO_POINT_OFFSET;
	}
	public static double getUncertaintyGNSSSquareRootDistanceDependent() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("GNSS_SQUARE_ROOT_DISTANCE_DEPENDENT")); } catch (Exception e) {}
		return value >= 0 ? value : GNSS_SQUARE_ROOT_DISTANCE_DEPENDENT;
	}
	public static double getUncertaintyGNSSDistanceDependent() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("GNSS_DISTANCE_DEPENDENT")); } catch (Exception e) {}
		return value >= 0 ? value : GNSS_DISTANCE_DEPENDENT;
	}
	public static double getUncertaintyX() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("X")); } catch (Exception e) {}
		return value >= 0 ? value : X;
	}
	public static double getUncertaintyY() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("Y")); } catch (Exception e) {}
		return value >= 0 ? value : Y;
	}
	public static double getUncertaintyZ() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("Z")); } catch (Exception e) {}
		return value >= 0 ? value : Z;
	}
	public static double getUncertaintyDeflectionX() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("DEFLECTION_X")); } catch (Exception e) {}
		return value >= 0 ? value : DEFLECTION_X;
	}
	public static double getUncertaintyDeflectionY() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("DEFLECTION_Y")); } catch (Exception e) {}
		return value >= 0 ? value : DEFLECTION_Y;
	}
}
