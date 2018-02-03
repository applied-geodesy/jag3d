package org.applied_geodesy.jag3d.ui.graphic;

import org.applied_geodesy.jag3d.ui.graphic.layer.LayerManager;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

public class UIGraphicPaneBuilder {
	private static UIGraphicPaneBuilder graphicPaneBuilder = new UIGraphicPaneBuilder();
	private BorderPane borderPane = null;
	private final LayerManager layerManager = new LayerManager();
	
	private UIGraphicPaneBuilder() {}
	
	public static UIGraphicPaneBuilder getInstance() {
		return graphicPaneBuilder;
	}
	
	public Pane getPane() {
		this.init();
		return this.borderPane;
	}
	
	private void init() {
		if (this.borderPane != null)
			return;
	
		this.borderPane = new BorderPane();
		this.borderPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		this.borderPane.setTop(this.layerManager.getToolBar());
		this.borderPane.setCenter(this.layerManager.getPane());
		
		Label coordinateLabel = this.layerManager.getCoordinateLabel();
		coordinateLabel.setFont(new Font(10));
		coordinateLabel.setPadding(new Insets(10, 10, 10, 10));
		
		Region spacer = new Region();
		HBox hbox = new HBox(10);
		HBox.setHgrow(spacer, Priority.ALWAYS);
		hbox.getChildren().addAll(spacer, coordinateLabel);
		
		this.borderPane.setBottom(hbox);
	}
	
	public LayerManager getLayerManager() {
		return this.layerManager;
	}
}
