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

package org.applied_geodesy.coordtrans.ui.table;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.applied_geodesy.adjustment.transformation.Transformation;
import org.applied_geodesy.adjustment.transformation.TransformationChangeListener;
import org.applied_geodesy.adjustment.transformation.TransformationEvent;
import org.applied_geodesy.adjustment.transformation.TransformationEvent.TransformationEventType;
import org.applied_geodesy.adjustment.transformation.TransformationType;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.coordtrans.ui.dialog.MatrixDialog;
import org.applied_geodesy.coordtrans.ui.utils.UiUtil;
import org.applied_geodesy.ui.table.AbsoluteValueComparator;
import org.applied_geodesy.ui.table.ColumnTooltipHeader;
import org.applied_geodesy.ui.table.ColumnType;
import org.applied_geodesy.ui.table.NaturalOrderComparator;
import org.applied_geodesy.util.CellValueType;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.util.Callback;

public class UIHomologousFramePositionPairTableBuilder extends UIEditableTableBuilder<HomologousFramePositionPair> implements TransformationChangeListener {	
	private TransformationType transformationType = TransformationType.SPATIAL;
	private Map<TransformationType, TableView<HomologousFramePositionPair>> tables = new HashMap<TransformationType, TableView<HomologousFramePositionPair>>(2);

	private static UIHomologousFramePositionPairTableBuilder tableBuilder = new UIHomologousFramePositionPairTableBuilder();
	private UIHomologousFramePositionPairTableBuilder() {
		super();
	}

	public static UIHomologousFramePositionPairTableBuilder getInstance() {
		return tableBuilder;
	}
	
	public void setTransformationType(TransformationType transformationType) {
		if (this.transformationType != transformationType) {
			this.getTableModel(this.table).clear();
			this.table.refresh();
			this.transformationType = transformationType;
		}
	}

	@Override
	public TableView<HomologousFramePositionPair> getTable() {
		this.init();
		return this.table;
	}

