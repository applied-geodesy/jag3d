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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.jag3d.ui.table.row.PointRow;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.PointTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.io.SourceFileReader;

import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser.ExtensionFilter;

public class M5FileReader extends SourceFileReader<TreeItem<TreeItemValue>> {
	private String startPointName = null, loopId = null, lastPointNameForeSightReading = null;
	private Double lastRb4Zb = null, distLastRb4Zb;
	private long cnt = 0;
	private LevelingData levelingData = null;
	private Set<String> pointNames = new HashSet<String>();
	private List<PointRow> points1d = null;
	private List<TerrestrialObservationRow> leveling = null;
	private TreeItem<TreeItemValue> lastTreeItem = null;
	
	public M5FileReader() {
		this.reset();
	}
	
	public M5FileReader(File f) {
		this(f.toPath());
	}
	
	public M5FileReader(String s) {
		this(new File(s));
	}
	
	public M5FileReader(Path p) {
		super(p);
		this.reset();
	}

	@Override
	public void reset() {
		this.levelingData = null;
		
		if (this.points1d == null)
			this.points1d = new ArrayList<PointRow>();
		
		if (this.leveling == null)
			this.leveling = new ArrayList<TerrestrialObservationRow>();
		
		if (this.pointNames == null)
			this.pointNames = new HashSet<String>();
		
		this.pointNames.clear();
		this.points1d.clear();
		this.leveling.clear();
		
		this.startPointName = null;
		this.lastPointNameForeSightReading = null;
		
		this.cnt = 0;
	}

	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.reset();
		this.pointNames.addAll(SQLManager.getInstance().getFullPointNameSet());
		this.lastTreeItem = null;
		this.ignoreLinesWhichStartWith("#");
		
		super.read();

		String itemName = this.createItemName(null, null);
		
		// Speichere Punkte
		if (!this.points1d.isEmpty())
			this.lastTreeItem = this.savePoints(itemName, TreeItemType.DATUM_POINT_1D_LEAF, this.points1d);
		
		// Speichere Daten
		this.saveLevelingGroup();

