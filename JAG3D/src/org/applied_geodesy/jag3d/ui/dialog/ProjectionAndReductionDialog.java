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
import org.applied_geodesy.transformation.datum.Ellipsoid;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class ProjectionAndReductionDialog {

	private enum EllipsoidType {
		SPHERE, GRS80, WGS84, BESSEL_1941, KRASSOWSKI, HAYFORD;
	}
	
	private class EllipsoidMenuEventHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() instanceof MenuItem) {
				MenuItem menuItem = (MenuItem)event.getSource();
				if (menuItem.getUserData() instanceof EllipsoidType) {
					EllipsoidType ellipsoidType = (EllipsoidType)menuItem.getUserData();
					Ellipsoid ellipsoid = Ellipsoid.SPHERE;

					switch (ellipsoidType) {
					case BESSEL_1941:
						ellipsoid = Ellipsoid.BESSEL1941;
						break;
					case GRS80:
						ellipsoid = Ellipsoid.GRS80;
						break;
					case HAYFORD:
						ellipsoid = Ellipsoid.HAYFORD;
						break;
					case KRASSOWSKI:
						ellipsoid = Ellipsoid.KRASSOWSKI;
						break;
					case WGS84:
						ellipsoid = Ellipsoid.WGS84;
						break;
					//case SPHERE:
					default:
						ellipsoid = Ellipsoid.SPHERE;
						break;
					}
					majorAxisTextField.setValue(ellipsoid.getMajorAxis());
					minorAxisTextField.setValue(ellipsoid.getMinorAxis());
				}
			}
		}
	}
	
	private class ProjectionSelectionChangeListener implements ChangeListener<Toggle> {
		@Override
		public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
			distanceReductionCheckBox.setDisable(ellipsoidalProjectionRadioButton.isSelected() || localCartesianProjectionRadioButton.isSelected());
			directionReductionCheckBox.setDisable(ellipsoidalProjectionRadioButton.isSelected() || localCartesianProjectionRadioButton.isSelected());
			heightReductionCheckBox.setDisable(ellipsoidalProjectionRadioButton.isSelected());
			earthCurvatureReductionCheckBox.setDisable(ellipsoidalProjectionRadioButton.isSelected());
			
			principalPointX0TextField.setDisable(!ellipsoidalProjectionRadioButton.isSelected());
			principalPointY0TextField.setDisable(!ellipsoidalProjectionRadioButton.isSelected());
			principalPointZ0TextField.setDisable(!ellipsoidalProjectionRadioButton.isSelected());
			referenceLongitudeTextField.setDisable(!ellipsoidalProjectionRadioButton.isSelected());
			
			if (utmProjectionRadioButton.isSelected() || gaussKruegerProjectionRadioButton.isSelected()) {
				distanceReductionCheckBox.setDisable(Boolean.FALSE);
				directionReductionCheckBox.setDisable(Boolean.FALSE);
			}
		} 
	}
	
	private I18N i18n = I18N.getInstance();
	private static ProjectionAndReductionDialog projectionAndReductionDialog = new ProjectionAndReductionDialog();
	private Dialog<Reduction> dialog = null;
	private Window window;
	private DoubleTextField referenceHeightTextField, referenceLatitudeTextField, referenceLongitudeTextField;
	private DoubleTextField principalPointX0TextField, principalPointY0TextField, principalPointZ0TextField;
	private DoubleTextField majorAxisTextField, minorAxisTextField;
	private CheckBox directionReductionCheckBox;
	private CheckBox heightReductionCheckBox;
	private CheckBox distanceReductionCheckBox;
	private CheckBox earthCurvatureReductionCheckBox;
	private RadioButton localCartesianProjectionRadioButton;
	private RadioButton gaussKruegerProjectionRadioButton;
	private RadioButton utmProjectionRadioButton;
	private RadioButton ellipsoidalProjectionRadioButton;
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
					
					double majorAxis = majorAxisTextField.getNumber();
					double minorAxis = minorAxisTextField.getNumber();
					double referenceLatitude  = referenceLatitudeTextField.getNumber();
					double referenceLongitude = referenceLongitudeTextField.getNumber();
					double referenceHeight    = referenceHeightTextField.getNumber();
					double x0 = principalPointX0TextField.getNumber();
					double y0 = principalPointY0TextField.getNumber();
					double z0 = principalPointZ0TextField.getNumber();
					
					ProjectionType projectionType = ProjectionType.LOCAL_CARTESIAN;
					if (utmProjectionRadioButton.isSelected())
						projectionType = ProjectionType.UTM;
					else if (gaussKruegerProjectionRadioButton.isSelected())
						projectionType = ProjectionType.GAUSS_KRUEGER;
					else if (ellipsoidalProjectionRadioButton.isSelected())
						projectionType = ProjectionType.LOCAL_ELLIPSOIDAL;
					
					reductions.setEllipsoid(Ellipsoid.createEllipsoidFromMinorAxis(Math.max(majorAxis, minorAxis), Math.min(majorAxis, minorAxis)));
					reductions.getPrincipalPoint().setCoordinates(x0, y0, z0, referenceLatitude, referenceLongitude, referenceHeight);
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
		VBox propertiesBox = this.createVbox();
		propertiesBox.getChildren().addAll(
				this.createProjectionPane(),
				this.createReductionOptionsPane()
		);

		VBox parameterBox = this.createVbox();
		parameterBox.getChildren().addAll(
				this.createParameterPane()
		);
		
		HBox.setHgrow(propertiesBox, Priority.ALWAYS);
		HBox.setHgrow(parameterBox, Priority.ALWAYS);
		
		HBox hbox = this.createHbox();
		hbox.getChildren().addAll(
				propertiesBox,
				parameterBox
		);		
		return hbox;
	}
	
	private TitledPane createProjectionPane() {
		String title   = i18n.getString("ProjectionAndReductionDialog.properties.title", "Properties");
		String tooltip = i18n.getString("ProjectionAndReductionDialog.properties.tooltip", "Projection properties");
		
		String labelProjLocalCartesian   = i18n.getString("ProjectionAndReductionDialog.properties.local_cartesian.label", "Local Cartesian system");
		String tooltipProjLocalCartesian = i18n.getString("ProjectionAndReductionDialog.properties.local_cartesian.tooltip", "If selected, a local Cartesian system will be used");

		String labelProjGK   = i18n.getString("ProjectionAndReductionDialog.properties.gauss_krueger.label", "Gau\u00DF-Kr\u00FCger");
		String tooltipProjGK = i18n.getString("ProjectionAndReductionDialog.properties.gauss_krueger.tooltip", "If selected, Gau\u00DF-Kr\u00FCger projection will be applied");

		String labelProjUTM   = i18n.getString("ProjectionAndReductionDialog.properties.utm.label", "Universale Transverse Mercator");
		String tooltipProjUTM = i18n.getString("ProjectionAndReductionDialog.properties.utm.tooltip", "If selected, UTM projection will be applied");
		
		String labelProjEllipsoidal   = i18n.getString("ProjectionAndReductionDialog.properties.ellipsoidal.label", "Local ellipsoidal system");
		String tooltipProjEllipsoidal = i18n.getString("ProjectionAndReductionDialog.properties.ellipsoidal.tooltip", "If selected, a local ellipsoidal Earth model will be used");

		ProjectionSelectionChangeListener projectionSelectionChangeListener = new ProjectionSelectionChangeListener();
		this.localCartesianProjectionRadioButton = this.createRadioButton(labelProjLocalCartesian, tooltipProjLocalCartesian);
		this.gaussKruegerProjectionRadioButton   = this.createRadioButton(labelProjGK, tooltipProjGK);
		this.utmProjectionRadioButton            = this.createRadioButton(labelProjUTM, tooltipProjUTM);
		this.ellipsoidalProjectionRadioButton    = this.createRadioButton(labelProjEllipsoidal, tooltipProjEllipsoidal);

		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(this.localCartesianProjectionRadioButton, this.utmProjectionRadioButton, this.gaussKruegerProjectionRadioButton, this.ellipsoidalProjectionRadioButton);
		group.selectedToggleProperty().addListener(projectionSelectionChangeListener);
		
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(20);
		gridPane.setVgap(7);
		gridPane.setPadding(new Insets(5, 10, 5, 10)); // oben, recht, unten, links

		GridPane.setHgrow(this.localCartesianProjectionRadioButton, Priority.ALWAYS);
		GridPane.setHgrow(this.gaussKruegerProjectionRadioButton,   Priority.ALWAYS);
		GridPane.setHgrow(this.utmProjectionRadioButton,            Priority.ALWAYS);
		GridPane.setHgrow(this.ellipsoidalProjectionRadioButton,    Priority.ALWAYS);
		
		int row = 0;
		gridPane.add(this.localCartesianProjectionRadioButton, 0, ++row, 1, 1);
		gridPane.add(this.ellipsoidalProjectionRadioButton,    0, ++row, 1, 1);
		gridPane.add(this.gaussKruegerProjectionRadioButton,   0, ++row, 1, 1);
		gridPane.add(this.utmProjectionRadioButton,            0, ++row, 1, 1);
		
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				localCartesianProjectionRadioButton.requestFocus();
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
		
		String earthCurvatureLabel   = i18n.getString("ProjectionAndReductionDialog.reduction.earth_curvature.label", "Earth's curvature reduction");
		String earthCurvatureTooltip = i18n.getString("ProjectionAndReductionDialog.reduction.earth_curvature.tooltip", "If checked, Earth's curvature reduction will be applied to horizontal distances and zenith angles during network adjustment");
		
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
	
	private TitledPane createParameterPane() {
		String title   = i18n.getString("ProjectionAndReductionDialog.parameter.title", "Parameter");
		String tooltip = i18n.getString("ProjectionAndReductionDialog.parameter.tooltip", "Parameters of projection model");
		
		this.majorAxisTextField = new DoubleTextField(Constant.EARTH_RADIUS, CellValueType.LENGTH, true, ValueSupport.INCLUDING_INCLUDING_INTERVAL, POLAR_RADIUS, EQUATORIAL_RADIUS);
		this.majorAxisTextField.setTooltip(new Tooltip(i18n.getString("ProjectionAndReductionDialog.parameter.ellipsoid.major_axis.tooltip", "Major axis length of reference ellipsoid")));
		this.majorAxisTextField.setMinWidth(100);
		this.majorAxisTextField.setPrefWidth(150);
		this.majorAxisTextField.setMaxWidth(Double.MAX_VALUE);
		
		this.minorAxisTextField = new DoubleTextField(Constant.EARTH_RADIUS, CellValueType.LENGTH, true, ValueSupport.INCLUDING_INCLUDING_INTERVAL, POLAR_RADIUS, EQUATORIAL_RADIUS);
		this.minorAxisTextField.setTooltip(new Tooltip(i18n.getString("ProjectionAndReductionDialog.parameter.ellipsoid.minor_axis.tooltip", "Minor axis length of reference ellipsoid")));
		this.minorAxisTextField.setMinWidth(100);
		this.minorAxisTextField.setPrefWidth(150);
		this.minorAxisTextField.setMaxWidth(Double.MAX_VALUE);
		
		
		this.referenceLatitudeTextField = new DoubleTextField(0.0, CellValueType.ANGLE, true, ValueSupport.INCLUDING_INCLUDING_INTERVAL, -0.5 * Math.PI - Constant.EPS, 0.5 * Math.PI + Constant.EPS);
		this.referenceLatitudeTextField.setTooltip(new Tooltip(i18n.getString("ProjectionAndReductionDialog.parameter.principal_point.latitude.tooltip", "Latitude of principal point")));
		this.referenceLatitudeTextField.setMinWidth(100);
		this.referenceLatitudeTextField.setPrefWidth(150);
		this.referenceLatitudeTextField.setMaxWidth(Double.MAX_VALUE);
		
		this.referenceLongitudeTextField = new DoubleTextField(0.0, CellValueType.ANGLE, true, ValueSupport.INCLUDING_INCLUDING_INTERVAL, -Math.PI - Constant.EPS, Math.PI + Constant.EPS);
		this.referenceLongitudeTextField.setTooltip(new Tooltip(i18n.getString("ProjectionAndReductionDialog.parameter.principal_point.longitude.tooltip", "Longitude of principal point")));
		this.referenceLongitudeTextField.setMinWidth(100);
		this.referenceLongitudeTextField.setPrefWidth(150);
		this.referenceLongitudeTextField.setMaxWidth(Double.MAX_VALUE);

		this.referenceHeightTextField = new DoubleTextField(0.0, CellValueType.LENGTH, true, ValueSupport.NON_NULL_VALUE_SUPPORT);
		this.referenceHeightTextField.setTooltip(new Tooltip(i18n.getString("ProjectionAndReductionDialog.parameter.principal_point.height.tooltip", "Height w.r.t. survey datum")));
		this.referenceHeightTextField.setMinWidth(100);
		this.referenceHeightTextField.setPrefWidth(150);
		this.referenceHeightTextField.setMaxWidth(Double.MAX_VALUE);
		
		
		this.principalPointX0TextField = new DoubleTextField(0.0, CellValueType.LENGTH, true, ValueSupport.NON_NULL_VALUE_SUPPORT);
		this.principalPointX0TextField.setTooltip(new Tooltip(i18n.getString("ProjectionAndReductionDialog.parameter.principal_point.x0.tooltip", "x-component of (local) principal point")));
		this.principalPointX0TextField.setMinWidth(30);
		this.principalPointX0TextField.setPrefWidth(50);
		this.principalPointX0TextField.setMaxWidth(Double.MAX_VALUE);
		
		this.principalPointY0TextField = new DoubleTextField(0.0, CellValueType.LENGTH, true, ValueSupport.NON_NULL_VALUE_SUPPORT);
		this.principalPointY0TextField.setTooltip(new Tooltip(i18n.getString("ProjectionAndReductionDialog.parameter.principal_point.y0.tooltip", "y-component of (local) principal point")));
		this.principalPointY0TextField.setMinWidth(30);
		this.principalPointY0TextField.setPrefWidth(50);
		this.principalPointY0TextField.setMaxWidth(Double.MAX_VALUE);
		
		this.principalPointZ0TextField = new DoubleTextField(0.0, CellValueType.LENGTH, true, ValueSupport.NON_NULL_VALUE_SUPPORT);
		this.principalPointZ0TextField.setTooltip(new Tooltip(i18n.getString("ProjectionAndReductionDialog.parameter.principal_point.z0.tooltip", "z-component of (local) principal point")));
		this.principalPointZ0TextField.setMinWidth(30);
		this.principalPointZ0TextField.setPrefWidth(50);
		this.principalPointZ0TextField.setMaxWidth(Double.MAX_VALUE);
		
		
		Label majorAxisLabel = new Label(i18n.getString("ProjectionAndReductionDialog.parameter.ellipsoid.major_axis.label", "Major axis a:"));
		majorAxisLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		majorAxisLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		majorAxisLabel.setPadding(new Insets(0,0,0,3));
		majorAxisLabel.setLabelFor(this.majorAxisTextField);
		
		Label minorAxisLabel = new Label(i18n.getString("ProjectionAndReductionDialog.parameter.ellipsoid.minor_axis.label", "Minor axis b:"));
		minorAxisLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		minorAxisLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		minorAxisLabel.setPadding(new Insets(0,0,0,3));
		minorAxisLabel.setLabelFor(this.minorAxisTextField);
		
		Label referenceLatitudeLabel = new Label(i18n.getString("ProjectionAndReductionDialog.parameter.principal_point.latitude.label", "Reference latitude \u03C60:"));
		referenceLatitudeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		referenceLatitudeLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		referenceLatitudeLabel.setPadding(new Insets(0,0,0,3));
		referenceLatitudeLabel.setLabelFor(this.referenceLatitudeTextField);
		
		Label referenceLongitudeLabel = new Label(i18n.getString("ProjectionAndReductionDialog.parameter.principal_point.longitude.label", "Reference longitude \u03BB0:"));
		referenceLongitudeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		referenceLongitudeLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		referenceLongitudeLabel.setPadding(new Insets(0,0,0,3));
		referenceLongitudeLabel.setLabelFor(this.referenceLongitudeTextField);
		
		Label referenceHeightLabel = new Label(i18n.getString("ProjectionAndReductionDialog.parameter.principal_point.height.label", "Reference height h0:"));
		referenceHeightLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		referenceHeightLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		referenceHeightLabel.setPadding(new Insets(0,0,0,3));
		referenceHeightLabel.setLabelFor(this.referenceHeightTextField);
		
		Label principalPointPointX0Label = new Label(i18n.getString("ProjectionAndReductionDialog.parameter.principal_point.x0.label", "Principal point x0:"));
		principalPointPointX0Label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		principalPointPointX0Label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		principalPointPointX0Label.setPadding(new Insets(0,0,0,3));
		principalPointPointX0Label.setLabelFor(this.principalPointX0TextField);
		
		Label principalPointPointY0Label = new Label(i18n.getString("ProjectionAndReductionDialog.parameter.principal_point.y0.label", "Principal point y0:"));
		principalPointPointY0Label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		principalPointPointY0Label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		principalPointPointY0Label.setPadding(new Insets(0,0,0,3));
		principalPointPointY0Label.setLabelFor(this.principalPointY0TextField);
		
		Label principalPointPointZ0Label = new Label(i18n.getString("ProjectionAndReductionDialog.parameter.principal_point.z0.label", "Principal point z0:"));
		principalPointPointZ0Label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		principalPointPointZ0Label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		principalPointPointZ0Label.setPadding(new Insets(0,0,0,3));
		principalPointPointZ0Label.setLabelFor(this.principalPointZ0TextField);

		
		HBox ellipsoidHbox = new HBox();
		ellipsoidHbox.setSpacing(0);
		ellipsoidHbox.getChildren().addAll(this.majorAxisTextField, getEllipsoidMenuBar());
		
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(20);
		gridPane.setVgap(7);
		gridPane.setPadding(new Insets(5, 10, 5, 10)); // oben, recht, unten, links

		GridPane.setHgrow(majorAxisLabel,                   Priority.SOMETIMES);	
		GridPane.setHgrow(this.majorAxisTextField,          Priority.ALWAYS);
		GridPane.setHgrow(minorAxisLabel,                   Priority.SOMETIMES);	
		GridPane.setHgrow(this.minorAxisTextField,          Priority.ALWAYS);	
		GridPane.setHgrow(referenceLatitudeLabel,           Priority.SOMETIMES);	
		GridPane.setHgrow(this.referenceLatitudeTextField,  Priority.ALWAYS);	
		GridPane.setHgrow(referenceLongitudeLabel,          Priority.SOMETIMES);	
		GridPane.setHgrow(this.referenceLongitudeTextField, Priority.ALWAYS);	
		GridPane.setHgrow(referenceHeightLabel,             Priority.SOMETIMES);	
		GridPane.setHgrow(this.referenceHeightTextField,    Priority.ALWAYS);	
		GridPane.setHgrow(principalPointPointY0Label,       Priority.SOMETIMES);	
		GridPane.setHgrow(this.principalPointY0TextField,   Priority.ALWAYS);	
		GridPane.setHgrow(principalPointPointX0Label,       Priority.SOMETIMES);	
		GridPane.setHgrow(this.principalPointX0TextField,   Priority.ALWAYS);	
		GridPane.setHgrow(principalPointPointZ0Label,       Priority.SOMETIMES);	
		GridPane.setHgrow(this.principalPointZ0TextField,   Priority.ALWAYS);	
		
		int row = 0;
		gridPane.add(majorAxisLabel,                   0, ++row, 1, 1);
		//gridPane.add(this.majorAxisTextField,          1,   row, 1, 1);
		gridPane.add(ellipsoidHbox,                    1,   row, 1, 1);
		gridPane.add(minorAxisLabel,                   0, ++row, 1, 1);
		gridPane.add(this.minorAxisTextField,          1,   row, 1, 1);
		gridPane.add(principalPointPointY0Label,       0, ++row, 1, 1);
		gridPane.add(this.principalPointY0TextField,   1,   row, 1, 1);
		gridPane.add(principalPointPointX0Label,       0, ++row, 1, 1);
		gridPane.add(this.principalPointX0TextField,   1,   row, 1, 1);
		gridPane.add(principalPointPointZ0Label,       0, ++row, 1, 1);
		gridPane.add(this.principalPointZ0TextField,   1,   row, 1, 1);
		gridPane.add(referenceLatitudeLabel,           0, ++row, 1, 1);
		gridPane.add(this.referenceLatitudeTextField,  1,   row, 1, 1);
		gridPane.add(referenceLongitudeLabel ,         0, ++row, 1, 1);
		gridPane.add(this.referenceLongitudeTextField, 1,   row, 1, 1);
		gridPane.add(referenceHeightLabel,             0, ++row, 1, 1);
		gridPane.add(this.referenceHeightTextField,    1,   row, 1, 1);

		return this.createTitledPane(title, tooltip, gridPane);
	}
	
	private void load() {
		try {
			Reduction reductions = new Reduction();
			SQLManager.getInstance().load(reductions);
			
			this.majorAxisTextField.setValue(reductions.getEllipsoid().getMajorAxis());
			this.minorAxisTextField.setValue(reductions.getEllipsoid().getMinorAxis());
			
			this.referenceLatitudeTextField.setValue(reductions.getPrincipalPoint().getLatitude());
			this.referenceLongitudeTextField.setValue(reductions.getPrincipalPoint().getLongitude());
			this.referenceHeightTextField.setValue(reductions.getPrincipalPoint().getHeight());

			this.principalPointX0TextField.setValue(reductions.getPrincipalPoint().getX());
			this.principalPointY0TextField.setValue(reductions.getPrincipalPoint().getY());
			this.principalPointZ0TextField.setValue(reductions.getPrincipalPoint().getZ());
			
			this.directionReductionCheckBox.setSelected(reductions.applyReductionTask(ReductionTaskType.DIRECTION));
			this.distanceReductionCheckBox.setSelected(reductions.applyReductionTask(ReductionTaskType.DISTANCE));
			this.heightReductionCheckBox.setSelected(reductions.applyReductionTask(ReductionTaskType.HEIGHT));
			this.earthCurvatureReductionCheckBox.setSelected(reductions.applyReductionTask(ReductionTaskType.EARTH_CURVATURE));
			
			ProjectionType projection = reductions.getProjectionType();
			this.localCartesianProjectionRadioButton.setSelected(true);
			this.gaussKruegerProjectionRadioButton.setSelected(projection == ProjectionType.GAUSS_KRUEGER);
			this.utmProjectionRadioButton.setSelected(projection == ProjectionType.UTM);
			this.ellipsoidalProjectionRadioButton.setSelected(projection == ProjectionType.LOCAL_ELLIPSOIDAL);
			
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
		titledPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		titledPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		titledPane.setPadding(new Insets(5, 5, 5, 5)); // oben, links, unten, rechts
		//titledPane.setText(title);
		Label label = new Label(title);
		label.setTooltip(new Tooltip(tooltip));
		titledPane.setGraphic(label);
		return titledPane;
	}
	
	private VBox createVbox() {
		VBox vBox = new VBox();
		vBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		vBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		vBox.setPadding(new Insets(5, 0, 5, 0)); // oben, recht, unten, links
		vBox.setSpacing(20);
		return vBox;
	}
	
	private HBox createHbox() {
		HBox hBox = new HBox();
		hBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		hBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		hBox.setPadding(new Insets(5, 10, 5, 10)); // oben, recht, unten, links
		hBox.setSpacing(5);
		return hBox;
	}
	
	private MenuBar getEllipsoidMenuBar() {
		EllipsoidMenuEventHandler handler = new EllipsoidMenuEventHandler();
		MenuBar menuBar = new MenuBar();
		Label menuLabel = new Label(i18n.getString("ProjectionAndReductionDialog.properties.ellipsoid.type.label", "\u25BC"));
		menuLabel.setTooltip(new Tooltip(i18n.getString("ProjectionAndReductionDialog.properties.ellipsoid.type.tooltip", "Potential reference ellipsoids")));
		menuLabel.setPadding(new Insets(0));
		
		Menu menu = new Menu();
		menu.setGraphic(menuLabel);
		menu.getItems().addAll(
				this.getMenuItem(i18n.getString("ProjectionAndReductionDialog.properties.ellipsoid.sphere.label", "Sphere"),         EllipsoidType.SPHERE,      handler),
				this.getMenuItem(i18n.getString("ProjectionAndReductionDialog.properties.ellipsoid.grs80.label", "GRS80"),           EllipsoidType.GRS80,       handler),
				this.getMenuItem(i18n.getString("ProjectionAndReductionDialog.properties.ellipsoid.wgs84.label", "WGS84"),           EllipsoidType.WGS84,       handler),
				this.getMenuItem(i18n.getString("ProjectionAndReductionDialog.properties.ellipsoid.bessel.label", "Bessel 1941"),    EllipsoidType.BESSEL_1941, handler),
				this.getMenuItem(i18n.getString("ProjectionAndReductionDialog.properties.ellipsoid.krassowski.label", "Krassowski"), EllipsoidType.KRASSOWSKI,  handler),
				this.getMenuItem(i18n.getString("ProjectionAndReductionDialog.properties.ellipsoid.hayford.label", "Hayford"),       EllipsoidType.HAYFORD,     handler)
		);

		menuBar.setPadding(new Insets(0));
		menuBar.getMenus().add(menu);
		
		return menuBar;
	}
	
	private MenuItem getMenuItem(String name, EllipsoidType ellipsoidType, EllipsoidMenuEventHandler handler) {
		MenuItem item = new MenuItem(name);
		item.setOnAction(handler);
		item.setUserData(ellipsoidType);
		return item;
	}
}
