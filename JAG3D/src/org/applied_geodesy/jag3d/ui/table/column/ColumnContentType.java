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

public enum ColumnContentType {
	DEFAULT(0),
	
	ENABLE(1),
	SIGNIFICANT(2),
	
	POINT_NAME(10),
	START_POINT_NAME(11),
	END_POINT_NAME(12),
	PARAMETER_NAME(13),
	VARIANCE_COMPONENT_TYPE(14),
	VARIANCE_COMPONENT_NAME(15),
	CODE(16),
	
	INSTRUMENT_HEIGHT(20),
	REFLECTOR_HEIGHT(21),
	
	VALUE_X_APRIORI(30),
	VALUE_Y_APRIORI(31),
	VALUE_Z_APRIORI(32),
	
	VALUE_X_APOSTERIORI(33),
	VALUE_Y_APOSTERIORI(34),
	VALUE_Z_APOSTERIORI(35),
	
	VALUE_APRIORI(40),
	VALUE_APOSTERIORI(41),
	
	APPROXIMATED_DISTANCE_APRIORI(50),
	UNCERTAINTY_APRIORI(51),
	UNCERTAINTY_X_APRIORI(52),
	UNCERTAINTY_Y_APRIORI(53),
	UNCERTAINTY_Z_APRIORI(54),
	
	UNCERTAINTY_APOSTERIORI(55),
	UNCERTAINTY_X_APOSTERIORI(56),
	UNCERTAINTY_Y_APOSTERIORI(57),
	UNCERTAINTY_Z_APOSTERIORI(58),
	
	RESIDUAL(60),
	RESIDUAL_X(61),
	RESIDUAL_Y(62),
	RESIDUAL_Z(63),
	
	REDUNDANCY(70),
	REDUNDANCY_X(71),
	REDUNDANCY_Y(72),
	REDUNDANCY_Z(73),
	NUMBER_OF_OBSERVATION(74),
	
	GROSS_ERROR(80),
	GROSS_ERROR_X(81),
	GROSS_ERROR_Y(82),
	GROSS_ERROR_Z(83),
	
	MINIMAL_DETECTABLE_BIAS(90),
	MINIMAL_DETECTABLE_BIAS_X(91),
	MINIMAL_DETECTABLE_BIAS_Y(92),
	MINIMAL_DETECTABLE_BIAS_Z(93),
	
	MAXIMUM_TOLERABLE_BIAS(150),
	MAXIMUM_TOLERABLE_BIAS_X(151),
	MAXIMUM_TOLERABLE_BIAS_Y(152),
	MAXIMUM_TOLERABLE_BIAS_Z(153),
	
	INFLUENCE_ON_POINT_POSITION(100),
	INFLUENCE_ON_POINT_POSITION_X(101),
	INFLUENCE_ON_POINT_POSITION_Y(102),
	INFLUENCE_ON_POINT_POSITION_Z(103),
	
	INFLUENCE_ON_NETWORK_DISTORTION(110),
	
	FIRST_PRINCIPLE_COMPONENT_X(120),
	FIRST_PRINCIPLE_COMPONENT_Y(121),
	FIRST_PRINCIPLE_COMPONENT_Z(122),
	
	CONFIDENCE_A(130),
	CONFIDENCE_B(131),
	CONFIDENCE_C(132),
	CONFIDENCE_ALPHA(133),
	CONFIDENCE_BETA(134),
	CONFIDENCE_GAMMA(135),

	OMEGA(140),
	P_VALUE_APRIORI(141),
	P_VALUE_APOSTERIORI(142),
	TEST_STATISTIC_APRIORI(143),
	TEST_STATISTIC_APOSTERIORI(144),
	VARIANCE(145),
	;

	private int id;
	private ColumnContentType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static ColumnContentType getEnumByValue(int value) {
		for(ColumnContentType element : ColumnContentType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	}  
}

