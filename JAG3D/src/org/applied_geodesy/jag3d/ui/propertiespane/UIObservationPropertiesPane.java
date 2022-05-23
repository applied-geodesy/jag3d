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

package org.applied_geodesy.jag3d.ui.propertiespane;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.applied_geodesy.adjustment.network.Epoch;
import org.applied_geodesy.adjustment.network.ObservationGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.observation.DeltaZ;
import org.applied_geodesy.adjustment.network.observation.Direction;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaX2D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaX3D;
import org.applied_geodesy.adjustment.network.observation.GNSSBaselineDeltaZ1D;
import org.applied_geodesy.adjustment.network.observation.HorizontalDistance;
import org.applied_geodesy.adjustment.network.observation.Observation;
import org.applied_geodesy.adjustment.network.observation.SlopeDistance;
import org.applied_geodesy.adjustment.network.observation.ZenithAngle;
import org.applied_geodesy.adjustment.network.observation.group.DeltaZGroup;
import org.applied_geodesy.adjustment.network.observation.group.DirectionGroup;
import org.applied_geodesy.adjustment.network.observation.group.GNSSBaseline1DGroup;
import org.applied_geodesy.adjustment.network.observation.group.GNSSBaseline2DGroup;
import org.applied_geodesy.adjustment.network.observation.group.GNSSBaseline3DGroup;
import org.applied_geodesy.adjustment.network.observation.group.HorizontalDistanceGroup;
import org.applied_geodesy.adjustment.network.observation.group.ObservationGroup;
import org.applied_geodesy.adjustment.network.observation.group.SlopeDistanceGroup;
import org.applied_geodesy.adjustment.network.observation.group.ZenithAngleGroup;
import org.applied_geodesy.adjustment.network.point.Point;
import org.applied_geodesy.adjustment.network.point.Point3D;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.ui.textfield.UncertaintyTextField;
import org.applied_geodesy.ui.textfield.DoubleTextField.ValueSupport;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.jag3d.ui.i18n.I18N;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class UIObservationPropertiesPane {
	private class TickFormatChangedListener implements FormatterChangedListener {

		@Override
		public void formatterChanged(FormatterEvent evt) {
			updateUncertaintyChart(lineChart);
			updateTickLabels(lineChart);
		}
	}
	
	private class NumberChangeListener implements ChangeListener<Double> {
		private final DoubleTextField field;
		
		private NumberChangeListener(DoubleTextField field) {
			this.field = field;
		}
		
		@Override
		public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
			if (!ignoreValueUpdate && this.field.getUserData() != null) {
				if (this.field.getUserData() instanceof ParameterType) {
					ParameterType paramType = (ParameterType)this.field.getUserData();
					save(paramType);
				}
				else if (this.field.getUserData() instanceof ObservationGroupUncertaintyType) {
					ObservationGroupUncertaintyType uncertaintyType = (ObservationGroupUncertaintyType)this.field.getUserData();
					save(uncertaintyType);
				}
			}
		}
	}
	
	private class BooleanChangeListener implements ChangeListener<Boolean> {
		private final ButtonBase button;
		
		private BooleanChangeListener(ButtonBase button) {
			this.button = button;
		}
		
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (!ignoreValueUpdate && this.button.getUserData() != null) {
				if (this.button == referenceEpochRadioButton) {
					save();
				}
				else if (this.button.getUserData() instanceof ParameterType) {
					ParameterType paramType = (ParameterType)this.button.getUserData();
					save(paramType);
				}
			}
		}
	}
	
	private class SequentialTransitionFinishedListener implements ChangeListener<EventHandler<ActionEvent>> {
		@Override
		public void changed(ObservableValue<? extends EventHandler<ActionEvent>> observable, EventHandler<ActionEvent> oldValue, EventHandler<ActionEvent> newValue) {
			setProgressIndicatorsVisible(false);
			if (sequentialTransition != null)
				sequentialTransition.setNode(null);
		}
	}

	private I18N i18n = I18N.getInstance();
	private Node propertiesNode = null;
	private final TreeItemType type;
	
	private DoubleTextField zeroPointOffsetField;
	private CheckBox zeroPointOffsetCheckBox;
	
	private DoubleTextField scaleField;
	private CheckBox scaleCheckBox;
	
	private DoubleTextField rotationXField;
	private CheckBox rotationXCheckBox;
	
	private DoubleTextField rotationYField;
	private CheckBox rotationYCheckBox;
	
	private DoubleTextField rotationZField;
	private CheckBox rotationZCheckBox;
	
	private DoubleTextField orientationOffsetField;
	private CheckBox orientationOffsetCheckBox;

	private DoubleTextField refractionIndexField;
	private CheckBox refractionIndexCheckBox;
	
	private UncertaintyTextField zeroPointOffsetUncertaintyField;
	private UncertaintyTextField squareRootDistanceDependentUncertaintyField;
	private UncertaintyTextField distanceDependentUncertaintyField;
	
	private RadioButton referenceEpochRadioButton;
	private RadioButton controlEpochRadioButton;
	
	private Map<Object, ProgressIndicator> databaseTransactionProgressIndicators = new HashMap<Object, ProgressIndicator>(10);
	private Map<Object, Node> warningIconNodes = new HashMap<Object, Node>(10);
	private SequentialTransition sequentialTransition = new SequentialTransition();
	
	private boolean ignoreValueUpdate = false;
	private ObservationTreeItemValue selectedObservationItemValues[] = null;
	
	private LineChart<Number,Number> lineChart;
	private final double minDistanceForUncertaintyChart = 5.0;
    private final double maxDistanceForUncertaintyChart = 150.0;
	
	private FormatterOptions options = FormatterOptions.getInstance();

	UIObservationPropertiesPane(TreeItemType type) {
		switch(type) {
		case LEVELING_LEAF:
		case DIRECTION_LEAF:
		case HORIZONTAL_DISTANCE_LEAF:
		case SLOPE_DISTANCE_LEAF:
		case ZENITH_ANGLE_LEAF:
		case GNSS_1D_LEAF:
		case GNSS_2D_LEAF:
		case GNSS_3D_LEAF:
			this.type = type;
			this.init();
			break;
		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Error, unsupported item type " + type);		
		}
	}
	
	public void setTreeItemValue(ObservationTreeItemValue... selectedObservationItemValues) {
		if (this.selectedObservationItemValues != selectedObservationItemValues) {
			this.reset();
			this.selectedObservationItemValues = selectedObservationItemValues;
		}
	}

	public Node getNode() {
		return this.propertiesNode;
	}
	
	private void reset() {
		this.sequentialTransition.stop();
		this.setProgressIndicatorsVisible(false);
		this.setWarningIconsVisible(false);
		
		// set focus to panel to commit text field values and to force db transaction
		UITreeBuilder.getInstance().getTree().requestFocus();
//		if (this.propertiesNode != null)
//			this.propertiesNode.requestFocus();
		
		double offset      = 0.0;
		double scale       = 1.0;
		double orientation = 0.0;
		double refraction  = 0.0;
		double rotationY   = 0.0;
		double rotationX   = 0.0;
		double rotationZ   = 0.0;
		
		this.setZeroPointOffsetUncertainty(ObservationTreeItemValue.getDefaultUncertainty(this.type, ObservationGroupUncertaintyType.ZERO_POINT_OFFSET));
		this.setSquareRootDistanceDependentUncertainty(ObservationTreeItemValue.getDefaultUncertainty(this.type, ObservationGroupUncertaintyType.SQUARE_ROOT_DISTANCE_DEPENDENT));
		this.setDistanceDependentUncertainty(ObservationTreeItemValue.getDefaultUncertainty(this.type, ObservationGroupUncertaintyType.DISTANCE_DEPENDENT));	

		this.setReferenceEpoch(true);
		
		this.setZeroPointOffset(offset, false);
		this.setScale(scale, false);
		this.setOrientation(orientation, true);
		this.setRotationX(rotationX, false);
		this.setRotationY(rotationY, false);
		this.setRotationZ(rotationZ, false);
		this.setRefractionIndex(refraction, false);
	}
	
	public boolean setZeroPointOffsetUncertainty(Double value) {
		if (this.zeroPointOffsetUncertaintyField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.zeroPointOffsetUncertaintyField.setValue(value != null && value > 0 ? value : null);
		//this.zeroPointOffsetUncertaintyField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
		this.updateUncertaintyChart(this.lineChart);
        this.updateTickLabels(this.lineChart);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setSquareRootDistanceDependentUncertainty(Double value) {
		if (this.squareRootDistanceDependentUncertaintyField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.squareRootDistanceDependentUncertaintyField.setValue(value != null && value >= 0 ? value : null);
		this.updateUncertaintyChart(this.lineChart);
        this.updateTickLabels(this.lineChart);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setDistanceDependentUncertainty(Double value) {
		if (this.distanceDependentUncertaintyField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.distanceDependentUncertaintyField.setValue(value != null && value >= 0 ? value : null);
		this.updateUncertaintyChart(this.lineChart);
        this.updateTickLabels(this.lineChart);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setReferenceEpoch(Boolean referenceEpoch) {
		if (this.referenceEpochRadioButton == null || this.controlEpochRadioButton == null)
			return false;
		this.ignoreValueUpdate = true;
		this.referenceEpochRadioButton.setSelected(referenceEpoch != null && referenceEpoch == Boolean.TRUE);
		this.controlEpochRadioButton.setSelected(referenceEpoch == null || referenceEpoch == Boolean.FALSE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setUncertainty(ObservationGroupUncertaintyType type, Double value, boolean displayWarningIcon) {
		if (this.warningIconNodes.containsKey(type)) {
			this.warningIconNodes.get(type).setVisible(displayWarningIcon);
			this.warningIconNodes.get(type).setManaged(displayWarningIcon);
		}

		switch(type) {
		case ZERO_POINT_OFFSET:
			return this.setZeroPointOffsetUncertainty(value);
		case DISTANCE_DEPENDENT:
			return this.setDistanceDependentUncertainty(value);
		case SQUARE_ROOT_DISTANCE_DEPENDENT:
			return this.setSquareRootDistanceDependentUncertainty(value);
		default:
			return false;
		}
	}
	
	public boolean setAdditionalParameter(ParameterType paramType, Double value, Boolean enable, boolean displayWarningIcon) {
		if (this.warningIconNodes.containsKey(paramType)) {
			this.warningIconNodes.get(paramType).setVisible(displayWarningIcon);
			this.warningIconNodes.get(paramType).setManaged(displayWarningIcon);
		}
		
		switch(paramType) {
		case ZERO_POINT_OFFSET:
			return this.setZeroPointOffset(value, enable);
		case SCALE:
			return this.setScale(value, enable);
		case ORIENTATION:
			return this.setOrientation(value, enable);
		case ROTATION_X:
			return this.setRotationX(value, enable);
		case ROTATION_Y:
			return this.setRotationY(value, enable);
		case ROTATION_Z:
			return this.setRotationZ(value, enable);
		case REFRACTION_INDEX:
			return this.setRefractionIndex(value, enable);
		default:
			return false;	
		}
	}
	
	public boolean setZeroPointOffset(Double value, Boolean enable) {
		if (this.zeroPointOffsetCheckBox == null || this.zeroPointOffsetField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.zeroPointOffsetCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.zeroPointOffsetField.setValue(value);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setScale(Double value, Boolean enable) {
		if (this.scaleCheckBox == null || this.scaleField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.scaleCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.scaleField.setValue(value);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setRotationX(Double value, Boolean enable) {
		if (this.rotationXCheckBox == null || this.rotationXField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.rotationXCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.rotationXField.setValue(value);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setRotationY(Double value, Boolean enable) {
		if (this.rotationYCheckBox == null || this.rotationYField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.rotationYCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.rotationYField.setValue(value);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setRotationZ(Double value, Boolean enable) {
		if (this.rotationZCheckBox == null || this.rotationZField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.rotationZCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.rotationZField.setValue(value);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setOrientation(Double value, Boolean enable) {
		if (this.orientationOffsetCheckBox == null || this.orientationOffsetField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.orientationOffsetCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.orientationOffsetField.setValue(value);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setRefractionIndex(Double value, Boolean enable) {
		if (this.refractionIndexCheckBox == null || this.refractionIndexField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.refractionIndexCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.refractionIndexField.setValue(value);
		this.ignoreValueUpdate = false;
		return true;
	}

	private Node createAdditionalParametersPane() {
		GridPane gridPane = this.createGridPane();

		double offset      = 0.0;
		double scale       = 1.0;
		double orientation = 0.0;
		double refraction  = 0.0;
		double rotationY   = 0.0;
		double rotationX   = 0.0;
		double rotationZ   = 0.0;
	
		int row = 0;
		ParameterType[] paramTypes = ObservationTreeItemValue.getParameterTypes(this.type);
		for (ParameterType paramType : paramTypes) {
			CheckBox box = null;
			DoubleTextField field = null;
			ProgressIndicator progressIndicator = null;
			Node warningIcon = null;
			
			switch(paramType) {

			case ORIENTATION:
				
				box   = this.orientationOffsetCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.orientation.label", "Orientation o"), i18n.getString("UIObservationPropertiesPane.additionalparameter.orientation.label.tooltip", "Checked, if orientation is an unknown parameter to be estimated"), false, paramType);
				field = this.orientationOffsetField    = this.createDoubleTextField(orientation, CellValueType.ANGLE_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.orientation.tooltip", "Set orientation offset"), paramType);				
				progressIndicator = this.createDatabaseTransactionProgressIndicator(paramType);
				warningIcon = this.createWarningIcon(paramType, i18n.getString("UIObservationPropertiesPane.additionalparameter.orientation.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.additionalparameter.orientation.warning.tooltip", "Note: The selected groups have different values and the orientation differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
				
				break;
				
			case REFRACTION_INDEX:
				
				box   = this.refractionIndexCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.refraction.label", "Refraction index k"), i18n.getString("UIObservationPropertiesPane.additionalparameter.refraction.label.tooltip", "Checked, if refraction index is an unknown parameter to be estimated"), true, paramType);
				field = this.refractionIndexField    = this.createDoubleTextField(refraction, CellValueType.STATISTIC, false, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.refraction.tooltip", "Set refraction index offset"), paramType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(paramType);
				warningIcon = this.createWarningIcon(paramType, i18n.getString("UIObservationPropertiesPane.additionalparameter.refraction.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.additionalparameter.refraction.warning.tooltip", "Note: The selected groups have different values and the refraction index differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
				
				break;
				
			case ROTATION_Y:
				
				box   = this.rotationYCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.y.label", "Rotation angle ry"), i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.y.label.tooltip", "Checked, if rotation angle around y-axis is an unknown parameter to be estimated"), true, paramType);
				field = this.rotationYField    = this.createDoubleTextField(rotationY, CellValueType.ANGLE_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.y.tooltip", "Set rotation angle around y-axis"), paramType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(paramType);
				warningIcon = this.createWarningIcon(paramType, i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.y.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.y.warning.tooltip", "Note: The selected groups have different values and the rotation angle around y-axis differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
				
				break;
				
			case ROTATION_X:
				
				box   = this.rotationXCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.x.label", "Rotation angle rx"), i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.x.label.tooltip", "Checked, if rotation angle around x-axis is an unknown parameter to be estimated"), true, paramType);
				field = this.rotationXField    = this.createDoubleTextField(rotationX, CellValueType.ANGLE_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.x.tooltip", "Set rotation angle around x-axis"), paramType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(paramType);
				warningIcon = this.createWarningIcon(paramType, i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.x.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.x.warning.tooltip", "Note: The selected groups have different values and the rotation angle around x-axis differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
				
				break;
				
			case ROTATION_Z:
				
				box   = this.rotationZCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.z.label", "Rotation angle rz"), i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.z.label.tooltip", "Checked, if rotation angle around z-axis is an unknown parameter to be estimated"), true, paramType);
				field = this.rotationZField    = this.createDoubleTextField(rotationZ, CellValueType.ANGLE_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.z.tooltip", "Set rotation angle around z-axis"), paramType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(paramType);
				warningIcon = this.createWarningIcon(paramType, i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.z.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.additionalparameter.rotation.z.warning.tooltip", "Note: The selected groups have different values and the rotation angle around z-axis differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
				
				break;
			case SCALE:
				
				box   = this.scaleCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.scale.label", "Scale s"), i18n.getString("UIObservationPropertiesPane.additionalparameter.scale.label.tooltip", "Checked, if scale is an unknown parameter to be estimated"), true, paramType);
				field = this.scaleField    = this.createDoubleTextField(scale, CellValueType.SCALE, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.scale.tooltip", "Set scale"), paramType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(paramType);
				warningIcon = this.createWarningIcon(paramType, i18n.getString("UIObservationPropertiesPane.additionalparameter.scale.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.additionalparameter.scale.warning.tooltip", "Note: The selected groups have different values and the scale differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
				
				break;

			case ZERO_POINT_OFFSET:
				
				box   = this.zeroPointOffsetCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.zero_point_offset.label", "Offset a"), i18n.getString("UIObservationPropertiesPane.additionalparameter.zero_point_offset.label.tooltip", "Checked, if zero point offset is an unknown parameter to be estimated"), true, paramType);
				field = this.zeroPointOffsetField    = this.createDoubleTextField(offset, CellValueType.LENGTH_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.zero_point_offset.tooltip", "Set zero point offset"), paramType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(paramType);
				warningIcon = this.createWarningIcon(paramType, i18n.getString("UIObservationPropertiesPane.additionalparameter.zero_point_offset.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.additionalparameter.zero_point_offset.warning.tooltip", "Note: The selected groups have different values and the zero point offset differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
				
				break;
				
			default:
				break;
			
			}
			
			if (field != null && box != null && progressIndicator != null && warningIcon != null) {
				gridPane.add(box,  0, row);
				gridPane.add(field, 1, row);
				gridPane.add(new HBox(warningIcon, progressIndicator), 2, row++);
			}
		}

		TitledPane additionalParametersTitledPane = this.createTitledPane(i18n.getString("UIObservationPropertiesPane.additionalparameter.title", "Additional parameters"));
		additionalParametersTitledPane.setContent(gridPane);
		return additionalParametersTitledPane;
	}

	private Node createUncertaintiesPane() {
		CellValueType constantUncertaintyCellValueType = null;
		CellValueType squareRootDistanceDependentUncertaintyCellValueType = CellValueType.LENGTH_UNCERTAINTY;
		CellValueType distanceDependentUncertaintyCellValueType = null;
		
		double sigmaZeroPointOffset    = ObservationTreeItemValue.getDefaultUncertainty(this.type, ObservationGroupUncertaintyType.ZERO_POINT_OFFSET);
		double sigmaSquareRootDistance = ObservationTreeItemValue.getDefaultUncertainty(this.type, ObservationGroupUncertaintyType.SQUARE_ROOT_DISTANCE_DEPENDENT);
		double sigmaDistanceDependent  = ObservationTreeItemValue.getDefaultUncertainty(this.type, ObservationGroupUncertaintyType.DISTANCE_DEPENDENT);

		switch(this.type) {
		case LEVELING_LEAF:
			constantUncertaintyCellValueType = CellValueType.LENGTH_UNCERTAINTY;
			distanceDependentUncertaintyCellValueType = CellValueType.SCALE_UNCERTAINTY;
			
			break;
			
		case HORIZONTAL_DISTANCE_LEAF:
		case SLOPE_DISTANCE_LEAF:
			constantUncertaintyCellValueType = CellValueType.LENGTH_UNCERTAINTY;
			distanceDependentUncertaintyCellValueType = CellValueType.SCALE_UNCERTAINTY;
			
			break;

		case DIRECTION_LEAF:
		case ZENITH_ANGLE_LEAF:
			constantUncertaintyCellValueType = CellValueType.ANGLE_UNCERTAINTY;
			distanceDependentUncertaintyCellValueType = CellValueType.LENGTH_UNCERTAINTY;
			
			break;
			
		case GNSS_1D_LEAF:
		case GNSS_2D_LEAF:
		case GNSS_3D_LEAF:
			constantUncertaintyCellValueType = CellValueType.LENGTH_UNCERTAINTY;
			distanceDependentUncertaintyCellValueType = CellValueType.SCALE_UNCERTAINTY;
			
			break;

		default:
			System.err.println(this.getClass().getSimpleName() + " : Error, unsupported node type " + type);
			return null;
		}

		GridPane gridPane = this.createGridPane();
		
		Node warningIconUncertaintyTypeANode = this.createWarningIcon(ObservationGroupUncertaintyType.ZERO_POINT_OFFSET, i18n.getString("UIObservationPropertiesPane.uncertainty.ua.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.uncertainty.ua.warning.tooltip", "Note: The selected groups have different values and \u03C3a differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
		ProgressIndicator databaseTransactionUncertaintyTypeAProgressIndicator = this.createDatabaseTransactionProgressIndicator(ObservationGroupUncertaintyType.ZERO_POINT_OFFSET);
		Label uncertaintyTypeALabel = new Label(i18n.getString("UIObservationPropertiesPane.uncertainty.ua.label", "\u03C3a"));
		uncertaintyTypeALabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.zeroPointOffsetUncertaintyField = this.createUncertaintyTextField(sigmaZeroPointOffset, constantUncertaintyCellValueType, true, DoubleTextField.ValueSupport.EXCLUDING_INCLUDING_INTERVAL, i18n.getString("UIObservationPropertiesPane.uncertainty.ua.tooltip", "Set constant part of combined uncertainty"), ObservationGroupUncertaintyType.ZERO_POINT_OFFSET);
		
		Node warningIconUncertaintyTypeBNode = this.createWarningIcon(ObservationGroupUncertaintyType.SQUARE_ROOT_DISTANCE_DEPENDENT, i18n.getString("UIObservationPropertiesPane.uncertainty.ub.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.uncertainty.ub.warning.tooltip", "Note: The selected groups have different values and \u03C3b differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
		ProgressIndicator databaseTransactionUncertaintyTypeBProgressIndicator = this.createDatabaseTransactionProgressIndicator(ObservationGroupUncertaintyType.SQUARE_ROOT_DISTANCE_DEPENDENT);
		Label uncertaintyTypeBLabel = new Label(i18n.getString("UIObservationPropertiesPane.uncertainty.ub.label", "\u03C3b(\u221Ad)"));
		uncertaintyTypeBLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.squareRootDistanceDependentUncertaintyField = this.createUncertaintyTextField(sigmaSquareRootDistance, squareRootDistanceDependentUncertaintyCellValueType, true, DoubleTextField.ValueSupport.INCLUDING_INCLUDING_INTERVAL, i18n.getString("UIObservationPropertiesPane.uncertainty.ub.tooltip", "Set square-root distance dependent part of combined uncertainty"), ObservationGroupUncertaintyType.SQUARE_ROOT_DISTANCE_DEPENDENT);  
		
		Node warningIconUncertaintyTypeCNode = this.createWarningIcon(ObservationGroupUncertaintyType.DISTANCE_DEPENDENT, i18n.getString("UIObservationPropertiesPane.uncertainty.uc.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.uncertainty.uc.warning.tooltip", "Note: The selected groups have different values and \u03C3c differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
		ProgressIndicator databaseTransactionUncertaintyTypeCProgressIndicator = this.createDatabaseTransactionProgressIndicator(ObservationGroupUncertaintyType.DISTANCE_DEPENDENT);
		Label uncertaintyTypeCLabel = new Label(i18n.getString("UIObservationPropertiesPane.uncertainty.uc.label", "\u03C3c(d)"));
		uncertaintyTypeCLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.distanceDependentUncertaintyField = this.createUncertaintyTextField(sigmaDistanceDependent, distanceDependentUncertaintyCellValueType, true, DoubleTextField.ValueSupport.INCLUDING_INCLUDING_INTERVAL, i18n.getString("UIObservationPropertiesPane.uncertainty.uc.tooltip", "Set distance dependent part of combined uncertainty"), ObservationGroupUncertaintyType.DISTANCE_DEPENDENT);
		
		uncertaintyTypeALabel.setLabelFor(this.zeroPointOffsetUncertaintyField);
		uncertaintyTypeBLabel.setLabelFor(this.squareRootDistanceDependentUncertaintyField);
		uncertaintyTypeCLabel.setLabelFor(this.distanceDependentUncertaintyField);
				
//		GridPane.setHgrow(uncertaintyTypeALabel, Priority.SOMETIMES);
//		GridPane.setHgrow(this.zeroPointOffsetUncertaintyField, Priority.ALWAYS);
//		GridPane.setHgrow(databaseTransactionUncertaintyTypeAProgressIndicator, Priority.NEVER);
//		
//		GridPane.setHgrow(uncertaintyTypeBLabel, Priority.SOMETIMES);
//		GridPane.setHgrow(this.squareRootDistanceDependentUncertaintyField, Priority.ALWAYS);
//		GridPane.setHgrow(databaseTransactionUncertaintyTypeBProgressIndicator, Priority.NEVER);
//		
//		GridPane.setHgrow(uncertaintyTypeCLabel, Priority.SOMETIMES);
//		GridPane.setHgrow(this.distanceDependentUncertaintyField, Priority.ALWAYS);
//		GridPane.setHgrow(databaseTransactionUncertaintyTypeCProgressIndicator, Priority.NEVER);
		
		gridPane.add(uncertaintyTypeALabel, 0, 0);
		gridPane.add(this.zeroPointOffsetUncertaintyField, 1, 0);
		gridPane.add(new HBox(warningIconUncertaintyTypeANode, databaseTransactionUncertaintyTypeAProgressIndicator), 2, 0);

		gridPane.add(uncertaintyTypeBLabel, 0, 1);
		gridPane.add(this.squareRootDistanceDependentUncertaintyField, 1, 1);
		gridPane.add(new HBox(warningIconUncertaintyTypeBNode, databaseTransactionUncertaintyTypeBProgressIndicator), 2, 1);

		gridPane.add(uncertaintyTypeCLabel, 0, 2);
		gridPane.add(this.distanceDependentUncertaintyField, 1, 2);
		gridPane.add(new HBox(warningIconUncertaintyTypeCNode, databaseTransactionUncertaintyTypeCProgressIndicator), 2, 2);

		TitledPane uncertaintiesTitledPane = this.createTitledPane(i18n.getString("UIObservationPropertiesPane.uncertainty.title", "Uncertainties"));
		uncertaintiesTitledPane.setContent(gridPane);
		return uncertaintiesTitledPane;
	}

	private Node createCongruenceAnalysisPane() {
		GridPane gridPane = this.createGridPane();

		ProgressIndicator databaseTransactionReferenceEpochProgressIndicator = this.createDatabaseTransactionProgressIndicator(Epoch.REFERENCE);
		ProgressIndicator databaseTransactionControlEpochProgressIndicator = this.createDatabaseTransactionProgressIndicator(Epoch.CONTROL);
		
		ToggleGroup group = new ToggleGroup();

		this.referenceEpochRadioButton = this.createRadioButton(i18n.getString("UIObservationPropertiesPane.congruenceanalysis.referenceepoch.label", "Reference epoch"), 
				i18n.getString("UIObservationPropertiesPane.congruenceanalysis.referenceepoch.tooltip", "Selected, if group is referred to reference epoch"), group, true, Epoch.REFERENCE);

		this.controlEpochRadioButton = this.createRadioButton(i18n.getString("UIObservationPropertiesPane.congruenceanalysis.controlepoch.label", "Control epoch"), 
				i18n.getString("UIObservationPropertiesPane.congruenceanalysis.controlepoch.tooltip", "Selected, if group is referred to control epoch"), group, false, Epoch.CONTROL);

		gridPane.add(this.referenceEpochRadioButton, 0, 0);
		gridPane.add(databaseTransactionReferenceEpochProgressIndicator, 1, 0);
		
		gridPane.add(this.controlEpochRadioButton, 0, 1);
		gridPane.add(databaseTransactionControlEpochProgressIndicator, 1, 1);

		TitledPane congruenceAnalysisTitledPane = this.createTitledPane(i18n.getString("UIObservationPropertiesPane.congruenceanalysis.title", "Congruence analysis"));
		congruenceAnalysisTitledPane.setContent(gridPane);
		return congruenceAnalysisTitledPane;
	}

	private Node createUncertaintyChartPane() {
		NumberAxis xAxis = new NumberAxis(0, 155, 10);
        NumberAxis yAxis = new NumberAxis();
        
        xAxis.setForceZeroInRange(true);
        xAxis.setMinorTickVisible(false);
        xAxis.setAnimated(false);
        
        yAxis.setMinorTickVisible(false);
        yAxis.setForceZeroInRange(false);
        yAxis.setAnimated(false);
        
		this.lineChart = new LineChart<Number,Number>(xAxis,yAxis);
		this.lineChart.setLegendVisible(false);
		this.lineChart.setAnimated(false);
		this.lineChart.setPadding(new Insets(0, 0, 0, 0));
        this.lineChart.setCreateSymbols(false);
        
        this.updateUncertaintyChart(this.lineChart);
        this.updateTickLabels(this.lineChart);

        TitledPane uncertaintyChartTitledPane = this.createTitledPane(i18n.getString("UIObservationPropertiesPane.chart.title", "Combined Uncertainties"));
		uncertaintyChartTitledPane.setContent(this.lineChart);
		return uncertaintyChartTitledPane;
	}

	private void init() {
		VBox content = new VBox();
		content.getChildren().addAll(
				this.createUncertaintiesPane(),
				this.createAdditionalParametersPane(),
				this.createCongruenceAnalysisPane(),
				this.createUncertaintyChartPane()
				);
		
		this.reset();

		ScrollPane scroller = new ScrollPane(content);
		scroller.setPadding(new Insets(20, 50, 20, 50)); // oben, links, unten, rechts
		scroller.setFitToHeight(true);
		scroller.setFitToWidth(true);
		this.propertiesNode = scroller;
		
		FadeTransition fadeIn  = new FadeTransition(Duration.millis(150));
		FadeTransition fadeOut = new FadeTransition(Duration.millis(150));

	    fadeIn.setFromValue(0.0);
	    fadeIn.setToValue(1.0);
	    fadeIn.setCycleCount(1);
	    fadeIn.setAutoReverse(false);

	    fadeOut.setFromValue(1.0);
	    fadeOut.setToValue(0.0);
	    fadeOut.setCycleCount(1);
	    fadeOut.setAutoReverse(false);
	    
	    this.sequentialTransition.getChildren().addAll(fadeIn, fadeOut);
	    this.sequentialTransition.setAutoReverse(false);
	    this.sequentialTransition.onFinishedProperty().addListener(new SequentialTransitionFinishedListener());
	    this.options.addFormatterChangedListener(new TickFormatChangedListener());
	}
	
	private DoubleTextField createDoubleTextField(double value, CellValueType type, boolean displayUnit, ValueSupport valueSupport, String tooltipText, ParameterType userData) {
		DoubleTextField field = new DoubleTextField(value, type, displayUnit, valueSupport);
		field.setTooltip(new Tooltip(tooltipText));
		field.setMinWidth(200);
		field.setMaxWidth(350);
		field.setUserData(userData);
		field.numberProperty().addListener(new NumberChangeListener(field));
		return field;
	}
	
	private UncertaintyTextField createUncertaintyTextField(double value, CellValueType type, boolean displayUnit, ValueSupport valueSupport, String tooltipText, ObservationGroupUncertaintyType userData) {
		UncertaintyTextField field = new UncertaintyTextField(value, type, displayUnit, valueSupport);
		field.setTooltip(new Tooltip(tooltipText));
		field.setMinWidth(200);
		field.setMaxWidth(350);
		field.setUserData(userData);
		field.numberProperty().addListener(new NumberChangeListener(field));
		return field;
	}

	private CheckBox createCheckBox(String title, String tooltipText, boolean selected, ParameterType userData) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,3));
		CheckBox checkBox = new CheckBox();
		checkBox.setGraphic(label);
		checkBox.setTooltip(new Tooltip(tooltipText));
		checkBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		checkBox.setSelected(selected);
		checkBox.setUserData(userData);
		checkBox.selectedProperty().addListener(new BooleanChangeListener(checkBox));
		return checkBox;
	}
	
	private RadioButton createRadioButton(String title, String tooltipText, ToggleGroup group, boolean selected, Epoch userData) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,3));
		RadioButton radioButton = new RadioButton();
		radioButton.setGraphic(label);
		radioButton.setTooltip(new Tooltip(tooltipText));
		radioButton.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		radioButton.setToggleGroup(group);
		radioButton.setSelected(selected);
		radioButton.setUserData(userData);
		radioButton.selectedProperty().addListener(new BooleanChangeListener(radioButton));
		return radioButton;
	}
	
	private ProgressIndicator createDatabaseTransactionProgressIndicator(Object userData) {
		ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);

		progressIndicator.setVisible(false);
		progressIndicator.setMinSize(17, 17);
		progressIndicator.setMaxSize(17, 17);
		progressIndicator.setUserData(userData);
				
		this.databaseTransactionProgressIndicators.put(userData, progressIndicator);
		return progressIndicator;
	}
	
	private Node createWarningIcon(Object userData, String text, String tooltip) {
		Label label = new Label();
		
		// Workaround, da setFont auf den Text und den Tooltip angewandt wird
		// https://bugs.openjdk.java.net/browse/JDK-8094344
		Label txtNode = new Label(text);
		txtNode.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		txtNode.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		txtNode.setTextFill(Color.DARKORANGE);
		txtNode.setPadding(new Insets(0,0,0,0));
		
		label.setGraphic(txtNode);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setTooltip(new Tooltip(tooltip));
		label.setUserData(userData);
		label.setVisible(false);
		label.setManaged(false);
		label.setPadding(new Insets(0,0,0,0));
		this.warningIconNodes.put(userData, label);
		return label;
	}
	
	private GridPane createGridPane() {
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(20, 10, 20, 10)); // oben, links, unten, rechts
		return gridPane;
	}
	
	private TitledPane createTitledPane(String title) {
		TitledPane parametersTitledPane = new TitledPane();
		parametersTitledPane.setMaxWidth(Double.MAX_VALUE);
		parametersTitledPane.setCollapsible(false);
		parametersTitledPane.setAnimated(false);
		parametersTitledPane.setText(title);
		parametersTitledPane.setPadding(new Insets(5, 0, 5, 0)); // oben, links, unten, rechts
		return parametersTitledPane;
	}
	
	private void setProgressIndicatorsVisible(boolean visible) {
		if (this.databaseTransactionProgressIndicators != null)
			for (ProgressIndicator progressIndicator : this.databaseTransactionProgressIndicators.values())
				progressIndicator.setVisible(visible);
	}
	
	private void setWarningIconsVisible(boolean visible) {
		if (this.warningIconNodes != null)
			for (Node warningIconNode : this.warningIconNodes.values()) {
				warningIconNode.setVisible(visible);
				warningIconNode.setManaged(visible);	
			}
	}
	
	private void save(ObservationGroupUncertaintyType uncertaintyType) {
		try {
			Double value = null;
			switch(uncertaintyType) {
			case ZERO_POINT_OFFSET:
				value = this.zeroPointOffsetUncertaintyField.getNumber();
				break;
			case DISTANCE_DEPENDENT:
				value = this.distanceDependentUncertaintyField.getNumber();
				break;
			case SQUARE_ROOT_DISTANCE_DEPENDENT:
				value = this.squareRootDistanceDependentUncertaintyField.getNumber();
				break;
			default:
				System.err.println(this.getClass().getSimpleName() + " : Error, unsupported uncertainty type " + uncertaintyType);
				break;
			}

			if (value != null && value.doubleValue() >= 0 && this.selectedObservationItemValues != null && this.selectedObservationItemValues.length > 0) {
				this.setProgressIndicatorsVisible(false);
				if (this.warningIconNodes.containsKey(uncertaintyType)) {
					Node warningIconNodes = this.warningIconNodes.get(uncertaintyType);
					warningIconNodes.setVisible(false);
					warningIconNodes.setManaged(false);
				}
				if (this.databaseTransactionProgressIndicators.containsKey(uncertaintyType)) {
					ProgressIndicator progressIndicator = this.databaseTransactionProgressIndicators.get(uncertaintyType);
					progressIndicator.setVisible(true);
					this.sequentialTransition.stop();
					this.sequentialTransition.setNode(progressIndicator);
					this.sequentialTransition.playFromStart();
				}
				SQLManager.getInstance().saveUncertainty(uncertaintyType, value.doubleValue(), this.selectedObservationItemValues);
				this.updateUncertaintyChart(this.lineChart);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
			this.setProgressIndicatorsVisible(false);
			this.sequentialTransition.stop();
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIObservationPropertiesPane.message.error.save.uncertainty.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIObservationPropertiesPane.message.error.save.uncertainty.exception.header", "Error, could not save group uncertainties to database."),
							i18n.getString("UIObservationPropertiesPane.message.error.save.uncertainty.exception.message", "An exception has occurred during database transaction."),
							e
					);
				}
			});
		}
	}
	
	private void save(ParameterType parameterType) {
		try {
			boolean enable = false;
			Double value = null;
			switch(parameterType) {
			case ORIENTATION:
				enable = this.orientationOffsetCheckBox.isSelected();
				value = this.orientationOffsetField.getNumber();
				break;
			case REFRACTION_INDEX:
				enable = this.refractionIndexCheckBox.isSelected();
				value = this.refractionIndexField.getNumber();
				break;
			case ROTATION_X:
				enable = this.rotationXCheckBox.isSelected();
				value = this.rotationXField.getNumber();
				break;
			case ROTATION_Y:
				enable = this.rotationYCheckBox.isSelected();
				value = this.rotationYField.getNumber();
				break;
			case ROTATION_Z:
				enable = this.rotationZCheckBox.isSelected();
				value = this.rotationZField.getNumber();
				break;
			case SCALE:
				enable = this.scaleCheckBox.isSelected();
				value = this.scaleField.getNumber();
				break;
			case ZERO_POINT_OFFSET:
				enable = this.zeroPointOffsetCheckBox.isSelected();
				value = this.zeroPointOffsetField.getNumber();
				break;
			default:
				System.err.println(this.getClass().getSimpleName() + " : Error, unsupported parameter type " + parameterType);
				break;
			}
			
			if (value != null && this.selectedObservationItemValues != null && this.selectedObservationItemValues.length > 0) {
				this.setProgressIndicatorsVisible(false);
				if (this.warningIconNodes.containsKey(parameterType)) {
					Node warningIconNodes = this.warningIconNodes.get(parameterType);
					warningIconNodes.setVisible(false);
					warningIconNodes.setManaged(false);
				}
				if (this.databaseTransactionProgressIndicators.containsKey(parameterType)) {
					ProgressIndicator node = this.databaseTransactionProgressIndicators.get(parameterType);
					node.setVisible(true);
					this.sequentialTransition.stop();
					this.sequentialTransition.setNode(node);
					this.sequentialTransition.playFromStart();
				}
				SQLManager.getInstance().saveAdditionalParameter(parameterType, enable, value.doubleValue(), this.selectedObservationItemValues);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
			this.setProgressIndicatorsVisible(false);
			this.sequentialTransition.stop();
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIObservationPropertiesPane.message.error.save.parameter.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIObservationPropertiesPane.message.error.save.parameter.exception.header", "Error, could not save properties of additional group parameters to database."),
							i18n.getString("UIObservationPropertiesPane.message.error.save.parameter.exception.message", "An exception has occurred during database transaction."),
							e
					);
				}
			});
		}
	}
	
	private void save() {
		try {
			if (this.selectedObservationItemValues != null && this.selectedObservationItemValues.length > 0) {
				this.setProgressIndicatorsVisible(false);
				if (this.databaseTransactionProgressIndicators.containsKey(this.referenceEpochRadioButton.isSelected() ? this.referenceEpochRadioButton.getUserData() : this.controlEpochRadioButton.getUserData())) {
					ProgressIndicator node = this.databaseTransactionProgressIndicators.get(this.referenceEpochRadioButton.isSelected() ? this.referenceEpochRadioButton.getUserData() : this.controlEpochRadioButton.getUserData());
					node.setVisible(true);
					this.sequentialTransition.stop();
					this.sequentialTransition.setNode(node);
					this.sequentialTransition.playFromStart();
				}
				SQLManager.getInstance().saveEpoch(this.referenceEpochRadioButton.isSelected(), this.selectedObservationItemValues);
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			this.setProgressIndicatorsVisible(false);
			this.setReferenceEpoch(!this.referenceEpochRadioButton.isSelected());
			this.sequentialTransition.stop();
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIObservationPropertiesPane.message.error.save.epoch.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIObservationPropertiesPane.message.error.save.epoch.exception.header", "Error, could not save observation epoch properties to database."),
							i18n.getString("UIObservationPropertiesPane.message.error.save.epoch.exception.message", "An exception has occurred during database transaction."),
							e
					);
				}
			});
		}
	}
	
	private void updateTickLabels(LineChart<Number,Number> lineChart) {
		NumberAxis xAxis = (NumberAxis)lineChart.getXAxis();
		NumberAxis yAxis = (NumberAxis)lineChart.getYAxis();

		CellValueType cellValueType;
		switch(this.type) {
		case DIRECTION_LEAF:
		case ZENITH_ANGLE_LEAF:
			cellValueType = CellValueType.ANGLE_UNCERTAINTY;
			yAxis.setLabel(String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.chart.axis.y.label", "\u03C30 [%s]"), options.getFormatterOptions().get(CellValueType.ANGLE_UNCERTAINTY).getUnit().getAbbreviation()));
			break;

		default:
			cellValueType = CellValueType.LENGTH_UNCERTAINTY;
			yAxis.setLabel(String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.chart.axis.y.label", "\u03C30 [%s]"), options.getFormatterOptions().get(CellValueType.LENGTH_UNCERTAINTY).getUnit().getAbbreviation()));
			break;
		}
		
		yAxis.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number number) {
				return options.toViewFormat(cellValueType, number.doubleValue(), false);
			}

			@Override
			public Number fromString(String string) {
				return null;
			}
		});

		xAxis.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number number) {
				return (number.doubleValue() > maxDistanceForUncertaintyChart) ? "" : String.format(Locale.ENGLISH, "%.0f", number.doubleValue());
			}

			@Override
			public Number fromString(String string) {
				return null;
			}
		});
		xAxis.setLabel(String.format(Locale.ENGLISH, i18n.getString("UIObservationPropertiesPane.chart.axis.x.label", "d0 [%s]"), options.getFormatterOptions().get(CellValueType.LENGTH).getUnit().getAbbreviation()));
	}
	
	private void updateUncertaintyChart(LineChart<Number,Number> lineChart) {
		XYChart.Series<Number, Number> uncertaintyChartSeries = new XYChart.Series<Number, Number>();
		
		double uncertaintyA = this.zeroPointOffsetUncertaintyField.getNumber();
		double uncertaintyB = this.squareRootDistanceDependentUncertaintyField.getNumber();
		double uncertaintyC = this.distanceDependentUncertaintyField.getNumber();
		
        ObservationGroup group = null;
        switch(this.type) {
		case LEVELING_LEAF:
			group = new DeltaZGroup(-1, uncertaintyA, uncertaintyB, uncertaintyC, Epoch.REFERENCE);
			break;
		case DIRECTION_LEAF:
			group = new DirectionGroup(-1, uncertaintyA, uncertaintyB, uncertaintyC, Epoch.REFERENCE);
			break;
		case HORIZONTAL_DISTANCE_LEAF:
			group = new HorizontalDistanceGroup(-1, uncertaintyA, uncertaintyB, uncertaintyC, Epoch.REFERENCE);
			break;
		case SLOPE_DISTANCE_LEAF:
			group = new SlopeDistanceGroup(-1, uncertaintyA, uncertaintyB, uncertaintyC, Epoch.REFERENCE);
			break;
		case ZENITH_ANGLE_LEAF:
			group = new ZenithAngleGroup(-1, uncertaintyA, uncertaintyB, uncertaintyC, Epoch.REFERENCE);
			break;
		case GNSS_1D_LEAF:
			group = new GNSSBaseline1DGroup(-1, uncertaintyA, uncertaintyB, uncertaintyC, Epoch.REFERENCE);
			break;
		case GNSS_2D_LEAF:
			group = new GNSSBaseline2DGroup(-1, uncertaintyA, uncertaintyB, uncertaintyC, Epoch.REFERENCE);
			break;
		case GNSS_3D_LEAF:
			group = new GNSSBaseline3DGroup(-1, uncertaintyA, uncertaintyB, uncertaintyC, Epoch.REFERENCE);
			break;
		default:
			System.err.println(this.getClass().getSimpleName() + " Error, unsupported obsewrvation group type " + type);	
			break;
        }
        
        if (group == null)
        	return;

        for (double distanceInViewUnit = this.minDistanceForUncertaintyChart; distanceInViewUnit <= this.maxDistanceForUncertaintyChart; distanceInViewUnit += 0.25) {
        	double distanceInModelUnit = options.convertLengthToModel(distanceInViewUnit); // distance in meter
        	Observation observation = this.createObservation(this.type, distanceInModelUnit);
        	if (observation == null)
        		break;

        	double uncertaintyInModelUnit = group.getStd(observation);        	
        	double uncertaintyInViewUnit = 0;
        	
        	switch(this.type) {
        	case DIRECTION_LEAF:
        	case ZENITH_ANGLE_LEAF:
        		uncertaintyInViewUnit = options.convertAngleUncertaintyToView(uncertaintyInModelUnit);
        		break;
        	default:
        		uncertaintyInViewUnit = options.convertLengthUncertaintyToView(uncertaintyInModelUnit);
        		break;
        	}
        	uncertaintyChartSeries.getData().add(new XYChart.Data<Number,Number>(distanceInViewUnit, uncertaintyInViewUnit));
        }

        this.lineChart.getData().clear();
        this.lineChart.getData().add(uncertaintyChartSeries);
        
        Node line = uncertaintyChartSeries.getNode().lookup(".chart-series-line");
        line.setStyle("-fx-stroke: rgba(200, 0, 0, 1); -fx-stroke-width: 2.5px;");        
	}
	
	private Observation createObservation(TreeItemType type, double distance) {
		Point startPoint = new Point3D("0",        0, 0, 0);
		Point endPoint   = new Point3D("1", distance, 0, 0);
		
		Observation observation = null;
		switch(this.type) {
		case LEVELING_LEAF:
			observation = new DeltaZ(-1, startPoint, endPoint, 0.0, 0.0, 0.0, -1, distance);
			break;
		case DIRECTION_LEAF:
			observation = new Direction(-1, startPoint, endPoint, 0.0, 0.0, 0.0, -1, distance);
			break;
		case HORIZONTAL_DISTANCE_LEAF:
			observation = new HorizontalDistance(-1, startPoint, endPoint, 0.0, 0.0, 0.0, -1, distance);
			break;
		case SLOPE_DISTANCE_LEAF:
			observation = new SlopeDistance(-1, startPoint, endPoint, 0.0, 0.0, 0.0, -1, distance);
			break;
		case ZENITH_ANGLE_LEAF:
			observation = new ZenithAngle(-1, startPoint, endPoint, 0.0, 0.0, 0.0, -1, distance);
			break;
		case GNSS_1D_LEAF:
			observation = new GNSSBaselineDeltaZ1D(-1, startPoint, endPoint, distance, -1);
			break;
		case GNSS_2D_LEAF:
			observation = new GNSSBaselineDeltaX2D(-1, startPoint, endPoint, distance, -1);
			break;
		case GNSS_3D_LEAF:
			observation = new GNSSBaselineDeltaX3D(-1, startPoint, endPoint, distance, -1);
			break;
		default:
			System.err.println(this.getClass().getSimpleName() + " Error, unsupported obsewrvation group type " + type);	
			break;
        }
		return observation;
	}

}