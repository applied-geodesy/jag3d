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
	
	private List<TerrestrialObservationRow> observations = new ArrayList<TerrestrialObservationRow>();
	private List<GNSSObservationRow> gnss = new ArrayList<GNSSObservationRow>();
	
	private boolean isDirectionGroupWithEqualStation = true;
	private String directionStartPointName = null;
	
	public ObservationFlatFileReader(ObservationType observationType) {
		this.observationType = observationType;
		this.treeItemType = TreeItemType.getTreeItemTypeByObservationType(observationType);
		if (this.treeItemType == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, observation type could not be transformed to tree item type. " + observationType);
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
	}
	
	@Override
	public void reset() {
		if (this.observations != null)
			this.observations.clear();
		if (this.gnss != null)
			this.gnss.clear();
		
		this.isDirectionGroupWithEqualStation = true;
		this.directionStartPointName = null;
	}
	
	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
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
					for (TerrestrialObservationRow row : this.observations)
						SQLManager.getInstance().saveItem(groupId, row);
				}
				else if (!this.gnss.isEmpty()) {
					for (GNSSObservationRow row : this.gnss)
						SQLManager.getInstance().saveItem(groupId, row);
				}

			} catch (SQLException e) {
				e.printStackTrace();
				throw new SQLException(e);
			}			
		}
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
			case GNSS2D:
			case GNSS3D:
				this.gnss.add(gnssObservationRow);
				break;

			case LEVELING:
			case DIRECTION:
			case HORIZONTAL_DISTANCE:
			case ZENITH_ANGLE:
			case SLOPE_DISTANCE:
				terrestrialObservationRow = TerrestrialObservationRow.scan(line, observationType);
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
				break;
			}
		}
		catch (Exception err) {
			return;
			// err.printStackTrace();
			// nichts, Beobachtung unbrauchbar...
		}	
	}

}
