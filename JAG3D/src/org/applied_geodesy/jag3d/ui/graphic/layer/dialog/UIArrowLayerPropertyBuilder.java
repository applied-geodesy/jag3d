package org.applied_geodesy.jag3d.ui.graphic.layer.dialog;

import org.applied_geodesy.jag3d.ui.graphic.layer.ArrowLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.ArrowSymbolType;
import org.applied_geodesy.util.i18.I18N;

import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class UIArrowLayerPropertyBuilder extends UILayerPropertyBuilder {
	
	private static I18N i18n = I18N.getInstance();
	private static UIArrowLayerPropertyBuilder arrowLayerPropertyBuilder = new UIArrowLayerPropertyBuilder();
	private ComboBox<ArrowSymbolType> symbolTypeComboBox;
	private ComboBox<Double> symbolSizeComboBox;
	private ComboBox<Double> lineWidthComboBox;
	private ColorPicker symbolColorPicker;
	private ArrowLayer currentLayer = null;
	private VBox propertyPane = null;
	
	private UIArrowLayerPropertyBuilder() {}
	
	private void init() {
		if (this.propertyPane != null)
			return;
		
		this.propertyPane = new VBox(20);
		this.propertyPane.setMaxWidth(Double.MAX_VALUE);
		this.propertyPane.getChildren().addAll(
				this.createSymbolPane()
//				,
//				this.createFontPane(),
//				this.createPointDimensionVisibilityPane()
				);
	}
	
	public static Node getLayerPropertyPane(ArrowLayer layer) {
		arrowLayerPropertyBuilder.init();
		arrowLayerPropertyBuilder.bindProperties(layer);
		return arrowLayerPropertyBuilder.propertyPane;
	}
	
	private void unbindProperties() {
		if (this.currentLayer != null) {
			this.currentLayer.symbolSizeProperty().unbind();
			this.currentLayer.lineWidthProperty().unbind();
			this.currentLayer.vectorScaleProperty().unbind();
			this.currentLayer.colorProperty().unbind();
			this.currentLayer.arrowSymbolTypeProperty().unbind();
			
//			this.currentLayer.fontFamilyProperty().unbind();
//			this.currentLayer.fontSizeProperty().unbind();
//			this.currentLayer.fontColorProperty().unbind();
		}
	}
	
	private void bindProperties(ArrowLayer layer) {
		// unbind
		this.unbindProperties();
		
		// set new layer
		this.currentLayer = layer;
		
		// Symbol properties
		this.symbolSizeComboBox.getSelectionModel().select(this.currentLayer.getSymbolSize());
		this.lineWidthComboBox.getSelectionModel().select(this.currentLayer.getLineWidth());
		this.symbolColorPicker.setValue(this.currentLayer.getColor());
		this.symbolTypeComboBox.setValue(this.currentLayer.getArrowSymbolType());
		
		this.currentLayer.symbolSizeProperty().bind(this.symbolSizeComboBox.getSelectionModel().selectedItemProperty());
		this.currentLayer.lineWidthProperty().bind(this.lineWidthComboBox.getSelectionModel().selectedItemProperty());
		this.currentLayer.colorProperty().bind(this.symbolColorPicker.valueProperty());
		this.currentLayer.arrowSymbolTypeProperty().bind(this.symbolTypeComboBox.valueProperty());
		
		//this.currentLayer.vectorScaleProperty().bind(this.vectorScaleComboBox.getSelectionModel().selectedItemProperty());
		
		// Font properties
//		this.fontFamilyComboBox.getSelectionModel().select(this.currentLayer.getFontFamily());
//		this.fontSizeComboBox.getSelectionModel().select(this.currentLayer.getFontSize());
//		this.fontColorPicker.setValue(this.currentLayer.getFontColor());
//		
//		this.currentLayer.fontFamilyProperty().bind(this.fontFamilyComboBox.getSelectionModel().selectedItemProperty());
//		this.currentLayer.fontSizeProperty().bind(this.fontSizeComboBox.getSelectionModel().selectedItemProperty());
//		this.currentLayer.fontColorProperty().bind(this.fontColorPicker.valueProperty());
	}
	
	private Node createSymbolPane() {
		GridPane gridPane = this.createGridPane();
		
		Label symbolTypeLabel = new Label(i18n.getString("UIArrowLayerPropertyBuilder.symbol.type.label", "Symbol type:"));
		symbolTypeLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label symbolSizeLabel = new Label(i18n.getString("UIArrowLayerPropertyBuilder.symbol.size.label", "Symbol size:"));
		symbolSizeLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label symbolColorLabel = new Label(i18n.getString("UIArrowLayerPropertyBuilder.symbol.color.label", "Symbol color:"));
		symbolColorLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label symbolLineWidthLabel = new Label(i18n.getString("UIArrowLayerPropertyBuilder.symbol.linewidth.label", "Line width:"));
		symbolLineWidthLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Double symbolSizes[] = new Double[21];
		for (int i = 0; i < symbolSizes.length; i++)
			symbolSizes[i] = 5 + 0.5 * i;

		this.symbolTypeComboBox  = this.createSymbolComboBox(i18n.getString("UIArrowLayerPropertyBuilder.symbol.type.tooltip", "Selected symbol"));
		this.symbolSizeComboBox  = this.createSizeComboBox(i18n.getString("UIArrowLayerPropertyBuilder.symbol.size.tooltip", "Selected symbol size"), symbolSizes, 1);
		this.lineWidthComboBox   = this.createLineWidthComboBox(i18n.getString("UIArrowLayerPropertyBuilder.symbol.linewidth.tooltip", "Selected line width"));
		this.symbolColorPicker   = new ColorPicker(Color.DARKBLUE);
		this.symbolColorPicker.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.symbolColorPicker.setMaxWidth(Double.MAX_VALUE);
		this.symbolColorPicker.getStyleClass().add("split-button");

		symbolTypeLabel.setLabelFor(this.symbolTypeComboBox);
		symbolSizeLabel.setLabelFor(this.symbolSizeComboBox);
		symbolColorLabel.setLabelFor(this.symbolColorPicker);
		symbolLineWidthLabel.setLabelFor(this.lineWidthComboBox);

		GridPane.setHgrow(symbolTypeLabel,      Priority.NEVER);
		GridPane.setHgrow(symbolSizeLabel,      Priority.NEVER);
		GridPane.setHgrow(symbolColorLabel,     Priority.NEVER);
		GridPane.setHgrow(symbolLineWidthLabel, Priority.NEVER);

		GridPane.setHgrow(this.symbolTypeComboBox,  Priority.ALWAYS);
		GridPane.setHgrow(this.symbolSizeComboBox,  Priority.ALWAYS);
		GridPane.setHgrow(this.symbolColorPicker,   Priority.ALWAYS);
		GridPane.setHgrow(this.lineWidthComboBox,   Priority.ALWAYS);

		int row = 0;
		gridPane.add(symbolTypeLabel,         0, row);
		gridPane.add(this.symbolTypeComboBox, 1, row++);
		
		gridPane.add(symbolSizeLabel,         0, row);
		gridPane.add(this.symbolSizeComboBox, 1, row++);
		
		gridPane.add(symbolColorLabel,        0, row);
		gridPane.add(this.symbolColorPicker,  1, row++);
		
		gridPane.add(symbolLineWidthLabel,    0, row);
		gridPane.add(this.lineWidthComboBox,  1, row++);

		return this.createTitledPane(i18n.getString("UIArrowLayerPropertyBuilder.symbol.title", "Symbol properties"), gridPane);
	}

	private ComboBox<ArrowSymbolType> createSymbolComboBox(String tooltip) {
		ComboBox<ArrowSymbolType> typeComboBox = new ComboBox<ArrowSymbolType>();
		typeComboBox.getItems().setAll(ArrowSymbolType.values());
		typeComboBox.getSelectionModel().select(0);
		typeComboBox.setCellFactory(new Callback<ListView<ArrowSymbolType>, ListCell<ArrowSymbolType>>() {
            @Override 
            public ListCell<ArrowSymbolType> call(ListView<ArrowSymbolType> param) {
				return new ArrowSymbolTypeListCell();
            }
		});
		typeComboBox.setButtonCell(new ArrowSymbolTypeListCell());
		typeComboBox.setTooltip(new Tooltip(tooltip));
		typeComboBox.setMaxWidth(Double.MAX_VALUE);
		return typeComboBox;
	}
}
