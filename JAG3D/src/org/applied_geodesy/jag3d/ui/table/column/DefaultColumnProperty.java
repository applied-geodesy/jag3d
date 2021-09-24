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

package org.applied_geodesy.jag3d.ui.table.column;

import java.io.BufferedInputStream;
import java.util.Properties;

import org.applied_geodesy.adjustment.network.DefaultUncertainty;

public class DefaultColumnProperty {
	private final static double NARROW = 75;
	private final static double NORMAL = 100;
	private final static double LARGE  = 125;
	private final static double WIDE   = 150;

	private final static Properties PROPERTIES = new Properties();
	
	static {
		BufferedInputStream bis = null;
		final String path = "/properties/tablecolumns.default";
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
	
	private DefaultColumnProperty() {}
	
	public static double getPrefWidth(ColumnContentType columnContentType) {
		double value = -1;
		switch(columnContentType) {
		case ENABLE:
		case CODE:
		case INSTRUMENT_HEIGHT:
		case REFLECTOR_HEIGHT:
		case OMEGA:
		case VARIANCE:
			
		case REDUNDANCY:
		case REDUNDANCY_X:
		case REDUNDANCY_Y:
		case REDUNDANCY_Z:
		case NUMBER_OF_OBSERVATION:
			
		case P_VALUE_APRIORI:
		case P_VALUE_APOSTERIORI:
		case TEST_STATISTIC_APRIORI:
		case TEST_STATISTIC_APOSTERIORI:
			value = -1;
			try { value = Double.parseDouble(PROPERTIES.getProperty(columnContentType.name())); } catch (Exception e) {}
			return value > 0 ? Math.max(ColumnProperty.MIN_WIDTH, value) : NARROW;

		case INFLUENCE_ON_POINT_POSITION:
		case INFLUENCE_ON_POINT_POSITION_X:
		case INFLUENCE_ON_POINT_POSITION_Y:
		case INFLUENCE_ON_POINT_POSITION_Z:
	
		case FIRST_PRINCIPLE_COMPONENT_X:
		case FIRST_PRINCIPLE_COMPONENT_Y:
		case FIRST_PRINCIPLE_COMPONENT_Z:
			
		case CONFIDENCE_ALPHA:
		case CONFIDENCE_BETA:
		case CONFIDENCE_GAMMA:
		
		case CONFIDENCE_A:
		case CONFIDENCE_B:
		case CONFIDENCE_C:
			
		case RESIDUAL:
		case RESIDUAL_X:
		case RESIDUAL_Y:
		case RESIDUAL_Z:
			
		case GROSS_ERROR:
		case GROSS_ERROR_X:
		case GROSS_ERROR_Y:
		case GROSS_ERROR_Z:
			
		case MINIMAL_DETECTABLE_BIAS:
		case MINIMAL_DETECTABLE_BIAS_X:
		case MINIMAL_DETECTABLE_BIAS_Y:
		case MINIMAL_DETECTABLE_BIAS_Z:

		case MAXIMUM_TOLERABLE_BIAS:
		case MAXIMUM_TOLERABLE_BIAS_X:
		case MAXIMUM_TOLERABLE_BIAS_Y:
		case MAXIMUM_TOLERABLE_BIAS_Z:

		case UNCERTAINTY_APRIORI:
		case UNCERTAINTY_APOSTERIORI:
		case UNCERTAINTY_X_APRIORI:
		case UNCERTAINTY_X_APOSTERIORI:
		case UNCERTAINTY_Y_APRIORI:
		case UNCERTAINTY_Y_APOSTERIORI:
		case UNCERTAINTY_Z_APRIORI:
		case UNCERTAINTY_Z_APOSTERIORI:
			
		case INFLUENCE_ON_NETWORK_DISTORTION:
		case APPROXIMATED_DISTANCE_APRIORI:
		case SIGNIFICANT:
			value = -1;
			try { value = Double.parseDouble(PROPERTIES.getProperty(columnContentType.name())); } catch (Exception e) {}
			return value > 0 ? Math.max(ColumnProperty.MIN_WIDTH, value) : NORMAL;
			
		case POINT_NAME:
		case START_POINT_NAME:
		case END_POINT_NAME:
		case VARIANCE_COMPONENT_NAME:
		case VARIANCE_COMPONENT_TYPE:
		case PARAMETER_NAME:
			value = -1;
			try { value = Double.parseDouble(PROPERTIES.getProperty(columnContentType.name())); } catch (Exception e) {}
			return value > 0 ? Math.max(ColumnProperty.MIN_WIDTH, value) : WIDE;

			
		case VALUE_APRIORI:
		case VALUE_APOSTERIORI:
		case VALUE_X_APRIORI:
		case VALUE_X_APOSTERIORI:
		case VALUE_Y_APRIORI:
		case VALUE_Y_APOSTERIORI:
		case VALUE_Z_APRIORI:
		case VALUE_Z_APOSTERIORI:
			value = -1;
			try { value = Double.parseDouble(PROPERTIES.getProperty(columnContentType.name())); } catch (Exception e) {}
			return value > 0 ? Math.max(ColumnProperty.MIN_WIDTH, value) : LARGE;

		case DEFAULT:
			return ColumnProperty.PREFERRED_WIDTH;
		}
		return ColumnProperty.PREFERRED_WIDTH;
	}
}
