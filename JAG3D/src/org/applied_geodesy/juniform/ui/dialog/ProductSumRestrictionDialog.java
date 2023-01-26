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

import java.util.Collections;
import java.util.Optional;

import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.restriction.ProductSumRestriction;
import org.applied_geodesy.adjustment.geometry.restriction.ProductSumRestriction.SignType;
import org.applied_geodesy.ui.tex.LaTexLabel;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.ui.textfield.DoubleTextField.ValueSupport;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.juniform.ui.i18n.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ProductSumRestrictionDialog {
	private class RegressorEventHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() == addParameterAButtom) {
				UnknownParameter parameter = regressorAComboBox.getValue();
				if (parameter != null)
					regressorAListView.getItems().add(parameter);
			}
			else if (event.getSource() == addParameterBButtom) {
				UnknownParameter parameter = regressorBComboBox.getValue();
				if (parameter != null)
					regressorBListView.getItems().add(parameter);
			}
			
			else if (event.getSource() == removeParameterAButtom) {
				UnknownParameter parameter = regressorAListView.getSelectionModel().getSelectedItem();
				if (parameter != null)
					regressorAListView.getItems().remove(parameter);
			}
			else if (event.getSource() == removeParameterBButtom) {
				UnknownParameter parameter = regressorBListView.getSelectionModel().getSelectedItem();
				if (parameter != null)
					regressorBListView.getItems().remove(parameter);
			}
			
			else if (event.getSource() == moveUpParameterAButtom) {
				int index = regressorAListView.getSelectionModel().getSelectedIndex();
				if (index > 0) {
					Collections.swap(regressorAListView.getItems(), index, index-1);
				}
			}
			
			else if (event.getSource() == moveDownParameterAButtom) {
				int index = regressorAListView.getSelectionModel().getSelectedIndex();
				if (index < regressorAListView.getItems().size() - 1) {
					Collections.swap(regressorAListView.getItems(), index, index+1);
				}
			}
			
			else if (event.getSource() == moveUpParameterBButtom) {
				int index = regressorBListView.getSelectionModel().getSelectedIndex();
				if (index > 0) {
					Collections.swap(regressorBListView.getItems(), index, index-1);
				}
			}
			
			else if (event.getSource() == moveDownParameterBButtom) {
				int index = regressorBListView.getSelectionModel().getSelectedIndex();
				if (index < regressorBListView.getItems().size() - 1) {
					Collections.swap(regressorBListView.getItems(), index, index+1);
				}
			}
			
			if (regressorAListView.getItems().size() > signTypeListView.getItems().size() || regressorBListView.getItems().size() > signTypeListView.getItems().size())
				signTypeListView.getItems().add(SignType.PLUS);
			
			if (signTypeListView.getItems().size() > regressorAListView.getItems().size() && regressorAListView.getItems().size() > regressorBListView.getItems().size())
				signTypeListView.getItems().remove(0);
		}
	}
	
	private static I18N i18N = I18N.getInstance();
	private static ProductSumRestrictionDialog restrictionTypeDialog = new ProductSumRestrictionDialog();
	private Dialog<ProductSumRestriction> dialog = null;
	private LaTexLabel latexLabel;
	private ComboBox<UnknownParameter> regressorAComboBox;
	private ComboBox<UnknownParameter> regressorBComboBox;
	private ListView<SignType> signTypeListView;
	private ListView<UnknownParameter> regressorAListView;
	private ListView<UnknownParameter> regressorBListView;
	private Button addParameterAButtom, removeParameterAButtom, moveUpParameterAButtom, moveDownParameterAButtom;
	private Button addParameterBButtom, removeParameterBButtom, moveUpParameterBButtom, moveDownParameterBButtom;
	private DoubleTextField exponentTextField;
	private ComboBox<UnknownParameter> regressandComboBox;
	private TextField descriptionTextField;
	private Window window;
	private ProductSumRestriction restriction;

	private ProductSumRestrictionDialog() {}

	public static void setOwner(Window owner) {
		restrictionTypeDialog.window = owner;
	}

	public static Optional<ProductSumRestriction> showAndWait(ObservableList<UnknownParameter> unknownParameters, ProductSumRestriction productSumRestriction) {
		restrictionTypeDialog.init();
		restrictionTypeDialog.setUnknownParameters(unknownParameters);
		restrictionTypeDialog.setRestriction(productSumRestriction);
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					restrictionTypeDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) restrictionTypeDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return restrictionTypeDialog.dialog.showAndWait();
	}
	
	private void setUnknownParameters(ObservableList<UnknownParameter> unknownParameters) {
		// a copy is needed because the argument list "unknownParameters" is a reference and becomes obsolete if the feature is changed 
		ObservableList<UnknownParameter> unknownParameterList = FXCollections.observableArrayList(unknownParameters);
		this.regressorAComboBox.setItems(unknownParameterList);
		this.regressorBComboBox.setItems(unknownParameterList);
		this.regressandComboBox.setItems(unknownParameterList);
	}
	
	private void setRestriction(ProductSumRestriction restriction) {
		this.moveUpParameterAButtom.setDisable(true);
		this.moveDownParameterAButtom.setDisable(true);
		
		this.moveUpParameterBButtom.setDisable(true);
		this.moveDownParameterBButtom.setDisable(true);
		
		if (this.restriction != null) {
			this.exponentTextField.numberProperty().unbindBidirectional(this.restriction.exponentProperty());
			this.descriptionTextField.textProperty().unbindBidirectional(this.restriction.descriptionProperty());
			this.regressandComboBox.valueProperty().unbindBidirectional(this.restriction.regressandProperty());
			this.signTypeListView.setItems(FXCollections.emptyObservableList());
			this.regressorAListView.setItems(FXCollections.emptyObservableList());
			this.regressorBListView.setItems(FXCollections.emptyObservableList());
		}
		
		this.restriction = restriction;
		this.descriptionTextField.textProperty().bindBidirectional(this.restriction.descriptionProperty());
		this.exponentTextField.numberProperty().bindBidirectional(this.restriction.exponentProperty());
		this.regressandComboBox.valueProperty().bindBidirectional(this.restriction.regressandProperty());
		this.signTypeListView.setItems(this.restriction.getCoefficientSigns());
		this.regressorAListView.setItems(this.restriction.getRegressorsA());
		this.regressorBListView.setItems(this.restriction.getRegressorsB());
		this.latexLabel.setTex(this.restriction.toLaTex());
		this.exponentTextField.setValue(this.restriction.getExponent());
		
		this.exponentTextField.setDisable(restriction.isIndispensable());

		this.signTypeListView.setDisable(restriction.isIndispensable());
		this.regressorAListView.setDisable(restriction.isIndispensable());
		this.regressorBListView.setDisable(restriction.isIndispensable());
		
		this.regressorAComboBox.setDisable(restriction.isIndispensable());
		this.regressorBComboBox.setDisable(restriction.isIndispensable());
		
		this.regressandComboBox.setDisable(restriction.isIndispensable());
		this.addParameterAButtom.setDisable(restriction.isIndispensable()); 
		this.removeParameterAButtom.setDisable(restriction.isIndispensable()); 
		this.moveUpParameterAButtom.setDisable(restriction.isIndispensable()); 
		this.moveDownParameterAButtom.setDisable(restriction.isIndispensable());
		this.addParameterBButtom.setDisable(restriction.isIndispensable()); 
		this.removeParameterBButtom.setDisable(restriction.isIndispensable()); 
		this.moveUpParameterBButtom.setDisable(restriction.isIndispensable()); 
		this.moveDownParameterBButtom.setDisable(restriction.isIndispensable());
		
		if(!restriction.isIndispensable()) {
			UnknownParameter regressand = this.restriction.getRegressand();
			if (regressand == null)
				this.regressandComboBox.getSelectionModel().clearAndSelect(0);
			this.regressorAComboBox.getSelectionModel().clearAndSelect(0);
			this.regressorBComboBox.getSelectionModel().clearAndSelect(0);
		}
		else {
			this.regressorAComboBox.getSelectionModel().clearSelection();
			this.regressorBComboBox.getSelectionModel().clearSelection();
		}
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<ProductSumRestriction>();
		this.dialog.setTitle(i18N.getString("ProductSumRestrictionDialog.title", "Product sum restriction"));
		this.dialog.setHeaderText(i18N.getString("ProductSumRestrictionDialog.header", "k-th Power of product sum"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, ProductSumRestriction>() {
			@Override
			public ProductSumRestriction call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {				
					return restriction;
				}
				return null;
			}
		});
	}
	
	private Node createPane() {
		GridPane gridPane = DialogUtil.createGridPane();
		
		Label equationLabel    = new Label(i18N.getString("ProductSumRestrictionDialog.equation.label",     "Equation:"));
		Label exponentLabel    = new Label(i18N.getString("ProductSumRestrictionDialog.exponent.label",     "Exponent k:"));
		Label descriptionLabel = new Label(i18N.getString("ProductSumRestrictionDialog.description.label",  "Description:"));
		Label regressandLabel  = new Label(i18N.getString("ProductSumRestrictionDialog.regressand.label",   "Regressand c:"));
		Label regressorALabel  = new Label(i18N.getString("ProductSumRestrictionDialog.regressor.a.label",  "Regressor a:"));
		Label regressorBLabel  = new Label(i18N.getString("ProductSumRestrictionDialog.regressor.b.label",  "Regressor b:"));

		regressorALabel.setPrefSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		regressorALabel.setMaxWidth(Double.MAX_VALUE);
		
		regressorBLabel.setPrefSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		regressorBLabel.setMaxWidth(Double.MAX_VALUE);

		this.exponentTextField = DialogUtil.createDoubleTextField(
				CellValueType.DOUBLE, 1.0, Boolean.FALSE, 
				ValueSupport.NON_NULL_VALUE_SUPPORT, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 
				i18N.getString("ProductSumRestrictionDialog.exponent.tooltip", "Select exponent k"));
		
		this.descriptionTextField = DialogUtil.createTextField(
				i18N.getString("ProductSumRestrictionDialog.description.tooltip", "Description of parameter restriction"),
				i18N.getString("ProductSumRestrictionDialog.description.prompt", "Restriction description"));
		
		this.signTypeListView = DialogUtil.createSignTypeListView(
				ProductSumRestrictionDialog.createSignTypeStringConverter());
		this.signTypeListView.setPrefWidth(50);
		
		this.regressorAComboBox = DialogUtil.createUnknownParameterComboBox(
				UnknownParameterDialog.createUnknownParameterCellFactory(), 
				i18N.getString("ProductSumRestrictionDialog.regressor.a.tooltip", "Select regressor a"));
		
		this.regressorBComboBox = DialogUtil.createUnknownParameterComboBox(
				UnknownParameterDialog.createUnknownParameterCellFactory(), 
				i18N.getString("ProductSumRestrictionDialog.regressor.b.tooltip", "Select regressor b"));
		
		this.regressandComboBox = DialogUtil.createUnknownParameterComboBox(
				UnknownParameterDialog.createUnknownParameterCellFactory(), 
				i18N.getString("ProductSumRestrictionDialog.regressand.tooltip", "Select regressand c"));
		
		this.latexLabel = new LaTexLabel();
		this.latexLabel.setPrefSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.latexLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		this.regressorAListView = DialogUtil.createParameterListView(UnknownParameterDialog.createUnknownParameterCellFactory());
		this.regressorBListView = DialogUtil.createParameterListView(UnknownParameterDialog.createUnknownParameterCellFactory());
		
		RegressorEventHandler regressorEventHandler = new RegressorEventHandler();
		
		this.addParameterAButtom = DialogUtil.createButton(
				i18N.getString("ProductSumRestrictionDialog.regressor.a.add.label", "+"),
				i18N.getString("ProductSumRestrictionDialog.regressor.a.add.tooltip", "Add parameter to regressor list"));
		
		this.removeParameterAButtom = DialogUtil.createButton(
				i18N.getString("ProductSumRestrictionDialog.regressor.a.remove.label", "-"),
				i18N.getString("ProductSumRestrictionDialog.regressor.a.remove.tooltip", "Remove regressor"));
		
		this.moveUpParameterAButtom = DialogUtil.createButton(
				i18N.getString("ProductSumRestrictionDialog.regressor.a.up.label", "\u25B2"),
				i18N.getString("ProductSumRestrictionDialog.regressor.a.up.tooltip", "Move up (change order)"));
		
		this.moveDownParameterAButtom = DialogUtil.createButton(
				i18N.getString("ProductSumRestrictionDialog.regressor.a.down.label", "\u25BC"),
				i18N.getString("ProductSumRestrictionDialog.regressor.a.down.tooltip", "Move down (change order)"));
		
		this.addParameterAButtom.setOnAction(regressorEventHandler);
		this.removeParameterAButtom.setOnAction(regressorEventHandler);
		this.moveUpParameterAButtom.setOnAction(regressorEventHandler);
		this.moveDownParameterAButtom.setOnAction(regressorEventHandler);
		
		HBox buttonBoxA = new HBox(5);
		HBox.setHgrow(this.addParameterAButtom,      Priority.NEVER);
		HBox.setHgrow(this.removeParameterAButtom,   Priority.NEVER);
		HBox.setHgrow(this.moveUpParameterAButtom,   Priority.NEVER);
		HBox.setHgrow(this.moveDownParameterAButtom, Priority.NEVER);
		
		buttonBoxA.getChildren().addAll(
				this.addParameterAButtom,
				this.removeParameterAButtom,
				this.moveUpParameterAButtom,
				this.moveDownParameterAButtom
		);
		
		this.addParameterBButtom = DialogUtil.createButton(
				i18N.getString("ProductSumRestrictionDialog.regressor.b.add.label", "+"),
				i18N.getString("ProductSumRestrictionDialog.regressor.b.add.tooltip", "Add parameter to regressor list"));
		
		this.removeParameterBButtom = DialogUtil.createButton(
				i18N.getString("ProductSumRestrictionDialog.regressor.b.remove.label", "-"),
				i18N.getString("ProductSumRestrictionDialog.regressor.b.remove.tooltip", "Remove regressor"));
		
		this.moveUpParameterBButtom = DialogUtil.createButton(
				i18N.getString("ProductSumRestrictionDialog.regressor.b.up.label", "\u25B2"),
				i18N.getString("ProductSumRestrictionDialog.regressor.b.up.tooltip", "Move up (change order)"));
		
		this.moveDownParameterBButtom = DialogUtil.createButton(
				i18N.getString("ProductSumRestrictionDialog.regressor.b.down.label", "\u25BC"),
				i18N.getString("ProductSumRestrictionDialog.regressor.b.down.tooltip", "Move down (change order)"));
		
		this.addParameterBButtom.setOnAction(regressorEventHandler);
		this.removeParameterBButtom.setOnAction(regressorEventHandler);
		this.moveUpParameterBButtom.setOnAction(regressorEventHandler);
		this.moveDownParameterBButtom.setOnAction(regressorEventHandler);
		
		HBox buttonBoxB = new HBox(5);
		HBox.setHgrow(this.addParameterBButtom,      Priority.NEVER);
		HBox.setHgrow(this.removeParameterBButtom,   Priority.NEVER);
		HBox.setHgrow(this.moveUpParameterBButtom,   Priority.NEVER);
		HBox.setHgrow(this.moveDownParameterBButtom, Priority.NEVER);
		
		buttonBoxB.getChildren().addAll(
				this.addParameterBButtom,
				this.removeParameterBButtom,
				this.moveUpParameterBButtom,
				this.moveDownParameterBButtom
		);
		
		this.regressorAListView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				moveUpParameterAButtom.setDisable(newValue == null || newValue.intValue() == 0);
				moveDownParameterAButtom.setDisable(newValue == null || newValue.intValue() == regressorAListView.getItems().size() - 1);
			}	
		});
		
		this.regressorBListView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				moveUpParameterBButtom.setDisable(newValue == null || newValue.intValue() == 0);
				moveDownParameterBButtom.setDisable(newValue == null || newValue.intValue() == regressorBListView.getItems().size() - 1);
			}	
		});
		
		exponentLabel.setLabelFor(this.exponentTextField);
		equationLabel.setLabelFor(this.latexLabel);
		descriptionLabel.setLabelFor(this.descriptionTextField);
		regressandLabel.setLabelFor(this.regressandComboBox);
		regressorALabel.setLabelFor(this.regressorAComboBox);
		regressorBLabel.setLabelFor(this.regressorBComboBox);
		
		regressorALabel.setAlignment(Pos.TOP_CENTER);
		regressorBLabel.setAlignment(Pos.TOP_CENTER);
		
		GridPane.setHgrow(exponentLabel,    Priority.NEVER);
		GridPane.setHgrow(equationLabel,    Priority.NEVER);
		GridPane.setHgrow(descriptionLabel, Priority.NEVER);
		GridPane.setHgrow(regressandLabel,  Priority.NEVER);
		GridPane.setVgrow(regressorALabel,  Priority.NEVER);
		GridPane.setVgrow(regressorBLabel,  Priority.NEVER);
		GridPane.setHgrow(this.latexLabel,  Priority.ALWAYS);
		
		GridPane.setHgrow(this.regressorAComboBox,   Priority.ALWAYS);
		GridPane.setHgrow(this.regressorBComboBox,   Priority.ALWAYS);
		GridPane.setVgrow(this.regressorAComboBox,   Priority.NEVER);
		GridPane.setVgrow(this.regressorBComboBox,   Priority.NEVER);
		GridPane.setHgrow(this.exponentTextField,    Priority.ALWAYS);
		GridPane.setHgrow(this.descriptionTextField, Priority.ALWAYS);
		GridPane.setHgrow(this.regressandComboBox,   Priority.ALWAYS);
		GridPane.setHgrow(this.regressorAListView,   Priority.ALWAYS);
		GridPane.setVgrow(this.regressorAListView,   Priority.ALWAYS);
		GridPane.setHgrow(this.regressorBListView,   Priority.ALWAYS);
		GridPane.setVgrow(this.regressorBListView,   Priority.ALWAYS);
		GridPane.setHgrow(this.signTypeListView,     Priority.NEVER);
		GridPane.setVgrow(this.signTypeListView,     Priority.ALWAYS);

		GridPane regressorsPane = DialogUtil.createGridPane();
		regressorsPane.setPrefHeight(200);
		regressorsPane.setHgap(10);
		regressorsPane.setVgap( 5);
		regressorsPane.setPadding(new Insets(0,0,0,0)); // oben, recht, unten, links

		int row = 0;
		regressorsPane.add(regressorALabel,          1, row); // column, row, columnspan, rowspan,
		regressorsPane.add(regressorBLabel,          2, row++); 
		regressorsPane.add(this.regressorAComboBox,  1, row); 
		regressorsPane.add(this.regressorBComboBox,  2, row++);
		regressorsPane.add(this.signTypeListView,    0, row); 
		regressorsPane.add(this.regressorAListView,  1, row); 
		regressorsPane.add(this.regressorBListView,  2, row++); 
		regressorsPane.add(buttonBoxA,               1, row); 
		regressorsPane.add(buttonBoxB,               2, row); 
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsLeft   = new Insets(5, 7, 5, 5);
		Insets insetsRight  = new Insets(5, 0, 5, 7);

		GridPane.setMargin(exponentLabel,    insetsLeft);
		GridPane.setMargin(equationLabel,    insetsLeft);
		GridPane.setMargin(regressandLabel,  insetsLeft);
		GridPane.setMargin(descriptionLabel, insetsLeft);

		GridPane.setMargin(this.exponentTextField,    insetsRight);
		GridPane.setMargin(this.latexLabel,           insetsRight);
		GridPane.setMargin(this.regressandComboBox,   insetsRight);
		GridPane.setMargin(this.descriptionTextField, insetsRight);
		
		GridPane.setMargin(regressorsPane, new Insets(10, 5, 5, 5));
		
		row = 0;
		
		gridPane.add(equationLabel,             0, row); // column, row, columnspan, rowspan,
		gridPane.add(this.latexLabel,           1, row++);
		
		gridPane.add(descriptionLabel,          0, row);
		gridPane.add(this.descriptionTextField, 1, row++);
		
		gridPane.add(regressandLabel,           0, row);
		gridPane.add(this.regressandComboBox,   1, row++);
		
		gridPane.add(exponentLabel,             0, row);
		gridPane.add(this.exponentTextField,    1, row++);
		
		gridPane.add(regressorsPane,            0, row++, 2, 1); 

		return gridPane;
	}
	
	static StringConverter<SignType> createSignTypeStringConverter() {
		return new StringConverter<SignType>() {

			@Override
			public String toString(SignType signType) {
				if (signType == null)
					return null;

				return signType.toString();
			}

			@Override
			public SignType fromString(String string) {
				return string != null && string.equals("-") ? SignType.MINUS : SignType.PLUS; 
			}
		};
	}
}
