package org.applied_geodesy.jag3d.ui.graphic.layer.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.applied_geodesy.jag3d.ui.graphic.layer.ArrowLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.ConfidenceLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.Layer;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerManager;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerType;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.PointLayer;
import org.applied_geodesy.util.i18.I18N;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
				if (selectedLayer != null && selectedLayer.getLayerType() != null) {
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
						propertiesNode = UIPointLayerPropertyBuilder.getLayerPropertyPane((PointLayer)selectedLayer);
						break;

					case OBSERVATION_APOSTERIORI:
					case OBSERVATION_APRIORI:
						propertiesNode = UIObservationLayerPropertyBuilder.getLayerPropertyPane((ObservationLayer)selectedLayer);
						break;

					case ABSOLUTE_CONFIDENCE:
					case RELATIVE_CONFIDENCE:
						propertiesNode = UIConfidenceLayerPropertyBuilder.getLayerPropertyPane((ConfidenceLayer<?>)selectedLayer);
						break;
						
					case ARROW:
						propertiesNode = UIArrowLayerPropertyBuilder.getLayerPropertyPane((ArrowLayer)selectedLayer);
						
					case MOUSE: // invisible
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
			Layer selectedLayer = layerList.getSelectionModel().getSelectedItem();
			boolean moveUp = event.getSource() == upButton;
			int selectedIndex = -1;
			if (selectedLayer != null && (selectedIndex = layerList.getItems().indexOf(selectedLayer)) >= 0) {
				Collections.swap(layerList.getItems(), selectedIndex + (moveUp ? -1 : 1), selectedIndex);
				List<Layer> reorderedList = new ArrayList<Layer>(layerList.getItems());
				Collections.reverse(reorderedList);
				layerManager.reorderLayer(reorderedList);
				
//				layerList.getSelectionModel().clearSelection();
				layerList.getSelectionModel().select(selectedLayer);
			}
		}
	}
	
	private static I18N i18n = I18N.getInstance();
	private static LayerManagerDialog layerManagerDialog = new LayerManagerDialog();
	private Dialog<Void> dialog = null;
	private static Window window;
	private LayerManager layerManager;
	private BorderPane layerPropertyBorderPane = new BorderPane();
	private Button upButton, downButton;
	private ListView<Layer> layerList;
	private LayerManagerDialog() {}
	

	public static void setOwner(Window owner) {
		window = owner;
	}

	public static Optional<Void> showAndWait(LayerManager layerManager) {
		layerManagerDialog.layerManager = layerManager;
		layerManagerDialog.init();
		layerManagerDialog.load();
		return layerManagerDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<Void>();

		this.dialog.setTitle(i18n.getString("LayerManagerDialog.title", "Layer properties"));
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
			}
		});
	}
	
	private Node createPane() {
		this.layerPropertyBorderPane.setTop(this.createLayerOrderToolBar());
		
		BorderPane rootNode = new BorderPane();
		this.layerList = this.createLayerListPane();
		rootNode.setLeft(this.layerList);
		rootNode.setCenter(this.layerPropertyBorderPane);
		return rootNode;
	}
	
	private ToolBar createLayerOrderToolBar() {
		UpDownEventHandler upDownEventHandler = new UpDownEventHandler();
		this.upButton = new Button(i18n.getString("LayerManagerDialog.up.label", "\u25B2"));
		this.upButton.setTooltip(new Tooltip(i18n.getString("LayerManagerDialog.up.tooltip", "Move selected layer up")));
		this.upButton.setOnAction(upDownEventHandler);
		this.downButton = new Button(i18n.getString("LayerManagerDialog.up.label", "\u25BC"));
		this.downButton.setTooltip(new Tooltip(i18n.getString("LayerManagerDialog.up.tooltip", "Move selected layer down")));
		this.downButton.setOnAction(upDownEventHandler);
		
		ToolBar toolBar = new ToolBar();
		HBox spacer = new HBox(); 
		HBox.setHgrow(spacer, Priority.ALWAYS);
		toolBar.getItems().addAll(spacer, this.upButton, this.downButton);
		return toolBar;
	}
	
	private ListView<Layer> createLayerListPane() {
		ListView<Layer> listView = new ListView<Layer>();
		int length = this.layerManager.getPane().getChildren().size();
		for (int i = length - 1; i >= 0; i--) {
			Node node = this.layerManager.getPane().getChildren().get(i);
			if (node != null && node instanceof Layer && ((Layer)node).getLayerType() != LayerType.MOUSE) {
				listView.getItems().add((Layer)node);
			}
		}
		listView.setCellFactory(new Callback<ListView<Layer>, 
				ListCell<Layer>>() {
			@Override 
			public ListCell<Layer> call(ListView<Layer> list) {
				return new LayerListCell();
			}
		});
		listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		//listView.getSelectionModel().selectedItemProperty().addListener(new LayerChangeListener());
		listView.getSelectionModel().selectedIndexProperty().addListener(new LayerIndexChangeListener(listView));
		listView.getSelectionModel().select(0);
		
		return listView;
	}
	
	private void load() {
		System.out.println("LOAD");
	}
	
	private void save() {
		System.out.println("SAVE");
	}
}
