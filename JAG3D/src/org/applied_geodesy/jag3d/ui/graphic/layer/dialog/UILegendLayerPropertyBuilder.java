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
import org.applied_geodesy.jag3d.ui.graphic.layer.LegendLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.LegendPositionType;
import org.applied_geodesy.jag3d.ui.i18n.I18N;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

public class UILegendLayerPropertyBuilder extends UILayerPropertyBuilder {

	private class FontSizeChangeListener implements ChangeListener<Double> {
		@Override
		public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setFontSize(newValue);
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
	
	private class FontFamilyChangeListener implements ChangeListener<String> {
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setFontFamily(newValue);
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
	
	private class LegendPositionTypeChangeListener implements ChangeListener<LegendPositionType> {
		@Override
		public void changed(ObservableValue<? extends LegendPositionType> observable, LegendPositionType oldValue, LegendPositionType newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setLegendPositionType(newValue);
				legendPositionTypeComboBox.setValue(newValue);
				layerManager.draw();
			}
		}
	}
	
	private I18N i18n = I18N.getInstance();
	private static UILegendLayerPropertyBuilder legendLayerPropertyBuilder = new UILegendLayerPropertyBuilder();
	

	private ColorPicker fontColorPicker;
	private ComboBox<String> fontFamilyComboBox;
	private ComboBox<Double> fontSizeComboBox;
	
	private ComboBox<Double> lineWidthComboBox;
	private ColorPicker borderColorPicker;
	private ComboBox<LegendPositionType> legendPositionTypeComboBox;
	
	private LegendLayer currentLayer = null;
	private LayerManager layerManager = null;
	
	private VBox propertyPane = null;
	
	private UILegendLayerPropertyBuilder() {}
	
	private void init() {
		if (this.propertyPane != null)
			return;
		
		this.propertyPane = new VBox(20);
		this.propertyPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.propertyPane.setMinHeight(Control.USE_PREF_SIZE);
		this.propertyPane.getChildren().addAll(
				this.createSymbolPane(),
				this.createFontPane()
				);
	}
	
	public static Node getLayerPropertyPane(LayerManager layerManager, LegendLayer layer) {
		legendLayerPropertyBuilder.layerManager = null;
		legendLayerPropertyBuilder.init();
		legendLayerPropertyBuilder.set(layer);
		legendLayerPropertyBuilder.layerManager = layerManager;
		return legendLayerPropertyBuilder.propertyPane;
	}
		
	private void set(LegendLayer layer) {	
		// set new layer
		this.currentLayer = layer;
		
		// Font properties
		this.fontFamilyComboBox.getSelectionModel().select(this.currentLayer.getFontFamily());
		this.fontSizeComboBox.getSelectionModel().select(this.currentLayer.getFontSize());
		this.fontColorPicker.setValue(this.currentLayer.getFontColor());
		
		// border properties
		this.lineWidthComboBox.getSelectionModel().select(this.currentLayer.getLineWidth());
		this.borderColorPicker.setValue(this.currentLayer.getColor());
		this.legendPositionTypeComboBox.getSelectionModel().select(this.currentLayer.getLegendPositionType());
	}
	
	private Node createSymbolPane() {
		GridPane gridPane = this.createGridPane();
				
		Label borderColorLabel = new Label(i18n.getString("UILegendLayerPropertyBuilder.border.color.label", "Border color:"));
		borderColorLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label borderLineWidthLabel = new Label(i18n.getString("UILegendLayerPropertyBuilder.border.linewidth.label", "Border width:"));
		borderLineWidthLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label legendPositionTypeLabel = new Label(i18n.getString("UILegendLayerPropertyBuilder.legendposition.label", "Position:"));
		legendPositionTypeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Double symbolSizes[] = new Double[21];
		for (int i = 0; i < symbolSizes.length; i++)
			symbolSizes[i] = 5 + 0.5 * i;
		
		this.lineWidthComboBox          = this.createLineWidthComboBox(i18n.getString("UILegendLayerPropertyBuilder.border.linewidth.tooltip", "Set line width"));
		this.legendPositionTypeComboBox = this.createEstimationTypeComboBox(LegendPositionType.NORTH_EAST, i18n.getString("UILegendLayerPropertyBuilder.legendposition.tooltip", "Set legend position"));
		this.borderColorPicker          = this.createColorPicker(Color.LIGHTSLATEGREY, i18n.getString("UILegendLayerPropertyBuilder.border.color.tooltip", "Set legend border color"));
		
		// add listeners
		this.lineWidthComboBox.getSelectionModel().selectedItemProperty().addListener(new LineWidthChangeListener());
		this.legendPositionTypeComboBox.getSelectionModel().selectedItemProperty().addListener(new LegendPositionTypeChangeListener());
		this.borderColorPicker.valueProperty().addListener(new ColorChangeListener());
		
		borderColorLabel.setLabelFor(this.borderColorPicker);
		borderLineWidthLabel.setLabelFor(this.lineWidthComboBox);
		legendPositionTypeLabel.setLabelFor(this.legendPositionTypeComboBox);
		
		GridPane.setHgrow(borderColorLabel,        Priority.NEVER);
		GridPane.setHgrow(borderLineWidthLabel,    Priority.NEVER);
		GridPane.setHgrow(legendPositionTypeLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.borderColorPicker,          Priority.ALWAYS);
		GridPane.setHgrow(this.lineWidthComboBox,          Priority.ALWAYS);
		GridPane.setHgrow(this.legendPositionTypeComboBox, Priority.ALWAYS);

		int row = 0;		
		gridPane.add(borderColorLabel,       0, row);
		gridPane.add(this.borderColorPicker, 1, row++);
		
		gridPane.add(borderLineWidthLabel,   0, row);
		gridPane.add(this.lineWidthComboBox, 1, row++);

		gridPane.add(legendPositionTypeLabel,         0, row);
		gridPane.add(this.legendPositionTypeComboBox, 1, row++);
		
		return this.createTitledPane(i18n.getString("UILegendLayerPropertyBuilder.border.title", "Border properties"), gridPane);
	}
	
