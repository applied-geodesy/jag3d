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

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.jag3d.ui.table.row.AveragedObservationRow;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class UIAverageObservationTableBuilder extends UITableBuilder<AveragedObservationRow> {
	private enum DisplayFormatType {
		NORMAL, RESIDUAL
	}
	private static UIAverageObservationTableBuilder tableBuilder = new UIAverageObservationTableBuilder();
	private boolean isInitialize = false;
	private UIAverageObservationTableBuilder() {
		super();
	}

	public static UIAverageObservationTableBuilder getInstance() {
		tableBuilder.init();
		return tableBuilder;
	}

	private void init() {
		if (this.isInitialize)
			return;

		TableColumn<AveragedObservationRow, String> stringColumn   = null;
		TableColumn<AveragedObservationRow, ObservationType> observationTypeColumn = null;
		TableColumn<AveragedObservationRow, Double> doubleColumn   = null; 

		TableView<AveragedObservationRow> table = this.createTable();

		// Observation type
		int columnIndex = table.getColumns().size(); 
		String labelText   = i18n.getString("UIAverageObservationTableBuilder.tableheader.type.label", "Observation");
		String tooltipText = i18n.getString("UIAverageObservationTableBuilder.tableheader.type.tooltip", "Type of the observation");
		ColumnTooltipHeader header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		observationTypeColumn = this.<ObservationType>getColumn(header, AveragedObservationRow::observationTypeProperty, getObservationTypeCallback(), ColumnType.VISIBLE, columnIndex, false); 
		table.getColumns().add(observationTypeColumn);

		// Station-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAverageObservationTableBuilder.tableheader.station.name.label", "Station-Id");
		tooltipText = i18n.getString("UIAverageObservationTableBuilder.tableheader.station.name.tooltip", "Id of station");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, AveragedObservationRow::startPointNameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true); 
		table.getColumns().add(stringColumn);

		// Target-ID
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAverageObservationTableBuilder.tableheader.target.name.label", "Target-Id");
		tooltipText = i18n.getString("UIAverageObservationTableBuilder.tableheader.target.name.tooltip", "Id of target point");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		stringColumn = this.<String>getColumn(header, AveragedObservationRow::endPointNameProperty, getStringCallback(), ColumnType.VISIBLE, columnIndex, true);
		table.getColumns().add(stringColumn);

		// Parameter value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAverageObservationTableBuilder.tableheader.value.label", "Value");
		tooltipText = i18n.getString("UIAverageObservationTableBuilder.tableheader.value.tooltip", "A-priori observation");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, AveragedObservationRow::valueProperty, getDoubleValueWithUnitCallback(DisplayFormatType.NORMAL), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);

		// Nabla
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAverageObservationTableBuilder.tableheader.grosserror.label", "\u2207");
		tooltipText = i18n.getString("UIAverageObservationTableBuilder.tableheader.grosserror.tooltip", "Deviation w.r.t. median");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, AveragedObservationRow::grossErrorProperty, getDoubleValueWithUnitCallback(DisplayFormatType.RESIDUAL), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		this.table = table;
		this.isInitialize = true;
	}

	// https://stackoverflow.com/questions/27281370/javafx-tableview-format-one-cell-based-on-the-value-of-another-in-the-row
	private static Callback<TableColumn<AveragedObservationRow, ObservationType>, TableCell<AveragedObservationRow, ObservationType>> getObservationTypeCallback() {
		return new Callback<TableColumn<AveragedObservationRow, ObservationType>, TableCell<AveragedObservationRow, ObservationType>>() {
			@Override
			public TableCell<AveragedObservationRow, ObservationType> call(TableColumn<AveragedObservationRow, ObservationType> cell) {
				TableCell<AveragedObservationRow, ObservationType> tableCell = new TableCell<AveragedObservationRow, ObservationType>() {
					@Override
					protected void updateItem(ObservationType value, boolean empty) {
						int currentIndex = indexProperty().getValue();
						if (!empty && value != null && currentIndex >= 0 && currentIndex < cell.getTableView().getItems().size()) {
							AveragedObservationRow row = cell.getTableView().getItems().get(currentIndex);
							if (row != null && row.getObservationType() != null) {
								String component = null;
								if (row.getComponentType() != null) {
									switch (row.getComponentType()) {
									case X:
										component = i18n.getString("UIAverageObservationTableBuilder.gnss.x.label", "x");
										break;
									case Y:
										component = i18n.getString("UIAverageObservationTableBuilder.gnss.y.label", "y");
										break;
									case Z:
										component = i18n.getString("UIAverageObservationTableBuilder.gnss.z.label", "z");
										break;											
									}
								}

								switch(row.getObservationType()) {
								case LEVELING:
									this.setText(i18n.getString("UIAverageObservationTableBuilder.leveling.label", "Leveling"));
									break;
								case DIRECTION:
									this.setText(i18n.getString("UIAverageObservationTableBuilder.direction.label", "Direction"));
									break;
								case HORIZONTAL_DISTANCE:
									this.setText(i18n.getString("UIAverageObservationTableBuilder.horizontal_distance.label", "Horizontal distance"));
									break;
								case SLOPE_DISTANCE:
									this.setText(i18n.getString("UIAverageObservationTableBuilder.slope_distance.label", "Slope distance"));
									break;
								case ZENITH_ANGLE:
									this.setText(i18n.getString("UIAverageObservationTableBuilder.zenith_angle.label", "Zenith angle"));
									break;
								case GNSS1D:
									this.setText(i18n.getString("UIAverageObservationTableBuilder.gnss.1d.label", "GNSS baseline 1D") + (component != null ? " " + component : ""));
									break;
								case GNSS2D:
									this.setText(i18n.getString("UIAverageObservationTableBuilder.gnss.2d.label", "GNSS baseline 2D") + (component != null ? " " + component : ""));
									break;
								case GNSS3D:
									this.setText(i18n.getString("UIAverageObservationTableBuilder.gnss.3d.label", "GNSS baseline 3D") + (component != null ? " " + component : ""));
									break;
								}
							}
							else 
								setText(null);
						}
						else
							setText(value == null ? null : value.toString());
					}
				};
				tableCell.setAlignment(Pos.CENTER_LEFT);
				return tableCell;
			}
		};
	}

	// https://stackoverflow.com/questions/27281370/javafx-tableview-format-one-cell-based-on-the-value-of-another-in-the-row
	private static Callback<TableColumn<AveragedObservationRow, Double>, TableCell<AveragedObservationRow, Double>> getDoubleValueWithUnitCallback(DisplayFormatType displayFormatType) {
		return new Callback<TableColumn<AveragedObservationRow, Double>, TableCell<AveragedObservationRow, Double>>() {
			@Override
			public TableCell<AveragedObservationRow, Double> call(TableColumn<AveragedObservationRow, Double> cell) {
				TableCell<AveragedObservationRow, Double> tableCell = new TableCell<AveragedObservationRow, Double>() {
					@Override
					protected void updateItem(Double value, boolean empty) {
						int currentIndex = indexProperty().getValue();
						if (!empty && value != null && currentIndex >= 0 && currentIndex < cell.getTableView().getItems().size()) {
							AveragedObservationRow row = cell.getTableView().getItems().get(currentIndex);
							if (row != null && row.getObservationType() != null) {
								switch(row.getObservationType()) {
								case DIRECTION:
								case ZENITH_ANGLE:
									if (displayFormatType == DisplayFormatType.NORMAL)
										this.setText(options.toAngleFormat(value.doubleValue(), true));
									else if (displayFormatType == DisplayFormatType.RESIDUAL)
										this.setText(options.toAngleResidualFormat(value.doubleValue(), true));
									break;
								default:
									if (displayFormatType == DisplayFormatType.NORMAL)
										this.setText(options.toLengthFormat(value.doubleValue(), true));
									else if (displayFormatType == DisplayFormatType.RESIDUAL)
										this.setText(options.toLengthResidualFormat(value.doubleValue(), true));
									break;
								}
							}
							else 
								setText(null);
						}
						else
							setText(value == null ? null : value.toString());
					}
				};
				tableCell.setAlignment(Pos.CENTER_RIGHT);
				return tableCell;
			}
		};
	}

	@Override
	public AveragedObservationRow getEmptyRow() {
		return new AveragedObservationRow();
	}

	@Override
	void setValue(AveragedObservationRow row, int columnIndex, Object oldValue, Object newValue) {}
}
