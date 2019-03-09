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

package org.applied_geodesy.jag3d.ui.dnd;

import org.applied_geodesy.jag3d.ui.tree.CongruenceAnalysisTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.PointTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;

import javafx.scene.control.TreeItem;

public class GroupTreeItemDnD extends DnD {
	private static final long serialVersionUID = 8338527371608133004L;
	
	private int dimension, itemIndex;
	private TreeItemType itemType, parentType;

	void setItemType(TreeItemType itemType) {
		this.itemType = itemType;
	}
	public TreeItemType getItemType() {
		return this.itemType;
	}
	void setParentType(TreeItemType parentType) {
		this.parentType = parentType;
	}
	public TreeItemType getParentType() {
		return this.parentType;
	}
	void setDimension(int dimension) {
		this.dimension = dimension;
	}
	public int getDimension() {
		return this.dimension;
	}
	void setIndex(int itemIndex) {
		this.itemIndex = itemIndex;
	}
	public int getIndex() {
		return this.itemIndex;
	}
	
	public static GroupTreeItemDnD fromTreeItem(TreeItem<TreeItemValue> treeItem) {
		TreeItemValue itemValue = treeItem.getValue();
		if (treeItem.getParent() == null || treeItem.getParent().getValue() == null || itemValue == null)
			return null;
		
		TreeItem<TreeItemValue> parent = treeItem.getParent();
		TreeItemType itemType = itemValue.getItemType();
		TreeItemType parentType = parent.getValue().getItemType();
		int groupId = -1, dimension = -1;
		
		if (itemType == null || parentType == null)
			return null;
		
		if (TreeItemType.isObservationTypeLeaf(itemType)) {
			groupId = ((ObservationTreeItemValue)itemValue).getGroupId();
			dimension = ((ObservationTreeItemValue)itemValue).getDimension();
		}
		else if (TreeItemType.isGNSSObservationTypeLeaf(itemType)) {
			groupId = ((ObservationTreeItemValue)itemValue).getGroupId();
			dimension = ((ObservationTreeItemValue)itemValue).getDimension();
		}
		else if (TreeItemType.isPointTypeLeaf(itemType)) {
			groupId = ((PointTreeItemValue)itemValue).getGroupId();
			dimension = ((PointTreeItemValue)itemValue).getDimension();
		}
		else if (TreeItemType.isCongruenceAnalysisTypeLeaf(itemType)) {
			groupId = ((CongruenceAnalysisTreeItemValue)itemValue).getGroupId();
			dimension = ((CongruenceAnalysisTreeItemValue)itemValue).getDimension();
		}
		else
			return null;

		int itemIndex = treeItem.getParent().getChildren().indexOf(treeItem);
		if (groupId >= 0 && dimension >= 0 && itemIndex >= 0) {
			GroupTreeItemDnD itemDnD = new GroupTreeItemDnD();
			itemDnD.setGroupId(groupId);
			itemDnD.setParentType(parentType);
			itemDnD.setItemType(itemType);
			itemDnD.setDimension(dimension);
			itemDnD.setIndex(itemIndex);
			return itemDnD;
		}
		return null;
	}
}