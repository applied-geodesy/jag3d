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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.applied_geodesy.adjustment.Constant;
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

public class ZFileReader extends SourceFileReader {
	private Map<String, Double> targetHeights = new HashMap<String, Double>();

	private Set<String> pointNames = null;

	private Map<String, String> pointNames1D = null;
	private Map<String, String> pointNames2D = null;
	private Map<String, String> pointNames3D = null;

	private List<PointRow> points1d = null;
	private List<PointRow> points2d = null;
	private List<PointRow> points3d = null;

	private String startPointName = null, startPointCode = "";
	private double ih = 0.0;
	private int cnt = 0;
	private boolean is2DStartPoint = false;

	private List<TerrestrialObservationRow> leveling = null;

	private List<TerrestrialObservationRow> horizontalDistances = null;
	private List<TerrestrialObservationRow> directions = null;

	private List<TerrestrialObservationRow> slopeDistances = null;
	private List<TerrestrialObservationRow> zenithAngles = null;

	private LevelingData levelingData = null;
	private String lastPointId = null, project = null;
	private Double rb = null, distRb = null;

	private TreeItem<TreeItemValue> lastTreeItem = null;

	public ZFileReader() {
		this.reset();
	}
	
	public ZFileReader(String s) {
		this(new File(s));
	}

	public ZFileReader(File f) {
		this(f.toPath());
	}
	
	public ZFileReader(Path p) {
		super(p);
		this.reset();
	}

	@Override
	public void reset() {
		this.levelingData = null;
		this.lastPointId  = null;
		this.startPointName = null;
		this.startPointCode = "";
		this.is2DStartPoint = false;

		this.ih = 0.0;
		this.rb = null;
		this.distRb = null;

		if (this.points1d == null)
			this.points1d = new ArrayList<PointRow>();
		if (this.points2d == null)
			this.points2d = new ArrayList<PointRow>();
		if (this.points3d == null)
			this.points3d = new ArrayList<PointRow>();

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
		if (this.pointNames == null)
			this.pointNames = new HashSet<String>();
		
		if (this.pointNames1D == null)
			this.pointNames1D = new LinkedHashMap<String, String>();
		if (this.pointNames2D == null)
			this.pointNames2D = new LinkedHashMap<String, String>();
		if (this.pointNames3D == null)
			this.pointNames3D = new LinkedHashMap<String, String>();

		this.pointNames.clear();
		
		this.pointNames1D.clear();
		this.pointNames2D.clear();
		this.pointNames3D.clear();

		this.points1d.clear();
		this.points2d.clear();
		this.points3d.clear();

		this.leveling.clear();

		this.horizontalDistances.clear();
		this.directions.clear();

		this.slopeDistances.clear();
		this.zenithAngles.clear();
	}

	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.reset();
		this.pointNames.addAll(SQLManager.getInstance().getFullPointNameSet());
		this.lastTreeItem = null;
		this.ignoreLinesWhichStartWith("!");

		super.read();

		String itemName = this.createItemName(null, null);
		this.ignoreLinesWhichStartWith("!");

		// Speichere Daten
		this.saveObservationGroups();

		// Normalisiere Punkte
		this.normalizePointNames();

		// Erzeuge Punkte
		if (!this.pointNames1D.isEmpty()) {
			for (Map.Entry<String, String> entry : this.pointNames1D.entrySet()) {
				String pid  = entry.getKey();
				String code = entry.getValue();
				if (!this.pointNames.contains(pid)) {
					PointRow point = new PointRow();
					point.setName(pid);
					point.setCode(code);
					this.points1d.add(point);
				}
			}
		}
		if (!this.pointNames2D.isEmpty()) {
			for (Map.Entry<String, String> entry : this.pointNames2D.entrySet()) {
				String pid  = entry.getKey();
				String code = entry.getValue();
				if (!this.pointNames.contains(pid)) {
					PointRow point = new PointRow();
					point.setName(pid);
					point.setCode(code);
					this.points2d.add(point);
				}
			}
		}
		if (!this.pointNames3D.isEmpty()) {
			for (Map.Entry<String, String> entry : this.pointNames3D.entrySet()) {
				String pid  = entry.getKey();
				String code = entry.getValue();
				if (!this.pointNames.contains(pid)) {
					PointRow point = new PointRow();
					point.setName(pid);
					point.setCode(code);
					this.points3d.add(point);
				}
			}
		}

