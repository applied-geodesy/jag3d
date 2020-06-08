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

import java.util.Optional;
import java.util.function.Predicate;

import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.parameter.ParameterType;
import org.applied_geodesy.adjustment.geometry.parameter.ProcessingType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.restriction.Restriction;
import org.applied_geodesy.juniform.ui.tree.UITreeBuilder;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.juniform.ui.i18n.I18N;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class UnknownParameterDialog {
	private class VisibleChangeListener implements ChangeListener<Boolean> {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			UITreeBuilder.getInstance().handleTreeSelections();
		}
	}
	
	private class UnknownParameterSelectionChangeListener implements ChangeListener<UnknownParameter> {
		@Override
		public void changed(ObservableValue<? extends UnknownParameter> observable, UnknownParameter oldValue, UnknownParameter newValue) {
			setUnknownParameter(newValue);
		}
	}
	
	private class UnknwonParameterEventHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() == addParameterButton) {
				addParameter();
			}
			else if (event.getSource() == removeParameterButton) {
				removeParameter();
			}
			else if (event.getSource() == moveUpParameterButtom) {
				int index = parameterList.getSelectionModel().getSelectedIndex();
				if (index > 0) {
					// Collections.swap(parameterList.getItems(), index, index-1);
					// A unique list does not support Collection.swap()
					// thus, just a tmp deep copy is used of the selected element
					UnknownParameter dummyElement  = UnknownParameterTypeDialog.getUnknownParameter(parameterList.getSelectionModel().getSelectedItem().getParameterType());
					UnknownParameter firstElement  = parameterList.getItems().set(index, dummyElement);
					UnknownParameter secondElement = parameterList.getItems().set(index - 1, firstElement);
					parameterList.getItems().set(index, secondElement);
					parameterList.getSelectionModel().clearAndSelect(index - 1);
				}
			}
			else if (event.getSource() == moveDownParameterButtom) {
				int index = parameterList.getSelectionModel().getSelectedIndex();
				if (index < parameterList.getItems().size() - 1) {
					// Collections.swap(parameterList.getItems(), index, index+1);
					// A unique list does not support Collection.swap()
					// thus, just a tmp deep copy is used of the selected element
					UnknownParameter dummyElement  = UnknownParameterTypeDialog.getUnknownParameter(parameterList.getSelectionModel().getSelectedItem().getParameterType());
					UnknownParameter firstElement  = parameterList.getItems().set(index, dummyElement);
					UnknownParameter secondElement = parameterList.getItems().set(index + 1, firstElement);
					parameterList.getItems().set(index, secondElement);
					parameterList.getSelectionModel().clearAndSelect(index + 1);
				}
			}
		}
	}

	private static I18N i18N = I18N.getInstance();
	private static UnknownParameterDialog unknownParameterDialog = new UnknownParameterDialog();
	private Dialog<Void> dialog = null;
	private DoubleTextField initialGuessTextField;
	private TextField descriptionTextField, nameTextField;
	private Label parameterOwner;
	private ComboBox<ParameterType> parameterTypeComboBox;
	private ComboBox<ProcessingType> processingTypeComboBox;
	private FilteredList<ProcessingType> filteredProcessingTypeList;
	private ListView<UnknownParameter> parameterList;
	private Button addParameterButton, removeParameterButton, moveUpParameterButtom, moveDownParameterButtom;
	private UnknownParameter unknownParameter;
	private CheckBox visibleCheckBox;
	private Window window;
	private Feature feature;
	private VisibleChangeListener visibleChangeListener = new VisibleChangeListener();
	private UnknownParameterDialog() {}
	
	public static void setOwner(Window owner) {
		unknownParameterDialog.window = owner;
	}

	public static Optional<Void> showAndWait(Feature feature) {
		unknownParameterDialog.init();
		unknownParameterDialog.setFeature(feature);
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					unknownParameterDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) unknownParameterDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return unknownParameterDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<Void>();
		this.dialog.setTitle(i18N.getString("UnknownParameterDialog.title", "Unknown parameter"));
		this.dialog.setHeaderText(i18N.getString("UnknownParameterDialog.header", "Unknown parameter properties"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, Void>() {
			@Override
			public Void call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {

				}
				return null;
			}
		});
	}
	
	private void setUnknownParameter(UnknownParameter unknownParameter) {
		// property un-binding
		if (this.unknownParameter != null) {
			this.processingTypeComboBox.valueProperty().unbindBidirectional(this.unknownParameter.processingTypeProperty());
			this.parameterTypeComboBox.valueProperty().unbindBidirectional(this.unknownParameter.parameterTypeProperty());
			this.nameTextField.textProperty().unbindBidirectional(this.unknownParameter.nameProperty());
			this.descriptionTextField.textProperty().unbindBidirectional(this.unknownParameter.descriptionProperty());
			this.initialGuessTextField.numberProperty().unbindBidirectional(this.unknownParameter.value0Property());
			this.visibleCheckBox.selectedProperty().unbindBidirectional(this.unknownParameter.visibleProperty());
		}

		this.unknownParameter = unknownParameter;
		this.visibleCheckBox.selectedProperty().removeListener(this.visibleChangeListener);
		
		if (unknownParameter == null) {
			this.processingTypeComboBox.setDisable(true);
			this.parameterTypeComboBox.setDisable(true);
			
			this.nameTextField.setDisable(true);
			this.descriptionTextField.setDisable(true);
			this.initialGuessTextField.setDisable(true);
			
			this.removeParameterButton.setDisable(true);
			this.parameterOwner.setText("");
			this.visibleCheckBox.setDisable(true);
			
			this.moveUpParameterButtom.setDisable(true);
			this.moveDownParameterButtom.setDisable(true);
		}
		else {
			boolean indispensable = this.unknownParameter.isIndispensable();

			this.filteredProcessingTypeList.setPredicate(
					new Predicate<ProcessingType>() {
						@Override
						public boolean test(ProcessingType processingType) {
							return processingType != ProcessingType.POSTPROCESSING || !indispensable;
						}
					});
			
			this.visibleCheckBox.setVisible(true);
			this.processingTypeComboBox.setDisable(false);
			this.parameterTypeComboBox.setDisable(indispensable);
			
			this.nameTextField.setDisable(false);
			this.descriptionTextField.setDisable(false);
			this.initialGuessTextField.setDisable(false);
			
			this.moveUpParameterButtom.setDisable(this.parameterList.getItems().isEmpty() || !this.parameterList.getItems().isEmpty() && this.parameterList.getSelectionModel().getSelectedIndex() == 0);
			this.moveDownParameterButtom.setDisable(this.parameterList.getItems().isEmpty() || !this.parameterList.getItems().isEmpty() && this.parameterList.getSelectionModel().getSelectedIndex() == this.parameterList.getItems().size()-1);

			// property binding
			this.processingTypeComboBox.valueProperty().bindBidirectional(this.unknownParameter.processingTypeProperty());
			this.parameterTypeComboBox.valueProperty().bindBidirectional(this.unknownParameter.parameterTypeProperty());
			this.nameTextField.textProperty().bindBidirectional(this.unknownParameter.nameProperty());
			this.visibleCheckBox.selectedProperty().bindBidirectional(this.unknownParameter.visibleProperty());
			this.descriptionTextField.textProperty().bindBidirectional(this.unknownParameter.descriptionProperty());
			this.initialGuessTextField.numberProperty().bindBidirectional(this.unknownParameter.value0Property());
			this.initialGuessTextField.setValue(this.unknownParameter.getValue0());
			this.removeParameterButton.setDisable(indispensable);
			
			if (!indispensable && this.feature != null) {
				for (Restriction restriction : this.feature.getRestrictions()) {
					if (restriction.contains(this.unknownParameter)) {
						this.removeParameterButton.setDisable(true);
						break;
					}
				}
				
				for (Restriction restriction : this.feature.getPostProcessingCalculations()) {
					if (restriction.contains(this.unknownParameter)) {
						this.removeParameterButton.setDisable(true);
						break;
					}
				}
			}
			
			if (this.feature != null) {
				boolean foundOwner = false;
				for (GeometricPrimitive geometry : this.feature.getGeometricPrimitives()) {
					if (geometry.contains(this.unknownParameter)) {
						this.parameterOwner.setText(geometry.getName());
						foundOwner = true;
						break;
					}
				}
				if (!foundOwner)
					this.parameterOwner.setText(i18N.getString("UnknownParameterDialog.parameter.owner.default", "Auxially parameter"));
			}
		}
		this.visibleCheckBox.selectedProperty().addListener(this.visibleChangeListener);
	}
	
	private Node createPane() {
		GridPane gridPane = DialogUtil.createGridPane();

		Label parameterTypeLabel  = new Label(i18N.getString("UnknownParameterDialog.parameter.type.label",  "Parameter type:"));
		Label processingTypeLabel = new Label(i18N.getString("UnknownParameterDialog.processing.type.label", "Processing type:"));
		Label initialGuessLabel   = new Label(i18N.getString("UnknownParameterDialog.initial.guess.label",   "Initial guess:"));
		
		Label descriptionLabel    = new Label(i18N.getString("UnknownParameterDialog.parameter.description.label", "Description:"));
		Label nameLabel           = new Label(i18N.getString("UnknownParameterDialog.parameter.name.label",        "Name:"));
		Label ownerLabel          = new Label(i18N.getString("UnknownParameterDialog.parameter.owner.label",       "Owner:"));
		
		this.initialGuessTextField = DialogUtil.createDoubleTextField(
				CellValueType.LENGTH, 0.0, 
				i18N.getString("UnknownParameterDialog.initial.guess.tooltip", "Initial guess of parameter"));
		this.nameTextField = DialogUtil.createTextField(
				i18N.getString("UnknownParameterDialog.parameter.name.tooltip", "Name of parameter"),
				i18N.getString("UnknownParameterDialog.parameter.name.prompt", "Parameter name"));
		this.descriptionTextField = DialogUtil.createTextField(
				i18N.getString("UnknownParameterDialog.parameter.description.tooltip", "Description of parameter or properties"),
				i18N.getString("UnknownParameterDialog.parameter.description.prompt", "Parameter description"));
		this.visibleCheckBox = DialogUtil.createCheckBox(
				i18N.getString("UnknownParameterDialog.parameter.visible.label",   "Show parameter in parameter table"),
				i18N.getString("UnknownParameterDialog.parameter.visible.tooltip", "If checked, the parameter will be shown in the parameter table of the feature"));

		this.visibleCheckBox.selectedProperty().addListener(this.visibleChangeListener);
		
		this.parameterOwner = new Label();
		this.parameterOwner.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.parameterOwner.setMaxWidth(Double.MAX_VALUE);
		
		this.parameterTypeComboBox = DialogUtil.createParameterTypeComboBox(UnknownParameterTypeDialog.createParameterTypeStrincConverter(), i18N.getString("UnknownParameterDialog.parameter.type.tooltip", "Select parameter type"));

		this.filteredProcessingTypeList = new FilteredList<ProcessingType>(FXCollections.<ProcessingType>observableArrayList(ProcessingType.values()));
		this.processingTypeComboBox = DialogUtil.createProcessingTypeComboBox(createProcessingTypeStringConverter(), i18N.getString("UnknownParameterDialog.processing.type.tooltip", "Select processing type of unknown parameter"));
		this.processingTypeComboBox.setItems(this.filteredProcessingTypeList);
		
		this.parameterList = DialogUtil.createParameterListView(createUnknownParameterCellFactory());
		this.parameterList.getSelectionModel().selectedItemProperty().addListener(new UnknownParameterSelectionChangeListener());
		
		UnknwonParameterEventHandler unknwonParameterEventHandler = new UnknwonParameterEventHandler();
		VBox buttonBox = new VBox(3);
		this.addParameterButton = DialogUtil.createButton(
				i18N.getString("UnknownParameterDialog.button.add.label",   "Add"),
				i18N.getString("UnknownParameterDialog.button.add.tooltip", "Add new unknown parameter")); 
		
		this.removeParameterButton = DialogUtil.createButton(
				i18N.getString("UnknownParameterDialog.button.remove.label",   "Remove"),
				i18N.getString("UnknownParameterDialog.button.remove.tooltip", "Remove selected unknown parameter")); 
		
		this.addParameterButton.setOnAction(unknwonParameterEventHandler);
		this.removeParameterButton.setOnAction(unknwonParameterEventHandler);
		
		VBox.setVgrow(this.addParameterButton,    Priority.ALWAYS);
		VBox.setVgrow(this.removeParameterButton, Priority.ALWAYS);
		buttonBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		buttonBox.getChildren().addAll(this.addParameterButton, this.removeParameterButton);
		
		HBox orderButtonBox = new HBox(3);
		this.moveUpParameterButtom = DialogUtil.createButton(
				i18N.getString("UnknownParameterDialog.parameter.up.label", "\u25B2"),
				i18N.getString("UnknownParameterDialog.parameter.up.tooltip", "Move up (change order)"));
		this.moveDownParameterButtom = DialogUtil.createButton(
				i18N.getString("UnknownParameterDialog.parameter.down.label", "\u25BC"),
				i18N.getString("UnknownParameterDialog.parameter.down.tooltip", "Move down (change order)"));
		
		this.moveUpParameterButtom.setOnAction(unknwonParameterEventHandler);
		this.moveDownParameterButtom.setOnAction(unknwonParameterEventHandler);
		
		HBox.setHgrow(this.moveUpParameterButtom, Priority.NEVER);
		HBox.setHgrow(this.moveDownParameterButtom, Priority.NEVER);
		orderButtonBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		orderButtonBox.getChildren().addAll(this.moveUpParameterButtom, this.moveDownParameterButtom);
		
		Region spacer = new Region();
		
		parameterTypeLabel.setLabelFor(this.parameterTypeComboBox);
		processingTypeLabel.setLabelFor(this.processingTypeComboBox);
		initialGuessLabel.setLabelFor(this.initialGuessTextField);
		descriptionLabel.setLabelFor(this.descriptionTextField);
		nameLabel.setLabelFor(this.nameTextField);
		ownerLabel.setLabelFor(this.parameterOwner);
		
		parameterTypeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		processingTypeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		initialGuessLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		descriptionLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		nameLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		ownerLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		parameterTypeLabel.setMaxWidth(Double.MAX_VALUE);
		processingTypeLabel.setMaxWidth(Double.MAX_VALUE);
		initialGuessLabel.setMaxWidth(Double.MAX_VALUE);
		descriptionLabel.setMaxWidth(Double.MAX_VALUE);
		nameLabel.setMaxWidth(Double.MAX_VALUE);
		ownerLabel.setMaxWidth(Double.MAX_VALUE);
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsLeft   = new Insets(5, 7, 5, 5);
		Insets insetsRight  = new Insets(5, 0, 5, 7);
		
		GridPane.setMargin(this.parameterList, new Insets(5,10,5,0));
		GridPane.setMargin(orderButtonBox,     new Insets(5,10,5,0));
		
		GridPane.setMargin(parameterTypeLabel,  insetsLeft);
		GridPane.setMargin(processingTypeLabel, insetsLeft);
		GridPane.setMargin(initialGuessLabel,   insetsLeft);
		GridPane.setMargin(descriptionLabel,    insetsLeft);
		GridPane.setMargin(nameLabel,           insetsLeft);
		GridPane.setMargin(ownerLabel,          insetsLeft);
		GridPane.setMargin(buttonBox,           insetsLeft);
		
		GridPane.setMargin(this.nameTextField,          insetsRight);
		GridPane.setMargin(this.parameterTypeComboBox,  insetsRight);
		GridPane.setMargin(this.processingTypeComboBox, insetsRight);
		GridPane.setMargin(this.initialGuessTextField,  insetsRight);
		GridPane.setMargin(this.descriptionTextField,   insetsRight);
		GridPane.setMargin(this.parameterOwner,         insetsRight);
		GridPane.setMargin(this.visibleCheckBox, new Insets(5,5,5,5));
		
		GridPane.setHgrow(parameterTypeLabel,  Priority.NEVER);
		GridPane.setHgrow(processingTypeLabel, Priority.NEVER);
		GridPane.setHgrow(initialGuessLabel,   Priority.NEVER);
		GridPane.setHgrow(descriptionLabel,    Priority.NEVER);
		GridPane.setHgrow(nameLabel,           Priority.NEVER);
		GridPane.setHgrow(ownerLabel,          Priority.NEVER);
		GridPane.setHgrow(buttonBox,           Priority.NEVER);
		GridPane.setHgrow(orderButtonBox,      Priority.NEVER);
		
		GridPane.setHgrow(this.nameTextField,           Priority.ALWAYS);
		GridPane.setHgrow(this.parameterTypeComboBox,   Priority.ALWAYS);
		GridPane.setHgrow(this.processingTypeComboBox,  Priority.ALWAYS);
		GridPane.setHgrow(this.initialGuessTextField,   Priority.ALWAYS);
		GridPane.setHgrow(this.descriptionTextField,    Priority.ALWAYS);
		GridPane.setHgrow(this.parameterList,           Priority.NEVER);
		GridPane.setHgrow(this.parameterOwner,          Priority.ALWAYS);
		GridPane.setHgrow(this.visibleCheckBox,         Priority.ALWAYS);

		GridPane.setVgrow(buttonBox,                    Priority.NEVER);
		GridPane.setVgrow(orderButtonBox,               Priority.NEVER);
		GridPane.setVgrow(this.parameterList,           Priority.ALWAYS);

		GridPane.setHgrow(spacer, Priority.ALWAYS);
		GridPane.setVgrow(spacer, Priority.ALWAYS);

		int row = 0;

		gridPane.add(ownerLabel,                  1, row);  // column, row, columnspan, rowspan
		gridPane.add(this.parameterOwner,         2, row++);

		gridPane.add(nameLabel,                   1, row); 
		gridPane.add(this.nameTextField,          2, row++);
				
		gridPane.add(parameterTypeLabel,          1, row);
		gridPane.add(this.parameterTypeComboBox,  2, row++);

		gridPane.add(processingTypeLabel,         1, row);
		gridPane.add(this.processingTypeComboBox, 2, row++);

		gridPane.add(initialGuessLabel,           1, row);
		gridPane.add(this.initialGuessTextField,  2, row++);

		gridPane.add(descriptionLabel,            1, row);
		gridPane.add(this.descriptionTextField,   2, row++);
		
		gridPane.add(this.visibleCheckBox,        1, row++, 2, 1);
				
		gridPane.add(spacer,                      1, row++, 2, 1);
		gridPane.add(buttonBox,                   1, row++);
	
		gridPane.add(this.parameterList,          0, 0, 1, row++);
		gridPane.add(orderButtonBox,              0, row);

		this.parameterTypeComboBox.valueProperty().addListener(new ChangeListener<ParameterType>() {

			@Override
			public void changed(ObservableValue<? extends ParameterType> observable, ParameterType oldValue, ParameterType newValue) {
				if (newValue == null)
					return;

				CellValueType cellValueType = initialGuessTextField.getCellValueType();
				switch (newValue) {
				case COORDINATE_X:
				case COORDINATE_Y:
				case COORDINATE_Z:
				case ORIGIN_COORDINATE_X:
				case ORIGIN_COORDINATE_Y:
				case ORIGIN_COORDINATE_Z:
				case RADIUS:
				case LENGTH:
				case PRIMARY_FOCAL_COORDINATE_X:
				case PRIMARY_FOCAL_COORDINATE_Y:
				case PRIMARY_FOCAL_COORDINATE_Z:
				case SECONDARY_FOCAL_COORDINATE_X:
				case SECONDARY_FOCAL_COORDINATE_Y:
				case SECONDARY_FOCAL_COORDINATE_Z:
					cellValueType = CellValueType.LENGTH;
					break;
				case VECTOR_LENGTH:
				case VECTOR_X:
				case VECTOR_Y:
				case VECTOR_Z:
					cellValueType = CellValueType.VECTOR;
					break;
				case ANGLE:
					cellValueType = CellValueType.ANGLE;
					break;
				case CONSTANT:
				case MAJOR_AXIS_COEFFICIENT:
				case MIDDLE_AXIS_COEFFICIENT:
				case MINOR_AXIS_COEFFICIENT:
				case ROTATION_COMPONENT_R11:
				case ROTATION_COMPONENT_R12:
				case ROTATION_COMPONENT_R13:
				case ROTATION_COMPONENT_R21:
				case ROTATION_COMPONENT_R22:
				case ROTATION_COMPONENT_R23:
				case ROTATION_COMPONENT_R31:
				case ROTATION_COMPONENT_R32:
				case ROTATION_COMPONENT_R33:
				case POLYNOMIAL_COEFFICIENT_A:
				case POLYNOMIAL_COEFFICIENT_B:
				case POLYNOMIAL_COEFFICIENT_C:
				case POLYNOMIAL_COEFFICIENT_D:
				case POLYNOMIAL_COEFFICIENT_E:
				case POLYNOMIAL_COEFFICIENT_F:
				case POLYNOMIAL_COEFFICIENT_G:
				case POLYNOMIAL_COEFFICIENT_H:
				case POLYNOMIAL_COEFFICIENT_I:
					cellValueType = CellValueType.DOUBLE;
					break;
				}
				
				if (initialGuessTextField.getCellValueType() != cellValueType) {
					initialGuessTextField.setCellValueType(cellValueType);
				}
			}
		});

		gridPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		gridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // width, height

		Platform.runLater(new Runnable() {
			@Override public void run() {
				parameterList.requestFocus();
				if (parameterList.getItems().size() > 0) {
					parameterList.getSelectionModel().clearAndSelect(0);
					setUnknownParameter(parameterList.getItems().get(0));
				}
			}
		});
		
		
//		ScrollPane scrollPane = new ScrollPane();
//		scrollPane.setContent(gridPane);
//		scrollPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
//
//		scrollPane.setFitToHeight(true);
//		scrollPane.setFitToWidth(true);

		return gridPane;
	}

	static StringConverter<ProcessingType> createProcessingTypeStringConverter() {
		return new StringConverter<ProcessingType>() {

			@Override
			public String toString(ProcessingType type) {
				if (type == null)
					return null;
				switch (type) {
				case ADJUSTMENT:
					return i18N.getString("UnknownParameterDialog.processing.type.adjusting", "Adjusting");

				case FIXED:
					return i18N.getString("UnknownParameterDialog.processing.type.fixing", "Fixing");
					
				case POSTPROCESSING:
					return i18N.getString("UnknownParameterDialog.processing.type.postprocessing", "Post processing");   			
				}
				return "";
			}

			@Override
			public ProcessingType fromString(String string) {
				return ProcessingType.valueOf(string);
			}
		};
	}

	static Callback<ListView<UnknownParameter>, ListCell<UnknownParameter>> createUnknownParameterCellFactory() {
		return new Callback<ListView<UnknownParameter>, ListCell<UnknownParameter>>() {
			@Override
			public ListCell<UnknownParameter> call(ListView<UnknownParameter> listView) {
				return new ListCell<UnknownParameter>() { 
					@Override
					protected void updateItem(UnknownParameter unknownParameter, boolean empty) {
						super.updateItem(unknownParameter, empty);
						
						// https://stackoverflow.com/questions/28549107/how-to-bind-data-in-a-javafx-cell
						this.textProperty().unbind();
						
						if (empty || unknownParameter == null) {
							this.setText(null);
							this.setTooltip(null);
							this.setGraphic(null);
						}
						else {
							this.textProperty().bind(Bindings.when(unknownParameter.nameProperty().isEqualTo("")).then(
										Bindings.concat(UnknownParameterTypeDialog.getParameterTypeLabel(unknownParameter.getParameterType()))
									).otherwise(
										Bindings.concat(unknownParameter.nameProperty()).concat(" (").concat(UnknownParameterTypeDialog.getParameterTypeLabel(unknownParameter.getParameterType())).concat(")"))
									);

							if (unknownParameter.getDescription() != null && !unknownParameter.getDescription().isBlank())
								this.setTooltip(new Tooltip(unknownParameter.getDescription()));
						}
					}
				};
			};
		};
	}

	private void addParameter() {
		if (this.feature != null) {
			Optional<UnknownParameter> optional = UnknownParameterTypeDialog.showAndWait();
			if (optional.isPresent()) {
				UnknownParameter unknownParameter = optional.get();
				this.feature.getUnknownParameters().add(unknownParameter);
				this.parameterList.getSelectionModel().select(unknownParameter);
			}
		}
	}
	
	private void removeParameter() {
		if (this.feature != null && this.unknownParameter != null && !this.unknownParameter.isIndispensable()) {
			this.feature.getUnknownParameters().remove(this.unknownParameter);
		}
	}
	
	private void setFeature(Feature feature) {
		this.feature = feature;
		if (feature != null) {
			this.parameterList.setItems(this.feature.getUnknownParameters());
			this.parameterList.requestFocus();
			if (this.parameterList.getItems().size() > 0) {
				this.setUnknownParameter(this.parameterList.getItems().get(0));
				this.parameterList.getSelectionModel().clearAndSelect(0);
			}
		}
	}
}
