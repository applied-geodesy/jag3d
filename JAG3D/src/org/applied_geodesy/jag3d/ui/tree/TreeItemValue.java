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

import org.applied_geodesy.jag3d.ui.tabpane.TabType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TreeItemValue {
	private TreeItemType type;
	private StringProperty name = new SimpleStringProperty();
	private BooleanProperty enable = new SimpleBooleanProperty(Boolean.TRUE);
	
	TreeItemValue(String name) {
		this(TreeItemType.UNSPECIFIC, name);
	}
	
	TreeItemValue(TreeItemType type, String name) {
		this.setItemType(type);
		this.setName(name);
	}
	
	public TreeItemType getItemType() {
		return this.type;
	}
	
	void setItemType(TreeItemType type) {
		this.type = type;
	}
	
	public StringProperty nameProperty() {
		return this.name;
	}
	
	public String getName() {
		return this.nameProperty().get();
	}
	
	public void setName(final String name) {
		this.nameProperty().set(name);
	}
	
	public TabType[] getTabTypes() {
		return null;
	}
	
	BooleanProperty enableProperty() {
		return this.enable;
	}
	
	public boolean isEnable() {
		return this.enableProperty().get();
	}
	
	void setEnable(final boolean enable) {
		this.enableProperty().set(enable);
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
}
