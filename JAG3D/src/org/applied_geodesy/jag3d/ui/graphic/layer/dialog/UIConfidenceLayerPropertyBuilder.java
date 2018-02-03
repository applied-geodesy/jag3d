package org.applied_geodesy.jag3d.ui.graphic.layer.dialog;

import org.applied_geodesy.jag3d.ui.graphic.layer.ConfidenceLayer;
import org.applied_geodesy.util.i18.I18N;

import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class UIConfidenceLayerPropertyBuilder extends UILayerPropertyBuilder {

	private static I18N i18n = I18N.getInstance();
	private static UIConfidenceLayerPropertyBuilder confidenceLayerPropertyBuilder = new UIConfidenceLayerPropertyBuilder();

	private ComboBox<Double> lineWidthComboBox;
	private ColorPicker symbolStrokeColorPicker;
	private ColorPicker symbolFillColorPicker;
	private ConfidenceLayer<?> currentLayer = null;
	private VBox propertyPane = null;

	private UIConfidenceLayerPropertyBuilder() {}
	
	private void init() {
		if (this.propertyPane != null)
			return;
		
		this.propertyPane = new VBox(20);
		this.propertyPane.setMaxWidth(Double.MAX_VALUE);
		this.propertyPane.getChildren().addAll(
				this.createSymbolPane()
				);
	}
	
	public static Node getLayerPropertyPane(ConfidenceLayer<?> layer) {
		confidenceLayerPropertyBuilder.init();
		confidenceLayerPropertyBuilder.bindProperties(layer);
		return confidenceLayerPropertyBuilder.propertyPane;
	}
	
	private void unbindProperties() {
		if (this.currentLayer != null) {
			this.currentLayer.symbolSizeProperty().unbind();
			this.currentLayer.lineWidthProperty().unbind();
			this.currentLayer.colorProperty().unbind();
		}
	}
	
	private void bindProperties(ConfidenceLayer<?> layer) {
		// unbind
		this.unbindProperties();
		
		// set new layer
		this.currentLayer = layer;

		// Symbol properties
		this.lineWidthComboBox.getSelectionModel().select(this.currentLayer.getLineWidth());
		this.symbolStrokeColorPicker.setValue(this.currentLayer.getStrokeColor());
		this.symbolFillColorPicker.setValue(this.currentLayer.getColor());
		
		//this.currentLayer.confidenceScaleProperty().bind(this.scaleComboBox.getSelectionModel().selectedItemProperty());
		this.currentLayer.lineWidthProperty().bind(this.lineWidthComboBox.getSelectionModel().selectedItemProperty());
		this.currentLayer.colorProperty().bind(this.symbolFillColorPicker.valueProperty());
		this.currentLayer.strokeColorProperty().bind(this.symbolStrokeColorPicker.valueProperty());	
	}
	
	private Node createSymbolPane() {
		GridPane gridPane = this.createGridPane();

		Label symbolStrokeColorLabel = new Label(i18n.getString("UIConfidenceLayerPropertyBuilder.symbol.color.label", "Stroke color:"));
		symbolStrokeColorLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label symbolFillColorLabel = new Label(i18n.getString("UIConfidenceLayerPropertyBuilder.symbol.fill_color.label", "Fill color:"));
		symbolFillColorLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label symbolLineWidthLabel = new Label(i18n.getString("UIConfidenceLayerPropertyBuilder.symbol.linewidth.label", "Line width:"));
		symbolLineWidthLabel.setMinWidth(Control.USE_PREF_SIZE);

		this.lineWidthComboBox  = this.createLineWidthComboBox(i18n.getString("UIConfidenceLayerPropertyBuilder.symbol.linewidth.tooltip", "Selected line width"));
		this.symbolStrokeColorPicker  = new ColorPicker(Color.BLACK);
		this.symbolStrokeColorPicker.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.symbolStrokeColorPicker.setMaxWidth(Double.MAX_VALUE);
		this.symbolStrokeColorPicker.getStyleClass().add("split-button");
		
		this.symbolFillColorPicker = new ColorPicker(Color.LIGHTGREY);
		this.symbolFillColorPicker.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.symbolFillColorPicker.setMaxWidth(Double.MAX_VALUE);
		this.symbolFillColorPicker.getStyleClass().add("split-button");
		
		symbolStrokeColorLabel.setLabelFor(this.symbolStrokeColorPicker);
		symbolFillColorLabel.setLabelFor(this.symbolFillColorPicker);
		symbolLineWidthLabel.setLabelFor(this.lineWidthComboBox);
		
		GridPane.setHgrow(symbolStrokeColorLabel, Priority.NEVER);
		GridPane.setHgrow(symbolFillColorLabel,   Priority.NEVER);
		GridPane.setHgrow(symbolLineWidthLabel,   Priority.NEVER);
		
		GridPane.setHgrow(this.symbolStrokeColorPicker, Priority.ALWAYS);
		GridPane.setHgrow(this.symbolFillColorPicker,   Priority.ALWAYS);
		GridPane.setHgrow(this.lineWidthComboBox,       Priority.ALWAYS);

		int row = 0;				
		gridPane.add(symbolStrokeColorLabel,  0, row);
		gridPane.add(this.symbolStrokeColorPicker, 1, row++);
		
		gridPane.add(symbolFillColorLabel,  0, row);
		gridPane.add(this.symbolFillColorPicker, 1, row++);
		
		gridPane.add(symbolLineWidthLabel, 0, row);
		gridPane.add(this.lineWidthComboBox,    1, row++);

		return this.createTitledPane(i18n.getString("UIConfidenceLayerPropertyBuilder.symbol.title", "Symbol properties"), gridPane);
	}
}
