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

import java.util.Locale;
import java.util.Optional;

import org.applied_geodesy.adjustment.statistic.TestStatisticDefinition;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.ui.spinner.DoubleSpinner;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterEventType;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.jag3d.ui.i18n.I18N;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
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
	private DoubleSpinner probabilityValueSpinner, testPowerSpinner;
	private ComboBox<TestStatisticType> testStatisticTypeComboBox;
	private CheckBox familywiseErrorRateCheckBox; 
	private Window window;
	private Label probabilityValueLabel, testPowerLabel;

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

					double probabilityValue = probabilityValueSpinner.getNumber().doubleValue();
					double powerOfTest      = testPowerSpinner.getNumber().doubleValue();

					boolean familywiseErrorRate = familywiseErrorRateCheckBox.isSelected();

					TestStatisticType testStatisticType = testStatisticTypeComboBox.getSelectionModel().getSelectedItem();
					TestStatisticDefinition testStatistic = new TestStatisticDefinition(
							testStatisticType, 
							probabilityValue, 
							powerOfTest, 
							familywiseErrorRate
							);
					save(testStatistic);
					return testStatistic;
				}
				return null;
			}
		});
		// add formatter listener
		options.addFormatterChangedListener(this);
	}

	private Node createPane() {
		String frmPercentUnit        = this.options.getFormatterOptions().get(CellValueType.PERCENTAGE).getUnit().toFormattedAbbreviation();
		String labelProbabilityValue = String.format(Locale.ENGLISH, "%s%s:", i18n.getString("TestStatisticDialog.probability.label", "Probability value \u03B1"), frmPercentUnit.isBlank() ? "" : " " + frmPercentUnit);
		String labelTestPower        = String.format(Locale.ENGLISH, "%s%s:", i18n.getString("TestStatisticDialog.testpower.label", "Power of test 1 - \u03B2"), frmPercentUnit.isBlank() ? "" : " " + frmPercentUnit);
		
		String tooltipProbabilityValue = i18n.getString("TestStatisticDialog.probability.tooltip", "Set probability value (type I error)");
		String tooltipTestPower        = i18n.getString("TestStatisticDialog.testpower.tooltip", "Set power of test (type II error)");

		String labelFamilywiseErrorRate   = i18n.getString("TestStatisticDialog.familywiseerror.label", "Familywise error rate");
		String tooltipFamilywiseErrorRate = i18n.getString("TestStatisticDialog.familywiseerror.tooltip", "If checked, probability value \u03B1 defines familywise error rate");

		this.probabilityValueLabel = new Label(labelProbabilityValue);
		this.testPowerLabel        = new Label(labelTestPower);
		
		this.familywiseErrorRateCheckBox = this.createCheckBox(labelFamilywiseErrorRate, tooltipFamilywiseErrorRate);
		
		this.probabilityValueSpinner = this.createDoubleSpinner(0.0005, 0.30, 0.01, tooltipProbabilityValue);
		this.testPowerSpinner        = this.createDoubleSpinner(0.50, 0.9995, 0.01, tooltipTestPower);

		this.testStatisticTypeComboBox = this.createTestStatisticTypeComboBox();
		
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setMinWidth(300);
		gridPane.setHgap(20);
		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(5,15,5,15)); // oben, recht, unten, links
		//gridPane.setGridLinesVisible(true);
		
		this.probabilityValueLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.testPowerLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		this.probabilityValueLabel.setMaxWidth(Double.MAX_VALUE);
		this.testPowerLabel.setMaxWidth(Double.MAX_VALUE);
		
		GridPane.setHgrow(probabilityValueLabel, Priority.NEVER);
		GridPane.setHgrow(testPowerLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.testStatisticTypeComboBox, Priority.ALWAYS);
		GridPane.setHgrow(this.familywiseErrorRateCheckBox, Priority.ALWAYS);
		
		GridPane.setHgrow(this.probabilityValueSpinner, Priority.ALWAYS);
		GridPane.setHgrow(this.testPowerSpinner, Priority.ALWAYS);
		
		int row = 0;
		gridPane.add(this.testStatisticTypeComboBox,   0, ++row, 2, 1);
		gridPane.add(this.familywiseErrorRateCheckBox, 0, ++row, 2, 1);

		gridPane.add(this.probabilityValueLabel,       0, ++row);
		gridPane.add(this.probabilityValueSpinner,     1, row);

		gridPane.add(this.testPowerLabel,              0, ++row);
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

	private DoubleSpinner createDoubleSpinner(double min, double max, double amountToStepBy, String tooltip) {
		DoubleSpinner doubleSpinner = new DoubleSpinner(CellValueType.PERCENTAGE, min, max, amountToStepBy);
		doubleSpinner.setMinWidth(75);
		doubleSpinner.setPrefWidth(100);
		doubleSpinner.setMaxWidth(Double.MAX_VALUE);
		doubleSpinner.setTooltip(new Tooltip(tooltip));
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
			
			probabilityValue = Math.max(Math.min(this.options.convertPercentToView(probabilityValue), probabilityValueSpinnerFactory.getMax()), probabilityValueSpinnerFactory.getMin());
			powerOfTest      = Math.max(Math.min(this.options.convertPercentToView(powerOfTest), testPowerSpinnerFactory.getMax()), testPowerSpinnerFactory.getMin());
			
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
	
	@Override
	public void formatterChanged(FormatterEvent evt) {
		if (evt != null && evt.getCellType() == CellValueType.PERCENTAGE && evt.getEventType() == FormatterEventType.UNIT_CHANGED) {
			String frmPercentUnit        = this.options.getFormatterOptions().get(CellValueType.PERCENTAGE).getUnit().toFormattedAbbreviation();
			String labelProbabilityValue = String.format(Locale.ENGLISH, "%s%s:", i18n.getString("TestStatisticDialog.probability.label", "Probability value \u03B1"), frmPercentUnit.isBlank() ? "" : " " + frmPercentUnit);
			String labelTestPower        = String.format(Locale.ENGLISH, "%s%s:", i18n.getString("TestStatisticDialog.testpower.label", "Power of test 1 - \u03B2"), frmPercentUnit.isBlank() ? "" : " " + frmPercentUnit);
			
			this.probabilityValueLabel.setText(labelProbabilityValue);
			this.testPowerLabel.setText(labelTestPower);
		}
	}
}
