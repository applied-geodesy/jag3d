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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.MathExtension;
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

public class GSIFileReader extends SourceFileReader {

	private enum UnitType {
		NONE(-1),
		METER(0),
		FEET(1),
		GRADIAN(2),
		DECIMAL_DEGREE(3),
		SEXAGESIMAL_DEGREE(4),
		MIL(5),
		METER_10(6),
		FEET_10000(7),
		METER_100(8),

		;

		private int id;
		private UnitType(int id) {
			this.id = id;
		}

		public static UnitType getEnumByValue(int value) {
			for(UnitType element : UnitType.values()) {
				if(element.id == value)
					return element;
			}
			return NONE;
		}  
	}

	private final DimensionType dim;
	private boolean isNewStation = false;
	private Set<String> reservedNames = null;
	private Map<String, PointRow> pointMap = new HashMap<String, PointRow>();
	private String startPointName = null, endPointName = null, directionStartPointName = null;
	private boolean isDirectionGroupWithEqualStation = true;

	private double ih = 0.0, th = 0.0;
	private Double rb1 = null, vb1 = null, distRb1 = null, distVb1 = null;
	private Double rb2 = null, vb2 = null, distRb2 = null, distVb2 = null;
	private Double zb = null, distZb = null, lastRb4Zb = null, distLastRb4Zb;

	private List<PointRow> points1d = null;
	private List<PointRow> points2d = null;
	private List<PointRow> points3d = null;

	private List<TerrestrialObservationRow> leveling = null;

	private List<TerrestrialObservationRow> horizontalDistances = null;
	private List<TerrestrialObservationRow> directions = null;

	private List<TerrestrialObservationRow> slopeDistances = null;
	private List<TerrestrialObservationRow> zenithAngles = null;

	private TreeItem<TreeItemValue> lastTreeItem = null;
	
	public GSIFileReader(DimensionType dim) {
		this.dim = dim;
		this.reset();
	}
	
	public GSIFileReader(Path p, DimensionType dim) {
		super(p);
		this.dim = dim;
		this.reset();
	}

	public GSIFileReader(File f, DimensionType dim) {
		this(f.toPath(), dim);
	}

	public GSIFileReader(String s, DimensionType dim) {
		this(new File(s), dim);
	}

	@Override
	public void reset() {
		this.isNewStation = false;

		this.startPointName = null;
		this.endPointName = null;
		this.ih = 0.0;
		this.th = 0.0;
		this.rb1 = null;
		this.vb1 = null;
		this.rb2 = null;
		this.vb2 = null;
		this.zb = null;
		this.distRb1 = null;
		this.distVb1 = null;
		this.distRb2 = null;
		this.distVb2 = null;
		this.distZb = null;
		this.lastRb4Zb = null;
		this.distLastRb4Zb = null;
		
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
		if (this.reservedNames == null)
			this.reservedNames = new HashSet<String>();
		
		this.reservedNames.clear();
		
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
		this.reservedNames = SQLManager.getInstance().getFullPointNameSet();
		this.lastTreeItem = null;
		this.ignoreLinesWhichStartWith("#");
		
		super.read();

		String itemName = this.createItemName(null, null);

		// Speichere Punkte
		if ((this.dim == DimensionType.HEIGHT || this.dim == DimensionType.PLAN_AND_HEIGHT) && !this.points1d.isEmpty()) 
			this.lastTreeItem = this.savePoints(itemName, TreeItemType.DATUM_POINT_1D_LEAF, this.points1d);

		if ((this.dim == DimensionType.PLAN || this.dim == DimensionType.PLAN_AND_HEIGHT) && !this.points2d.isEmpty()) 
			this.lastTreeItem = this.savePoints(itemName, TreeItemType.DATUM_POINT_2D_LEAF, this.points2d);

		if ((this.dim == DimensionType.SPATIAL || this.dim == DimensionType.PLAN_AND_HEIGHT) && !this.points3d.isEmpty())
			this.lastTreeItem = this.savePoints(itemName, TreeItemType.DATUM_POINT_3D_LEAF, this.points3d);

		// Speichere Daten
		if ((this.dim == DimensionType.HEIGHT || this.dim == DimensionType.PLAN_AND_HEIGHT) && !this.leveling.isEmpty()) 
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.LEVELING_LEAF, this.leveling);

