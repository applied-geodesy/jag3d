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

package org.applied_geodesy.coordtrans.ui.dialog;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.applied_geodesy.adjustment.EstimationType;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;
import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.ui.spinner.DoubleSpinner;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.ui.textfield.DoubleTextField.ValueSupport;
import org.applied_geodesy.util.CellValueType;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

class DialogUtil {
	private DialogUtil() {}
	
	static ComboBox<EstimationType> createEstimationTypeComboBox(StringConverter<EstimationType> estimationTypeStringConverter, String tooltip) {
		ComboBox<EstimationType> typeComboBox = new ComboBox<EstimationType>();
		typeComboBox.getItems().setAll(EstimationType.values());
		typeComboBox.setConverter(estimationTypeStringConverter);
		typeComboBox.setTooltip(new Tooltip(tooltip));
		typeComboBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		typeComboBox.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height
		return typeComboBox;
	}
	
	static ComboBox<UnknownParameter> createUnknownParameterComboBox(Callback<ListView<UnknownParameter>, ListCell<UnknownParameter>> unknownParameterCellFactory, String tooltip) {
		ComboBox<UnknownParameter> comboBox = new ComboBox<UnknownParameter>();
		comboBox.setCellFactory(unknownParameterCellFactory);
		comboBox.setButtonCell(unknownParameterCellFactory.call(null));

		comboBox.setTooltip(new Tooltip(tooltip));
		comboBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		comboBox.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height
		return comboBox;
	}
	
	static GridPane createGridPane() {
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setMinWidth(Control.USE_PREF_SIZE); // 300
//		gridPane.setHgap(20);
//		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.TOP_CENTER);
		gridPane.setPadding(new Insets(5,15,5,15)); // oben, recht, unten, links
		return gridPane;
	}
	
