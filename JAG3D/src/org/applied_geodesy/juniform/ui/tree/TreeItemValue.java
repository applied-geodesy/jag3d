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

package org.applied_geodesy.juniform.ui.tree;

import org.applied_geodesy.juniform.ui.tabpane.TabType;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public abstract class TreeItemValue<T> {
	private ObjectProperty<String> name = new SimpleObjectProperty<String>(this, "name");
	private ObjectProperty<T> object = new SimpleObjectProperty<T>(this, "object");
	
	private final TreeItemType treeItemType;
	
	TreeItemValue(String name, TreeItemType treeItemType) {
		this.setName(name);
		this.treeItemType = treeItemType;
	}
	
	public ObjectProperty<T> objectProperty() {
		return this.object;
	}
	
	public TreeItemType getTreeItemType() {
		return this.treeItemType;
	}
	
	public abstract TabType[] getTabTypes();
	
	public T getObject() {
		return this.objectProperty().get();
	}
	
	public void setObject(T object) {
		this.objectProperty().set(object);
	}
	
	public ObjectProperty<String> nameProperty() {
		return this.name;
	}
	
	public String getName() {
		return this.nameProperty().get();
	}
	
	public void setName(final String name) {
		this.nameProperty().set(name);
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
}
