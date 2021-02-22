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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterOptions;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class AnalysisChartsDialog {
	
	private class TickFormatChangedListener implements FormatterChangedListener {
		@Override
		public void formatterChanged(FormatterEvent evt) {
			if (evt.getCellType() == CellValueType.PERCENTAGE)
				updateTickLabels(histogramChart);
		}
	}

	private class TerrestrialObservationTypeChangeListener implements ChangeListener<TerrestrialObservationType> {
		@Override
		public void changed(ObservableValue<? extends TerrestrialObservationType> observable, TerrestrialObservationType oldValue, TerrestrialObservationType newValue) {
			if (newValue != null)
				updateChartData(newValue);
		}
	}
	
	private class ProbabilityDensityFunctionChangeListener implements ChangeListener<Boolean> {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			TerrestrialObservationType type = terrestrialObservationTypeComboBox.getValue();
			updateChartData(type);
		}
	}
	
	private enum TerrestrialObservationType {
		ALL(null),
		LEVELING(ObservationType.LEVELING),
		DIRECTION(ObservationType.DIRECTION),
		HORIZONTAL_DISTANCE(ObservationType.HORIZONTAL_DISTANCE),
		SLOPE_DISTANCE(ObservationType.SLOPE_DISTANCE),
		ZENITH_ANGLE(ObservationType.ZENITH_ANGLE),
		;
		
		private ObservationType observationType;
		private TerrestrialObservationType(ObservationType observationType) {
			this.observationType = observationType;
		}

		public final ObservationType getObservationType() {
			return observationType;
		}

		public static TerrestrialObservationType getEnumByValue(ObservationType observationType) {
			for(TerrestrialObservationType element : TerrestrialObservationType.values()) {
				if(element.observationType != null && element.observationType == observationType)
					return element;
			}
			return null;
		}  
	}

	private I18N i18n = I18N.getInstance();
	private FormatterOptions options = FormatterOptions.getInstance();
	private static AnalysisChartsDialog analysisChartsDialog = new AnalysisChartsDialog();
	private Dialog<Void> dialog = null;
	private Window window;
	private ComboBox<TerrestrialObservationType> terrestrialObservationTypeComboBox;
	private CheckBox gaussianProbabilityDensityFunctionCheckBox, kernelDensityEstimationCheckBox;
	private AreaChart<Number,Number> histogramChart;
	private AnalysisChartsDialog() {}

	public static void setOwner(Window owner) {
		analysisChartsDialog.window = owner;
	}

	public static Optional<Void> showAndWait() {
		analysisChartsDialog.init();
		analysisChartsDialog.load();
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	try {
            		analysisChartsDialog.dialog.getDialogPane().requestLayout();
            		Stage stage = (Stage) analysisChartsDialog.dialog.getDialogPane().getScene().getWindow();
            		stage.sizeToScene();
            	} 
            	catch (Exception e) {
            		e.printStackTrace();
            	}
            }
		});
		return analysisChartsDialog.dialog.showAndWait();
	}


	private void init() {
		if (this.dialog != null)
			return;
		
		this.dialog = new Dialog<Void>();
		this.dialog.setTitle(i18n.getString("ResidualAnalysisDialog.title", "Analysis charts"));
		this.dialog.setHeaderText(i18n.getString("ResidualAnalysisDialog.chart.histogram.header", "Histogram of normalized residuals"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
	}
	
	private Node createPane() {
		BorderPane borderPane = new BorderPane();
				
		this.gaussianProbabilityDensityFunctionCheckBox = this.createCheckBox(i18n.getString("ResidualAnalysisDialog.histogram.probability_density_function.label", "Gaussian probability density function"), i18n.getString("ResidualAnalysisDialog.histogram.probability_density_function.tooltip", "If selected, the probability density function of the standard normal distribution is estimated"));
		this.kernelDensityEstimationCheckBox = this.createCheckBox(i18n.getString("ResidualAnalysisDialog.histogram.kernel_density_estimation.label", "Kernel density estimation"), i18n.getString("ResidualAnalysisDialog.histogram.kernel_density_estimation.tooltip", "If selected, the probability density function is estimated by a kernel density estimation"));
		this.gaussianProbabilityDensityFunctionCheckBox.setSelected(true);
		this.kernelDensityEstimationCheckBox.setSelected(true);
		HBox cbNode = new HBox(10);
		Region regionLeft  = new Region();
		Region regionRight = new Region();
        HBox.setHgrow(regionLeft,  Priority.ALWAYS);
        HBox.setHgrow(regionRight, Priority.ALWAYS);
		cbNode.setPadding(new Insets(5, 10, 5, 10));
		cbNode.getChildren().addAll(regionLeft, this.gaussianProbabilityDensityFunctionCheckBox, this.kernelDensityEstimationCheckBox, regionRight);
		ProbabilityDensityFunctionChangeListener probabilityDensityFunctionChangeListener = new ProbabilityDensityFunctionChangeListener();
		this.gaussianProbabilityDensityFunctionCheckBox.selectedProperty().addListener(probabilityDensityFunctionChangeListener);
		this.kernelDensityEstimationCheckBox.selectedProperty().addListener(probabilityDensityFunctionChangeListener);
		
		this.terrestrialObservationTypeComboBox = this.createTerrestrialObservationTypeComboBox(TerrestrialObservationType.ALL, i18n.getString("ResidualAnalysisDialog.observationtype.terrestrial.tooltip", "Set observational residual type"));
		Region spacer = new Region();
		HBox hbox = new HBox(10);
		hbox.setPadding(new Insets(5, 10, 5, 15));
		HBox.setHgrow(spacer, Priority.ALWAYS);
		hbox.getChildren().addAll(spacer, this.terrestrialObservationTypeComboBox);
		
		this.histogramChart = this.createHistogramChart();
		
		borderPane.setTop(hbox);
		borderPane.setCenter(this.histogramChart);
		borderPane.setBottom(cbNode);
		
		this.terrestrialObservationTypeComboBox.getSelectionModel().selectedItemProperty().addListener(new TerrestrialObservationTypeChangeListener());
		this.options.addFormatterChangedListener(new TickFormatChangedListener());
		
		return borderPane;
	}
	
	private AreaChart<Number,Number> createHistogramChart() {
		NumberAxis xAxis = new NumberAxis(-5, 5, 1);
		NumberAxis yAxis = new NumberAxis(0, 0.45, 0.1);
        
        xAxis.setLabel(i18n.getString("ResidualAnalysisDialog.chart.histogram.axis.x.label", "Normalized residuals"));
        xAxis.setForceZeroInRange(true);
        xAxis.setMinorTickVisible(false);
        xAxis.setAnimated(false);
        xAxis.setAutoRanging(false);
        
        yAxis.setLabel(String.format(Locale.ENGLISH, i18n.getString("ResidualAnalysisDialog.chart.histogram.axis.y.label", "Probability density [%s]"), options.getFormatterOptions().get(CellValueType.PERCENTAGE).getUnit().getAbbreviation()));
        yAxis.setForceZeroInRange(true);
        yAxis.setMinorTickVisible(false);
        yAxis.setAnimated(false);
        yAxis.setAutoRanging(false);

        AreaChart<Number,Number> areaChart = new AreaChart<Number,Number>(xAxis, yAxis); 
        areaChart.setLegendVisible(false);
        areaChart.setAnimated(false);
        areaChart.setCreateSymbols(false);
        areaChart.setVerticalZeroLineVisible(false);
        areaChart.setPadding(new Insets(0, 0, 0, 0));
        
        this.updateTickLabels(areaChart);

        return areaChart;
	}
	
	private void updateTickLabels(AreaChart<Number,Number> areaChart) {
		NumberAxis yAxis = (NumberAxis)areaChart.getYAxis();
		yAxis.setLabel(String.format(Locale.ENGLISH, i18n.getString("ResidualAnalysisDialog.chart.histogram.axis.y.label", "Probability density [%s]"), options.getFormatterOptions().get(CellValueType.PERCENTAGE).getUnit().getAbbreviation()));
		
		yAxis.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number number) {
				return options.toPercentFormat(number.doubleValue(), false);
			}

			@Override
			public Number fromString(String string) {
				return null;
			}
		});
	}
	
	private ComboBox<TerrestrialObservationType> createTerrestrialObservationTypeComboBox(TerrestrialObservationType item, String tooltip) {
		ComboBox<TerrestrialObservationType> typeComboBox = new ComboBox<TerrestrialObservationType>();
		TerrestrialObservationType[] terrestrialObservationTypeArray = TerrestrialObservationType.values();
		
		typeComboBox.getItems().setAll(terrestrialObservationTypeArray);
		typeComboBox.getSelectionModel().select(item);
		typeComboBox.setConverter(new StringConverter<TerrestrialObservationType>() {

			@Override
			public String toString(TerrestrialObservationType type) {
				if (type == null)
					return null;
				switch(type) {
				case ALL:
					return i18n.getString("ResidualAnalysisDialog.observationtype.terrestrial.all.label", "All terrestrial observations");
				case LEVELING:
					return i18n.getString("ResidualAnalysisDialog.observationtype.terrestrial.leveling.label", "Leveling");
				case DIRECTION:
					return i18n.getString("ResidualAnalysisDialog.observationtype.terrestrial.direction.label", "Directions");
				case HORIZONTAL_DISTANCE:
					return i18n.getString("ResidualAnalysisDialog.observationtype.terrestrial.horizontal_distance.label", "Horizontal distances");
				case SLOPE_DISTANCE:
					return i18n.getString("ResidualAnalysisDialog.observationtype.terrestrial.slope_distance.label", "Slope distances");
				case ZENITH_ANGLE:
					return i18n.getString("ResidualAnalysisDialog.observationtype.terrestrial.zenith_angle.label", "Zenith angles");
				}
				return null;
			}

			@Override
			public TerrestrialObservationType fromString(String string) {
				return TerrestrialObservationType.valueOf(string);
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
	
	private void updateChartData(TerrestrialObservationType terrestrialObservationType) {
		try {
			terrestrialObservationType = terrestrialObservationType == null ? TerrestrialObservationType.ALL : terrestrialObservationType;
			ObservationType[] observationTypes = this.getSelectedObservationTypes(terrestrialObservationType);
			List<Double> normalizedResiduals = SQLManager.getInstance().getNormalizedResiduals(observationTypes);

			this.histogramChart.getData().clear();

			if (normalizedResiduals == null || normalizedResiduals.isEmpty())
				return;
			
			// Sort data
			Collections.sort(normalizedResiduals);

			double xRange = 0, yRange = 0; 
			double range[] = this.plotHistogram(this.histogramChart, normalizedResiduals);
			xRange = range[0];
			yRange = range[1];
			
			if (this.gaussianProbabilityDensityFunctionCheckBox.isSelected()) {
				range = this.plotGaussianProbabilityDensityFunction(this.histogramChart);
				xRange = Math.max(xRange, range[0]);
				yRange = Math.max(yRange, range[1]);
			}
			
			if (this.kernelDensityEstimationCheckBox.isSelected()) {
				range = this.plotKernelDensityEstimation(this.histogramChart, normalizedResiduals);
				xRange = Math.max(xRange, range[0]);
				yRange = Math.max(yRange, range[1]);
			}
			
			xRange = Math.ceil(xRange + 0.05);
			yRange = Math.ceil((yRange + 0.005) * 10) / 10;

			NumberAxis xAxis = (NumberAxis)this.histogramChart.getXAxis();
			NumberAxis yAxis = (NumberAxis)this.histogramChart.getYAxis();
			
			xAxis.setLowerBound(-xRange);
			xAxis.setUpperBound(+xRange);

			yAxis.setLowerBound(0);
			yAxis.setUpperBound(yRange);

		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("ResidualAnalysisDialog.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("ResidualAnalysisDialog.message.error.load.exception.header", "Error, could not load data from database."),
							i18n.getString("ResidualAnalysisDialog.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
	
	private double[] plotHistogram(AreaChart<Number,Number> areaChart, List<Double> data) {
		int length = data.size();
		
		double iqr = this.getInterQuartileRange(data);
		double binWidth = 2.0 * iqr * Math.pow(length, -1.0/3.0);
		double minValue = data.get(0);
		double maxValue = data.get(length - 1);
		int numberOfBins = Math.max((int)Math.ceil((maxValue - minValue) / binWidth), 1);

		int bins[] = new int[numberOfBins];

		for (int i = 0, j = 0; i < length; i++) {
			if (data.get(i) <= (minValue + (j+1) * binWidth) && j < numberOfBins) {
				bins[j]++;
			}
			else {
				do {
					j++;
				}
				while (!(data.get(i) <= (minValue + (j+1) * binWidth) && j < numberOfBins));
				
				if (j < numberOfBins)
					bins[j]++;
			}
		}

		double yRange = 0.0;
		
		for (int i=0; i<bins.length; i++) {       	
			if (bins[i] == 0)
				continue;

			double width = minValue + i * binWidth;
			double pdf   = (double)bins[i]/(double)length/binWidth;
			XYChart.Series<Number, Number> bar = new XYChart.Series<Number, Number>();
			bar.getData().add(new XYChart.Data<Number, Number>(width,            0));
			bar.getData().add(new XYChart.Data<Number, Number>(width,            pdf));
			bar.getData().add(new XYChart.Data<Number, Number>(width + binWidth, pdf));
			bar.getData().add(new XYChart.Data<Number, Number>(width + binWidth, 0));

			this.histogramChart.getData().add(bar);
			Node line = bar.getNode().lookup(".chart-series-area-line");
			if (line != null)
				line.setStyle("-fx-stroke: rgba(75, 75, 75, 1); -fx-stroke-width: 1.0px;");  
			Node area = bar.getNode().lookup(".chart-series-area-fill");
			if (area != null)
				area.setStyle("-fx-fill: rgba(75, 75, 75, 0.25);");

			yRange = Math.max(yRange, pdf);
		}
		
		double xRange = Math.max(Math.abs(minValue), Math.abs(maxValue + binWidth));
		
		return new double[] {xRange, yRange};
	}
	
	private double[] plotGaussianProbabilityDensityFunction(AreaChart<Number,Number> areaChart) {
		double xRange = 0, yRange = 0;
		XYChart.Series<Number, Number> probabilityDensity = new XYChart.Series<Number, Number>();
		for (double x = -3.9; x <= 3.9; x += 0.01) {
			double pdf = this.getStandardGaussian(x);
			probabilityDensity.getData().add(new XYChart.Data<Number, Number>(x,  pdf));
			xRange = Math.max(xRange, Math.abs(x));
			yRange = Math.max(yRange, Math.abs(pdf));
		}

		areaChart.getData().add(probabilityDensity);
		
		Node line = probabilityDensity.getNode().lookup(".chart-series-area-line"); 
		if (line != null)
			line.setStyle("-fx-stroke: rgba(200, 0, 0, 1); -fx-stroke-width: 2.5px;");  
		Node area = probabilityDensity.getNode().lookup(".chart-series-area-fill");
		if (area != null)
			area.setStyle("-fx-fill: rgba(200, 0, 0, 0);"); 
		
		return new double[] {xRange, yRange};
	}
	
	private double[] plotKernelDensityEstimation(AreaChart<Number,Number> areaChart, List<Double> data) {
		double iqr = this.getInterQuartileRange(data);
		int length = data.size();
		double mean = length > 1 ? this.getMean(data) : 0;
		double std  = length > 1 ? Math.sqrt(this.getVariance(data, mean)) : 1.0;		
		double minValue = data.get(0);
		double maxValue = data.get(length - 1);
		double xRange = 0, yRange = 0;
		double bandWidth = 0.9 * Math.min(std, iqr / 1.34897950039216) * Math.pow(length, -1.0/5.0); // 1.34... == norminv(0.75)*2


		XYChart.Series<Number, Number> kernelEstimation = new XYChart.Series<Number, Number>();
		
		double t = mean;
		double yt = Double.MAX_VALUE;
		double inc = 0.01;
		int cnt = 0;
		do {
			yt = 0;
			t = mean - (cnt++) * inc;  
			for (int j = 0; j < length; j++) {
				double x = (t - (data.get(j))) /  bandWidth;
				double pdf = this.getStandardGaussian(x);
				yt += pdf;
			}
			yt = 1.0/length/bandWidth * yt;
			kernelEstimation.getData().add(new XYChart.Data<Number, Number>(t,  yt));
			xRange = Math.max(xRange, Math.abs(t));
			yRange = Math.max(yRange, Math.abs(yt));	
		} 
		while( Math.abs(yt) > 0.0005 || t > minValue );
		
		t = mean;
		yt = Double.MAX_VALUE;
		cnt = 0;
		do {
			yt = 0;
			t = mean + (++cnt) * inc;  
			for (int j = 0; j < length; j++) {
				double x = (t - (data.get(j))) /  bandWidth;
				double pdf = this.getStandardGaussian(x);
				yt += pdf;
			}
			yt = 1.0/length/bandWidth * yt;
			kernelEstimation.getData().add(new XYChart.Data<Number, Number>(t,  yt));
			xRange = Math.max(xRange, Math.abs(t));
			yRange = Math.max(yRange, Math.abs(yt));	
		} 
		while( Math.abs(yt) > 0.0005 || t < maxValue);
				
		areaChart.getData().add(kernelEstimation);
		if (kernelEstimation.getNode() != null) {
			Node line = kernelEstimation.getNode().lookup(".chart-series-area-line");
			line.setStyle("-fx-stroke: rgba(0, 0, 150, 1); -fx-stroke-width: 2.5px; -fx-stroke-dash-array: 10 7 10 7;");  
			Node area = kernelEstimation.getNode().lookup(".chart-series-area-fill");
			area.setStyle("-fx-fill: rgba(0, 0, 0, 0);"); 
		}
		return new double[] {xRange, yRange};
	}
		
	private ObservationType[] getSelectedObservationTypes(TerrestrialObservationType terrestrialObservationType) {
		ObservationType observationTypes[];
		if (terrestrialObservationType == TerrestrialObservationType.ALL) {
			observationTypes = new ObservationType[] {
					ObservationType.LEVELING,
					ObservationType.DIRECTION,
					ObservationType.HORIZONTAL_DISTANCE,
					ObservationType.SLOPE_DISTANCE,
					ObservationType.ZENITH_ANGLE
			};
		}
		else {
			observationTypes = new ObservationType[] {
					terrestrialObservationType.getObservationType()	
			};
		}
		return observationTypes;
	}
	
	private void load() {
		try {
			
			List<ObservationType> projectObservationTypes = SQLManager.getInstance().getProjectObservationTypes();
			TerrestrialObservationType[] terrestrialObservationTypeArray = new TerrestrialObservationType[projectObservationTypes.size() == 1 ? 1 : projectObservationTypes.size() + 1];
			terrestrialObservationTypeArray[0] = TerrestrialObservationType.ALL;

			if (projectObservationTypes.size() > 1) {
				int idx = 1;
				for (ObservationType obsType : projectObservationTypes) {
					TerrestrialObservationType type = TerrestrialObservationType.getEnumByValue(obsType);
					if (type != null)
						terrestrialObservationTypeArray[idx++] = type;
				}
			}

			this.terrestrialObservationTypeComboBox.getSelectionModel().clearSelection();
			this.terrestrialObservationTypeComboBox.getItems().setAll(terrestrialObservationTypeArray);
			this.terrestrialObservationTypeComboBox.getSelectionModel().select(TerrestrialObservationType.ALL);
			
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("ResidualAnalysisDialog.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("ResidualAnalysisDialog.message.error.load.exception.header", "Error, could not load data from database."),
							i18n.getString("ResidualAnalysisDialog.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
	
	private double getInterQuartileRange(List<Double> values) {
    	double n = values.size();
    	double q1 = values.get((int)Math.floor(0.25 * n));
    	double q3 = values.get((int)Math.floor(0.75 * n));
    	
		return q3 - q1;
    }
	
	private double getStandardGaussian(double x) {
		final double fac = 1.0/Math.sqrt(2.0*Math.PI);
		return fac * Math.exp(-0.5 * x*x);
	}
	
	private double getVariance(List<Double> values, double mean) {
		double var = 0;
		double n = values.size();

		for (Double value : values) {
			double res = value.doubleValue() - mean;
			var += res * res;
		}
		return var / n;
	}
	
	private double getMean(List<Double> values) {
		double mean = 0;
		double n = values.size();
		for (Double value : values) {
			mean += value.doubleValue();
		}
		return mean = n > 0 ? mean / n : 0;
	}
}
