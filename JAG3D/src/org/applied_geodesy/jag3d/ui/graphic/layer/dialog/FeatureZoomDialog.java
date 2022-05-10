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

package org.applied_geodesy.jag3d.ui.graphic.layer.dialog;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

import org.applied_geodesy.jag3d.ui.graphic.layer.Layer;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerManager;
import org.applied_geodesy.jag3d.ui.graphic.layer.PointLayer;
import org.applied_geodesy.jag3d.ui.graphic.sql.GraphicPoint;
import org.applied_geodesy.jag3d.ui.graphic.util.GraphicExtent;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.ui.table.NaturalOrderComparator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class FeatureZoomDialog {

	private I18N i18n = I18N.getInstance();
	private static FeatureZoomDialog featureZoomDialog = new FeatureZoomDialog();
	private Dialog<GraphicPoint> dialog = null;
	private static Window window;
	private LayerManager layerManager;
	private ObservableList<Layer> layers;
	private ComboBox<GraphicPoint> pointsComboBox;	
	private FeatureZoomDialog() {}
	private Slider scaleSlider;

	public static void setOwner(Window owner) {
		window = owner;
	}

	public static Optional<GraphicPoint> showAndWait(LayerManager layerManager, ObservableList<Layer> layers) {
		featureZoomDialog.layerManager = layerManager;
		featureZoomDialog.layers = layers;
		featureZoomDialog.init();
		featureZoomDialog.load();
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					featureZoomDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) featureZoomDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});		
		return featureZoomDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<GraphicPoint>();
		this.dialog.setTitle(i18n.getString("FeatureZoomDialog.title", "Feature zoom"));
		this.dialog.setHeaderText(i18n.getString("FeatureZoomDialog.header", "Zoom to feature"));

		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		//		this.dialog.initStyle(StageStyle.UTILITY);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, GraphicPoint>() {
			@Override
			public GraphicPoint call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					GraphicPoint selectedPoint = pointsComboBox.getValue();
					if (selectedPoint != null)
						//List<GraphicPoint> selectedPoints = List.of(selectedPoint);
						zoomToFeature(selectedPoint);
					
					return selectedPoint;
				}
				return null;
			}
		});

		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
	}
	
	private Node createPane() {
		Label pointsLabel   = new Label(i18n.getString("FeatureZoomDialog.feature.points.label", "Adjusted points:"));
		Label scaleLabel    = new Label(i18n.getString("FeatureZoomDialog.scale.label", "Scaling factor:"));
		
		this.pointsComboBox = this.createPointComboBox(i18n.getString("FeatureZoomDialog.feature.points.tooltip", "Select point to zoom"));
		this.scaleSlider    = this.createScalingSlider(0.25, 1.75, 1.0, i18n.getString("FeatureZoomDialog.scale.tooltip", "Set scaling factor to zoom in or to zoom out"));
		
		pointsLabel.setLabelFor(this.pointsComboBox);
		scaleLabel.setLabelFor(this.scaleSlider);
		
		GridPane.setHgrow(pointsLabel, Priority.NEVER);
		GridPane.setHgrow(scaleLabel,  Priority.NEVER);
		
		GridPane.setHgrow(this.pointsComboBox,   Priority.ALWAYS);
		GridPane.setHgrow(this.scaleSlider,      Priority.ALWAYS);
		
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(5,15,5,15)); 
		
		int row = 1;
		gridPane.add(pointsLabel,         0, row);
		gridPane.add(this.pointsComboBox, 1, row++);
		gridPane.add(scaleLabel,          0, row);
		gridPane.add(this.scaleSlider,    1, row++);
	
		return gridPane;
	}
	
	private ComboBox<GraphicPoint> createPointComboBox(String tooltip) {
		ComboBox<GraphicPoint> typeComboBox = new ComboBox<GraphicPoint>();

		typeComboBox.setTooltip(new Tooltip(tooltip));
		typeComboBox.setMinWidth(150);
		typeComboBox.setMaxWidth(Double.MAX_VALUE);
		return typeComboBox;
	}
	
	private Slider createScalingSlider(double min, double max, double value, String tooltip) {
		Slider slider = new Slider(min, max, value);

		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(0.5);
		slider.setMinorTickCount(1);
		slider.setBlockIncrement(0.25);
		slider.setMinWidth(150);
		slider.setMaxWidth(Double.MAX_VALUE);
		slider.setTooltip(new Tooltip(tooltip));
		return slider;
	}
	
	private void load() {
		ObservableList<GraphicPoint> graphicPoints = FXCollections.observableArrayList();
		for (Layer layer : this.layers) {
			if (!layer.isVisible())
				continue;
			switch(layer.getLayerType()) {
//			case REFERENCE_POINT_APRIORI:
//			case STOCHASTIC_POINT_APRIORI:
//			case DATUM_POINT_APRIORI:
//			case NEW_POINT_APRIORI:
			case DATUM_POINT_APOSTERIORI:
			case NEW_POINT_APOSTERIORI:
			case REFERENCE_POINT_APOSTERIORI:
			case STOCHASTIC_POINT_APOSTERIORI:
				PointLayer pointLayer = (PointLayer)layer;
				graphicPoints.addAll(pointLayer.getPoints());
				break;
			default:
				break;
			
			}
		}
		
		if (!graphicPoints.isEmpty()) {
			Collections.sort(graphicPoints, new NaturalOrderComparator<GraphicPoint>());
			FilteredList<GraphicPoint> filteredPoints = new FilteredList<GraphicPoint>(graphicPoints, new Predicate<GraphicPoint>() {
				@Override
				public boolean test(GraphicPoint point) {
					return point.isVisible();
				}
			});

			int selectedIndex = this.pointsComboBox.getSelectionModel().getSelectedIndex();
			this.pointsComboBox.getItems().setAll(filteredPoints);
			if (filteredPoints.size() > selectedIndex && selectedIndex >= 0)
				this.pointsComboBox.getSelectionModel().clearAndSelect(selectedIndex); 
			else if (!filteredPoints.isEmpty())
				this.pointsComboBox.getSelectionModel().clearAndSelect(0);
		}
	}
	
	private void zoomToFeature(GraphicPoint selectedPoint) {
		GraphicExtent graphicExtent = this.layerManager.getCurrentGraphicExtent();
		
		double pointX = selectedPoint.getCoordinate().getX();
		double pointY = selectedPoint.getCoordinate().getY();

		double extentWidth  = graphicExtent.getExtentWidth();
		double extentHeight = graphicExtent.getExtentHeight();
		double scale        = graphicExtent.getScale();

		double newMinX = pointX - 0.5 * extentWidth;
		double newMaxX = pointX + 0.5 * extentWidth;
		
		double newMinY = pointY + 0.5 * extentHeight;
		double newMaxY = pointY - 0.5 * extentHeight;

		graphicExtent.set(newMinX, newMinY, newMaxX, newMaxY);
		graphicExtent.setScale(scale * this.scaleSlider.getValue());
		
		this.layerManager.draw();
	}
}
