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

import java.util.Optional;

import org.applied_geodesy.adjustment.DefaultValue;
import org.applied_geodesy.adjustment.EstimationType;
import org.applied_geodesy.adjustment.UnscentedTransformationParameter;
import org.applied_geodesy.adjustment.geometry.FeatureAdjustment;
import org.applied_geodesy.juniform.ui.i18n.I18N;
import org.applied_geodesy.ui.spinner.DoubleSpinner;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.ui.textfield.DoubleTextField.ValueSupport;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterOptions;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class LeastSquaresSettingDialog implements FormatterChangedListener {
	private static I18N i18N = I18N.getInstance();
	private static LeastSquaresSettingDialog leastSquaresSettingDialog = new LeastSquaresSettingDialog();
	private Dialog<Void> dialog = null;
	private Window window;
	private ComboBox<EstimationType> estimationTypeComboBox;
	private DoubleTextField utAlphaTextField, utBetaTextField, utWeight0TextField;
	private Spinner<Integer> iterationSpinner;
	private DoubleSpinner lmDampingSpinner;
	private CheckBox applyVarianceOfUnitWeightCheckBox, adjustModelParametersOnlyCheckBox, preconditioningCheckBox, estimateCenterOfMassCheckBox, estimateInitialGuessCheckBox;
	private FeatureAdjustment adjustment;
	private LeastSquaresSettingDialog() {}
	private FormatterOptions options = FormatterOptions.getInstance();
	private boolean enableUnscentedTransformation = false;
	
	public static void setOwner(Window owner) {
		leastSquaresSettingDialog.window = owner;
	}

	public static Optional<Void> showAndWait(FeatureAdjustment adjustment) {
		leastSquaresSettingDialog.init();
		leastSquaresSettingDialog.setFeatureAdjustment(adjustment);
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
	
	private void setFeatureAdjustment(FeatureAdjustment adjustment) {
		this.adjustment = adjustment;

		this.applyVarianceOfUnitWeightCheckBox.setSelected(this.adjustment.getVarianceComponentOfUnitWeight().isApplyAposterioriVarianceOfUnitWeight());
		this.adjustModelParametersOnlyCheckBox.setSelected(this.adjustment.isAdjustModelParametersOnly());
		this.preconditioningCheckBox.setSelected(this.adjustment.isPreconditioning());
				
		this.estimateCenterOfMassCheckBox.setDisable(true);
		this.estimateInitialGuessCheckBox.setDisable(true);
		
		this.estimateCenterOfMassCheckBox.setSelected(false);
		this.estimateInitialGuessCheckBox.setSelected(false);
		
		double dampingValue = this.adjustment.getLevenbergMarquardtDampingValue();
		SpinnerValueFactory.DoubleSpinnerValueFactory dampingSpinnerFactory = (SpinnerValueFactory.DoubleSpinnerValueFactory)this.lmDampingSpinner.getValueFactory();
		dampingValue = Math.max(Math.min(dampingValue, dampingSpinnerFactory.getMax()), dampingSpinnerFactory.getMin());
		dampingSpinnerFactory.setValue(dampingValue);

		int iteration = this.adjustment.getMaximalNumberOfIterations();
		SpinnerValueFactory.IntegerSpinnerValueFactory iterationSpinnerFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory)this.iterationSpinner.getValueFactory();
		iteration = Math.max(Math.min(iteration, iterationSpinnerFactory.getMax()), iterationSpinnerFactory.getMin());
		iterationSpinnerFactory.setValue(iteration);
		
		if (this.enableUnscentedTransformation) {
			this.estimationTypeComboBox.setValue(this.adjustment.getEstimationType());
			this.utAlphaTextField.setValue(this.adjustment.getUnscentedTransformationScaling());
			this.utBetaTextField.setValue(this.adjustment.getUnscentedTransformationDamping());
			this.utWeight0TextField.setValue(this.adjustment.getUnscentedTransformationWeightZero());
		}
		
		if (adjustment.getFeature() != null) {
			// if feature is user-defined, disable centroid and init. guess options
			if (!adjustment.getFeature().isImmutable()) {
				this.estimateCenterOfMassCheckBox.setDisable(true);
				this.estimateInitialGuessCheckBox.setDisable(true);
			}
			else {
				this.estimateCenterOfMassCheckBox.setDisable(false);
				this.estimateInitialGuessCheckBox.setDisable(false);

				this.estimateCenterOfMassCheckBox.setSelected(this.adjustment.getFeature().isEstimateCenterOfMass());
				this.estimateInitialGuessCheckBox.setSelected(this.adjustment.getFeature().isEstimateInitialGuess());
			}
		}
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
					adjustment.getVarianceComponentOfUnitWeight().setApplyAposterioriVarianceOfUnitWeight(applyVarianceOfUnitWeightCheckBox.isSelected());
					adjustment.setAdjustModelParametersOnly(adjustModelParametersOnlyCheckBox.isSelected());
					adjustment.setPreconditioning(preconditioningCheckBox.isSelected());
					adjustment.setMaximalNumberOfIterations(iterationSpinner.getValue());
					adjustment.setLevenbergMarquardtDampingValue(lmDampingSpinner.getValue());

					if (enableUnscentedTransformation) {
						adjustment.setEstimationType(estimationTypeComboBox.getValue());
						adjustment.setUnscentedTransformationScaling(utAlphaTextField.getNumber());
						adjustment.setUnscentedTransformationDamping(utBetaTextField.getNumber());
						adjustment.setUnscentedTransformationWeightZero(utWeight0TextField.getNumber());
					}
					
					if (adjustment.getFeature() != null && adjustment.getFeature().isImmutable()) {
						// if feature is *NOT* an user-defined one, store options
						if (adjustment.getFeature().isImmutable()) {
							adjustment.getFeature().setEstimateCenterOfMass(estimateCenterOfMassCheckBox.isSelected());
							adjustment.getFeature().setEstimateInitialGuess(estimateInitialGuessCheckBox.isSelected());
						}
					}
					
				}
				return null;
			}
		});
		// add formatter listener
		this.options.addFormatterChangedListener(this);
	}
	
	private Node createPane() {
		VBox contentPane = new VBox();
		
		if (this.enableUnscentedTransformation) {
			this.estimationTypeComboBox = DialogUtil.createEstimationTypeComboBox(estimationTypeStringConverter(),
					i18N.getString("LeastSquaresSettingDialog.estimationtype.tooltip", "Select estimation type"));
			this.estimationTypeComboBox.getItems().setAll(EstimationType.L2NORM, EstimationType.SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION);
			this.estimationTypeComboBox.setValue(EstimationType.L2NORM);
			
			VBox.setMargin(this.estimationTypeComboBox, new Insets(5, 5, 5, 5)); // oben, recht, unten, links
			
			contentPane.getChildren().add(this.estimationTypeComboBox);
			TabPane tabPane = new TabPane();
			Tab tabGen = new Tab(i18N.getString("LeastSquaresSettingDialog.tab.leastsquares.label", "Least-squares"), this.createGeneralSettingPane());
			tabGen.setTooltip(new Tooltip(i18N.getString("LeastSquaresSettingDialog.tab.leastsquares.tooltip", "Least-squares options")));
			tabGen.setClosable(false);
			tabPane.getTabs().add(tabGen);
		
			Tab tabUT  = new Tab(i18N.getString("LeastSquaresSettingDialog.tab.unscented_transformation.label", "Unscented transformation"), this.createUnscentedTransformationSettingPane());
			tabUT.setTooltip(new Tooltip(i18N.getString("LeastSquaresSettingDialog.tab.unscented_transformation.tooltip", "Unscented transformation parameters")));
			tabUT.setClosable(false);
			tabPane.getTabs().add(tabUT);
			
			tabPane.setPadding(new Insets(5, 0, 0, 0)); // oben, recht, unten, links
			tabPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
			tabPane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
			
			contentPane.getChildren().add(tabPane);
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					estimationTypeComboBox.requestFocus();
				}
			});
		}
		else 
			contentPane.getChildren().add(this.createGeneralSettingPane());
		
		contentPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		contentPane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		
		return contentPane;
	}
		
	private Node createGeneralSettingPane() {
		Label iterationLabel = new Label(i18N.getString("LeastSquaresSettingDialog.iterations.label", "Maximum number of iterations:"));
		Label dampingLabel   = new Label(i18N.getString("LeastSquaresSettingDialog.lm.damping.label", "Levenberg-Marquardt damping value \u03BB:"));
		
		this.iterationSpinner = DialogUtil.createIntegerSpinner(0, DefaultValue.getMaximumNumberOfIterations(), 10, i18N.getString("LeastSquaresSettingDialog.iterations.tooltip", "Set maximum permissible iteration value"));
		this.lmDampingSpinner = DialogUtil.createDoubleSpinner(CellValueType.STATISTIC, 0.0, 100.0, 0.01, i18N.getString("LeastSquaresSettingDialog.lm.damping.tooltip", "Set damping value of Levenberg-Marquardt algorithm. The algorithm will be applied, if \u03BB \u003E 0"));
		
		iterationLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		iterationLabel.setMaxSize(Control.USE_PREF_SIZE, Double.MAX_VALUE);
		
		dampingLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		dampingLabel.setMaxSize(Control.USE_PREF_SIZE, Double.MAX_VALUE);
		
		iterationLabel.setLabelFor(this.iterationSpinner);
		dampingLabel.setLabelFor(this.lmDampingSpinner);

		this.applyVarianceOfUnitWeightCheckBox = DialogUtil.createCheckBox(
				i18N.getString("LeastSquaresSettingDialog.applyvarianceofunitweight.label", "Apply variance of the unit weight"),
				i18N.getString("LeastSquaresSettingDialog.applyvarianceofunitweight.tooltip", "If checked, the estimated variance of the unit weight will be applied to scale the variance-covariance matrix")
		);
		
		this.adjustModelParametersOnlyCheckBox = DialogUtil.createCheckBox(
				i18N.getString("LeastSquaresSettingDialog.adjustperametersonly.label", "Adjust only model parameters"),
				i18N.getString("LeastSquaresSettingDialog.adjustperametersonly.tooltip", "If checked, the model parameters will be adjusted but no outlier test will be carried out")
		);
		
		this.preconditioningCheckBox = DialogUtil.createCheckBox(
				i18N.getString("LeastSquaresSettingDialog.preconditioning.label", "Preconditioning of normal system"),
				i18N.getString("LeastSquaresSettingDialog.preconditioning.tooltip", "If checked, a preconditioned iterative adjustment will be used")
		);
		
		this.estimateCenterOfMassCheckBox = DialogUtil.createCheckBox(
				i18N.getString("LeastSquaresSettingDialog.centerofmass.label", "Center of mass reduction"),
				i18N.getString("LeastSquaresSettingDialog.centerofmass.tooltip", "If checked, points will be reduced by the center of mass. This option is not supported by user-defined features")
		); 
		
		this.estimateInitialGuessCheckBox = DialogUtil.createCheckBox(
				i18N.getString("LeastSquaresSettingDialog.initialguess.label", "Derive initial guess"),
				i18N.getString("LeastSquaresSettingDialog.initialguess.tooltip", "If checked, an initial guess of the parameter to be estimated will be derived. This option is not supported by user-defined features")
		); 

		GridPane gridPane = DialogUtil.createGridPane();
		
		GridPane.setHgrow(iterationLabel, Priority.NEVER);
		GridPane.setHgrow(dampingLabel,   Priority.NEVER);
		
		GridPane.setHgrow(this.applyVarianceOfUnitWeightCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.adjustModelParametersOnlyCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.preconditioningCheckBox,           Priority.ALWAYS);
		GridPane.setHgrow(this.estimateCenterOfMassCheckBox,      Priority.ALWAYS);
		GridPane.setHgrow(this.estimateInitialGuessCheckBox,      Priority.ALWAYS);
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
		GridPane.setMargin(this.estimateCenterOfMassCheckBox,      insetsCenter);
		GridPane.setMargin(this.estimateInitialGuessCheckBox,      insetsCenter);
		
		GridPane.setMargin(iterationLabel, insetsLeft);
		GridPane.setMargin(dampingLabel,   insetsLeft);
		
		GridPane.setMargin(this.iterationSpinner, insetsRight);
		GridPane.setMargin(this.lmDampingSpinner, insetsRight);
		
		int row = 0;
		gridPane.add(this.applyVarianceOfUnitWeightCheckBox, 0, row++, 2, 1);
		gridPane.add(this.adjustModelParametersOnlyCheckBox, 0, row++, 2, 1);
		gridPane.add(this.preconditioningCheckBox,           0, row++, 2, 1);
		gridPane.add(this.estimateCenterOfMassCheckBox,      0, row++, 2, 1);
		gridPane.add(this.estimateInitialGuessCheckBox,      0, row++, 2, 1);
		
		gridPane.add(iterationLabel,        0, row);
		gridPane.add(this.iterationSpinner, 1, row++);
		
		gridPane.add(dampingLabel,          0, row);
		gridPane.add(this.lmDampingSpinner, 1, row++);
		
		gridPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		gridPane.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height

		return gridPane;
	}
	
	private Node createUnscentedTransformationSettingPane() {
		GridPane gridPane = DialogUtil.createGridPane();
		
		Label alphaLabel = new Label(i18N.getString("LeastSquaresSettingDialog.ut.scaling.label", "Scaling \u03B1:"));
		Label betaLabel  = new Label(i18N.getString("LeastSquaresSettingDialog.ut.damping.label", "Damping \u03B2:"));
		Label utWeight0Label = new Label(i18N.getString("LeastSquaresSettingDialog.ut.weight0.label", "Weight w0:"));  

		this.utAlphaTextField = DialogUtil.createDoubleTextField(
				CellValueType.STATISTIC,
				UnscentedTransformationParameter.getAlpha(),
				Boolean.FALSE,
				ValueSupport.EXCLUDING_INCLUDING_INTERVAL, 0.0, 1.0, 
				i18N.getString("LeastSquaresSettingDialog.ut.scaling.tooltip", "Defines spread of sigma points around the mean value, if \u03B1 \u2260 1"));
		this.utBetaTextField  = DialogUtil.createDoubleTextField(
				CellValueType.STATISTIC,
				UnscentedTransformationParameter.getBeta(), 
				Boolean.FALSE,
				ValueSupport.NON_NULL_VALUE_SUPPORT, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 
				i18N.getString("LeastSquaresSettingDialog.ut.damping.tooltip", "Considers prior knowledge of the distribution, if \u03B2 \u2260 0"));
		this.utWeight0TextField = DialogUtil.createDoubleTextField(
				CellValueType.STATISTIC,
				UnscentedTransformationParameter.getWeightZero(),
				Boolean.FALSE,
				ValueSupport.INCLUDING_EXCLUDING_INTERVAL, Double.NEGATIVE_INFINITY, 1.0, 
				i18N.getString("LeastSquaresSettingDialog.ut.weight0.tooltip", "Set the weight related to the zero sigma point (SUT: 0 \u2264 w0 \u003c 1)"));

		alphaLabel.setLabelFor(this.utAlphaTextField);
		betaLabel.setLabelFor(this.utBetaTextField);
		utWeight0Label.setLabelFor(this.utWeight0TextField);
		
		GridPane.setHgrow(alphaLabel, Priority.NEVER);
		GridPane.setHgrow(betaLabel, Priority.NEVER);
		GridPane.setHgrow(utWeight0Label, Priority.NEVER);
		
		GridPane.setHgrow(this.utAlphaTextField, Priority.ALWAYS);
		GridPane.setHgrow(this.utBetaTextField, Priority.ALWAYS);
		GridPane.setHgrow(this.utWeight0TextField, Priority.ALWAYS);
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsLeft   = new Insets(5, 7, 5, 2);
		Insets insetsRight  = new Insets(5, 2, 5, 7);

		GridPane.setMargin(alphaLabel, insetsLeft);
		GridPane.setMargin(this.utAlphaTextField, insetsRight);
		
		GridPane.setMargin(betaLabel, insetsLeft);
		GridPane.setMargin(this.utBetaTextField, insetsRight);
		
		GridPane.setMargin(utWeight0Label, insetsLeft);
		GridPane.setMargin(this.utWeight0TextField, insetsRight);
	
		int row = 0;
		gridPane.add(utWeight0Label,          0, row);
		gridPane.add(this.utWeight0TextField, 1, row++);
		
		gridPane.add(alphaLabel,            0, row);
		gridPane.add(this.utAlphaTextField, 1, row++, 2, 1);

		gridPane.add(betaLabel,            0, row);
		gridPane.add(this.utBetaTextField, 1, row++, 2, 1);

		return gridPane;
	}

	static StringConverter<EstimationType> estimationTypeStringConverter() {
		return new StringConverter<EstimationType>() {

			@Override
			public String toString(EstimationType type) {
				if (type == null)
					return null;
				switch(type) {
				case L2NORM:
					return i18N.getString("LeastSquaresSettingDialog.estimationtype.l2norm.label", "Least-squares adjustment (L2-Norm)");
				case SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION:
					return i18N.getString("LeastSquaresSettingDialog.estimationtype.sut.label", "Spherical simplex unscented transformation (SUT)");
				default:
					throw new IllegalArgumentException("Error, unsupported estimation type " + type + "!");
				}
			}

			@Override
			public EstimationType fromString(String string) {
				return EstimationType.valueOf(string);
			}
		};
	}
	
	@Override
	public void formatterChanged(FormatterEvent evt) {
		this.lmDampingSpinner.getEditor().setText(options.toStatisticFormat(this.lmDampingSpinner.getValueFactory().getValue()));
	}
		
	public static void setEnableUnscentedTransformation(boolean enable) {
		leastSquaresSettingDialog.enableUnscentedTransformation = enable;
	}
}
