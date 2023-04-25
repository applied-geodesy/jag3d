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
import org.applied_geodesy.adjustment.transformation.TransformationType;
import org.applied_geodesy.adjustment.transformation.TransformationEvent.TransformationEventType;
import org.applied_geodesy.adjustment.transformation.point.FramePositionPair;
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
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class UIFramePositionPairTableBuilder extends UIEditableTableBuilder<FramePositionPair> implements TransformationChangeListener {	
	private TransformationType transformationType = TransformationType.SPATIAL;
	private Map<TransformationType, TableView<FramePositionPair>> tables = new HashMap<TransformationType, TableView<FramePositionPair>>(2);

	private static UIFramePositionPairTableBuilder tableBuilder = new UIFramePositionPairTableBuilder();
	private UIFramePositionPairTableBuilder() {
		super();
	}

	public static UIFramePositionPairTableBuilder getInstance() {
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
	public TableView<FramePositionPair> getTable() {
		this.init();
		return this.table;
	}

	private void init() {
		if (this.tables.containsKey(this.transformationType)) {
			this.table = this.tables.get(this.transformationType);
			return;
		}
		TableColumn<FramePositionPair, Boolean> booleanColumn = null;
		TableColumn<FramePositionPair, String> stringColumn   = null;
		TableColumn<FramePositionPair, Double> doubleColumn   = null;
		TableColumn<FramePositionPair, Boolean> matrixColumn   = null;

		TableView<FramePositionPair> table = this.createTable();
		///////////////// A-PRIORI VALUES /////////////////////////////
		// Enable/Disable
		int columnIndex = table.getColumns().size(); 
		final int columnIndexEnable = columnIndex;
		String labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.enable.label", "Enable");
		String tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.enable.tooltip", "State of the point");
		CellValueType cellValueType = CellValueType.BOOLEAN;
		ColumnTooltipHeader header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, FramePositionPair::enableProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, true);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<FramePositionPair, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<FramePositionPair, Boolean> param) {
				final TableCellChangeListener<Boolean> enableChangeListener = new TableCellChangeListener<Boolean>(columnIndexEnable, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isEnable());
				booleanProp.addListener(enableChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		// Point-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.station.name.label", "Point-Id");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.station.name.tooltip", "Id of the point");
		cellValueType = CellValueType.STRING;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, FramePositionPair::nameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true); 
		stringColumn.setComparator(new NaturalOrderComparator<String>());
		table.getColumns().add(stringColumn);

		// SOURCE
		// A-priori Components
		// x0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.source.x0.label", "x0");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.source.x0.tooltip", "A-priori x-component of the point in source system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FramePositionPair::sourceXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// y0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.source.y0.label", "y0");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.source.y0.tooltip", "A-priori y-component of the point in source system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FramePositionPair::sourceYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true);
		table.getColumns().add(doubleColumn);

		// z0-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.source.z0.label", "z0");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.source.z0.tooltip", "A-priori z-component of the point in source system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FramePositionPair::sourceZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APRIORI_POINT : ColumnType.HIDDEN, columnIndex, true);
		table.getColumns().add(doubleColumn);
		
		// Covariance of both positions
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.covariance.label", "Uncertainties");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.covariance.tooltip", "A-priori variance-covariance matrix");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		matrixColumn = this.<Boolean>getColumn(header, FramePositionPair::containsDispersionablePositionProperty, getMatrixCallback(), ColumnType.APRIORI_POINT, columnIndex, true);
		table.getColumns().add(matrixColumn);


		///////////////// A-POSTERIORI VALUES /////////////////////////////
		// A-posteriori Components
		// TARGET
		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.target.x.label", "X");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.target.x.tooltip", "A-posteriori x-component of the point in target system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FramePositionPair::targetXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.target.y.label", "Y");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.target.y.tooltip", "A-posteriori y-component of the point in target system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FramePositionPair::targetYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.target.z.label", "Z");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.target.z.tooltip", "A-posteriori z-component of the point in target system");
		cellValueType = CellValueType.LENGTH;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FramePositionPair::targetZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		table.getColumns().add(doubleColumn);

		
		// A-posteriori Uncertainties
		// X-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.uncertainty.x.label", "\u03C3x");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.uncertainty.x.tooltip", "A-posteriori uncertainty of x-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FramePositionPair::targetUncertaintyXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Y-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.uncertainty.y.label", "\u03C3y");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.uncertainty.y.tooltip", "A-posteriori uncertainty of y-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FramePositionPair::targetUncertaintyYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// Z-Comp.
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.uncertainty.z.label", "\u03C3z");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.uncertainty.z.tooltip", "A-posteriori uncertainty of z-component");
		cellValueType = CellValueType.LENGTH_UNCERTAINTY;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FramePositionPair::targetUncertaintyZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// Residual 
		// x-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.residualgap.x.label", "\u03B4x");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.residualgap.x.tooltip", "Residual gap of x-component applied by selected interpolation approach");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FramePositionPair::targetResidualXProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// y-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.residualgap.y.label", "\u03B4y");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.residualgap.y.tooltip", "Residual gap of y-component applied by selected interpolation approach");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FramePositionPair::targetResidualYProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.HEIGHT ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// z-Comp
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIFramePositionPairTableBuilder.tableheader.residualgap.z.label", "\u03B4z");
		tooltipText = i18n.getString("UIFramePositionPairTableBuilder.tableheader.residualgap.z.tooltip", "Residual gap of z-component applied by selected interpolation approach");
		cellValueType = CellValueType.LENGTH_RESIDUAL;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, FramePositionPair::targetResidualZProperty, getDoubleCallback(cellValueType), this.transformationType != TransformationType.PLANAR ? ColumnType.APOSTERIORI_TARGET_SYSTEM : ColumnType.HIDDEN, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		this.tables.put(this.transformationType, table);
		this.table = table;
	}
	
	@Override
	void setValue(FramePositionPair framePositionPair, int columnIndex, Object oldValue, Object newValue) {
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
				framePositionPair.getSourceSystemPosition().setX((Double)newValue);	
				valid = true;
			}
			else
				framePositionPair.getSourceSystemPosition().setX(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 3:
			if (newValue != null && newValue instanceof Double) {
				framePositionPair.getSourceSystemPosition().setY((Double)newValue);	
				valid = true;
			}
			else
				framePositionPair.getSourceSystemPosition().setY(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
			break;
		case 4:
			if (newValue != null && newValue instanceof Double) {
				framePositionPair.getSourceSystemPosition().setZ((Double)newValue);	
				valid = true;
			}
			else
				framePositionPair.getSourceSystemPosition().setZ(oldValue != null && oldValue instanceof Double ? (Double)oldValue : null);
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
		for (FramePositionPair positionPair : this.table.getItems())
			positionPair.reset();

		if (transformation != null)
			transformation.getFramePositionPairs().setAll(this.table.getItems());
	}
	
	@Override
	public void transformationChanged(TransformationEvent evt) {
		if (evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_ADDED)
			this.setPositionsToTransformation(evt.getSource());
		else if (evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_REMOVED)
			this.setPositionsToTransformation(null);
	}

	private static Callback<TableColumn<FramePositionPair, Boolean>, TableCell<FramePositionPair, Boolean>> getMatrixCallback() {
		return new Callback<TableColumn<FramePositionPair, Boolean>, TableCell<FramePositionPair, Boolean>>() {
			@Override
			public TableCell<FramePositionPair, Boolean> call(TableColumn<FramePositionPair, Boolean> cell) {
				final TableCell<FramePositionPair, Boolean> tableCell = new TableCell<FramePositionPair, Boolean>() {

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
									i18n.getString("UIFramePositionPairTableBuilder.dispersion.button.tooltip", "Show dispersion of point %s"),
									getTableRow().getItem().getName()));

							this.button.setOnMousePressed(new EventHandler<MouseEvent>() {
								@Override
								public void handle(MouseEvent event) {
									getTableView().getSelectionModel().clearAndSelect(getTableRow().getIndex());
								}
							});
							this.button.setOnAction(new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent event) {
									FramePositionPair positionPair = getTableRow().getItem();
									getTableView().getSelectionModel().clearAndSelect(getTableRow().getIndex());
									MatrixDialog.showAndWait(positionPair.getName(), positionPair.getSourceSystemPosition(), null);
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
		Button button = UiUtil.createButton(i18n.getString("UIFramePositionPairTableBuilder.dispersion.button.label", "Dispersion"),"");
		button.setMaxWidth(Control.USE_PREF_SIZE);
		return button;
	}
}
