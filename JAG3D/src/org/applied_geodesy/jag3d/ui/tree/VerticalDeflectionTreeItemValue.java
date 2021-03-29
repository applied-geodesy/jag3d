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

import org.applied_geodesy.adjustment.network.DefaultUncertainty;
import org.applied_geodesy.adjustment.network.VerticalDeflectionGroupUncertaintyType;
import org.applied_geodesy.jag3d.ui.tabpane.TabType;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class VerticalDeflectionTreeItemValue extends TreeItemValue implements Sortable, Groupable {
	private IntegerProperty groupId = new SimpleIntegerProperty(-1);
	private int orderId;
	
	VerticalDeflectionTreeItemValue(int groupId, TreeItemType type, String name, int orderId) throws IllegalArgumentException {
		this(groupId, type, name, Boolean.TRUE, orderId);
	}
	
	VerticalDeflectionTreeItemValue(int groupId, TreeItemType type, String name, boolean enable, int orderId) throws IllegalArgumentException {
		super(type, name);
		this.setGroupId(groupId);
		this.setEnable(enable);
		this.setOrderId(orderId);
	}
	
	public IntegerProperty groupIdProperty() {
		return this.groupId;
	}
	
	@Override
	public int getGroupId() {
		return this.groupIdProperty().get();
	}
	
	@Override
	public void setGroupId(final int groupId) {
		this.groupIdProperty().set(groupId);
	}
	
	@Override
	public final int getDimension() {
		return 2;
	}
	
	@Override
	public TabType[] getTabTypes() {
		TreeItemType type = this.getItemType();
		List<TabType> tabTyps = new ArrayList<TabType>(5);

		// A-priori Values
		tabTyps.add(TabType.RAW_DATA);
		
		// Properties 
		if (type == TreeItemType.STOCHASTIC_VERTICAL_DEFLECTION_LEAF)
			tabTyps.add(TabType.PROPERTIES);
		
		// Results Point
		tabTyps.add(TabType.RESULT_DATA);
		
		// Variance components estimation 
		if (type == TreeItemType.STOCHASTIC_VERTICAL_DEFLECTION_LEAF)
			tabTyps.add(TabType.VARIANCE_COMPONENT);
		
		return tabTyps.toArray(new TabType[tabTyps.size()]);
	}
	
	public static double getDefaultUncertainty(VerticalDeflectionGroupUncertaintyType uncertaintyType) {
		switch(uncertaintyType) {
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
