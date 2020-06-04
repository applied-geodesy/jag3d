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

package org.applied_geodesy.jag3d.ui.dialog;

import java.util.Optional;

import org.applied_geodesy.adjustment.Constant;
import org.applied_geodesy.adjustment.network.observation.reduction.ProjectionType;
import org.applied_geodesy.adjustment.network.observation.reduction.Reduction;
import org.applied_geodesy.adjustment.network.observation.reduction.ReductionTaskType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.ui.textfield.DoubleTextField.ValueSupport;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.jag3d.ui.i18n.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class ProjectionAndReductionDialog {
	
	private class ProjectionSelectionChangeListener implements ChangeListener<Boolean> {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			boolean isProjection = utmProjectionRadioButton.isSelected() || gaussKruegerProjectionRadioButton.isSelected();
			
			distanceReductionCheckBox.setDisable(!isProjection);
			directionReductionCheckBox.setDisable(!isProjection);
		}
	}
	
	private I18N i18n = I18N.getInstance();
	private static ProjectionAndReductionDialog projectionAndReductionDialog = new ProjectionAndReductionDialog();
	private Dialog<Reduction> dialog = null;
	private Window window;
	private DoubleTextField referenceHeightTextField;
	private DoubleTextField earthRadiusTextField;
	private CheckBox directionReductionCheckBox;
	private CheckBox heightReductionCheckBox;
	private CheckBox distanceReductionCheckBox;
	private CheckBox earthCurvatureReductionCheckBox;
	private RadioButton noneProjectionRadioButton;
	private RadioButton gaussKruegerProjectionRadioButton;
	private RadioButton utmProjectionRadioButton;
	
	// For validating given earth radius cf. https://de.wikipedia.org/wiki/Erdradius#Radien_einiger_wichtiger_Erdellipsoide
	private final static double EQUATORIAL_RADIUS = 6385000.0;
	private final static double POLAR_RADIUS      = 6300000.0;
	
	private ProjectionAndReductionDialog() {}

	public static void setOwner(Window owner) {
		projectionAndReductionDialog.window = owner;
	}

	public static Optional<Reduction> showAndWait() {
		projectionAndReductionDialog.init();
		projectionAndReductionDialog.load();
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					projectionAndReductionDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) projectionAndReductionDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return projectionAndReductionDialog.dialog.showAndWait();
	}
	
	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<Reduction>();

		this.dialog.setTitle(i18n.getString("ProjectionAndReductionDialog.title", "Projection and reductions"));
		this.dialog.setHeaderText(i18n.getString("ProjectionAndReductionDialog.header", "Projection and reduction properties"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		//		this.dialog.initStyle(StageStyle.UTILITY);
		this.dialog.initOwner(window);

		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);

		this.dialog.setResultConverter(new Callback<ButtonType, Reduction>() {
			@Override
			public Reduction call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					Reduction reductions = new Reduction();
					
					double earthRadius = earthRadiusTextField.getNumber();
					double referenceHeight = referenceHeightTextField.getNumber();
					
					ProjectionType projectionType = ProjectionType.NONE;
					if (utmProjectionRadioButton.isSelected())
						projectionType = ProjectionType.UTM;
					else if (gaussKruegerProjectionRadioButton.isSelected())
						projectionType = ProjectionType.GAUSS_KRUEGER;
					
					// validate Earth radius
					if (earthRadius > EQUATORIAL_RADIUS || earthRadius < POLAR_RADIUS)
						earthRadius = Constant.EARTH_RADIUS;
					
					reductions.setReferenceHeight(referenceHeight);
					reductions.setEarthRadius(earthRadius);
					reductions.setProjectionType(projectionType);
					
					if (directionReductionCheckBox.isSelected()) 
						reductions.addReductionTaskType(ReductionTaskType.DIRECTION);
					if (distanceReductionCheckBox.isSelected()) 
						reductions.addReductionTaskType(ReductionTaskType.DISTANCE);
					if (heightReductionCheckBox.isSelected()) 
						reductions.addReductionTaskType(ReductionTaskType.HEIGHT);
					if (earthCurvatureReductionCheckBox.isSelected()) 
						reductions.addReductionTaskType(ReductionTaskType.EARTH_CURVATURE);

					save(reductions);
					
					return reductions;

				}
				return null;
			}
		});
	}
	
	private Node createPane() {
		VBox box = this.createVbox();
		box.getChildren().addAll(
				this.createProjectionPane(),
				this.createReductionOptionsPane()
		);
		
		return box;
	}
	
	private TitledPane createProjectionPane() {
		String title   = i18n.getString("ProjectionAndReductionDialog.projection.title", "Map projection");
		String tooltip = i18n.getString("ProjectionAndReductionDialog.projection.tooltip", "Map projection properties");
		
		String labelProjNone   = i18n.getString("ProjectionAndReductionDialog.projection.none.label", "None");
		String tooltipProjNone = i18n.getString("ProjectionAndReductionDialog.projection.none.tooltip", "If checked, no specific map projection will be applied");

		String labelProjGK   = i18n.getString("ProjectionAndReductionDialog.projection.gk.label", "Gau\u00DF-Kr\u00FCger");
		String tooltipProjGK = i18n.getString("ProjectionAndReductionDialog.projection.gk.tooltip", "If checked, Gau\u00DF-Kr\u00FCger projection will be applied");

		String labelProjUTM   = i18n.getString("ProjectionAndReductionDialog.projection.utm.label", "Universale Transverse Mercator");
		String tooltipProjUTM = i18n.getString("ProjectionAndReductionDialog.projection.utm.tooltip", "If checked, UTM projection will be applied");

		ProjectionSelectionChangeListener projectionSelectionChangeListener = new ProjectionSelectionChangeListener();
		this.noneProjectionRadioButton = this.createRadioButton(labelProjNone, tooltipProjNone);
		this.gaussKruegerProjectionRadioButton = this.createRadioButton(labelProjGK, tooltipProjGK);
		this.utmProjectionRadioButton = this.createRadioButton(labelProjUTM, tooltipProjUTM);
		this.noneProjectionRadioButton.selectedProperty().addListener(projectionSelectionChangeListener);
		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(this.noneProjectionRadioButton, this.utmProjectionRadioButton, this.gaussKruegerProjectionRadioButton);
		
		this.earthRadiusTextField = new DoubleTextField(Constant.EARTH_RADIUS, CellValueType.LENGTH, true, ValueSupport.NON_NULL_VALUE_SUPPORT);
		this.earthRadiusTextField.setTooltip(new Tooltip(i18n.getString("ProjectionAndReductionDialog.projection.earthradius.tooltip", "Local radius of the Earth")));
		this.earthRadiusTextField.setMinWidth(100);
		this.earthRadiusTextField.setPrefWidth(150);
		this.earthRadiusTextField.setMaxWidth(Double.MAX_VALUE);
		
		this.referenceHeightTextField = new DoubleTextField(0.0, CellValueType.LENGTH, true, ValueSupport.NON_NULL_VALUE_SUPPORT);
		this.referenceHeightTextField.setTooltip(new Tooltip(i18n.getString("ProjectionAndReductionDialog.projection.height.tooltip", "Height w.r.t. survey datum")));
		this.referenceHeightTextField.setMinWidth(100);
		this.referenceHeightTextField.setPrefWidth(150);
		this.referenceHeightTextField.setMaxWidth(Double.MAX_VALUE);
		
		Label earthRadiusLabel = new Label(i18n.getString("ProjectionAndReductionDialog.projection.earthradius.label", "Earth radius:"));
		earthRadiusLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		earthRadiusLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		earthRadiusLabel.setPadding(new Insets(0,0,0,3));
		earthRadiusLabel.setLabelFor(this.earthRadiusTextField);
		
		Label referenceHeightLabel = new Label(i18n.getString("ProjectionAndReductionDialog.projection.referenceheight.label", "Reference height:"));
		referenceHeightLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		referenceHeightLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		referenceHeightLabel.setPadding(new Insets(0,0,0,3));
		referenceHeightLabel.setLabelFor(this.referenceHeightTextField);

		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(20);
		gridPane.setVgap(7);
		gridPane.setPadding(new Insets(5, 10, 5, 10)); // oben, recht, unten, links

		GridPane.setHgrow(this.noneProjectionRadioButton,         Priority.ALWAYS);
		GridPane.setHgrow(this.gaussKruegerProjectionRadioButton, Priority.ALWAYS);
		GridPane.setHgrow(this.utmProjectionRadioButton,          Priority.ALWAYS);
		GridPane.setHgrow(earthRadiusLabel,                       Priority.SOMETIMES);	
		GridPane.setHgrow(this.earthRadiusTextField,              Priority.ALWAYS);	
		GridPane.setHgrow(referenceHeightLabel,                   Priority.SOMETIMES);	
		GridPane.setHgrow(this.referenceHeightTextField,          Priority.ALWAYS);	
		
		int row = 0;
		gridPane.add(this.noneProjectionRadioButton,          0, ++row, 2, 1);
		gridPane.add(this.gaussKruegerProjectionRadioButton,  0, ++row, 2, 1);
		gridPane.add(this.utmProjectionRadioButton,           0, ++row, 2, 1);
		gridPane.add(earthRadiusLabel,                        0, ++row, 1, 1);
		gridPane.add(this.earthRadiusTextField,               1,   row, 1, 1);
		gridPane.add(referenceHeightLabel,                    0, ++row, 1, 1);
		gridPane.add(this.referenceHeightTextField,           1,   row, 1, 1);
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				noneProjectionRadioButton.requestFocus();
			}
		});

		return this.createTitledPane(title, tooltip, gridPane);
	}
	
	private TitledPane createReductionOptionsPane() {
		String title   = i18n.getString("ProjectionAndReductionDialog.reduction.title", "Reduction options");
		String tooltip = i18n.getString("ProjectionAndReductionDialog.reduction.tooltip", "Observation reductions");
		
		String distanceLabel   = i18n.getString("ProjectionAndReductionDialog.reduction.distance.label", "Horizontal distance reduction");
		String distanceTooltip = i18n.getString("ProjectionAndReductionDialog.reduction.distance.tooltip", "If checked, horizontal distance reduction will be applied during network adjustment");
		
		String directionLabel   = i18n.getString("ProjectionAndReductionDialog.reduction.direction.label", "Direction reduction");
		String directionTooltip = i18n.getString("ProjectionAndReductionDialog.reduction.direction.tooltip", "If checked, direction reduction will be applied during network adjustment");
		
		String earthCurvatureLabel   = i18n.getString("ProjectionAndReductionDialog.reduction.earthcurvature.label", "Earth's curvature reduction");
		String earthCurvatureTooltip = i18n.getString("ProjectionAndReductionDialog.reduction.earthcurvature.tooltip", "If checked, Earth's curvature reduction will be applied to horizontal distances and zenith angles during network adjustment");
		
		String heightLabel   = i18n.getString("ProjectionAndReductionDialog.reduction.height.label", "Height reduction");
		String heightTooltip = i18n.getString("ProjectionAndReductionDialog.reduction.height.tooltip", "If checked, height reduction will be applied to horizontal distances during network adjustment");

		this.distanceReductionCheckBox       = this.createCheckBox(distanceLabel, distanceTooltip);
		this.directionReductionCheckBox      = this.createCheckBox(directionLabel, directionTooltip);
		this.heightReductionCheckBox         = this.createCheckBox(heightLabel, heightTooltip);
		this.earthCurvatureReductionCheckBox = this.createCheckBox(earthCurvatureLabel, earthCurvatureTooltip);
		
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(20);
		gridPane.setVgap(7);
		gridPane.setPadding(new Insets(5, 10, 5, 10)); // oben, recht, unten, links

		GridPane.setHgrow(this.distanceReductionCheckBox,       Priority.ALWAYS);
		GridPane.setHgrow(this.heightReductionCheckBox,         Priority.ALWAYS);
		GridPane.setHgrow(this.directionReductionCheckBox,      Priority.ALWAYS);
		GridPane.setHgrow(this.earthCurvatureReductionCheckBox, Priority.ALWAYS);

		int row = 0;
		gridPane.add(this.distanceReductionCheckBox,       0, ++row, 1, 1);
		gridPane.add(this.directionReductionCheckBox,      0, ++row, 1, 1);
		gridPane.add(this.heightReductionCheckBox,         0, ++row, 1, 1);
		gridPane.add(this.earthCurvatureReductionCheckBox, 0, ++row, 1, 1);

		return this.createTitledPane(title, tooltip, gridPane);
	}
	
	private void load() {
		try {
			Reduction reductions = SQLManager.getInstance().getReductionDefinition();
			this.referenceHeightTextField.setNumber(reductions.getReferenceHeight());
			this.earthRadiusTextField.setNumber(reductions.getEarthRadius());
			
			this.directionReductionCheckBox.setSelected(reductions.applyReductionTask(ReductionTaskType.DIRECTION));
			this.distanceReductionCheckBox.setSelected(reductions.applyReductionTask(ReductionTaskType.DISTANCE));
			this.heightReductionCheckBox.setSelected(reductions.applyReductionTask(ReductionTaskType.HEIGHT));
			this.earthCurvatureReductionCheckBox.setSelected(reductions.applyReductionTask(ReductionTaskType.EARTH_CURVATURE));
			
			ProjectionType projection = reductions.getProjectionType();
			this.noneProjectionRadioButton.setSelected(true);
			this.gaussKruegerProjectionRadioButton.setSelected(projection == ProjectionType.GAUSS_KRUEGER);
			this.utmProjectionRadioButton.setSelected(projection == ProjectionType.UTM);
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("ProjectionAndReductionDialog.message.error.load.exception.title", "Unexpected SQL-Error"),
							i18n.getString("ProjectionAndReductionDialog.message.error.load.exception.header", "Error, could not load projection and reduction properties from database."),
							i18n.getString("ProjectionAndReductionDialog.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}

	private void save(Reduction reductions) {
		try {
			SQLManager.getInstance().save(reductions);
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("ProjectionAndReductionDialog.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("ProjectionAndReductionDialog.message.error.save.exception.header", "Error, could not save projection and reduction properties to database"),
							i18n.getString("ProjectionAndReductionDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
	
	private RadioButton createRadioButton(String title, String tooltip) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,3));
		RadioButton radioButton = new RadioButton();
		radioButton.setGraphic(label);
		radioButton.setTooltip(new Tooltip(tooltip));
		radioButton.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		radioButton.setMaxHeight(Double.MAX_VALUE);
		return radioButton;
	}
	
	private CheckBox createCheckBox(String title, String tooltip) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,3));
		CheckBox checkBox = new CheckBox();
		checkBox.setGraphic(label);
		checkBox.setTooltip(new Tooltip(tooltip));
		checkBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		checkBox.setMaxWidth(Double.MAX_VALUE);
		return checkBox;
	}
	
	private TitledPane createTitledPane(String title, String tooltip, Node content) {
		TitledPane titledPane = new TitledPane();
		titledPane.setCollapsible(false);
		titledPane.setAnimated(false);
		titledPane.setContent(content);
		titledPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		titledPane.setPadding(new Insets(0, 10, 5, 10)); // oben, links, unten, rechts
		//titledPane.setText(title);
		Label label = new Label(title);
		label.setTooltip(new Tooltip(tooltip));
		titledPane.setGraphic(label);
		return titledPane;
	}
	
	private VBox createVbox() {
		VBox vBox = new VBox();
		vBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		vBox.setPadding(new Insets(5, 10, 5, 10)); // oben, recht, unten, links
		vBox.setSpacing(10);
		return vBox;
	}
}