	private void init() {
		if (this.tables.containsKey(this.transformationType)) {
			this.table = this.tables.get(this.transformationType);
			return;
		}
		TableColumn<HomologousFramePositionPair, Boolean> booleanColumn = null;
		TableColumn<HomologousFramePositionPair, String> stringColumn   = null;
		TableColumn<HomologousFramePositionPair, Double> doubleColumn   = null;
		TableColumn<HomologousFramePositionPair, Boolean> matrixColumn   = null;

		TableView<HomologousFramePositionPair> table = this.createTable();
		///////////////// A-PRIORI VALUES /////////////////////////////
		// Enable/Disable
		int columnIndex = table.getColumns().size(); 
		final int columnIndexEnable = columnIndex;
		String labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.enable.label", "Enable");
		String tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.enable.tooltip", "State of the point");
		CellValueType cellValueType = CellValueType.BOOLEAN;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, HomologousFramePositionPair::enableProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, true);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<HomologousFramePositionPair, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<HomologousFramePositionPair, Boolean> param) {
				final TableCellChangeListener<Boolean> enableChangeListener = new TableCellChangeListener<Boolean>(columnIndexEnable, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isEnable());
				booleanProp.addListener(enableChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		// Point-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.station.name.label", "Point-Id");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.station.name.tooltip", "Id of the point");
		cellValueType = CellValueType.STRING;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, HomologousFramePositionPair::nameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true); 
		stringColumn.setComparator(new NaturalOrderComparator<String>());
		table.getColumns().add(stringColumn);

		// SOURCE
		// A-priori Components
		// x0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.source.x0.label", "x0");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.source.x0.tooltip", "A-priori x-component of the point in source system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceX0Property, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// y0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.source.y0.label", "y0");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.source.y0.tooltip", "A-priori y-component of the point in source system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceY0Property, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// z0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.source.z0.label", "z0");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.source.z0.tooltip", "A-priori z-component of the point in source system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceZ0Property, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// TARGET
		// X0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.target.x0.label", "X0");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.target.x0.tooltip", "A-priori x-component of the point in target system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetX0Property, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// Y0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.target.y0.label", "Y0");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.target.y0.tooltip", "A-priori y-component of the point in target system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetY0Property, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// Z0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.target.z0.label", "Z0");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.target.z0.tooltip", "A-priori z-component of the point in target system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetZ0Property, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true);
		table.getColumns().add(doubleColumn);
		
		
		// Covariance of both positions
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.covariance.label", "Uncertainties");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.covariance.tooltip", "A-priori variance-covariance matrix");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		matrixColumn = this.<Boolean>getColumn(header, HomologousFramePositionPair::containsDispersionablePositionProperty, getMatrixCallback(), ColumnType.APRIORI_POINT, columnIndex, true);
		table.getColumns().add(matrixColumn);


		///////////////// A-POSTERIORI VALUES /////////////////////////////
		// A-posteriori Components
		// SOURCE
		// x-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.source.x.label", "x");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.source.x.tooltip", "A-posteriori x-component of the point in source system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_SOURCE_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		// y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.source.y.label", "y");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.source.y.tooltip", "A-posteriori y-component of the point in source system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_SOURCE_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.source.z.label", "z");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.source.z.tooltip", "A-posteriori z-component of the point in source system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_SOURCE_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		// A-posteriori Uncertainties
		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.uncertainty.x.label", "\u03C3x");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.uncertainty.x.tooltip", "A-posteriori uncertainty of x-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceUncertaintyXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_SOURCE_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.uncertainty.y.label", "\u03C3y");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.uncertainty.y.tooltip", "A-posteriori uncertainty of y-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceUncertaintyYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_SOURCE_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.uncertainty.z.label", "\u03C3z");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.uncertainty.z.tooltip", "A-posteriori uncertainty of z-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceUncertaintyZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_SOURCE_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Redundancy
		// rx
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.redundancy.x.label", "rx");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.redundancy.x.tooltip", "Redundancy of x-component");
		cellValueType = CellValueType.PERCENTAGE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceRedundancyXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_SOURCE_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// ry
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.redundancy.y.label", "ry");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.redundancy.y.tooltip", "Redundancy of y-component");
		cellValueType = CellValueType.PERCENTAGE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceRedundancyYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_SOURCE_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// rz
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.redundancy.z.label", "rz");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.redundancy.z.tooltip", "Redundancy of z-component");
		cellValueType = CellValueType.PERCENTAGE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceRedundancyZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_SOURCE_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// Residual 
		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.residual.x.label", "\u03B5x");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.residual.x.tooltip", "Residual of x-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceResidualXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_SOURCE_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.residual.y.label", "\u03B5y");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.residual.y.tooltip", "Residual of y-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceResidualYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_SOURCE_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.residual.z.label", "\u03B5z");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.residual.z.tooltip", "Residual of z-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::sourceResidualZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_SOURCE_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		

		// TARGET
		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.target.x.label", "X");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.target.x.tooltip", "A-posteriori x-component of the point in target system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.target.y.label", "Y");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.target.y.tooltip", "A-posteriori y-component of the point in target system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.target.z.label", "Z");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.target.z.tooltip", "A-posteriori z-component of the point in target system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		
		// A-posteriori Uncertainties
		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.uncertainty.x.label", "\u03C3x");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.uncertainty.x.tooltip", "A-posteriori uncertainty of x-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetUncertaintyXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.uncertainty.y.label", "\u03C3y");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.uncertainty.y.tooltip", "A-posteriori uncertainty of y-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetUncertaintyYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.uncertainty.z.label", "\u03C3z");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.uncertainty.z.tooltip", "A-posteriori uncertainty of z-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetUncertaintyZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Redundancy
		// rx
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.redundancy.x.label", "rx");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.redundancy.x.tooltip", "Redundancy of x-component");
		cellValueType = CellValueType.PERCENTAGE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetRedundancyXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// ry
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.redundancy.y.label", "ry");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.redundancy.y.tooltip", "Redundancy of y-component");
		cellValueType = CellValueType.PERCENTAGE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetRedundancyYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// rz
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.redundancy.z.label", "rz");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.redundancy.z.tooltip", "Redundancy of z-component");
		cellValueType = CellValueType.PERCENTAGE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetRedundancyZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// Residual 
		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.residual.x.label", "\u03B5x");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.residual.x.tooltip", "Residual of x-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetResidualXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.residual.y.label", "\u03B5y");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.residual.y.tooltip", "Residual of y-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetResidualYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.residual.z.label", "\u03B5z");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.residual.z.tooltip", "Residual of z-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::targetResidualZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);


		
		// Gross-Error
		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.grosserror.x.label", "\u2207x");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.grosserror.x.tooltip", "Gross-error in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::grossErrorXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.grosserror.y.label", "\u2207y");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.grosserror.y.tooltip", "Gross-error in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::grossErrorYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.grosserror.z.label", "\u2207z");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.grosserror.z.tooltip", "Gross-error in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::grossErrorZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// MTB
		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.maximumtolerablebias.x.label", "\u2207x(1)");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.maximumtolerablebias.x.tooltip", "Maximum tolerable bias in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::maximumTolerableBiasXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.maximumtolerablebias.y.label", "\u2207y(1)");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.maximumtolerablebias.y.tooltip", "Maximum tolerable bias in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::maximumTolerableBiasYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.maximumtolerablebias.z.label", "\u2207z(1)");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.maximumtolerablebias.z.tooltip", "Maximum tolerable bias in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::maximumTolerableBiasZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);		

		// MDB
		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.minimaldetectablebias.x.label", "\u2207x(\u03BB)");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.minimaldetectablebias.x.tooltip", "Minimal detectable bias in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::minimalDetectableBiasXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.minimaldetectablebias.y.label", "\u2207y(\u03BB)");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.minimaldetectablebias.y.tooltip", "Minimal detectable bias in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::minimalDetectableBiasYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.minimaldetectablebias.z.label", "\u2207z(\u03BB)");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.minimaldetectablebias.z.tooltip", "Minimal detectable bias in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::minimalDetectableBiasZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
	
		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.pvalue.apriori.label", "log(Pprio)");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.pvalue.apriori.tooltip", "A-priori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::pValueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.pvalue.aposteriori.label", "log(Ppost)");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.pvalue.aposteriori.tooltip", "A-posteriori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::pValueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Tprio
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.teststatistic.apriori.label", "Tprio");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.teststatistic.apriori.tooltip", "A-priori test statistic");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::testStatisticAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Tpost
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.teststatistic.aposteriori.label", "Tpost");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.teststatistic.aposteriori.tooltip", "A-posteriori test statistic");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, HomologousFramePositionPair::testStatisticAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexSignificant = columnIndex;
		labelText   = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.testdecision.label", "Significant");
		tooltipText = i18n.getString("UIHomologousFramePositionPairTableBuilder.tableheader.testdecision.tooltip", "Checked, if null-hypothesis is rejected");
		cellValueType = CellValueType.BOOLEAN;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, HomologousFramePositionPair::significantProperty, getBooleanCallback(), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<HomologousFramePositionPair, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<HomologousFramePositionPair, Boolean> param) {
				final TableCellChangeListener<Boolean> significantChangeListener = new TableCellChangeListener<Boolean>(columnIndexSignificant, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isSignificant());
				booleanProp.addListener(significantChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		this.tables.put(this.transformationType, table);
		this.table = table;
	}

	@Override
	void setValue(HomologousFramePositionPair framePositionPair, int columnIndex, Object oldValue, Object newValue) {
		boolean valid = (oldValue == null || oldValue.toString().trim().isEmpty()) && (newValue == null || newValue.toString().trim().isEmpty());
		switch (columnIndex) {
		case 0:
			framePositionPair.setEnable(newValue != null && newValue instanceof Boolean && (Boolean)newValue);
			valid = true;
			break;
		case 1:
			if (newValue != null && !newValue.toString().trim().isEmpty()) {
				framePositionPair.setName(newValue.toString().trim());
				valid = true;
			}
			else
				framePositionPair.setName(oldValue == null ? null : oldValue.toString().trim());
			break;
		case 2:
			if (newValue != null && newValue instanceof Double) {
				framePositionPair.getSourceSystemPosition().setX0((Double)newValue);	
				valid = true;
			}
			else
				framePositionPair.getSourceSystemPosition().setX0(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 3:
			if (newValue != null && newValue instanceof Double) {
				framePositionPair.getSourceSystemPosition().setY0((Double)newValue);	
				valid = true;
			}
			else
				framePositionPair.getSourceSystemPosition().setY0(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 4:
			if (newValue != null && newValue instanceof Double) {
				framePositionPair.getSourceSystemPosition().setZ0((Double)newValue);	
				valid = true;
			}
			else
				framePositionPair.getSourceSystemPosition().setZ0(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 5:
			if (newValue != null && newValue instanceof Double) {
				framePositionPair.getTargetSystemPosition().setX0((Double)newValue);	
				valid = true;
			}
			else
				framePositionPair.getTargetSystemPosition().setX0(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 6:
			if (newValue != null && newValue instanceof Double) {
				framePositionPair.getTargetSystemPosition().setY0((Double)newValue);	
				valid = true;
			}
			else
				framePositionPair.getTargetSystemPosition().setY0(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 7:
			if (newValue != null && newValue instanceof Double) {
				framePositionPair.getTargetSystemPosition().setZ0((Double)newValue);	
				valid = true;
			}
			else
				framePositionPair.getTargetSystemPosition().setZ0(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
			
		default:
			System.err.println(this.getClass().getSimpleName() + " : Editable column exceed " + columnIndex);
			valid = false;
			break;
		}
		
		if (!valid) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					table.refresh();
					table.requestFocus();
					table.getSelectionModel().clearSelection();
					table.getSelectionModel().select(framePositionPair);
				}
			});
		}
	}
	
	private void setPositionsToTransformation(Transformation transformation) {
		for (HomologousFramePositionPair positionPair : this.table.getItems())
			positionPair.reset();
		if (transformation != null) {
			transformation.getTransformationEquations().getHomologousFramePositionPairs().setAll(this.table.getItems());
		}
	}
	
	@Override
	public void transformationChanged(TransformationEvent evt) {
		if (evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_ADDED)
			this.setPositionsToTransformation(evt.getSource());
		else if (evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_REMOVED)
			this.setPositionsToTransformation(null);
	}

	private static Callback<TableColumn<HomologousFramePositionPair, Boolean>, TableCell<HomologousFramePositionPair, Boolean>> getMatrixCallback() {
		return new Callback<TableColumn<HomologousFramePositionPair, Boolean>, TableCell<HomologousFramePositionPair, Boolean>>() {
			@Override
			public TableCell<HomologousFramePositionPair, Boolean> call(TableColumn<HomologousFramePositionPair, Boolean> cell) {
				final TableCell<HomologousFramePositionPair, Boolean> tableCell = new TableCell<HomologousFramePositionPair, Boolean>() {

					final Button button = getDispersionButton();
										
					@Override
					public void updateItem(Boolean item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
							setText(null);
						} else {
							this.button.getTooltip().setText(String.format(
									Locale.ENGLISH, 
									i18n.getString("UIHomologousFramePositionPairTableBuilder.dispersion.button.tooltip", "Show dispersion of point %s"),
									getTableRow().getItem() == null || getTableRow().getItem().getName() == null ? "" : getTableRow().getItem().getName()));

							this.button.setOnMousePressed(new EventHandler<MouseEvent>() {
								@Override
								public void handle(MouseEvent event) {
									getTableView().getSelectionModel().clearAndSelect(getTableRow().getIndex());
								}
							});
							this.button.setOnAction(new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent event) {
									TableViewSelectionModel<HomologousFramePositionPair> tableSelectionModel = getTableView().getSelectionModel();
									//HomologousFramePositionPair positionPair = getTableRow().getItem();
									tableSelectionModel.clearAndSelect(getTableRow().getIndex());
									MatrixDialog.showAndWait(tableSelectionModel);
								}
								
							});
							this.setGraphic(this.button);
							this.setText(null);
						}
					}
				};

				tableCell.setAlignment(Pos.CENTER);
				return tableCell;
			}
		};
	}
	
	private static Button getDispersionButton() {
		Button button = UiUtil.createButton(i18n.getString("UIHomologousFramePositionPairTableBuilder.dispersion.button.label", "Dispersion"),"");
		button.setMaxWidth(Control.USE_PREF_SIZE);
		return button;
	}
}
