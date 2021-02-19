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

package org.applied_geodesy.ui.spinner;

import java.text.NumberFormat;

import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterOptions;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

public class DoubleSpinner extends Spinner<Double> implements FormatterChangedListener {
	private FormatterOptions options = FormatterOptions.getInstance();
	private final CellValueType cellValueType;
	private final double min, max, amountToStepBy;
	private Double number; 
	private NumberFormat editorNumberFormat;
	private final static int EDITOR_ADDITIONAL_DIGITS = 10;
	private SpinnerValueFactory.DoubleSpinnerValueFactory doubleFactory = (SpinnerValueFactory.DoubleSpinnerValueFactory)this.getValueFactory();
	
	public DoubleSpinner(CellValueType cellValueType, double min, double max, double amountToStepBy) {
		this(cellValueType, min, max, min, amountToStepBy);
	}
	
	public DoubleSpinner(CellValueType cellValueType, double min, double max, double initialValue, double amountToStepBy) {
		this.options.addFormatterChangedListener(this);
		this.min = min;
		this.max = max;
		this.number = initialValue;
		this.amountToStepBy = amountToStepBy;
		this.cellValueType = cellValueType;
		this.init();
	}
	
	private void prepareEditorNumberFormat() {
		this.editorNumberFormat = (NumberFormat)this.options.getFormatterOptions().get(this.cellValueType).getFormatter().clone();
		this.editorNumberFormat.setMinimumFractionDigits(this.editorNumberFormat.getMaximumFractionDigits());
		this.editorNumberFormat.setMaximumFractionDigits(this.editorNumberFormat.getMaximumFractionDigits() + EDITOR_ADDITIONAL_DIGITS);
	}
	
	private TextFormatter<Double> createTextFormatter() {
		StringConverter<Double> converter = new StringConverter<Double>() {
			@Override
			public Double fromString(String s) {
				if (s == null || s.trim().isEmpty())
					return null;
				else {
					try {
						return editorNumberFormat.parse(s.replaceAll(",", ".")).doubleValue();
					}catch (Exception nfe) {
						nfe.printStackTrace();
					}
				}
				return null;
			}

			@Override
			public String toString(Double d) {
				return d == null ? "" : editorNumberFormat.format(d);
			}
		};
		return new TextFormatter<Double>(converter, this.doubleFactory.getValue());
	}
		
