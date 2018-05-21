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

import org.applied_geodesy.jag3d.ui.graphic.layer.ConfidenceLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.LayerManager;
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

public class UIConfidenceLayerPropertyBuilder extends UILayerPropertyBuilder {

	private class LineWidthChangeListener implements ChangeListener<Double> {
		@Override
		public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setLineWidth(newValue);
				layerManager.draw();
			}
		}
	}
	
	private class StrokeColorChangeListener implements ChangeListener<Color> {
		@Override
		public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setStrokeColor(newValue);
				layerManager.draw();
			}
		}
	}
	
	private class FillColorChangeListener implements ChangeListener<Color> {
		@Override
		public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setColor(newValue);
				layerManager.draw();
			}
		}
	}
	
	private I18N i18n = I18N.getInstance();
	private static UIConfidenceLayerPropertyBuilder confidenceLayerPropertyBuilder = new UIConfidenceLayerPropertyBuilder();

	private ComboBox<Double> lineWidthComboBox;
	private ColorPicker symbolStrokeColorPicker;
	private ColorPicker symbolFillColorPicker;
	private ConfidenceLayer<?> currentLayer = null;
	private VBox propertyPane = null;
	private LayerManager layerManager = null;

	private UIConfidenceLayerPropertyBuilder() {}
	
	private void init() {
		if (this.propertyPane != null)
			return;
		
		this.propertyPane = new VBox(20);
		this.propertyPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.propertyPane.setMinWidth(Control.USE_PREF_SIZE);
		this.propertyPane.getChildren().addAll(
				this.createSymbolPane()
				);
	}
	
	public static Node getLayerPropertyPane(LayerManager layerManager, ConfidenceLayer<?> layer) {
		confidenceLayerPropertyBuilder.layerManager = null;
		confidenceLayerPropertyBuilder.init();
		confidenceLayerPropertyBuilder.set(layer);
		confidenceLayerPropertyBuilder.layerManager = layerManager;
		return confidenceLayerPropertyBuilder.propertyPane;
	}
		
	private void set(ConfidenceLayer<?> layer) {		
		// set new layer
		this.currentLayer = layer;

		// Symbol properties
		this.lineWidthComboBox.getSelectionModel().select(this.currentLayer.getLineWidth());
		this.symbolStrokeColorPicker.setValue(this.currentLayer.getStrokeColor());
		this.symbolFillColorPicker.setValue(this.currentLayer.getColor());
	}
	
	private Node createSymbolPane() {
		GridPane gridPane = this.createGridPane();

		Label symbolStrokeColorLabel = new Label(i18n.getString("UIConfidenceLayerPropertyBuilder.symbol.color.label", "Stroke color:"));
		symbolStrokeColorLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label symbolFillColorLabel = new Label(i18n.getString("UIConfidenceLayerPropertyBuilder.symbol.fill_color.label", "Fill color:"));
		symbolFillColorLabel.setMinWidth(Control.USE_PREF_SIZE);
		
		Label symbolLineWidthLabel = new Label(i18n.getString("UIConfidenceLayerPropertyBuilder.symbol.linewidth.label", "Line width:"));
		symbolLineWidthLabel.setMinWidth(Control.USE_PREF_SIZE);

		this.lineWidthComboBox  = this.createLineWidthComboBox(i18n.getString("UIConfidenceLayerPropertyBuilder.symbol.linewidth.tooltip", "Set line width"));
		this.symbolStrokeColorPicker = new ColorPicker(Color.BLACK);
		this.symbolStrokeColorPicker.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.symbolStrokeColorPicker.setMaxWidth(Double.MAX_VALUE);
		this.symbolStrokeColorPicker.getStyleClass().add("split-button");
		
		this.symbolFillColorPicker = new ColorPicker(Color.LIGHTGREY);
		this.symbolFillColorPicker.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.symbolFillColorPicker.setMaxWidth(Double.MAX_VALUE);
		this.symbolFillColorPicker.getStyleClass().add("split-button");
		
		// add listeners
		this.lineWidthComboBox.getSelectionModel().selectedItemProperty().addListener(new LineWidthChangeListener());
		this.symbolFillColorPicker.valueProperty().addListener(new FillColorChangeListener());
		this.symbolStrokeColorPicker.valueProperty().addListener(new StrokeColorChangeListener());
		
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
