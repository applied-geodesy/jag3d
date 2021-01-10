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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.applied_geodesy.adjustment.network.VerticalDeflectionType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.row.VerticalDeflectionRow;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.jag3d.ui.tree.VerticalDeflectionTreeItemValue;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.io.SourceFileReader;
import org.applied_geodesy.util.io.csv.CSVColumnType;
import org.applied_geodesy.util.io.csv.CSVParser;
import org.applied_geodesy.util.io.csv.ColumnRange;

import javafx.scene.control.TreeItem;

public class CSVVerticalDeflectionFileReader extends SourceFileReader<TreeItem<TreeItemValue>> {
	private final CSVParser parser;
	
	private final VerticalDeflectionType verticalDeflectionType;
	private TreeItemType treeItemType;
	private Set<String> reservedNames = null;
	
	private FormatterOptions options = FormatterOptions.getInstance();
	private NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);

	private List<ColumnRange> columnRanges = new ArrayList<ColumnRange>(0);
	private List<VerticalDeflectionRow> verticalDeflections = null;

	private List<String> parsedLine = null;
	
	public CSVVerticalDeflectionFileReader(VerticalDeflectionType verticalDeflectionType, CSVParser parser) {
		this.verticalDeflectionType = verticalDeflectionType;
		this.parser = parser;
		this.treeItemType = TreeItemType.getTreeItemTypeByVerticalDeflectionType(verticalDeflectionType); 
		if (this.treeItemType == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, observation type could not be transformed to tree item type. " + verticalDeflectionType);
		this.reset();
	}

	public CSVVerticalDeflectionFileReader(String fileName, VerticalDeflectionType verticalDeflectionType, CSVParser parser) {
		this(new File(fileName).toPath(), verticalDeflectionType, parser);
	}

	public CSVVerticalDeflectionFileReader(File sf, VerticalDeflectionType verticalDeflectionType, CSVParser parser) {
		this(sf.toPath(), verticalDeflectionType, parser);
	}

	public CSVVerticalDeflectionFileReader(Path path, VerticalDeflectionType verticalDeflectionType, CSVParser parser) {
		super(path);
		this.verticalDeflectionType = verticalDeflectionType;
		this.parser = parser;
		this.treeItemType = TreeItemType.getTreeItemTypeByVerticalDeflectionType(verticalDeflectionType); 
		if (this.treeItemType == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : Error, observation type could not be transformed to tree item type. " + verticalDeflectionType);
		this.reset();
	}

	public void setColumnRanges(List<ColumnRange> columnRanges) {
		this.columnRanges = columnRanges;
	}

	public void setFileLocale(Locale locale) {
		this.numberFormat = NumberFormat.getNumberInstance(locale);
	}

	@Override
	public TreeItem<TreeItemValue> readAndImport() throws IOException, SQLException {
		this.reset();
		this.ignoreLinesWhichStartWith("#");
		this.reservedNames = SQLManager.getInstance().getFullVerticalDeflectionNameSet();
		TreeItem<TreeItemValue> newTreeItem = null;
		
		boolean hasXComponent = false;
		boolean hasYComponent = false;

		for (ColumnRange range : this.columnRanges) {
			if (range.getType() == CSVColumnType.X)
				hasXComponent = true;
			else if (range.getType() == CSVColumnType.Y)
				hasYComponent = true;
		}

		if (hasXComponent && hasYComponent) {
			super.read();

			if (!this.verticalDeflections.isEmpty()) {
				this.treeItemType = TreeItemType.getTreeItemTypeByVerticalDeflectionType(this.verticalDeflectionType);
				String itemName = this.createItemName(null, null);
				TreeItemType parentType = TreeItemType.getDirectoryByLeafType(this.treeItemType);
				newTreeItem = UITreeBuilder.getInstance().addItem(parentType, -1, itemName, true, false);
				try {
					SQLManager.getInstance().saveGroup((VerticalDeflectionTreeItemValue)newTreeItem.getValue());
				} catch (SQLException e) {
					UITreeBuilder.getInstance().removeItem(newTreeItem);
					e.printStackTrace();
					throw new SQLException(e);
				}

				try {
					int groupId = ((VerticalDeflectionTreeItemValue)newTreeItem.getValue()).getGroupId();
					for (VerticalDeflectionRow row : this.verticalDeflections) {
						row.setGroupId(groupId);
						SQLManager.getInstance().saveItem(row);
					}

				} catch (SQLException e) {
					e.printStackTrace();
					throw new SQLException(e);
				}			
			}
		}
		this.reset();
		return newTreeItem;
	}

	@Override
	public void reset() {
		if (this.verticalDeflections == null) 
			this.verticalDeflections = new ArrayList<VerticalDeflectionRow>();
		if (this.reservedNames == null)
			this.reservedNames = new HashSet<String>();
		if (this.parsedLine == null)
			this.parsedLine = new ArrayList<String>(20);

		this.parsedLine.clear();
		this.verticalDeflections.clear();
		this.reservedNames.clear();
	}

	@Override
	public void parse(String line) throws SQLException {
		try {
			String parsedLine[] = this.parser.parseLineMulti(line);
			if (parsedLine != null && parsedLine.length > 0) {
				this.parsedLine.addAll(Arrays.asList(parsedLine));
			}

			if (!this.parser.isPending()) {
				this.parseVerticalDeflection(this.parsedLine);
				this.parsedLine.clear();
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseVerticalDeflection(List<String> parsedLine) {
		VerticalDeflectionRow row = new VerticalDeflectionRow();
		
		for (ColumnRange range : this.columnRanges) {
			try {
				CSVColumnType type = range.getType();
				int pos = range.getColumnStart() - 1;
				if (pos < 0 || pos >= parsedLine.size())
					continue;

				double value;
				switch(type) {
				case POINT_ID:
					String name = parsedLine.get(pos).trim();
					if (name != null && !name.isEmpty())
						row.setName(name);
					else
						continue;
					break;
					
				case X:
					value = this.numberFormat.parse(parsedLine.get(pos).trim()).doubleValue();
					row.setXApriori(options.convertLengthToModel(value));
					break;
				case Y:
					value = this.numberFormat.parse(parsedLine.get(pos).trim()).doubleValue();
					row.setYApriori(options.convertLengthToModel(value));
					break;
					
				case UNCERTAINTY_X:
					value = this.numberFormat.parse(parsedLine.get(pos).trim()).doubleValue();
					row.setSigmaXapriori(options.convertLengthToModel(value));
					break;
				case UNCERTAINTY_Y:
					value = this.numberFormat.parse(parsedLine.get(pos).trim()).doubleValue();
					row.setSigmaYapriori(options.convertLengthToModel(value));
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

		if (row.getName() != null && !row.getName().isEmpty() && !this.reservedNames.contains(row.getName()) && (row.getXApriori() != null && row.getYApriori() != null)) {
			this.verticalDeflections.add(row);
			this.reservedNames.add(row.getName());
		}
	}
}