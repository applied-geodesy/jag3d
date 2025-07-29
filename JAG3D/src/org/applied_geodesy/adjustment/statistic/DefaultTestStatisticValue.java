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

package org.applied_geodesy.adjustment.statistic;

import java.io.BufferedInputStream;
import java.util.Properties;

public class DefaultTestStatisticValue {
	private final static double PROBABILITY_VALUE       = 0.001;
	private final static double POWER_OF_TEST           = 0.8;
	
	private static final TestStatisticType TEST_STATISTIC_TYPE = TestStatisticType.BAARDA_METHOD;
	
	private final static Properties PROPERTIES = new Properties();
	
	static {
		BufferedInputStream bis = null;
		final String path = "properties/teststatistic.default";
		try {
			if (DefaultTestStatisticValue.class.getClassLoader().getResourceAsStream(path) != null) {
				bis = new BufferedInputStream(DefaultTestStatisticValue.class.getClassLoader().getResourceAsStream(path));
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
	
	private DefaultTestStatisticValue() {}
	
	public static TestStatisticType getTestStatisticType() {
		TestStatisticType value = null;
		try { value = TestStatisticType.valueOf(PROPERTIES.getProperty("TEST_STATISTIC_TYPE")); } catch (Exception e) {}
		return value != null ? value : TEST_STATISTIC_TYPE;
	}
	
	public static double getProbabilityValue() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("PROBABILITY_VALUE")); } catch (Exception e) {}
		return value > 0 && value < 1 ? value : PROBABILITY_VALUE;
	}
	
	public static double getPowerOfTest() {
		double value = -1;
		try { value = Double.parseDouble(PROPERTIES.getProperty("POWER_OF_TEST")); } catch (Exception e) {}
		return value > 0 && value < 1 ? value : POWER_OF_TEST;
	}

}
