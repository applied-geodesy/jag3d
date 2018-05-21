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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dialog.OptionDialog;
import org.applied_geodesy.jag3d.ui.graphic.layer.ArrowLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.ConfidenceLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.Layer;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerManager;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerType;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.PointLayer;
import org.applied_geodesy.jag3d.ui.graphic.sql.SQLGraphicManager;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class LayerManagerDialog {

	private class LayerIndexChangeListener implements ChangeListener<Number> {
		private ListView<Layer> listView;
		private LayerIndexChangeListener(ListView<Layer> listView) {
			this.listView = listView;
		}
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {			
			if (newValue == null || newValue.intValue() < 0) {
				upButton.setDisable(true);
				downButton.setDisable(true);
			}
			else {
				upButton.setDisable(newValue.intValue() == 0);
				downButton.setDisable(newValue.intValue() == this.listView.getItems().size() - 1);

				Layer selectedLayer = this.listView.getItems().get(newValue.intValue());
				if (selectedLayer != null && selectedLayer.getLayerType() != null && layerManager != null) {
					LayerType type = selectedLayer.getLayerType();

					Node propertiesNode = null;
					switch(type) {
					case DATUM_POINT_APOSTERIORI:
					case DATUM_POINT_APRIORI:
					case NEW_POINT_APOSTERIORI:
					case NEW_POINT_APRIORI:
					case REFERENCE_POINT_APOSTERIORI:
					case REFERENCE_POINT_APRIORI:
					case STOCHASTIC_POINT_APOSTERIORI:
					case STOCHASTIC_POINT_APRIORI:
						propertiesNode = UIPointLayerPropertyBuilder.getLayerPropertyPane(layerManager, (PointLayer)selectedLayer);
						break;

					case OBSERVATION_APOSTERIORI:
					case OBSERVATION_APRIORI:
						propertiesNode = UIObservationLayerPropertyBuilder.getLayerPropertyPane(layerManager, (ObservationLayer)selectedLayer);
						break;

					case ABSOLUTE_CONFIDENCE:
					case RELATIVE_CONFIDENCE:
						propertiesNode = UIConfidenceLayerPropertyBuilder.getLayerPropertyPane(layerManager, (ConfidenceLayer<?>)selectedLayer);
						break;

					case POINT_SHIFT:
					case PRINCIPAL_COMPONENT_HORIZONTAL:
					case PRINCIPAL_COMPONENT_VERTICAL:
						propertiesNode = UIArrowLayerPropertyBuilder.getLayerPropertyPane(layerManager, (ArrowLayer)selectedLayer);

						break;
					}

					if (propertiesNode != null) {
						ScrollPane scroller = new ScrollPane(propertiesNode);
						scroller.setPadding(new Insets(10, 10, 10, 10)); // oben, links, unten, rechts
						scroller.setFitToHeight(true);
						scroller.setFitToWidth(true);
						layerPropertyBorderPane.setCenter(scroller);
					}
				}
			}
		}
	}

	private class UpDownEventHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			Layer selectedLayer = layerListView.getSelectionModel().getSelectedItem();
			boolean moveUp = event.getSource() == upButton;
			int selectedIndex = -1;
			if (selectedLayer != null && (selectedIndex = layerListView.getItems().indexOf(selectedLayer)) >= 0) {
				Collections.swap(layerListView.getItems(), selectedIndex + (moveUp ? -1 : 1), selectedIndex);
				List<Layer> reorderedList = new ArrayList<Layer>(layerListView.getItems());
				Collections.reverse(reorderedList);
				layerManager.reorderLayer(reorderedList);

				//layerListView.getSelectionModel().clearSelection();
				layerListView.getSelectionModel().select(selectedLayer);
				
				layerManager.draw();
			}
		}
	}

	private I18N i18n = I18N.getInstance();
	private static LayerManagerDialog layerManagerDialog = new LayerManagerDialog();
	private Dialog<Void> dialog = null;
	private static Window window;
	private LayerManager layerManager;
	private BorderPane layerPropertyBorderPane = new BorderPane();
	private Button upButton, downButton;
	private ListView<Layer> layerListView;
	private ObservableList<Layer> layers;
	private LayerManagerDialog() {}


	public static void setOwner(Window owner) {
		window = owner;
	}

	public static Optional<Void> showAndWait(LayerManager layerManager, ObservableList<Layer> layers) {
		layerManagerDialog.layerManager = layerManager;
		layerManagerDialog.layers = layers;
		layerManagerDialog.init();
		layerManagerDialog.load();
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					layerManagerDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) layerManagerDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});		
		return layerManagerDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<Void>();

		this.dialog.setTitle(i18n.getString("LayerManagerDialog.title", "Layer manager"));
		this.dialog.setHeaderText(i18n.getString("LayerManagerDialog.header", "Layer properties"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
		this.dialog.initModality(Modality.APPLICATION_MODAL);

		this.dialog.initOwner(window);

		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);

		this.dialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
			@Override
			public void handle(DialogEvent event) {
				save();
				layerListView.getSelectionModel().select(0);
			}
		});
	}

	private Node createPane() {
		this.layerPropertyBorderPane.setTop(this.createLayerOrderToolBar());

		BorderPane rootNode = new BorderPane();
		this.layerListView = this.createLayerListPane();
		rootNode.setLeft(this.layerListView);
		rootNode.setCenter(this.layerPropertyBorderPane);
		return rootNode;
	}

	private ToolBar createLayerOrderToolBar() {
		UpDownEventHandler upDownEventHandler = new UpDownEventHandler();
		this.upButton = new Button(i18n.getString("LayerManagerDialog.up.label", "\u25B2"));
		this.upButton.setTooltip(new Tooltip(i18n.getString("LayerManagerDialog.up.tooltip", "Move selected layer up")));
		this.upButton.setOnAction(upDownEventHandler);
		this.downButton = new Button(i18n.getString("LayerManagerDialog.down.label", "\u25BC"));
		this.downButton.setTooltip(new Tooltip(i18n.getString("LayerManagerDialog.down.tooltip", "Move selected layer down")));
		this.downButton.setOnAction(upDownEventHandler);

		ToolBar toolBar = new ToolBar();
		HBox spacer = new HBox(); 
		HBox.setHgrow(spacer, Priority.ALWAYS);
		toolBar.getItems().addAll(spacer, this.upButton, this.downButton);
		return toolBar;
	}

	private ListView<Layer> createLayerListPane() {
		ListView<Layer> listView = new ListView<Layer>();
		
		int length = this.layers.size();
		for (int i = length - 1; i >= 0; i--)
			listView.getItems().add(this.layers.get(i));

		listView.setCellFactory(new Callback<ListView<Layer>, ListCell<Layer>>() {
			@Override 
			public ListCell<Layer> call(ListView<Layer> list) {
				return new LayerListCell();
			}
		});
		listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		listView.getSelectionModel().selectedIndexProperty().addListener(new LayerIndexChangeListener(listView));
		listView.getSelectionModel().select(0);

		return listView;
	}

	private void load() {
		SQLGraphicManager sqlGraphicManager = SQLManager.getInstance().getSQLGraphicManager();
		if (sqlGraphicManager == null) 
			return;

		try {
			sqlGraphicManager.initLayer(this.layerManager);
			
			ListView<Layer> listView = new ListView<Layer>();
			int length = this.layers.size();
			for (int i = length - 1; i >= 0; i--) {
				listView.getItems().add(this.layers.get(i));
			}
			this.layerListView.getSelectionModel().clearSelection();
			this.layerListView.getItems().setAll(listView.getItems());
			this.layerListView.getSelectionModel().select(0);
		}
		catch(Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("LayerManagerDialog.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("LayerManagerDialog.message.error.load.exception.header", "Error, could not load layer properties from database."),
							i18n.getString("LayerManagerDialog.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}

	private void save() {
		SQLGraphicManager sqlGraphicManager = SQLManager.getInstance().getSQLGraphicManager();
		if (sqlGraphicManager == null) 
			return;

		try {
			int order = 0;
			for (Layer layer : this.layers) {
				LayerType layerType = layer.getLayerType();

				switch(layerType) {
				case DATUM_POINT_APOSTERIORI:
				case DATUM_POINT_APRIORI:
				case NEW_POINT_APOSTERIORI:
				case NEW_POINT_APRIORI:
				case REFERENCE_POINT_APOSTERIORI:
				case REFERENCE_POINT_APRIORI:
				case STOCHASTIC_POINT_APOSTERIORI:
				case STOCHASTIC_POINT_APRIORI:
					sqlGraphicManager.save((PointLayer)layer, order);
					break;

				case OBSERVATION_APOSTERIORI:
				case OBSERVATION_APRIORI:
					sqlGraphicManager.save((ObservationLayer)layer, order);
					break;

				case RELATIVE_CONFIDENCE:			
				case ABSOLUTE_CONFIDENCE:
					sqlGraphicManager.save((ConfidenceLayer<?>)layer, order);
					break;

				case POINT_SHIFT:
				case PRINCIPAL_COMPONENT_HORIZONTAL:
				case PRINCIPAL_COMPONENT_VERTICAL:
					sqlGraphicManager.save((ArrowLayer)layer, order);
					break;
				}
				order++;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("LayerManagerDialog.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("LayerManagerDialog.message.error.save.exception.header", "Error, could not save layer properties to database."),
							i18n.getString("LayerManagerDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
}