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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.applied_geodesy.adjustment.DefaultValue;
import org.applied_geodesy.adjustment.EstimationType;
import org.applied_geodesy.adjustment.UnscentedTransformationParameter;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.ui.spinner.DoubleSpinner;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.ui.textfield.DoubleTextField.ValueSupport;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterEventType;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.jag3d.ui.i18n.I18N;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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

public class LeastSquaresSettingDialog implements FormatterChangedListener {

	public class LeastSquaresSettings {
		private ObjectProperty<Integer> iteration             = new SimpleObjectProperty<Integer>(50);
		private ObjectProperty<Integer> principalComponents   = new SimpleObjectProperty<Integer>(1);
		private ObjectProperty<Double> robustEstimationLimit  = new SimpleObjectProperty<Double>(DefaultValue.getRobustEstimationLimit());
		private BooleanProperty orientation                   = new SimpleBooleanProperty(Boolean.TRUE);
		private BooleanProperty congruenceAnalysis            = new SimpleBooleanProperty(Boolean.FALSE);
		private BooleanProperty applyVarianceOfUnitWeight     = new SimpleBooleanProperty(DefaultValue.applyVarianceOfUnitWeight());
		private ObjectProperty<EstimationType> estimationType = new SimpleObjectProperty<EstimationType>(DefaultValue.getEstimationType());
		private ObjectProperty<Double> scalingParameterAlphaUT = new SimpleObjectProperty<Double>(UnscentedTransformationParameter.getAlpha());
		private ObjectProperty<Double> dampingParameterBetaUT  = new SimpleObjectProperty<Double>(UnscentedTransformationParameter.getBeta());
		private ObjectProperty<Double> weightZero = new SimpleObjectProperty<Double>(UnscentedTransformationParameter.getWeightZero());
		
		private double confidenceLevel = DefaultValue.getConfidenceLevel();
		
		public ObjectProperty<Integer> iterationProperty() {
			return this.iteration;
		}
		
		public int getIteration() {
			return this.iterationProperty().get();
		}
		
		public void setIteration(final int iteration) {
			this.iterationProperty().set(iteration);
		}
		
		public ObjectProperty<Integer> principalComponentsProperty() {
			return this.principalComponents;
		}
		
		public int getPrincipalComponents() {
			return this.principalComponentsProperty().get();
		}
		
		public void setPrincipalComponents(final int principalComponents) {
			this.principalComponentsProperty().set(principalComponents);
		}
		
		public ObjectProperty<Double> robustEstimationLimitProperty() {
			return this.robustEstimationLimit;
		}
		
		public double getRobustEstimationLimit() {
			return this.robustEstimationLimitProperty().get();
		}
		
		public void setRobustEstimationLimit(final double robustEstimationLimit) {
			this.robustEstimationLimitProperty().set(robustEstimationLimit);
		}
		
		public double getConfidenceLevel() {
			return this.confidenceLevel;
		}
		
		public void setConfidenceLevel(final double confidenceLevel) {
			this.confidenceLevel = confidenceLevel;
		}
		
		public BooleanProperty orientationProperty() {
			return this.orientation;
		}
		
		public boolean isOrientation() {
			return this.orientationProperty().get();
		}
		
		public void setOrientation(final boolean orientation) {
			this.orientationProperty().set(orientation);
		}
		
		public BooleanProperty congruenceAnalysisProperty() {
			return this.congruenceAnalysis;
		}

		public boolean isCongruenceAnalysis() {
			return this.congruenceAnalysisProperty().get();
		}
		
		public void setCongruenceAnalysis(final boolean congruenceAnalysis) {
			this.congruenceAnalysisProperty().set(congruenceAnalysis);
		}
		
		public BooleanProperty applyVarianceOfUnitWeightProperty() {
			return this.applyVarianceOfUnitWeight;
		}
		
		public boolean isApplyVarianceOfUnitWeight() {
			return this.applyVarianceOfUnitWeightProperty().get();
		}
		
		public void setApplyVarianceOfUnitWeight(final boolean applyVarianceOfUnitWeight) {
			this.applyVarianceOfUnitWeightProperty().set(applyVarianceOfUnitWeight);
		}
		
		public ObjectProperty<EstimationType> estimationTypeProperty() {
			return this.estimationType;
		}
		
		public EstimationType getEstimationType() {
			return this.estimationTypeProperty().get();
		}
		
		public void setEstimationType(final EstimationType estimationType) {
			this.estimationTypeProperty().set(estimationType);
		}
		
