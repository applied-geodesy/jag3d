package org.applied_geodesy.jag3d.ui.table;

import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.jag3d.ui.table.row.AdditionalParameterRow;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class UIAdditionalParameterTableBuilder extends UITableBuilder<AdditionalParameterRow> {
	private enum DisplayFormatType {
		NORMAL, UNCERTAINTY, RESIDUAL
	}
	private static UIAdditionalParameterTableBuilder tableBuilder = new UIAdditionalParameterTableBuilder();
	private boolean isInitialize = false;
	private UIAdditionalParameterTableBuilder() {
		super();
	}

	public static UIAdditionalParameterTableBuilder getInstance() {
		tableBuilder.init();
		return tableBuilder;
	}

	private void init() {
		if (this.isInitialize)
			return;
		
		TableColumn<AdditionalParameterRow, ParameterType> parameterTypeColumn = null;
		TableColumn<AdditionalParameterRow, Boolean> booleanColumn = null;
		TableColumn<AdditionalParameterRow, Double> doubleColumn   = null; 

		TableView<AdditionalParameterRow> table = this.createTable();
		
		// Parameter type
		int columnIndex = table.getColumns().size(); 
		String labelText   = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.type.label", "Parameter type");
		String tooltipText = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.type.tooltip", "Type of the additional parameter");
		ColumnTooltipHeader header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		parameterTypeColumn = this.<ParameterType>getColumn(header, AdditionalParameterRow::parameterTypeProperty, getParameterTypeCallback(), ColumnType.VISIBLE, columnIndex, false); 
		table.getColumns().add(parameterTypeColumn);
		
		// Parameter value
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.value.label", "Value");
		tooltipText = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.value.tooltip", "Estimated value of additional parameter");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, AdditionalParameterRow::valueAposterioriProperty, getDoubleValueWithUnitCallback(DisplayFormatType.NORMAL), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		// Uncertainty
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.uncertainty.label", "\u03C3");
		tooltipText = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.uncertainty.tooltip", "A-posteriori uncertainty of additional parameter");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, AdditionalParameterRow::sigmaAposterioriProperty, getDoubleValueWithUnitCallback(DisplayFormatType.UNCERTAINTY), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		// Confidence
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.semiaxis.label", "a");
		tooltipText = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.semiaxis.tooltip", "Confidence interval");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, AdditionalParameterRow::confidenceProperty, getDoubleValueWithUnitCallback(DisplayFormatType.UNCERTAINTY), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		// Nabla
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.grosserror.label", "\u2207");
		tooltipText = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.grosserror.tooltip", "Gross-error of additional parameter");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, AdditionalParameterRow::grossErrorProperty, getDoubleValueWithUnitCallback(DisplayFormatType.RESIDUAL), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		// MDB
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.minimaldetectablebias.label", "\u2207(\u03B1,\u03B2)");
		tooltipText = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.minimaldetectablebias.tooltip", "Minimal detectable bias of additional parameter");
		header = new ColumnTooltipHeader(CellValueType.STRING, labelText, tooltipText);
		doubleColumn = this.<Double>getColumn(header, AdditionalParameterRow::minimalDetectableBiasProperty, getDoubleValueWithUnitCallback(DisplayFormatType.RESIDUAL), ColumnType.VISIBLE, columnIndex, false);
		table.getColumns().add(doubleColumn);
		
		// A-priori log(p)
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.pvalue.apriori.label", "log(Pprio)");
		tooltipText = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.pvalue.apriori.tooltip", "A-priori p-value in logarithmic representation");
		CellValueType cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, AdditionalParameterRow::pValueAprioriProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// A-priori log(p)
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.pvalue.aposteriori.label", "log(Ppost)");
		tooltipText = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.pvalue.aposteriori.tooltip", "A-posteriori p-value in logarithmic representation");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, AdditionalParameterRow::pValueAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// A-priori test statistic
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.teststatistic.apriori.label", "Tprio");
		tooltipText = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.teststatistic.apriori.tooltip", "A-priori test statistic");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, AdditionalParameterRow::testStatisticAprioriProperty, getDoubleCallback(cellValueType), ColumnType.VISIBLE, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);

		// A-posteriori test statistic
		columnIndex = table.getColumns().size(); 
		labelText   = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.teststatistic.aposteriori.label", "Tpost");
		tooltipText = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.teststatistic.aposteriori.tooltip", "A-posteriori test statistic");
		cellValueType = CellValueType.STATISTIC;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText, options.getFormatterOptions().get(cellValueType).getUnit());
		doubleColumn = this.<Double>getColumn(header, AdditionalParameterRow::testStatisticAposterioriProperty, getDoubleCallback(cellValueType), ColumnType.APOSTERIORI_TERRESTRIAL_OBSERVATION, columnIndex, false);
		doubleColumn.setComparator(new AbsoluteValueComparator());
		table.getColumns().add(doubleColumn);
		
		// Decision of test statistic
		columnIndex = table.getColumns().size(); 
		final int columnIndexOutlier = columnIndex;
		labelText   = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.significant.label", "Significant");
		tooltipText = i18n.getString("UIAdditionalParameterTableBuilder.tableheader.significant.tooltip", "Checked, if additional parameter is significant");
		cellValueType = CellValueType.BOOLEAN;
		header = new ColumnTooltipHeader(cellValueType, labelText, tooltipText);
		booleanColumn = this.<Boolean>getColumn(header, AdditionalParameterRow::significantProperty, getBooleanCallback(), ColumnType.VISIBLE, columnIndex, false);
		booleanColumn.setCellValueFactory(new Callback<CellDataFeatures<AdditionalParameterRow, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<AdditionalParameterRow, Boolean> param) {
				final TableCellChangeListener<Boolean> significantChangeListener = new TableCellChangeListener<Boolean>(columnIndexOutlier, param.getValue());
				BooleanProperty booleanProp = new SimpleBooleanProperty(param.getValue().isSignificant());
				booleanProp.addListener(significantChangeListener);
				return booleanProp;
			}
		});
		table.getColumns().add(booleanColumn);

		this.table = table;
		this.isInitialize = true;
	}
	
	private static Callback<TableColumn<AdditionalParameterRow, ParameterType>, TableCell<AdditionalParameterRow, ParameterType>> getParameterTypeCallback() {
        return new Callback<TableColumn<AdditionalParameterRow, ParameterType>, TableCell<AdditionalParameterRow, ParameterType>>() {
            @Override
            public TableCell<AdditionalParameterRow, ParameterType> call(TableColumn<AdditionalParameterRow, ParameterType> cell) {
            	TableCell<AdditionalParameterRow, ParameterType> tableCell = new TextFieldTableCell<AdditionalParameterRow, ParameterType>(
            			new StringConverter<ParameterType>() {

            				@Override
            				public String toString(ParameterType type) {
            					if (type == null)
            						return null;
            					
            					switch (type) {
            					case ZERO_POINT_OFFSET:
            						return i18n.getString("UIAdditionalParameterTableBuilder.type.zeropointoffset", "Zero point offset");
            					case SCALE:
            						return i18n.getString("UIAdditionalParameterTableBuilder.type.scale", "Scale");
            					case ORIENTATION:
            						return i18n.getString("UIAdditionalParameterTableBuilder.type.orientation", "Orientation");
								case REFRACTION_INDEX:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.refractionindex", "Refraction index");
									
								case ROTATION_X:
								case STRAIN_ROTATION_X:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.rotation.x", "Rotation angle rx");
								case ROTATION_Y:
								case STRAIN_ROTATION_Y:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.rotation.y", "Rotation angle ry");
								case ROTATION_Z:
								case STRAIN_ROTATION_Z:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.rotation.z", "Rotation angle rz");
									
								case STRAIN_SCALE_X:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.scale.x", "Scale mx");
								case STRAIN_SCALE_Y:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.scale.y", "Scale my");
								case STRAIN_SCALE_Z:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.scale.z", "Scale mz");
									
								case STRAIN_SHEAR_X:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.shear.x", "Shear angle sx");
								case STRAIN_SHEAR_Y:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.shear.y", "Shear angle sy");
								case STRAIN_SHEAR_Z:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.shear.z", "Shear angle sz");
									
								case STRAIN_TRANSLATION_X:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.translation.x", "Shift tx");
								case STRAIN_TRANSLATION_Y:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.translation.y", "Shift ty");
								case STRAIN_TRANSLATION_Z:
									return i18n.getString("UIAdditionalParameterTableBuilder.type.translation.z", "Shift tz");
									
								case STRAIN_S11:
									break;
								case STRAIN_S12:
									break;
								case STRAIN_S13:
									break;
								case STRAIN_S22:
									break;
								case STRAIN_S23:
									break;
								case STRAIN_S33:
									break;
									
								case STRAIN_A11:
									break;
								case STRAIN_A12:
									break;
								case STRAIN_A21:
									break;
								case STRAIN_A22:
									break;
								
								
								default:
									break;
            					
            					}
            					return null;
            				}

            				@Override
            				public ParameterType fromString(String string) {
            					return ParameterType.valueOf(string);
            				}
            			});
            	tableCell.setAlignment(Pos.CENTER_LEFT);
            	return tableCell;	
            }
            
        };
    }
	// https://stackoverflow.com/questions/27281370/javafx-tableview-format-one-cell-based-on-the-value-of-another-in-the-row
	private static Callback<TableColumn<AdditionalParameterRow, Double>, TableCell<AdditionalParameterRow, Double>> getDoubleValueWithUnitCallback(DisplayFormatType displayFormatType) {
		return new Callback<TableColumn<AdditionalParameterRow, Double>, TableCell<AdditionalParameterRow, Double>>() {
			@Override
			public TableCell<AdditionalParameterRow, Double> call(TableColumn<AdditionalParameterRow, Double> cell) {
				TableCell<AdditionalParameterRow, Double> tableCell = new TableCell<AdditionalParameterRow, Double>() {
						@Override
                        protected void updateItem(Double value, boolean empty) {
							int currentIndex = indexProperty().getValue();
							if (!empty && value != null && currentIndex >= 0 && currentIndex < cell.getTableView().getItems().size()) {
								AdditionalParameterRow paramRow = cell.getTableView().getItems().get(currentIndex);
								if (paramRow != null && paramRow.getParameterType() != null) {
									switch(paramRow.getParameterType()) {
									case ZERO_POINT_OFFSET:
									case STRAIN_TRANSLATION_X:
									case STRAIN_TRANSLATION_Y:
									case STRAIN_TRANSLATION_Z:
										
										if (displayFormatType == DisplayFormatType.NORMAL)
											this.setText(options.toLengthFormat(value.doubleValue(), true));
										else if (displayFormatType == DisplayFormatType.UNCERTAINTY)
											this.setText(options.toLengthUncertaintyFormat(value.doubleValue(), true));
										else if (displayFormatType == DisplayFormatType.RESIDUAL)
											this.setText(options.toLengthResidualFormat(value.doubleValue(), true));
										
										break;
									case SCALE:
									case STRAIN_SCALE_X:
									case STRAIN_SCALE_Y:
									case STRAIN_SCALE_Z:
										
										if (displayFormatType == DisplayFormatType.NORMAL)
											this.setText(options.toScaleFormat(value.doubleValue(), true));
										else if (displayFormatType == DisplayFormatType.UNCERTAINTY)
											this.setText(options.toScaleUncertaintyFormat(value.doubleValue(), true));
										else if (displayFormatType == DisplayFormatType.RESIDUAL)
											this.setText(options.toScaleResidualFormat(value.doubleValue(), true));

										break;
									case ORIENTATION:
									case ROTATION_X:
									case ROTATION_Y:
									case ROTATION_Z:
									case STRAIN_ROTATION_X:
									case STRAIN_ROTATION_Y:
									case STRAIN_ROTATION_Z:
									case STRAIN_SHEAR_X:
									case STRAIN_SHEAR_Y:
									case STRAIN_SHEAR_Z:
										
										if (displayFormatType == DisplayFormatType.NORMAL)
											this.setText(options.toAngleFormat(value.doubleValue(), true));
										else if (displayFormatType == DisplayFormatType.UNCERTAINTY)
											this.setText(options.toAngleUncertaintyFormat(value.doubleValue(), true));
										else if (displayFormatType == DisplayFormatType.RESIDUAL)
											this.setText(options.toAngleResidualFormat(value.doubleValue(), true));

										break;
									case REFRACTION_INDEX:
										
										this.setText(options.toTestStatisticFormat(value.doubleValue()));
										break;
										
									default:
										
										System.err.println(UIAdditionalParameterTableBuilder.class.getSimpleName() + " : Error, unknown parameter type " + paramRow.getParameterType());
										setText(null);
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
	public AdditionalParameterRow getEmptyRow() {
		return new AdditionalParameterRow();
	}
	
	@Override
	void setValue(AdditionalParameterRow row, int columnIndex, int rowIndex, Object oldValue, Object newValue) {}
}
