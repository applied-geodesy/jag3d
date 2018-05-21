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

import org.applied_geodesy.jag3d.ui.graphic.layer.LayerManager;
import org.applied_geodesy.jag3d.ui.graphic.layer.PointLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.PointSymbolType;
import org.applied_geodesy.util.i18.I18N;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
	private class SymbolTypeChangeListener implements ChangeListener<PointSymbolType> {
		@Override
		public void changed(ObservableValue<? extends PointSymbolType> observable, PointSymbolType oldValue, PointSymbolType newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setSymbolType(newValue);
				layerManager.draw();
			}
		}
	}
	
	private class SymbolSizeChangeListener implements ChangeListener<Double> {
		@Override
		public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setSymbolSize(newValue);
				layerManager.draw();
			}
		}
	}
	
	private class LineWidthChangeListener implements ChangeListener<Double> {
		@Override
		public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setLineWidth(newValue);
				layerManager.draw();
			}
		}
	}
		
	private class ColorChangeListener implements ChangeListener<Color> {
		@Override
		public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setColor(newValue);
				layerManager.draw();
			}
		}
	}
	
	private class FontFamilyChangeListener implements ChangeListener<String> {
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setFontFamily(newValue);
				layerManager.draw();
			}
		}
	}
	
	private class FontColorChangeListener implements ChangeListener<Color> {
		@Override
		public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setFontColor(newValue);
				layerManager.draw();
			}
		}
	}
	
	private class FontSizeChangeListener implements ChangeListener<Double> {
		@Override
		public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setFontSize(newValue);
				layerManager.draw();
			}
		}
	}
	
	private class HighlightColorChangeListener implements ChangeListener<Color> {
		@Override
		public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setHighlightColor(newValue);
				layerManager.draw();
			}
		}
	}
	
	private class HighlightLineWidthChangeListener implements ChangeListener<Double> {
		@Override
		public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) { 
				currentLayer.setHighlightLineWidth(newValue);
				layerManager.draw();
			}
		}
	}
	
	private class PointVisibleChangeListener implements ChangeListener<Boolean> {
		private int dim;
		PointVisibleChangeListener(int dim) {
			this.dim = dim;
		}
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				switch(this.dim) {
				case 1:
					currentLayer.setPoint1DVisible(newValue);
					layerManager.draw();
					break;
				case 2:
					currentLayer.setPoint2DVisible(newValue);
					layerManager.draw();
					break;
				case 3:
					currentLayer.setPoint3DVisible(newValue);
					layerManager.draw();
					break;
				}
			}
		}
	}
	
	private I18N i18n = I18N.getInstance();
	private static UIPointLayerPropertyBuilder pointLayerPropertyBuilder = new UIPointLayerPropertyBuilder();
	
	private ComboBox<PointSymbolType> symbolTypeComboBox;
	private ComboBox<Double> symbolSizeComboBox;
	private ComboBox<Double> lineWidthComboBox;
	private ColorPicker symbolColorPicker;
	
	private ComboBox<String> fontFamilyComboBox;
	private ComboBox<Double> fontSizeComboBox;
	private ColorPicker fontColorPicker;
	
	private Node highlightPane = null;
	private ComboBox<Double> highlightLineWidthComboBox;
	private ColorPicker highlightColorPicker;
	
	private CheckBox point1DVisibleCheckBox;
	private CheckBox point2DVisibleCheckBox;
	private CheckBox point3DVisibleCheckBox;
	
	private PointLayer currentLayer = null;
	private LayerManager layerManager = null;
	
	private VBox propertyPane = null;
	
	private UIPointLayerPropertyBuilder() {}
	
	private void init() {
		if (this.propertyPane != null)
			return;
		
		this.highlightPane = this.createHighlightPane();
		this.propertyPane = new VBox(20);
		this.propertyPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.propertyPane.setMinHeight(Control.USE_PREF_SIZE);
		this.propertyPane.getChildren().addAll(
				this.createSymbolPane(),
				this.createFontPane(),
				this.createPointDimensionVisibilityPane(),
				this.highlightPane
				);
	}
	
	public static Node getLayerPropertyPane(LayerManager layerManager, PointLayer layer) {
		pointLayerPropertyBuilder.layerManager = null;
		pointLayerPropertyBuilder.init();
		pointLayerPropertyBuilder.set(layer);
		pointLayerPropertyBuilder.layerManager = layerManager;
		return pointLayerPropertyBuilder.propertyPane;
	}
		
	private void set(PointLayer layer) {	
		// set new layer
		this.currentLayer = layer;
		
		// Symbol properties
		this.symbolTypeComboBox.getSelectionModel().select(this.currentLayer.getPointSymbolType());
		this.symbolSizeComboBox.getSelectionModel().select(this.currentLayer.getSymbolSize());
		this.lineWidthComboBox.getSelectionModel().select(this.currentLayer.getLineWidth());
		this.symbolColorPicker.setValue(this.currentLayer.getColor());
				
		switch(this.currentLayer.getLayerType()) {
		case DATUM_POINT_APOSTERIORI:
		case REFERENCE_POINT_APOSTERIORI:
		case STOCHASTIC_POINT_APOSTERIORI:
			this.highlightColorPicker.setValue(this.currentLayer.getHighlightColor());
			this.highlightLineWidthComboBox.getSelectionModel().select(this.currentLayer.getHighlightLineWidth());
			this.highlightPane.setVisible(true);
			this.highlightPane.setManaged(true);
			break;
			
		default:
			this.highlightPane.setVisible(false);
			this.highlightPane.setManaged(false);
			break;
		}
		
		// Font properties
		this.fontFamilyComboBox.getSelectionModel().select(this.currentLayer.getFontFamily());
		this.fontSizeComboBox.getSelectionModel().select(this.currentLayer.getFontSize());
		this.fontColorPicker.setValue(this.currentLayer.getFontColor());
		
		// Visibility properties w.r.t. dimension
		this.point1DVisibleCheckBox.setSelected(this.currentLayer.isPoint1DVisible());
		this.point2DVisibleCheckBox.setSelected(this.currentLayer.isPoint2DVisible());
		this.point3DVisibleCheckBox.setSelected(this.currentLayer.isPoint3DVisible());
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
		
		this.fontFamilyComboBox = this.createFontFamliyComboBox(i18n.getString("UIPointLayerPropertyBuilder.font.family.tooltip", "Set font familiy"));
		this.fontSizeComboBox   = this.createSizeComboBox(i18n.getString("UIPointLayerPropertyBuilder.font.size.tooltip", "Set font size"), fontSizes, 1);
		this.fontColorPicker    = new ColorPicker(Color.BLACK);
		this.fontColorPicker.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.fontColorPicker.setMaxWidth(Double.MAX_VALUE);
		this.fontColorPicker.getStyleClass().add("split-button");
		
		this.fontFamilyComboBox.getSelectionModel().selectedItemProperty().addListener(new FontFamilyChangeListener());
		this.fontSizeComboBox.getSelectionModel().selectedItemProperty().addListener(new FontSizeChangeListener());
		this.fontColorPicker.valueProperty().addListener(new FontColorChangeListener());
		
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
		symbolTypeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label symbolSizeLabel = new Label(i18n.getString("UIPointLayerPropertyBuilder.symbol.size.label", "Symbol size:"));
		symbolSizeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label symbolColorLabel = new Label(i18n.getString("UIPointLayerPropertyBuilder.symbol.color.label", "Symbol color:"));
		symbolColorLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label symbolLineWidthLabel = new Label(i18n.getString("UIPointLayerPropertyBuilder.symbol.linewidth.label", "Line width:"));
		symbolLineWidthLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Double symbolSizes[] = new Double[21];
		for (int i = 0; i < symbolSizes.length; i++)
			symbolSizes[i] = 5 + 0.5 * i;
		
		this.symbolTypeComboBox = this.createSymbolComboBox(i18n.getString("UIPointLayerPropertyBuilder.symbol.type.tooltip", "Set point symbol type"));
		this.symbolSizeComboBox = this.createSizeComboBox(i18n.getString("UIPointLayerPropertyBuilder.symbol.size.tooltip", "Set point symbol size"), symbolSizes, 1);
		this.lineWidthComboBox  = this.createLineWidthComboBox(i18n.getString("UIPointLayerPropertyBuilder.symbol.linewidth.tooltip", "Set line width"));
		
		this.symbolColorPicker = new ColorPicker(Color.DARKBLUE);
		this.symbolColorPicker.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.symbolColorPicker.setMaxWidth(Double.MAX_VALUE);
		this.symbolColorPicker.getStyleClass().add("split-button");
		
		// add listeners
		this.symbolTypeComboBox.getSelectionModel().selectedItemProperty().addListener(new SymbolTypeChangeListener());
		this.symbolSizeComboBox.getSelectionModel().selectedItemProperty().addListener(new SymbolSizeChangeListener());
		this.lineWidthComboBox.getSelectionModel().selectedItemProperty().addListener(new LineWidthChangeListener());
		this.symbolColorPicker.valueProperty().addListener(new ColorChangeListener());
		
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
		gridPane.add(symbolTypeLabel,         0, row);
		gridPane.add(this.symbolTypeComboBox, 1, row++);
		
		gridPane.add(symbolSizeLabel,         0, row);
		gridPane.add(this.symbolSizeComboBox, 1, row++);
		
		gridPane.add(symbolColorLabel,       0, row);
		gridPane.add(this.symbolColorPicker, 1, row++);
		
		gridPane.add(symbolLineWidthLabel,   0, row);
		gridPane.add(this.lineWidthComboBox, 1, row++);

		return this.createTitledPane(i18n.getString("UIPointLayerPropertyBuilder.symbol.title", "Symbol properties"), gridPane);
	}
	
	private Node createHighlightPane() {
		GridPane gridPane = this.createGridPane();
		
		Label highlightColorLabel = new Label(i18n.getString("UIPointLayerPropertyBuilder.highlight.color.label", "Highlight color:"));
		highlightColorLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label highlightLineWidthLabel = new Label(i18n.getString("UIPointLayerPropertyBuilder.highlight.linewidth.label", "Line width:"));
		highlightLineWidthLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		this.highlightLineWidthComboBox = this.createLineWidthComboBox(i18n.getString("UIPointLayerPropertyBuilder.highlight.linewidth.tooltip", "Set line width for highlighting"));
				
		this.highlightColorPicker  = new ColorPicker(Color.DARKBLUE);
		this.highlightColorPicker.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.highlightColorPicker.setMaxWidth(Double.MAX_VALUE);
		this.highlightColorPicker.getStyleClass().add("split-button");
		
		// add listener
		this.highlightColorPicker.valueProperty().addListener(new HighlightColorChangeListener());
		this.highlightLineWidthComboBox.getSelectionModel().selectedItemProperty().addListener(new HighlightLineWidthChangeListener());
		
		highlightColorLabel.setLabelFor(this.highlightColorPicker);
		highlightLineWidthLabel.setLabelFor(this.highlightLineWidthComboBox);
		
		GridPane.setHgrow(highlightColorLabel,     Priority.NEVER);
		GridPane.setHgrow(highlightLineWidthLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.highlightColorPicker,       Priority.ALWAYS);
		GridPane.setHgrow(this.highlightLineWidthComboBox, Priority.ALWAYS);
		
		int row = 0;
		gridPane.add(highlightColorLabel,       0, row);
		gridPane.add(this.highlightColorPicker, 1, row++);
		
		gridPane.add(highlightLineWidthLabel,         0, row);
		gridPane.add(this.highlightLineWidthComboBox, 1, row++);

		return this.createTitledPane(i18n.getString("UIPointLayerPropertyBuilder.highlight.title", "Highlight properties"), gridPane);
	}
	
	private Node createPointDimensionVisibilityPane() {
		GridPane gridPane = this.createGridPane();

		this.point1DVisibleCheckBox = this.createCheckBox(
				i18n.getString("UIPointLayerPropertyBuilder.dimension.1d.label", "1D points"), 
				i18n.getString("UIPointLayerPropertyBuilder.dimension.1d.tooltip", "If checked, 1D points will be drawn")
				);
		
		this.point2DVisibleCheckBox = this.createCheckBox(
				i18n.getString("UIPointLayerPropertyBuilder.dimension.2d.label", "2D points"), 
				i18n.getString("UIPointLayerPropertyBuilder.dimension.2d.tooltip", "If checked, 2D points will be drawn")
				);
		
		this.point3DVisibleCheckBox = this.createCheckBox(
				i18n.getString("UIPointLayerPropertyBuilder.dimension.3d.label", "3D points"), 
				i18n.getString("UIPointLayerPropertyBuilder.dimension.3d.tooltip", "If checked, 3D points will be drawn")
				);
		
		this.point1DVisibleCheckBox.selectedProperty().addListener(new PointVisibleChangeListener(1));
		this.point2DVisibleCheckBox.selectedProperty().addListener(new PointVisibleChangeListener(2));
		this.point3DVisibleCheckBox.selectedProperty().addListener(new PointVisibleChangeListener(3));
		
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
