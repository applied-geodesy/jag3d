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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.row.GNSSObservationRow;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.io.csv.CSVColumnType;
import org.applied_geodesy.util.io.csv.ColumnRange;

import javafx.scene.control.TreeItem;

public class ColumnDefinedObservationFileReader extends SourceFileReader {
	private final String tabulator;
	private final ObservationType observationType;
	private final TreeItemType treeItemType;
	private FormatterOptions options = FormatterOptions.getInstance();
	private NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);

	private int lastCharacterPosition = 0;
	private List<ColumnRange> columnRanges = new ArrayList<ColumnRange>(0);
	private List<TerrestrialObservationRow> observations = null;
	private List<GNSSObservationRow> gnss = null;

	private boolean isDirectionGroupWithEqualStation = true;
	private String directionStartPointName = null;

	public ColumnDefinedObservationFileReader(ObservationType observationType, String tabulator) {
		this.observationType = observationType;
		this.tabulator = tabulator;
		this.treeItemType = TreeItemType.getTreeItemTypeByObservationType(observationType);
		if (this.treeItemType == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, observation type could not be transformed to tree item type. " + observationType);
		this.reset();
	}

	public ColumnDefinedObservationFileReader(String fileName, ObservationType observationType, String tabulator) {
		this(new File(fileName).toPath(), observationType, tabulator);
	}

	public ColumnDefinedObservationFileReader(File sf, ObservationType observationType, String tabulator) {
		this(sf.toPath(), observationType, tabulator);
	}

	public ColumnDefinedObservationFileReader(Path path, ObservationType observationType, String tabulator) {
		super(path);
		this.observationType = observationType;
		this.tabulator = tabulator;
		this.treeItemType = TreeItemType.getTreeItemTypeByObservationType(observationType);
		if (this.treeItemType == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, observation type could not be transformed to tree item type. " + observationType);
		this.reset();
	}

	public void setColumnRanges(List<ColumnRange> columnRanges) {
		this.columnRanges = columnRanges;
	}

	public void setFileLocale(Locale locale) {
		this.numberFormat = NumberFormat.getNumberInstance(locale);
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

		this.lastCharacterPosition = 0;
	}


	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.reset();
		this.ignoreLinesWhichStartWith("#");

		for (ColumnRange range : this.columnRanges) {
			this.lastCharacterPosition = Math.max(this.lastCharacterPosition, range.getColumnEnd() + 1);
		}
		
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
	public void parse(String line) throws SQLException {
		line = String.format("%" + this.lastCharacterPosition + "s", line.replaceAll("\t", this.tabulator));

		if (this.isGNSS())
			this.parseGNSSObservation(line);
		else
			this.parseTerrestrialObservation(line);
	}
	
	private boolean isGNSS() {
		switch(this.observationType) {
		case GNSS1D:
		case GNSS2D:
		case GNSS3D:
			return true;
		default:
			return false;
		}
	}

	private void parseTerrestrialObservation(String line) {
		TerrestrialObservationRow row = new TerrestrialObservationRow();
		
		for (ColumnRange range : this.columnRanges) {
			try {
				CSVColumnType type = range.getType();
				int startPos = range.getColumnStart();
				int endPos   = range.getColumnEnd() + 1;

				double value;
				switch(type) {
				case STATION:
					String station = line.substring(startPos, endPos).trim();
					if (station != null && !station.isEmpty())
						row.setStartPointName(station);
					else
						continue;
					break;
				case TARGET:
					String target = line.substring(startPos, endPos).trim();
					if (target != null && !target.isEmpty())
						row.setEndPointName(target);
					else
						continue;
					break;
				case INSTRUMENT_HEIGHT:
					value = this.numberFormat.parse(line.substring(startPos, endPos).trim()).doubleValue();
					row.setInstrumentHeight(options.convertLengthToModel(value));
					break;
				case TARGET_HEIGHT:
					value = this.numberFormat.parse(line.substring(startPos, endPos).trim()).doubleValue();
					row.setReflectorHeight(options.convertLengthToModel(value));
					break;
				case VALUE:
					value = this.numberFormat.parse(line.substring(startPos, endPos).trim()).doubleValue();
					switch(this.observationType) {
					case DIRECTION:
					case ZENITH_ANGLE:
						row.setValueApriori(options.convertAngleToModel(value));
						break;
					default:
						row.setValueApriori(options.convertLengthToModel(value));
						if (this.observationType == ObservationType.HORIZONTAL_DISTANCE || this.observationType == ObservationType.SLOPE_DISTANCE)
							row.setDistanceApriori(options.convertLengthToModel(value));
						break;
					}
					break;
				case UNCERTAINTY:
					value = this.numberFormat.parse(line.substring(startPos, endPos).trim()).doubleValue();
					switch(this.observationType) {
					case DIRECTION:
					case ZENITH_ANGLE:
						row.setSigmaApriori(options.convertAngleToModel(value));
						break;
					default:
						row.setSigmaApriori(options.convertLengthToModel(value));
						break;
					}
					break;
				case DISTANCE_FOR_UNCERTAINTY:
					value = this.numberFormat.parse(line.substring(startPos, endPos).trim()).doubleValue();
					row.setDistanceApriori(options.convertLengthToModel(value));
					break;
				default:
					System.err.println(this.getClass().getSimpleName() + " Error, unsupported column type! " + type);
					break;
				}

			} catch(Exception e) {
				e.printStackTrace();
				return;
			}
		}

		if (row.getStartPointName() != null && row.getEndPointName() != null && row.getValueApriori() != null && !row.getStartPointName().isEmpty() && !row.getEndPointName().isEmpty() && !row.getStartPointName().equals(row.getEndPointName()))
			this.observations.add(row);
	}
	
	private void parseGNSSObservation(String line) {
		GNSSObservationRow row = new GNSSObservationRow();
		
		for (ColumnRange range : this.columnRanges) {
			try {
				CSVColumnType type = range.getType();
				int startPos = range.getColumnStart();
				int endPos   = range.getColumnEnd() + 1;

				double value;
				switch(type) {
				case STATION:
					String station = line.substring(startPos, endPos).trim();
					if (station != null && !station.isEmpty())
						row.setStartPointName(station);
					else
						continue;
					break;
				case TARGET:
					String target = line.substring(startPos, endPos).trim();
					if (target != null && !target.isEmpty())
						row.setEndPointName(target);
					else
						continue;
					break;
					
				case X:
					value = this.numberFormat.parse(line.substring(startPos, endPos).trim()).doubleValue();
					row.setXApriori(options.convertLengthToModel(value));
					break;
				case Y:
					value = this.numberFormat.parse(line.substring(startPos, endPos).trim()).doubleValue();
					row.setYApriori(options.convertLengthToModel(value));
					break;
				case Z:
					value = this.numberFormat.parse(line.substring(startPos, endPos).trim()).doubleValue();
					row.setZApriori(options.convertLengthToModel(value));
					break;
					
				case UNCERTAINTY_X:
					value = this.numberFormat.parse(line.substring(startPos, endPos).trim()).doubleValue();
					row.setSigmaXapriori(options.convertLengthToModel(value));
					break;
				case UNCERTAINTY_Y:
					value = this.numberFormat.parse(line.substring(startPos, endPos).trim()).doubleValue();
					row.setSigmaYapriori(options.convertLengthToModel(value));
					break;
				case UNCERTAINTY_Z:
					value = this.numberFormat.parse(line.substring(startPos, endPos).trim()).doubleValue();
					row.setSigmaZapriori(options.convertLengthToModel(value));
					break;
				default:
					System.err.println(this.getClass().getSimpleName() + " Error, unsupported column type! " + type);
					break;
				}

			} catch(Exception e) {
				e.printStackTrace();
				return;
			}
		}

		if (row.getStartPointName() != null && row.getEndPointName() != null && !row.getStartPointName().isEmpty() && !row.getEndPointName().isEmpty() && !row.getStartPointName().equals(row.getEndPointName()) && 
				((this.observationType == ObservationType.GNSS3D && row.getZApriori() != null && row.getXApriori() != null && row.getYApriori() != null) ||
						(this.observationType == ObservationType.GNSS2D && row.getXApriori() != null && row.getYApriori() != null) ||
						(this.observationType == ObservationType.GNSS1D && row.getZApriori() != null)))
			this.gnss.add(row);

	}

}
