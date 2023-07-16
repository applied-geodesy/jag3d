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

package org.applied_geodesy.jag3d.ui.table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dnd.PointRowDnD;
import org.applied_geodesy.jag3d.ui.table.column.ColumnContentType;
import org.applied_geodesy.jag3d.ui.table.column.TableContentType;
import org.applied_geodesy.jag3d.ui.table.row.PointRow;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlight;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightRangeType;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightType;
import org.applied_geodesy.jag3d.ui.tree.EditableMenuCheckBoxTreeCell;
import org.applied_geodesy.jag3d.ui.tree.PointTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.ui.table.AbsoluteValueComparator;
import org.applied_geodesy.ui.table.ColumnTooltipHeader;
import org.applied_geodesy.ui.table.ColumnType;
import org.applied_geodesy.ui.table.NaturalOrderTableColumnComparator;
import org.applied_geodesy.util.CellValueType;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

public class UIPointTableBuilder extends UIEditableTableBuilder<PointRow> {	
	private class TableKey {
		private final PointType type;
		private final int dimension;
		private TableKey(PointType type, int dimension) {
			this.dimension = dimension;
			this.type = type;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + dimension;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			
			if (obj == null)
				return false;
			
			if (getClass() != obj.getClass())
				return false;
			
			TableKey other = (TableKey) obj;
			if (dimension != other.dimension || type != other.type)
				return false;

			return true;
		}
	}

	private PointTreeItemValue pointItemValue;
	private PointType type;
	private int dimension;
	private Map<TableKey, TableView<PointRow>> tables = new HashMap<TableKey, TableView<PointRow>>();

	private static UIPointTableBuilder tableBuilder = new UIPointTableBuilder();
	private UIPointTableBuilder() {
		super();
	}

	public static UIPointTableBuilder getInstance() {
		return tableBuilder;
	}

	public TableView<PointRow> getTable(PointTreeItemValue pointItemValue) {
		this.pointItemValue = pointItemValue;
		this.type      = TreeItemType.getPointTypeByTreeItemType(pointItemValue.getItemType());
		this.dimension = pointItemValue.getDimension();
		this.init();
		return this.table;
	}
	
	private TableContentType getTableContentType() {
		switch (this.type) {
		case REFERENCE_POINT:
			switch(this.dimension) {
			case 1:
				return TableContentType.REFERENCE_POINT_1D;
			case 2:
				return TableContentType.REFERENCE_POINT_2D;
			default:
				return TableContentType.REFERENCE_POINT_3D;
			}
		case STOCHASTIC_POINT:
			switch(this.dimension) {
			case 1:
				return TableContentType.STOCHASTIC_POINT_1D;
			case 2:
				return TableContentType.STOCHASTIC_POINT_2D;
			default:
				return TableContentType.STOCHASTIC_POINT_3D;
			}
		case DATUM_POINT:
			switch(this.dimension) {
			case 1:
				return TableContentType.DATUM_POINT_1D;
			case 2:
				return TableContentType.DATUM_POINT_2D;
			default:
				return TableContentType.DATUM_POINT_3D;
			}
		case NEW_POINT:
			switch(this.dimension) {
			case 1:
				return TableContentType.NEW_POINT_1D;
			case 2:
				return TableContentType.NEW_POINT_2D;
			default:
				return TableContentType.NEW_POINT_3D;
			}
		default:
			return TableContentType.UNSPECIFIC;
		}
	}

	private void init() {
		if (this.tables.containsKey(new TableKey(this.type, this.dimension))) {
			this.table = this.tables.get(new TableKey(this.type, this.dimension));
			return;
		}
		TableColumn<PointRow, Boolean> booleanColumn = null;
		TableColumn<PointRow, String> stringColumn   = null;
		TableColumn<PointRow, Double> doubleColumn   = null;
		TableColumn<PointRow, Integer> integerColumn = null; 
		
		TableContentType tableContentType = this.getTableContentType();

		TableView<PointRow> table = this.createTable();
		///////////////// A-PRIORI VALUES /////////////////////////////
		// Enable/Disable
		int columnIndex = table.getColumns().size(); 
		final int columnIndexEnable = columnIndex;
		String labelText   = i18n.getString("UIPointTableBuilder.tableheader.enable.label", "Enable");
		String tooltipText = i18n.getString("UIPointTableBuilder.tableheader.enable.tooltip", "State of the point");
		CellValueType cellValueType = CellValueType.BOOLEAN;
		ColumnContentType columnContentType = ColumnContentType.ENABLE;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(tableContentType, columnContentType, header, PointRow::enableProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, true, true);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<PointRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<PointRow, Boolean> param) {
				final TableCellChangeListener<Boolean> enableChangeListener = new TableCellChangeListener<Boolean>(columnIndexEnable, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isEnable());
				booleanProp.addListener(enableChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		// Point-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.station.name.label", "Point-Id");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.station.name.tooltip", "Id of the point");
		cellValueType = CellValueType.STRING;
		columnContentType = ColumnContentType.POINT_NAME;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(tableContentType, columnContentType, header, PointRow::nameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true, true); 
		stringColumn.setComparator(new NaturalOrderTableColumnComparator<String>(stringColumn));
		table.getColumns().add(stringColumn);

		// Code
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.station.code.label", "Code");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.station.code.tooltip", "Code of the point");
		cellValueType = CellValueType.STRING;
		columnContentType = ColumnContentType.CODE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(tableContentType, columnContentType, header, PointRow::codeProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true, true); 
		stringColumn.setComparator(new NaturalOrderTableColumnComparator<String>(stringColumn));
		table.getColumns().add(stringColumn);

		// A-priori Components
		// Y0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.y0.label", "y0");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.y0.tooltip", "A-priori y-component of the point");
		cellValueType = CellValueType.LENGTH;
		columnContentType = ColumnContentType.VALUE_Y_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::yAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_POINT, columnIndex, true, true);
		table.getColumns().add(doubleColumn);

