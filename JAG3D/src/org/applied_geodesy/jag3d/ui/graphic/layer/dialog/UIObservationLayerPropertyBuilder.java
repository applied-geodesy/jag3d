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

import java.util.HashMap;
import java.util.Map;

import org.applied_geodesy.jag3d.ui.graphic.layer.LayerManager;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationLayer;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationSymbolProperties;
import org.applied_geodesy.jag3d.ui.graphic.layer.ObservationSymbolProperties.ObservationType;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightType;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.util.StringConverter;

public class UIObservationLayerPropertyBuilder extends UILayerPropertyBuilder {	
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
	
	private class HighlightColorChangeListener implements ChangeListener<Color> {
		@Override
		public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setHighlightColor(newValue);
				layerManager.draw();
			}
		}
	}
	
	private class HighlightTypeChangeListener implements ChangeListener<TableRowHighlightType> {
		@Override
		public void changed(ObservableValue<? extends TableRowHighlightType> observable, TableRowHighlightType oldValue, TableRowHighlightType newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				currentLayer.setHighlightType(newValue);
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
	
	private class ObservationColorChangeListener implements ChangeListener<Color> {
		private ObservationType observationType;
		ObservationColorChangeListener(ObservationType observationType) {
			this.observationType = observationType;
		}
		@Override
		public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				ObservationSymbolProperties properties = currentLayer.getObservationSymbolProperties(this.observationType);
				if (properties != null) {
					properties.setColor(newValue);
					layerManager.draw();
				}
			}
		}
	}
	
	private class ObservationEnableChangeListener implements ChangeListener<Boolean> {
		private ObservationType observationType;
		ObservationEnableChangeListener(ObservationType observationType) {
			this.observationType = observationType;
		}
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (currentLayer != null && newValue != null && layerManager != null) {
				ObservationSymbolProperties properties = currentLayer.getObservationSymbolProperties(this.observationType);
				if (properties != null) {
					properties.setVisible(newValue);
					layerManager.draw();
				}
			}
		}
	}
	
	private I18N i18n = I18N.getInstance();
	private static UIObservationLayerPropertyBuilder observationLayerPropertyBuilder = new UIObservationLayerPropertyBuilder();

	private ComboBox<Double> symbolSizeComboBox;
	private ComboBox<Double> lineWidthComboBox;
	private ColorPicker symbolColorPicker;
	
	private Node highlightPane = null;
	private ComboBox<Double> highlightLineWidthComboBox;
	private ComboBox<TableRowHighlightType> highlightTypeComboBox;
	private ColorPicker highlightColorPicker;
	
	private Map<ObservationType, ColorPicker> observationColorPickerMap = new HashMap<ObservationType, ColorPicker>();
	private Map<ObservationType, CheckBox> observationCheckBoxMap = new HashMap<ObservationType, CheckBox>();
	
	private ObservationLayer currentLayer = null;
	private LayerManager layerManager = null;
	
	private VBox propertyPane = null;
	
	private UIObservationLayerPropertyBuilder() {}
	
	private void init() {
		if (this.propertyPane != null)
			return;
		
		this.highlightPane = this.createHighlightPane();
		this.propertyPane = new VBox(20);
		this.propertyPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.propertyPane.setMinWidth(Control.USE_PREF_SIZE);
		this.propertyPane.getChildren().addAll(
				this.createSymbolPane(),
				this.createObservationPaintPane(),
				this.highlightPane
				);
	}
	
	public static Node getLayerPropertyPane(LayerManager layerManager, ObservationLayer layer) {
		observationLayerPropertyBuilder.layerManager = null;
		observationLayerPropertyBuilder.init();
		observationLayerPropertyBuilder.set(layer);
		observationLayerPropertyBuilder.layerManager = layerManager;
		return observationLayerPropertyBuilder.propertyPane;
	}

	private void set(ObservationLayer layer) {
		// set new layer
		this.currentLayer = layer;
		
		// Symbol properties
		this.symbolSizeComboBox.getSelectionModel().select(this.currentLayer.getSymbolSize());
		this.lineWidthComboBox.getSelectionModel().select(this.currentLayer.getLineWidth());
		this.symbolColorPicker.setValue(this.currentLayer.getColor());
		
		switch(this.currentLayer.getLayerType()) {
		case OBSERVATION_APOSTERIORI:
			this.highlightColorPicker.setValue(this.currentLayer.getHighlightColor());
			this.highlightLineWidthComboBox.getSelectionModel().select(this.currentLayer.getHighlightLineWidth());
			this.highlightTypeComboBox.getSelectionModel().select(this.currentLayer.getHighlightType());
			this.highlightPane.setVisible(true);
			this.highlightPane.setManaged(true);
			break;
			
		default:
			this.highlightPane.setVisible(false);
			this.highlightPane.setManaged(false);
			break;
		}

		ObservationType observationTypes[] = ObservationType.values();
		for (ObservationType observationType : observationTypes) {
			ObservationSymbolProperties properties = this.currentLayer.getObservationSymbolProperties(observationType);	
			CheckBox checkBox       = this.observationCheckBoxMap.get(observationType);
			ColorPicker colorPicker = this.observationColorPickerMap.get(observationType);
			
			checkBox.setSelected(properties.isVisible());
			colorPicker.setValue(properties.getColor());
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
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.leveling.enable.tooltip", "If checked, leveling will be drawn"))
						);
				this.observationColorPickerMap.put(observationType, this.createColorPicker(Color.DARKBLUE, i18n.getString("UIObservationLayerPropertyBuilder.color.leveling.tooltip", "Set color of leveling data")));
				break;
				
			case DIRECTION:
				this.observationCheckBoxMap.put(observationType, this.createCheckBox(
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.direction.enable.label", "Direction:"), 
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.direction.enable.tooltip", "If checked, directions will be drawn"))
						);
				this.observationColorPickerMap.put(observationType, this.createColorPicker(Color.DARKBLUE, i18n.getString("UIObservationLayerPropertyBuilder.color.direction.tooltip", "Set color of direction measurements")));
				break;
				
			case DISTANCE:
				this.observationCheckBoxMap.put(observationType, this.createCheckBox(
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.distance.enable.label", "Distance:"), 
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.distance.enable.tooltip", "If checked, distances will be drawn"))
						);
				this.observationColorPickerMap.put(observationType, this.createColorPicker(Color.DARKBLUE, i18n.getString("UIObservationLayerPropertyBuilder.color.distance.tooltip", "Set color of distance measurements")));
				break;
			case ZENITH_ANGLE:
				this.observationCheckBoxMap.put(observationType, this.createCheckBox(
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.zenith_angle.enable.label", "Zenith angle:"), 
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.zenith_angle.enable.tooltip", "If checked, zenith angles will be drawn"))
						);
				this.observationColorPickerMap.put(observationType, this.createColorPicker(Color.DARKBLUE, i18n.getString("UIObservationLayerPropertyBuilder.color.zenith_angle.tooltip", "Set color of zenith angle measurements")));
				break;
				
			case GNSS:
				this.observationCheckBoxMap.put(observationType, this.createCheckBox(
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.gnss.enable.label", "GNSS Baseline:"), 
						i18n.getString("UIObservationLayerPropertyBuilder.symbol.gnss.enable.tooltip", "If checked, GNSS baselines will be drawn"))
						);
				this.observationColorPickerMap.put(observationType, this.createColorPicker(Color.DARKBLUE, i18n.getString("UIObservationLayerPropertyBuilder.color.gnss.tooltip", "Set color of GNSS measurements")));
				break;
			}
		}
		
		int row = 0;
		for (ObservationType observationType : observationTypes) {
			CheckBox checkBox  = this.observationCheckBoxMap.get(observationType);
			ColorPicker colorPicker = this.observationColorPickerMap.get(observationType);
			
			checkBox.selectedProperty().addListener(new ObservationEnableChangeListener(observationType));
			colorPicker.valueProperty().addListener(new ObservationColorChangeListener(observationType));
			
			GridPane.setHgrow(checkBox, Priority.NEVER);
			GridPane.setHgrow(colorPicker, Priority.ALWAYS);

			gridPane.add(checkBox,    0, row);
			gridPane.add(colorPicker, 1, row++);
		}

		return this.createTitledPane(i18n.getString("UIObservationLayerPropertyBuilder.observation.title", "Observation properties"), gridPane);
	}
	
	private Node createSymbolPane() {
		GridPane gridPane = this.createGridPane();
		
		Label symbolSizeLabel = new Label(i18n.getString("UIObservationLayerPropertyBuilder.symbol.size.label", "Symbol size:"));
		symbolSizeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label symbolColorLabel = new Label(i18n.getString("UIObservationLayerPropertyBuilder.symbol.color.label", "Symbol color:"));
		symbolColorLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label symbolLineWidthLabel = new Label(i18n.getString("UIObservationLayerPropertyBuilder.symbol.linewidth.label", "Line width:"));
		symbolLineWidthLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Double symbolSizes[] = new Double[21];
		for (int i = 0; i < symbolSizes.length; i++)
			symbolSizes[i] = 5 + 0.5 * i;
		
		this.symbolSizeComboBox = this.createSizeComboBox(i18n.getString("UIObservationLayerPropertyBuilder.symbol.size.tooltip", "Set symbol size"), symbolSizes, 1);
		this.lineWidthComboBox  = this.createLineWidthComboBox(i18n.getString("UIObservationLayerPropertyBuilder.symbol.linewidth.tooltip", "Set line width"));
		this.symbolColorPicker  = this.createColorPicker(Color.DARKBLUE, i18n.getString("UIObservationLayerPropertyBuilder.symbol.color.tooltip", "Set symbol color"));
		
		// add listeners
		this.symbolSizeComboBox.getSelectionModel().selectedItemProperty().addListener(new SymbolSizeChangeListener());
		this.lineWidthComboBox.getSelectionModel().selectedItemProperty().addListener(new LineWidthChangeListener());
		this.symbolColorPicker.valueProperty().addListener(new ColorChangeListener());
		
		symbolSizeLabel.setLabelFor(this.symbolSizeComboBox);
		symbolColorLabel.setLabelFor(this.symbolColorPicker);
		symbolLineWidthLabel.setLabelFor(this.lineWidthComboBox);
		
		GridPane.setHgrow(symbolSizeLabel,      Priority.NEVER);
		GridPane.setHgrow(symbolColorLabel,     Priority.NEVER);
		GridPane.setHgrow(symbolLineWidthLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.symbolSizeComboBox,   Priority.ALWAYS);
		GridPane.setHgrow(this.symbolColorPicker,    Priority.ALWAYS);
		GridPane.setHgrow(this.lineWidthComboBox,    Priority.ALWAYS);

		int row = 0;		
		gridPane.add(symbolSizeLabel,         0, row);
		gridPane.add(this.symbolSizeComboBox, 1, row++);
		
		gridPane.add(symbolColorLabel,       0, row);
		gridPane.add(this.symbolColorPicker, 1, row++);
		
		gridPane.add(symbolLineWidthLabel,   0, row);
		gridPane.add(this.lineWidthComboBox, 1, row++);

		return this.createTitledPane(i18n.getString("UIObservationLayerPropertyBuilder.symbol.title", "Symbol properties"), gridPane);
	}
	
	private Node createHighlightPane() {
		GridPane gridPane = this.createGridPane();
		
		Label highlightColorLabel = new Label(i18n.getString("UIObservationLayerPropertyBuilder.highlight.color.label", "Highlight color:"));
		highlightColorLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label highlightTypeLabel = new Label(i18n.getString("UIObservationLayerPropertyBuilder.highlight.type.label", "Highlight type:"));
		highlightTypeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label highlightLineWidthLabel = new Label(i18n.getString("UIObservationLayerPropertyBuilder.highlight.linewidth.label", "Line width:"));
		highlightLineWidthLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		this.highlightTypeComboBox      = this.createHighlightTypeComboBox(i18n.getString("UIObservationLayerPropertyBuilder.highlight.type.tooltip", "Set type of highlighting"), this.getHighlightTypeStringConverter());
		this.highlightLineWidthComboBox = this.createLineWidthComboBox(i18n.getString("UIObservationLayerPropertyBuilder.highlight.linewidth.tooltip", "Set line width for highlighting"));
		this.highlightColorPicker       = this.createColorPicker(Color.DARKBLUE, i18n.getString("UIObservationLayerPropertyBuilder.highlight.color.tooltip", "Set color of highlighting"));

		// add listener
		this.highlightColorPicker.valueProperty().addListener(new HighlightColorChangeListener());
		this.highlightLineWidthComboBox.getSelectionModel().selectedItemProperty().addListener(new HighlightLineWidthChangeListener());
		this.highlightTypeComboBox.getSelectionModel().selectedItemProperty().addListener(new HighlightTypeChangeListener());
		
		highlightColorLabel.setLabelFor(this.highlightColorPicker);
		highlightTypeLabel.setLabelFor(this.highlightTypeComboBox);
		highlightLineWidthLabel.setLabelFor(this.highlightLineWidthComboBox);
		
		GridPane.setHgrow(highlightColorLabel,     Priority.NEVER);
		GridPane.setHgrow(highlightTypeLabel,      Priority.NEVER);
		GridPane.setHgrow(highlightLineWidthLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.highlightColorPicker,       Priority.ALWAYS);
		GridPane.setHgrow(this.highlightTypeComboBox,      Priority.ALWAYS);
		GridPane.setHgrow(this.highlightLineWidthComboBox, Priority.ALWAYS);
				
		int row = 0;
		gridPane.add(highlightTypeLabel,         0, row);
		gridPane.add(this.highlightTypeComboBox, 1, row++);
		
		gridPane.add(highlightColorLabel,       0, row);
		gridPane.add(this.highlightColorPicker, 1, row++);
		
		gridPane.add(highlightLineWidthLabel,         0, row);
		gridPane.add(this.highlightLineWidthComboBox, 1, row++);

		return this.createTitledPane(i18n.getString("UIObservationLayerPropertyBuilder.highlight.title", "Highlight properties"), gridPane);
	}
	
	private StringConverter<TableRowHighlightType> getHighlightTypeStringConverter() {
		return new StringConverter<TableRowHighlightType>() {

			@Override
			public String toString(TableRowHighlightType type) {
				if (type == null)
					return null;
				switch(type) {
				case NONE:
					return i18n.getString("UIObservationLayerPropertyBuilder.highlight.type.none.label", "None");
				case TEST_STATISTIC:
					return i18n.getString("UIObservationLayerPropertyBuilder.highlight.type.test_statistic.label", "Test statistic");
				case REDUNDANCY:
					return i18n.getString("UIObservationLayerPropertyBuilder.highlight.type.redundancy.label", "Redundancy");
				case INFLUENCE_ON_POSITION:
					return i18n.getString("UIObservationLayerPropertyBuilder.highlight.type.influence_on_position.label", "Influence on position");
				case P_PRIO_VALUE:
					return i18n.getString("UIObservationLayerPropertyBuilder.highlight.type.p_prio.label", "p-Value (a-priori)");
				case GROSS_ERROR:
					return i18n.getString("UIObservationLayerPropertyBuilder.highlight.type.gross_error.label", "Gross error");
				}
				return null;
			}

			@Override
			public TableRowHighlightType fromString(String string) {
				return TableRowHighlightType.valueOf(string);
			}
		};
	}
}
