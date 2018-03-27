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

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.row.CongruenceAnalysisRow;
import org.applied_geodesy.jag3d.ui.tree.CongruenceAnalysisTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;

import javafx.scene.control.TreeItem;

public class CongruenceAnalysisFlatFileReader extends SourceFileReader {
	private class CongruenceAnalysisPointPair {
		private final String name1, name2;
		private CongruenceAnalysisPointPair (String name1, String name2) {
			this.name1 = name1;
			this.name2 = name2;
		}
		@Override
		public int hashCode() {
			return name1.hashCode() + name2.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			
			if (obj == null)
				return false;
			
			if (!(obj instanceof CongruenceAnalysisPointPair))
				return false;
			
			CongruenceAnalysisPointPair other = (CongruenceAnalysisPointPair) obj;

			if (this.name1.equals(other.name1) && this.name2.equals(other.name2) ||
					this.name1.equals(other.name2) && this.name2.equals(other.name1))
				return true;
			
			return false;
		}		
	}
	
	private Set<CongruenceAnalysisPointPair> reservedNamePairs = null;
	private final TreeItemType treeItemType;
	private List<CongruenceAnalysisRow> congruenceAnalysisPairs = null;
	
	public CongruenceAnalysisFlatFileReader(int dimension) {
		switch(dimension) {
		case 1:
			this.treeItemType = TreeItemType.CONGRUENCE_ANALYSIS_1D_LEAF;
			break;
		case 2:
			this.treeItemType = TreeItemType.CONGRUENCE_ANALYSIS_2D_LEAF;
			break;
		case 3:
			this.treeItemType = TreeItemType.CONGRUENCE_ANALYSIS_3D_LEAF;
			break;
			default:
				throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, dimension could not be transformed to tree item type. " + dimension);
		}
		this.reset();
	}
	
	public CongruenceAnalysisFlatFileReader(String fileName, int dimension) {
		this(new File(fileName).toPath(), dimension);
	}

	public CongruenceAnalysisFlatFileReader(File sf, int dimension) {
		this(sf.toPath(), dimension);
	}
	
	public CongruenceAnalysisFlatFileReader(Path path, int dimension) {
		super(path);
		switch(dimension) {
		case 1:
			this.treeItemType = TreeItemType.CONGRUENCE_ANALYSIS_1D_LEAF;
			break;
		case 2:
			this.treeItemType = TreeItemType.CONGRUENCE_ANALYSIS_2D_LEAF;
			break;
		case 3:
			this.treeItemType = TreeItemType.CONGRUENCE_ANALYSIS_3D_LEAF;
			break;
			default:
				throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, dimension could not be transformed to tree item type. " + dimension);
		}
		this.reset();
	}
	
	@Override
	public void reset() {
		if (this.congruenceAnalysisPairs == null) 
			this.congruenceAnalysisPairs = new ArrayList<CongruenceAnalysisRow>();
		if (this.reservedNamePairs == null)
			this.reservedNamePairs = new HashSet<CongruenceAnalysisPointPair>();
		
		this.congruenceAnalysisPairs.clear();
		this.reservedNamePairs.clear();
	}

	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.reset();
		this.ignoreLinesWhichStartWith("#");
		TreeItem<TreeItemValue> newTreeItem = null;
		
		super.read();
		
		if (!this.congruenceAnalysisPairs.isEmpty()) {
			String itemName = this.createItemName(null, null);
			TreeItemType parentType = TreeItemType.getDirectoryByLeafType(this.treeItemType);
			newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, itemName, true, false);
			try {
				SQLManager.getInstance().saveGroup((CongruenceAnalysisTreeItemValue)newTreeItem.getValue());
			} catch (SQLException e) {
				UITreeBuilder.getInstance().removeItem(newTreeItem);
				e.printStackTrace();
				throw new SQLException(e);
			}
			
			try {
				int groupId = ((CongruenceAnalysisTreeItemValue)newTreeItem.getValue()).getGroupId();
				if (!this.congruenceAnalysisPairs.isEmpty()) {
					for (CongruenceAnalysisRow row : this.congruenceAnalysisPairs) {
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
		
		CongruenceAnalysisRow congruenceAnalysisRow = null;
		try {
			congruenceAnalysisRow = CongruenceAnalysisRow.scan(line);
			
			if (congruenceAnalysisRow == null)
				return;
			
			String name1 = congruenceAnalysisRow.getNameInReferenceEpoch();
			String name2 = congruenceAnalysisRow.getNameInControlEpoch();
			
			if (name1 == null || name2 == null || name1.trim().isEmpty() || name2.trim().isEmpty() || name1.equals(name2))
				return;
			
			CongruenceAnalysisPointPair pair = new CongruenceAnalysisPointPair(name1, name2);
			
			if (!this.reservedNamePairs.contains(pair)) {
				this.congruenceAnalysisPairs.add(congruenceAnalysisRow);
				this.reservedNamePairs.add(pair);
			}
		}
		catch (Exception err) {
			return;
			// err.printStackTrace();
			// nichts, Beobachtung unbrauchbar...
		}	
	}
}