		// X0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.x0.label", "x0");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.x0.tooltip", "A-priori x-component of the point");
		cellValueType = CellValueType.LENGTH;
		columnContentType = ColumnContentType.VALUE_X_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::xAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APRIORI_POINT, columnIndex, true, true);
		table.getColumns().add(doubleColumn);

		// Z0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.z0.label", "z0");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.z0.tooltip", "A-priori z-component of the point");
		cellValueType = CellValueType.LENGTH;
		columnContentType = ColumnContentType.VALUE_Z_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::zAprioriProperty, getDoubleCallback(cellValueType), this.dimension != 2 ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true, true);
		table.getColumns().add(doubleColumn);

		// A-priori Uncertainties
		// Y0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.sigma.y0.label", "\u03C3y0");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.sigma.y0.tooltip", "A-priori uncertainty of y-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		columnContentType = ColumnContentType.UNCERTAINTY_Y_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::sigmaYaprioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 && type == PointType.STOCHASTIC_POINT ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// X0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.sigma.x0.label", "\u03C3x0");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.sigma.x0.tooltip", "A-priori uncertainty of x-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		columnContentType = ColumnContentType.UNCERTAINTY_X_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::sigmaXaprioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 && type == PointType.STOCHASTIC_POINT ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Z0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.sigma.z0.label", "\u03C3z0");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.sigma.z0.tooltip", "A-priori uncertainty of z-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		columnContentType = ColumnContentType.UNCERTAINTY_Z_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::sigmaZaprioriProperty, getDoubleCallback(cellValueType), this.dimension != 2 && type == PointType.STOCHASTIC_POINT ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		///////////////// A-POSTERIORI VALUES /////////////////////////////
		// A-posteriori Components

		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.y.label", "y");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.y.tooltip", "A-posteriori y-component of the point");
		cellValueType = CellValueType.LENGTH;
		columnContentType = ColumnContentType.VALUE_Y_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::yAposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.x.label", "x");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.x.tooltip", "A-posteriori x-component of the point");
		cellValueType = CellValueType.LENGTH;
		columnContentType = ColumnContentType.VALUE_X_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::xAposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.z.label", "z");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.z.tooltip", "A-posteriori z-component of the point");
		cellValueType = CellValueType.LENGTH;
		columnContentType = ColumnContentType.VALUE_Z_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::zAposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 2 ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);


		// A-posteriori Uncertainties
		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.sigma.y.label", "\u03C3y");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.sigma.y.tooltip", "A-posteriori uncertainty of y-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		columnContentType = ColumnContentType.UNCERTAINTY_Y_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::sigmaYaposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 && type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.sigma.x.label", "\u03C3x");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.sigma.x.tooltip", "A-posteriori uncertainty of x-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		columnContentType = ColumnContentType.UNCERTAINTY_X_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::sigmaXaposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 1 && type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.sigma.z.label", "\u03C3z");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.sigma.z.tooltip", "A-posteriori uncertainty of z-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		columnContentType = ColumnContentType.UNCERTAINTY_Z_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::sigmaZaposterioriProperty, getDoubleCallback(cellValueType), this.dimension != 2 && type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Confidence
		// A
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.semiaxis.major.label", "a");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.semiaxis.major.tooltip", "Major semi axis");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		columnContentType = ColumnContentType.CONFIDENCE_A;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::confidenceAProperty, getDoubleCallback(cellValueType), this.type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// B
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.semiaxis.middle.label", "b");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.semiaxis.middle.tooltip", "Middle semi axis");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		columnContentType = ColumnContentType.CONFIDENCE_B;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::confidenceBProperty, getDoubleCallback(cellValueType), this.dimension == 3 && this.type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// C
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.semiaxis.minor.label", "c");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.semiaxis.minor.tooltip", "Minor semi axis");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		columnContentType = ColumnContentType.CONFIDENCE_C;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::confidenceCProperty, getDoubleCallback(cellValueType), this.dimension != 1 && this.type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// alpha
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.semiaxisrotation.alpha.label", "\u03B1");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.semiaxisrotation.alpha.tooltip", "Rotation angle of confidence region");
		cellValueType = CellValueType.ANGLE;
		columnContentType = ColumnContentType.CONFIDENCE_ALPHA;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::confidenceAlphaProperty, getDoubleCallback(cellValueType), this.dimension == 3 && this.type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// beta
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.semiaxisrotation.beta.label", "\u03B2");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.semiaxisrotation.beta.tooltip", "Rotation angle of confidence region");
		cellValueType = CellValueType.ANGLE;
		columnContentType = ColumnContentType.CONFIDENCE_BETA;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::confidenceBetaProperty, getDoubleCallback(cellValueType), this.dimension == 3 && this.type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// gamma
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.semiaxisrotation.gamma.label", "\u03B3");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.semiaxisrotation.gamma.tooltip", "Rotation angle of confidence region");
		cellValueType = CellValueType.ANGLE;
		columnContentType = ColumnContentType.CONFIDENCE_GAMMA;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::confidenceGammaProperty, getDoubleCallback(cellValueType), this.dimension != 1 && this.type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		table.getColumns().add(doubleColumn);

		// Redundancy
		// ry
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.redundancy.y.label", "ry");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.redundancy.y.tooltip", "Redundancy of y-component");
		cellValueType = CellValueType.PERCENTAGE;
		columnContentType = ColumnContentType.REDUNDANCY_Y;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::redundancyYProperty, getDoubleCallback(cellValueType), this.type == PointType.STOCHASTIC_POINT && this.dimension != 1 ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// rx
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.redundancy.x.label", "rx");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.redundancy.x.tooltip", "Redundancy of x-component");
		cellValueType = CellValueType.PERCENTAGE;
		columnContentType = ColumnContentType.REDUNDANCY_X;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::redundancyXProperty, getDoubleCallback(cellValueType), this.type == PointType.STOCHASTIC_POINT && this.dimension != 1 ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// rz
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.redundancy.z.label", "rz");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.redundancy.z.tooltip", "Redundancy of z-component");
		cellValueType = CellValueType.PERCENTAGE;
		columnContentType = ColumnContentType.REDUNDANCY_Z;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::redundancyZProperty, getDoubleCallback(cellValueType), this.type == PointType.STOCHASTIC_POINT && this.dimension != 2 ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// number of observations
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.number_of_observations.label", "n");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.number_of_observations.tooltip", "Number of observations involving point");
		cellValueType = CellValueType.INTEGER;
		columnContentType = ColumnContentType.NUMBER_OF_OBSERVATIONS;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		integerColumn = this.<Integer>getColumn(tableContentType, columnContentType, header, PointRow::numberOfObservationsProperty, getIntegerCallback(), ColumnType.APOSTERIORI_POINT, columnIndex, false, true);
		table.getColumns().add(integerColumn);
		
		// Residual 
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = this.type == PointType.STOCHASTIC_POINT ? i18n.getString("UIPointTableBuilder.tableheader.residual.y.label", "\u03B5y")                                                 : i18n.getString("UIPointTableBuilder.tableheader.deviation.y.label", "\u0394y");
		tooltipText = this.type == PointType.STOCHASTIC_POINT ? i18n.getString("UIPointTableBuilder.tableheader.residual.y.tooltip", "Residual of y-component of point, i.e. computed minus observed") : i18n.getString("UIPointTableBuilder.tableheader.deviation.y.tooltip", "Deviation of y-component w.r.t. y0");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.RESIDUAL_X;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::residualYProperty, getDoubleCallback(cellValueType), this.dimension != 1 && this.type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = this.type == PointType.STOCHASTIC_POINT ? i18n.getString("UIPointTableBuilder.tableheader.residual.x.label", "\u03B5x")                                                 : i18n.getString("UIPointTableBuilder.tableheader.deviation.x.label", "\u0394x");
		tooltipText = this.type == PointType.STOCHASTIC_POINT ? i18n.getString("UIPointTableBuilder.tableheader.residual.x.tooltip", "Residual of x-component of point, i.e. computed minus observed") : i18n.getString("UIPointTableBuilder.tableheader.deviation.x.tooltip", "Deviation of x-component w.r.t. x0");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.RESIDUAL_Y;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::residualXProperty, getDoubleCallback(cellValueType), this.dimension != 1 && this.type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = this.type == PointType.STOCHASTIC_POINT ? i18n.getString("UIPointTableBuilder.tableheader.residual.z.label", "\u03B5z")                                                 : i18n.getString("UIPointTableBuilder.tableheader.deviation.z.label", "\u0394z");
		tooltipText = this.type == PointType.STOCHASTIC_POINT ? i18n.getString("UIPointTableBuilder.tableheader.residual.z.tooltip", "Residual of z-component of point, i.e. computed minus observed") : i18n.getString("UIPointTableBuilder.tableheader.deviation.z.tooltip", "Deviation of z-component w.r.t. z0");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.RESIDUAL_Z;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::residualZProperty, getDoubleCallback(cellValueType), this.dimension != 2 && this.type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Gross-Error
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.grosserror.y.label", "\u2207y");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.grosserror.y.tooltip", "Gross-error in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.GROSS_ERROR_Y;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::grossErrorYProperty, getDoubleCallback(cellValueType), this.dimension != 1 && (this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT) ? ColumnType.APOSTERIORI_POINT : this.dimension != 1 && this.type == PointType.DATUM_POINT ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.grosserror.x.label", "\u2207x");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.grosserror.x.tooltip", "Gross-error in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.GROSS_ERROR_X;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::grossErrorXProperty, getDoubleCallback(cellValueType), this.dimension != 1 && (this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT) ? ColumnType.APOSTERIORI_POINT : this.dimension != 1 && this.type == PointType.DATUM_POINT ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.grosserror.z.label", "\u2207z");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.grosserror.z.tooltip", "Gross-error in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.GROSS_ERROR_Z;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::grossErrorZProperty, getDoubleCallback(cellValueType), this.dimension != 2 && (this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT) ? ColumnType.APOSTERIORI_POINT : this.dimension != 2 && this.type == PointType.DATUM_POINT ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// MTB
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.maximumtolerablebias.y.label", "\u2207y(1)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.maximumtolerablebias.y.tooltip", "Maximum tolerable bias in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.MAXIMUM_TOLERABLE_BIAS_Y;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::maximumTolerableBiasYProperty, getDoubleCallback(cellValueType), this.dimension != 1 && (this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT) ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.maximumtolerablebias.x.label", "\u2207x(1)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.maximumtolerablebias.x.tooltip", "Maximum tolerable bias in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.MAXIMUM_TOLERABLE_BIAS_X;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::maximumTolerableBiasXProperty, getDoubleCallback(cellValueType), this.dimension != 1 && (this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT) ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.maximumtolerablebias.z.label", "\u2207z(1)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.maximumtolerablebias.z.tooltip", "Maximum tolerable bias in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.MAXIMUM_TOLERABLE_BIAS_Z;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::maximumTolerableBiasZProperty, getDoubleCallback(cellValueType), this.dimension != 2 && (this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT) ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// MDB
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.minimaldetectablebias.y.label", "\u2207y(\u03BB)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.minimaldetectablebias.y.tooltip", "Minimal detectable bias in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.MINIMAL_DETECTABLE_BIAS_Y;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::minimalDetectableBiasYProperty, getDoubleCallback(cellValueType), this.dimension != 1 && (this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT) ? ColumnType.APOSTERIORI_POINT : this.dimension != 1 && this.type == PointType.DATUM_POINT ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.minimaldetectablebias.x.label", "\u2207x(\u03BB)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.minimaldetectablebias.x.tooltip", "Minimal detectable bias in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.MINIMAL_DETECTABLE_BIAS_X;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::minimalDetectableBiasXProperty, getDoubleCallback(cellValueType), this.dimension != 1 && (this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT) ? ColumnType.APOSTERIORI_POINT : this.dimension != 1 && this.type == PointType.DATUM_POINT ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.minimaldetectablebias.z.label", "\u2207z(\u03BB)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.minimaldetectablebias.z.tooltip", "Minimal detectable bias in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.MINIMAL_DETECTABLE_BIAS_Z;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::minimalDetectableBiasZProperty, getDoubleCallback(cellValueType), this.dimension != 2 && (this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT) ? ColumnType.APOSTERIORI_POINT : this.dimension != 2 && this.type == PointType.DATUM_POINT ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Influence on point position (EP)
		// EP-y
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.influenceonposition.y.label", "EPy");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.influenceonposition.y.tooltip", "Influence on point position due to an undetected gross-error in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.INFLUENCE_ON_POINT_POSITION_Y;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::influenceOnPointPositionYProperty, getDoubleCallback(cellValueType), this.type == PointType.STOCHASTIC_POINT && this.dimension != 1 ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// EP-x
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.influenceonposition.x.label", "EPx");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.influenceonposition.x.tooltip", "Influence on point position due to an undetected gross-error in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.INFLUENCE_ON_POINT_POSITION_X;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::influenceOnPointPositionXProperty, getDoubleCallback(cellValueType), this.type == PointType.STOCHASTIC_POINT && this.dimension != 1 ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// EP-z
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.influenceonposition.z.label", "EPz");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.influenceonposition.z.tooltip", "Influence on point position due to an undetected gross-error in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.INFLUENCE_ON_POINT_POSITION_Z;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::influenceOnPointPositionZProperty, getDoubleCallback(cellValueType), this.type == PointType.STOCHASTIC_POINT && this.dimension != 2 ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// EFSP
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.influenceonnetworkdistortion.label", "EF\u00B7SPmax");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.influenceonnetworkdistortion.tooltip", "Maximum influence on network distortion");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.INFLUENCE_ON_NETWORK_DISTORTION;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::influenceOnNetworkDistortionProperty, getDoubleCallback(cellValueType), this.type == PointType.STOCHASTIC_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// PCA
		// y-comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.principalcomponent.y.label", "\u03C2y");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.principalcomponent.y.tooltip", "Y-component of first principal component analysis");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.FIRST_PRINCIPLE_COMPONENT_Y;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::firstPrincipalComponentYProperty, getDoubleCallback(cellValueType), this.dimension != 1 && this.type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// x-comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.principalcomponent.x.label", "\u03C2x");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.principalcomponent.x.tooltip", "X-component of first principal component analysis");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.FIRST_PRINCIPLE_COMPONENT_X;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::firstPrincipalComponentXProperty, getDoubleCallback(cellValueType), this.dimension != 1 && this.type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.principalcomponent.z.label", "\u03C2z");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.principalcomponent.z.tooltip", "Z-component of first principal component analysis");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		columnContentType = ColumnContentType.FIRST_PRINCIPLE_COMPONENT_Z;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::firstPrincipalComponentZProperty, getDoubleCallback(cellValueType), this.dimension != 2 && this.type != PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);			

		// vTPv
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.omega.label", "\u03A9");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.omega.tooltip", "Weighted squares of residual");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = ColumnContentType.OMEGA;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::omegaProperty, getDoubleCallback(cellValueType), this.type == PointType.STOCHASTIC_POINT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);	

		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.pvalue.apriori.label", "log(Pprio)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.pvalue.apriori.tooltip", "A-priori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = ColumnContentType.P_VALUE_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::pValueAprioriProperty, getDoubleCallback(cellValueType), this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : this.type == PointType.DATUM_POINT ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.pvalue.aposteriori.label", "log(Ppost)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.pvalue.aposteriori.tooltip", "A-posteriori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = ColumnContentType.P_VALUE_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::pValueAposterioriProperty, getDoubleCallback(cellValueType), this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : this.type == PointType.DATUM_POINT ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Tprio
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.teststatistic.apriori.label", "Tprio");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.teststatistic.apriori.tooltip", "A-priori test statistic");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = ColumnContentType.TEST_STATISTIC_APRIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::testStatisticAprioriProperty, getDoubleCallback(cellValueType), this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : this.type == PointType.DATUM_POINT ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Tpost
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.teststatistic.aposteriori.label", "Tpost");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.teststatistic.aposteriori.tooltip", "A-posteriori test statistic");
		cellValueType = CellValueType.STATISTIC;
		columnContentType = ColumnContentType.TEST_STATISTIC_APOSTERIORI;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(tableContentType, columnContentType, header, PointRow::testStatisticAposterioriProperty, getDoubleCallback(cellValueType), this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : this.type == PointType.DATUM_POINT ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false, true);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexOutlier = columnIndex;
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.testdecision.label", "Significant");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.testdecision.tooltip", "Checked, if null-hypothesis is rejected");
		cellValueType = CellValueType.BOOLEAN;
		columnContentType = ColumnContentType.SIGNIFICANT;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(tableContentType, columnContentType, header, PointRow::significantProperty, getBooleanCallback(), this.type == PointType.STOCHASTIC_POINT || this.type == PointType.REFERENCE_POINT ? ColumnType.APOSTERIORI_POINT : this.type == PointType.DATUM_POINT ? ColumnType.APOSTERIORI_POINT_CONGRUENCE : ColumnType.HIDDEN, columnIndex, false, true);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<PointRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<PointRow, Boolean> param) {
				final TableCellChangeListener<Boolean> significantChangeListener = new TableCellChangeListener<Boolean>(columnIndexOutlier, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isSignificant());
				booleanProp.addListener(significantChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		this.addContextMenu(table, this.createContextMenu(true));
		this.addDynamicRowAdder(table);
		this.addColumnOrderSequenceListeners(tableContentType, table);

		this.tables.put(new TableKey(this.type, this.dimension), table);
		this.table = table;
	}

	
	@Override
	public PointRow getEmptyRow() {
		return new PointRow();
	}

	@Override
	void setValue(PointRow rowData, int columnIndex, Object oldValue, Object newValue) {
		boolean valid = (oldValue == null || oldValue.toString().trim().isEmpty()) && (newValue == null || newValue.toString().trim().isEmpty());
		switch (columnIndex) {
		case 0:
			rowData.setEnable(newValue != null && newValue instanceof Boolean && (Boolean)newValue);
			valid = true;
			break;
		case 1:
			if (newValue != null && !newValue.toString().trim().isEmpty()) {
				rowData.setName(newValue.toString().trim());
				valid = true;
			}
			else
				rowData.setName(oldValue == null ? null : oldValue.toString().trim());
			break;
		case 2:
			rowData.setCode(newValue != null ? newValue.toString().trim() : null);
			valid = true;
			break;
		case 3:
			if (newValue != null && newValue instanceof Double) {
				rowData.setYApriori((Double)newValue);	
				valid = true;
			}
			else
				rowData.setYApriori(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 4:
			if (newValue != null && newValue instanceof Double) {
				rowData.setXApriori((Double)newValue);	
				valid = true;
			}
			else
				rowData.setXApriori(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 5:
			if (newValue != null && newValue instanceof Double) {
				rowData.setZApriori((Double)newValue);	
				valid = true;
			}
			else
				rowData.setZApriori(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 6:
			rowData.setSigmaYapriori(newValue == null ? null : newValue instanceof Double && (Double)newValue > 0 ? (Double)newValue : null);
			valid = true;
			break;
		case 7:
			rowData.setSigmaXapriori(newValue == null ? null : newValue instanceof Double && (Double)newValue > 0 ? (Double)newValue : null);
			valid = true;
			break;
		case 8:
			rowData.setSigmaZapriori(newValue == null ? null : newValue instanceof Double && (Double)newValue > 0 ? (Double)newValue : null);
			valid = true;
			break;
		default:
			System.err.println(this.getClass().getSimpleName() + " : Editable column exceed " + columnIndex);
			valid = false;
			break;
		}
		
		if (valid && this.isComplete(rowData)) {
			// Set observation group id, if not exists
			if (rowData.getGroupId() < 0)
				rowData.setGroupId(this.pointItemValue.getGroupId());
						
			try {
				SQLManager.getInstance().saveItem(rowData);
			} catch (Exception e) {
				switch (columnIndex) {
				case 1:
					rowData.setName(oldValue == null ? null : oldValue.toString().trim());
					break;
				default:
					break;
				}
				valid = false;
				raiseErrorMessageSaveValue(e);
				e.printStackTrace();
			}
		}

		if (!valid) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					table.refresh();
					table.requestFocus();
					table.getSelectionModel().clearSelection();
					table.getSelectionModel().select(rowData);
				}
			});
		}
		else if (this.isComplete(rowData))
			this.table.sort();
	}
	
	private boolean isComplete(PointRow row) {
		return row.getName() != null && !row.getName().trim().isEmpty() &&
				row.getXApriori() != null &&
				row.getYApriori() != null &&
				row.getZApriori() != null;
	}
	
	@Override
	void enableDragSupport() {
		this.table.setOnDragDetected(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent event) {
		    	List<PointRow> selectedRows = new ArrayList<PointRow>(table.getSelectionModel().getSelectedItems());
		    	if (selectedRows != null && !selectedRows.isEmpty()) {

		    		List<PointRowDnD> rowsDnD = new ArrayList<PointRowDnD>(selectedRows.size());
		    		for (PointRow selectedRow : selectedRows) {
		    			PointRowDnD rowDnD = null;
		    			if (isComplete(selectedRow) && (rowDnD = PointRowDnD.fromPointRow(selectedRow)) != null) {
		    				rowsDnD.add(rowDnD);
		    			}
		    		}

		    		if (!rowsDnD.isEmpty()) {
		    			Dragboard db = table.startDragAndDrop(TransferMode.MOVE);
		    			ClipboardContent content = new ClipboardContent();
		    			content.put(EditableMenuCheckBoxTreeCell.TREE_ITEM_TYPE_DATA_FORMAT, pointItemValue.getItemType());
		    			content.put(EditableMenuCheckBoxTreeCell.GROUP_ID_DATA_FORMAT,       pointItemValue.getGroupId());
		    			content.put(EditableMenuCheckBoxTreeCell.DIMENSION_DATA_FORMAT,      dimension);
		    			content.put(EditableMenuCheckBoxTreeCell.POINT_ROWS_DATA_FORMAT, rowsDnD);

		    			db.setContent(content);
		    		}
		    	}
		        event.consume();
		    }
		});
	}
	
	@Override
	void duplicateRows() {
		List<PointRow> selectedRows = new ArrayList<PointRow>(this.table.getSelectionModel().getSelectedItems());
		if (selectedRows == null || selectedRows.isEmpty())
			return;

		List<PointRow> clonedRows = new ArrayList<PointRow>(selectedRows.size());
		for (PointRow row : selectedRows) {
			PointRow clonedRow = PointRow.cloneRowApriori(row);
			
			if (this.isComplete(clonedRow)) {
				try {
					// Generate next unique point name
					clonedRow.setName(SQLManager.getInstance().getNextValidPointName(row.getName()));
					SQLManager.getInstance().saveItem(clonedRow);
				} 
				catch (Exception e) {
					raiseErrorMessage(ContextMenuType.DUPLICATE, e);
					e.printStackTrace();
					break;
				}
			}
			clonedRows.add(clonedRow);
		}

		if (clonedRows != null && !clonedRows.isEmpty()) {
			ObservableList<PointRow> tableModel = this.getTableModel(this.table);
			tableModel.addAll(clonedRows);
			this.table.getSelectionModel().clearSelection();
			for (PointRow clonedRow : clonedRows)
				this.table.getSelectionModel().select(clonedRow);
			this.table.scrollTo(clonedRows.get(0));
		}
	}
	
	@Override
	void removeRows() {
		List<PointRow> selectedRows = new ArrayList<PointRow>(this.table.getSelectionModel().getSelectedItems());
		if (selectedRows == null || selectedRows.isEmpty())
			return;
		
		List<PointRow> removedRows = new ArrayList<PointRow>(selectedRows.size());
		for (PointRow row : selectedRows) {
			if (this.isComplete(row)) {
				try {
					SQLManager.getInstance().remove(row);
				} 
				catch (Exception e) {
					raiseErrorMessage(ContextMenuType.REMOVE, e);
					e.printStackTrace();
					break;
				}
			}
			removedRows.add(row);
		}
		if (removedRows != null && !removedRows.isEmpty()) {
			this.table.getSelectionModel().clearSelection();
			ObservableList<PointRow> tableModel = this.getTableModel(this.table);
			tableModel.removeAll(removedRows);
			if (tableModel.isEmpty())
				tableModel.setAll(getEmptyRow());
		}
	}
	
	@Override
	void moveRows(ContextMenuType type) {
		TreeItemType parentType = null;

		switch(type) {
		case MOVETO_DATUM:
			if (this.dimension == 1)
				parentType = TreeItemType.DATUM_POINT_1D_DIRECTORY;
			else if (this.dimension == 2)
				parentType = TreeItemType.DATUM_POINT_2D_DIRECTORY;
			else if (this.dimension == 3)
				parentType = TreeItemType.DATUM_POINT_3D_DIRECTORY;
			break;
		case MOVETO_NEW:
			if (this.dimension == 1)
				parentType = TreeItemType.NEW_POINT_1D_DIRECTORY;
			else if (this.dimension == 2)
				parentType = TreeItemType.NEW_POINT_2D_DIRECTORY;
			else if (this.dimension == 3)
				parentType = TreeItemType.NEW_POINT_3D_DIRECTORY;
			break;
		case MOVETO_REFERENCE:
			if (this.dimension == 1)
				parentType = TreeItemType.REFERENCE_POINT_1D_DIRECTORY;
			else if (this.dimension == 2)
				parentType = TreeItemType.REFERENCE_POINT_2D_DIRECTORY;
			else if (dimension == 3)
				parentType = TreeItemType.REFERENCE_POINT_3D_DIRECTORY;
			break;
		case MOVETO_STOCHASTIC:
			if (this.dimension == 1)
				parentType = TreeItemType.STOCHASTIC_POINT_1D_DIRECTORY;
			else if (this.dimension == 2)
				parentType = TreeItemType.STOCHASTIC_POINT_2D_DIRECTORY;
			else if (this.dimension == 3)
				parentType = TreeItemType.STOCHASTIC_POINT_3D_DIRECTORY;
			break;
		default:
			parentType = null;
			break;
		}
		
		List<PointRow> selectedRows = new ArrayList<PointRow>(this.table.getSelectionModel().getSelectedItems());
		if (parentType == null || selectedRows == null || selectedRows.isEmpty())
			return;
		
		TreeItem<TreeItemValue> newTreeItem = UITreeBuilder.getInstance().addItem(parentType, false);
		try {
			SQLManager.getInstance().saveGroup((PointTreeItemValue)newTreeItem.getValue());
		} 
		catch (Exception e) {
			raiseErrorMessage(type, e);
			UITreeBuilder.getInstance().removeItem(newTreeItem);
			e.printStackTrace();
			return;
		}
		
		try {
			int groupId = ((PointTreeItemValue)newTreeItem.getValue()).getGroupId();
			for (PointRow row : selectedRows) {
				row.setGroupId(groupId);
				SQLManager.getInstance().saveItem(row);
			}
		} 
		catch (Exception e) {
			raiseErrorMessage(type, e);
			e.printStackTrace();
			return;
		}

		MultipleSelectionModel<TreeItem<TreeItemValue>> selectionModel = UITreeBuilder.getInstance().getTree().getSelectionModel();
		selectionModel.clearSelection();
		selectionModel.select(newTreeItem);
	}
	
	public void export(File file, boolean aprioriValues) throws IOException {
		List<PointRow> rows = this.table.getItems();

		String exportFormatString = "%15s \t";
		//String exportFormatDouble = "%+15.6f \t";
		String exportFormatDouble = "%20s \t";
		
		PrintWriter writer = null;

		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter( file )));

			for (PointRow row : rows) {
				if (!row.isEnable())
					continue;

				String name = row.getName();

				if (name == null || name.trim().isEmpty())
					continue;

				Double y = aprioriValues ? row.getYApriori() : row.getYAposteriori();
				Double x = aprioriValues ? row.getXApriori() : row.getXAposteriori();
				Double z = aprioriValues ? row.getZApriori() : row.getZAposteriori();

				if (this.dimension != 2 && z == null)
					continue;
				
				if (this.dimension != 1 && (y == null || x == null))
					continue;

				Double sigmaY = aprioriValues ? row.getSigmaYapriori() : row.getSigmaYaposteriori();
				Double sigmaX = aprioriValues ? row.getSigmaXapriori() : row.getSigmaXaposteriori();
				Double sigmaZ = aprioriValues ? row.getSigmaZapriori() : row.getSigmaZaposteriori();

				if (!aprioriValues && (sigmaY == null || sigmaX == null || sigmaZ == null))
					continue;
				
				if (aprioriValues) {
					if (sigmaY != null && sigmaY <= 0)
						sigmaY = null;
					if (sigmaX != null && sigmaX <= 0)
						sigmaX = null;
					if (sigmaZ != null && sigmaZ <= 0)
						sigmaZ = null;
				}
				
				String yValue = y != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(y, false)) : "";
				String xValue = x != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(x, false)) : "";
				String zValue = this.dimension != 2 && z != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(z, false)) : "";

				String sigmaYvalue = this.dimension != 1 && sigmaY != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(sigmaY, false)) : "";
				String sigmaXvalue = this.dimension != 1 && sigmaX != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(sigmaX, false)) : "";
				String sigmaZvalue = this.dimension != 2 && sigmaZ != null ? String.format(Locale.ENGLISH, exportFormatDouble, options.toLengthFormat(sigmaZ, false)) : "";

				writer.println(
						String.format(exportFormatString, name) +
						yValue + xValue + zValue + sigmaYvalue + sigmaXvalue + sigmaZvalue
						);
			}
		}
		finally {
			if (writer != null)
				writer.close();
		}
	}
	
	@Override
	void highlightTableRow(TableRow<PointRow> row) {
		if (row == null)
			return;
		
		TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
		TableRowHighlightType tableRowHighlightType = tableRowHighlight.getSelectedTableRowHighlightType(); 
		double leftBoundary  = tableRowHighlight.getLeftBoundary(tableRowHighlightType); 
		double rightBoundary = tableRowHighlight.getRightBoundary(tableRowHighlightType);

		PointRow item = row.getItem();

		if (!row.isSelected() && item != null) {
			switch(tableRowHighlightType) {
			case TEST_STATISTIC:
				if (this.type != PointType.NEW_POINT)
					this.setTableRowHighlight(row, item.isSignificant() ? TableRowHighlightRangeType.INADEQUATE : TableRowHighlightRangeType.EXCELLENT);
				else
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				break;
				
			case GROSS_ERROR:
				if (this.type != PointType.NEW_POINT) {
					Double grossErrorX = item.getGrossErrorX();
					Double grossErrorY = item.getGrossErrorY();
					Double grossErrorZ = item.getGrossErrorZ();

					Double mtbX = item.getMaximumTolerableBiasX();
					Double mtbY = item.getMaximumTolerableBiasY();
					Double mtbZ = item.getMaximumTolerableBiasZ();

					Double mdbX = item.getMinimalDetectableBiasX();
					Double mdbY = item.getMinimalDetectableBiasY();
					Double mdbZ = item.getMinimalDetectableBiasZ();

					double dMTB = Double.NaN;
					double dMDB = Double.NaN;

					if (this.dimension == 1 && grossErrorZ != null && mtbZ != null && mdbZ != null) {
						dMTB = Math.abs(grossErrorZ) - Math.abs(mtbZ);
						dMDB = Math.abs(mdbZ) - Math.abs(grossErrorZ);
					}

					else if (this.dimension == 2 && grossErrorX != null && grossErrorY != null && mtbX != null && mtbY != null && mdbX != null && mdbY != null) {
						dMTB = Math.max(Math.abs(grossErrorX) - Math.abs(mtbX), Math.abs(grossErrorY) - Math.abs(mtbY));
						dMDB = Math.min(Math.abs(mdbX) - Math.abs(grossErrorX), Math.abs(mdbY) - Math.abs(grossErrorY));
					}

					else if (this.dimension == 3 && grossErrorX != null && grossErrorY != null && grossErrorZ != null && mtbZ != null && mtbX != null && mtbY != null && mdbZ != null && mdbX != null && mdbY != null) {
						dMTB = Math.max(Math.max(Math.abs(grossErrorX) - Math.abs(mtbX), Math.abs(grossErrorY) - Math.abs(mtbY)), Math.abs(grossErrorZ) - Math.abs(mtbZ));
						dMDB = Math.min(Math.min(Math.abs(mdbX) - Math.abs(grossErrorX), Math.abs(mdbY) - Math.abs(grossErrorY)), Math.abs(mdbZ) - Math.abs(grossErrorZ));
					}

					if (!Double.isNaN(dMTB) && !Double.isNaN(dMDB)) {
						if (dMDB < 0) 
							this.setTableRowHighlight(row, TableRowHighlightRangeType.INADEQUATE);
						
						else if (dMTB > 0 && dMDB >= 0) 
							this.setTableRowHighlight(row, TableRowHighlightRangeType.SATISFACTORY);
						
						else // (dMTB <= 0) 
							this.setTableRowHighlight(row, TableRowHighlightRangeType.EXCELLENT);
					}
					else
						this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				}
				else
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				
				break;
				
			case REDUNDANCY:
				if (this.type == PointType.STOCHASTIC_POINT) {
					Double redundancyX = item.getRedundancyX();
					Double redundancyY = item.getRedundancyY();
					Double redundancyZ = item.getRedundancyZ();
					Double redundancy = null;
					
					if (this.dimension == 1 && redundancyZ != null) 
						redundancy = redundancyZ;

					else if (this.dimension == 2 && redundancyY != null && redundancyX != null) 
						redundancy = Math.min(redundancyY, redundancyX);
					
					else if (this.dimension == 3 && redundancyY != null && redundancyX != null && redundancyZ != null) 
						redundancy = Math.min(redundancyZ, Math.min(redundancyY, redundancyX));
					
					if (redundancy == null) 
						this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
					else
						this.setTableRowHighlight(row, redundancy < leftBoundary ? TableRowHighlightRangeType.INADEQUATE : 
							redundancy <= rightBoundary ? TableRowHighlightRangeType.SATISFACTORY :
								TableRowHighlightRangeType.EXCELLENT);
				}
				else
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				
				break;
				
			case INFLUENCE_ON_POSITION:
				if (this.type == PointType.STOCHASTIC_POINT) {
					Double influenceOnPositionX = item.getInfluenceOnPointPositionX();
					Double influenceOnPositionY = item.getInfluenceOnPointPositionY();
					Double influenceOnPositionZ = item.getInfluenceOnPointPositionZ();
					Double influenceOnPosition = null;

					if (this.dimension == 1 && influenceOnPositionZ != null) 
						influenceOnPosition = influenceOnPositionZ;

					else if (this.dimension == 2 && influenceOnPositionY != null && influenceOnPositionX != null) 
						influenceOnPosition = Math.max(influenceOnPositionY, influenceOnPositionX);

					else if (this.dimension == 3 && influenceOnPositionY != null && influenceOnPositionX != null && influenceOnPositionZ != null)
						influenceOnPosition = Math.max(influenceOnPositionZ, Math.max(influenceOnPositionY, influenceOnPositionX));

					if (influenceOnPosition == null) 
						this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
					else
						this.setTableRowHighlight(row, Math.abs(influenceOnPosition) < leftBoundary ? TableRowHighlightRangeType.EXCELLENT : 
							Math.abs(influenceOnPosition) <= rightBoundary ? TableRowHighlightRangeType.SATISFACTORY :
								TableRowHighlightRangeType.INADEQUATE);
				}
				else
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				
				break;
				
			case P_PRIO_VALUE:
				if (this.type != PointType.NEW_POINT) {
					Double pValue = item.getPValueApriori();
					if (pValue == null) 
						this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
					else
						this.setTableRowHighlight(row, pValue < Math.log(leftBoundary / 100.0) ? TableRowHighlightRangeType.INADEQUATE : 
							pValue <= Math.log(rightBoundary / 100.0) ? TableRowHighlightRangeType.SATISFACTORY :
								TableRowHighlightRangeType.EXCELLENT);
				}
				else
					this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);

				break;
				
			case NONE:
				this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
				
				break;
			}

		} 
		else {
			this.setTableRowHighlight(row, TableRowHighlightRangeType.NONE);
		}
	}
}
