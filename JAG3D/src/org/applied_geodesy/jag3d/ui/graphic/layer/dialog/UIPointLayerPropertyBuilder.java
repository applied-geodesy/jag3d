package org.applied_geodesy.jag3d.ui.graphic.layer.dialog;

import org.applied_geodesy.jag3d.ui.graphic.layer.PointLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.PointSymbolType;
import org.applied_geodesy.util.i18.I18N;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class UIPointLayerPropertyBuilder extends UILayerPropertyBuilder {
	private static I18N i18n = I18N.getInstance();
	private static UIPointLayerPropertyBuilder pointLayerPropertyBuilder = new UIPointLayerPropertyBuilder();
	
	private ComboBox<PointSymbolType> symbolTypeComboBox;
	private ComboBox<Double> symbolSizeComboBox;
	private ComboBox<Double> lineWidthComboBox;
	private ColorPicker symbolColorPicker;
	
	private ComboBox<String> fontFamilyComboBox;
	private ComboBox<Double> fontSizeComboBox;
	private ColorPicker fontColorPicker;
	
	private CheckBox point1DVisibleCheckBox;
	private CheckBox point2DVisibleCheckBox;
	private CheckBox point3DVisibleCheckBox;
	
	private PointLayer currentLayer = null;
	
	private VBox propertyPane = null;
	
	private UIPointLayerPropertyBuilder() {}
	
	private void init() {
		if (this.propertyPane != null)
			return;
		
		this.propertyPane = new VBox(20);
		this.propertyPane.setMaxWidth(Double.MAX_VALUE);
		this.propertyPane.getChildren().addAll(
				this.createSymbolPane(),
				this.createFontPane(),
				this.createPointDimensionVisibilityPane()
				);
	}
	
	public static Node getLayerPropertyPane(PointLayer layer) {
		pointLayerPropertyBuilder.init();
		pointLayerPropertyBuilder.bindProperties(layer);
		return pointLayerPropertyBuilder.propertyPane;
	}
	
	private void bindProperties(PointLayer layer) {
		// unbind
		if (this.currentLayer != null) {
			this.currentLayer.symbolSizeProperty().unbind();
			this.currentLayer.lineWidthProperty().unbind();
			this.currentLayer.pointSymbolTypeProperty().unbind();
			this.currentLayer.colorProperty().unbind();
			
			this.currentLayer.fontFamilyProperty().unbind();
			this.currentLayer.fontSizeProperty().unbind();
			this.currentLayer.fontColorProperty().unbind();
			
			this.currentLayer.point1DVisibleProperty().unbind();
			this.currentLayer.point2DVisibleProperty().unbind();
			this.currentLayer.point3DVisibleProperty().unbind();
		}
		
		// set new layer
		this.currentLayer = layer;
		
		// Symbol properties
		this.symbolTypeComboBox.getSelectionModel().select(this.currentLayer.getPointSymbolType());
		this.symbolSizeComboBox.getSelectionModel().select(this.currentLayer.getSymbolSize());
		this.lineWidthComboBox.getSelectionModel().select(this.currentLayer.getLineWidth());
		this.symbolColorPicker.setValue(this.currentLayer.getColor());
		
		this.currentLayer.symbolSizeProperty().bind(this.symbolSizeComboBox.getSelectionModel().selectedItemProperty());
		this.currentLayer.lineWidthProperty().bind(this.lineWidthComboBox.getSelectionModel().selectedItemProperty());
		this.currentLayer.pointSymbolTypeProperty().bind(this.symbolTypeComboBox.getSelectionModel().selectedItemProperty());
		this.currentLayer.colorProperty().bind(this.symbolColorPicker.valueProperty());
		
		// Font properties
		this.fontFamilyComboBox.getSelectionModel().select(this.currentLayer.getFontFamily());
		this.fontSizeComboBox.getSelectionModel().select(this.currentLayer.getFontSize());
		this.fontColorPicker.setValue(this.currentLayer.getFontColor());
		
		this.currentLayer.fontFamilyProperty().bind(this.fontFamilyComboBox.getSelectionModel().selectedItemProperty());
		this.currentLayer.fontSizeProperty().bind(this.fontSizeComboBox.getSelectionModel().selectedItemProperty());
		this.currentLayer.fontColorProperty().bind(this.fontColorPicker.valueProperty());
		
		// Visibility properties w.r.t. dimension
		this.point1DVisibleCheckBox.setSelected(this.currentLayer.isPoint1DVisible());
		this.point2DVisibleCheckBox.setSelected(this.currentLayer.isPoint2DVisible());
		this.point3DVisibleCheckBox.setSelected(this.currentLayer.isPoint3DVisible());
		
		this.currentLayer.point1DVisibleProperty().bind(this.point1DVisibleCheckBox.selectedProperty());
		this.currentLayer.point2DVisibleProperty().bind(this.point2DVisibleCheckBox.selectedProperty());
		this.currentLayer.point3DVisibleProperty().bind(this.point3DVisibleCheckBox.selectedProperty());
	}
	
	private Node createFontPane() {
		GridPane gridPane = this.createGridPane();

		Label fontFamilyLabel = new Label(i18n.getString("UIPointLayerPropertyBuilder.font.family.label", "Font family:"));
		fontFamilyLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label fontSizeLabel = new Label(i18n.getString("UIPointLayerPropertyBuilder.font.size.label", "Font size:"));
		fontSizeLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label fontColorLabel = new Label(i18n.getString("UIPointLayerPropertyBuilder.font.color.label", "Font color:"));
		fontColorLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Double fontSizes[] = new Double[10];
		for (int i = 0; i < fontSizes.length; i++)
			fontSizes[i] = 6.0 + 2*i; //fontSizes[i] = 5 + 0.5 * i;
		
		this.fontFamilyComboBox = this.createFontFamliyComboBox(i18n.getString("UIPointLayerPropertyBuilder.symbol.type.tooltip", "Selected symbol"));
		this.fontSizeComboBox   = this.createSizeComboBox(i18n.getString("UIPointLayerPropertyBuilder.font.size.tooltip", "Selected font size"), fontSizes, 1);
		this.fontColorPicker    = new ColorPicker(Color.BLACK);
		this.fontColorPicker.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.fontColorPicker.setMaxWidth(Double.MAX_VALUE);
		this.fontColorPicker.getStyleClass().add("split-button");
		
		fontFamilyLabel.setLabelFor(this.fontFamilyComboBox);
		fontSizeLabel.setLabelFor(this.fontSizeComboBox);
		fontColorLabel.setLabelFor(this.fontColorPicker);
		
		GridPane.setHgrow(fontFamilyLabel, Priority.NEVER);
		GridPane.setHgrow(fontSizeLabel,   Priority.NEVER);
		GridPane.setHgrow(fontColorLabel,   Priority.NEVER);
		
		GridPane.setHgrow(this.fontFamilyComboBox, Priority.ALWAYS);
		GridPane.setHgrow(this.fontSizeComboBox,   Priority.ALWAYS);
		GridPane.setHgrow(this.fontColorPicker,    Priority.ALWAYS);

		int row = 0;
		gridPane.add(fontFamilyLabel,         0, row);
		gridPane.add(this.fontFamilyComboBox, 1, row++);
		
		gridPane.add(fontSizeLabel,         0, row);
		gridPane.add(this.fontSizeComboBox, 1, row++);
		
		gridPane.add(fontColorLabel,       0, row);
		gridPane.add(this.fontColorPicker, 1, row++);

		return this.createTitledPane(i18n.getString("UIPointLayerPropertyBuilder.font.title", "Font properties"), gridPane);
	}

	private Node createSymbolPane() {
		GridPane gridPane = this.createGridPane();
		
		Label symbolTypeLabel = new Label(i18n.getString("UIPointLayerPropertyBuilder.symbol.type.label", "Symbol type:"));
		symbolTypeLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label symbolSizeLabel = new Label(i18n.getString("UIPointLayerPropertyBuilder.symbol.size.label", "Symbol size:"));
		symbolSizeLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label symbolColorLabel = new Label(i18n.getString("UIPointLayerPropertyBuilder.symbol.color.label", "Symbol color:"));
		symbolColorLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label symbolLineWidthLabel = new Label(i18n.getString("UIPointLayerPropertyBuilder.symbol.linewidth.label", "Line width:"));
		symbolLineWidthLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Double symbolSizes[] = new Double[21];
		for (int i = 0; i < symbolSizes.length; i++)
			symbolSizes[i] = 5 + 0.5 * i;
		
		this.symbolTypeComboBox = this.createSymbolComboBox(i18n.getString("UIPointLayerPropertyBuilder.symbol.type.tooltip", "Selected symbol"));
		this.symbolSizeComboBox = this.createSizeComboBox(i18n.getString("UIPointLayerPropertyBuilder.symbol.size.tooltip", "Selected symbol size"), symbolSizes, 1);
		this.lineWidthComboBox  = this.createLineWidthComboBox(i18n.getString("UIPointLayerPropertyBuilder.symbol.linewidth.tooltip", "Selected line width"));
		this.symbolColorPicker = new ColorPicker(Color.DARKBLUE);
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
		
		GridPane.setHgrow(this.symbolTypeComboBox, Priority.ALWAYS);
		GridPane.setHgrow(this.symbolSizeComboBox, Priority.ALWAYS);
		GridPane.setHgrow(this.symbolColorPicker,  Priority.ALWAYS);
		GridPane.setHgrow(this.lineWidthComboBox,  Priority.ALWAYS);

		int row = 0;
		gridPane.add(symbolTypeLabel,    0, row);
		gridPane.add(this.symbolTypeComboBox, 1, row++);
		
		gridPane.add(symbolSizeLabel,    0, row);
		gridPane.add(this.symbolSizeComboBox, 1, row++);
		
		gridPane.add(symbolColorLabel,  0, row);
		gridPane.add(this.symbolColorPicker, 1, row++);
		
		gridPane.add(symbolLineWidthLabel, 0, row);
		gridPane.add(this.lineWidthComboBox,    1, row++);

		return this.createTitledPane(i18n.getString("UIPointLayerPropertyBuilder.symbol.title", "Symbol properties"), gridPane);
	}
	
	private Node createPointDimensionVisibilityPane() {
		GridPane gridPane = this.createGridPane();

		this.point1DVisibleCheckBox = this.createCheckBox(
				i18n.getString("UIPointLayerPropertyBuilder.dimension.1d.label", "1D points"), 
				i18n.getString("UIPointLayerPropertyBuilder.dimension.1d.tooltip", "If checked, 1D points will be drawn.")
				);
		
		this.point2DVisibleCheckBox = this.createCheckBox(
				i18n.getString("UIPointLayerPropertyBuilder.dimension.2d.label", "2D points"), 
				i18n.getString("UIPointLayerPropertyBuilder.dimension.2d.tooltip", "If checked, 2D points will be drawn.")
				);
		
		this.point3DVisibleCheckBox = this.createCheckBox(
				i18n.getString("UIPointLayerPropertyBuilder.dimension.3d.label", "3D points"), 
				i18n.getString("UIPointLayerPropertyBuilder.dimension.3d.tooltip", "If checked, 3D points will be drawn.")
				);
		
		GridPane.setHgrow(this.point1DVisibleCheckBox, Priority.NEVER);
		GridPane.setHgrow(this.point2DVisibleCheckBox, Priority.NEVER);
		GridPane.setHgrow(this.point3DVisibleCheckBox, Priority.NEVER);
		
		int row = 0;
		gridPane.add(this.point1DVisibleCheckBox, 0, row++);
		gridPane.add(this.point2DVisibleCheckBox, 0, row++);
		gridPane.add(this.point3DVisibleCheckBox, 0, row++);
		
		Region spacer = new Region();
		GridPane.setHgrow(spacer, Priority.ALWAYS);
		gridPane.add(spacer, 1, 0, 1, row);
		
		return this.createTitledPane(i18n.getString("UIPointLayerPropertyBuilder.dimension.title", "Visibility properties"), gridPane);
	}
	
	private ComboBox<PointSymbolType> createSymbolComboBox(String tooltip) {
		ComboBox<PointSymbolType> typeComboBox = new ComboBox<PointSymbolType>();
		typeComboBox.getItems().setAll(PointSymbolType.values());
		typeComboBox.getSelectionModel().select(0);
		typeComboBox.setCellFactory(new Callback<ListView<PointSymbolType>, ListCell<PointSymbolType>>() {
            @Override 
            public ListCell<PointSymbolType> call(ListView<PointSymbolType> param) {
				return new PointSymbolTypeListCell();
            }
		});
		typeComboBox.setButtonCell(new PointSymbolTypeListCell());
		typeComboBox.setTooltip(new Tooltip(tooltip));
		typeComboBox.setMaxWidth(Double.MAX_VALUE);
		return typeComboBox;
	}
}
