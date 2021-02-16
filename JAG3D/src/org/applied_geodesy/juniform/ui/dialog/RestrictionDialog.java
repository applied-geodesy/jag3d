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
import org.applied_geodesy.adjustment.geometry.parameter.ProcessingType;
import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.restriction.AverageRestriction;
import org.applied_geodesy.adjustment.geometry.restriction.FeaturePointRestriction;
import org.applied_geodesy.adjustment.geometry.restriction.ProductSumRestriction;
import org.applied_geodesy.adjustment.geometry.restriction.Restriction;
import org.applied_geodesy.adjustment.geometry.restriction.RestrictionType;
import org.applied_geodesy.adjustment.geometry.restriction.TrigonometricRestriction;
import org.applied_geodesy.adjustment.geometry.restriction.VectorAngleRestriction;
import org.applied_geodesy.ui.tex.LaTexLabel;
import org.applied_geodesy.juniform.ui.i18n.I18N;
import org.applied_geodesy.juniform.ui.table.UIPointTableBuilder;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class RestrictionDialog {
	private class PostProcessingPredicate implements Predicate<UnknownParameter> {
		@Override
		public boolean test(UnknownParameter unknownParameter) {
			return enablePostProcessing || unknownParameter.getProcessingType() != ProcessingType.POSTPROCESSING;
		}
	}
	
	private class RestrictionEventHandler implements EventHandler<ActionEvent> {
		
		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() == addRestrictionButton) {
				Optional<RestrictionType> optional = RestrictionTypeDialog.showAndWait();

				if (optional.isPresent()) {
					RestrictionType restrictionType = optional.get();
					if (restrictionType == null)
						return;
					
					Restriction restriction = createRestriction(restrictionType);
					if (restriction == null)
						return;
					
					restrictionList.getItems().add(restriction);
					restrictionList.getSelectionModel().clearSelection();
					restrictionList.getSelectionModel().select(restriction);
					
					editRestriction(restriction);
					editRestrictionButton.setDisable(restrictionList.getItems().size() == 0);
					removeRestrictionButton.setDisable(restrictionList.getItems().size() == 0);
				}
			}
			else if (event.getSource() == editRestrictionButton) {
				Restriction restriction = restrictionList.getSelectionModel().getSelectedItem();
				if (restriction != null)
					editRestriction(restriction);
			}
			else if (event.getSource() == removeRestrictionButton) {
				Restriction restriction = restrictionList.getSelectionModel().getSelectedItem();
				if (restriction != null)
					removeRestriction(restriction);
			}
			else if (event.getSource() == moveUpRestrictionButtom) {
				int index = restrictionList.getSelectionModel().getSelectedIndex();
				if (index > 0) {
					// Collections.swap(restrictionList.getItems(), index, index-1);
					// A unique list does not support Collection.swap()
					// thus, just a tmp deep copy is used of the selected element
					Restriction dummyElement  = createRestriction(restrictionList.getSelectionModel().getSelectedItem().getRestrictionType());
					Restriction firstElement  = restrictionList.getItems().set(index, dummyElement);
					Restriction secondElement = restrictionList.getItems().set(index-1, firstElement);
					restrictionList.getItems().set(index, secondElement);
					restrictionList.getSelectionModel().clearAndSelect(index-1);
					
				}
			}
			else if (event.getSource() == moveDownRestrictionButtom) {
				int index = restrictionList.getSelectionModel().getSelectedIndex();
				if (index < restrictionList.getItems().size() - 1) {
					// Collections.swap(restrictionList.getItems(), index, index+1);
					// A unique list does not support Collection.swap()
					// thus, just a tmp deep copy is used of the selected element
					Restriction dummyElement  = createRestriction(restrictionList.getSelectionModel().getSelectedItem().getRestrictionType());
					Restriction firstElement  = restrictionList.getItems().set(index, dummyElement);
					Restriction secondElement = restrictionList.getItems().set(index+1, firstElement);
					restrictionList.getItems().set(index, secondElement);
					restrictionList.getSelectionModel().clearAndSelect(index+1);
				}
			}
		}
	}
	private I18N i18n = I18N.getInstance();
	private static RestrictionDialog restrictionDialog = new RestrictionDialog();
	private Dialog<Void> dialog = null;
	private LaTexLabel latexLabel;
	private TextField descriptionTextField;
	private Label restrictionTypeLabel;
	private Label parameterOwner;
	private ListView<Restriction> restrictionList;
	private Button addRestrictionButton, editRestrictionButton, removeRestrictionButton, moveUpRestrictionButtom, moveDownRestrictionButtom;
	private Restriction restriction;
	private Window window;
	private Feature feature;
	private boolean enablePostProcessing = false;
	private FilteredList<UnknownParameter> filteredUnknownParameters = null;
	private RestrictionDialog() {}

	public static void setOwner(Window owner) {
		restrictionDialog.window = owner;
	}

	public static Optional<Void> showAndWait(Feature feature, boolean enablePostProcessing) {
		restrictionDialog.init();
		restrictionDialog.setFeature(feature, enablePostProcessing);
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					restrictionDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) restrictionDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return restrictionDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<Void>();
		this.dialog.setTitle(i18n.getString("RestrictionDialog.title", "Restrictions"));
		this.dialog.setHeaderText(i18n.getString("RestrictionDialog.header", "Parameter restrictions"));
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

	private void setRestriction(Restriction restriction) {
		// property un-binding
		if (this.restriction != null) {
			this.descriptionTextField.textProperty().unbindBidirectional(this.restriction.descriptionProperty());
		}
		
		this.restriction = restriction;
		
		if (restriction == null) {
			this.descriptionTextField.setDisable(true);
			this.editRestrictionButton.setDisable(true);
			this.removeRestrictionButton.setDisable(true);
			
			this.latexLabel.setTex("");
			this.restrictionTypeLabel.setText("");
			this.parameterOwner.setText("");
			
			this.moveUpRestrictionButtom.setDisable(true);
			this.moveDownRestrictionButtom.setDisable(true);
		}
		else {
			boolean indispensable = this.restriction.isIndispensable();

			this.descriptionTextField.setDisable(false);
			this.editRestrictionButton.setDisable(false);
			this.removeRestrictionButton.setDisable(indispensable);
			
			this.moveUpRestrictionButtom.setDisable(indispensable || this.restrictionList.getItems().isEmpty() || !this.restrictionList.getItems().isEmpty() && this.restrictionList.getSelectionModel().getSelectedIndex() == 0);
			this.moveDownRestrictionButtom.setDisable(indispensable || this.restrictionList.getItems().isEmpty() || !this.restrictionList.getItems().isEmpty() && this.restrictionList.getSelectionModel().getSelectedIndex() == this.restrictionList.getItems().size()-1);
			
			this.latexLabel.setTex(restriction.toLaTex());
			
			this.restrictionTypeLabel.setText(RestrictionTypeDialog.getRestrictionTypeLabel(this.restriction.getRestrictionType()));
			
			// property binding
			this.descriptionTextField.textProperty().bindBidirectional(this.restriction.descriptionProperty());

			boolean foundOwner = false;
			for (GeometricPrimitive geometry : this.feature.getGeometricPrimitives()) {
				if (geometry.getRestrictions().contains(restriction)) {
					this.parameterOwner.setText(geometry.getName());
					foundOwner = true;
					break;
				}
			}
			if (!foundOwner)
				this.parameterOwner.setText(i18n.getString("RestrictionDialog.restriction.owner.default", "Auxially restriction"));

		}
	}
	
	private Node createPane() {
		GridPane gridPane = DialogUtil.createGridPane();
		
		Label ownerLabel           = new Label(i18n.getString("RestrictionDialog.restriction.owner.label", "Owner:"));
		Label equationLabel        = new Label(i18n.getString("RestrictionDialog.equation.label",          "Equation:"));
		Label restrictionTypeLabel = new Label(i18n.getString("RestrictionDialog.restriction.type.label",  "Restriction type:"));
		Label descriptionLabel     = new Label(i18n.getString("RestrictionDialog.description.label",       "Description:"));
		
		this.restrictionTypeLabel = new Label("");
		
		this.restrictionList = this.createRestrictionListView();
		
		this.descriptionTextField  = DialogUtil.createTextField(
				i18n.getString("RestrictionDialog.description.tooltip", "Description of parameter restriction"),
				i18n.getString("RestrictionDialog.description.prompt", "Restriction description"));
		
		this.parameterOwner = new Label();
		this.parameterOwner.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.parameterOwner.setMaxWidth(Double.MAX_VALUE);
				
		this.latexLabel = new LaTexLabel();
		this.latexLabel.setMinSize(225, Control.USE_PREF_SIZE);
		this.latexLabel.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE);
		
		RestrictionEventHandler restrictionEventHandler = new RestrictionEventHandler();
		VBox buttonBox = new VBox(3);
		this.addRestrictionButton = DialogUtil.createButton(
				i18n.getString("RestrictionDialog.restriction.add.label",   "Add"),
				i18n.getString("RestrictionDialog.restriction.add.tooltip", "Add new parameter restriction")); 
		
		this.editRestrictionButton = DialogUtil.createButton(
				i18n.getString("RestrictionDialog.restriction.edit.label",   "Edit"),
				i18n.getString("RestrictionDialog.restriction.edit.tooltip", "Edit selected restriction")); 
		
		this.removeRestrictionButton = DialogUtil.createButton(
				i18n.getString("RestrictionDialog.restriction.remove.label",   "Remove"),
				i18n.getString("RestrictionDialog.restriction.remove.tooltip", "Remove selected restriction")); 
		
		TextFlow noticeFlowPane = new TextFlow(); 
		noticeFlowPane.setPadding(new Insets(10, 5, 10, 5));
		noticeFlowPane.setTextAlignment(TextAlignment.JUSTIFY); 
		noticeFlowPane.setLineSpacing(2.0); 
		Text noticeLabel = new Text(i18n.getString("RestrictionDialog.warning.initial_guess.label", "Please note:")); 
		noticeLabel.setFill(Color.DARKRED);
		Text noticeMessage = new Text(i18n.getString("RestrictionDialog.warning.initial_guess.message", "For some options the automatic estimation of an initial guess must be disabled, and appropriate approximations have to be specified manually.")); 
		noticeMessage.setFill(Color.BLACK);
		noticeFlowPane.getChildren().addAll(noticeLabel, new Text(" "), noticeMessage);
		
		this.addRestrictionButton.setOnAction(restrictionEventHandler);
		this.editRestrictionButton.setOnAction(restrictionEventHandler);
		this.removeRestrictionButton.setOnAction(restrictionEventHandler);

		VBox.setVgrow(this.addRestrictionButton,    Priority.ALWAYS);
		VBox.setVgrow(this.editRestrictionButton,   Priority.ALWAYS);
		VBox.setVgrow(this.removeRestrictionButton, Priority.ALWAYS);
		buttonBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		buttonBox.getChildren().addAll(this.addRestrictionButton, this.editRestrictionButton, this.removeRestrictionButton);
		
		HBox orderButtonBox = new HBox(3);
		this.moveUpRestrictionButtom = DialogUtil.createButton(
				i18n.getString("RestrictionDialog.restriction.up.label", "\u25B2"),
				i18n.getString("RestrictionDialog.restriction.up.tooltip", "Move up (change order)"));
		this.moveDownRestrictionButtom = DialogUtil.createButton(
				i18n.getString("RestrictionDialog.restriction.down.label", "\u25BC"),
				i18n.getString("RestrictionDialog.restriction.down.tooltip", "Move down (change order)"));
		
		this.moveUpRestrictionButtom.setOnAction(restrictionEventHandler);
		this.moveDownRestrictionButtom.setOnAction(restrictionEventHandler);
		
		HBox.setHgrow(this.moveUpRestrictionButtom, Priority.NEVER);
		HBox.setHgrow(this.moveDownRestrictionButtom, Priority.NEVER);
		orderButtonBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		orderButtonBox.getChildren().addAll(this.moveUpRestrictionButtom, this.moveDownRestrictionButtom);
		
		ownerLabel.setLabelFor(this.parameterOwner);
		equationLabel.setLabelFor(this.latexLabel);
		restrictionTypeLabel.setLabelFor(this.restrictionTypeLabel);
		descriptionLabel.setLabelFor(this.descriptionTextField);
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsLeft   = new Insets(5, 7, 5, 5);
		Insets insetsRight  = new Insets(5, 0, 5, 7);

		GridPane.setMargin(this.restrictionList, new Insets(5,10,5,0));
		GridPane.setMargin(orderButtonBox,     new Insets(5,10,5,0));

		GridPane.setMargin(restrictionTypeLabel, insetsLeft);
		GridPane.setMargin(descriptionLabel,     insetsLeft);
		GridPane.setMargin(equationLabel,        insetsLeft);
		GridPane.setMargin(buttonBox,            insetsLeft);
		GridPane.setMargin(ownerLabel,           insetsLeft);
		
		GridPane.setMargin(this.restrictionTypeLabel, insetsRight);
		GridPane.setMargin(this.descriptionTextField, insetsRight);
		GridPane.setMargin(this.latexLabel,           insetsRight);
		GridPane.setMargin(this.parameterOwner,       insetsRight);
		
		this.restrictionTypeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		restrictionTypeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		descriptionLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		equationLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		ownerLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		noticeFlowPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		noticeFlowPane.setPrefSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		this.restrictionTypeLabel.setMaxWidth(Double.MAX_VALUE);
		restrictionTypeLabel.setMaxWidth(Double.MAX_VALUE);
		descriptionLabel.setMaxWidth(Double.MAX_VALUE);
		equationLabel.setMaxWidth(Double.MAX_VALUE);
		ownerLabel.setMaxWidth(Double.MAX_VALUE);
		
		Region spacer = new Region();
				
		GridPane.setHgrow(equationLabel,        Priority.NEVER);
		GridPane.setHgrow(restrictionTypeLabel, Priority.NEVER);
		GridPane.setHgrow(descriptionLabel,     Priority.NEVER);
		GridPane.setHgrow(ownerLabel,           Priority.NEVER);
		
		GridPane.setHgrow(this.latexLabel,           Priority.ALWAYS);
		GridPane.setHgrow(this.restrictionTypeLabel, Priority.ALWAYS);
		GridPane.setHgrow(this.parameterOwner,       Priority.ALWAYS);
		GridPane.setHgrow(this.restrictionList,      Priority.SOMETIMES);
		GridPane.setHgrow(buttonBox,                 Priority.NEVER);
		GridPane.setHgrow(orderButtonBox,            Priority.NEVER);
		GridPane.setHgrow(noticeFlowPane,            Priority.ALWAYS);

		GridPane.setVgrow(this.restrictionList,      Priority.ALWAYS);
		GridPane.setVgrow(this.latexLabel,           Priority.NEVER);
		GridPane.setVgrow(this.restrictionTypeLabel, Priority.NEVER);
		GridPane.setVgrow(buttonBox,                 Priority.NEVER);
		GridPane.setVgrow(orderButtonBox,            Priority.NEVER);
		GridPane.setHgrow(noticeFlowPane,            Priority.NEVER);
		
		GridPane.setHgrow(spacer, Priority.ALWAYS);
		GridPane.setVgrow(spacer, Priority.ALWAYS);
		
		GridPane.setValignment(equationLabel, VPos.TOP);
		
		int row = 0;
		
		gridPane.add(ownerLabel,                1, row);  // column, row, columnspan, rowspan
		gridPane.add(this.parameterOwner,       2, row++);
	
		gridPane.add(equationLabel,             1, row);
		gridPane.add(this.latexLabel,           2, row++);
		
		gridPane.add(restrictionTypeLabel,      1, row);
		gridPane.add(this.restrictionTypeLabel, 2, row++);
		
		gridPane.add(descriptionLabel,          1, row);
		gridPane.add(this.descriptionTextField, 2, row++);
		
		gridPane.add(noticeFlowPane,            1, row++, 2, 1);
		
		gridPane.add(spacer,                    1, row++, 2, 1); // column, row, columnspan, rowspan
		gridPane.add(buttonBox,                 1, row++);
		
		gridPane.add(this.restrictionList,      0, 0, 1, row++); // column, row, columnspan, rowspan
		gridPane.add(orderButtonBox,            0, row);
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				restrictionList.requestFocus();
				if (restrictionList.getItems().size() > 0) {
					restrictionList.getSelectionModel().clearAndSelect(0);
					setRestriction(restrictionList.getItems().get(0));
				}
			}
		});
		return gridPane;
	}
	
	private ListView<Restriction> createRestrictionListView() {
		ListView<Restriction> list = new ListView<Restriction>();
		list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Restriction>() {
			public void changed(ObservableValue<? extends Restriction> observable, Restriction oldValue, Restriction newValue) {
				setRestriction(newValue);
			}
		});
		
		list.setCellFactory(new Callback<ListView<Restriction>, ListCell<Restriction>>() {
			@Override
			public ListCell<Restriction> call(ListView<Restriction> listView) {
				return new ListCell<Restriction>() { 
					@Override
					protected void updateItem(Restriction restriction, boolean empty) {
						super.updateItem(restriction, empty);
						if (empty || restriction == null)
							this.setText(null);
						else 
							this.setText(RestrictionTypeDialog.getRestrictionTypeLabel(restriction.getRestrictionType()));
					}
				};
			};
		});
		list.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		list.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		ListView<String> placeholderList = new ListView<String>();
		placeholderList.getItems().add(new String());
		placeholderList.setDisable(true);
		list.setPlaceholder(placeholderList);
		
		return list;
	}
	
	private void editRestriction(Restriction restriction) {
		RestrictionType restrictionType = restriction.getRestrictionType();
				
		switch(restrictionType) {
		case AVERAGE:
			AverageRestrictionDialog.showAndWait(this.filteredUnknownParameters, (AverageRestriction)restriction);
			break;
			
		case PRODUCT_SUM:
			ProductSumRestrictionDialog.showAndWait(this.filteredUnknownParameters, (ProductSumRestriction)restriction);
			break;
			
		case FEATURE_POINT:
			FeaturePointRestrictionDialog.showAndWait(this.feature.getGeometricPrimitives(), UIPointTableBuilder.getInstance().getTable().getItems(), (FeaturePointRestriction)restriction);
			break;
			
		case VECTOR_ANGLE:
			VectorAngleRestrictionDialog.showAndWait(this.filteredUnknownParameters, (VectorAngleRestriction)restriction);
			break;
			
		case TRIGONOMERTIC_FUNCTION:
			TrigonometricRestrictionDialog.showAndWait(this.filteredUnknownParameters, (TrigonometricRestriction)restriction);
			break;
		}
	}
	
	private void removeRestriction(Restriction restriction) {
		if (restriction.isIndispensable())
			return;

		this.restrictionList.getItems().remove(restriction);
		this.removeRestrictionButton.setDisable(this.restrictionList.getItems().size() == 0);
	}
	
	private Restriction createRestriction(RestrictionType restrictionType) {
		switch(restrictionType) {
		case AVERAGE:
			return new AverageRestriction();
			
		case PRODUCT_SUM:
			return new ProductSumRestriction();
			
		case FEATURE_POINT:
			return new FeaturePointRestriction();
			
		case VECTOR_ANGLE:
			return new VectorAngleRestriction();
			
		case TRIGONOMERTIC_FUNCTION:
			return new TrigonometricRestriction();
		}
		return null;
	}
	
	private void setUnknownParameters(ObservableList<UnknownParameter> unknownParameters) {
		if (unknownParameters != null)
			this.filteredUnknownParameters = new FilteredList<UnknownParameter>(unknownParameters, new PostProcessingPredicate());
	}
	
	private void setFeature(Feature feature, boolean enablePostProcessing) {
		this.enablePostProcessing = enablePostProcessing;
		this.feature = feature;
		
		if (this.feature != null) {
			if (enablePostProcessing) {
				this.setUnknownParameters(feature.getUnknownParameters());
				this.setEquations(feature.getPostProcessingCalculations());
			}
			else {
				this.setUnknownParameters(feature.getUnknownParameters());
				this.setEquations(feature.getRestrictions());
			}
		}
	}
	
	private void setEquations(ObservableList<Restriction> equations) {
		if (equations != null) {
			this.editRestrictionButton.setDisable(equations.size() == 0);
			this.removeRestrictionButton.setDisable(equations.size() == 0);
			this.restrictionList.setItems(equations);
			this.restrictionList.requestFocus();
			if (this.restrictionList.getItems().size() > 0) {
				this.setRestriction(this.restrictionList.getItems().get(0));
				this.restrictionList.getSelectionModel().clearAndSelect(0);
			}
		}
	}
}