	private void init() {
		this.initDoubleValueFactory();
		this.prepareEditorNumberFormat();

		TextFormatter<Double> formatter = this.createTextFormatter();
		this.doubleFactory.setConverter(formatter.getValueConverter());
		
		this.setEditable(true);
		this.setValueFactory(this.doubleFactory);

		this.getEditor().setTextFormatter(formatter);
		this.getEditor().setAlignment(Pos.BOTTOM_RIGHT);

//		this.doubleFactory.valueProperty().bindBidirectional(formatter.valueProperty());
		this.doubleFactory.valueProperty().addListener(new ChangeListener<Double>() {
			@Override
			public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
				if (newValue == null) {
					doubleFactory.setValue(oldValue);
					return;
				}

				switch(cellValueType) {
				case ANGLE:
					number = options.convertAngleToModel(newValue);
					break;
				case ANGLE_RESIDUAL:
					number = options.convertAngleResidualToModel(newValue);
					break;
				case ANGLE_UNCERTAINTY:
					number = options.convertAngleUncertaintyToModel(newValue);
					break;
				case LENGTH:
					number = options.convertLengthToModel(newValue);
					break;
				case LENGTH_RESIDUAL:
					number = options.convertLengthResidualToModel(newValue);
					break;
				case LENGTH_UNCERTAINTY:
					number = options.convertLengthUncertaintyToModel(newValue);
					break;
				case SCALE:
					number = options.convertScaleToModel(newValue);
					break;
				case SCALE_RESIDUAL:
					number = options.convertScaleResidualToModel(newValue);
					break;
				case SCALE_UNCERTAINTY:
					number = options.convertScaleUncertaintyToModel(newValue);
					break;
				case VECTOR:
					number = options.convertVectorToModel(newValue);
					break;
				case VECTOR_RESIDUAL:
					number = options.convertVectorResidualToModel(newValue);
					break;
				case VECTOR_UNCERTAINTY:
					number = options.convertVectorUncertaintyToModel(newValue);
					break;
				case TEMPERATURE:
					number = options.convertTemperatureToModel(newValue);
					break;
				case PRESSURE:
					number = options.convertPressureToModel(newValue);
					break;
				case PERCENTAGE:
					number = options.convertPercentToModel(newValue);
					break;
				case STATISTIC:
				case DOUBLE:
					number = newValue;
					break;
				default:
					number = newValue;
					break;
				
				}
				//System.out.println("CHANGED " + oldValue+"   "+newValue+"   STORED " + number+"    "+doubleFactory.getValue());
			}
		});
	}
	
	private void initDoubleValueFactory() {
		double vmin, vmax, vamountToStepBy, vvalue;

		switch(this.cellValueType) {
		case ANGLE:
			vmin   = this.options.convertAngleToView(this.min);
			vmax   = this.options.convertAngleToView(this.max);
			vvalue = this.options.convertAngleToView(this.number);
			vamountToStepBy = this.options.convertAngleToView(this.amountToStepBy);
			break;
		case ANGLE_RESIDUAL:
			vmin   = this.options.convertAngleResidualToView(this.min);
			vmax   = this.options.convertAngleResidualToView(this.max);
			vvalue = this.options.convertAngleResidualToView(this.number);
			vamountToStepBy = this.options.convertAngleResidualToView(this.amountToStepBy);
			break;
		case ANGLE_UNCERTAINTY:
			vmin   = this.options.convertAngleUncertaintyToView(this.min);
			vmax   = this.options.convertAngleUncertaintyToView(this.max);
			vvalue = this.options.convertAngleUncertaintyToView(this.number);
			vamountToStepBy = this.options.convertAngleUncertaintyToView(this.amountToStepBy);
			break;
		case LENGTH:
			vmin   = this.options.convertLengthToView(this.min);
			vmax   = this.options.convertLengthToView(this.max);
			vvalue = this.options.convertLengthToView(this.number);
			vamountToStepBy = this.options.convertLengthToView(this.amountToStepBy);
			break;
		case LENGTH_RESIDUAL:
			vmin   = this.options.convertLengthResidualToView(this.min);
			vmax   = this.options.convertLengthResidualToView(this.max);
			vvalue = this.options.convertLengthResidualToView(this.number);
			vamountToStepBy = this.options.convertLengthResidualToView(this.amountToStepBy);
			break;
		case LENGTH_UNCERTAINTY:
			vmin   = this.options.convertLengthUncertaintyToView(this.min);
			vmax   = this.options.convertLengthUncertaintyToView(this.max);
			vvalue = this.options.convertLengthUncertaintyToView(this.number);
			vamountToStepBy = this.options.convertLengthUncertaintyToView(this.amountToStepBy);
			break;
		case SCALE:
			vmin   = this.options.convertScaleToView(this.min);
			vmax   = this.options.convertScaleToView(this.max);
			vvalue = this.options.convertScaleToView(this.number);
			vamountToStepBy = this.options.convertScaleToView(this.amountToStepBy);
			break;
		case SCALE_RESIDUAL:
			vmin   = this.options.convertScaleResidualToView(this.min);
			vmax   = this.options.convertScaleResidualToView(this.max);
			vvalue = this.options.convertScaleResidualToView(this.number);
			vamountToStepBy = this.options.convertScaleResidualToView(this.amountToStepBy);
			break;
		case SCALE_UNCERTAINTY:
			vmin   = this.options.convertScaleUncertaintyToView(this.min);
			vmax   = this.options.convertScaleUncertaintyToView(this.max);
			vvalue = this.options.convertScaleUncertaintyToView(this.number);
			vamountToStepBy = this.options.convertScaleUncertaintyToView(this.amountToStepBy);
			break;
		case VECTOR:
			vmin   = this.options.convertVectorToView(this.min);
			vmax   = this.options.convertVectorToView(this.max);
			vvalue = this.options.convertVectorToView(this.number);
			vamountToStepBy = this.options.convertVectorToView(this.amountToStepBy);
			break;
		case VECTOR_RESIDUAL:
			vmin   = this.options.convertVectorResidualToView(this.min);
			vmax   = this.options.convertVectorResidualToView(this.max);
			vvalue = this.options.convertVectorResidualToView(this.number);
			vamountToStepBy = this.options.convertVectorResidualToView(this.amountToStepBy);
			break;
		case VECTOR_UNCERTAINTY:
			vmin   = this.options.convertVectorUncertaintyToView(this.min);
			vmax   = this.options.convertVectorUncertaintyToView(this.max);
			vvalue = this.options.convertVectorUncertaintyToView(this.number);
			vamountToStepBy = this.options.convertVectorUncertaintyToView(this.amountToStepBy);
			break;
		case TEMPERATURE:
			vmin   = this.options.convertTemperatureToView(this.min);
			vmax   = this.options.convertTemperatureToView(this.max);
			vvalue = this.options.convertTemperatureToView(this.number);
			vamountToStepBy = this.options.convertTemperatureToView(this.amountToStepBy);
			break;
		case PRESSURE:
			vmin   = this.options.convertPressureToView(this.min);
			vmax   = this.options.convertPressureToView(this.max);
			vvalue = this.options.convertPressureToView(this.number);
			vamountToStepBy = this.options.convertPressureToView(this.amountToStepBy);
			break;
		case PERCENTAGE:
			vmin   = this.options.convertPercentToView(this.min);
			vmax   = this.options.convertPercentToView(this.max);
			vvalue = this.options.convertPercentToView(this.number);
			vamountToStepBy = this.options.convertPercentToView(this.amountToStepBy);
			break;
		case STATISTIC:
		case DOUBLE:
			vmin   = this.min;
			vmax   = this.max;
			vvalue = this.number;
			vamountToStepBy = this.amountToStepBy;
			break;
		default:
			vmin   = this.min;
			vmax   = this.max;
			vvalue = this.number;
			vamountToStepBy = this.amountToStepBy;
			break;
		}

		if (this.doubleFactory == null) {
			this.doubleFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
					vmin,
					vmax,
					vvalue,
					vamountToStepBy
					);
		}
		else {
			this.doubleFactory.setMin(vmin);
			this.doubleFactory.setMax(vmax);
			this.doubleFactory.setAmountToStepBy(vamountToStepBy);
			this.doubleFactory.setValue(vvalue);
		}
	}
	
	public Number getNumber() {
		return number;
	}

	@Override
	public void formatterChanged(FormatterEvent evt) {
		this.initDoubleValueFactory();
		this.prepareEditorNumberFormat();
		TextFormatter<Double> formatter = this.createTextFormatter();
		this.getEditor().setTextFormatter(formatter);
		this.doubleFactory.setConverter(formatter.getValueConverter());
	}
}
