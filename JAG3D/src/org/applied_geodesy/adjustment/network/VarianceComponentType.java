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

package org.applied_geodesy.adjustment.network;

public enum VarianceComponentType {
	GLOBAL(0),
	
	STOCHASTIC_POINT_1D_COMPONENT(101),
	STOCHASTIC_POINT_2D_COMPONENT(102),
	STOCHASTIC_POINT_3D_COMPONENT(103),
	
	STOCHASTIC_POINT_DEFLECTION_COMPONENT(201),
	
	LEVELING_COMPONENT(10),
	LEVELING_ZERO_POINT_OFFSET_COMPONENT(11),
	LEVELING_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT(12),
	LEVELING_DISTANCE_DEPENDENT_COMPONENT(13),
	
	DIRECTION_COMPONENT(20),
	DIRECTION_ZERO_POINT_OFFSET_COMPONENT(21),
	DIRECTION_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT(22),
	DIRECTION_DISTANCE_DEPENDENT_COMPONENT(23),

	HORIZONTAL_DISTANCE_COMPONENT(30),
	HORIZONTAL_DISTANCE_ZERO_POINT_OFFSET_COMPONENT(31),
	HORIZONTAL_DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT(32),
	HORIZONTAL_DISTANCE_DISTANCE_DEPENDENT_COMPONENT(33),
	
	SLOPE_DISTANCE_COMPONENT(40),
	SLOPE_DISTANCE_ZERO_POINT_OFFSET_COMPONENT(41),
	SLOPE_DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT(42),
	SLOPE_DISTANCE_DISTANCE_DEPENDENT_COMPONENT(43),

	ZENITH_ANGLE_COMPONENT(50),
	ZENITH_ANGLE_ZERO_POINT_OFFSET_COMPONENT(51),
	ZENITH_ANGLE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT(52),
	ZENITH_ANGLE_DISTANCE_DEPENDENT_COMPONENT(53),
	
	GNSS1D_COMPONENT(60),
	GNSS1D_ZERO_POINT_OFFSET_COMPONENT(61),
	GNSS1D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT(62),
	GNSS1D_DISTANCE_DEPENDENT_COMPONENT(63),
	
	GNSS2D_COMPONENT(70),
	GNSS2D_ZERO_POINT_OFFSET_COMPONENT(71),
	GNSS2D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT(72),
	GNSS2D_DISTANCE_DEPENDENT_COMPONENT(73),
	
	GNSS3D_COMPONENT(80),
	GNSS3D_ZERO_POINT_OFFSET_COMPONENT(81),
	GNSS3D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT(82),
	GNSS3D_DISTANCE_DEPENDENT_COMPONENT(83);

	private int id;
	private VarianceComponentType(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	public static VarianceComponentType getEnumByValue(int value) {
		for(VarianceComponentType element : VarianceComponentType.values()) {
			if(element.id == value)
				return element;
		}
		return null;
	} 
	
	public static VarianceComponentType getVarianceComponentTypeByObservationType(ObservationType type) {
		switch(type) {
		case LEVELING:
			return LEVELING_COMPONENT;
		case DIRECTION:
			return DIRECTION_COMPONENT;
		case HORIZONTAL_DISTANCE:
			return HORIZONTAL_DISTANCE_COMPONENT;
		case SLOPE_DISTANCE:
			return SLOPE_DISTANCE_COMPONENT;
		case ZENITH_ANGLE:
			return ZENITH_ANGLE_COMPONENT;
		case GNSS1D:
			return GNSS1D_COMPONENT;
		case GNSS2D:
			return GNSS2D_COMPONENT;
		case GNSS3D:
			return GNSS3D_COMPONENT;
		}
		return null;
	}
	
	public static VarianceComponentType getZeroPointOffsetVarianceComponentTypeByObservationType(ObservationType type) {
		switch(type) {
		case LEVELING:
			return LEVELING_ZERO_POINT_OFFSET_COMPONENT;
		case DIRECTION:
			return DIRECTION_ZERO_POINT_OFFSET_COMPONENT;
		case HORIZONTAL_DISTANCE:
			return HORIZONTAL_DISTANCE_ZERO_POINT_OFFSET_COMPONENT;
		case SLOPE_DISTANCE:
			return SLOPE_DISTANCE_ZERO_POINT_OFFSET_COMPONENT;
		case ZENITH_ANGLE:
			return ZENITH_ANGLE_ZERO_POINT_OFFSET_COMPONENT;
		case GNSS1D:
			return GNSS1D_ZERO_POINT_OFFSET_COMPONENT;
		case GNSS2D:
			return GNSS2D_ZERO_POINT_OFFSET_COMPONENT;
		case GNSS3D:
			return GNSS3D_ZERO_POINT_OFFSET_COMPONENT;
		}
		return null;
	}
	
	public static VarianceComponentType getSquareRootDistanceDependentVarianceComponentTypeByObservationType(ObservationType type) {
		switch(type) {
		case LEVELING:
			return LEVELING_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT;
		case DIRECTION:
			return DIRECTION_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT;
		case HORIZONTAL_DISTANCE:
			return HORIZONTAL_DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT;
		case SLOPE_DISTANCE:
			return SLOPE_DISTANCE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT;
		case ZENITH_ANGLE:
			return ZENITH_ANGLE_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT;
		case GNSS1D:
			return GNSS1D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT;
		case GNSS2D:
			return GNSS2D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT;
		case GNSS3D:
			return GNSS3D_SQUARE_ROOT_DISTANCE_DEPENDENT_COMPONENT;
		}
		return null;
	}
	
	public static VarianceComponentType getDistanceDependentVarianceComponentTypeByObservationType(ObservationType type) {
		switch(type) {
		case LEVELING:
			return LEVELING_DISTANCE_DEPENDENT_COMPONENT;
		case DIRECTION:
			return DIRECTION_DISTANCE_DEPENDENT_COMPONENT;
		case HORIZONTAL_DISTANCE:
			return HORIZONTAL_DISTANCE_DISTANCE_DEPENDENT_COMPONENT;
		case SLOPE_DISTANCE:
			return SLOPE_DISTANCE_DISTANCE_DEPENDENT_COMPONENT;
		case ZENITH_ANGLE:
			return ZENITH_ANGLE_DISTANCE_DEPENDENT_COMPONENT;
		case GNSS1D:
			return GNSS1D_DISTANCE_DEPENDENT_COMPONENT;
		case GNSS2D:
			return GNSS2D_DISTANCE_DEPENDENT_COMPONENT;
		case GNSS3D:
			return GNSS3D_DISTANCE_DEPENDENT_COMPONENT;
		}
		return null;
	} 
	
	public static VarianceComponentType getComponentTypeByPointDimension(int dimension) {
		switch(dimension) {
		case 1:
			return STOCHASTIC_POINT_1D_COMPONENT;
		case 2:
			return STOCHASTIC_POINT_2D_COMPONENT;
		case 3:
			return STOCHASTIC_POINT_3D_COMPONENT;
		}		
		return null;
	}  
}
