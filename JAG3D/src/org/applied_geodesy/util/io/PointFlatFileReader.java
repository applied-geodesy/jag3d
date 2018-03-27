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

package org.applied_geodesy.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.row.PointRow;
import org.applied_geodesy.jag3d.ui.tree.PointTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;

import javafx.scene.control.TreeItem;

public class PointFlatFileReader extends SourceFileReader {
	private final int dimension;
	private Set<String> reservedNames = null;
	private final TreeItemType treeItemType;
	
	private List<PointRow> points = null;
	
	public PointFlatFileReader(PointType pointType, int dimension) {
		this.dimension = dimension;
		this.treeItemType = TreeItemType.getTreeItemTypeByPointType(pointType, dimension);
		if (this.treeItemType == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, point type could not be transformed to tree item type. " + pointType);
		this.reset();
	}
	
	public PointFlatFileReader(String fileName, PointType pointType, int dimension) {
		this(new File(fileName).toPath(), pointType, dimension);
	}

	public PointFlatFileReader(File sf, PointType pointType, int dimension) {
		this(sf.toPath(), pointType, dimension);
	}
	
	public PointFlatFileReader(Path path, PointType pointType, int dimension) {
		super(path);
		this.dimension = dimension;
		this.treeItemType = TreeItemType.getTreeItemTypeByPointType(pointType, dimension);
		if (this.treeItemType == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, point type could not be transformed to tree item type. " + pointType);
		this.reset();
	}
	
	@Override
	public void reset() {
		if (this.points == null) 
			this.points = new ArrayList<PointRow>();
		if (this.reservedNames == null)
			this.reservedNames = new HashSet<String>();
		
		this.points.clear();
		this.reservedNames.clear();
	}

	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.reset();
		this.ignoreLinesWhichStartWith("#");
		this.reservedNames = SQLManager.getInstance().getFullPointNameSet();
		TreeItem<TreeItemValue> newTreeItem = null;
		
		super.read();
		
		if (!this.points.isEmpty()) {
			String itemName = this.createItemName(null, null);
			TreeItemType parentType = TreeItemType.getDirectoryByLeafType(this.treeItemType);
			newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, itemName, true, false);
			try {
				SQLManager.getInstance().saveGroup((PointTreeItemValue)newTreeItem.getValue());
			} catch (SQLException e) {
				UITreeBuilder.getInstance().removeItem(newTreeItem);
				e.printStackTrace();
				throw new SQLException(e);
			}
			
			try {
				int groupId = ((PointTreeItemValue)newTreeItem.getValue()).getGroupId();
				if (!this.points.isEmpty()) {
					for (PointRow row : this.points) {
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
		
		PointRow pointRow = null;
		try {
			pointRow = PointRow.scan(line, this.dimension);
			if (pointRow != null && !this.reservedNames.contains(pointRow.getName())) {
				this.points.add(pointRow);
				this.reservedNames.add(pointRow.getName());
			}
		}
		catch (Exception err) {
			return;
			// err.printStackTrace();
			// nichts, Beobachtung unbrauchbar...
		}	
	}
}
