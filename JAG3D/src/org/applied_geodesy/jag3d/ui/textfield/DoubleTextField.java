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

package org.applied_geodesy.jag3d.ui.textfield;

import java.text.NumberFormat;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterOptions;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;

public class DoubleTextField extends TextField implements FormatterChangedListener {
	public enum ValueSupport {
		GREATER_THAN_ZERO,
		LESS_THAN_ZERO,
		GREATER_THAN_OR_EQUAL_TO_ZERO,
		LESS_THAN_OR_EQUAL_TO_ZERO,
		NULL_VALUE_SUPPORT,
		NON_NULL_VALUE_SUPPORT;
	}
	
	private FormatterOptions options = FormatterOptions.getInstance();
	private final static int EDITOR_ADDITIONAL_DIGITS = 10;
	private NumberFormat editorNumberFormat;
	private final CellValueType type;
	private final boolean displayUnit;
	private final ValueSupport valueSupport;
	private ObjectProperty<Double> number = new SimpleObjectProperty<>();

	
	public DoubleTextField(CellValueType type) {
		this(null, type, false);
	}
	
	public DoubleTextField(CellValueType type, boolean displayUnit) {
		this(null, type, displayUnit);
	}

	public DoubleTextField(Double value, CellValueType type, boolean displayUnit) {
		this(value, type, displayUnit, ValueSupport.NULL_VALUE_SUPPORT);
	}
	
	public DoubleTextField(Double value, CellValueType type, boolean displayUnit, ValueSupport valueSupport) {
		super();		
			
		this.type = type;
		this.valueSupport = valueSupport;
		this.displayUnit  = displayUnit;

		if (this.check(value))
			this.setNumber(value);
		this.prepareEditorNumberFormat();
		this.initHandlers();
		this.setTextFormatter(this.addTextFormatter());
		
		if (this.check(value))
			this.setText(this.getRendererFormat(value));
		
		this.options.addFormatterChangedListener(this);
	}
	
	private void prepareEditorNumberFormat() {
		this.editorNumberFormat = (NumberFormat)this.options.getFormatterOptions().get(this.type).getFormatter().clone();
		this.editorNumberFormat.setMinimumFractionDigits(this.editorNumberFormat.getMaximumFractionDigits());
		this.editorNumberFormat.setMaximumFractionDigits(this.editorNumberFormat.getMaximumFractionDigits() + EDITOR_ADDITIONAL_DIGITS);
	}
	
	public boolean check(Double value) {
		switch(this.valueSupport) {
		case GREATER_THAN_OR_EQUAL_TO_ZERO:
			return value != null && value.doubleValue() >= 0;
		case GREATER_THAN_ZERO:
			return value != null && value.doubleValue() >  0;
		case LESS_THAN_OR_EQUAL_TO_ZERO:
			return value != null && value.doubleValue() <= 0;
		case LESS_THAN_ZERO:
			return value != null && value.doubleValue() <  0;
		case NON_NULL_VALUE_SUPPORT:
			return value != null;
		default: // NULL_VALUE_SUPPORT:
			return true;
		}
	}

