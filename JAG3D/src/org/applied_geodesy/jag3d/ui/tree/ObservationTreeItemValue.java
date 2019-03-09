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

package org.applied_geodesy.jag3d.ui.tree;

import org.applied_geodesy.adjustment.DefaultUncertainty;
import org.applied_geodesy.adjustment.network.ObservationGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.jag3d.ui.tabpane.TabType;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class ObservationTreeItemValue extends TreeItemValue implements Sortable {
	private IntegerProperty groupId = new SimpleIntegerProperty(-1);
	private int orderId;
	
//	ObservationTreeItemValue(TreeItemType type, String name) throws IllegalArgumentException {
//		this(-1, type, name);
//	}
	
	ObservationTreeItemValue(int groupId, TreeItemType type, String name, int orderId) throws IllegalArgumentException {
		this(groupId, type, name, Boolean.TRUE, Boolean.TRUE, orderId);
	}
	
	ObservationTreeItemValue(int groupId, TreeItemType type, String name, boolean referenceEpoch, boolean enable, int orderId) throws IllegalArgumentException { 
		super(type, name);
		this.setGroupId(groupId);
		this.setEnable(enable);
		this.setOrderId(orderId);
	}
	
	public final ObservationType getObservationType() {
		switch (this.getItemType()) {
		case LEVELING_LEAF:
			return ObservationType.LEVELING;

		case DIRECTION_LEAF:
			return ObservationType.DIRECTION;

		case HORIZONTAL_DISTANCE_LEAF:
			return ObservationType.HORIZONTAL_DISTANCE;

		case SLOPE_DISTANCE_LEAF:
			return ObservationType.SLOPE_DISTANCE;

		case ZENITH_ANGLE_LEAF:
			return ObservationType.ZENITH_ANGLE;

		case GNSS_1D_LEAF:
			return ObservationType.GNSS1D;

		case GNSS_2D_LEAF:
			return ObservationType.GNSS2D;

		case GNSS_3D_LEAF:
			return ObservationType.GNSS3D;

		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, TreeItemType does not refer to a terrestrial ObservationType " + this.getItemType());
		}
	}
	
	public IntegerProperty groupIdProperty() {
		return this.groupId;
	}
	
	public int getGroupId() {
		return this.groupIdProperty().get();
	}
	
	public void setGroupId(final int groupId) {
		this.groupIdProperty().set(groupId);
	}
	
	public final int getDimension() {
		switch (this.getItemType()) {
		case GNSS_2D_LEAF:
			return 2;
		case GNSS_3D_LEAF:
			return 3;
		default:
			return 1;
		}
	}
		
	@Override
	public TabType[] getTabTypes() {
		return new TabType[] {
				TabType.RAW_DATA,
				TabType.PROPERTIES,
				TabType.RESULT_DATA,
				TabType.ADDITIONAL_PARAMETER
		};
	}
	
	public static ParameterType[] getParameterTypes(TreeItemType itemType) {
		switch (itemType) {
		case LEVELING_LEAF:
			return new ParameterType[] {
					ParameterType.SCALE,
			};
		case HORIZONTAL_DISTANCE_LEAF:
		case SLOPE_DISTANCE_LEAF:
			return new ParameterType[] {
					ParameterType.ZERO_POINT_OFFSET,
					ParameterType.SCALE,
			};
		case DIRECTION_LEAF:
			return new ParameterType[] {
					ParameterType.ORIENTATION
			};
		case ZENITH_ANGLE_LEAF:
			return new ParameterType[] {
					ParameterType.REFRACTION_INDEX
			};
		case GNSS_1D_LEAF:
			return new ParameterType[] {
					ParameterType.ROTATION_Y,
					ParameterType.ROTATION_X,
					ParameterType.SCALE
			};
		case GNSS_2D_LEAF:
			return new ParameterType[] {
					ParameterType.ROTATION_Z,
					ParameterType.SCALE
			};
		case GNSS_3D_LEAF:
			return new ParameterType[] {
					ParameterType.ROTATION_Y,
					ParameterType.ROTATION_X,
					ParameterType.ROTATION_Z,
					ParameterType.SCALE
			};
		default:
			return new ParameterType[] {};
		}
	}
	
	public static double getDefaultUncertainty(TreeItemType itemType, ObservationGroupUncertaintyType uncertaintyType) {
		switch (itemType) {
		case LEVELING_LEAF:
			
			switch(uncertaintyType) {
			case ZERO_POINT_OFFSET:
				return DefaultUncertainty.getUncertaintyLevelingZeroPointOffset();
			case SQUARE_ROOT_DISTANCE_DEPENDENT:
				return DefaultUncertainty.getUncertaintyLevelingSquareRootDistanceDependent();
			case  DISTANCE_DEPENDENT:
				return DefaultUncertainty.getUncertaintyLevelingDistanceDependent();
			}

		case HORIZONTAL_DISTANCE_LEAF:
		case SLOPE_DISTANCE_LEAF:

			switch(uncertaintyType) {
			case ZERO_POINT_OFFSET:
				return DefaultUncertainty.getUncertaintyDistanceZeroPointOffset();
			case SQUARE_ROOT_DISTANCE_DEPENDENT:
				return DefaultUncertainty.getUncertaintyDistanceSquareRootDistanceDependent();
			case  DISTANCE_DEPENDENT:
				return DefaultUncertainty.getUncertaintyDistanceDistanceDependent();
			}
			
		case DIRECTION_LEAF:
		case ZENITH_ANGLE_LEAF:

			switch(uncertaintyType) {
			case ZERO_POINT_OFFSET:
				return DefaultUncertainty.getUncertaintyAngleZeroPointOffset();
			case SQUARE_ROOT_DISTANCE_DEPENDENT:
				return DefaultUncertainty.getUncertaintyAngleSquareRootDistanceDependent();
			case  DISTANCE_DEPENDENT:
				return DefaultUncertainty.getUncertaintyAngleDistanceDependent();
			}
			
		case GNSS_1D_LEAF:
		case GNSS_2D_LEAF:
		case GNSS_3D_LEAF:

			switch(uncertaintyType) {
			case ZERO_POINT_OFFSET:
				return DefaultUncertainty.getUncertaintyGNSSZeroPointOffset();
			case SQUARE_ROOT_DISTANCE_DEPENDENT:
				return DefaultUncertainty.getUncertaintyGNSSSquareRootDistanceDependent();
			case  DISTANCE_DEPENDENT:
				return DefaultUncertainty.getUncertaintyGNSSDistanceDependent();
			}
			
		default:
			return 0;
		}
	}

	@Override
	public int getOrderId() {
		return this.orderId;
	}

	@Override
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
}