	private Node createFontPane() {
		GridPane gridPane = this.createGridPane();

		Label fontFamilyLabel = new Label(i18n.getString("UILegendLayerPropertyBuilder.font.family.label", "Font family:"));
		fontFamilyLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label fontSizeLabel = new Label(i18n.getString("UILegendLayerPropertyBuilder.font.size.label", "Font size:"));
		fontSizeLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label fontColorLabel = new Label(i18n.getString("UILegendLayerPropertyBuilder.font.color.label", "Font color:"));
		fontColorLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Double fontSizes[] = new Double[10];
		for (int i = 0; i < fontSizes.length; i++)
			fontSizes[i] = 6.0 + 2*i; //fontSizes[i] = 5 + 0.5 * i;
		
		this.fontFamilyComboBox = this.createFontFamliyComboBox(i18n.getString("UILegendLayerPropertyBuilder.font.family.tooltip", "Set font familiy"));
		this.fontSizeComboBox   = this.createSizeComboBox(i18n.getString("UILegendLayerPropertyBuilder.font.size.tooltip", "Set font size"), fontSizes, 1);
		this.fontColorPicker    = this.createColorPicker(Color.SLATEGREY, i18n.getString("UILegendLayerPropertyBuilder.font.color.tooltip", "Set font color"));
		
		this.fontFamilyComboBox.getSelectionModel().selectedItemProperty().addListener(new FontFamilyChangeListener());
		this.fontSizeComboBox.getSelectionModel().selectedItemProperty().addListener(new FontSizeChangeListener());
		this.fontColorPicker.valueProperty().addListener(new FontColorChangeListener());
		
		fontFamilyLabel.setLabelFor(this.fontFamilyComboBox);
		fontSizeLabel.setLabelFor(this.fontSizeComboBox);
		fontColorLabel.setLabelFor(this.fontColorPicker);
		
		GridPane.setHgrow(fontFamilyLabel, Priority.NEVER);
		GridPane.setHgrow(fontSizeLabel,   Priority.NEVER);
		GridPane.setHgrow(fontColorLabel,  Priority.NEVER);
		
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

		return this.createTitledPane(i18n.getString("UILegendLayerPropertyBuilder.font.title", "Font properties"), gridPane);
	}
	
	private ComboBox<LegendPositionType> createEstimationTypeComboBox(LegendPositionType item, String tooltip) {
		ComboBox<LegendPositionType> typeComboBox = new ComboBox<LegendPositionType>();
		typeComboBox.getItems().setAll(LegendPositionType.values()); 
		typeComboBox.getSelectionModel().select(item);
		typeComboBox.setConverter(new StringConverter<LegendPositionType>() {

			@Override
			public String toString(LegendPositionType type) {
				if (type == null)
					return null;
				switch(type) {
				
				case NORTH:
					return i18n.getString("UILegendLayerPropertyBuilder.legendposition.north.label", "Noth");
				case EAST:
					return i18n.getString("UILegendLayerPropertyBuilder.legendposition.east.label", "East");
				case SOUTH:
					return i18n.getString("UILegendLayerPropertyBuilder.legendposition.south.label", "South");
				case WEST:
					return i18n.getString("UILegendLayerPropertyBuilder.legendposition.west.label", "West");
				
				case NORTH_EAST:
					return i18n.getString("UILegendLayerPropertyBuilder.legendposition.northeast.label", "Northeast");
				case NORTH_WEST:
					return i18n.getString("UILegendLayerPropertyBuilder.legendposition.northwest.label", "Northwest");
				case SOUTH_EAST:
					return i18n.getString("UILegendLayerPropertyBuilder.legendposition.southeast.label", "Southeast");
				case SOUTH_WEST:
					return i18n.getString("UILegendLayerPropertyBuilder.legendposition.southwest.label", "Southwest");	
					
				}
				return null;
			}

			@Override
			public LegendPositionType fromString(String string) {
				return LegendPositionType.valueOf(string);
			}
		});
		typeComboBox.setTooltip(new Tooltip(tooltip));
		typeComboBox.setMaxWidth(Double.MAX_VALUE);
		return typeComboBox;
	}
}