		public ObjectProperty<Double> scalingParameterAlphaUTProperty() {
			return this.scalingParameterAlphaUT;
		}
		
		public double getScalingParameterAlphaUT() {
			return this.scalingParameterAlphaUTProperty().get();
		}
		
		public void setScalingParameterAlphaUT(final double alpha) {
			this.scalingParameterAlphaUTProperty().set(alpha);
		}
		
		public ObjectProperty<Double> dampingParameterBetaUTProperty() {
			return this.dampingParameterBetaUT;
		}
		
		public double getDampingParameterBetaUT() {
			return this.dampingParameterBetaUTProperty().get();
		}
		
		public void setDampingParameterBetaUT(final double beta) {
			this.dampingParameterBetaUTProperty().set(beta);
		}
		
		public ObjectProperty<Double> weightZeroProperty() {
			return this.weightZero;
		}
		
		public double getWeightZero() {
			return this.weightZeroProperty().get();
		}
		
		public void setWeightZero(final double weight0) {
			this.weightZeroProperty().set(weight0);
		}
	}
	
	private class EstimationTypeChangeListener implements ChangeListener<EstimationType> {
		@Override
		public void changed(ObservableValue<? extends EstimationType> observable, EstimationType oldValue, EstimationType newValue) {
			settings.setEstimationType(newValue);
			estimationTypeComboBox.setValue(newValue);
		}
	}

	private boolean enableUnscentedTransformation = false;
	private I18N i18n = I18N.getInstance();
	private FormatterOptions options = FormatterOptions.getInstance();
	private static LeastSquaresSettingDialog leastSquaresSettingDialog = new LeastSquaresSettingDialog();
	private Dialog<LeastSquaresSettings> dialog = null;
	private Window window;
	private ComboBox<EstimationType> estimationTypeComboBox;
	private LeastSquaresSettings settings = new LeastSquaresSettings();
	private Spinner<Integer> iterationSpinner;
	private Spinner<Integer> principalComponentSpinner;
	private DoubleSpinner robustSpinner, confidenceLevelSpinner;
	private DoubleTextField alphaTextField, betaTextField, weight0TextField;
	private CheckBox orientationApproximationCheckBox, congruenceAnalysisCheckBox, applyVarianceOfUnitWeightCheckBox;
	private Label confidenceLevelLabel;
	private LeastSquaresSettingDialog() {}

	public static void setOwner(Window owner) {
		leastSquaresSettingDialog.window = owner;
	}

