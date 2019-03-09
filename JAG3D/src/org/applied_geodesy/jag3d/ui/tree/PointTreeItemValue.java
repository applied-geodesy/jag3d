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

import java.util.ArrayList;
import java.util.List;

import org.applied_geodesy.adjustment.DefaultUncertainty;
import org.applied_geodesy.adjustment.network.PointGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.jag3d.ui.tabpane.TabType;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class PointTreeItemValue extends TreeItemValue implements Sortable {
	private IntegerProperty groupId = new SimpleIntegerProperty(-1);
	private int orderId;
	
//	PointTreeItemValue(TreeItemType type, String name) throws IllegalArgumentException {
//		this(-1, type, name);
//	}
	
	PointTreeItemValue(int groupId, TreeItemType type, String name, int orderId) throws IllegalArgumentException {
		this(groupId, type, name, Boolean.TRUE, orderId);
	}
	
	PointTreeItemValue(int groupId, TreeItemType type, String name, boolean enable, int orderId) throws IllegalArgumentException {
		super(type, name);
		this.setGroupId(groupId);
		this.setEnable(enable);
		this.setOrderId(orderId);
	}
	
	public final PointType getPointType() {
		switch (this.getItemType()) {
		case REFERENCE_POINT_1D_LEAF:
		case REFERENCE_POINT_2D_LEAF:
		case REFERENCE_POINT_3D_LEAF:
			return PointType.REFERENCE_POINT;
			
		case STOCHASTIC_POINT_1D_LEAF:
		case STOCHASTIC_POINT_2D_LEAF:
		case STOCHASTIC_POINT_3D_LEAF:
			return PointType.STOCHASTIC_POINT;
			
		case DATUM_POINT_1D_LEAF:
		case DATUM_POINT_2D_LEAF:
		case DATUM_POINT_3D_LEAF:
			return PointType.DATUM_POINT;
			
		case NEW_POINT_1D_LEAF:
		case NEW_POINT_2D_LEAF:
		case NEW_POINT_3D_LEAF:
			return PointType.NEW_POINT;

		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, TreeItemType does not refer to a PointType " + this.getItemType());
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
		case REFERENCE_POINT_1D_LEAF:
		case STOCHASTIC_POINT_1D_LEAF:
		case DATUM_POINT_1D_LEAF:
		case NEW_POINT_1D_LEAF:
			return 1;

		case REFERENCE_POINT_2D_LEAF:
		case STOCHASTIC_POINT_2D_LEAF:	
		case DATUM_POINT_2D_LEAF:
		case NEW_POINT_2D_LEAF:
			return 2;

		case REFERENCE_POINT_3D_LEAF:
		case STOCHASTIC_POINT_3D_LEAF:
		case DATUM_POINT_3D_LEAF:
		case NEW_POINT_3D_LEAF:
			return 3;
		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, TreeItemType does not refer to a PointType " + this.getItemType());
		}
	}


	@Override
	public TabType[] getTabTypes() {
		TreeItemType type = this.getItemType();
		List<TabType> tabTyps = new ArrayList<TabType>(5);

		// A-priori Values
		tabTyps.add(TabType.RAW_DATA);
		
		// Properties 
		switch (type) {
		case STOCHASTIC_POINT_1D_LEAF:
		case STOCHASTIC_POINT_2D_LEAF:	
		case STOCHASTIC_POINT_3D_LEAF:
		case REFERENCE_POINT_3D_LEAF:
		case DATUM_POINT_3D_LEAF:
		case NEW_POINT_3D_LEAF:
			tabTyps.add(TabType.PROPERTIES);
			break;
		default:
			break;
		}
		
		// Results Point
		tabTyps.add(TabType.RESULT_DATA);
		
		// Result Deflections
		switch (type) {
		//case REFERENCE_POINT_3D_LEAF:
		case STOCHASTIC_POINT_3D_LEAF:
		case DATUM_POINT_3D_LEAF:
		case NEW_POINT_3D_LEAF:
			tabTyps.add(TabType.RESULT_DEFLECTION);
			break;
		default:
			break;
		}
		
		// Congruence points
		switch (type) {
		case DATUM_POINT_1D_LEAF:
		case DATUM_POINT_2D_LEAF:
		case DATUM_POINT_3D_LEAF:
			tabTyps.add(TabType.RESULT_CONGRUENCE_ANALYSIS_POINT);
			break;
		default:
			break;
		}
		
		// Congruence deflection
		switch (type) {
		case DATUM_POINT_3D_LEAF:
			tabTyps.add(TabType.RESULT_CONGRUENCE_ANALYSIS_DEFLECTION);
			break;
		default:
			break;
		}
		
		return tabTyps.toArray(new TabType[tabTyps.size()]);
	}
	
	public static double getDefaultUncertainty(PointGroupUncertaintyType uncertaintyType) {
		switch(uncertaintyType) {
		case CONSTANT_X:
			return DefaultUncertainty.getUncertaintyX();
		case CONSTANT_Y:
			return DefaultUncertainty.getUncertaintyY();
		case CONSTANT_Z:
			return DefaultUncertainty.getUncertaintyZ();
		case DEFLECTION_X:
			return DefaultUncertainty.getUncertaintyDeflectionX();
		case DEFLECTION_Y:
			return DefaultUncertainty.getUncertaintyDeflectionY();
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