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

import java.util.Optional;

import org.applied_geodesy.adjustment.DefaultValue;
import org.applied_geodesy.adjustment.transformation.TransformationAdjustment;
import org.applied_geodesy.adjustment.transformation.VarianceComponentType;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.coordtrans.ui.utils.UiUtil;
import org.applied_geodesy.ui.spinner.DoubleSpinner;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterOptions;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class LeastSquaresSettingDialog implements FormatterChangedListener {
	private static I18N i18N = I18N.getInstance();
	private static LeastSquaresSettingDialog leastSquaresSettingDialog = new LeastSquaresSettingDialog();
	private Dialog<Void> dialog = null;
	private Window window;
	private Spinner<Integer> iterationSpinner;
	private DoubleSpinner lmDampingSpinner;
	private CheckBox applyVarianceOfUnitWeightCheckBox, adjustModelParametersOnlyCheckBox, preconditioningCheckBox;
	private TransformationAdjustment adjustment;
	private LeastSquaresSettingDialog() {}
	private FormatterOptions options = FormatterOptions.getInstance();
	
	public static void setOwner(Window owner) {
		leastSquaresSettingDialog.window = owner;
	}

	public static Optional<Void> showAndWait(TransformationAdjustment adjustment) {
		leastSquaresSettingDialog.init();
		leastSquaresSettingDialog.setTransformationAdjustment(adjustment);
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	try {
            		leastSquaresSettingDialog.dialog.getDialogPane().requestLayout();
            		Stage stage = (Stage) leastSquaresSettingDialog.dialog.getDialogPane().getScene().getWindow();
            		stage.sizeToScene();
            	} 
            	catch (Exception e) {
            		e.printStackTrace();
            	}
            }
		});
		return leastSquaresSettingDialog.dialog.showAndWait();
	}
	
	private void setTransformationAdjustment(TransformationAdjustment adjustment) {
		this.adjustment = adjustment;

		this.applyVarianceOfUnitWeightCheckBox.setSelected(this.adjustment.getVarianceComponent(VarianceComponentType.GLOBAL).isApplyAposterioriVarianceOfUnitWeight());
		this.adjustModelParametersOnlyCheckBox.setSelected(this.adjustment.isAdjustModelParametersOnly());
		this.preconditioningCheckBox.setSelected(this.adjustment.isPreconditioning());
	
		double dampingValue = this.adjustment.getLevenbergMarquardtDampingValue();
		SpinnerValueFactory.DoubleSpinnerValueFactory dampingSpinnerFactory = (SpinnerValueFactory.DoubleSpinnerValueFactory)this.lmDampingSpinner.getValueFactory();
		dampingValue = Math.max(Math.min(dampingValue, dampingSpinnerFactory.getMax()), dampingSpinnerFactory.getMin());
		dampingSpinnerFactory.setValue(dampingValue);

		int iteration = this.adjustment.getMaximalNumberOfIterations();
		SpinnerValueFactory.IntegerSpinnerValueFactory iterationSpinnerFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory)this.iterationSpinner.getValueFactory();
		iteration = Math.max(Math.min(iteration, iterationSpinnerFactory.getMax()), iterationSpinnerFactory.getMin());
		iterationSpinnerFactory.setValue(iteration);
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<Void>();
		this.dialog.setTitle(i18N.getString("LeastSquaresSettingDialog.title", "Least-squares"));
		this.dialog.setHeaderText(i18N.getString("LeastSquaresSettingDialog.header", "Least-squares properties"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, Void>() {
			@Override
			public Void call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					adjustment.getVarianceComponent(VarianceComponentType.GLOBAL).setApplyAposterioriVarianceOfUnitWeight(applyVarianceOfUnitWeightCheckBox.isSelected());
					adjustment.setAdjustModelParametersOnly(adjustModelParametersOnlyCheckBox.isSelected());
					adjustment.setPreconditioning(preconditioningCheckBox.isSelected());
					adjustment.setMaximalNumberOfIterations(iterationSpinner.getValue());
					adjustment.setLevenbergMarquardtDampingValue(lmDampingSpinner.getValue());					
				}
				return null;
			}
		});
		// add formatter listener
		this.options.addFormatterChangedListener(this);
	}
	
	private Node createPane() {
		VBox contentPane = new VBox();
		
		contentPane.getChildren().add(this.createGeneralSettingPane());
		contentPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		contentPane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		
		return contentPane;
	}
		
	private Node createGeneralSettingPane() {
		Label iterationLabel = new Label(i18N.getString("LeastSquaresSettingDialog.iterations.label", "Maximum number of iterations:"));
		Label dampingLabel   = new Label(i18N.getString("LeastSquaresSettingDialog.lm.damping.label", "Levenberg-Marquardt damping value \u03BB:"));
		
		this.iterationSpinner = UiUtil.createIntegerSpinner(0, DefaultValue.getMaximalNumberOfIterations(), 10, i18N.getString("LeastSquaresSettingDialog.iterations.tooltip", "Set maximum permissible iteration value"));
		this.lmDampingSpinner = UiUtil.createDoubleSpinner(CellValueType.STATISTIC, 0.0, 100000.0, 10.0, i18N.getString("LeastSquaresSettingDialog.lm.damping.tooltip", "Set damping value of Levenberg-Marquardt algorithm. The algorithm will be applied, if \u03BB \u003E 0"));
		
		iterationLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		iterationLabel.setMaxSize(Control.USE_PREF_SIZE, Double.MAX_VALUE);
		
		dampingLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		dampingLabel.setMaxSize(Control.USE_PREF_SIZE, Double.MAX_VALUE);
		
		iterationLabel.setLabelFor(this.iterationSpinner);
		dampingLabel.setLabelFor(this.lmDampingSpinner);

		this.applyVarianceOfUnitWeightCheckBox = UiUtil.createCheckBox(
				i18N.getString("LeastSquaresSettingDialog.applyvarianceofunitweight.label", "Apply variance of the unit weight"),
				i18N.getString("LeastSquaresSettingDialog.applyvarianceofunitweight.tooltip", "If checked, the estimated variance of the unit weight will be applied to scale the variance-covariance matrix")
		);
		
		this.adjustModelParametersOnlyCheckBox = UiUtil.createCheckBox(
				i18N.getString("LeastSquaresSettingDialog.adjustperametersonly.label", "Adjust only model parameters"),
				i18N.getString("LeastSquaresSettingDialog.adjustperametersonly.tooltip", "If checked, the model parameters will be adjusted but no outlier test will be carried out")
		);
		
		this.preconditioningCheckBox = UiUtil.createCheckBox(
				i18N.getString("LeastSquaresSettingDialog.preconditioning.label", "Preconditioning of normal system"),
				i18N.getString("LeastSquaresSettingDialog.preconditioning.tooltip", "If checked, a preconditioned iterative adjustment will be used")
		);

		GridPane gridPane = UiUtil.createGridPane();
		
		GridPane.setHgrow(iterationLabel, Priority.NEVER);
		GridPane.setHgrow(dampingLabel,   Priority.NEVER);
		
		GridPane.setHgrow(this.applyVarianceOfUnitWeightCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.adjustModelParametersOnlyCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.preconditioningCheckBox,           Priority.ALWAYS);
		GridPane.setHgrow(this.iterationSpinner,                  Priority.ALWAYS);
		GridPane.setHgrow(this.lmDampingSpinner,                  Priority.ALWAYS);
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsCenter = new Insets(5, 2, 5, 2);
		Insets insetsTop    = new Insets(10, 2, 5, 2);
		Insets insetsLeft   = new Insets(5, 7, 5, 2);
		Insets insetsRight  = new Insets(5, 2, 5, 7);
		
		GridPane.setMargin(this.applyVarianceOfUnitWeightCheckBox, insetsTop);
		GridPane.setMargin(this.adjustModelParametersOnlyCheckBox, insetsCenter);
		GridPane.setMargin(this.preconditioningCheckBox,           insetsCenter);
		
		GridPane.setMargin(iterationLabel, insetsLeft);
		GridPane.setMargin(dampingLabel,   insetsLeft);
		
		GridPane.setMargin(this.iterationSpinner, insetsRight);
		GridPane.setMargin(this.lmDampingSpinner, insetsRight);
		
		int row = 0;
		gridPane.add(this.applyVarianceOfUnitWeightCheckBox, 0, row++, 2, 1);
		gridPane.add(this.adjustModelParametersOnlyCheckBox, 0, row++, 2, 1);
		gridPane.add(this.preconditioningCheckBox,           0, row++, 2, 1);
		
		gridPane.add(iterationLabel,        0, row);
		gridPane.add(this.iterationSpinner, 1, row++);
		
		gridPane.add(dampingLabel,          0, row);
		gridPane.add(this.lmDampingSpinner, 1, row++);
		
		gridPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		gridPane.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height

		return gridPane;
	}
	
	@Override
	public void formatterChanged(FormatterEvent evt) {
		this.lmDampingSpinner.getEditor().setText(options.toStatisticFormat(this.lmDampingSpinner.getValueFactory().getValue()));
	}
}