	public static Optional<LeastSquaresSettings> showAndWait() {
		leastSquaresSettingDialog.init();
		leastSquaresSettingDialog.load();
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


	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<LeastSquaresSettings>();
		this.dialog.setTitle(i18n.getString("LeastSquaresSettingDialog.title", "Least-squares"));
		this.dialog.setHeaderText(i18n.getString("LeastSquaresSettingDialog.header", "Least-squares properties"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		//		this.dialog.initStyle(StageStyle.UTILITY);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, LeastSquaresSettings>() {
			@Override
			public LeastSquaresSettings call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					save();	
					return settings;
				}
				return null;
			}
		});
		// add formatter listener
		options.addFormatterChangedListener(this);
	}
	
	private Node createPane() {
		VBox contentPane = new VBox();
		
		this.estimationTypeComboBox = this.createEstimationTypeComboBox(DefaultValue.getEstimationType(), i18n.getString("LeastSquaresSettingDialog.estimationtype.tooltip", "Set estimation method")); 
		this.settings.estimationTypeProperty().addListener(new EstimationTypeChangeListener());
		this.estimationTypeComboBox.getSelectionModel().selectedItemProperty().addListener(new EstimationTypeChangeListener());
		VBox.setMargin(this.estimationTypeComboBox, new Insets(0, 0, 5, 0)); // oben, recht, unten, links
		
		contentPane.getChildren().add(this.estimationTypeComboBox);
		
		if (this.enableUnscentedTransformation) {
			TabPane tabPane = new TabPane();
			Tab tabGen = new Tab(i18n.getString("LeastSquaresSettingDialog.tab.leastsquares.label", "Least-squares"), this.createGeneralSettingPane());
			tabGen.setTooltip(new Tooltip(i18n.getString("LeastSquaresSettingDialog.tab.leastsquares.tooltip", "Least-squares options")));
			tabGen.setClosable(false);
			tabPane.getTabs().add(tabGen);
		
			Tab tabUT  = new Tab(i18n.getString("LeastSquaresSettingDialog.tab.unscented_transformation.label", "Unscented transformation"), this.createUnscentedTransformationSettingPane());
			tabUT.setTooltip(new Tooltip(i18n.getString("LeastSquaresSettingDialog.tab.unscented_transformation.tooltip", "Unscented transformation parameters")));
			tabUT.setClosable(false);
			tabPane.getTabs().add(tabUT);
			
			tabPane.setPadding(new Insets(5, 0, 0, 0)); // oben, recht, unten, links
			tabPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
			tabPane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
			
			contentPane.getChildren().add(tabPane);
		}
		else {
			contentPane.getChildren().add(this.createGeneralSettingPane());
		}
		
		contentPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		contentPane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				estimationTypeComboBox.requestFocus();
			}
		});
		
		return contentPane;
	}
	
	private Node createUnscentedTransformationSettingPane() {
		Label alphaLabel = new Label(i18n.getString("LeastSquaresSettingDialog.ut.scaling.label", "Scaling \u03B1:"));
		Label betaLabel  = new Label(i18n.getString("LeastSquaresSettingDialog.ut.damping.label", "Damping \u03B2:"));
		Label utWeight0Label = new Label(i18n.getString("LeastSquaresSettingDialog.ut.weight0.label", "Weight w0:"));  
		
		this.alphaTextField = this.createDoubleTextField(UnscentedTransformationParameter.getAlpha(), ValueSupport.EXCLUDING_INCLUDING_INTERVAL, 0.0, 1.0, i18n.getString("LeastSquaresSettingDialog.ut.scaling.tooltip", "Defines spread of sigma points around the mean value, if \u03B1 \u2260 1"));
		this.betaTextField  = this.createDoubleTextField(UnscentedTransformationParameter.getBeta(), ValueSupport.NON_NULL_VALUE_SUPPORT, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, i18n.getString("LeastSquaresSettingDialog.ut.damping.tooltip", "Considers prior knowledge of the distribution, if \u03B2 \u2260 0"));
		this.weight0TextField = this.createDoubleTextField(UnscentedTransformationParameter.getWeightZero(), ValueSupport.INCLUDING_EXCLUDING_INTERVAL, Double.NEGATIVE_INFINITY, 1.0, i18n.getString("LeastSquaresSettingDialog.ut.weight0.tooltip", "Set the weight related to the zero sigma point (MUT: w0 \u003c 1, SUT: 0 \u2264 w0 \u003c 1)"));

		this.alphaTextField.numberProperty().bindBidirectional(this.settings.scalingParameterAlphaUTProperty());
		this.betaTextField.numberProperty().bindBidirectional(this.settings.dampingParameterBetaUTProperty());
		this.weight0TextField.numberProperty().bindBidirectional(this.settings.weightZeroProperty());
		
		alphaLabel.setLabelFor(this.alphaTextField);
		betaLabel.setLabelFor(this.betaTextField);
		utWeight0Label.setLabelFor(this.weight0TextField);
		
		GridPane gridPane = new GridPane();
		gridPane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		gridPane.setAlignment(Pos.TOP_CENTER);
		gridPane.setPadding(new Insets(5,15,5,15)); // oben, recht, unten, links
//		gridPane.setGridLinesVisible(true);
		
		GridPane.setHgrow(alphaLabel, Priority.NEVER);
		GridPane.setHgrow(betaLabel, Priority.NEVER);
		GridPane.setHgrow(utWeight0Label, Priority.NEVER);
		
		GridPane.setHgrow(this.alphaTextField, Priority.ALWAYS);
		GridPane.setHgrow(this.betaTextField, Priority.ALWAYS);
		GridPane.setHgrow(this.weight0TextField, Priority.ALWAYS);
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsLeft   = new Insets(5, 7, 5, 2);
		Insets insetsRight  = new Insets(5, 2, 5, 7);

		GridPane.setMargin(alphaLabel, insetsLeft);
		GridPane.setMargin(this.alphaTextField, insetsRight);
		
		GridPane.setMargin(betaLabel, insetsLeft);
		GridPane.setMargin(this.betaTextField, insetsRight);
		
		GridPane.setMargin(utWeight0Label, insetsLeft);
		GridPane.setMargin(this.weight0TextField, insetsRight);
	
		int row = 0;
		gridPane.add(utWeight0Label,        0, ++row);
		gridPane.add(this.weight0TextField, 1,   row);
		
		gridPane.add(alphaLabel,          0, ++row);
		gridPane.add(this.alphaTextField, 1,   row, 2, 1);

		gridPane.add(betaLabel,          0, ++row);
		gridPane.add(this.betaTextField, 1,   row, 2, 1);

		return gridPane;
	}
	
	private Node createGeneralSettingPane() {
		String frmPercentUnit = this.options.getFormatterOptions().get(CellValueType.PERCENTAGE).getUnit().toFormattedAbbreviation();
		
		Label iterationLabel = new Label(i18n.getString("LeastSquaresSettingDialog.iterations.label", "Maximum number of iterations:"));
		this.iterationSpinner = this.createIntegerSpinner(0, DefaultValue.getMaximumNumberOfIterations(), 10, i18n.getString("LeastSquaresSettingDialog.iterations.tooltip", "Set maximum permissible iteration value"));
		iterationLabel.setLabelFor(this.iterationSpinner);
		iterationLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label principalComponentLabel = new Label(i18n.getString("LeastSquaresSettingDialog.principal_components.label", "Number of principal components:"));
		this.principalComponentSpinner = this.createIntegerSpinner(0, Integer.MAX_VALUE, 1, i18n.getString("LeastSquaresSettingDialog.principal_components.tooltip", "Set number of principal components to be estimated"));
		principalComponentLabel.setLabelFor(this.principalComponentSpinner);
		principalComponentLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

		Label robustLabel = new Label(i18n.getString("LeastSquaresSettingDialog.robust.label", "Robust estimation limit:"));
		this.robustSpinner = this.createDoubleSpinner(CellValueType.STATISTIC, 1.5, Math.max(DefaultValue.getRobustEstimationLimit(), 6.0), 0.5, i18n.getString("LeastSquaresSettingDialog.robust.tooltip", "Set robust estimation limit of BIBER estimator"));
		robustLabel.setLabelFor(this.robustSpinner);
		robustLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		this.confidenceLevelLabel   = new Label(String.format(Locale.ENGLISH, "%s%s:", i18n.getString("LeastSquaresSettingDialog.confidence.label", "Confidence level 1 - \u03B1"), frmPercentUnit.isBlank() ? "" : " " + frmPercentUnit));
		this.confidenceLevelSpinner = this.createDoubleSpinner(CellValueType.PERCENTAGE, 0.0005, 1.0 - 0.0005, 0.01, i18n.getString("LeastSquaresSettingDialog.confidence.tooltip", "Set confidence level of parameters"));
		this.confidenceLevelLabel.setLabelFor(this.confidenceLevelSpinner);
		this.confidenceLevelLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		this.orientationApproximationCheckBox = this.createCheckBox(
			i18n.getString("LeastSquaresSettingDialog.orientation.label", "Orientation approximation"),
			i18n.getString("LeastSquaresSettingDialog.orientation.tooltip", "If checked, orientation approximations of direction sets will be estimated before network adjustment starts")
		);

		this.congruenceAnalysisCheckBox = this.createCheckBox(
				i18n.getString("LeastSquaresSettingDialog.congruenceanalysis.label", "Congruence analysis"),
				i18n.getString("LeastSquaresSettingDialog.congruenceanalysis.tooltip", "If checked, a congruence analysis will be carry out in case of a free network adjustment")
		);
		
		this.applyVarianceOfUnitWeightCheckBox = this.createCheckBox(
				i18n.getString("LeastSquaresSettingDialog.applyvarianceofunitweight.label", "Variance of the unit weight"),
				i18n.getString("LeastSquaresSettingDialog.applyvarianceofunitweight.tooltip", "If checked, the estimated variance of the unit weight will be applied to scale the variance-covariance matrix")
		);
		
		this.orientationApproximationCheckBox.selectedProperty().bindBidirectional(this.settings.orientationProperty());
		this.congruenceAnalysisCheckBox.selectedProperty().bindBidirectional(this.settings.congruenceAnalysisProperty());
		this.applyVarianceOfUnitWeightCheckBox.selectedProperty().bindBidirectional(this.settings.applyVarianceOfUnitWeightProperty());
		this.iterationSpinner.getValueFactory().valueProperty().bindBidirectional(this.settings.iterationProperty());
		this.principalComponentSpinner.getValueFactory().valueProperty().bindBidirectional(this.settings.principalComponentsProperty());
		this.robustSpinner.getValueFactory().valueProperty().bindBidirectional(this.settings.robustEstimationLimitProperty());
//		this.confidenceLevelSpinner.getValueFactory().valueProperty().bindBidirectional(this.settings.confidenceLevelProperty());

		GridPane gridPane = new GridPane();
		gridPane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);

