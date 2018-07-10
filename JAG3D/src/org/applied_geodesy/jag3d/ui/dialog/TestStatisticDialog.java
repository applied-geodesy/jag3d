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

package org.applied_geodesy.jag3d.ui.dialog;

import java.text.NumberFormat;
import java.util.Optional;

import org.applied_geodesy.adjustment.statistic.TestStatisticDefinition;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class TestStatisticDialog implements FormatterChangedListener {
	private I18N i18n = I18N.getInstance();
	private static TestStatisticDialog testStatisticDialog = new TestStatisticDialog();
	private FormatterOptions options = FormatterOptions.getInstance();
	private Dialog<TestStatisticDefinition> dialog = null;
	private Spinner<Double> probabilityValueSpinner, testPowerSpinner;
	private ComboBox<TestStatisticType> testStatisticTypeComboBox;
	private CheckBox familywiseErrorRateCheckBox; 
	private Window window;

	private TestStatisticDialog() {}

	public static void setOwner(Window owner) {
		testStatisticDialog.window = owner;
	}

	public static Optional<TestStatisticDefinition> showAndWait() {
		testStatisticDialog.init();
		testStatisticDialog.load();
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					testStatisticDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) testStatisticDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return testStatisticDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<TestStatisticDefinition>();
		this.dialog.setTitle(i18n.getString("TestStatisticDialog.title", "Test statistic"));
		this.dialog.setHeaderText(i18n.getString("TestStatisticDialog.header", "Test statistic properties"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		//		this.dialog.initStyle(StageStyle.UTILITY);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, TestStatisticDefinition>() {
			@Override
			public TestStatisticDefinition call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {

					double probabilityValue = probabilityValueSpinner.getValue();
					double powerOfTest      = testPowerSpinner.getValue();

					boolean familywiseErrorRate = familywiseErrorRateCheckBox.isSelected();

					TestStatisticType testStatisticType = testStatisticTypeComboBox.getSelectionModel().getSelectedItem();
					TestStatisticDefinition testStatistic = new TestStatisticDefinition(
							testStatisticType, 
							probabilityValue, 
							powerOfTest, 
							familywiseErrorRate
							);
					save(testStatistic);					
				}
				return null;
			}
		});
		// add formatter listener
		options.addFormatterChangedListener(this);
	}

	private Node createPane() {
		String labelProbabilityValue   = i18n.getString("TestStatisticDialog.probability.label", "Probability value \u03B1 [\u0025]:");
		String tooltipProbabilityValue = i18n.getString("TestStatisticDialog.probability.tooltip", "Set probability value (type I error)");

		String labelTestPower   = i18n.getString("TestStatisticDialog.testpower.label", "Power of test 1 - \u03B2 [\u0025]:");
		String tooltipTestPower = i18n.getString("TestStatisticDialog.testpower.tooltip", "Set power of test (type II error)");

		String labelFamilywiseErrorRate   = i18n.getString("TestStatisticDialog.familywiseerror.label", "Familywise error rate");
		String tooltipFamilywiseErrorRate = i18n.getString("TestStatisticDialog.familywiseerror.tooltip", "If checked, probability value \u03B1 defines familywise error rate");

		Label probabilityValueLabel = new Label(labelProbabilityValue);
		Label testPowerLabel        = new Label(labelTestPower);
		
		this.familywiseErrorRateCheckBox = this.createCheckBox(labelFamilywiseErrorRate, tooltipFamilywiseErrorRate);
		
		this.probabilityValueSpinner = this.createDoubleSpinner(0.05, 30.00, 0.05, tooltipProbabilityValue);
		this.testPowerSpinner        = this.createDoubleSpinner(50.0, 99.95, 0.05, tooltipTestPower);

		this.testStatisticTypeComboBox = this.createTestStatisticTypeComboBox();
		
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setMinWidth(300);
		gridPane.setHgap(20);
		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(5,15,5,15)); // oben, recht, unten, links
		//gridPane.setGridLinesVisible(true);
		
		probabilityValueLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		testPowerLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		probabilityValueLabel.setMaxWidth(Double.MAX_VALUE);
		testPowerLabel.setMaxWidth(Double.MAX_VALUE);
		
		GridPane.setHgrow(probabilityValueLabel, Priority.NEVER);
		GridPane.setHgrow(testPowerLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.testStatisticTypeComboBox, Priority.ALWAYS);
		GridPane.setHgrow(this.familywiseErrorRateCheckBox, Priority.ALWAYS);
		
		GridPane.setHgrow(this.probabilityValueSpinner, Priority.ALWAYS);
		GridPane.setHgrow(this.testPowerSpinner, Priority.ALWAYS);
		
		int row = 0;
		gridPane.add(this.testStatisticTypeComboBox,   0, ++row, 2, 1);
		gridPane.add(this.familywiseErrorRateCheckBox, 0, ++row, 2, 1);

		gridPane.add(probabilityValueLabel,            0, ++row);
		gridPane.add(this.probabilityValueSpinner,     1, row);

		gridPane.add(testPowerLabel,                   0, ++row);
		gridPane.add(this.testPowerSpinner,            1, row);

		Platform.runLater(new Runnable() {
			@Override public void run() {
				testStatisticTypeComboBox.requestFocus();
			}
		});
		
		return gridPane;
	}

	private ComboBox<TestStatisticType> createTestStatisticTypeComboBox() {
		ComboBox<TestStatisticType> typeComboBox = new ComboBox<TestStatisticType>();

		typeComboBox.getItems().setAll(TestStatisticType.values());
		typeComboBox.setConverter(new StringConverter<TestStatisticType>() {

			@Override
			public String toString(TestStatisticType type) {
				if (type == null)
					return null;
				switch(type) {
				case BAARDA_METHOD:
					return i18n.getString("TestStatisticDialog.type.baarda", "Baardas B-method");
				case SIDAK:
					return i18n.getString("TestStatisticDialog.type.sidak", "\u0160id\u00E1k correction");
				case NONE:
					return i18n.getString("TestStatisticDialog.type.none", "None");	
				}
				return "";
			}

			@Override
			public TestStatisticType fromString(String string) {
				return TestStatisticType.valueOf(string);
			}
		});
		typeComboBox.setTooltip(new Tooltip(i18n.getString("TestStatisticDialog.type.tooltip", "Select method for type I error adaption")));
		typeComboBox.getSelectionModel().select(TestStatisticType.NONE);
		typeComboBox.setMinWidth(150);
		typeComboBox.setPrefWidth(200);
		typeComboBox.setMaxWidth(Double.MAX_VALUE);
		return typeComboBox;
	}

	private Spinner<Double> createDoubleSpinner(double min, double max, double amountToStepBy, String tooltip) {
		NumberFormat numberFormat = options.getFormatterOptions().get(CellValueType.STATISTIC).getFormatter();

		StringConverter<Double> converter = new StringConverter<Double>() {
			@Override
			public Double fromString(String s) {
				if (s == null || s.trim().isEmpty())
					return null;
				else {
					try {
						return numberFormat.parse(s.replaceAll(",", ".")).doubleValue();
					}
					catch (Exception nfe) {
						nfe.printStackTrace();
					}
				}
				return null;
			}

			@Override
			public String toString(Double d) {
				return d == null ? "" : numberFormat.format(d);
			}
		};

		SpinnerValueFactory.DoubleSpinnerValueFactory doubleFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max);
		Spinner<Double> doubleSpinner = new Spinner<Double>();
		doubleSpinner.setEditable(true);
		doubleSpinner.setValueFactory(doubleFactory);
		//doubleSpinner.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);

		doubleFactory.setConverter(converter);
		doubleFactory.setAmountToStepBy(amountToStepBy);

		TextFormatter<Double> formatter = new TextFormatter<Double>(doubleFactory.getConverter(), doubleFactory.getValue());
		doubleSpinner.getEditor().setTextFormatter(formatter);
		doubleSpinner.getEditor().setAlignment(Pos.BOTTOM_RIGHT);
		doubleFactory.valueProperty().bindBidirectional(formatter.valueProperty());

		doubleSpinner.setMinWidth(75);
		doubleSpinner.setPrefWidth(100);
		doubleSpinner.setMaxWidth(Double.MAX_VALUE);
		doubleSpinner.setTooltip(new Tooltip(tooltip));
		
		doubleFactory.valueProperty().addListener(new ChangeListener<Double>() {
			@Override
			public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
				if (newValue == null)
					doubleFactory.setValue(oldValue);
			}
		});

		return doubleSpinner;
	}

	private void save(TestStatisticDefinition testStatistic) {
		try {
			SQLManager.getInstance().save(testStatistic);
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("TestStatisticDialog.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("TestStatisticDialog.message.error.save.exception.header", "Error, could not save test statistics to database."),
							i18n.getString("TestStatisticDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}

	private void load() {
		try {
			TestStatisticDefinition testStatistic = SQLManager.getInstance().getTestStatisticDefinition();
			this.familywiseErrorRateCheckBox.setSelected(testStatistic.isFamilywiseErrorRate());
			this.testStatisticTypeComboBox.getSelectionModel().select(testStatistic.getTestStatisticType());

			SpinnerValueFactory.DoubleSpinnerValueFactory probabilityValueSpinnerFactory = (SpinnerValueFactory.DoubleSpinnerValueFactory)this.probabilityValueSpinner.getValueFactory();
			SpinnerValueFactory.DoubleSpinnerValueFactory testPowerSpinnerFactory        = (SpinnerValueFactory.DoubleSpinnerValueFactory)this.testPowerSpinner.getValueFactory();

			double probabilityValue = testStatistic.getProbabilityValue();
			double powerOfTest      = testStatistic.getPowerOfTest();

			probabilityValue = Math.max(Math.min(probabilityValue, probabilityValueSpinnerFactory.getMax()), probabilityValueSpinnerFactory.getMin());
			powerOfTest      = Math.max(Math.min(powerOfTest, testPowerSpinnerFactory.getMax()), testPowerSpinnerFactory.getMin());

			probabilityValueSpinnerFactory.setValue(probabilityValue);
			testPowerSpinnerFactory.setValue(powerOfTest);

		} 
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("TestStatisticDialog.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("TestStatisticDialog.message.error.load.exception.header", "Error, could not load test statistics from database."),
							i18n.getString("TestStatisticDialog.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
	
	@Override
	public void formatterChanged(FormatterEvent evt) {
		this.probabilityValueSpinner.getEditor().setText(options.toStatisticFormat(this.probabilityValueSpinner.getValueFactory().getValue()));
		this.testPowerSpinner.getEditor().setText(options.toStatisticFormat(this.testPowerSpinner.getValueFactory().getValue()));
	}
	
	private CheckBox createCheckBox(String title, String tooltip) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,3));
		CheckBox checkBox = new CheckBox();
		checkBox.setGraphic(label);
		checkBox.setTooltip(new Tooltip(tooltip));
		checkBox.setMinHeight(Control.USE_PREF_SIZE);
		checkBox.setMaxHeight(Double.MAX_VALUE);
		return checkBox;
	}
}
