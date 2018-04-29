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

import org.applied_geodesy.adjustment.network.ObservationType;

public class DefaultAverageThreshold {
	private static final double LEVELING            = DefaultUncertainty.getUncertaintyLevelingZeroPointOffset() * 10.0;
	
	private static final double DIRECTION           = DefaultUncertainty.getUncertaintyAngleZeroPointOffset() * 10.0;
	private static final double ZENITH_ANGLE        = DefaultUncertainty.getUncertaintyAngleZeroPointOffset() * 10.0;
	
	private static final double HORIZONTAL_DISTANCE = DefaultUncertainty.getUncertaintyDistanceZeroPointOffset() * 10.0;
	private static final double SLOPE_DISTANCE      = DefaultUncertainty.getUncertaintyDistanceZeroPointOffset() * 10.0;

	private static final double GNSS1D              = DefaultUncertainty.getUncertaintyGNSSZeroPointOffset() * 10.0;
	private static final double GNSS2D              = DefaultUncertainty.getUncertaintyGNSSZeroPointOffset() * 10.0;
	private static final double GNSS3D              = DefaultUncertainty.getUncertaintyGNSSZeroPointOffset() * 10.0;
	
	private final static Properties PROPERTIES = new Properties();
	
	static {
		BufferedInputStream bis = null;
		final String path = "/properties/averagethresholds.default";
		try {
			if (DefaultAverageThreshold.class.getResource(path) != null) {
				bis = new BufferedInputStream(DefaultAverageThreshold.class.getResourceAsStream(path));
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

	private DefaultAverageThreshold() {}

	public static double getThresholdLeveling() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("LEVELING")); } catch (Exception e) {}
		return value > 0 ? value : LEVELING;
	}
	
	public static double getThresholdDirection() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("DIRECTION")); } catch (Exception e) {}
		return value > 0 ? value : DIRECTION;
	}
	
	public static double getThresholdZenithAngle() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("ZENITH_ANGLE")); } catch (Exception e) {}
		return value > 0 ? value : ZENITH_ANGLE;
	}
	
	public static double getThresholdHorizontalDistance() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("HORIZONTAL_DISTANCE")); } catch (Exception e) {}
		return value > 0 ? value : HORIZONTAL_DISTANCE;
	}
	
	public static double getThresholdSlopeDistance() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("SLOPE_DISTANCE")); } catch (Exception e) {}
		return value > 0 ? value : SLOPE_DISTANCE;
	}
	
	public static double getThresholdGNSSBaseline1D() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("GNSS1D")); } catch (Exception e) {}
		return value > 0 ? value : GNSS1D;
	}
	
	public static double getThresholdGNSSBaseline2D() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("GNSS2D")); } catch (Exception e) {}
		return value > 0 ? value : GNSS2D;
	}
	
	public static double getThresholdGNSSBaseline3D() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("GNSS3D")); } catch (Exception e) {}
		return value > 0 ? value : GNSS3D;
	}
	
	public static double getThreshold(ObservationType type) {
		switch(type) {
		case LEVELING:
			return getThresholdLeveling();
		case DIRECTION:
			return getThresholdDirection();
		case HORIZONTAL_DISTANCE:
			return getThresholdHorizontalDistance();
		case SLOPE_DISTANCE:
			return getThresholdSlopeDistance();
		case ZENITH_ANGLE:
			return getThresholdZenithAngle();
		case GNSS1D:
			return getThresholdGNSSBaseline1D();
		case GNSS2D:
			return getThresholdGNSSBaseline2D();
		case GNSS3D:
			return getThresholdGNSSBaseline3D();
		}
		return 0;
	}
}
