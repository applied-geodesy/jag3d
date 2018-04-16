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

import java.text.NumberFormat;
import java.util.Locale;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.util.Callback;
import javafx.util.StringConverter;

public abstract class UILayerPropertyBuilder {

	ComboBox<Double> createLineWidthComboBox(String tooltip) {
		Double values[] = new Double[17];
		for (int i = 0; i < values.length; i++)
			values[i] = 0.25 + 0.25 * i;
		
		ComboBox<Double> lineWidthComboBox = new ComboBox<Double>();
		lineWidthComboBox.getItems().setAll(values);
		lineWidthComboBox.getSelectionModel().select(0);
		lineWidthComboBox.setCellFactory(new Callback<ListView<Double>, ListCell<Double>>() {
            @Override 
            public ListCell<Double> call(ListView<Double> param) {
				return new LineWidthListCell();
            }
		});
		lineWidthComboBox.setButtonCell(new LineWidthListCell());
		lineWidthComboBox.setTooltip(new Tooltip(tooltip));
		lineWidthComboBox.setMaxWidth(Double.MAX_VALUE);
		return lineWidthComboBox;
	}
	
	ComboBox<Double> createSizeComboBox(String tooltip, Double values[], int digits) {
		NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
		numberFormat.setMaximumFractionDigits(digits);
		numberFormat.setMinimumFractionDigits(digits);
		numberFormat.setGroupingUsed(false);
		
		ComboBox<Double> symbolSizeComboBox = new ComboBox<Double>();
		symbolSizeComboBox.getItems().setAll(values);

		symbolSizeComboBox.setEditable(true);        
		symbolSizeComboBox.valueProperty().addListener(new ChangeListener<Double>() {
			@Override 
			public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
				if (newValue == null)
					symbolSizeComboBox.getSelectionModel().select(oldValue);
			}    
		});

		symbolSizeComboBox.setConverter(new StringConverter<Double>() {
			@Override
			public String toString(Double value) {
				return value == null ? null : numberFormat.format(value);
			}

			@Override
			public Double fromString(String string) {
				if (string == null || string.isEmpty())
					return null;
				try {
					return numberFormat.parse(string).doubleValue();
				}
				catch (Exception e) {
					return null;
				}
			}
		});
		
		symbolSizeComboBox.getSelectionModel().select(0);
		symbolSizeComboBox.setTooltip(new Tooltip(tooltip));
		symbolSizeComboBox.setMaxWidth(Double.MAX_VALUE);
		symbolSizeComboBox.setMinWidth(65);
		symbolSizeComboBox.setPrefWidth(75);
		return symbolSizeComboBox;
	}
		
	GridPane createGridPane() {
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(20);
		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(5,15,5,15)); // oben, recht, unten, links
		//gridPane.setGridLinesVisible(true);
		return gridPane;
	}
	
	TitledPane createTitledPane(String title, Node content) {
		TitledPane titledPane = new TitledPane();
		titledPane.setCollapsible(false);
		titledPane.setAnimated(false);
		titledPane.setContent(content);
		titledPane.setPadding(new Insets(0, 10, 5, 10)); // oben, links, unten, rechts
		titledPane.setText(title);
		return titledPane;
	}
	
	CheckBox createCheckBox(String title, String tooltip) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,3));
		CheckBox checkBox = new CheckBox();
		checkBox.setGraphic(label);
		checkBox.setTooltip(new Tooltip(tooltip));
		checkBox.setMinSize(Control.USE_PREF_SIZE,Control.USE_PREF_SIZE);
		checkBox.setMaxWidth(Double.MAX_VALUE);
		return checkBox;
	}
	
	ComboBox<String> createFontFamliyComboBox(String tooltip) {
		ComboBox<String> typeComboBox = new ComboBox<String>();
		typeComboBox.getItems().setAll(Font.getFamilies());
		typeComboBox.getSelectionModel().select(Font.getDefault().getFamily());
		typeComboBox.setTooltip(new Tooltip(tooltip));
		typeComboBox.setMaxWidth(Double.MAX_VALUE);
		return typeComboBox;
	}
}
