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

public class BeoFileReader extends SourceFileReader<TreeItem<TreeItemValue>> {
	private boolean isNewStation = false;
	private String startPointName = null, lastStartPointName = null;
	private double ih = 0.0;
	private TreeItem<TreeItemValue> lastTreeItem = null;

	private List<TerrestrialObservationRow> leveling = null;

	private List<TerrestrialObservationRow> horizontalDistances = null;
	private List<TerrestrialObservationRow> directions = null;

	private List<TerrestrialObservationRow> slopeDistances = null;
	private List<TerrestrialObservationRow> zenithAngles = null;

	public BeoFileReader() {
		this.reset();
	}
	
	public BeoFileReader(File f) {
		this(f.toPath());
	}

	public BeoFileReader(String s) {
		this(new File(s));
	}
	
	public BeoFileReader(Path p) {
		super(p);
		this.reset();
	}

	@Override
	public void reset() {
		this.isNewStation = false;
		
		this.startPointName     = null;
		this.lastStartPointName = null;
		
		this.ih = 0.0;
		
		if (this.leveling == null)
			this.leveling = new ArrayList<TerrestrialObservationRow>();
		if (this.horizontalDistances == null)
			this.horizontalDistances = new ArrayList<TerrestrialObservationRow>();
		if (this.directions == null)
			this.directions = new ArrayList<TerrestrialObservationRow>();
		if (this.slopeDistances == null)
			this.slopeDistances = new ArrayList<TerrestrialObservationRow>();
		if (this.zenithAngles == null)
			this.zenithAngles = new ArrayList<TerrestrialObservationRow>();

		this.leveling.clear();

		this.horizontalDistances.clear();
		this.directions.clear();

		this.slopeDistances.clear();
		this.zenithAngles.clear();
	}

	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.lastTreeItem = null;
		this.reset();
		this.ignoreLinesWhichStartWith("#");
		
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

		if (line.length() < 89)
			return;

		String key = line.substring(0, 3).trim();
		// Standpunkt
		if (key.equalsIgnoreCase("10.")) {
			this.startPointName = line.substring(15, 28).trim();
			if (this.startPointName.isBlank())
				return;


			this.isNewStation = this.startPointName != null;
			try { 
				this.ih = Double.parseDouble(line.substring(79, line.length()).trim().replaceAll("\\s+.*", ""));
				this.ih = this.ih == -1000.0 ? 0 : this.ih;
			} 
			catch(NumberFormatException e) { };
		}
		
