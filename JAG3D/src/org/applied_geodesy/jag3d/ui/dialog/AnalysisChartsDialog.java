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

import java.util.Optional;

import org.applied_geodesy.jag3d.ui.dialog.chart.AnalysisChartType;
import org.applied_geodesy.jag3d.ui.dialog.chart.AnalysisChartTypeListCell;
import org.applied_geodesy.jag3d.ui.dialog.chart.UIInfluenceOnPositionAnalysisChart;
import org.applied_geodesy.jag3d.ui.dialog.chart.UIRedundancyAnalysisChart;
import org.applied_geodesy.jag3d.ui.dialog.chart.UIResidualAnalysisChart;
import org.applied_geodesy.jag3d.ui.dialog.chart.UISignAnalysisChart;
import org.applied_geodesy.jag3d.ui.i18n.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class AnalysisChartsDialog {
	
	private class AnalysisChartTypeChangeListener implements ChangeListener<AnalysisChartType> {

		@Override
		public void changed(ObservableValue<? extends AnalysisChartType> observable, AnalysisChartType oldType, AnalysisChartType newType) {
			if (newType != null)
				loadChart(newType);
		}
	}

	private static AnalysisChartsDialog analysisChartsDialog = new AnalysisChartsDialog();
	private I18N i18n = I18N.getInstance();
	private Dialog<Void> dialog = null;
	private Window window;
	private ListView<AnalysisChartType> analysisChartTypeList;
	private AnalysisChartType lastSelectedAnalysisChartType = AnalysisChartType.RESIDUALS;
	private BorderPane chartPane;
	private AnalysisChartsDialog() {}

	public static void setOwner(Window owner) {
		analysisChartsDialog.window = owner;
	}

	public static Optional<Void> showAndWait() {
		analysisChartsDialog.init();
		analysisChartsDialog.analysisChartTypeList.getSelectionModel().clearSelection();

		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	try {
            		analysisChartsDialog.analysisChartTypeList.getSelectionModel().select(analysisChartsDialog.lastSelectedAnalysisChartType);
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
		this.dialog.setTitle(i18n.getString("AnalysisChartsDialog.title", "Analysis charts"));
		this.dialog.setHeaderText(i18n.getString("AnalysisChartsDialog.chart.header", "Analysis charts"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
	}
	
	private BorderPane createPane() {
		this.chartPane = new BorderPane();
		 
		this.analysisChartTypeList = new ListView<AnalysisChartType>();
		this.analysisChartTypeList.getItems().addAll(AnalysisChartType.values());
		this.analysisChartTypeList.getSelectionModel().selectedItemProperty().addListener(new AnalysisChartTypeChangeListener());
		//this.analysisChartTypeList.getSelectionModel().clearAndSelect(0); // fire event
		
		this.analysisChartTypeList.setTooltip(new Tooltip(i18n.getString("AnalysisChartsDialog.chart.type.tooltip", "Select analysis chart type")));
		this.analysisChartTypeList.setCellFactory(new Callback<ListView<AnalysisChartType>, ListCell<AnalysisChartType>>() {
		    @Override
		    public ListCell<AnalysisChartType> call(ListView<AnalysisChartType> analysisChartTypeList) {
		        return new AnalysisChartTypeListCell();
		    }
		});
		
		this.chartPane.setLeft(this.analysisChartTypeList);
		Region spacer = new Region();
		spacer.setPrefWidth(450);
		spacer.setPrefHeight(400);
		this.chartPane.setCenter(spacer);
		
		return this.chartPane;
	}
	
	private void setChart(Node node) {
		if (node != null) {
			this.chartPane.setCenter(node);
		}
	}
	
	private void loadChart(AnalysisChartType analysisChartType) {
		final Node node;
		switch (analysisChartType) {
		case RESIDUALS:
			this.dialog.setHeaderText(i18n.getString("AnalysisChartsDialog.chart.type.residual.header", "Histogram of normalized residuals"));
			node = UIResidualAnalysisChart.getInstance().getNode();
			this.setChart(node);
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					UIResidualAnalysisChart.getInstance().load();	
				}
			});
			
			break;

		case REDUNDANCY:
			this.dialog.setHeaderText(i18n.getString("AnalysisChartsDialog.chart.type.redundancy.header", "Pie chart of redundancy"));
			node = UIRedundancyAnalysisChart.getInstance().getNode();
			this.setChart(node);
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					UIRedundancyAnalysisChart.getInstance().load();
				}
			});

			break;
			
		case INFLUENCE_ON_POSITION:
			this.dialog.setHeaderText(i18n.getString("AnalysisChartsDialog.chart.type.influence_on_position.header", "Pie chart of influence on position distribution"));
			node = UIInfluenceOnPositionAnalysisChart.getInstance().getNode();
			this.setChart(node);
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					UIInfluenceOnPositionAnalysisChart.getInstance().load();
				}
			});

			break;
			
		case SIGN:
			this.dialog.setHeaderText(i18n.getString("AnalysisChartsDialog.chart.type.sign.header", "Pie chart of sign distribution"));
			node = UISignAnalysisChart.getInstance().getNode();
			this.setChart(node);
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					UISignAnalysisChart.getInstance().load();
				}
			});

			break;
		}

		this.lastSelectedAnalysisChartType = analysisChartType;
	}
}