//		gridPane.setHgap(20);
//		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.TOP_CENTER);
		gridPane.setPadding(new Insets(5,15,5,15)); // oben, recht, unten, links
		//gridPane.setGridLinesVisible(true);
		
		GridPane.setHgrow(iterationLabel, Priority.NEVER);
		GridPane.setHgrow(principalComponentLabel, Priority.NEVER);
		GridPane.setHgrow(robustLabel, Priority.NEVER);
		GridPane.setHgrow(this.confidenceLevelLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.orientationApproximationCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.applyVarianceOfUnitWeightCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.congruenceAnalysisCheckBox, Priority.ALWAYS);
		
		GridPane.setHgrow(this.robustSpinner, Priority.ALWAYS);
		GridPane.setHgrow(this.confidenceLevelSpinner, Priority.ALWAYS);
		GridPane.setHgrow(this.principalComponentSpinner, Priority.ALWAYS);
		GridPane.setHgrow(this.iterationSpinner, Priority.ALWAYS);
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsCenter = new Insets(5, 2, 5, 2);
		Insets insetsTop    = new Insets(10, 2, 5, 2);
		Insets insetsLeft   = new Insets(5, 7, 5, 2);
		Insets insetsRight  = new Insets(5, 2, 5, 7);
		
		GridPane.setMargin(this.applyVarianceOfUnitWeightCheckBox, insetsTop);
		GridPane.setMargin(this.orientationApproximationCheckBox, insetsCenter);
		
		GridPane.setMargin(this.congruenceAnalysisCheckBox, insetsTop);

		GridPane.setMargin(this.confidenceLevelLabel, insetsLeft);
		GridPane.setMargin(this.confidenceLevelSpinner, insetsRight);
		
		GridPane.setMargin(iterationLabel, insetsLeft);
		GridPane.setMargin(this.iterationSpinner, insetsRight);

		GridPane.setMargin(robustLabel, insetsLeft);
		GridPane.setMargin(this.robustSpinner, insetsRight);
		
		GridPane.setMargin(principalComponentLabel, insetsLeft);
		GridPane.setMargin(this.principalComponentSpinner, insetsRight);
		
		int row = 0;
		gridPane.add(this.applyVarianceOfUnitWeightCheckBox, 0, ++row, 2, 1);
		gridPane.add(this.orientationApproximationCheckBox,  0, ++row, 2, 1);

		gridPane.add(this.confidenceLevelLabel,   0, ++row);
		gridPane.add(this.confidenceLevelSpinner, 1,   row);
		
		gridPane.add(iterationLabel,        0, ++row);
		gridPane.add(this.iterationSpinner, 1,   row);
		
		gridPane.add(robustLabel,        0, ++row);
		gridPane.add(this.robustSpinner, 1,   row);
		
		gridPane.add(principalComponentLabel,         0, ++row);
		gridPane.add(this.principalComponentSpinner,  1,   row);

		gridPane.add(this.congruenceAnalysisCheckBox, 0, ++row, 2, 1);

		return gridPane;
	}
	
	
	private ComboBox<EstimationType> createEstimationTypeComboBox(EstimationType item, String tooltip) {
		ComboBox<EstimationType> typeComboBox = new ComboBox<EstimationType>();
		EstimationType[] estimationTypeArray = EstimationType.values();
		if (!this.enableUnscentedTransformation) {
			List<EstimationType> estimationTypeList = new ArrayList<EstimationType>(Arrays.asList(estimationTypeArray));
			estimationTypeList.remove(EstimationType.SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION);
			estimationTypeList.remove(EstimationType.MODIFIED_UNSCENTED_TRANSFORMATION);
			estimationTypeArray = estimationTypeList.toArray(new EstimationType[estimationTypeList.size()]);
			if (item == EstimationType.SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION || item == EstimationType.MODIFIED_UNSCENTED_TRANSFORMATION)
				item = EstimationType.L2NORM;
		}
		typeComboBox.getItems().setAll(estimationTypeArray);  // EstimationType.values()
		typeComboBox.getSelectionModel().select(item);
		typeComboBox.setConverter(new StringConverter<EstimationType>() {

			@Override
			public String toString(EstimationType type) {
				if (type == null)
					return null;
				switch(type) {
				case L1NORM:
					return i18n.getString("LeastSquaresSettingDialog.estimationtype.l1norm.label", "Robust estimation (L1-Norm)");
				case L2NORM:
					return i18n.getString("LeastSquaresSettingDialog.estimationtype.l2norm.label", "Least-squares adjustment (L2-Norm)");
				case SIMULATION:
					return i18n.getString("LeastSquaresSettingDialog.estimationtype.simulation.label", "Simulation (Pre-analysis)");
				case MODIFIED_UNSCENTED_TRANSFORMATION:
					return i18n.getString("LeastSquaresSettingDialog.estimationtype.mut.label", "Modified unscented transformation (MUT)");
				case SPHERICAL_SIMPLEX_UNSCENTED_TRANSFORMATION:
					return i18n.getString("LeastSquaresSettingDialog.estimationtype.sut.label", "Spherical simplex unscented transformation (SUT)");
				}
				return null;
			}

			@Override
			public EstimationType fromString(String string) {
				return EstimationType.valueOf(string);
			}
		});
		typeComboBox.setTooltip(new Tooltip(tooltip));
		typeComboBox.setMinWidth(150);
		typeComboBox.setMaxWidth(Double.MAX_VALUE);
		return typeComboBox;
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
  
	private DoubleTextField createDoubleTextField(double value, ValueSupport valueSupport, double lowerBoundary, double upperBoundary, String tooltip) {
		DoubleTextField field = new DoubleTextField(value, CellValueType.STATISTIC, Boolean.FALSE, valueSupport, lowerBoundary, upperBoundary);
		field.setTooltip(new Tooltip(tooltip));
		field.setAlignment(Pos.CENTER_RIGHT);
		field.setMinHeight(Control.USE_PREF_SIZE);
		field.setMaxHeight(Double.MAX_VALUE);
		return field;
	}
	
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
	
	private DoubleSpinner createDoubleSpinner(CellValueType cellValueType, double min, double max, double amountToStepBy, String tooltip) {
		DoubleSpinner doubleSpinner = new DoubleSpinner(cellValueType, min, max, amountToStepBy);
		doubleSpinner.setMinWidth(75);
		doubleSpinner.setPrefWidth(100);
		doubleSpinner.setMaxWidth(Double.MAX_VALUE);
		doubleSpinner.setTooltip(new Tooltip(tooltip));
		return doubleSpinner;
	}
		
	private void save() {
		try {
			this.settings.setConfidenceLevel(this.confidenceLevelSpinner.getNumber().doubleValue());
			SQLManager.getInstance().save(this.settings);
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("LeastSquaresSettingDialog.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("LeastSquaresSettingDialog.message.error.save.exception.header", "Error, could not save least-squares settings to database."),
							i18n.getString("LeastSquaresSettingDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
	
	private void load() {
		try {
			SQLManager.getInstance().load(this.settings);
			
			SpinnerValueFactory.DoubleSpinnerValueFactory confidenceLevelSpinnerFactory = (SpinnerValueFactory.DoubleSpinnerValueFactory)this.confidenceLevelSpinner.getValueFactory();
			double confidenceLevel = this.settings.getConfidenceLevel();

			confidenceLevel = Math.max(Math.min(this.options.convertPercentToView(confidenceLevel), confidenceLevelSpinnerFactory.getMax()), confidenceLevelSpinnerFactory.getMin());
			confidenceLevelSpinnerFactory.setValue(confidenceLevel);
			
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("LeastSquaresSettingDialog.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("LeastSquaresSettingDialog.message.error.load.exception.header", "Error, could not load least-squares settings from database."),
							i18n.getString("LeastSquaresSettingDialog.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
	
	public static void setEnableUnscentedTransformation(boolean enable) {
		leastSquaresSettingDialog.enableUnscentedTransformation = enable;
	}
	
	@Override
	public void formatterChanged(FormatterEvent evt) {
		if (evt != null && evt.getCellType() == CellValueType.PERCENTAGE && evt.getEventType() == FormatterEventType.UNIT_CHANGED) {
			String frmPercentUnit        = this.options.getFormatterOptions().get(CellValueType.PERCENTAGE).getUnit().toFormattedAbbreviation();
			String labelConfidenceLevel  = String.format(Locale.ENGLISH, "%s%s:", i18n.getString("LeastSquaresSettingDialog.confidence.label", "Confidence level 1 - \u03B1"), frmPercentUnit.isBlank() ? "" : " " + frmPercentUnit);

			this.confidenceLevelLabel.setText(labelConfidenceLevel);
		}
	}
}
