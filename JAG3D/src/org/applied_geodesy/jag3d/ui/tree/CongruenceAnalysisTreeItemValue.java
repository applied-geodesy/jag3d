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

import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.congruence.strain.RestrictionType;
import org.applied_geodesy.jag3d.ui.tabpane.TabType;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class CongruenceAnalysisTreeItemValue extends TreeItemValue implements Sortable {
	private IntegerProperty groupId = new SimpleIntegerProperty(-1);
	private int orderId;
	
//	CongruenceAnalysisTreeItemValue(TreeItemType type, String name) throws IllegalArgumentException {
//		this(-1, type, name);
//	}
	
	CongruenceAnalysisTreeItemValue(int groupId, TreeItemType type, String name, int orderId) throws IllegalArgumentException {
		this(groupId, type, name, Boolean.TRUE, orderId);
	}
	
	CongruenceAnalysisTreeItemValue(int groupId, TreeItemType type, String name, boolean enable, int orderId) throws IllegalArgumentException {
		super(type, name);
		this.setGroupId(groupId);
		this.setEnable(enable);
		this.setOrderId(orderId);
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
		case CONGRUENCE_ANALYSIS_1D_LEAF:
			return 1;

		case CONGRUENCE_ANALYSIS_2D_LEAF:
			return 2;

		case CONGRUENCE_ANALYSIS_3D_LEAF:
			return 3;
		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, TreeItemType does not refer to an object point nexus (congruence analysis) " + this.getItemType());
		}
	}
	
	public static RestrictionType[] getRestrictionTypes(TreeItemType itemType) {
		switch (itemType) {
		case CONGRUENCE_ANALYSIS_1D_LEAF:
			return new RestrictionType[] {
					RestrictionType.FIXED_TRANSLATION_Z,
					
					RestrictionType.FIXED_SCALE_Z
			};
		case CONGRUENCE_ANALYSIS_2D_LEAF:
			return new RestrictionType[] {
					RestrictionType.FIXED_TRANSLATION_Y,
					RestrictionType.FIXED_TRANSLATION_X,
					
					RestrictionType.FIXED_ROTATION_Z,
					
					RestrictionType.FIXED_SCALE_Y,
					RestrictionType.FIXED_SCALE_X,
					
					RestrictionType.FIXED_SHEAR_Z,
					
					RestrictionType.IDENT_SCALES_XY
			};
		case CONGRUENCE_ANALYSIS_3D_LEAF:
			return new RestrictionType[] {
					RestrictionType.FIXED_TRANSLATION_Y,
					RestrictionType.FIXED_TRANSLATION_X,
					RestrictionType.FIXED_TRANSLATION_Z,
					
					RestrictionType.FIXED_ROTATION_Y,
					RestrictionType.FIXED_ROTATION_X,
					RestrictionType.FIXED_ROTATION_Z,
					
					RestrictionType.FIXED_SCALE_Y,
					RestrictionType.FIXED_SCALE_X,
					RestrictionType.FIXED_SCALE_Z,
					
					RestrictionType.FIXED_SHEAR_Y,
					RestrictionType.FIXED_SHEAR_X,
					RestrictionType.FIXED_SHEAR_Z,
					
					RestrictionType.IDENT_SCALES_XY,
					RestrictionType.IDENT_SCALES_XZ,
					RestrictionType.IDENT_SCALES_YZ
			};
		default:
			throw new IllegalArgumentException(ObservationTreeItemValue.class.getSimpleName() + " : Error, TreeItemType does not refer to a congruence analysis type " + itemType);
		}
	}
	
	public static ParameterType[] getParameterTypes(TreeItemType itemType) {
		switch (itemType) {
		case CONGRUENCE_ANALYSIS_1D_LEAF:
			return new ParameterType[] {
					ParameterType.STRAIN_TRANSLATION_Z,
					ParameterType.STRAIN_SCALE_Z,
			};
		case CONGRUENCE_ANALYSIS_2D_LEAF:
			return new ParameterType[] {
					ParameterType.STRAIN_TRANSLATION_Y,
					ParameterType.STRAIN_TRANSLATION_X,
					ParameterType.STRAIN_ROTATION_Z,
					ParameterType.STRAIN_SCALE_Y,
					ParameterType.STRAIN_SCALE_X,
					ParameterType.STRAIN_SHEAR_Z,
			};
		case CONGRUENCE_ANALYSIS_3D_LEAF:
			return new ParameterType[] {
					ParameterType.STRAIN_TRANSLATION_Y,
					ParameterType.STRAIN_TRANSLATION_X,
					ParameterType.STRAIN_TRANSLATION_Z,
					ParameterType.STRAIN_ROTATION_Y,
					ParameterType.STRAIN_ROTATION_X,
					ParameterType.STRAIN_ROTATION_Z,
					ParameterType.STRAIN_SCALE_Y,
					ParameterType.STRAIN_SCALE_X,
					ParameterType.STRAIN_SHEAR_Y,
					ParameterType.STRAIN_SHEAR_X,
					ParameterType.STRAIN_SHEAR_Z,
			};
		default:
			throw new IllegalArgumentException(ObservationTreeItemValue.class.getSimpleName() + " : Error, TreeItemType does not refer to a congruence analysis type " + itemType);
		}
	}

	@Override
	public TabType[] getTabTypes() {
		return new TabType[] {
				TabType.RAW_DATA,
				TabType.PROPERTIES,
				TabType.RESULT_CONGRUENCE_ANALYSIS_POINT,
				TabType.ADDITIONAL_PARAMETER
		};
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