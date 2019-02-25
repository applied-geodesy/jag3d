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
import java.util.Locale;
import java.util.Optional;

import org.applied_geodesy.adjustment.DefaultValue;
import org.applied_geodesy.adjustment.EstimationType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.i18.I18N;

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
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class LeastSquaresSettingDialog {

	public class LeastSquaresSettings {
		private ObjectProperty<Integer> iteration             = new SimpleObjectProperty<Integer>(50);
		private ObjectProperty<Integer> principalComponents   = new SimpleObjectProperty<Integer>(1);
		private ObjectProperty<Double> robustEstimationLimit  = new SimpleObjectProperty<Double>(DefaultValue.getRobustEstimationLimit());
		private BooleanProperty orientation                   = new SimpleBooleanProperty(Boolean.TRUE);
		private BooleanProperty congruenceAnalysis            = new SimpleBooleanProperty(Boolean.FALSE);
		private BooleanProperty applyVarianceOfUnitWeight     = new SimpleBooleanProperty(Boolean.TRUE);
		private BooleanProperty exportCovarianceMatrix        = new SimpleBooleanProperty(Boolean.FALSE);
		private ObjectProperty<EstimationType> estimationType = new SimpleObjectProperty<EstimationType>(EstimationType.L2NORM);

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

		public BooleanProperty exportCovarianceMatrixProperty() {
			return this.exportCovarianceMatrix;
		}
		
		public boolean isExportCovarianceMatrix() {
			return this.exportCovarianceMatrixProperty().get();
		}
		
		public void setExportCovarianceMatrix(final boolean exportCovarianceMatrix) {
			this.exportCovarianceMatrixProperty().set(exportCovarianceMatrix);
		}
	}
	
	private class EstimationTypeChangeListener implements ChangeListener<EstimationType> {
		@Override
		public void changed(ObservableValue<? extends EstimationType> observable, EstimationType oldValue, EstimationType newValue) {
			settings.setEstimationType(newValue);
			estimationTypeComboBox.setValue(newValue);
		}
	}

	private I18N i18n = I18N.getInstance();
	private static LeastSquaresSettingDialog leastSquaresSettingDialog = new LeastSquaresSettingDialog();
	private FormatterOptions options = FormatterOptions.getInstance();
	private Dialog<LeastSquaresSettings> dialog = null;
	private Window window;
	private ComboBox<EstimationType> estimationTypeComboBox;
	private LeastSquaresSettings settings = new LeastSquaresSettings();
	private Spinner<Integer> iterationSpinner;
	private Spinner<Integer> principalComponentSpinner;
	private Spinner<Double> robustSpinner;
	private CheckBox orientationApproximationCheckBox, congruenceAnalysisCheckBox, applyVarianceOfUnitWeightCheckBox, exportCovarianceMatrixCheckBox;
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
				}
				return null;
			}
		});
	}
	
	private Node createPane() {
		this.estimationTypeComboBox = this.createEstimationTypeComboBox(EstimationType.L2NORM, i18n.getString("LeastSquaresSettingDialog.estimationtype.tooltip", "Set estimation method")); 
		
		Label iterationLabel = new Label(i18n.getString("LeastSquaresSettingDialog.iterations.label", "Maximum number of iterations:"));
		this.iterationSpinner = this.createIntegerSpinner(0, DefaultValue.getMaximalNumberOfIterations(), 10, i18n.getString("LeastSquaresSettingDialog.iterations.tooltip", "Set maximum permissible iteration value"));
		iterationLabel.setLabelFor(this.iterationSpinner);
		iterationLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label principalComponentLabel = new Label(i18n.getString("LeastSquaresSettingDialog.principal_components.label", "Number of principal components:"));
		this.principalComponentSpinner = this.createIntegerSpinner(0, Integer.MAX_VALUE, 1, i18n.getString("LeastSquaresSettingDialog.principal_components.tooltip", "Set number of principal components to be estimated"));
		principalComponentLabel.setLabelFor(this.principalComponentSpinner);
		principalComponentLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

		Label robustLabel = new Label(i18n.getString("LeastSquaresSettingDialog.robust.label", "Robust estimation limit:"));
		this.robustSpinner = this.createDoubleSpinner(1.5, Math.max(DefaultValue.getRobustEstimationLimit(), 6.0), 0.5, i18n.getString("LeastSquaresSettingDialog.robust.tooltip", "Set robust estimation limit of BIBER estimator"));
		robustLabel.setLabelFor(this.robustSpinner);
		robustLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
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
		
		this.exportCovarianceMatrixCheckBox = this.createCheckBox(
				i18n.getString("LeastSquaresSettingDialog.covariance.label", "Export variance-covariance matrix"),
				i18n.getString("LeastSquaresSettingDialog.covariance.tooltip", "If checked, variance-covariance matrix will be exported to the working directory")
		);
		
		this.exportCovarianceMatrixCheckBox.selectedProperty().bindBidirectional(this.settings.exportCovarianceMatrixProperty());
		this.orientationApproximationCheckBox.selectedProperty().bindBidirectional(this.settings.orientationProperty());
		this.congruenceAnalysisCheckBox.selectedProperty().bindBidirectional(this.settings.congruenceAnalysisProperty());
		this.applyVarianceOfUnitWeightCheckBox.selectedProperty().bindBidirectional(this.settings.applyVarianceOfUnitWeightProperty());
		this.iterationSpinner.getValueFactory().valueProperty().bindBidirectional(this.settings.iterationProperty());
		this.principalComponentSpinner.getValueFactory().valueProperty().bindBidirectional(this.settings.principalComponentsProperty());
		this.robustSpinner.getValueFactory().valueProperty().bindBidirectional(this.settings.robustEstimationLimitProperty());
		
		this.settings.estimationTypeProperty().addListener(new EstimationTypeChangeListener());
		this.estimationTypeComboBox.getSelectionModel().selectedItemProperty().addListener(new EstimationTypeChangeListener());
		
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		//gridPane.setMinWidth(300);
		gridPane.setHgap(20);
		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(5,15,5,15)); // oben, recht, unten, links
		//gridPane.setGridLinesVisible(true);
		
		GridPane.setHgrow(iterationLabel, Priority.NEVER);
		GridPane.setHgrow(principalComponentLabel, Priority.NEVER);
		GridPane.setHgrow(robustLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.orientationApproximationCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.applyVarianceOfUnitWeightCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.congruenceAnalysisCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.exportCovarianceMatrixCheckBox, Priority.ALWAYS);
		
		GridPane.setHgrow(this.robustSpinner, Priority.ALWAYS);
		GridPane.setHgrow(this.principalComponentSpinner, Priority.ALWAYS);
		GridPane.setHgrow(this.iterationSpinner, Priority.ALWAYS);
		
		int row = 0;
		gridPane.add(this.estimationTypeComboBox, 0, ++row, 2, 1);

		gridPane.add(this.applyVarianceOfUnitWeightCheckBox, 0, ++row, 2, 1);
		gridPane.add(this.orientationApproximationCheckBox,  0, ++row, 2, 1);

		gridPane.add(iterationLabel,        0, ++row);
		gridPane.add(this.iterationSpinner, 1,   row);

		gridPane.add(robustLabel,        0, ++row);
		gridPane.add(this.robustSpinner, 1,   row);
		
		gridPane.add(principalComponentLabel,        0, ++row);
		gridPane.add(this.principalComponentSpinner, 1,   row);

		gridPane.add(this.congruenceAnalysisCheckBox,     0, ++row, 2, 1);
		gridPane.add(this.exportCovarianceMatrixCheckBox, 0, ++row, 2, 1);

		Platform.runLater(new Runnable() {
			@Override public void run() {
				estimationTypeComboBox.requestFocus();
			}
		});
		
		return gridPane;
	}
	
	
	private ComboBox<EstimationType> createEstimationTypeComboBox(EstimationType item, String tooltip) {
		ComboBox<EstimationType> typeComboBox = new ComboBox<EstimationType>();
		typeComboBox.getItems().setAll(EstimationType.values());
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
	
	private void save() {
		try {
			SQLManager.getInstance().save(this.settings);
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("FormatterOptionDialog.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("FormatterOptionDialog.message.error.save.exception.header", "Error, could not save least-squares settings to database."),
							i18n.getString("FormatterOptionDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
	
	private void load() {
		try {
			SQLManager.getInstance().load(this.settings);
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("ProjectionDialog.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("ProjectionDialog.message.error.load.exception.header", "Error, could not load least-squares settings from database."),
							i18n.getString("ProjectionDialog.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
}
