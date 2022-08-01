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

package org.applied_geodesy.juniform.ui.dialog;

import java.util.Locale;
import java.util.Optional;

import org.applied_geodesy.adjustment.statistic.TestStatisticDefinition;
import org.applied_geodesy.adjustment.statistic.TestStatisticType;

import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterEventType;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.juniform.ui.i18n.I18N;
import org.applied_geodesy.ui.spinner.DoubleSpinner;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class TestStatisticDialog implements FormatterChangedListener {
	private static I18N i18N = I18N.getInstance();
	private static TestStatisticDialog testStatisticDialog = new TestStatisticDialog();
	private FormatterOptions options = FormatterOptions.getInstance();
	private Dialog<TestStatisticDefinition> dialog = null;
	private DoubleSpinner probabilityValueSpinner, testPowerSpinner;
	private ComboBox<TestStatisticType> testStatisticTypeComboBox;
	private CheckBox familywiseErrorRateCheckBox; 
	private Window window;
	private TestStatisticDefinition testStatisticDefinition;
	private Label probabilityValueLabel, testPowerLabel;

	private TestStatisticDialog() {}

	public static void setOwner(Window owner) {
		testStatisticDialog.window = owner;
	}

	public static Optional<TestStatisticDefinition> showAndWait(TestStatisticDefinition testStatisticDefinition) {
		testStatisticDialog.init();
		testStatisticDialog.setTestStatisticDefinition(testStatisticDefinition);
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
	
	private void setTestStatisticDefinition(TestStatisticDefinition testStatisticDefinition) {
		this.testStatisticDefinition = testStatisticDefinition;

		this.familywiseErrorRateCheckBox.setSelected(this.testStatisticDefinition.isFamilywiseErrorRate());
		this.testStatisticTypeComboBox.getSelectionModel().select(this.testStatisticDefinition.getTestStatisticType());

		SpinnerValueFactory.DoubleSpinnerValueFactory probabilityValueSpinnerFactory = (SpinnerValueFactory.DoubleSpinnerValueFactory)this.probabilityValueSpinner.getValueFactory();
		SpinnerValueFactory.DoubleSpinnerValueFactory testPowerSpinnerFactory        = (SpinnerValueFactory.DoubleSpinnerValueFactory)this.testPowerSpinner.getValueFactory();

		double probabilityValue = this.testStatisticDefinition.getProbabilityValue();
		double powerOfTest      = this.testStatisticDefinition.getPowerOfTest();

		probabilityValue = Math.max(Math.min(this.options.convertPercentToView(probabilityValue), probabilityValueSpinnerFactory.getMax()), probabilityValueSpinnerFactory.getMin());
		powerOfTest      = Math.max(Math.min(this.options.convertPercentToView(powerOfTest), testPowerSpinnerFactory.getMax()), testPowerSpinnerFactory.getMin());

		probabilityValueSpinnerFactory.setValue(probabilityValue);
		testPowerSpinnerFactory.setValue(powerOfTest);
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<TestStatisticDefinition>();
		this.dialog.setTitle(i18N.getString("TestStatisticDialog.title", "Test statistic"));
		this.dialog.setHeaderText(i18N.getString("TestStatisticDialog.header", "Test statistic properties"));
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
					
					testStatisticDefinition.setTestStatisticType(testStatisticType);
					testStatisticDefinition.setFamilywiseErrorRate(familywiseErrorRate);
					testStatisticDefinition.setProbabilityValue(probabilityValue);
					testStatisticDefinition.setPowerOfTest(powerOfTest);
					
					return testStatisticDefinition;		
				}
				return null;
			}
		});
		// add formatter listener
		this.options.addFormatterChangedListener(this);
	}

	private Node createPane() {
		String frmPercentUnit        = this.options.getFormatterOptions().get(CellValueType.PERCENTAGE).getUnit().toFormattedAbbreviation();
		String labelProbabilityValue = String.format(Locale.ENGLISH, "%s%s:", i18N.getString("TestStatisticDialog.probability.label", "Probability value \u03B1"), frmPercentUnit.isBlank() ? "" : " " + frmPercentUnit);
		String labelTestPower        = String.format(Locale.ENGLISH, "%s%s:", i18N.getString("TestStatisticDialog.testpower.label", "Power of test 1 - \u03B2"), frmPercentUnit.isBlank() ? "" : " " + frmPercentUnit);

		String tooltipProbabilityValue = i18N.getString("TestStatisticDialog.probability.tooltip", "Set probability value (type I error)");
		String tooltipTestPower        = i18N.getString("TestStatisticDialog.testpower.tooltip", "Set power of test (type II error)");

		String labelFamilywiseErrorRate   = i18N.getString("TestStatisticDialog.familywiseerror.label", "Familywise error rate");
		String tooltipFamilywiseErrorRate = i18N.getString("TestStatisticDialog.familywiseerror.tooltip", "If checked, probability value \u03B1 defines familywise error rate");

		this.probabilityValueLabel = new Label(labelProbabilityValue);
		this.testPowerLabel        = new Label(labelTestPower);
		
		this.familywiseErrorRateCheckBox = DialogUtil.createCheckBox(labelFamilywiseErrorRate, tooltipFamilywiseErrorRate);
		
		this.probabilityValueSpinner = DialogUtil.createDoubleSpinner(CellValueType.PERCENTAGE, 0.0005, 0.30, 0.01, tooltipProbabilityValue);
		this.testPowerSpinner        = DialogUtil.createDoubleSpinner(CellValueType.PERCENTAGE, 0.50, 0.9995, 0.01, tooltipTestPower);

		this.testStatisticTypeComboBox = DialogUtil.createTestStatisticTypeComboBox(createTestStatisticTypeStringConverter(), i18N.getString("TestStatisticDialog.type.tooltip", "Select method for type I error adaption"));
		
		probabilityValueLabel.setLabelFor(this.probabilityValueSpinner);
		testPowerLabel.setLabelFor(this.testPowerSpinner);
		
		GridPane gridPane = DialogUtil.createGridPane();
		
		probabilityValueLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		testPowerLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		probabilityValueLabel.setMaxWidth(Double.MAX_VALUE);
		testPowerLabel.setMaxWidth(Double.MAX_VALUE);
		
		GridPane.setHgrow(probabilityValueLabel, Priority.NEVER);
		GridPane.setHgrow(testPowerLabel,        Priority.NEVER);
		
		GridPane.setHgrow(this.testStatisticTypeComboBox,   Priority.ALWAYS);
		GridPane.setHgrow(this.familywiseErrorRateCheckBox, Priority.ALWAYS);
		
		GridPane.setHgrow(this.probabilityValueSpinner, Priority.ALWAYS);
		GridPane.setHgrow(this.testPowerSpinner,        Priority.ALWAYS);
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsLeft   = new Insets(5, 7, 5, 2);
		Insets insetsRight  = new Insets(5, 2, 5, 7);
		Insets insetsCenter = new Insets(5, 2, 5, 2);

		GridPane.setMargin(this.testStatisticTypeComboBox, insetsCenter);
		GridPane.setMargin(this.familywiseErrorRateCheckBox, insetsCenter);
		
		GridPane.setMargin(probabilityValueLabel, insetsLeft);
		GridPane.setMargin(testPowerLabel,        insetsLeft);
		
		GridPane.setMargin(this.probabilityValueSpinner, insetsRight);
		GridPane.setMargin(this.testPowerSpinner,        insetsRight);
		
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
		
		gridPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		gridPane.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height
		
		return gridPane;
	}
	
	static StringConverter<TestStatisticType> createTestStatisticTypeStringConverter() {
		return new StringConverter<TestStatisticType>() {
			@Override
			public String toString(TestStatisticType type) {
				if (type == null)
					return null;
				switch(type) {
				case BAARDA_METHOD:
					return i18N.getString("TestStatisticDialog.type.baarda", "Baardas B-method");
				case SIDAK:
					return i18N.getString("TestStatisticDialog.type.sidak", "\u0160id\u00E1k correction");
				case NONE:
					return i18N.getString("TestStatisticDialog.type.none", "None");	
				}
				return "";
			}

			@Override
			public TestStatisticType fromString(String string) {
				return TestStatisticType.valueOf(string);
			}
		};
	}
	
	@Override
	public void formatterChanged(FormatterEvent evt) {
		if (evt != null && evt.getCellType() == CellValueType.PERCENTAGE && evt.getEventType() == FormatterEventType.UNIT_CHANGED) {
			String frmPercentUnit        = this.options.getFormatterOptions().get(CellValueType.PERCENTAGE).getUnit().toFormattedAbbreviation();
			String labelProbabilityValue = String.format(Locale.ENGLISH, "%s%s:", i18N.getString("TestStatisticDialog.probability.label", "Probability value \u03B1"), frmPercentUnit.isBlank() ? "" : " " + frmPercentUnit);
			String labelTestPower        = String.format(Locale.ENGLISH, "%s%s:", i18N.getString("TestStatisticDialog.testpower.label", "Power of test 1 - \u03B2"), frmPercentUnit.isBlank() ? "" : " " + frmPercentUnit);


			this.probabilityValueLabel.setText(labelProbabilityValue);
			this.testPowerLabel.setText(labelTestPower);
		}
	}
}
