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
import java.util.List;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.MathExtension;
import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.io.SourceFileReader;

import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser.ExtensionFilter;

public class GKAFileReader extends SourceFileReader<TreeItem<TreeItemValue>> {
	private boolean isNewStation = false;
	private String startPointName = null, lastStartPointName = null;
	private double ih = 0.0;
	private TreeItem<TreeItemValue> lastTreeItem = null;

	private List<TerrestrialObservationRow> horizontalDistances = null;
	private List<TerrestrialObservationRow> directions = null;

	private List<TerrestrialObservationRow> slopeDistances = null;
	private List<TerrestrialObservationRow> zenithAngles = null;

	public GKAFileReader() {
		this.reset();
	}
	
	public GKAFileReader(File f) {
		this(f.toPath());
	}

	public GKAFileReader(String s) {
		this(new File(s));
	}
	
	public GKAFileReader(Path p) {
		super(p);
		this.reset();
	}

	@Override
	public void reset() {
		this.isNewStation = false;
		
		this.startPointName     = null;
		this.lastStartPointName = null;
		
		this.ih = 0.0;

		if (this.horizontalDistances == null)
			this.horizontalDistances = new ArrayList<TerrestrialObservationRow>();
		if (this.directions == null)
			this.directions = new ArrayList<TerrestrialObservationRow>();
		if (this.slopeDistances == null)
			this.slopeDistances = new ArrayList<TerrestrialObservationRow>();
		if (this.zenithAngles == null)
			this.zenithAngles = new ArrayList<TerrestrialObservationRow>();

		this.horizontalDistances.clear();
		this.directions.clear();

		this.slopeDistances.clear();
		this.zenithAngles.clear();
	}
	
	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.lastTreeItem = null;
		this.reset();
		this.ignoreLinesWhichStartWith(";");
		
		super.read();

		// Speichere Daten
		this.saveObservationGroups(true);
		
		// Clear all lists
		this.reset();
		