	private TextFormatter<Double> addTextFormatter() {
		//Pattern decimalPattern = Pattern.compile("^[-|+]?\\d*?\\D*\\d{0,"+this.editorNumberFormat.getMaximumFractionDigits()+"}\\s*\\D*$");
		Pattern decimalPattern = Pattern.compile("^[+|-]?[\\d\\D]*?\\d{0,"+this.editorNumberFormat.getMaximumFractionDigits()+"}\\s*\\D*$");
		UnaryOperator<TextFormatter.Change> filter = new UnaryOperator<TextFormatter.Change>() {

			@Override
			public Change apply(TextFormatter.Change change) {
				if (!change.isContentChange())
		            return change;

				try {
					String input = change.getControlNewText().trim();
					if (input == null || input.isEmpty() || decimalPattern.matcher(input).matches())
						return change;

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		
		return new TextFormatter<Double>(filter);
	}

	private void initHandlers() {

		this.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				parseAndFormatInput();
			}
		});

		this.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue.booleanValue()) {
					parseAndFormatInput();
					setText(getRendererFormat(getNumber()));
				}
				else {
					setText(getEditorFormat(getNumber()));
				}
				
			}
		});

		numberProperty().addListener(new ChangeListener<Double>() {
			@Override
			public void changed(ObservableValue<? extends Double> obserable, Double oldValue, Double newValue) {
				setText(getEditorFormat(getNumber()));
				//setText(getRendererFormat(newValue));
			}
		});
	}
	
	private String getEditorFormat(Double value) {
		if (!this.check(value))
			return null;

		value = this.getNumber();

		switch(this.type) {
		case ANGLE:
			return this.editorNumberFormat.format(this.options.convertAngleToView(value.doubleValue()));
			
		case ANGLE_RESIDUAL:
			return this.editorNumberFormat.format(this.options.convertAngleResidualToView(value.doubleValue()));

		case ANGLE_UNCERTAINTY:
			return this.editorNumberFormat.format(this.options.convertAngleUncertaintyToView(value.doubleValue()));

		case LENGTH:
			return this.editorNumberFormat.format(this.options.convertLengthToView(value.doubleValue()));

		case LENGTH_RESIDUAL:
			return this.editorNumberFormat.format(this.options.convertLengthResidualToView(value.doubleValue()));

		case LENGTH_UNCERTAINTY:
			return this.editorNumberFormat.format(this.options.convertLengthUncertaintyToView(value.doubleValue()));
	
		case SCALE:
			return this.editorNumberFormat.format(this.options.convertScaleToView(value.doubleValue()));
			
		case SCALE_RESIDUAL:
			return this.editorNumberFormat.format(this.options.convertScaleResidualToView(value.doubleValue()));

		case SCALE_UNCERTAINTY:
			return this.editorNumberFormat.format(this.options.convertScaleUncertaintyToView(value.doubleValue()));
			
		case STATISTIC:
		case DOUBLE:
			return this.editorNumberFormat.format(value.doubleValue());

		case VECTOR:
			return this.editorNumberFormat.format(this.options.convertVectorToView(value.doubleValue()));

		case VECTOR_UNCERTAINTY:
			return this.editorNumberFormat.format(this.options.convertVectorUncertaintyToView(value.doubleValue()));

		default:
			return this.editorNumberFormat.format(value.doubleValue());
		}
	}
	
	String getRendererFormat(Double value) {
		if (!this.check(value))
			return null;
		
		value = this.getNumber();
		
		switch(this.type) {
		case ANGLE:
			return this.options.toAngleFormat(value.doubleValue(), this.displayUnit);
			
		case ANGLE_RESIDUAL:
			return this.options.toAngleResidualFormat(value.doubleValue(), this.displayUnit);

		case ANGLE_UNCERTAINTY:
			return this.options.toAngleUncertaintyFormat(value.doubleValue(), this.displayUnit);

		case LENGTH:
			return this.options.toLengthFormat(value.doubleValue(), this.displayUnit);

		case LENGTH_RESIDUAL:
			return this.options.toLengthResidualFormat(value.doubleValue(), this.displayUnit);

		case LENGTH_UNCERTAINTY:
			return this.options.toLengthUncertaintyFormat(value.doubleValue(), this.displayUnit);

		case SCALE:
			return this.options.toScaleFormat(value.doubleValue(), this.displayUnit);
			
		case SCALE_RESIDUAL:
			return this.options.toScaleResidualFormat(value.doubleValue(), this.displayUnit);

		case SCALE_UNCERTAINTY:
			return this.options.toScaleUncertaintyFormat(value.doubleValue(), this.displayUnit);

		case STATISTIC:
			return this.options.toStatisticFormat(value.doubleValue());

		case VECTOR:
			return this.options.toVectorFormat(value.doubleValue(), this.displayUnit);

		case VECTOR_UNCERTAINTY:
			return this.options.toVectorUncertaintyFormat(value.doubleValue(), this.displayUnit);

		default:
			return null;
		}
	}

	/**
	 * Tries to parse the user input to a number according to the provided
	 * NumberFormat
	 */
	private void parseAndFormatInput() {
		try {
			Double newValue = null;
			String input = this.getText();
			if (input != null && !input.trim().isEmpty()) {
				input = input.replaceAll(",", ".");
				newValue = this.options.getFormatterOptions().get(this.type).parse(input).doubleValue();
				if (newValue != null) {
					switch(this.type) {
					case ANGLE:
						newValue = this.options.convertAngleToModel(newValue.doubleValue());
						break;
					case ANGLE_RESIDUAL:
						newValue = this.options.convertAngleResidualToModel(newValue.doubleValue());
						break;
					case ANGLE_UNCERTAINTY:
						newValue = this.options.convertAngleUncertaintyToModel(newValue.doubleValue());
						break;
					case LENGTH:
						newValue = this.options.convertLengthToModel(newValue.doubleValue());
						break;
					case LENGTH_RESIDUAL:
						newValue = this.options.convertLengthResidualToModel(newValue.doubleValue());
						break;
					case LENGTH_UNCERTAINTY:
						newValue = this.options.convertLengthUncertaintyToModel(newValue.doubleValue());
						break;
					case SCALE:
						newValue = this.options.convertScaleToModel(newValue.doubleValue());
						break;
					case SCALE_RESIDUAL:
						newValue = this.options.convertScaleResidualToModel(newValue.doubleValue());
						break;
					case SCALE_UNCERTAINTY:
						newValue = this.options.convertScaleUncertaintyToModel(newValue.doubleValue());
						break;
					case STATISTIC:
						newValue = newValue.doubleValue();
						break;
					case VECTOR:
						newValue = this.options.convertVectorToModel(newValue.doubleValue());
						break;
					case VECTOR_UNCERTAINTY:
						newValue = this.options.convertVectorUncertaintyToModel(newValue.doubleValue());
						break;
					default:
						newValue = newValue.doubleValue();
						break;
					}
				}
			}
			
			this.setNumber(!this.check(newValue) ? this.getNumber() : newValue);
			this.selectAll();
			
		} catch (Exception ex) {
			this.setText(this.getRendererFormat(this.getNumber()));
		}
	}
	
	public final Double getNumber() {
		return this.number.get();
	}

	public final void setNumber(Double value) {
		this.number.set(value);
	}
	
	public final void setValue(Double value) {
		this.number.set(value);
		this.setText(this.getRendererFormat(value));
	}

	public ObjectProperty<Double> numberProperty() {
		return this.number;
	}
	
	public CellValueType getCellValueType() {
		return this.type;
	}
	
	public boolean isDisplayUnit() {
		return this.displayUnit;
	}
	
	public ValueSupport getValueSupport() {
		return this.valueSupport;
	}

	@Override
	public void formatterChanged(FormatterEvent evt) {
		this.prepareEditorNumberFormat();
		this.setText(this.getRendererFormat(this.getNumber()));
	}
}