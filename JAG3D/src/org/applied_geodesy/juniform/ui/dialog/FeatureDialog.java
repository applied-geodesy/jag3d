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

package org.applied_geodesy.juniform.ui.dialog;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.PrimitiveType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.restriction.Restriction;
import org.applied_geodesy.juniform.ui.tree.UITreeBuilder;
import org.applied_geodesy.juniform.ui.i18n.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class FeatureDialog {
	
	private class GeometricPrimitiveSelectionChangeListener implements ChangeListener<GeometricPrimitive> {
		@Override
		public void changed(ObservableValue<? extends GeometricPrimitive> observable, GeometricPrimitive oldValue, GeometricPrimitive newValue) {
			setGeometricPrimitive(newValue);
		}
	}
	
	private class ManageGeometricPrimitiveEventHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() == addPrimitiveButton && feature != null) {
				Optional<GeometricPrimitive> optional = GeometricPrimitiveDialog.showAndWait(feature.getFeatureType());

				if (optional.isPresent()) {
					GeometricPrimitive result = optional.get();
					addGeometricPrimitive(result);
				}
			}
			else if (event.getSource() == removePrimitiveButton) {
				removeGeometricPrimitive();
			}
		}
	}
	
	private static I18N i18N = I18N.getInstance();
	private static FeatureDialog featureDialog = new FeatureDialog();
	private Dialog<Feature> dialog = null;
	private TextField nameTextField;
	private ListView<GeometricPrimitive> geometricPrimitiveList;
	private Label primitiveTypeLabel;
	private Button addPrimitiveButton, removePrimitiveButton;
	private Window window;
	private Feature feature;
	private GeometricPrimitive geometricPrimitive;
	
	private FeatureDialog() {}

	public static void setOwner(Window owner) {
		featureDialog.window = owner;
	}

	public static Optional<Feature> showAndWait(Feature feature) {
		featureDialog.init();
		featureDialog.setFeature(feature);
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					featureDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) featureDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return featureDialog.dialog.showAndWait();
	}
	
	private void setFeature(Feature feature) {
		this.feature = feature;

		if (feature != null) {
			this.geometricPrimitiveList.getSelectionModel().clearSelection();
			this.geometricPrimitiveList.setItems(this.feature.getGeometricPrimitives());
			this.addPrimitiveButton.setDisable(this.feature.isImmutable());
			this.geometricPrimitiveList.requestFocus();
			if (this.geometricPrimitiveList.getItems().size() > 0) {
				this.setGeometricPrimitive(this.geometricPrimitiveList.getItems().get(0));
				this.geometricPrimitiveList.getSelectionModel().clearAndSelect(0);
			}
		}
	}
	
	private void setGeometricPrimitive(GeometricPrimitive geometricPrimitive) {
		if (this.geometricPrimitive != null) {
			this.nameTextField.textProperty().unbindBidirectional(this.geometricPrimitive.nameProperty());
			this.primitiveTypeLabel.setText(null);
		}
		
		this.geometricPrimitive = geometricPrimitive;
		if (this.geometricPrimitive == null) {
			this.nameTextField.setDisable(true);
			this.primitiveTypeLabel.setText(null);
			
			this.removePrimitiveButton.setDisable(true);
		}
		else {
			this.nameTextField.setDisable(false);
			this.removePrimitiveButton.setDisable(false);
			
			if (this.feature.isImmutable()) {
				this.removePrimitiveButton.setDisable(true);
				this.addPrimitiveButton.setDisable(true);
			}
			else {
				// check, if one of the unknown parameters is bonded to a restriction
				// if yes, disable remove button
				Set<Restriction> equations = new HashSet<Restriction>(geometricPrimitive.getRestrictions());
				for (Restriction restriction : this.feature.getRestrictions()) {
					if (equations.contains(restriction))
						continue;
					for (UnknownParameter unknownParameter : geometricPrimitive.getUnknownParameters()) {
						if (restriction.contains(unknownParameter) || restriction.contains(geometricPrimitive)) {
							this.removePrimitiveButton.setDisable(true);
							break;
						}
					}	
				}

				for (Restriction restriction : this.feature.getPostProcessingCalculations()) {
					if (equations.contains(restriction))
						continue;
					for (UnknownParameter unknownParameter : geometricPrimitive.getUnknownParameters()) {
						if (restriction.contains(unknownParameter) || restriction.contains(geometricPrimitive)) {
							this.removePrimitiveButton.setDisable(true);
							break;
						}
					}	
				}
			}
			this.primitiveTypeLabel.setText(GeometricPrimitiveDialog.getPrimitiveTypeLabel(this.geometricPrimitive.getPrimitiveType()));
			this.nameTextField.textProperty().bindBidirectional(this.geometricPrimitive.nameProperty());
		}
	}
	
	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<Feature>();
		this.dialog.setTitle(i18N.getString("FeatureDialog.title", "Feature"));
		this.dialog.setHeaderText(i18N.getString("FeatureDialog.header", "Feature properties"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, Feature>() {
			@Override
			public Feature call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					return feature;
				}
				return null;
			}
		});
	}
	
	private Node createPane() {
		GridPane gridPane = DialogUtil.createGridPane();
		
		Label nameLabel = new Label(i18N.getString("FeatureDialog.primitive.name.label", "Name:"));
		Label typeLabel = new Label(i18N.getString("FeatureDialog.primitive.type.label", "Type:"));
		
		this.nameTextField = DialogUtil.createTextField(i18N.getString("FeatureDialog.primitive.name.tooltip", "Name of geometric primitive"), 
				i18N.getString("FeatureDialog.primitive.name.prompt", "Geometric primitive name"));
		
		this.nameTextField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				UITreeBuilder.getInstance().getTree().refresh();
			}
		});
		
		this.primitiveTypeLabel = new Label();
		this.geometricPrimitiveList = DialogUtil.createGeometricPrimitiveListView(createGeometricPrimitiveCellFactory());
		this.geometricPrimitiveList.getSelectionModel().selectedItemProperty().addListener(new GeometricPrimitiveSelectionChangeListener());
		
		ManageGeometricPrimitiveEventHandler manageGeometricPrimitiveEventHandler = new ManageGeometricPrimitiveEventHandler();
		VBox buttonBox = new VBox(3);
		this.addPrimitiveButton = DialogUtil.createButton(
				i18N.getString("FeatureDialog.button.add.label",   "Add"),
				i18N.getString("FeatureDialog.button.add.tooltip", "Add further geometric primitve")); 
		
		this.removePrimitiveButton = DialogUtil.createButton(
				i18N.getString("FeatureDialog.button.remove.label",   "Remove"),
				i18N.getString("FeatureDialog.button.remove.tooltip", "Remove selected geometric primitve")); 
		
		this.addPrimitiveButton.setOnAction(manageGeometricPrimitiveEventHandler);
		this.removePrimitiveButton.setOnAction(manageGeometricPrimitiveEventHandler);
		this.addPrimitiveButton.setDisable(true);
		this.removePrimitiveButton.setDisable(true);

		VBox.setVgrow(this.addPrimitiveButton,    Priority.ALWAYS);
		VBox.setVgrow(this.removePrimitiveButton, Priority.ALWAYS);
		buttonBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		buttonBox.getChildren().addAll(this.addPrimitiveButton, this.removePrimitiveButton);
		
		Region spacer = new Region();
		
		nameLabel.setLabelFor(this.nameTextField);
		typeLabel.setLabelFor(this.primitiveTypeLabel);
		
		nameLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		typeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		nameLabel.setMaxWidth(Double.MAX_VALUE);
		typeLabel.setMaxWidth(Double.MAX_VALUE);
		
		GridPane.setHgrow(nameLabel, Priority.NEVER);
		GridPane.setHgrow(typeLabel, Priority.NEVER);
		GridPane.setHgrow(buttonBox, Priority.NEVER);
		
		GridPane.setHgrow(this.nameTextField,          Priority.ALWAYS);
		GridPane.setHgrow(this.primitiveTypeLabel,     Priority.ALWAYS);
		GridPane.setHgrow(this.geometricPrimitiveList, Priority.NEVER);
		
		GridPane.setVgrow(this.geometricPrimitiveList, Priority.ALWAYS);
		GridPane.setVgrow(buttonBox,                   Priority.NEVER);
		
		GridPane.setHgrow(spacer, Priority.ALWAYS);
		GridPane.setVgrow(spacer, Priority.ALWAYS);
		
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsLeft   = new Insets(5, 7, 5, 5);
		Insets insetsRight  = new Insets(5, 0, 5, 7);

		GridPane.setMargin(this.geometricPrimitiveList, new Insets(5,10,5,0));

		GridPane.setMargin(nameLabel, insetsLeft);
		GridPane.setMargin(typeLabel, insetsLeft);
		GridPane.setMargin(buttonBox, insetsLeft);

		GridPane.setMargin(this.nameTextField,      insetsRight);
		GridPane.setMargin(this.primitiveTypeLabel, insetsRight);

		
		int row = 0;
		
		gridPane.add(nameLabel,               1, row);  // column, row, columnspan, rowspan
		gridPane.add(this.nameTextField,      2, row++);
				
		gridPane.add(typeLabel,               1, row);
		gridPane.add(this.primitiveTypeLabel, 2, row++);
		
		gridPane.add(spacer,    1, row++, 2, 1);
		gridPane.add(buttonBox, 1, row++);
	
		gridPane.add(this.geometricPrimitiveList, 0, 0, 1, row);
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				geometricPrimitiveList.requestFocus();
				if (geometricPrimitiveList.getItems().size() > 0) {
					geometricPrimitiveList.getSelectionModel().clearAndSelect(0);
					setGeometricPrimitive(geometricPrimitiveList.getItems().get(0));
				}
			}
		});
	
		return gridPane;
	}
	
	static StringConverter<PrimitiveType> createPrimitiveTypeStringConverter() {
		return new StringConverter<PrimitiveType>() {

			@Override
			public String toString(PrimitiveType primitiveType) {
				return GeometricPrimitiveDialog.getPrimitiveTypeLabel(primitiveType);
			}

			@Override
			public PrimitiveType fromString(String string) {
				return PrimitiveType.valueOf(string);
			}
		};
	}
		
	static Callback<ListView<GeometricPrimitive>, ListCell<GeometricPrimitive>> createGeometricPrimitiveCellFactory() {
		return new Callback<ListView<GeometricPrimitive>, ListCell<GeometricPrimitive>>() {
			@Override
			public ListCell<GeometricPrimitive> call(ListView<GeometricPrimitive> listView) {
				return new ListCell<GeometricPrimitive>() { 
					@Override
					protected void updateItem(GeometricPrimitive geometricPrimitive, boolean empty) {
						super.updateItem(geometricPrimitive, empty);
						
						// https://stackoverflow.com/questions/28549107/how-to-bind-data-in-a-javafx-cell
						this.textProperty().unbind();
						
						if (empty || geometricPrimitive == null) {
							this.setText(null);
							this.setTooltip(null);
							this.setGraphic(null);
						}
						else {
							this.textProperty().bind(geometricPrimitive.nameProperty());
						}
					}
				};
			};
		};
	}
	
	private void addGeometricPrimitive(GeometricPrimitive geometricPrimitive) {
		if (geometricPrimitive != null) {
			this.feature.getGeometricPrimitives().add(geometricPrimitive);
		}
	}
	
	private void removeGeometricPrimitive() {
		if (this.feature != null && this.geometricPrimitive != null) {
			this.feature.getGeometricPrimitives().remove(this.geometricPrimitive);
		}
	}
	
}
