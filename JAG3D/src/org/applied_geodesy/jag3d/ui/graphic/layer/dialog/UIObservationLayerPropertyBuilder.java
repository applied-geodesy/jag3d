package org.applied_geodesy.jag3d.ui.graphic.layer.dialog;

import java.util.HashMap;
import java.util.Map;

import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationSymbolProperties;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationSymbolProperties.ObservationType;
import org.applied_geodesy.util.i18.I18N;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class UIObservationLayerPropertyBuilder extends UILayerPropertyBuilder {
	private static I18N i18n = I18N.getInstance();
	private static UIObservationLayerPropertyBuilder observationLayerPropertyBuilder = new UIObservationLayerPropertyBuilder();

	private ComboBox<Double> symbolSizeComboBox;
	private ComboBox<Double> lineWidthComboBox;
	private ColorPicker symbolColorPicker;
	
	private Map<ObservationType, ColorPicker> observationColorPickerMap = new HashMap<ObservationType, ColorPicker>();
	private Map<ObservationType, CheckBox> observationCheckBoxMap = new HashMap<ObservationType, CheckBox>();
	
	private ObservationLayer currentLayer = null;
	
	private VBox propertyPane = null;
	
	private UIObservationLayerPropertyBuilder() {}
	
	private void init() {
		if (this.propertyPane != null)
			return;
		
		this.propertyPane = new VBox(20);
		this.propertyPane.setMaxWidth(Double.MAX_VALUE);
		this.propertyPane.getChildren().addAll(
				this.createSymbolPane(),
				this.createObservationPaintPane()
				);
	}
	
	public static Node getLayerPropertyPane(ObservationLayer layer) {
		observationLayerPropertyBuilder.init();
		observationLayerPropertyBuilder.bindProperties(layer);
		return observationLayerPropertyBuilder.propertyPane;
	}
	
	private void bindProperties(ObservationLayer layer) {
		ObservationType observationTypes[] = ObservationType.values();
		// unbind
		if (this.currentLayer != null) {
			this.currentLayer.symbolSizeProperty().unbind();
			this.currentLayer.lineWidthProperty().unbind();
			this.currentLayer.colorProperty().unbind();

			for (ObservationType observationType : observationTypes) {
				ObservationSymbolProperties properties = this.currentLayer.getObservationSymbolProperties(observationType);
				if (properties != null) {
					properties.colorProperty().unbind();
					properties.enableProperty().unbind();
				}
			}
		}
		
		// set new layer
		this.currentLayer = layer;
		
		// Symbol properties
		this.symbolSizeComboBox.getSelectionModel().select(this.currentLayer.getSymbolSize());
		this.lineWidthComboBox.getSelectionModel().select(this.currentLayer.getLineWidth());
		this.symbolColorPicker.setValue(this.currentLayer.getColor());
		
		this.currentLayer.symbolSizeProperty().bind(this.symbolSizeComboBox.getSelectionModel().selectedItemProperty());
		this.currentLayer.lineWidthProperty().bind(this.lineWidthComboBox.getSelectionModel().selectedItemProperty());
		this.currentLayer.colorProperty().bind(this.symbolColorPicker.valueProperty());
		
		for (ObservationType observationType : observationTypes) {
			ObservationSymbolProperties properties = this.currentLayer.getObservationSymbolProperties(observationType);	
			CheckBox checkBox       = this.observationCheckBoxMap.get(observationType);
			ColorPicker colorPicker = this.observationColorPickerMap.get(observationType);
			
			checkBox.setSelected(properties.isEnable());
			properties.enableProperty().bind(checkBox.selectedProperty());
			colorPicker.setValue(properties.getColor());
			properties.colorProperty().bind(colorPicker.valueProperty());
		}
	}
	
	private Node createObservationPaintPane() {
		GridPane gridPane = this.createGridPane();

		ObservationType observationTypes[] = ObservationType.values();
		for (ObservationType observationType : observationTypes) {
			switch(observationType) {
			case LEVELING:
				this.observationCheckBoxMap.put(observationType, this.createCheckBox(
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.leveling.enable.label", "Leveling:"), 
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.leveling.enable.tooltip", "If checked, leveling will be drawn."))
						);
				this.observationColorPickerMap.put(observationType, new ColorPicker(Color.DARKBLUE));
				break;
				
			case DIRECTION:
				this.observationCheckBoxMap.put(observationType, this.createCheckBox(
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.direction.enable.label", "Direction:"), 
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.direction.enable.tooltip", "If checked, directions will be drawn."))
						);
				this.observationColorPickerMap.put(observationType, new ColorPicker(Color.DARKBLUE));
				break;
				
			case DISTANCE:
				this.observationCheckBoxMap.put(observationType, this.createCheckBox(
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.distance.enable.label", "Distance:"), 
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.distance.enable.tooltip", "If checked, distance will be drawn."))
						);
				this.observationColorPickerMap.put(observationType, new ColorPicker(Color.DARKBLUE));
				break;
			case ZENITH_ANGLE:
				this.observationCheckBoxMap.put(observationType, this.createCheckBox(
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.zenithangle.enable.label", "Zenith angle:"), 
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.zenithangle.enable.tooltip", "If checked, zenith angle will be drawn."))
						);
				this.observationColorPickerMap.put(observationType, new ColorPicker(Color.DARKBLUE));
				break;
				
			case GNSS:
				this.observationCheckBoxMap.put(observationType, this.createCheckBox(
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.gnss.enable.label", "GNSS Baseline:"), 
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.gnss.enable.tooltip", "If checked, GNSS baselines will be drawn."))
						);
				this.observationColorPickerMap.put(observationType, new ColorPicker(Color.DARKBLUE));
				break;
			
			}
		}
		
		for (CheckBox checkBox : this.observationCheckBoxMap.values()) {
				GridPane.setHgrow(checkBox, Priority.NEVER);
		}
		
		for (ColorPicker colorPicker : this.observationColorPickerMap.values()) {
			colorPicker.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
			GridPane.setHgrow(colorPicker, Priority.ALWAYS);
		}
		
		int row = 0;
		for (ObservationType observationType : observationTypes) {
			gridPane.add(this.observationCheckBoxMap.get(observationType),     0, row);
			gridPane.add( this.observationColorPickerMap.get(observationType), 1, row++);
		}

		return this.createTitledPane(i18n.getString("UIObservationLayerPropertyBuilder.observation.title", "Observation properties"), gridPane);
	}
	
	private Node createSymbolPane() {
		GridPane gridPane = this.createGridPane();
		
		Label symbolSizeLabel = new Label(i18n.getString("UIObservationLayerPropertyBuilder.symbol.size.label", "Symbol size:"));
		symbolSizeLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label symbolColorLabel = new Label(i18n.getString("UIObservationLayerPropertyBuilder.symbol.color.label", "Symbol color:"));
		symbolColorLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label symbolLineWidthLabel = new Label(i18n.getString("UIObservationLayerPropertyBuilder.symbol.linewidth.label", "Line width:"));
		symbolLineWidthLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Double symbolSizes[] = new Double[21];
		for (int i = 0; i < symbolSizes.length; i++)
			symbolSizes[i] = 5 + 0.5 * i;
		
		this.symbolSizeComboBox = this.createSizeComboBox(i18n.getString("UIObservationLayerPropertyBuilder.symbol.size.tooltip", "Selected symbol size"), symbolSizes, 1);
		this.lineWidthComboBox  = this.createLineWidthComboBox(i18n.getString("UIObservationLayerPropertyBuilder.symbol.linewidth.tooltip", "Selected line width"));
		this.symbolColorPicker  = new ColorPicker(Color.DARKBLUE);
		this.symbolColorPicker.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.symbolColorPicker.setMaxWidth(Double.MAX_VALUE);
		this.symbolColorPicker.getStyleClass().add("split-button");
		
		symbolSizeLabel.setLabelFor(this.symbolSizeComboBox);
		symbolColorLabel.setLabelFor(this.symbolColorPicker);
		symbolLineWidthLabel.setLabelFor(this.lineWidthComboBox);
		
		GridPane.setHgrow(symbolSizeLabel,      Priority.NEVER);
		GridPane.setHgrow(symbolColorLabel,     Priority.NEVER);
		GridPane.setHgrow(symbolLineWidthLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.symbolSizeComboBox, Priority.ALWAYS);
		GridPane.setHgrow(this.symbolColorPicker,  Priority.ALWAYS);
		GridPane.setHgrow(this.lineWidthComboBox,  Priority.ALWAYS);

		int row = 0;		
		gridPane.add(symbolSizeLabel,    0, row);
		gridPane.add(this.symbolSizeComboBox, 1, row++);
		
		gridPane.add(symbolColorLabel,  0, row);
		gridPane.add(this.symbolColorPicker, 1, row++);
		
		gridPane.add(symbolLineWidthLabel, 0, row);
		gridPane.add(this.lineWidthComboBox,    1, row++);

		return this.createTitledPane(i18n.getString("UIObservationLayerPropertyBuilder.symbol.title", "Symbol properties"), gridPane);
	}
	

}