		return this.lastTreeItem;
	}

	@Override
	public void parse(String line) { 
		// an error in M5 is denoted by an E after the last |-seperator
		if (!line.toUpperCase().startsWith("FOR M5") || line.length() < 118 || !line.trim().endsWith("|"))
			return;

		String T2a    = line.substring( 17, 20).trim();
		String value2 = line.substring( 21, 48);
		
		String T3     = line.substring( 49, 51).trim();
		String value3 = line.substring( 52, 66);
		String unit3  = line.substring( 67, 71);
		
		String T4     = line.substring( 72, 74).trim();
		String value4 = line.substring( 75, 89);
		String unit4  = line.substring( 90, 94);
		
		String T5     = line.substring( 95, 97).trim();
		String value5 = line.substring( 98,112);
		String unit5  = line.substring(113,117);
		
		if (value2.contains("#####") || !T2a.startsWith("KD") && !T2a.startsWith("KN")) 
			return;

		try {
//			Pattern pattern = Pattern.compile( "\\s*(\\w+).+" );
//			Matcher matcher = pattern.matcher( value2 );
//			if (matcher.matches()) {
//				String pointName = matcher.group(1);

			if (value2.matches("\\s*(\\w+).+") && value2.length() >= 27) {
				String pointName = value2.substring( 0, 8).trim();
				String code    = value2.substring( 8,13).trim();
				String currentLoopId = value2.substring(23,27).trim();

				if (this.loopId != null && !this.loopId.equals(currentLoopId)) 
					this.saveLevelingGroup();
				
				this.loopId = currentLoopId;

				// Rueckblick
				if (T3.equals("Lr") || T3.equals("Rb")) {
					double d = 0;
					double r = Double.parseDouble(value3.trim());
					r = unit3.toLowerCase().trim().equals("ft") ? 0.3048 * r : r;
					
					// Pruefe, ob es eine Strecke gibt
					if (T4.equals("E") || T4.equals("HD")) {
						d = Double.parseDouble(value4.trim());
						d = unit4.toLowerCase().trim().equals("ft") ? 0.3048 * d : d;
					}
					
					if (this.levelingData == null)
						this.levelingData = new LevelingData();
					
					// Pruefe, ob der Rueckblick eine Pkt-Id besitzt, 
					// verwende ansonsten die letzte guelte Bezeichnung bzw.
					// generiere eine neue Pkt-Id.
					// --> Manche M5-Files enthalten keine Pkt fuer Wechselpunkte
					if (pointName.isBlank() && !this.levelingData.hasFirstBackSightReading()) {
						pointName = this.lastPointNameForeSightReading == null || this.lastPointNameForeSightReading.isBlank() ? generatePointId(++this.cnt) : this.lastPointNameForeSightReading;
						code      = "JAG3D";
					}
					
					if (!this.levelingData.hasFirstBackSightReading()) {
						// Speichere Punktname und Messung vom letzten Rueckblick fuer mgl. Zwischenblicke R-I
						this.startPointName = pointName;
						this.lastRb4Zb     = r;
						this.distLastRb4Zb = d;
					}
					else { // Speichere letzten Rueckblick fuer mgl. Zwischenblicke R-II bei bspw. RVVR
						this.lastRb4Zb     = 0.5 * (this.lastRb4Zb + r);
						this.distLastRb4Zb = 0.5 * (this.distLastRb4Zb + d);
					}
					
					this.levelingData.addBackSightReading(pointName, r, d);
				}
				// Vorblick
				else if (T3.equals("Lv") || T3.equals("Rf")) {
					double d = 0;
					double v = Double.parseDouble(value3.trim());
					v = unit3.toLowerCase().trim().equals("ft") ? 0.3048 * v : v;
					
					// Pruefe, ob es eine Strecke gibt
					if (T4.equals("E") || T4.equals("HD")) {
						d = Double.parseDouble(value4.trim());
						d = unit4.toLowerCase().trim().equals("ft") ? 0.3048 * d : d;
					}
					
					if (this.levelingData == null)
						this.levelingData = new LevelingData();
					
					// Pruefe, ob der Vorblick eine Pkt-Id besitzt, 
					// generiere ansonsten eine Pkt-Id.
					// --> Manche M5-Files enthalten keine Pkt fuer Wechselpunkte
					if (pointName.isBlank() && !this.levelingData.hasFirstForeSightReading()) {
						pointName = generatePointId(++this.cnt);
						code      = "JAG3D";
					}
					
					this.levelingData.addForeSightReading(pointName, v, d);
				}
				// Zwischenblick
				else if (T3.equals("Lz") || T3.equals("Rz")) {
					double d = 0;
					double s = Double.parseDouble(value3.trim());
					s = unit3.toLowerCase().trim().equals("ft") ? 0.3048 * s : s;
					
					// Pruefe, ob es eine Strecke gibt
					if (T4.equals("E") || T4.equals("HD")) {
						d = Double.parseDouble(value4.trim());
						d = unit4.toLowerCase().trim().equals("ft") ? 0.3048 * d : d;
					}
					
					// Zwischenblicke werden direkt ausgewertet, sodass Z direkt verfuegbar ist
					if (T5.equals("Z") && !this.pointNames.contains(pointName)) {
						double z = Double.parseDouble(value5.trim());
						z = unit5.toLowerCase().trim().equals("ft") ? 0.3048 * z : z;
						PointRow point = new PointRow();
						point.setName(pointName);
						point.setCode(code);
						point.setZApriori(z);
						this.points1d.add(point);
						this.pointNames.add(pointName);
					}
					
					if (this.lastRb4Zb != null) {
						LevelingData sideLevelingData = new LevelingData();
						sideLevelingData.addBackSightReading(this.startPointName, this.lastRb4Zb, this.distLastRb4Zb == null ? 0 : this.distLastRb4Zb);
						sideLevelingData.addForeSightReading(pointName, s, d);
						if (sideLevelingData != null)
							this.addLevelingData(sideLevelingData);
					}
				}
				else if (T5.equals("Z")) {
					if (this.levelingData != null) {
						this.addLevelingData(this.levelingData);
						this.lastPointNameForeSightReading = this.levelingData.getEndPointName();
						this.levelingData = null;
					}
					
					// Pruefe, ob der Punkt eine Pkt-Id besitzt, 
					// verwende ansonsten die letzte guelte Bezeichnung
					// --> Manche M5-Files enthalten keine Pkt fuer Wechselpunkte
					if (pointName.isBlank()) {
						pointName = this.lastPointNameForeSightReading;
						code      = "JAG3D";
					}
					if (!this.pointNames.contains(pointName)) {
						double z = Double.parseDouble(value5.trim());
						z = unit5.toLowerCase().trim().equals("ft") ? 0.3048 * z : z;
						PointRow point = new PointRow();
						point.setName(pointName);
						point.setCode(code);
						point.setZApriori(z);
						this.points1d.add(point);
						this.pointNames.add(pointName);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}		
	}
	
	private void saveLevelingGroup() throws SQLException {
		if (!this.leveling.isEmpty()) {
			String prefix = this.loopId + " ";
			String itemName = this.createItemName(prefix, null);
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.LEVELING_LEAF, this.leveling);
			this.leveling.clear();
			this.lastPointNameForeSightReading = null;
			this.cnt = 0;
		}
	}
	
	private void addLevelingData(LevelingData levelingData) {
		if (levelingData != null) {
			double dist2D = levelingData.getDistance();
			double deltaH = levelingData.getDeltaH();
			
			TerrestrialObservationRow obs = new TerrestrialObservationRow();
			obs.setStartPointName(levelingData.getStartPointName());
			obs.setEndPointName(levelingData.getEndPointName());
			
			obs.setInstrumentHeight(0.0);
			obs.setReflectorHeight(0.0);
			
			if (dist2D > 0)
				obs.setDistanceApriori(dist2D);
			
			obs.setValueApriori(deltaH);
			this.leveling.add(obs);
		}
	}
	
	public static ExtensionFilter[] getExtensionFilters() {
		return new ExtensionFilter[] {
				new ExtensionFilter(I18N.getInstance().getString("M5FileReader.extension.m5", "M5 (DiNi)"), "*.m5", "*.rec", "*.dat", "*.din", "*.M5", "*.REC", "*.DAT", "*.DIN"),
				new ExtensionFilter(I18N.getInstance().getString("FlatFileReader.extension", "All files"), "*.*")
		};
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
				//SQLManager.getInstance().saveItem(groupId, row);
				row.setGroupId(groupId);
				SQLManager.getInstance().saveItem(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}			

		return newTreeItem;
	}
	
	private TreeItem<TreeItemValue> savePoints(String itemName, TreeItemType treeItemType, List<PointRow> points) throws SQLException {
		if (points == null || points.isEmpty())
			return null;

		TreeItemType parentType = TreeItemType.getDirectoryByLeafType(treeItemType);
		TreeItem<TreeItemValue> newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, itemName, true, false);
		try {
			SQLManager.getInstance().saveGroup((PointTreeItemValue)newTreeItem.getValue());
		} catch (SQLException e) {
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			throw new SQLException(e);
		}

		try {
			int groupId = ((PointTreeItemValue)newTreeItem.getValue()).getGroupId();
			for (PointRow row : points) {
				row.setGroupId(groupId);
				SQLManager.getInstance().saveItem(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		}			

		return newTreeItem;
	}
	
	private static String generatePointId(long cnt) {
		LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("UTC"));
		long doy   = localDateTime.getDayOfYear();
		long musec = localDateTime.getNano()/1000;
		long sec   = localDateTime.getSecond();
		long min   = localDateTime.getMinute();
		long hour  = localDateTime.getHour();
		long musod = (hour * 3600L + min * 60L + sec) * 1000000L + musec;
		int salt   = (int)Math.abs(Math.random()*1000);
		return String.format(Locale.ENGLISH, "%c%04d%03d%011d%03d", 'W', cnt, doy, musod, salt);
	}
}
