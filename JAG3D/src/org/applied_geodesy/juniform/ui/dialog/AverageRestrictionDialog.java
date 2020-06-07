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

import org.applied_geodesy.adjustment.geometry.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.geometry.restriction.AverageRestriction;
import org.applied_geodesy.ui.tex.LaTexLabel;
import org.applied_geodesy.juniform.ui.i18n.I18N;

import javafx.application.Platform;
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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class AverageRestrictionDialog {
	private class RegressorEventHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() == addParameterButtom) {
				UnknownParameter parameter = regressorComboBox.getValue();
				if (parameter != null)
					regressorListView.getItems().add(parameter);
			}	
			else if (event.getSource() == removeParameterButtom) {
				UnknownParameter parameter = regressorListView.getSelectionModel().getSelectedItem();
				if (parameter != null)
					regressorListView.getItems().remove(parameter);
			}
		}
	}

	private static I18N i18N = I18N.getInstance();
	private static AverageRestrictionDialog restrictionTypeDialog = new AverageRestrictionDialog();
	private Dialog<AverageRestriction> dialog = null;
	private LaTexLabel latexLabel;
	private ComboBox<UnknownParameter> regressorComboBox;
	private ListView<UnknownParameter> regressorListView;
	private Button addParameterButtom, removeParameterButtom;
	private ComboBox<UnknownParameter> regressandComboBox;
	private TextField descriptionTextField;
	private Window window;
	private AverageRestriction restriction;

	private AverageRestrictionDialog() {}

	public static void setOwner(Window owner) {
		restrictionTypeDialog.window = owner;
	}

	public static Optional<AverageRestriction> showAndWait(ObservableList<UnknownParameter> unknownParameters, AverageRestriction averageRestriction) {
		restrictionTypeDialog.init();
		restrictionTypeDialog.setUnknownParameters(unknownParameters);
		restrictionTypeDialog.setRestriction(averageRestriction);
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
		this.regressorComboBox.setItems(unknownParameterList);
		this.regressandComboBox.setItems(unknownParameterList);
	}
	
	private void setRestriction(AverageRestriction restriction) {		
		if (this.restriction != null) {
			this.descriptionTextField.textProperty().unbindBidirectional(this.restriction.descriptionProperty());
			this.regressandComboBox.valueProperty().unbindBidirectional(this.restriction.regressandProperty());
			this.regressorListView.setItems(FXCollections.emptyObservableList());
		}
		
		this.restriction = restriction;
		this.descriptionTextField.textProperty().bindBidirectional(this.restriction.descriptionProperty());
		this.regressandComboBox.valueProperty().bindBidirectional(this.restriction.regressandProperty());
		this.regressorListView.setItems(this.restriction.getRegressors());
		this.latexLabel.setTex(this.restriction.toLaTex());

		this.regressorListView.setDisable(restriction.isIndispensable());
		this.regressorComboBox.setDisable(restriction.isIndispensable());
		this.regressandComboBox.setDisable(restriction.isIndispensable());
		
		this.addParameterButtom.setDisable(restriction.isIndispensable()); 
		this.removeParameterButtom.setDisable(restriction.isIndispensable()); 
				
		if(!restriction.isIndispensable()) {
			UnknownParameter regressand = this.restriction.getRegressand();
			if (regressand == null)
				this.regressandComboBox.getSelectionModel().clearAndSelect(0);
			this.regressorComboBox.getSelectionModel().clearAndSelect(0);
		}
		else {
			this.regressorComboBox.getSelectionModel().clearSelection();
		}
	}
	
	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<AverageRestriction>();
		this.dialog.setTitle(i18N.getString("AverageRestrictionDialog.title", "Average restriction"));
		this.dialog.setHeaderText(i18N.getString("AverageRestrictionDialog.header", "Average value restriction"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, AverageRestriction>() {
			@Override
			public AverageRestriction call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {				
					return restriction;
				}
				return null;
			}
		});
	}
	
	private Node createPane() {
		GridPane gridPane = DialogUtil.createGridPane();
		
		Label equationLabel    = new Label(i18N.getString("AverageRestrictionDialog.equation.label",    "Equation:"));
		Label descriptionLabel = new Label(i18N.getString("AverageRestrictionDialog.description.label", "Description:"));
		Label regressandLabel  = new Label(i18N.getString("AverageRestrictionDialog.regressand.label",  "Regressand c:"));
		Label regressorLabel   = new Label(i18N.getString("AverageRestrictionDialog.regressor.label",   "Regressor a:"));

		regressorLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		regressorLabel.setMaxWidth(Double.MAX_VALUE);
		
		this.descriptionTextField = DialogUtil.createTextField(
				i18N.getString("AverageRestrictionDialog.description.tooltip", "Description of parameter restriction"),
				i18N.getString("AverageRestrictionDialog.description.prompt", "Restriction description"));
		
		this.regressorComboBox = DialogUtil.createUnknownParameterComboBox(
				UnknownParameterDialog.createUnknownParameterCellFactory(), 
				i18N.getString("AverageRestrictionDialog.regressor.tooltip", "Select regressor a"));
		
		this.regressandComboBox = DialogUtil.createUnknownParameterComboBox(
				UnknownParameterDialog.createUnknownParameterCellFactory(), 
				i18N.getString("AverageRestrictionDialog.regressand.tooltip", "Select regressand c"));
		
		this.latexLabel = new LaTexLabel();
		this.latexLabel.setPrefSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.latexLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		this.regressorListView = DialogUtil.createParameterListView(UnknownParameterDialog.createUnknownParameterCellFactory());
		
		RegressorEventHandler regressorEventHandler = new RegressorEventHandler();
		this.addParameterButtom = DialogUtil.createButton(
				i18N.getString("AverageRestrictionDialog.regressor.add.label", "+"),
				i18N.getString("AverageRestrictionDialog.regressor.add.tooltip", "Add parameter to regressor list"));
		
		this.removeParameterButtom = DialogUtil.createButton(
				i18N.getString("AverageRestrictionDialog.regressor.remove.label", "-"),
				i18N.getString("AverageRestrictionDialog.regressor.remove.tooltip", "Remove regressor"));
			
		this.addParameterButtom.setOnAction(regressorEventHandler);
		this.removeParameterButtom.setOnAction(regressorEventHandler);
		
		HBox buttonBoxA = new HBox(5);
		HBox.setHgrow(this.addParameterButtom,      Priority.NEVER);
		HBox.setHgrow(this.removeParameterButtom,   Priority.NEVER);
		
		buttonBoxA.getChildren().addAll(
				this.addParameterButtom,
				this.removeParameterButtom
		);
		
		equationLabel.setLabelFor(this.latexLabel);
		descriptionLabel.setLabelFor(this.descriptionTextField);
		regressandLabel.setLabelFor(this.regressandComboBox);
		regressorLabel.setLabelFor(this.regressorComboBox);
		regressorLabel.setAlignment(Pos.TOP_CENTER);
		
		HBox regressorsBox = new HBox(10);
		VBox regressorABox = new VBox(5);
		regressorsBox.setPrefHeight(200);
		
		HBox.setHgrow(regressorABox, Priority.ALWAYS);
		
		VBox.setVgrow(regressorLabel, Priority.NEVER);
		VBox.setVgrow(this.regressorComboBox, Priority.NEVER);
		VBox.setVgrow(this.regressorListView, Priority.ALWAYS);
		
		regressorABox.getChildren().addAll(regressorLabel, this.regressorComboBox, this.regressorListView, buttonBoxA);
		regressorsBox.getChildren().addAll(regressorABox);

		GridPane.setHgrow(equationLabel,    Priority.NEVER);
		GridPane.setHgrow(descriptionLabel, Priority.NEVER);
		GridPane.setHgrow(regressandLabel,  Priority.NEVER);
		GridPane.setHgrow(this.latexLabel,  Priority.ALWAYS);

		GridPane.setHgrow(this.descriptionTextField, Priority.ALWAYS);
		GridPane.setHgrow(this.regressandComboBox,   Priority.ALWAYS);
		GridPane.setHgrow(regressorsBox,             Priority.ALWAYS);
		GridPane.setVgrow(regressorsBox,             Priority.ALWAYS);
						
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsLeft   = new Insets(5, 7, 5, 5);
		Insets insetsRight  = new Insets(5, 0, 5, 7);

		GridPane.setMargin(equationLabel,    insetsLeft);
		GridPane.setMargin(regressandLabel,  insetsLeft);
		GridPane.setMargin(descriptionLabel, insetsLeft);

		GridPane.setMargin(this.latexLabel,           insetsRight);
		GridPane.setMargin(this.regressandComboBox,   insetsRight);
		GridPane.setMargin(this.descriptionTextField, insetsRight);
		
		GridPane.setMargin(regressorsBox, new Insets(10, 5, 5, 5));
		
		int row = 0;
		
		gridPane.add(equationLabel,             0, row); // column, row, columnspan, rowspan,
		gridPane.add(this.latexLabel,           1, row++);
		
		gridPane.add(descriptionLabel,          0, row);
		gridPane.add(this.descriptionTextField, 1, row++);
		
		gridPane.add(regressandLabel,           0, row);
		gridPane.add(this.regressandComboBox,   1, row++);
		
		gridPane.add(regressorsBox,             0, row++, 2, 1); 

		return gridPane;
	}

	
	
}