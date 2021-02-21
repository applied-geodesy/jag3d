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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
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
		
		this.terrestrialObservationTypeComboBox = this.createTerrestrialObservationTypeComboBox(TerrestrialObservationType.ALL, i18n.getString("ResidualAnalysisDialog.observationtype.terrestrial.tooltip", "Set observational residual type"));
		Region spacer = new Region();
		HBox hbox = new HBox(10);
		hbox.setPadding(new Insets(5, 10, 5, 15));
		HBox.setHgrow(spacer, Priority.ALWAYS);
		hbox.getChildren().addAll(spacer, this.terrestrialObservationTypeComboBox);
		
		this.histogramChart = this.createHistogramChart();
		
		borderPane.setTop(hbox);
		borderPane.setCenter(this.histogramChart);
		
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
	
	private void updateChartData(TerrestrialObservationType terrestrialObservationType) {
		try {
			terrestrialObservationType = terrestrialObservationType == null ? TerrestrialObservationType.ALL : terrestrialObservationType;
			ObservationType[] observationTypes = this.getSelectedObservationTypes(terrestrialObservationType);
			List<Double> normalizedResiduals = SQLManager.getInstance().getNormalizedResiduals(observationTypes);

			this.histogramChart.getData().clear();

			if (normalizedResiduals == null || normalizedResiduals.isEmpty())
				return;

			int length = normalizedResiduals.size();
			Collections.sort(normalizedResiduals);

			double binWidth = this.getBinWidth(normalizedResiduals);
			double minValue = normalizedResiduals.get(0);
			double maxValue = normalizedResiduals.get(length - 1);
			int numberOfBins = Math.max((int)Math.ceil((maxValue - minValue) / binWidth), 1);

			int bins[] = new int[numberOfBins];

			for (int i = 0, j = 0; i < length; i++) {
				if (normalizedResiduals.get(i) <= (minValue + (j+1) * binWidth) && j < numberOfBins) {
					bins[j]++;
				}
				else {
					do {
						j++;
					}
					while (!(normalizedResiduals.get(i) <= (minValue + (j+1) * binWidth) && j < numberOfBins));
					
					if (j < numberOfBins)
						bins[j]++;
				}
			}

			double xRange = Math.ceil(Math.max(4, Math.max(Math.abs(minValue), Math.abs(maxValue))) + 0.5);
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
				line.setStyle("-fx-stroke: rgba(75, 75, 75, 1); -fx-stroke-width: 1.0px;");  
				Node area = bar.getNode().lookup(".chart-series-area-fill");
				area.setStyle("-fx-fill: rgba(75, 75, 75, 0.25);");

				yRange = Math.max(yRange, pdf);
			}

			XYChart.Series<Number, Number> probabilityDensity = new XYChart.Series<Number, Number>();
			final double fac = 1.0/Math.sqrt(2.0*Math.PI);
			for (double i = -4; i <= 4; i += 0.01) {
				double pdf = fac * Math.exp(-0.5 * i*i);
				probabilityDensity.getData().add(new XYChart.Data<Number, Number>(i,  pdf));
				yRange = Math.max(yRange, pdf);
			}
			yRange = Math.ceil(yRange * 10) / 10;
			this.histogramChart.getData().add(probabilityDensity);
			
			Node line = probabilityDensity.getNode().lookup(".chart-series-area-line"); 
			line.setStyle("-fx-stroke: rgba(200, 0, 0, 1); -fx-stroke-width: 2.5px;");  
			Node area = probabilityDensity.getNode().lookup(".chart-series-area-fill");
			area.setStyle("-fx-fill: rgba(200, 0, 0, 0);"); 
			
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
			TerrestrialObservationType[] terrestrialObservationTypeArray = new TerrestrialObservationType[projectObservationTypes.size() + 1];
			terrestrialObservationTypeArray[0] = TerrestrialObservationType.ALL;
			int idx = 1;
			for (ObservationType obsType : projectObservationTypes) {
				TerrestrialObservationType type = TerrestrialObservationType.getEnumByValue(obsType);
				if (type != null)
					terrestrialObservationTypeArray[idx++] = type;
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
	
	private double getInterquartileRange(List<Double> values) {
    	double n = values.size();
    	double q1 = values.get((int)Math.floor(0.25 * n));
    	double q3 = values.get((int)Math.floor(0.75 * n));
    	
		return q3 - q1;
    }
    
	private double getBinWidth(List<Double> values) {
    	double iqr = this.getInterquartileRange(values);
    	double n = values.size();
    	return 2.0 * iqr / Math.pow(n, 1.0/3.0);
    }
}