		return this.lastTreeItem;
	}
	
	@Override
	public void parse(String line) throws SQLException {
		line = line.trim();
		
		if (line.startsWith("#GNV11")) {
			this.isNewStation = false;
			this.startPointName = null;
			this.ih = 0.0;
		}

		// Parse observations
		if (!this.isNewStation && this.startPointName != null && !this.startPointName.trim().isEmpty()) {
			// P_targ, Targname, GPS_ww, GPS_d, GPS_ss, nSatz, npos, s, ss, h_s, r, sr, z, sz, h_z, sadd, refr, bre, I_extar, EX_tar, EY_tar, EZ_tar, dTrunnion, dSighting, nErrorFlag, dPreasure, dTemperature, dSignalstrength, nAutoLockMode, nPrismNr, nTargetID, bDR
			//1,2007_2,2207,4,345660.92,1,2,201.73478,0.00120,0.000,265.9469,0.00020000,297.0004,0.00020000,0.000,0.000,0.142,0.0,1,,,,-0.0304,-0.0003,0,1022.10,12.90,,3,,,0
			//2,2008_2,2207,4,345669.75,1,1,154.83761,0.00115,0.000,51.4744,0.00020000,104.4044,0.00020000,0.000,0.000,0.142,0.0,1,,,,0.0244,-0.0029,0,1022.00,12.90,,3,,,0
			String columns[] = line.split(",");
			if (columns.length < 15)
				return;
			
			String endPointName = columns[1].trim();
			
			boolean isFaceII = columns[6].equalsIgnoreCase("2");
			double th = 0.0;
			
			// Target height for distance measurement
			try {th = Double.parseDouble(columns[9].trim());} catch(NumberFormatException e) {th = 0.0;};
			
			double prismConst = 0; 
			if (columns.length > 15)
				try {prismConst = Double.parseDouble(columns[15].trim());} catch(NumberFormatException e) {prismConst = 0.0;};

			double distance = 0;
			try {
				TerrestrialObservationRow obs = new TerrestrialObservationRow();
				obs.setStartPointName(this.startPointName);
				obs.setEndPointName(endPointName);
				obs.setInstrumentHeight(this.ih);
				obs.setReflectorHeight(th);
				distance = Double.parseDouble(columns[7].trim()); 
				if (distance > 0 && (distance + prismConst) > 0) {
					distance = distance + prismConst;
					obs.setValueApriori(distance);
					obs.setDistanceApriori(distance);
					this.slopeDistances.add(obs);
				}
			} catch(NumberFormatException e) { };
			
			// Target height for angle measurement
			try {th = Double.parseDouble(columns[14].trim());} catch(NumberFormatException e) {th = 0.0;};

			try {
				TerrestrialObservationRow obs = new TerrestrialObservationRow();
				obs.setStartPointName(this.startPointName);
				obs.setEndPointName(endPointName);
				obs.setInstrumentHeight(this.ih);
				obs.setReflectorHeight(th);
				double zenithAngle = Double.parseDouble(columns[12].trim()) * Constant.RHO_GRAD2RAD; 
				isFaceII = isFaceII || zenithAngle > Math.PI;
				if (isFaceII)
					zenithAngle = MathExtension.MOD(2.0*Math.PI - zenithAngle, 2.0*Math.PI);
				obs.setValueApriori(zenithAngle);
				if (distance > 0)
					obs.setDistanceApriori(distance);
				this.zenithAngles.add(obs);
			} catch(NumberFormatException e) { };
			
			try {
				TerrestrialObservationRow obs = new TerrestrialObservationRow();
				obs.setStartPointName(this.startPointName);
				obs.setEndPointName(endPointName);
				obs.setInstrumentHeight(this.ih);
				obs.setReflectorHeight(th);
				double direction = Double.parseDouble(columns[10].trim()) * Constant.RHO_GRAD2RAD; 
				if (isFaceII)
					direction = MathExtension.MOD(direction + Math.PI, 2.0*Math.PI);
				obs.setValueApriori(direction);
				if (distance > 0)
					obs.setDistanceApriori(distance);
				this.directions.add(obs);
			} catch(NumberFormatException e) { };
						
		}
		
		// Parse station
		if (this.isNewStation) {
			// P_tach, Tachname, orient, nsatzr, nsatzz, h_tach, I_extach, EX_tach, EY_tach, EZ_tach, ori, J,N,	nWeatherFlag, nBattery
			// 38140004,Ost,0,0,0,0.00000000,1,,,,0.0,278.77885605,80.65533842,1,100
			String columns[] = line.split(",");
			if (columns.length < 6)
				return;
			this.startPointName = columns[1].trim();
			if (this.lastStartPointName == null)
				this.lastStartPointName = this.startPointName;
			try {this.ih = Double.parseDouble(columns[5].trim());} catch(NumberFormatException e) {this.ih = 0.0;};
			this.isNewStation = false;
		}
		
		if (line.startsWith("#GNV11")) {
			// Speichere Daten bspw. Richtungen, da diese Satzweise zu halten sind
			this.saveObservationGroups(false);
			this.lastStartPointName = this.startPointName;
			this.isNewStation = true;
			this.startPointName = null;
			this.ih = 0.0;
		}
	}
	
	private void saveObservationGroups(boolean forceSaving) throws SQLException {
		if (!this.directions.isEmpty() && (forceSaving || ImportOption.getInstance().isGroupSeparation(ObservationType.DIRECTION)))
			this.lastTreeItem = this.saveObservationGroup(TreeItemType.DIRECTION_LEAF, this.directions);
		
		if (!this.horizontalDistances.isEmpty() && (forceSaving || ImportOption.getInstance().isGroupSeparation(ObservationType.HORIZONTAL_DISTANCE)))
			this.lastTreeItem = this.saveObservationGroup(TreeItemType.HORIZONTAL_DISTANCE_LEAF, this.horizontalDistances);
		
		if (!this.slopeDistances.isEmpty() && (forceSaving || ImportOption.getInstance().isGroupSeparation(ObservationType.SLOPE_DISTANCE)))
			this.lastTreeItem = this.saveObservationGroup(TreeItemType.SLOPE_DISTANCE_LEAF, this.slopeDistances);

		if (!this.zenithAngles.isEmpty() && (forceSaving || ImportOption.getInstance().isGroupSeparation(ObservationType.ZENITH_ANGLE)))
			this.lastTreeItem = this.saveObservationGroup(TreeItemType.ZENITH_ANGLE_LEAF, this.zenithAngles);
	}
	
	private TreeItem<TreeItemValue> saveObservationGroup(TreeItemType itemType, List<TerrestrialObservationRow> observations) throws SQLException {
		TreeItem<TreeItemValue> treeItem = null;
		if (!observations.isEmpty()) {
			boolean isGroupWithEqualStation = ImportOption.getInstance().isGroupSeparation(TreeItemType.getObservationTypeByTreeItemType(itemType));
			String itemName = this.createItemName(null, isGroupWithEqualStation && this.lastStartPointName != null ? " (" + this.lastStartPointName + ")" : null); 
			treeItem = this.saveTerrestrialObservations(itemName, itemType, observations);
		}
		observations.clear();
		return treeItem;
	}
	
	private TreeItem<TreeItemValue> saveTerrestrialObservations(String itemName, TreeItemType treeItemType, List<TerrestrialObservationRow> observations) throws SQLException {
		if (observations == null || observations.isEmpty())
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
			for (TerrestrialObservationRow row : observations) {
				row.setGroupId(groupId);
				SQLManager.getInstance().saveItem(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}			

		return newTreeItem;
	}
	
	public static ExtensionFilter[] getExtensionFilters() {
		return new ExtensionFilter[] {
				new ExtensionFilter(I18N.getInstance().getString("GKAFileReader.extension.gka", "GKA (Trimble)"), "*.gka", "*.GKA")
		};
	}

}