	static Button createButton(String title, String tooltip) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,0));
		Button button = new Button();
		button.setGraphic(label);
		button.setTooltip(new Tooltip(tooltip));
		button.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		button.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height
		return button;
	}
	
	static ListView<UnknownParameter> createParameterListView(Callback<ListView<UnknownParameter>, ListCell<UnknownParameter>> unknownParameterCellFactory) {
		ListView<UnknownParameter> list = new ListView<UnknownParameter>();
		list.setCellFactory(unknownParameterCellFactory);
		list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		ListView<String> placeholderList = new ListView<String>();
		placeholderList.getItems().add(new String());
		placeholderList.setDisable(true);
		list.setPlaceholder(placeholderList);
		
		return list;
	}
	
	static TextField createTextField(String tooltip, String prompt) {
		TextField textField = new TextField();
		textField.setTooltip(new Tooltip(tooltip));
		textField.setPromptText(prompt);
//		textField.setMinWidth(100);
//		textField.setPrefWidth(150);
//		textField.setMaxWidth(Double.MAX_VALUE);
		textField.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		textField.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height
		return textField;
	}
	
	static DoubleTextField createDoubleTextField(CellValueType cellValueType, double value, String tooltip) {
		DoubleTextField textField = new DoubleTextField(value, cellValueType, true, ValueSupport.NON_NULL_VALUE_SUPPORT);
		textField.setTooltip(new Tooltip(tooltip));
		textField.setAlignment(Pos.CENTER_RIGHT);
		textField.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
//		textField.setPrefWidth(150);
		textField.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height
		return textField;
	}
	
	static DoubleTextField createDoubleTextField(CellValueType cellValueType, double value, boolean displayUnit, ValueSupport valueSupport, double lowerBoundary, double upperBoundary, String tooltip) {
		DoubleTextField textField = new DoubleTextField(value, cellValueType, displayUnit, valueSupport, lowerBoundary, upperBoundary);
		textField.setTooltip(new Tooltip(tooltip));
		textField.setAlignment(Pos.CENTER_RIGHT);
		textField.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		textField.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height
		return textField;
	}
	
	static ComboBox<Boolean> createBooleanComboBox(StringConverter<Boolean> booleanTypeStringConverter, String tooltip) {
		ComboBox<Boolean> typeComboBox = new ComboBox<Boolean>();
		typeComboBox.getItems().setAll(List.of(Boolean.TRUE, Boolean.FALSE));
		typeComboBox.setConverter(booleanTypeStringConverter);			
		typeComboBox.setTooltip(new Tooltip(tooltip));
		typeComboBox.getSelectionModel().select(Boolean.TRUE);
		typeComboBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		typeComboBox.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height
		
		return typeComboBox;
	}
	
	static CheckBox createCheckBox(String title, String tooltip) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,3));
		CheckBox checkBox = new CheckBox();
		checkBox.setGraphic(label);
		checkBox.setTooltip(new Tooltip(tooltip));
		checkBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		checkBox.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE);
		return checkBox;
	}
	
	static RadioButton createRadioButton(String title, String tooltip, ToggleGroup toggleGroup) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,3));
		RadioButton radioButton = new RadioButton();
		radioButton.setGraphic(label);
		radioButton.setTooltip(new Tooltip(tooltip));
		radioButton.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		radioButton.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE);
		if (toggleGroup != null)
			radioButton.setToggleGroup(toggleGroup);
		return radioButton;
	}
	
	static ComboBox<TestStatisticType> createTestStatisticTypeComboBox(StringConverter<TestStatisticType> testStatisticTypeStringConverter, String tooltip) {
		ComboBox<TestStatisticType> typeComboBox = new ComboBox<TestStatisticType>();
		typeComboBox.getItems().setAll(TestStatisticType.values());
		typeComboBox.setConverter(testStatisticTypeStringConverter);
		typeComboBox.setTooltip(new Tooltip(tooltip));
		typeComboBox.getSelectionModel().select(TestStatisticType.NONE);
//		typeComboBox.setMinWidth(150);
//		typeComboBox.setPrefWidth(200);
		typeComboBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		typeComboBox.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height
		typeComboBox.setMaxWidth(Double.MAX_VALUE);
		return typeComboBox;
	}
	
	static DoubleSpinner createDoubleSpinner(CellValueType cellValueType, double min, double max, double amountToStepBy, String tooltip) {
		DoubleSpinner doubleSpinner = new DoubleSpinner(cellValueType, min, max, amountToStepBy);
		doubleSpinner.setMinWidth(75);
		doubleSpinner.setPrefWidth(100);
		doubleSpinner.setMaxWidth(Double.MAX_VALUE);
		doubleSpinner.setTooltip(new Tooltip(tooltip));
		return doubleSpinner;
	}
		
	static Spinner<Integer> createIntegerSpinner(int min, int max, int amountToStepBy, String tooltip) {
		NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
		numberFormat.setMaximumFractionDigits(0);
		numberFormat.setMinimumFractionDigits(0);
		numberFormat.setGroupingUsed(false);
		
		StringConverter<Integer> converter = new StringConverter<Integer>() {
		    @Override
		    public Integer fromString(String s) {
		    	if (s == null || s.trim().isEmpty())
		    		return null;
		    	else {
		    		try {
		    			return numberFormat.parse(s).intValue();
		    		}
		    		catch (Exception nfe) {
						nfe.printStackTrace();
					}
		    	}
		        return null;
		    }

		    @Override
		    public String toString(Integer d) {
		        return d == null ? "" : numberFormat.format(d);
		    }
		};
		
		SpinnerValueFactory.IntegerSpinnerValueFactory integerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max);
		Spinner<Integer> integerSpinner = new Spinner<Integer>();
		integerSpinner.setEditable(true);
		integerSpinner.setValueFactory(integerFactory);
		//integerSpinner.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);
		
		integerFactory.setConverter(converter);
		integerFactory.setAmountToStepBy(amountToStepBy);
		
		TextFormatter<Integer> formatter = new TextFormatter<Integer>(integerFactory.getConverter(), integerFactory.getValue());
		integerSpinner.getEditor().setTextFormatter(formatter);
		integerSpinner.getEditor().setAlignment(Pos.BOTTOM_RIGHT);
		integerFactory.valueProperty().bindBidirectional(formatter.valueProperty());

		integerSpinner.setMinWidth(75);
		integerSpinner.setPrefWidth(100);
		integerSpinner.setMaxWidth(Double.MAX_VALUE);
		integerSpinner.setTooltip(new Tooltip(tooltip));
		
		integerFactory.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				if (newValue == null)
					integerFactory.setValue(oldValue);
			}
		});
		
		return integerSpinner;
	}
	
	static TitledPane createTitledPane(String title, String tooltip, Node content) {
		Label label = new Label(title);
		label.setTooltip(new Tooltip(tooltip));
		TitledPane titledPane = new TitledPane();
		titledPane.setGraphic(label);
		titledPane.setCollapsible(true);
		titledPane.setAnimated(true);
		titledPane.setContent(content);
		titledPane.setPadding(new Insets(0, 10, 5, 10)); // oben, links, unten, rechts
//		titledPane.setText(title);
//		titledPane.setTooltip(new Tooltip(tooltip));
		return titledPane;
	}
}
