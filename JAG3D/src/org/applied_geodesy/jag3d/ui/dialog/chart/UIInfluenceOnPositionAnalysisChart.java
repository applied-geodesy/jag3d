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

package org.applied_geodesy.jag3d.ui.dialog.chart;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlight;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightRangeType;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightType;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.util.FormatterOptions;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public class UIInfluenceOnPositionAnalysisChart {

	private class TerrestrialObservationTypeChangeListener implements ChangeListener<TerrestrialObservationType> {
		@Override
		public void changed(ObservableValue<? extends TerrestrialObservationType> observable, TerrestrialObservationType oldValue, TerrestrialObservationType newValue) {
			if (newValue != null)
				updateChartData(newValue);
		}
	}
	
	private I18N i18n = I18N.getInstance();
	private TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
	
	private FormatterOptions options = FormatterOptions.getInstance();
	private static UIInfluenceOnPositionAnalysisChart analysisChart = new UIInfluenceOnPositionAnalysisChart();
	private Node analysisChartNode;
	private Map<TableRowHighlightRangeType, Integer> chartData = new HashMap<TableRowHighlightRangeType, Integer>(0);
	private final String INADEQUATE   = "INADEQUATE";   // TableRowHighlightRangeType.INADEQUATE.name()
	private final String SATISFACTORY = "SATISFACTORY";
	private final String EXCELLENT    = "EXCELLENT";
	
	private ComboBox<TerrestrialObservationType> terrestrialObservationTypeComboBox;
	private PieChart pieChart;

	private UIInfluenceOnPositionAnalysisChart() {}

	public static UIInfluenceOnPositionAnalysisChart getInstance() {
		analysisChart.init();
		return analysisChart;
	}

	public Node getNode() {
		return this.analysisChartNode;
	}

	private void init() {
		if (this.analysisChartNode != null)
			return;

		BorderPane borderPane = new BorderPane();

		this.terrestrialObservationTypeComboBox = this.createTerrestrialObservationTypeComboBox(TerrestrialObservationType.ALL, i18n.getString("UIInfluenceOnPositionAnalysisChart.observationtype.terrestrial.tooltip", "Set observational type"));
		Region spacer = new Region();
		HBox hbox = new HBox(10);
		hbox.setPadding(new Insets(5, 10, 5, 15));
		HBox.setHgrow(spacer, Priority.ALWAYS);
		hbox.getChildren().addAll(spacer, this.terrestrialObservationTypeComboBox);

		this.pieChart = this.createPieChart();

		borderPane.setTop(hbox);
		borderPane.setCenter(this.pieChart);
		borderPane.setPrefWidth(500);
		
		this.terrestrialObservationTypeComboBox.getSelectionModel().selectedItemProperty().addListener(new TerrestrialObservationTypeChangeListener());

		this.analysisChartNode = borderPane;
	}
	
	private PieChart createPieChart() {	
		PieChart chart = new PieChart() {
			// https://stackoverflow.com/questions/35479375/display-additional-values-in-pie-chart
			@Override
			protected void layoutChartChildren(double top, double left, double contentWidth, double contentHeight) {
				if (getLabelsVisible()) {
					Set<Node> items = this.lookupAll(".chart-pie-label");
					for (Node item : items) {
						if (item instanceof Text && ((Text)item).getText() != null) {
							Text text = (Text)item;
							setChartLabelText(text);
						}
					}
				}
				super.layoutChartChildren(top, left, contentWidth, contentHeight);
			}
		};
		chart.setLegendSide(Side.BOTTOM);
		chart.setLabelsVisible(Boolean.TRUE);
		//chart.setAnimated(Boolean.FALSE);
		return chart;
	}
	
	private void setChartLabelText(Text text) {
		String name = text.getText();
		if (name.equals(INADEQUATE) || name.equals(SATISFACTORY) || name.equals(EXCELLENT)) {
			TableRowHighlightRangeType tableRowHighlightRangeType = TableRowHighlightRangeType.valueOf(name);
			text.setText(String.format(Locale.ENGLISH, "%d", this.chartData.get(tableRowHighlightRangeType)));
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

	private void updateChartData(TerrestrialObservationType terrestrialObservationType) {
		try {
			terrestrialObservationType = terrestrialObservationType == null ? TerrestrialObservationType.ALL : terrestrialObservationType;
			ObservationType[] observationTypes = this.getSelectedObservationTypes(terrestrialObservationType);
			
			double leftBoundary  = this.tableRowHighlight.getLeftBoundary(TableRowHighlightType.INFLUENCE_ON_POSITION);
			double rightBoundary = this.tableRowHighlight.getRightBoundary(TableRowHighlightType.INFLUENCE_ON_POSITION);
			
			this.chartData = SQLManager.getInstance().getChartData(TableRowHighlightType.INFLUENCE_ON_POSITION, observationTypes);
			this.pieChart.getData().clear();

			List<PieChart.Data> dataList = FXCollections.observableArrayList();
			for (Map.Entry<TableRowHighlightRangeType, Integer> entry : this.chartData.entrySet()) {
				if (entry.getValue() > 0) 
					dataList.add(new PieChart.Data(entry.getKey().name(), entry.getValue()));
			}

			this.pieChart.getData().setAll(dataList);

			// set data color
			for (PieChart.Data data : this.pieChart.getData()) {
				try {
					String name = data.getName();
					switch (name) {
					case INADEQUATE:
					case SATISFACTORY:
					case EXCELLENT:
						TableRowHighlightRangeType tableRowHighlightRangeType = TableRowHighlightRangeType.valueOf(name);

						Color color = this.tableRowHighlight.getColor(tableRowHighlightRangeType);
						String rgbColor = String.format(Locale.ENGLISH, "rgb(%.0f, %.0f, %.0f, %.7f)", color.getRed()*255, color.getGreen()*255, color.getBlue()*255, color.getOpacity());

						data.getNode().setStyle(color != null && color != Color.TRANSPARENT ? String.format("-fx-pie-color: %s;", rgbColor) : "");
						break;
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}

			// Adapt legend
			Set<Node> nodes = this.pieChart.lookupAll("Label.chart-legend-item");
			String legendAbbr = i18n.getString("UIInfluenceOnPositionAnalysisChart.chart.legend.abbreviation", "EP");
			for (Node node : nodes) {
				if (node instanceof Label && ((Label) node).getGraphic() instanceof Region) {
					Label label = (Label) node;
					Region region = (Region)label.getGraphic();
					
					String name = label.getText();
					if (name.equals(INADEQUATE) || name.equals(SATISFACTORY) || name.equals(EXCELLENT)) {
						TableRowHighlightRangeType tableRowHighlightRangeType = TableRowHighlightRangeType.valueOf(name);
						Color color = this.tableRowHighlight.getColor(tableRowHighlightRangeType);
						String rgbColor = String.format(Locale.ENGLISH, "rgb(%.0f, %.0f, %.0f, %.7f)", color.getRed()*255, color.getGreen()*255, color.getBlue()*255, color.getOpacity());
						region.setStyle(color != null && color != Color.TRANSPARENT ? String.format("-fx-pie-color: %s;", rgbColor) : "");

						label.setMaxWidth(Double.MAX_VALUE);
						label.setAlignment(Pos.CENTER);
						
						switch (tableRowHighlightRangeType) {
						case INADEQUATE:
							label.setText(String.format(Locale.ENGLISH, "%s \u003E %s", legendAbbr,  options.toLengthResidualFormat(rightBoundary, true)));
							break;	
						case SATISFACTORY:
							label.setText(String.format(Locale.ENGLISH, "%s \u2264 %s \u2264 %s", options.toLengthResidualFormat(leftBoundary, false), legendAbbr, options.toLengthResidualFormat(rightBoundary, true)));
							break;
						case EXCELLENT:
							label.setText(String.format(Locale.ENGLISH, "%s \u003C %s", legendAbbr, options.toLengthResidualFormat(leftBoundary, true)));
							break;											
						default:
							System.err.println(this.getClass().getSimpleName() + ": Unsupported table row highlight type" + tableRowHighlightRangeType);
							break;
						}
					}
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIInfluenceOnPositionAnalysisChart.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIInfluenceOnPositionAnalysisChart.message.error.load.exception.header", "Error, could not load data from database."),
							i18n.getString("UIInfluenceOnPositionAnalysisChart.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
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
					return i18n.getString("UIInfluenceOnPositionAnalysisChart.observationtype.terrestrial.all.label", "All terrestrial observations");
				case LEVELING:
					return i18n.getString("UIInfluenceOnPositionAnalysisChart.observationtype.terrestrial.leveling.label", "Leveling");
				case DIRECTION:
					return i18n.getString("UIInfluenceOnPositionAnalysisChart.observationtype.terrestrial.direction.label", "Directions");
				case HORIZONTAL_DISTANCE:
					return i18n.getString("UIInfluenceOnPositionAnalysisChart.observationtype.terrestrial.horizontal_distance.label", "Horizontal distances");
				case SLOPE_DISTANCE:
					return i18n.getString("UIInfluenceOnPositionAnalysisChart.observationtype.terrestrial.slope_distance.label", "Slope distances");
				case ZENITH_ANGLE:
					return i18n.getString("UIInfluenceOnPositionAnalysisChart.observationtype.terrestrial.zenith_angle.label", "Zenith angles");
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
	
	public void load() {
		try {
			SQLManager.getInstance().loadTableRowHighlight();

			List<ObservationType> projectObservationTypes = SQLManager.getInstance().getProjectObservationTypes();
			TerrestrialObservationType[] terrestrialObservationTypeArray = new TerrestrialObservationType[projectObservationTypes.size() == 1 ? 1 : projectObservationTypes.size() + 1];
			terrestrialObservationTypeArray[0] = TerrestrialObservationType.ALL;

			TerrestrialObservationType lastSelectedTerrestrialObservationType = this.terrestrialObservationTypeComboBox.getSelectionModel().getSelectedItem();
			boolean containsLastSelectedTerrestrialObservationType = false;
			if (projectObservationTypes.size() > 1) {
				int idx = 1;
				for (ObservationType obsType : projectObservationTypes) {
					TerrestrialObservationType type = TerrestrialObservationType.getEnumByValue(obsType);
					if (type != null) {
						terrestrialObservationTypeArray[idx++] = type;
						if (lastSelectedTerrestrialObservationType == type)
							containsLastSelectedTerrestrialObservationType = true;
					}
				}
			}

			this.terrestrialObservationTypeComboBox.getSelectionModel().clearSelection();
			this.terrestrialObservationTypeComboBox.getItems().setAll(terrestrialObservationTypeArray);
			this.terrestrialObservationTypeComboBox.getSelectionModel().select(containsLastSelectedTerrestrialObservationType ? lastSelectedTerrestrialObservationType : TerrestrialObservationType.ALL);
			
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIInfluenceOnPositionAnalysisChart.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIInfluenceOnPositionAnalysisChart.message.error.load.exception.header", "Error, could not load data from database."),
							i18n.getString("UIInfluenceOnPositionAnalysisChart.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
}