		else if (this.startPointName != null && !this.startPointName.isBlank() && (key.equalsIgnoreCase("20.") || key.equalsIgnoreCase("21.") || key.equalsIgnoreCase("24.") || key.equalsIgnoreCase("31."))) {
			if (this.isNewStation) {
				this.saveObservationGroups(false);
				this.lastStartPointName = null;
			}
			this.isNewStation = false;

			String endPointName = line.substring(15, 28).trim();
			if (endPointName.isBlank() || this.startPointName.equals(endPointName))
				return;

			double th = 0.0;
			try { 
				th = Double.parseDouble(line.substring(79, line.length()).trim().replaceAll("\\s+.*", ""));
				th = th == -1000.0 ? 0 : th;
			} catch(NumberFormatException e) { };
			
			if (this.lastStartPointName == null)
				this.lastStartPointName = this.startPointName;

			boolean isFaceI = true;
			// Strecke3D
			String value = line.substring(33, 49).trim();
			double distance = 0;
			if (!value.isBlank() && !value.startsWith("-1000.")) {
				try { 
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(this.startPointName);
					obs.setEndPointName(endPointName);
					obs.setInstrumentHeight(this.ih);
					obs.setReflectorHeight(th);
					distance = Double.parseDouble(value); 
					if (distance > 0) {
						obs.setValueApriori(distance);
						obs.setDistanceApriori(distance);
						this.slopeDistances.add(obs);
					}
				} 
				catch(NumberFormatException e) { };
			}
			// Zenitwinkel
			value = line.substring(64, 79).trim();
			double zenith = 0.5 * Math.PI;
			if (!value.isBlank() && !value.startsWith("-1000.")) {
				try { 
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(this.startPointName);
					obs.setEndPointName(endPointName);
					obs.setInstrumentHeight(this.ih);
					obs.setReflectorHeight(th);
					zenith = Double.parseDouble(value) * Constant.RHO_GRAD2RAD;
					isFaceI = (zenith <= Math.PI);
					if (!isFaceI)
						zenith = MathExtension.MOD(2.0*Math.PI - zenith, 2.0*Math.PI);
					obs.setValueApriori(zenith);
					if (distance > 0)
						obs.setDistanceApriori(distance);
					this.zenithAngles.add(obs);
				} 
				catch(NumberFormatException e) { };
			}
			// Richtung
			value = line.substring(49, 64).trim();
			if (!value.isBlank() && !value.startsWith("-1000.")) {
				try {
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(this.startPointName);
					obs.setEndPointName(endPointName);
					obs.setInstrumentHeight(this.ih);
					obs.setReflectorHeight(th);
					double dir = Double.parseDouble(value) * Constant.RHO_GRAD2RAD;
					if (!isFaceI)
						dir = MathExtension.MOD(dir + Math.PI, 2.0*Math.PI);
					obs.setValueApriori(dir);
					if (distance > 0)
						obs.setDistanceApriori(Math.abs(zenith) > SQRT_EPS ? distance * Math.sin(zenith) : distance * Math.sin(SQRT_EPS));
					this.directions.add(obs);
				} 
				catch(NumberFormatException e) { };
			}
		}
		else if (this.startPointName != null && !this.startPointName.isBlank() && key.equalsIgnoreCase("50.")) {
			if (this.isNewStation) {
				this.saveObservationGroups(false);
				this.lastStartPointName = null;
			}
			this.isNewStation = false;

			String endPointName = line.substring(15, 28).trim();
			if (endPointName.isBlank())
				return;

			double th = 0.0;
			try { 
				th = Double.parseDouble(line.substring(79, line.length()).trim().replaceAll("\\s+.*", "")); 
				th = th == -1000.0 ? 0 : th;
			} catch(NumberFormatException e) { };
			 	
			if (this.lastStartPointName == null)
				this.lastStartPointName = this.startPointName;

			// Strecke2D
			double distance = 0.0; // Wird ggf. fuer delta-H mitverwendet
			String value = line.substring(33, 49).trim();
			if (!value.isBlank() && !value.startsWith("-1000.")) {
				try { 
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(this.startPointName);
					obs.setEndPointName(endPointName);
					obs.setInstrumentHeight(this.ih);
					obs.setReflectorHeight(th);
					distance = Double.parseDouble(value); 
					if (distance > 0) {
						obs.setValueApriori(distance);
						obs.setDistanceApriori(distance);
						this.horizontalDistances.add(obs);
					}
				} 
				catch(NumberFormatException e) { };
			}
			// Richtung
			value = line.substring(49, 64).trim();
			if (!value.isBlank() && !value.startsWith("-1000.")) {
				try {
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(this.startPointName);
					obs.setEndPointName(endPointName);
					obs.setInstrumentHeight(this.ih);
					obs.setReflectorHeight(th);
					double dir = Double.parseDouble(value) * Constant.RHO_GRAD2RAD; 
					obs.setValueApriori(dir);
					if (distance > 0)
						obs.setDistanceApriori(distance);
					this.directions.add(obs);
				} 
				catch(NumberFormatException e) { };
			}
			// Hoehenunterschied
			value = line.substring(64, 79).trim();
			if (!value.isBlank() && !value.startsWith("-1000.")) {
				try { 
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(this.startPointName);
					obs.setEndPointName(endPointName);
					obs.setInstrumentHeight(this.ih);
					obs.setReflectorHeight(th);
					double dh = Double.parseDouble(value); 
					obs.setValueApriori(dh);
					if (distance > 0)
						obs.setDistanceApriori(distance);
					this.leveling.add(obs);
				} 
				catch(NumberFormatException e) { };
			}
		}
		else if (this.startPointName != null && !this.startPointName.isBlank() && key.equalsIgnoreCase("70.")) {
			if (this.isNewStation) {
				this.saveObservationGroups(false);
				this.lastStartPointName = null;
			}
			this.isNewStation = false;

			String endPointName = line.substring(15, 28).trim();
			if (endPointName.isBlank())
				return;

			double th = 0.0;
			try { 
				th = Double.parseDouble(line.substring(79, line.length()).trim().replaceAll("\\s+.*", "")); 
				th = th == -1000.0 ? 0 : th;
			} catch(NumberFormatException e) { };

			// Hoehenunterschied
			String value = line.substring(64, 79).trim();
			if (!value.isBlank() && !value.startsWith("-1000.")) {
				try { 
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(this.startPointName);
					obs.setEndPointName(endPointName);
					obs.setInstrumentHeight(this.ih);
					obs.setReflectorHeight(th);
					double dh = Double.parseDouble(value); 
					obs.setValueApriori(dh);
					try { 
						obs.setDistanceApriori(Double.parseDouble(line.substring(33, 49).trim())); 
					} catch(NumberFormatException e) { };
					this.leveling.add(obs);
				} 
				catch(NumberFormatException e) { };
			}
		}
	}
	
	private void saveObservationGroups(boolean forceSaving) throws SQLException {
		if (!this.leveling.isEmpty() && (forceSaving || ImportOption.getInstance().isGroupSeparation(ObservationType.LEVELING))) 
			this.lastTreeItem = this.saveObservationGroup(TreeItemType.LEVELING_LEAF, this.leveling);
		
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
				new ExtensionFilter(I18N.getInstance().getString("BeoFileReader.extension.beo", "Beo (Neptan)"), "*.beo", "*.BEO"),
				new ExtensionFilter(I18N.getInstance().getString("FlatFileReader.extension", "All files"), "*.*")
		};
	}
}
