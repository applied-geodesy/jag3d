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
import org.applied_geodesy.util.i18.I18N;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

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
	
	private I18N i18n = I18N.getInstance();
	private static UILegendLayerPropertyBuilder legendLayerPropertyBuilder = new UILegendLayerPropertyBuilder();
	

	private ColorPicker fontColorPicker;
	private ComboBox<String> fontFamilyComboBox;
	private ComboBox<Double> fontSizeComboBox;
	
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
}