		if (this.dim != DimensionType.HEIGHT && !this.directions.isEmpty()) {
			this.saveDirectionGroup();
		}
		if ((this.dim == DimensionType.PLAN || this.dim == DimensionType.PLAN_AND_HEIGHT) && !this.horizontalDistances.isEmpty()) 
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.HORIZONTAL_DISTANCE_LEAF, this.horizontalDistances);

		if (this.dim == DimensionType.SPATIAL && !this.slopeDistances.isEmpty()) 
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.SLOPE_DISTANCE_LEAF, this.slopeDistances);

		if (this.dim == DimensionType.SPATIAL && !this.zenithAngles.isEmpty()) 
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.ZENITH_ANGLE_LEAF, this.zenithAngles);

		this.reset();
		
		return this.lastTreeItem;
	}

	@Override
	public void parse(String line) throws SQLException {
		// entferne Withspaces und fuege am Ende ein Leerzeichen an (GSI-Datensatzblockende)
		line = line.trim() + " ";
		// Bestimme das GSI-Format GSI8 bzw. GSI16
		int gsiType = line.startsWith("*") ? 16 : 8;

		// Wenn GSI16 - entferne fuehrenden *
		if (gsiType == 16)
			line = line.substring(1);

		//String dataBlocks[] = line.split("\\s+");
		//String blockTemplate = String.format(Locale.ENGLISH, "%%-%ds", (7 + gsiType));

		String pointName = null, pointCode = "";
		Double ih = null, th = null;
		Double deltaH = null, dir = null, dist2d = null, dist3d = null, zenith = null;
		Double x = null, y = null, z = null;

		int startIdx = 0;
		int endIdx = 7 + gsiType;
		
		while (endIdx < line.length()){
			String dataBlock = line.substring(startIdx, endIdx);
			startIdx = endIdx + 1;
			endIdx = startIdx + 7 + gsiType;
		
//		for (String dataBlock : dataBlocks) {
//			// Pruefe, ob Datenblock genuegend Zeichen enthaelt
//			// 7 Schluesselzeichen, <gsiType> Datenzeichen, Leerzeichen
//			if (dataBlock.length() < 7 + gsiType) 
//				dataBlock = String.format(Locale.ENGLISH, blockTemplate, dataBlock);
//			else if (dataBlock.length() > 7 + gsiType) 
//				continue;

			// Bestimme die Laenge der Wortidentifikation 2 oder 3
			int wordIndexLength = dataBlock.charAt(2) == '.' ? 2 : 3;
			// Zerlege Zeichenkette
			try {
				int key  = Integer.parseInt(dataBlock.substring(0, wordIndexLength));
				UnitType unit = UnitType.getEnumByValue(dataBlock.charAt(5) == '.'?-1:Character.getNumericValue(dataBlock.charAt(5)));
				int sign = dataBlock.charAt(6) == '-' ? -1 : 1;
				String data = dataBlock.substring(7, 7 + gsiType);

				if (key == 11 || key > 100 && key/10 == 11) {
					pointName = this.removeLeadingZeros(data).trim();
					if (pointName.isEmpty())
						pointName = "0";
				}

				else if (key == 41 || key > 400 && key/10 == 41)
					pointCode = this.removeLeadingZeros(data).trim();

				else if (isNumericValue(key) && data.matches("\\d+")) {
					if (key == 84 || key == 85 || key == 86 || key == 331 || key == 335) //  || key == 88
						this.isNewStation = true;

					switch (key) {
					// Hz-Winkel
					case 21:
						dir   = this.convertToTrueValue(unit, sign, data);
						break;
						// V-Winkel
					case 22:
						zenith = this.convertToTrueValue(unit, sign, data);
						break;
						// Dist2D
					case 32:
						dist2d = this.convertToTrueValue(unit, sign, data);
						dist2d = dist2d == 0?null:dist2d;
						break;
						// Dist3D
					case 31:
						dist3d = this.convertToTrueValue(unit, sign, data);
						dist3d = dist3d == 0?null:dist3d;
						break;
						// deltaH
					case 33:
						deltaH = this.convertToTrueValue(unit, sign, data);
						break;
						// Koordinaten
					case 81:
					case 84:
						y = this.convertToTrueValue(unit, sign, data);
						break;
					case 82:
					case 85:
						x = this.convertToTrueValue(unit, sign, data);
						break;
					case 83:
					case 86:
						z = this.convertToTrueValue(unit, sign, data);
						break;
						// Zielpunkthoehe
					case 87:
						th = this.convertToTrueValue(unit, sign, data);
						break;
						// Standpunkthoehe
					case 88:
						ih = this.convertToTrueValue(unit, sign, data);
						break;

						// Nivellement
					case 331:
						this.rb1 = this.convertToTrueValue(unit, sign, data);
						this.lastRb4Zb = this.rb1;
						break;
					case 335:
						this.rb2 = this.convertToTrueValue(unit, sign, data);
						this.lastRb4Zb = this.lastRb4Zb == null ? this.rb2 : 0.5 * (this.lastRb4Zb + this.rb2);
						break;
					case 332:
						this.vb1 = this.convertToTrueValue(unit, sign, data);
						break;
					case 336:
						this.vb2 = this.convertToTrueValue(unit, sign, data);
						break;
					case 333:
						this.zb  = this.convertToTrueValue(unit, sign, data);
						break;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		
		// Reduziere Richtungen und Zenitwinkel auf Lage I
		if (this.dim != DimensionType.HEIGHT) {
			boolean isFaceI = !(zenith != null && !Double.isNaN(zenith) && !Double.isInfinite(zenith) && zenith > Math.PI);
			if (!isFaceI) {
				if (dir != null)
					dir = MathExtension.MOD(dir + Math.PI, 2.0*Math.PI);
				if (this.dim == DimensionType.SPATIAL && zenith != null && zenith > Math.PI)
					zenith = MathExtension.MOD(2.0*Math.PI - zenith, 2.0*Math.PI);
			}
		}

		// Speichere die (neue) Standpunktnummer und speichere den bisherigen Richtungssatz des alten Standpunkes
		if (this.isNewStation && pointName != null && !pointName.trim().isEmpty()) {
			this.startPointName = pointName;
			this.ih = ih==null ? 0.0 : ih;
			this.th = th==null ? 0.0 : th;

			// Strecken beim Niv sind hier zu reseten, da Zwischenblicke immer nach dem Vorblick kommen.
			this.distRb1 = null;
			this.distRb2 = null;
			this.distLastRb4Zb = null;

			// Speichere Richtungen, da diese Satzweise zu halten sind
			if (this.dim != DimensionType.HEIGHT && !this.directions.isEmpty()) {
				this.saveDirectionGroup();
			}
			this.directions.clear();
		}
		else if (pointName != null && !pointName.trim().isEmpty()) {
			this.endPointName = pointName;
		}

		// Speichere Punkte aus der GSI
		if (pointName != null && !pointName.trim().isEmpty() && !this.reservedNames.contains(pointName) && !this.pointMap.containsKey(pointName)) {
			PointRow point = new PointRow();
			point.setName(pointName);
			point.setCode(pointCode);
			point.setXApriori(x);
			point.setYApriori(y);
			point.setZApriori(z);

			if ((this.dim == DimensionType.SPATIAL || this.dim == DimensionType.PLAN_AND_HEIGHT) && x != null && y != null && z != null) {
				this.pointMap.put(pointName, point);
				this.points3d.add(point);
			}
			else if ((this.dim == DimensionType.PLAN || this.dim == DimensionType.PLAN_AND_HEIGHT) && x != null && y != null) {
				this.pointMap.put(pointName, point);
				this.points2d.add(point);
			}
			else if ((this.dim == DimensionType.HEIGHT || this.dim == DimensionType.PLAN_AND_HEIGHT) && z != null) {
				this.pointMap.put(pointName, point);
				this.points1d.add(point);
			}
		}
		else if (this.pointMap.containsKey(pointName)) {
			PointRow point = this.pointMap.get(pointName);
			if (this.hasZerosCoordinates(point)) {
				if ((this.dim == DimensionType.SPATIAL || this.dim == DimensionType.PLAN_AND_HEIGHT) && x != null && y != null && z != null) {
					point.setXApriori(x);
					point.setYApriori(y);
					point.setZApriori(z);
				}
				else if ((this.dim == DimensionType.PLAN || this.dim == DimensionType.PLAN_AND_HEIGHT) && x != null && y != null) {
					point.setXApriori(x);
					point.setYApriori(y);
				}
				else if ((this.dim == DimensionType.HEIGHT || this.dim == DimensionType.PLAN_AND_HEIGHT) && z != null) {
					point.setZApriori(z);
				}
			}
		}

		// Speichere Beobachtungen in den Gruppen
		if (this.startPointName != null) {
			// Pruefe, ob Werte neu gesetzt wurden
			this.ih = ih == null ? this.ih : ih;
			this.th = th == null ? this.th : th;

			// Speichere Abstand zwischen 1. Rueckblick und Instrument
			if (this.rb1 != null && dist2d != null && this.distRb1 == null) {
				this.distRb1       = dist2d;
				this.distLastRb4Zb = dist2d;
				dist2d = null;
			}
			// Speichere Abstand zwischen 2. Rueckblick und Instrument
			else if (this.rb2 != null && dist2d != null && this.distRb2 == null) {
				this.distRb2       = dist2d;
				this.distLastRb4Zb = this.distLastRb4Zb == null ? dist2d : 0.5 * (this.distLastRb4Zb + dist2d);
				dist2d = null;
			}			
			// Speichere Abstand zwischen 1. Vorblick und Instrument
			else if (this.vb1 != null && dist2d != null && this.distVb1 == null) {
				this.distVb1 = dist2d;
				dist2d = null;
			}
			// Speichere Abstand zwischen 2. Vorblick und Instrument
			else if (this.vb2 != null && dist2d != null && this.distVb2 == null) {
				this.distVb2 = dist2d;
				dist2d = null;
			}			
			// Speichere Abstand zwischen Zwischenblick und Instrument
			else if (this.zb != null && dist2d != null && this.distZb == null) {
				this.distZb = dist2d;
				dist2d = null;
			}

			// ermittle Hoehenunterschied aus 0.5 * (RI+RII) und ZB
			if (this.lastRb4Zb != null && this.zb != null) {
				deltaH = this.lastRb4Zb - this.zb;
				this.zb = null;
				if (this.distLastRb4Zb != null && this.distZb != null) {
					dist2d = this.distLastRb4Zb + this.distZb;
					this.distZb = null;
				}
			}

			// ermittle Hoehenunterschied aus R1 und V1
			if (this.rb1 != null && this.vb1 != null) {
				deltaH = this.rb1 - this.vb1;
				this.rb1 = this.vb1 = null;
				if (this.distRb1 != null && this.distVb1 != null) {
					dist2d = this.distRb1 + this.distVb1;
					this.distRb1 = this.distVb1 = null;
				}
			}
			
			// ermittle Hoehenunterschied aus R2 und V2
			else if (this.rb2 != null && this.vb2 != null) {
				deltaH = this.rb2 - this.vb2;
				this.rb2 = this.vb2 = null;
				if (this.distRb2 != null && this.distVb2 != null) {
					dist2d = this.distRb2 + this.distVb2;
					this.distRb2 = this.distVb2 = null;
				}
			}

			// Fuege Beobachtungen hinzu
			if (this.startPointName != null && this.endPointName != null) {
				if (deltaH != null) {
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(this.startPointName);
					obs.setEndPointName(this.endPointName);

					// Delta-Hs sind bereits um ih und th korrigiert, daher Null
					//obs.setInstrumentHeight(this.ih);
					//obs.setReflectorHeight(this.th);

					obs.setInstrumentHeight(0.0);
					obs.setReflectorHeight(0.0);

					if (dist2d != null && dist2d > 0)
						obs.setDistanceApriori(dist2d);

					obs.setValueApriori(deltaH);
					this.leveling.add(obs);
				}

				if (dir != null) {
					if (this.directionStartPointName == null)
						this.directionStartPointName = this.startPointName;

					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(this.startPointName);
					obs.setEndPointName(this.endPointName);
					obs.setInstrumentHeight(this.ih);
					obs.setReflectorHeight(this.th);				
					obs.setValueApriori(dir);
					if (this.dim == DimensionType.SPATIAL && dist3d != null && dist3d > 0) 
						obs.setDistanceApriori(dist3d);
					else if ((this.dim == DimensionType.PLAN || this.dim == DimensionType.PLAN_AND_HEIGHT) && dist2d != null && dist2d > 0)
						obs.setDistanceApriori(dist2d);
					this.directions.add(obs);
					this.isDirectionGroupWithEqualStation = this.isDirectionGroupWithEqualStation && this.directionStartPointName.equals(this.startPointName);
				}

				if (dist2d != null) {
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(this.startPointName);
					obs.setEndPointName(this.endPointName);				
					obs.setInstrumentHeight(this.ih);
					obs.setReflectorHeight(this.th);
					obs.setValueApriori(dist2d);
					obs.setDistanceApriori(dist2d);
					this.horizontalDistances.add(obs);
				}

				if (dist3d != null) {
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(this.startPointName);
					obs.setEndPointName(this.endPointName);
					obs.setInstrumentHeight(this.ih);
					obs.setReflectorHeight(this.th);				
					obs.setValueApriori(dist3d);
					obs.setDistanceApriori(dist3d);
					this.slopeDistances.add(obs);
				}

				if (zenith != null) {
					TerrestrialObservationRow obs = new TerrestrialObservationRow();
					obs.setStartPointName(this.startPointName);
					obs.setEndPointName(this.endPointName);
					obs.setInstrumentHeight(this.ih);
					obs.setReflectorHeight(this.th);				
					obs.setValueApriori(zenith);
					if (dist3d != null && dist3d > 0) 
						obs.setDistanceApriori(dist3d);
					this.zenithAngles.add(obs);
				}
			}			
		}			
		this.isNewStation = false;
	}

	private boolean hasZerosCoordinates(PointRow point) {
		Double x = point.getXApriori();
		Double y = point.getYApriori();
		Double z = point.getZApriori();

		if (x != null && x != 0)
			return false;
		if (y != null && y != 0)
			return false;
		if (z != null && z != 0)
			return false;
		return true;
	}

	/**
	 * Bestimmt anhand der Einheit den wahren Wert in Meter bzw. RAD
	 * 
	 * @param unit
	 * @param sign
	 * @param data
	 * @return value
	 * @throws NumberFormatException
	 */
	private double convertToTrueValue(UnitType unit, int sign, String data) throws NumberFormatException {
		data = this.removeLeadingZeros(data);
		double value = 0.0;
		if (!data.isEmpty())
			value = Double.parseDouble(data);

		switch (unit) {
		case METER:
			value = value / 1000.0;
			break;
		case FEET:
			value = value / 1000.0 * 0.3048;
			break;
		case GRADIAN:
			value = value / 100000.0 * Constant.RHO_GRAD2RAD;
			break;
		case DECIMAL_DEGREE:
			value = value / 100000.0 * Constant.RHO_DEG2RAD;
			break;
		case SEXAGESIMAL_DEGREE:
			double dd = (int)value/100000;
			value -= dd*100000;
			double mm = (int)value/1000;
			value -= mm*1000;
			double ss = value/10.0;
			value = (dd + mm/60 + ss/3600) * Constant.RHO_DEG2RAD;
			break;
		case MIL:
			value = value / 10000.0  * Constant.RHO_MIL2RAD;
			break;
		case METER_10:
			value = value / 10000.0;
			break;
		case FEET_10000:
			value = value / 100000.0 * 0.3048;
			break;
		case METER_100:
			value = value / 100000.0;
			break;
		case NONE:
			break;
		}

		return sign*value;
	}

	/**
	 * Entfernt fuehrende Nullen
	 * @param str
	 * @return nonZeroStartString
	 */
	private String removeLeadingZeros(String str) {
		return str.replaceAll("^0+", "");
	}

	/**
	 * Liefert true, wenn der Wert des Schluessels eine Zahl sein soll/muss
	 * @param key
	 * @return isNumericValue
	 */
	private boolean isNumericValue(int key) {
		return !(key % 10 == 0 || key <= 19 || key >= 41 && key <= 49 || key >= 70 && key <= 79); 
	}

	public static ExtensionFilter[] getExtensionFilters() {
		return new ExtensionFilter[] {
				new ExtensionFilter(i18n.getString("GSIFileReader.extension.gsi", "Geo Serial Interface (GSI)"), "*.gsi")
		};
	}
	
	private void saveDirectionGroup() throws SQLException {
		if (!this.directions.isEmpty()) {
			String itemName = this.createItemName(null, this.isDirectionGroupWithEqualStation ? " (" + this.directionStartPointName + ")" : null); 
			this.lastTreeItem = this.saveTerrestrialObservations(itemName, TreeItemType.DIRECTION_LEAF, this.directions);
		}
		this.directions.clear();
		this.directionStartPointName = null;
		this.isDirectionGroupWithEqualStation = true;
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
