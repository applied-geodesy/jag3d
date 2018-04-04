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

package org.applied_geodesy.jag3d.ui.spinner;

import java.text.NumberFormat;

import org.applied_geodesy.jag3d.ui.table.CellValueType;
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
	private boolean ignoreChangeEvent = false;
	private SpinnerValueFactory.DoubleSpinnerValueFactory doubleFactory = (SpinnerValueFactory.DoubleSpinnerValueFactory)this.getValueFactory();
	
	public DoubleSpinner(CellValueType cellValueType, double min, double max, double initialValue, double amountToStepBy) {
		options.addFormatterChangedListener(this);
		this.min = min;
		this.max = max;
		this.number = initialValue;
		this.amountToStepBy = amountToStepBy;
		this.cellValueType = cellValueType;
		this.init();
	}
	
	public DoubleSpinner(CellValueType cellValueType, double min, double max, double initialValue) {
		this(cellValueType, initialValue, min, max, 1);
	}
	
	public DoubleSpinner(CellValueType cellValueType, double min, double max) {
		this(cellValueType, min, min, max, 1);
	}
	
	private void init() {
		this.initDoubleValueFactory();
		
		StringConverter<Double> converter = new StringConverter<Double>() {
			NumberFormat numberFormat = options.getFormatterOptions().get(cellValueType).getFormatter();
			@Override
			public Double fromString(String s) {
				if (s == null || s.trim().isEmpty())
					return null;
				else {
					try {
						return this.numberFormat.parse(s).doubleValue();
					}catch (Exception nfe) {
						nfe.printStackTrace();
					}
				}
				return null;
			}

			@Override
			public String toString(Double d) {
				return d == null ? "" : this.numberFormat.format(d);
			}
		};

		this.doubleFactory.setConverter(converter);
		this.setEditable(true);
		this.setValueFactory(this.doubleFactory);

		TextFormatter<Double> formatter = new TextFormatter<Double>(this.doubleFactory.getConverter(), this.doubleFactory.getValue());
		this.getEditor().setTextFormatter(formatter);
		this.getEditor().setAlignment(Pos.BOTTOM_RIGHT);
		
		this.doubleFactory.valueProperty().addListener(new ChangeListener<Double>() {
			@Override
			public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
				if (ignoreChangeEvent)
					return;
				
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
				case VECTOR_UNCERTAINTY:
					number = options.convertVectorUncertaintyToModel(newValue);
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
			vmin   = options.convertAngleToView(this.min);
			vmax   = options.convertAngleToView(this.max);
			vvalue = options.convertAngleToView(this.number);
			vamountToStepBy = options.convertAngleToView(this.amountToStepBy);
			break;
		case ANGLE_RESIDUAL:
			vmin   = options.convertAngleResidualToView(this.min);
			vmax   = options.convertAngleResidualToView(this.max);
			vvalue = options.convertAngleResidualToView(this.number);
			vamountToStepBy = options.convertAngleResidualToView(this.amountToStepBy);
			break;
		case ANGLE_UNCERTAINTY:
			vmin   = options.convertAngleUncertaintyToView(this.min);
			vmax   = options.convertAngleUncertaintyToView(this.max);
			vvalue = options.convertAngleUncertaintyToView(this.number);
			vamountToStepBy = options.convertAngleUncertaintyToView(this.amountToStepBy);
			break;
		case LENGTH:
			vmin   = options.convertLengthToView(this.min);
			vmax   = options.convertLengthToView(this.max);
			vvalue = options.convertLengthToView(this.number);
			vamountToStepBy = options.convertLengthToView(this.amountToStepBy);
			break;
		case LENGTH_RESIDUAL:
			vmin   = options.convertLengthResidualToView(this.min);
			vmax   = options.convertLengthResidualToView(this.max);
			vvalue = options.convertLengthResidualToView(this.number);
			vamountToStepBy = options.convertLengthResidualToView(this.amountToStepBy);
			break;
		case LENGTH_UNCERTAINTY:
			vmin   = options.convertLengthUncertaintyToView(this.min);
			vmax   = options.convertLengthUncertaintyToView(this.max);
			vvalue = options.convertLengthUncertaintyToView(this.number);
			vamountToStepBy = options.convertLengthUncertaintyToView(this.amountToStepBy);
			break;
		case SCALE:
			vmin   = options.convertScaleToView(this.min);
			vmax   = options.convertScaleToView(this.max);
			vvalue = options.convertScaleToView(this.number);
			vamountToStepBy = options.convertScaleToView(this.amountToStepBy);
			break;
		case SCALE_RESIDUAL:
			vmin   = options.convertScaleResidualToView(this.min);
			vmax   = options.convertScaleResidualToView(this.max);
			vvalue = options.convertScaleResidualToView(this.number);
			vamountToStepBy = options.convertScaleResidualToView(this.amountToStepBy);
			break;
		case SCALE_UNCERTAINTY:
			vmin   = options.convertScaleUncertaintyToView(this.min);
			vmax   = options.convertScaleUncertaintyToView(this.max);
			vvalue = options.convertScaleUncertaintyToView(this.number);
			vamountToStepBy = options.convertScaleUncertaintyToView(this.amountToStepBy);
			break;
		case VECTOR:
			vmin   = options.convertVectorToView(this.min);
			vmax   = options.convertVectorToView(this.max);
			vvalue = options.convertVectorToView(this.number);
			vamountToStepBy = options.convertVectorToView(this.amountToStepBy);
			break;
		case VECTOR_UNCERTAINTY:
			vmin   = options.convertVectorUncertaintyToView(this.min);
			vmax   = options.convertVectorUncertaintyToView(this.max);
			vvalue = options.convertVectorUncertaintyToView(this.number);
			vamountToStepBy = options.convertVectorUncertaintyToView(this.amountToStepBy);
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
		this.ignoreChangeEvent = true;
		this.initDoubleValueFactory();
		this.ignoreChangeEvent = false;

		switch(this.cellValueType) {
		case ANGLE:
			this.getEditor().setText(options.toAngleFormat(this.number, false));
			break;
		case ANGLE_RESIDUAL:
			this.getEditor().setText(options.toAngleResidualFormat(this.number, false));
			break;
		case ANGLE_UNCERTAINTY:
			this.getEditor().setText(options.toAngleUncertaintyFormat(this.number, false));
			break;
		case LENGTH:
			this.getEditor().setText(options.toLengthFormat(this.number, false));
			break;
		case LENGTH_RESIDUAL:
			this.getEditor().setText(options.toLengthResidualFormat(this.number, false));
			break;
		case LENGTH_UNCERTAINTY:
			this.getEditor().setText(options.toLengthUncertaintyFormat(this.number, false));
			break;
		case SCALE:
			this.getEditor().setText(options.toScaleFormat(this.number, false));
			break;
		case SCALE_RESIDUAL:
			this.getEditor().setText(options.toScaleResidualFormat(this.number, false));
			break;
		case SCALE_UNCERTAINTY:
			this.getEditor().setText(options.toScaleUncertaintyFormat(this.number, false));
			break;
		case VECTOR:
			this.getEditor().setText(options.toVectorFormat(this.number, false));
			break;
		case VECTOR_UNCERTAINTY:
			this.getEditor().setText(options.toVectorUncertaintyFormat(this.number, false));
			break;
		default:
			this.getEditor().setText(String.valueOf(this.number));
			break;
		}
	}
}
