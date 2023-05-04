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

package org.applied_geodesy.juniform.ui.table;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.FeatureChangeListener;
import org.applied_geodesy.adjustment.geometry.FeatureEvent;
import org.applied_geodesy.adjustment.geometry.FeatureType;
import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.FeatureEvent.FeatureEventType;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.juniform.ui.dialog.MatrixDialog;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import no.uib.cipr.matrix.Matrix;

public class UIPointTableBuilder extends UIEditableTableBuilder<FeaturePoint> implements FeatureChangeListener {	
	private FeatureType featureType = FeatureType.CURVE;
	private Map<FeatureType, TableView<FeaturePoint>> tables = new HashMap<FeatureType, TableView<FeaturePoint>>(2);

	private static UIPointTableBuilder tableBuilder = new UIPointTableBuilder();
	private UIPointTableBuilder() {
		super();
	}

	public static UIPointTableBuilder getInstance() {
		return tableBuilder;
	}
	
	public void setFeatureType(FeatureType featureType) {
		if (this.featureType != featureType) {
			this.getTableModel(this.table).clear();
			this.table.refresh();
			this.featureType = featureType;
		}
	}

	@Override
	public TableView<FeaturePoint> getTable() {
		this.init();
		return this.table;
	}

	private void init() {
		if (this.tables.containsKey(this.featureType)) {
			this.table = this.tables.get(this.featureType);
			return;
		}
		TableColumn<FeaturePoint, Boolean> booleanColumn = null;
		TableColumn<FeaturePoint, String> stringColumn   = null;
		TableColumn<FeaturePoint, Double> doubleColumn   = null;
		TableColumn<FeaturePoint, Matrix> matrixColumn   = null;

		TableView<FeaturePoint> table = this.createTable();
		///////////////// A-PRIORI VALUES /////////////////////////////
		// Enable/Disable
		int columnIndex = table.getColumns().size(); 
		final int columnIndexEnable = columnIndex;
		String labelText   = i18n.getString("UIPointTableBuilder.tableheader.enable.label", "Enable");
		String tooltipText = i18n.getString("UIPointTableBuilder.tableheader.enable.tooltip", "State of the point");
		CellValueType cellValueType = CellValueType.BOOLEAN;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, FeaturePoint::enableProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, true);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<FeaturePoint, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<FeaturePoint, Boolean> param) {
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
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, FeaturePoint::nameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true); 
		stringColumn.setComparator(new NaturalOrderTableColumnComparator<String>(stringColumn));
		table.getColumns().add(stringColumn);

		// A-priori Components
		// X0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.x0.label", "x0");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.x0.tooltip", "A-priori x-component of the point");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::x0Property, getDoubleCallback(cellValueType), ColumnType.APRIORI_POINT, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// Y0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.y0.label", "y0");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.y0.tooltip", "A-priori y-component of the point");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::y0Property, getDoubleCallback(cellValueType), ColumnType.APRIORI_POINT, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// Z0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.z0.label", "z0");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.z0.tooltip", "A-priori z-component of the point");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::z0Property, getDoubleCallback(cellValueType), this.featureType == FeatureType.SURFACE ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true);
		table.getColumns().add(doubleColumn);
		
		
		// Covariance
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.covariance.label", "Uncertainties");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.covariance.tooltip", "A-priori variance-covariance matrix");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		matrixColumn = this.<Matrix>getColumn(header, FeaturePoint::dispersionAprioriProperty, getMatrixCallback(), ColumnType.APRIORI_POINT, columnIndex, true);
		table.getColumns().add(matrixColumn);


		///////////////// A-POSTERIORI VALUES /////////////////////////////
		// A-posteriori Components

		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.x.label", "x");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.x.tooltip", "A-posteriori x-component of the point");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::xProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.y.label", "y");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.y.tooltip", "A-posteriori y-component of the point");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::yProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.z.label", "z");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.z.tooltip", "A-posteriori z-component of the point");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::zProperty, getDoubleCallback(cellValueType), this.featureType == FeatureType.SURFACE ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		// A-posteriori Uncertainties
		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.uncertainty.x.label", "\u03C3x");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.uncertainty.x.tooltip", "A-posteriori uncertainty of x-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::uncertaintyXProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.uncertainty.y.label", "\u03C3y");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.uncertainty.y.tooltip", "A-posteriori uncertainty of y-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::uncertaintyYProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.uncertainty.z.label", "\u03C3z");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.uncertainty.z.tooltip", "A-posteriori uncertainty of z-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::uncertaintyZProperty, getDoubleCallback(cellValueType), this.featureType == FeatureType.SURFACE ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Redundancy
		// rx
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.redundancy.x.label", "rx");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.redundancy.x.tooltip", "Redundancy of x-component");
		cellValueType = CellValueType.PERCENTAGE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::redundancyXProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// ry
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.redundancy.y.label", "ry");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.redundancy.y.tooltip", "Redundancy of y-component");
		cellValueType = CellValueType.PERCENTAGE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::redundancyYProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// rz
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.redundancy.z.label", "rz");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.redundancy.z.tooltip", "Redundancy of z-component");
		cellValueType = CellValueType.PERCENTAGE;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::redundancyZProperty, getDoubleCallback(cellValueType), this.featureType == FeatureType.SURFACE ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// Residual 
		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.residual.x.label", "\u03B5x");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.residual.x.tooltip", "Residual of x-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::residualXProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.residual.y.label", "\u03B5y");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.residual.y.tooltip", "Residual of y-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::residualYProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.residual.z.label", "\u03B5z");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.residual.z.tooltip", "Residual of z-component");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::residualZProperty, getDoubleCallback(cellValueType), this.featureType == FeatureType.SURFACE ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Gross-Error
		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.grosserror.x.label", "\u2207x");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.grosserror.x.tooltip", "Gross-error in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::grossErrorXProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.grosserror.y.label", "\u2207y");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.grosserror.y.tooltip", "Gross-error in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::grossErrorYProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.grosserror.z.label", "\u2207z");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.grosserror.z.tooltip", "Gross-error in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::grossErrorZProperty, getDoubleCallback(cellValueType), this.featureType == FeatureType.SURFACE ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// MTB
		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.maximumtolerablebias.x.label", "\u2207x(1)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.maximumtolerablebias.x.tooltip", "Maximum tolerable bias in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::maximumTolerableBiasXProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.maximumtolerablebias.y.label", "\u2207y(1)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.maximumtolerablebias.y.tooltip", "Maximum tolerable bias in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::maximumTolerableBiasYProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.maximumtolerablebias.z.label", "\u2207z(1)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.maximumtolerablebias.z.tooltip", "Maximum tolerable bias in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::maximumTolerableBiasZProperty, getDoubleCallback(cellValueType), this.featureType == FeatureType.SURFACE ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);		

		// MDB
		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.minimaldetectablebias.x.label", "\u2207x(\u03BB)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.minimaldetectablebias.x.tooltip", "Minimal detectable bias in x");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::minimalDetectableBiasXProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.minimaldetectablebias.y.label", "\u2207y(\u03BB)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.minimaldetectablebias.y.tooltip", "Minimal detectable bias in y");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::minimalDetectableBiasYProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.minimaldetectablebias.z.label", "\u2207z(\u03BB)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.minimaldetectablebias.z.tooltip", "Minimal detectable bias in z");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::minimalDetectableBiasZProperty, getDoubleCallback(cellValueType), this.featureType == FeatureType.SURFACE ? ColumnType.APOSTERIORI_POINT : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
	
		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.pvalue.apriori.label", "log(Pprio)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.pvalue.apriori.tooltip", "A-priori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::pValueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// p-Value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.pvalue.aposteriori.label", "log(Ppost)");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.pvalue.aposteriori.tooltip", "A-posteriori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::pValueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Tprio
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.teststatistic.apriori.label", "Tprio");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.teststatistic.apriori.tooltip", "A-priori test statistic");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::testStatisticAprioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Tpost
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.teststatistic.aposteriori.label", "Tpost");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.teststatistic.aposteriori.tooltip", "A-posteriori test statistic");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, FeaturePoint::testStatisticAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexSignificant = columnIndex;
		labelText   = i18n.getString("UIPointTableBuilder.tableheader.testdecision.label", "Significant");
		tooltipText = i18n.getString("UIPointTableBuilder.tableheader.testdecision.tooltip", "Checked, if null-hypothesis is rejected");
		cellValueType = CellValueType.BOOLEAN;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, FeaturePoint::significantProperty, getBooleanCallback(), ColumnType.APOSTERIORI_POINT, columnIndex, false);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<FeaturePoint, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<FeaturePoint, Boolean> param) {
				final TableCellChangeListener<Boolean> significantChangeListener = new TableCellChangeListener<Boolean>(columnIndexSignificant, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isSignificant());
				booleanProp.addListener(significantChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		this.tables.put(this.featureType, table);
		this.table = table;
	}

	@Override
	void setValue(FeaturePoint featurePoint, int columnIndex, Object oldValue, Object newValue) {
		boolean valid = (oldValue == null || oldValue.toString().trim().isEmpty()) && (newValue == null || newValue.toString().trim().isEmpty());
		switch (columnIndex) {
		case 0:
			featurePoint.setEnable(newValue != null && newValue instanceof Boolean && (Boolean)newValue);
			valid = true;
			break;
		case 1:
			if (newValue != null && !newValue.toString().trim().isEmpty()) {
				featurePoint.setName(newValue.toString().trim());
				valid = true;
			}
			else
				featurePoint.setName(oldValue == null ? null : oldValue.toString().trim());
			break;
		case 2:
			if (newValue != null && newValue instanceof Double) {
				featurePoint.setX0((Double)newValue);	
				valid = true;
			}
			else
				featurePoint.setX0(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 3:
			if (newValue != null && newValue instanceof Double) {
				featurePoint.setY0((Double)newValue);	
				valid = true;
			}
			else
				featurePoint.setY0(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 5:
			if (newValue != null && newValue instanceof Double) {
				featurePoint.setZ0((Double)newValue);	
				valid = true;
			}
			else
				featurePoint.setZ0(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
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
					table.getSelectionModel().select(featurePoint);
				}
			});
		}
	}
	
	private void setPointsToFeature(Feature feature) {
		ObservableList<FeaturePoint> tableModel = this.getTableModel(this.table);
		for (FeaturePoint featurePoint : tableModel)
			featurePoint.clear();
		if (feature != null) {
			for (GeometricPrimitive geometricPrimitive : feature) 
				geometricPrimitive.getFeaturePoints().setAll(tableModel);
		}
	}
	
	@Override
	public void featureChanged(FeatureEvent evt) {
		if (evt.getEventType() == FeatureEventType.FEATURE_ADDED)
			this.setPointsToFeature(evt.getSource());
		else if (evt.getEventType() == FeatureEventType.FEATURE_REMOVED)
			this.setPointsToFeature(null);
	}

	private static Callback<TableColumn<FeaturePoint,Matrix>, TableCell<FeaturePoint,Matrix>> getMatrixCallback() {
		return new Callback<TableColumn<FeaturePoint, Matrix>, TableCell<FeaturePoint, Matrix>>() {
			@Override
			public TableCell<FeaturePoint, Matrix> call(TableColumn<FeaturePoint, Matrix> cell) {
				final TableCell<FeaturePoint, Matrix> tableCell = new TableCell<FeaturePoint, Matrix>() {

					final Button button = createButton(i18n.getString("UIPointTableBuilder.dispersion.button.label", "Dispersion"),"");
					
					@Override
					public void updateItem(Matrix item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
							setText(null);
						} else {
							this.button.getTooltip().setText(String.format(
									Locale.ENGLISH, 
									i18n.getString("UIPointTableBuilder.dispersion.button.tooltip", "Show dispersion of point %s"),
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
									FeaturePoint featurePoint = getTableRow().getItem();
									getTableView().getSelectionModel().clearAndSelect(getTableRow().getIndex());
									MatrixDialog.showAndWait(featurePoint);
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
	
	static Button createButton(String title, String tooltip) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setAlignment(Pos.CENTER);
		label.setPadding(new Insets(0,0,0,0));
		Button button = new Button();
		button.setGraphic(label);
		button.setTooltip(new Tooltip(tooltip));
//		button.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
//		button.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height
		return button;
	}
}
