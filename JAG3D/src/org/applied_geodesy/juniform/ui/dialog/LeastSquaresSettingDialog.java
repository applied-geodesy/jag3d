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

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

import org.applied_geodesy.adjustment.DefaultValue;
import org.applied_geodesy.adjustment.geometry.FeatureAdjustment;
import org.applied_geodesy.juniform.ui.i18n.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class LeastSquaresSettingDialog {
	private static I18N i18N = I18N.getInstance();
	private static LeastSquaresSettingDialog leastSquaresSettingDialog = new LeastSquaresSettingDialog();
	private Dialog<Void> dialog = null;
	private Window window;
//	private ComboBox<EstimationType> estimationTypeComboBox;
	private Spinner<Integer> iterationSpinner;
	private CheckBox applyVarianceOfUnitWeightCheckBox, adjustModelParametersOnlyCheckBox, preconditioningCheckBox, estimateCenterOfMassCheckBox, estimateInitialGuessCheckBox;
	private FeatureAdjustment adjustment;
	private LeastSquaresSettingDialog() {}

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
		
		int iteration = this.adjustment.getMaximalNumberOfIterations();
		SpinnerValueFactory.IntegerSpinnerValueFactory iterationSpinnerFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory)this.iterationSpinner.getValueFactory();
		iteration = Math.max(Math.min(iteration, iterationSpinnerFactory.getMax()), iterationSpinnerFactory.getMin());
		iterationSpinnerFactory.setValue(iteration);
		
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
//					adjustment.setEstimationType(estimationTypeComboBox.getValue());
					
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
	}
	
	private Node createPane() {
		VBox contentPane = new VBox();
		
//		this.estimationTypeComboBox = DialogUtil.createEstimationTypeComboBox(estimationTypeStringConverter(),
//				i18N.getString("LeastSquaresSettingDialog.estimationtype.tooltip", "Select estimation type"));
//		
//		this.estimationTypeComboBox.setDisable(true);
//		this.estimationTypeComboBox.setVisible(false);
//		this.estimationTypeComboBox.setManaged(false);
//		
//		VBox.setMargin(this.estimationTypeComboBox, new Insets(5, 5, 5, 5)); // oben, recht, unten, links
//		
//		contentPane.getChildren().add(this.estimationTypeComboBox);
		contentPane.getChildren().add(this.createGeneralSettingPane());
		
		contentPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		contentPane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		
//		Platform.runLater(new Runnable() {
//			@Override public void run() {
//				estimationTypeComboBox.requestFocus();
//			}
//		});
		
		return contentPane;
	}
		
	private Node createGeneralSettingPane() {
		Label iterationLabel = new Label(i18N.getString("LeastSquaresSettingDialog.iterations.label", "Maximum number of iterations:"));
		this.iterationSpinner = this.createIntegerSpinner(0, DefaultValue.getMaximalNumberOfIterations(), 10, i18N.getString("LeastSquaresSettingDialog.iterations.tooltip", "Set maximum permissible iteration value"));
		iterationLabel.setLabelFor(this.iterationSpinner);
		iterationLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		iterationLabel.setMaxSize(Control.USE_PREF_SIZE, Double.MAX_VALUE);

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
		
		GridPane.setHgrow(this.applyVarianceOfUnitWeightCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.adjustModelParametersOnlyCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.preconditioningCheckBox,           Priority.ALWAYS);
		GridPane.setHgrow(this.estimateCenterOfMassCheckBox,      Priority.ALWAYS);
		GridPane.setHgrow(this.estimateInitialGuessCheckBox,      Priority.ALWAYS);
		GridPane.setHgrow(this.iterationSpinner,                  Priority.ALWAYS);
		
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
		
		GridPane.setMargin(iterationLabel,        insetsLeft);
		GridPane.setMargin(this.iterationSpinner, insetsRight);
		
		int row = 0;
		gridPane.add(this.applyVarianceOfUnitWeightCheckBox, 0, row++, 2, 1);
		gridPane.add(this.adjustModelParametersOnlyCheckBox, 0, row++, 2, 1);
		gridPane.add(this.preconditioningCheckBox,           0, row++, 2, 1);
		gridPane.add(this.estimateCenterOfMassCheckBox,      0, row++, 2, 1);
		gridPane.add(this.estimateInitialGuessCheckBox,      0, row++, 2, 1);
		
		gridPane.add(iterationLabel,        0, row);
		gridPane.add(this.iterationSpinner, 1, row++);
		
		gridPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		gridPane.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE); // width, height

		return gridPane;
	}

//	static StringConverter<EstimationType> estimationTypeStringConverter() {
//		return new StringConverter<EstimationType>() {
//
//			@Override
//			public String toString(EstimationType type) {
//				if (type == null)
//					return null;
//				switch(type) {
//				case L1NORM:
//					return i18N.getString("LeastSquaresSettingDialog.estimationtype.l1norm.label", "Robust estimation (L1-Norm)");
//				case L2NORM:
//					return i18N.getString("LeastSquaresSettingDialog.estimationtype.l2norm.label", "Least-squares adjustment (L2-Norm)");
//				case SIMULATION:
//					return i18N.getString("LeastSquaresSettingDialog.estimationtype.simulation.label", "Simulation (Pre-analysis)");
//				case MODIFIED_UNSCENTED_TRANSFORMATION:
//					return i18N.getString("LeastSquaresSettingDialog.estimationtype.mut.label", "Modified unscented transformation (MUT)");
//				case SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION:
//					return i18N.getString("LeastSquaresSettingDialog.estimationtype.sut.label", "Spherical simplex unscented transformation (SUT)");
//				}
//				return null;
//			}
//
//			@Override
//			public EstimationType fromString(String string) {
//				return EstimationType.valueOf(string);
//			}
//		};
//	}
	
	private Spinner<Integer> createIntegerSpinner(int min, int max, int amountToStepBy, String tooltip) {
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
}