		// Speichere Punkte
		if (!this.pointNames1D.isEmpty())
			this.lastTreeItem = this.savePoints(itemName, TreeItemType.DATUM_POINT_1D_LEAF, this.points1d);

		if (!this.pointNames2D.isEmpty())
			this.lastTreeItem = this.savePoints(itemName, TreeItemType.DATUM_POINT_2D_LEAF, this.points2d);

		if (!this.pointNames3D.isEmpty())
			this.lastTreeItem = this.savePoints(itemName, TreeItemType.DATUM_POINT_3D_LEAF, this.points3d);

		this.pointNames.addAll(this.pointNames1D.keySet());
		this.pointNames.addAll(this.pointNames2D.keySet());
		this.pointNames.addAll(this.pointNames3D.keySet());

		this.pointNames1D.clear();
		this.pointNames2D.clear();
		this.pointNames3D.clear();

		return this.lastTreeItem;
	}

	@Override
	public void parse(String line) {
		try {
			if (line.length() < 32 || !line.substring(5, 7).matches("\\d{2}"))
				return;

			int c1 = Integer.parseInt(line.substring(5, 6));
			int c2 = Integer.parseInt(line.substring(6, 7));

			String key1 = line.substring( 8, 15).trim();
			String key2 = line.substring(16, 32).trim();

			if (c1 < 1)
				this.saveObservationGroups();

			if (c1 == 0 && c2 == 4) {
				this.project = line.substring(16, 32).trim();
			}

			// 10 --> Standpunkt
			if (c1 == 1 && c2 == 0) {
				//Zwischenspeichern der Richtungen, da diese eine Orientierungsunbekannte haben
				this.saveDirectionGroup();

				// Neuer Standpunkt
				this.startPointName = key2;
				this.startPointCode = "";
				this.ih = 0;
				this.targetHeights.clear();

				if (key1.substring(2).matches("\\d+")) {
					this.is2DStartPoint = key1.substring(2).matches("^9+$");
					if (!this.is2DStartPoint)
						try {this.ih = 0.001 * Integer.parseInt(key1.substring(2).trim());} catch(Exception e){}
				}

				if (line.substring(70, 71).equals("|") && line.length() > 71) {
					String attributes[] = line.substring(71, line.length()).split("\\|");
					if (attributes.length > 0)
						this.startPointCode = attributes[0].trim();
				}
			}

			else if (this.startPointName != null && !this.startPointName.isEmpty() && line.length() >= 71 && c1 >= 2 && c1 <= 8 && c2 >= 0 && c2 <= 4 && line.charAt(68) != '*') {
				String endPointId = key2;
				double th = 0;
				boolean is2DEndPoint = key1.substring(2).matches("^9+$");
				if (!is2DEndPoint) {
					if (key1.substring(2).matches("^0+$") && this.targetHeights.containsKey(endPointId)) 
						th = this.targetHeights.get(endPointId).doubleValue();
					else if (key1.substring(2).matches("\\d+")) {
						try {th = 0.001 * Integer.parseInt(key1.substring(2).trim());} catch(Exception e){}
						this.targetHeights.put(endPointId, th);	
					}
				}

				String endPointCode = "";
				if (line.substring(70, 71).equals("|") && line.length() > 71) {
					String attributes[] = line.substring(71, line.length()).split("\\|");
					if (attributes.length > 0)
						endPointCode = attributes[0].trim();
				}

				// Distance 3D
				double dist = 0;
				if ((c1 == 2 || c1 == 3) && !this.is2DStartPoint && !is2DEndPoint) {
					double dist3D = Double.parseDouble(line.substring(32, 44).trim());
					dist = dist3D;
					if (dist3D > 0) {
						TerrestrialObservationRow distance3D = this.getObservation(this.startPointName, endPointId, this.ih, th, dist3D);
						distance3D.setDistanceApriori(dist);
						this.slopeDistances.add(distance3D);

						// Fuege Punkte hinzu
						this.addPoint(this.startPointName, this.startPointCode, 3);
						this.addPoint(endPointId, endPointCode, 3);
					}
				}

				// Distance 2D
				if (c1 == 5 || c1 == 6) {
					double dist2D = Double.parseDouble(line.substring(32, 44).trim());
					dist = dist2D;
					if (dist2D > 0) {
						TerrestrialObservationRow distance2D = this.getObservation(this.startPointName, endPointId, this.is2DStartPoint ? 0 : this.ih, is2DEndPoint ? 0 : th, dist2D);
						distance2D.setDistanceApriori(dist);
						this.horizontalDistances.add(distance2D);

						// Fuege Punkte hinzu
						this.addPoint(this.startPointName, this.startPointCode, c1 == 6 || this.is2DStartPoint ? 2 : 3);
						this.addPoint(endPointId, endPointCode, c1 == 6 || is2DEndPoint ? 2 : 3);
					}
				}

				// Direction
				if (c1 == 2 || c1 == 5 || c1 == 8) {
					double dir = Double.parseDouble(line.substring(44, 56).trim()) * Constant.RHO_GRAD2RAD;
					TerrestrialObservationRow direction = this.getObservation(this.startPointName, endPointId, this.is2DStartPoint ? 0 : this.ih, is2DEndPoint ? 0 : th, dir);
					if (dist > 0)
						direction.setDistanceApriori(dist);
					this.directions.add(direction);

					// Fuege Punkte hinzu
					this.addPoint(this.startPointName, this.startPointCode, c1 == 8 || this.is2DStartPoint ? 2 : 3);
					this.addPoint(endPointId, endPointCode, c1 == 8 || is2DEndPoint ? 2 : 3);
				}

				// Zenith
				if ((c1 == 2 || c1 == 4) && !this.is2DStartPoint && !is2DEndPoint) {
					double zenith = Double.parseDouble(line.substring(56, 68).trim()) * Constant.RHO_GRAD2RAD;
					TerrestrialObservationRow zenithangle = this.getObservation(this.startPointName, endPointId, this.ih, th, zenith);
					if (dist > 0)
						zenithangle.setDistanceApriori(dist);
					this.zenithAngles.add(zenithangle);

					// Fuege Punkte hinzu
					this.addPoint(this.startPointName, this.startPointCode, 3);
					this.addPoint(endPointId, endPointCode, 3);
				}

				// Delta H
				if ((c1 == 5 || c1 == 7) && !this.is2DStartPoint && !is2DEndPoint) {
					double deltaH = Double.parseDouble(line.substring(56, 68).trim());
					TerrestrialObservationRow levelling = this.getObservation(this.startPointName, endPointId, this.ih, th, deltaH);
					if (dist > 0)
						levelling.setDistanceApriori(dist);
					this.leveling.add(levelling);

					// Fuege Punkte hinzu
					this.addPoint(this.startPointName, this.startPointCode, c1 == 5 ? 3 : 1);
					this.addPoint(endPointId, endPointCode, c1 == 5 ? 3 : 1);
				}
			}


			else if (line.length() >= 71 && c1 >= 2 && c1 <=3 && c2 >= 5 && c2 <= 8) {
				boolean precisionMode = line.substring(8, 9).equals("2"); // R-V-V-R
				double sign = c1 == 3 ? -1.0 : 1.0;
				String pointName = line.substring(16, 32).trim();
				String code = "";

				Double dist = 0.0, value1 = null, value2 = null;

				if (c2 != 8)
					try {dist = Double.parseDouble(line.substring(32, 44).trim());}catch(Exception e) {}

				value1 = sign * Double.parseDouble(line.substring(44, 56).trim());
				value2 = sign * Double.parseDouble(line.substring(56, 68).trim());

				if (line.substring(70, 71).equals("|") && line.length() > 71) {
					String attributes[] = line.substring(71, line.length()).split("\\|");
					if (attributes.length > 0)
						code = attributes[0].trim();
				}
				// Rueckblick = 25/35
				if (c2 == 5) {					
					if ((pointName == null || pointName.trim().isEmpty() || pointName.equals("0")) && this.lastPointId != null) {
						pointName = this.lastPointId;
					}
					this.startPointName = pointName;
					this.startPointCode = code;
					this.levelingData = new LevelingData();

					this.startPointName = pointName;
					this.rb     = value1;
					this.distRb = dist;

					this.levelingData.addBackSightReading(pointName, value1, dist, true);
					if (precisionMode)
						this.levelingData.addBackSightReading(pointName, value2, dist, false);

					this.addPoint(this.startPointName, this.startPointCode, 1);
				}
				// Vorblick/Zwischenblick
				if (this.levelingData != null) {
					if (c2 >= 6 && c2 <= 8) {
						if (pointName == null || pointName.trim().isEmpty() || pointName.equals("0")) {
							//pointName = "W"+(++cnt)+"_"+this.getFile().getName().substring(0, this.getFile().getName().lastIndexOf('.'));
							pointName = String.format(Locale.ENGLISH, "%c%07d", 'W', ++this.cnt);
							this.lastPointId = pointName;
						}

						// Kein Vorblick
						if (c2 > 6 && this.rb != null && this.startPointName != null) {
							LevelingData sideLevelingData = new LevelingData();
							sideLevelingData.addBackSightReading(this.startPointName, this.rb, this.distRb == null ? 0 : this.distRb);
							sideLevelingData.addForeSightReading(pointName, value1, dist);
							this.addLevelingData(sideLevelingData);
						}

						// Vorblick
						if (c2 == 6) {
							this.levelingData.addForeSightReading(pointName, value1, dist, true);
							if (precisionMode)
								this.levelingData.addForeSightReading(pointName, value2, dist, false);
							this.addLevelingData(this.levelingData);
							this.levelingData = null;
						}

						this.addPoint(pointName, code, 1);
					}
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private void normalizePointNames() {
		for (Map.Entry<String, String> entry : this.pointNames1D.entrySet()) {
			String pid  = entry.getKey();
			String code = entry.getValue();

			// Wenn eine Pkt in 1D und 2D enthalten ist, ist es ein 3D-Punkt
			if (this.pointNames2D.containsKey(pid))
				this.pointNames3D.put(pid, code.isEmpty() ? this.pointNames2D.get(pid) : code);
		}

		for (String pid : this.pointNames3D.keySet()) {
			String code = this.pointNames3D.get(pid);
			if (this.pointNames1D.containsKey(pid)) {
				this.pointNames3D.put(pid, code.isEmpty() ? this.pointNames1D.get(pid) : code);
				this.pointNames1D.remove(pid);
			}
			if (this.pointNames2D.containsKey(pid)) {
				this.pointNames3D.put(pid, code.isEmpty() ? this.pointNames2D.get(pid) : code);
				this.pointNames2D.remove(pid);
			}
		}
	}

	private void addPoint(String name, String code, int dim) {
		if (dim == 1 && !this.pointNames1D.containsKey(name))
			this.pointNames1D.put(name, code);

		else if (dim == 2 && !this.pointNames2D.containsKey(name))
			this.pointNames2D.put(name, code);

		else if (dim == 3 && !this.pointNames3D.containsKey(name))
			this.pointNames3D.put(name, code);
	}

	private TerrestrialObservationRow getObservation(String startPoint, String endPoint, double ih, double th, double value) {
		TerrestrialObservationRow obs = new TerrestrialObservationRow();
		obs.setStartPointName(startPoint);
		obs.setEndPointName(endPoint);
		obs.setInstrumentHeight(ih);
		obs.setReflectorHeight(th);
		obs.setValueApriori(value);
		return obs;
	}

	private void saveObservationGroups() throws SQLException {
		String prefix = this.project == null ? "" : this.project + " ";
		String itemName = this.createItemName(null, prefix);

		// Speichere Daten
		if (!this.leveling.isEmpty())
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.LEVELING_LEAF, this.leveling);

		if (!this.directions.isEmpty())
			this.saveDirectionGroup();

		if (!this.horizontalDistances.isEmpty())
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.HORIZONTAL_DISTANCE_LEAF, this.horizontalDistances);

		if (!this.slopeDistances.isEmpty()) 
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.SLOPE_DISTANCE_LEAF, this.slopeDistances);

		if (!this.zenithAngles.isEmpty())
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.ZENITH_ANGLE_LEAF, this.zenithAngles);

		this.leveling.clear();
		this.directions.clear();
		this.horizontalDistances.clear();
		this.slopeDistances.clear();
		this.zenithAngles.clear();
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
				new ExtensionFilter(i18n.getString("ZFileReader.extension.z", "Cremer Caplan"), "*.z")
		};
	}

	private void saveDirectionGroup() throws SQLException {
		if (this.startPointName != null && !this.directions.isEmpty()) {
			String itemName = this.createItemName(null, " (" + this.startPointName + ")"); 
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.DIRECTION_LEAF, this.directions);
		}
		this.directions.clear();
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
