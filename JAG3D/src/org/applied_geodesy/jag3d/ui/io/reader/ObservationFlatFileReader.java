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

package org.applied_geodesy.jag3d.ui.io.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.row.GNSSObservationRow;
import org.applied_geodesy.jag3d.ui.table.row.ObservationRow;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;

import javafx.scene.control.TreeItem;

public class ObservationFlatFileReader extends FlatFileReader<TreeItem<TreeItemValue>> {
	private final ObservationType observationType;
	private final TreeItemType treeItemType;
	
	private boolean separateGroup = true;
	
	private List<TerrestrialObservationRow> observations = null;
	private List<GNSSObservationRow> gnss = null;
	
	private boolean isGroupWithEqualStation = true;
	private String startPointName = null;
	
	public ObservationFlatFileReader(ObservationType observationType) {
		this.observationType = observationType;
		this.separateGroup   = ImportOption.getInstance().isGroupSeparation(observationType);
		this.treeItemType    = TreeItemType.getTreeItemTypeByObservationType(observationType);
		if (this.treeItemType == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, observation type could not be transformed to tree item type. " + observationType);
		this.reset();
	}
	
	public ObservationFlatFileReader(String fileName, ObservationType observationType) {
		this(new File(fileName).toPath(), observationType);
	}

	public ObservationFlatFileReader(File sf, ObservationType observationType) {
		this(sf.toPath(), observationType);
	}
	
	public ObservationFlatFileReader(Path path, ObservationType observationType) {
		super(path);
		this.observationType = observationType;
		this.treeItemType = TreeItemType.getTreeItemTypeByObservationType(observationType);
		if (this.treeItemType == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, observation type could not be transformed to tree item type. " + observationType);
		this.reset();
	}
	
	@Override
	public void reset() {
		if (this.observations == null)
			this.observations = new ArrayList<TerrestrialObservationRow>();
		if (this.gnss == null)
			this.gnss = new ArrayList<GNSSObservationRow>();
		
		this.observations.clear();
		this.gnss.clear();
		
		this.isGroupWithEqualStation = true;
		this.startPointName = null;
	}
	
	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.reset();
		this.ignoreLinesWhichStartWith("#");
		TreeItem<TreeItemValue> newTreeItem = null;
		
		super.read();
		 
		if (!this.observations.isEmpty() || !this.gnss.isEmpty()) {
			if (!this.observations.isEmpty())
				newTreeItem = this.saveGroup(this.treeItemType, this.observations);
			else if (!this.gnss.isEmpty())
				newTreeItem = this.saveGroup(this.treeItemType, this.gnss);
		}
		this.reset();
		return newTreeItem;
	}

	@Override
	public void parse(String line) {
		line = line.trim();
		
		TerrestrialObservationRow terrestrialObservationRow = null;
		GNSSObservationRow gnssObservationRow = null;

		try {
			switch(this.observationType) {		
			case GNSS1D:
				gnssObservationRow = GNSSObservationRow.scan(line, 1);
				break;
				
			case GNSS2D:
				gnssObservationRow = GNSSObservationRow.scan(line, 2);
				break;
				
			case GNSS3D:
				gnssObservationRow = GNSSObservationRow.scan(line, 3);
				break;

			case LEVELING:
			case DIRECTION:
			case HORIZONTAL_DISTANCE:
			case ZENITH_ANGLE:
			case SLOPE_DISTANCE:
				terrestrialObservationRow = TerrestrialObservationRow.scan(line, this.observationType);
				break;
			}
			
			String startPointName = null;
			if (terrestrialObservationRow != null)
				startPointName = terrestrialObservationRow.getStartPointName().trim();
			else if (gnssObservationRow != null)
				startPointName = gnssObservationRow.getStartPointName().trim();
			
			if (startPointName != null) {
				if (this.startPointName == null)
					this.startPointName = startPointName;
				this.isGroupWithEqualStation = this.isGroupWithEqualStation && this.startPointName.equals(startPointName);
				
				if (this.separateGroup && !this.startPointName.equals(startPointName)) {
					this.isGroupWithEqualStation = true;
					this.saveGroup(this.treeItemType, this.observations);
					this.startPointName = startPointName;
				}

				if (terrestrialObservationRow != null)
					this.observations.add(terrestrialObservationRow);
				else if (gnssObservationRow != null)
					this.gnss.add(gnssObservationRow);
			}
		}
		catch (Exception err) {
			return;
			// err.printStackTrace();
			// nichts, Beobachtung unbrauchbar...
		}	
	}
	
	private TreeItem<TreeItemValue> saveGroup(TreeItemType itemType, List<? extends ObservationRow> observations) throws SQLException {
		TreeItem<TreeItemValue> treeItem = null;
		if (!observations.isEmpty()) {
			String itemName = this.createItemName(null, this.isGroupWithEqualStation ? " (" + this.startPointName + ")" : null); 
			treeItem = this.saveObservations(itemName, itemType);
		}
		observations.clear();
		this.startPointName = null;
		this.isGroupWithEqualStation = true;
		return treeItem;
	}
	
	private TreeItem<TreeItemValue> saveObservations(String itemName, TreeItemType treeItemType) throws SQLException {
		if ((this.observations == null || this.observations.isEmpty()) && (this.gnss == null || this.gnss.isEmpty()))
			return null;

		TreeItemType parentType = TreeItemType.getDirectoryByLeafType(treeItemType);
		TreeItem<TreeItemValue> newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, itemName, true, false);
		try {
			SQLManager.getInstance().saveGroup((ObservationTreeItemValue)newTreeItem.getValue());
		} catch (SQLException e) {
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			throw new SQLException(e);
		}

		try {
			int groupId = ((ObservationTreeItemValue)newTreeItem.getValue()).getGroupId();
			if (!this.observations.isEmpty()) {
				for (TerrestrialObservationRow row : this.observations) {
					row.setGroupId(groupId);
					SQLManager.getInstance().saveItem(row);
				}
			}
			else if (!this.gnss.isEmpty()) {
				for (GNSSObservationRow row : this.gnss) {
					row.setGroupId(groupId);
					SQLManager.getInstance().saveItem(row);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}			

		return newTreeItem;
	}
}
