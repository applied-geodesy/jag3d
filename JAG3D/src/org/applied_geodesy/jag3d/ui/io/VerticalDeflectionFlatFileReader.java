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

package org.applied_geodesy.jag3d.ui.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.applied_geodesy.adjustment.network.VerticalDeflectionType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.row.VerticalDeflectionRow;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.jag3d.ui.tree.VerticalDeflectionTreeItemValue;

import javafx.scene.control.TreeItem;

public class VerticalDeflectionFlatFileReader extends FlatFileReader<TreeItem<TreeItemValue>> {
	private Set<String> reservedNames = null;
	private final TreeItemType treeItemType;
	
	private List<VerticalDeflectionRow> verticalDeflections = null;
	
	public VerticalDeflectionFlatFileReader(VerticalDeflectionType verticalDeflectionType) {
		this.treeItemType = TreeItemType.getTreeItemTypeByVerticalDeflectionType(verticalDeflectionType);
		if (this.treeItemType == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, point type could not be transformed to tree item type. " + verticalDeflectionType);
		this.reset();
	}
	
	public VerticalDeflectionFlatFileReader(String fileName, VerticalDeflectionType verticalDeflectionType) {
		this(new File(fileName).toPath(), verticalDeflectionType);
	}

	public VerticalDeflectionFlatFileReader(File sf,  VerticalDeflectionType verticalDeflectionType) {
		this(sf.toPath(), verticalDeflectionType);
	}
	
	public VerticalDeflectionFlatFileReader(Path path,  VerticalDeflectionType verticalDeflectionType) {
		super(path);
		this.treeItemType = TreeItemType.getTreeItemTypeByVerticalDeflectionType(verticalDeflectionType);
		if (this.treeItemType == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, point type could not be transformed to tree item type. " + verticalDeflectionType);
		this.reset();
	}
	
	@Override
	public void reset() {
		if (this.verticalDeflections == null) 
			this.verticalDeflections = new ArrayList<VerticalDeflectionRow>();
		if (this.reservedNames == null)
			this.reservedNames = new HashSet<String>();
		
		this.verticalDeflections.clear();
		this.reservedNames.clear();
	}

	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.reset();
		this.ignoreLinesWhichStartWith("#");
		this.reservedNames = SQLManager.getInstance().getFullVerticalDeflectionNameSet();
		TreeItem<TreeItemValue> newTreeItem = null;
		
		super.read();
		
		if (!this.verticalDeflections.isEmpty()) {
			String itemName = this.createItemName(null, null);
			TreeItemType parentType = TreeItemType.getDirectoryByLeafType(this.treeItemType);
			newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, itemName, true, false);
			try {
				SQLManager.getInstance().saveGroup((VerticalDeflectionTreeItemValue)newTreeItem.getValue());
			} catch (SQLException e) {
				UITreeBuilder.getInstance().removeItem(newTreeItem);
				e.printStackTrace();
				throw new SQLException(e);
			}
			
			try {
				int groupId = ((VerticalDeflectionTreeItemValue)newTreeItem.getValue()).getGroupId();
				if (!this.verticalDeflections.isEmpty()) {
					for (VerticalDeflectionRow row : this.verticalDeflections) {
						row.setGroupId(groupId);
						SQLManager.getInstance().saveItem(row);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new SQLException(e);
			}			
		}
		
		this.reset();
		return newTreeItem;
	}

	@Override
	public void parse(String line) {
		line = line.trim();
		
		VerticalDeflectionRow verticalDeflectionRow = null;
		try {
			verticalDeflectionRow = VerticalDeflectionRow.scan(line);
			if (verticalDeflectionRow != null && !this.reservedNames.contains(verticalDeflectionRow.getName())) {
				this.verticalDeflections.add(verticalDeflectionRow);
				this.reservedNames.add(verticalDeflectionRow.getName());
			}
		}
		catch (Exception err) {
			return;
			// err.printStackTrace();
			// nichts, Beobachtung unbrauchbar...
		}	
	}
}