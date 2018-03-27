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
import java.util.List;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.row.GNSSObservationRow;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;

import javafx.scene.control.TreeItem;

public class ObservationFlatFileReader extends SourceFileReader {
	private final ObservationType observationType;
	private final TreeItemType treeItemType;
	
	private List<TerrestrialObservationRow> observations = null;
	private List<GNSSObservationRow> gnss = null;
	
	private boolean isDirectionGroupWithEqualStation = true;
	private String directionStartPointName = null;
	
	public ObservationFlatFileReader(ObservationType observationType) {
		this.observationType = observationType;
		this.treeItemType = TreeItemType.getTreeItemTypeByObservationType(observationType);
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
		
		this.isDirectionGroupWithEqualStation = true;
		this.directionStartPointName = null;
	}
	
	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.reset();
		this.ignoreLinesWhichStartWith("#");
		TreeItem<TreeItemValue> newTreeItem = null;
		if (this.observationType != ObservationType.DIRECTION)
			this.isDirectionGroupWithEqualStation = false;
		
		super.read();
		 
		if (!this.observations.isEmpty() || !this.gnss.isEmpty()) {
			String itemName = this.createItemName(null, this.isDirectionGroupWithEqualStation && this.directionStartPointName != null ? " (" + this.directionStartPointName + ")" : null);
			TreeItemType parentType = TreeItemType.getDirectoryByLeafType(this.treeItemType);
			newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, itemName, true, false);
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
			
			if (terrestrialObservationRow != null) {
				if (this.observationType == ObservationType.DIRECTION) {
					if (this.directionStartPointName == null)
						this.directionStartPointName = terrestrialObservationRow.getStartPointName();
					this.isDirectionGroupWithEqualStation = this.isDirectionGroupWithEqualStation && this.directionStartPointName.equals(terrestrialObservationRow.getStartPointName());
				}
				else
					this.directionStartPointName = null;

				this.observations.add(terrestrialObservationRow);
			}
			
			if (gnssObservationRow != null)
				this.gnss.add(gnssObservationRow);
			
		}
		catch (Exception err) {
			return;
			// err.printStackTrace();
			// nichts, Beobachtung unbrauchbar...
		}	
	}
}
