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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.row.PointRow;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.PointTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;

import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser.ExtensionFilter;

public class DL100FileReader extends SourceFileReader {
	private LevelingData levelingData = null;
	private Set<String> pointNames = null;
	private List<PointRow> points1d = null;
	private List<TerrestrialObservationRow> leveling = null;
	private TreeItem<TreeItemValue> lastTreeItem = null;

	private Date loopDate = null;
	private SimpleDateFormat dateFormatIn  = new SimpleDateFormat("yyMMddHHmm");
	private SimpleDateFormat dateFormatOut = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private String startPointName = null, loopId = null;
	private Double rb = null, distRb = null;
	
	public DL100FileReader() {
		this.reset();
	}
	
	public DL100FileReader(File f) {
		this(f.toPath());
	}

	public DL100FileReader(String s) {
		this(new File(s));
	}
	
	public DL100FileReader(Path p) {
		super(p);
		this.reset();
	}

	@Override
	public void reset() {	
		this.levelingData = null;
		this.loopDate = null;
		
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
		this.rb     = null;
		this.distRb = null;
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
		try {
			String data[] = line.split(",");
			
			// Zugstart/Zugende 
			if (line.toUpperCase().startsWith("B") || line.toUpperCase().startsWith("C") || line.toUpperCase().startsWith("W") || line.toUpperCase().startsWith("T") || line.toUpperCase().startsWith("Z")) {
				boolean startLevellingLoop = line.toUpperCase().startsWith("B") || line.toUpperCase().startsWith("C");
				// Zugbeginn: B = RV; C = RVVR/RRVV
				// b,28,J13,100021,+0,1410231113,,,,V,
				// c,28,J20,1,2.0,,,,,,,,,,,,,,111,+200000,1410300709,,,,S,
				
				if (startLevellingLoop && data.length > 2) {
					this.saveLevelingGroup();
					this.loopId = data[2].trim();
					
					// Bestimme Anschlusshoehe vom ersten Rueckblick
					char unit = data[1].trim().charAt(1);
					String pointName = null;
					Double z0 = null;
					if (line.toUpperCase().startsWith("B") && data.length > 5) {
						if (!data[5].trim().isEmpty())
							this.loopDate = this.dateFormatIn.parse(data[5].trim());
						if (!data[3].trim().isEmpty()) {
							pointName = data[3].trim();
							if (!this.pointNames.contains(pointName) && !data[4].trim().isEmpty()) 
								z0 = this.convertInputToTrueValue(unit, data[4].trim());
						}
					}
					else if (line.toUpperCase().startsWith("C") && data.length > 20) {
						if (!data[20].trim().isEmpty())
							this.loopDate = this.dateFormatIn.parse(data[20].trim());
						if (!data[18].trim().isEmpty()) {
							pointName = data[18].trim();
							if (!this.pointNames.contains(pointName) && !data[19].trim().isEmpty()) 
								z0 = this.convertInputToTrueValue(unit, data[19].trim());
						}
					}
					if (pointName != null && z0 != null && !pointName.isEmpty() && !this.pointNames.contains(pointName)) {
						PointRow point = new PointRow();
						point.setName(pointName);
						point.setZApriori(z0);
						this.points1d.add(point);
						this.pointNames.add(pointName);
					}
				}
				// Zugende - Fuege letzte Messung zur Gruppe hinzu
				this.addLevelingData(this.levelingData);

				this.levelingData = null;
				this.startPointName = null;
				this.rb     = null;
				this.distRb = null;
			}

			if (this.loopId != null && data.length > 7) {
				// Rueckblick g == r1; h == r2; Vorblick i == v1; j == v2.
				// g,28,+77616,+12012,+77616,3,0,1,1,1314,D,
				// g,28,+81288,+13125,+81463,3,0,2,1,1316,H,
				if (line.toUpperCase().startsWith("G") || line.toUpperCase().startsWith("H") || line.toUpperCase().startsWith("I") || line.toUpperCase().startsWith("J") || line.toUpperCase().startsWith("K")) {
					boolean isFirstBackSightMeasurment  = line.toUpperCase().startsWith("G");
					boolean isBackSightMeasurment = isFirstBackSightMeasurment || line.toUpperCase().startsWith("H");
					
					boolean isFirstForeSightMeasurment = line.toUpperCase().startsWith("I");
					//boolean isSecondForeSightMeasurment = line.toUpperCase().startsWith("J");
					
					boolean isSideShot = line.toUpperCase().startsWith("K") && this.startPointName != null && this.rb != null;
					
					if (data[1].trim().length() < 2 || data[7].trim().isEmpty())
						return;
					
					String pointName = data[7].trim();
					char unit = data[1].trim().charAt(1);
					//String information = String.format("%8s", Integer.toBinaryString((int)data[1].trim().charAt(0))).replace(' ', '0');				
					//System.out.println(information.substring(0, 4)+"  "+information.substring(4,6)+"  "+information.substring(6,8));	

					// Messwert, Strecke, Koordinate
					double m = 0.0, d = 0.0, z = 0.0;

					if (data[2].trim().isEmpty())
						return;
					m = this.convertMeasurmentToTrueValue(unit, data[2], true);
					
					if (!data[3].trim().isEmpty())
						d = this.convertMeasurmentToTrueValue(unit, data[3], false);
					
					if (!data[4].trim().isEmpty())
						z = this.convertMeasurmentToTrueValue(unit, data[4], true);

					if (isFirstBackSightMeasurment) {
						// Fuege zunaechst den letzten Datenblock zur Gruppe hinzu, 
						// bevor neuer Datensatz erzeugt wird.
						this.addLevelingData(this.levelingData);
						
						this.levelingData = new LevelingData();
						this.startPointName = pointName;
						this.rb     = m;
						this.distRb = d;
					}
					// Zwischenblick
					if (isSideShot) {
						LevelingData sideLevelingData = new LevelingData();
						sideLevelingData.addBackSightReading(this.startPointName, this.rb, this.distRb == null ? 0 : this.distRb);
						sideLevelingData.addForeSightReading(pointName, m, d);
						
						if (!this.pointNames.contains(pointName)) {
							PointRow point = new PointRow();
							point.setName(pointName);
							point.setZApriori(z);
							this.points1d.add(point);
							this.pointNames.add(pointName);
						}		
						this.addLevelingData(sideLevelingData);
					}
					// Normale Messung
					else if (this.levelingData != null) {
						if (isBackSightMeasurment)
							this.levelingData.addBackSightReading(pointName, m, d, isFirstBackSightMeasurment);
						else
							this.levelingData.addForeSightReading(pointName, m, d, isFirstForeSightMeasurment);

						if (!this.pointNames.contains(pointName)) {
							PointRow point = new PointRow();
							point.setName(pointName);
							point.setZApriori(z);
							this.points1d.add(point);
							this.pointNames.add(pointName);
						}
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
		if (!this.leveling.isEmpty() && this.loopId != null) {
			String prefix = this.loopId + " "+ (this.loopDate == null ? "" : "(" + this.dateFormatOut.format(this.loopDate) + ") ");
			String itemName = this.createItemName(prefix, null);
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.LEVELING_LEAF, this.leveling);
			this.leveling.clear();
		}
	}

	private void addLevelingData(LevelingData levelingData) {
		if (levelingData != null && levelingData.getStartPointName() != null && levelingData.getEndPointName() != null && !levelingData.getStartPointName().equals(levelingData.getEndPointName())) {
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
	
	/**
	 * Bestimmt anhand der kodierten Einheit den wahren Wert in Meter
	 * 
	 * @param unit
	 * @param data
	 * @param isStaffReading
	 * @return value
	 * @throws NumberFormatException
	 */
	private double convertMeasurmentToTrueValue(char unit, String data, boolean isStaffReading) throws NumberFormatException {
		double u1 = 1.0;
		double u2 = this.getUnitConversionFactor(unit);
		double d = Double.parseDouble( data.trim() );
				
		if (isStaffReading) { // Staff Readings
			switch (Character.toUpperCase(unit)) {			
				case '1': // 1  
					u1 = 1.0E-3;
				break;
				case '2': // 0.1 
				case '7':
					u1 = 1.0E-4;
				break;
				case '3': // 0.01 
				case '4':
				case '8':
					u1 = 1.0E-5;
				break;
				case '5': // 0.001
				case '9':
					u1 = 1.0E-6;
				break;
				case '6': // 0.0001
				case 'A':
					u1 = 1.0E-7;
				break;
				default:
				throw new NumberFormatException(this.getClass().getSimpleName() + ": Fehler "
						+ "beim Konvertieren des Messwertes " + data + ". Einheit " + unit + " unbekannt.");
			}
		}
		else { // Distance to Staff
			switch (Character.toUpperCase(unit)) {
				case '1':
				case '2':
				case '7':
					u1 = 1.0E-2;
				break;
				case '3':
				case '8':
					u1 = 1.0E-3;
				break;
				case '4':
				case '5':
				case '6':
				case '9':
				case 'A':
					u1 = 1.0E-5;
				break;
				default:
				throw new NumberFormatException(this.getClass().getSimpleName() + ": Fehler "
						+ "beim Konvertieren des Messwertes " + data + ". Einheit " + unit + " unbekannt.");
			}			
		}

		return u1 * u2 * d;
	}
	
	/**
	 * Bestimmt anhand der kodierten Einheit den wahren Wert in Meter
	 * 
	 * @param unit
	 * @param data
	 * @param isStaffReading
	 * @return value
	 * @throws NumberFormatException
	 */
	private double convertInputToTrueValue(char unit, String data) throws NumberFormatException {
		double u1 = 1.0;
		double u2 = this.getUnitConversionFactor(unit);
		double d = Double.parseDouble( data.trim() );
		
		switch (Character.toUpperCase(unit)) {
			case '1': // 1
			case '2':
				u1 = 1.0E-3;
			break;
			case '3': // 0.1
			case '7':
				u1 = 1.0E-4;
			break;
			case '4': // 0.01
			case '5':
			case '8':
				u1 = 1.0E-5;
			break;
			case '6': // 0.001
			case '9': 
				u1 = 1.0E-6;
			break;
			case 'A': // 0.0001
				u1 = 1.0E-7;
			break;
			default:
			throw new NumberFormatException(this.getClass().getSimpleName() + ": Fehler "
					+ "beim Konvertieren des Messwertes " + data + ". Einheit " + unit + " unbekannt.");			
		}

		return u1 * u2 * d;
	}
	
	private double getUnitConversionFactor(char unit) {
		double u = 1.0;
		switch (Character.toUpperCase(unit)) {
			case '4':
			case '5':
			case '6':
			case '9':
			case 'A':
				u = 0.3048;
			break;
		}
		return u;
	}
	
	public static ExtensionFilter[] getExtensionFilters() {
		return new ExtensionFilter[] {
				new ExtensionFilter(i18n.getString("DL100FileReader.extension.dl100", "DL-100 (Topcon)"), "*.l", "*.top")
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
}
