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

import org.applied_geodesy.adjustment.statistic.DefaultTestStatisticValue;

public class DefaultValue {
	private final static int MAXIMUM_ITERATIONS                = 5000;
	private final static double ROBUST_ESTIMATION_LIMIT        = 3.5;
	private final static double CONFIDENCE_LEVEL               = 1.0 - DefaultTestStatisticValue.getProbabilityValue();
	private final static EstimationType ESTIMATION_TYPE        = EstimationType.L2NORM;
	private final static boolean APPLY_VARIANCE_OF_UNIT_WEIGHT = Boolean.TRUE;

	private final static Properties PROPERTIES = new Properties();
	
	static {
		BufferedInputStream bis = null;
		final String path = "properties/leastsquares.default";
		try {
			if (DefaultValue.class.getClassLoader().getResourceAsStream(path) != null) {
				bis = new BufferedInputStream(DefaultValue.class.getClassLoader().getResourceAsStream(path));
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
	
	private DefaultValue() {}

	public static EstimationType getEstimationType() {
		EstimationType value = null;
		try { value = EstimationType.valueOf(PROPERTIES.getProperty("ESTIMATION_TYPE")); } catch (Exception e) {}
		return value != null ? value : ESTIMATION_TYPE;
	}
	
	public static int getMaximumNumberOfIterations() {
		int value = -1;
		try { value = Integer.parseInt(PROPERTIES.getProperty("MAXIMUM_ITERATIONS")); } catch (Exception e) {}
		return value > 0 ? value : MAXIMUM_ITERATIONS;
	}
	
	public static double getRobustEstimationLimit() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("ROBUST_ESTIMATION_LIMIT")); } catch (Exception e) {}
		return value > 0 ? value : ROBUST_ESTIMATION_LIMIT;
	}
	
	public static boolean applyVarianceOfUnitWeight() {
		boolean value = APPLY_VARIANCE_OF_UNIT_WEIGHT;
		try { value = PROPERTIES.getProperty("APPLY_VARIANCE_OF_UNIT_WEIGHT") != null && PROPERTIES.getProperty("APPLY_VARIANCE_OF_UNIT_WEIGHT").equalsIgnoreCase("FALSE") ? Boolean.FALSE : Boolean.TRUE; } catch (Exception e) {}
		return value;
	}
	
	public static double getConfidenceLevel() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("CONFIDENCE_LEVEL")); } catch (Exception e) {}
		return value > 0 && value < 1 ? value : CONFIDENCE_LEVEL;
	}
}
