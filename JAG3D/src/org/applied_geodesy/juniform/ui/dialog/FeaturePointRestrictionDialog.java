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

import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.adjustment.geometry.restriction.FeaturePointRestriction;
import org.applied_geodesy.juniform.ui.i18n.I18N;
import org.applied_geodesy.juniform.ui.propertiespane.UIPointSelectionPaneBuilder;
import org.applied_geodesy.ui.tex.LaTexLabel;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class FeaturePointRestrictionDialog {

	private static I18N i18N = I18N.getInstance();
	private static FeaturePointRestrictionDialog restrictionTypeDialog = new FeaturePointRestrictionDialog();
	private Dialog<FeaturePointRestriction> dialog = null;
	private LaTexLabel latexLabel;
	private ComboBox<GeometricPrimitive> geometricPrimitiveComboBox;
	private ComboBox<FeaturePoint> featurePointComboBox;
	private TextField descriptionTextField;
	private Window window;
	private FeaturePointRestriction restriction;
	
	
	private FeaturePointRestrictionDialog() {}

	public static void setOwner(Window owner) {
		restrictionTypeDialog.window = owner;
	}

	public static Optional<FeaturePointRestriction> showAndWait(ObservableList<GeometricPrimitive> geometricPrimitives, ObservableList<FeaturePoint> featurePoints, FeaturePointRestriction featurePointRestriction) {
		restrictionTypeDialog.init();
		restrictionTypeDialog.setGeometricPrimitives(geometricPrimitives);
		restrictionTypeDialog.setFeaturePoints(featurePoints);
		restrictionTypeDialog.setRestriction(featurePointRestriction);
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
	
	private void setRestriction(FeaturePointRestriction restriction) {
		if (this.restriction != null) {
			this.geometricPrimitiveComboBox.valueProperty().unbindBidirectional(this.restriction.geometricPrimitiveProperty());
			this.featurePointComboBox.valueProperty().unbindBidirectional(this.restriction.featurePointProperty());
			this.descriptionTextField.textProperty().unbindBidirectional(this.restriction.descriptionProperty());
		}
		
		this.restriction = restriction;

		this.geometricPrimitiveComboBox.valueProperty().bindBidirectional(this.restriction.geometricPrimitiveProperty());
		this.featurePointComboBox.valueProperty().bindBidirectional(this.restriction.featurePointProperty());
		this.descriptionTextField.textProperty().bindBidirectional(this.restriction.descriptionProperty());
		this.latexLabel.setTex(this.restriction.toLaTex());
		
		this.geometricPrimitiveComboBox.setDisable(restriction.isIndispensable());
		this.featurePointComboBox.setDisable(restriction.isIndispensable());
			
		if (!restriction.isIndispensable()) {
			if (restriction.getGeometricPrimitive() != null)
				this.geometricPrimitiveComboBox.getSelectionModel().select(restriction.getGeometricPrimitive());
			else
				this.geometricPrimitiveComboBox.getSelectionModel().clearAndSelect(0);
			
			if (restriction.getFeaturePoint() != null)
				this.featurePointComboBox.getSelectionModel().select(restriction.getFeaturePoint());
			else
				this.featurePointComboBox.getSelectionModel().clearAndSelect(0);
		}
	}
	
	private void setFeaturePoints(ObservableList<FeaturePoint> featurePoints) {
		ObservableList<FeaturePoint> featurePointList = FXCollections.observableArrayList(featurePoints);
		this.featurePointComboBox.setItems(featurePointList);
	}
	
	private void setGeometricPrimitives(ObservableList<GeometricPrimitive> geometricPrimitives) {
		ObservableList<GeometricPrimitive> geometricPrimitiveList = FXCollections.observableArrayList(geometricPrimitives);
		this.geometricPrimitiveComboBox.setItems(geometricPrimitiveList);
	}
	
	
	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<FeaturePointRestriction>();
		this.dialog.setTitle(i18N.getString("FeaturePointRestrictionDialog.title", "Feature point restriction"));
		this.dialog.setHeaderText(i18N.getString("FeaturePointRestrictionDialog.header", "Feature point restriction"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, FeaturePointRestriction>() {
			@Override
			public FeaturePointRestriction call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {				
					return restriction;
				}
				return null;
			}
		});
	}
	 //TODO i18n
	private Node createPane() {
		GridPane gridPane = DialogUtil.createGridPane();
		
		Label equationLabel    = new Label(i18N.getString("FeaturePointRestrictionDialog.equation.label",     "Equation:"));
		Label descriptionLabel = new Label(i18N.getString("FeaturePointRestrictionDialog.description.label",  "Description:"));
		Label geometryLabel    = new Label(i18N.getString("FeaturePointRestrictionDialog.primitive.label",    "Geometric primitive:"));
		Label pointLabel       = new Label(i18N.getString("FeaturePointRestrictionDialog.featurepoint.label", "Feature point:"));
		
		equationLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		descriptionLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		geometryLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		pointLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

		this.descriptionTextField = DialogUtil.createTextField(
				i18N.getString("FeaturePointRestrictionDialog.description.tooltip", "Description of parameter restriction"),
				i18N.getString("FeaturePointRestrictionDialog.description.prompt",  "Restriction description"));
		
		this.geometricPrimitiveComboBox = DialogUtil.createGeometricPrimitiveComboBox(
				FeatureDialog.createGeometricPrimitiveCellFactory(), 
				i18N.getString("FeaturePointRestrictionDialog.primitive.tooltip", "Select geometric primitive"));
		
		this.featurePointComboBox = DialogUtil.createFeaturePointComboBox(
				UIPointSelectionPaneBuilder.createFeaturePointCellFactory(), 
				i18N.getString("FeaturePointRestrictionDialog.featurepoint.tooltip", "Select feature point"));
		
		this.latexLabel = new LaTexLabel();
		this.latexLabel.setPrefSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.latexLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		equationLabel.setLabelFor(this.latexLabel);
		descriptionLabel.setLabelFor(this.descriptionTextField);
		geometryLabel.setLabelFor(this.geometricPrimitiveComboBox);
		pointLabel.setLabelFor(this.featurePointComboBox);
		
		
		GridPane.setHgrow(equationLabel,    Priority.NEVER);
		GridPane.setHgrow(descriptionLabel, Priority.NEVER);
		GridPane.setHgrow(geometryLabel,    Priority.NEVER);
		GridPane.setHgrow(pointLabel,       Priority.NEVER);
		GridPane.setHgrow(this.latexLabel,  Priority.ALWAYS);

		GridPane.setHgrow(this.descriptionTextField,       Priority.ALWAYS);
		GridPane.setHgrow(this.geometricPrimitiveComboBox, Priority.ALWAYS);
		GridPane.setHgrow(this.featurePointComboBox,       Priority.ALWAYS);
		
		// https://stackoverflow.com/questions/50479384/gridpane-with-gaps-inside-scrollpane-rendering-wrong
		Insets insetsLeft   = new Insets(5, 7, 5, 5);
		Insets insetsRight  = new Insets(5, 0, 5, 7);

		GridPane.setMargin(equationLabel,    insetsLeft);
		GridPane.setMargin(descriptionLabel, insetsLeft);
		GridPane.setMargin(geometryLabel,    insetsLeft);
		GridPane.setMargin(pointLabel,       insetsLeft);

		GridPane.setMargin(this.latexLabel,                 insetsRight);
		GridPane.setMargin(this.geometricPrimitiveComboBox, insetsRight);
		GridPane.setMargin(this.featurePointComboBox,       insetsRight);
		GridPane.setMargin(this.descriptionTextField,       insetsRight);
		
		int row = 0;
		
		gridPane.add(equationLabel,                   0, row); // column, row, columnspan, rowspan,
		gridPane.add(this.latexLabel,                 1, row++);
		
		gridPane.add(descriptionLabel,                0, row);
		gridPane.add(this.descriptionTextField,       1, row++);
		
		gridPane.add(geometryLabel,                   0, row);
		gridPane.add(this.geometricPrimitiveComboBox, 1, row++);
		
		gridPane.add(pointLabel,                      0, row);
		gridPane.add(this.featurePointComboBox,       1, row++);

		return gridPane;
	}
}
