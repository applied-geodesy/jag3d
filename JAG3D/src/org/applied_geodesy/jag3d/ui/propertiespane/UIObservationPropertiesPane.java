package org.applied_geodesy.jag3d.ui.propertiespane;

import java.sql.SQLException;

import org.applied_geodesy.adjustment.network.Epoch;
import org.applied_geodesy.adjustment.network.ObservationGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.jag3d.ui.textfield.DoubleTextField;
import org.applied_geodesy.jag3d.ui.textfield.LengthUnceraintySquareRootTextField;
import org.applied_geodesy.jag3d.ui.textfield.UncertaintyTextField;
import org.applied_geodesy.jag3d.ui.textfield.DoubleTextField.ValueSupport;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.util.i18.I18N;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class UIObservationPropertiesPane {
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
					saveAdditionalParameter(paramType);
				}
				else if (this.field.getUserData() instanceof ObservationGroupUncertaintyType) {
					ObservationGroupUncertaintyType uncertaintyType = (ObservationGroupUncertaintyType)this.field.getUserData();
					saveUncertainties(uncertaintyType);
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
					saveEpoch();
				}
				else if (this.button.getUserData() instanceof ParameterType) {
					ParameterType paramType = (ParameterType)this.button.getUserData();
					saveAdditionalParameter(paramType);
				}
			}
		}
	}
	
	
	private static I18N i18n = I18N.getInstance();
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
	
	private boolean ignoreValueUpdate = false;
	private ObservationTreeItemValue selectedObservationItemValues[] = null;
	
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
		this.selectedObservationItemValues = selectedObservationItemValues;
	}

	public Node getNode() {
		return this.propertiesNode;
	}
	
	public void reset() {
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
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setSquareRootDistanceDependentUncertainty(Double value) {
		if (this.squareRootDistanceDependentUncertaintyField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.squareRootDistanceDependentUncertaintyField.setValue(value != null && value >= 0 ? value : null);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setDistanceDependentUncertainty(Double value) {
		if (this.distanceDependentUncertaintyField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.distanceDependentUncertaintyField.setValue(value != null && value >= 0 ? value : null);
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
	
	public boolean setUncertainty(ObservationGroupUncertaintyType type, Double value) {
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
	
	public boolean setAdditionalParameter(ParameterType paramType, Double value, Boolean enable) {
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
			switch(paramType) {

			case ORIENTATION:
				
				box   = this.orientationOffsetCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.orientation.label", "Orientation o"), i18n.getString("UIObservationPropertiesPane.additionalparameter.orientation.label.tooltip", "Checked, if orientation is a known parameter"), false, ParameterType.ORIENTATION);
				field = this.orientationOffsetField    = this.createDoubleTextField(orientation, CellValueType.ANGLE_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.orientation.tooltip", "Choose orientation offset"), ParameterType.ORIENTATION);				

				break;
				
			case REFRACTION_INDEX:
				
				box   = this.refractionIndexCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.refraction.label", "Refraction index k"), i18n.getString("UIObservationPropertiesPane.additionalparameter.refraction.label.tooltip", "Checked, if refraction index is a known parameter"), true, ParameterType.REFRACTION_INDEX);
				field = this.refractionIndexField    = this.createDoubleTextField(refraction, CellValueType.STATISTIC, false, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.refraction.tooltip", "Choose refraction index offset"), ParameterType.REFRACTION_INDEX);

				break;
				
			case ROTATION_Y:
				
				box   = this.rotationYCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.rotationy.label", "Rotation angle ry"), i18n.getString("UIObservationPropertiesPane.additionalparameter.rotationy.label.tooltip", "Checked, if rotation angle around y-axis is a known parameter"), true, ParameterType.ROTATION_Y);
				field = this.rotationYField    = this.createDoubleTextField(rotationY, CellValueType.ANGLE_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.rotationy.tooltip", "Choose rotation angle around y-axis"), ParameterType.ROTATION_Y);

				break;
				
			case ROTATION_X:
				
				box   = this.rotationXCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.rotationx.label", "Rotation angle rx"), i18n.getString("UIObservationPropertiesPane.additionalparameter.rotationx.label.tooltip", "Checked, if rotation angle around x-axis is a known parameter"), true, ParameterType.ROTATION_X);
				field = this.rotationXField    = this.createDoubleTextField(rotationX, CellValueType.ANGLE_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.rotationx.tooltip", "Choose rotation angle around x-axis"), ParameterType.ROTATION_X);

				break;
				
			case ROTATION_Z:
				
				box   = this.rotationZCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.rotationz.label", "Rotation angle rz"), i18n.getString("UIObservationPropertiesPane.additionalparameter.rotationz.label.tooltip", "Checked, if rotation angle around z-axis is a known parameter"), true, ParameterType.ROTATION_Z);
				field = this.rotationZField    = this.createDoubleTextField(rotationZ, CellValueType.ANGLE_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.rotationz.tooltip", "Choose rotation angle around z-axis"), ParameterType.ROTATION_Z);

				break;
			case SCALE:
				
				box   = this.scaleCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.scale.label", "Scale s"), i18n.getString("UIObservationPropertiesPane.additionalparameter.scale.label.tooltip", "Checked, if scale is a known parameter"), true, ParameterType.SCALE);
				field = this.scaleField    = this.createDoubleTextField(scale, CellValueType.SCALE, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.scale.tooltip", "Choose scale"), ParameterType.SCALE);

				break;

			case ZERO_POINT_OFFSET:
				
				box   = this.zeroPointOffsetCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.additionalparameter.zeropoint.label", "Offset a"), i18n.getString("UIObservationPropertiesPane.additionalparameter.zeropoint.label.tooltip", "Checked, if zero point offset is a known parameter"), true, ParameterType.ZERO_POINT_OFFSET);
				field = this.zeroPointOffsetField    = this.createDoubleTextField(offset, CellValueType.LENGTH_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIObservationPropertiesPane.additionalparameter.zeropoint.tooltip", "Choose zero point offset"), ParameterType.ZERO_POINT_OFFSET);
				
				break;
				
			default:
				break;
			
			}
			
			if (field != null && box != null) {
				GridPane.setHgrow(box, Priority.SOMETIMES);
				GridPane.setHgrow(field, Priority.ALWAYS);
				
				gridPane.add(box,   0, row);
				gridPane.add(field, 1, row++);
			}
		}

		TitledPane additionalParametersTitledPane = this.createTitledPane(i18n.getString("UIObservationPropertiesPane.additionalparameter.title", "Additional parameters"));
		additionalParametersTitledPane.setContent(gridPane);
		return additionalParametersTitledPane;
	}

	private Node createUncertaintiesPane() {
		CellValueType constantUncertaintyCellValueType = null;
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

		Label uncertaintyTypeALabel = new Label(i18n.getString("UIObservationPropertiesPane.uncertainty.ua.label", "\u03C3a"));
		uncertaintyTypeALabel.setMinWidth(Control.USE_PREF_SIZE);
		this.zeroPointOffsetUncertaintyField = new UncertaintyTextField(sigmaZeroPointOffset, constantUncertaintyCellValueType, true, DoubleTextField.ValueSupport.GREATER_THAN_ZERO);
		this.zeroPointOffsetUncertaintyField.setTooltip(new Tooltip(i18n.getString("UIObservationPropertiesPane.uncertainty.ua.tooltip", "Choose constant part of combined uncertainty")));
		this.zeroPointOffsetUncertaintyField.setUserData(ObservationGroupUncertaintyType.ZERO_POINT_OFFSET);
		this.zeroPointOffsetUncertaintyField.numberProperty().addListener(new NumberChangeListener(this.zeroPointOffsetUncertaintyField));
		this.zeroPointOffsetUncertaintyField.setMinWidth(150);
		this.zeroPointOffsetUncertaintyField.setMaxWidth(250);
		
		Label uncertaintyTypeBLabel = new Label(i18n.getString("UIObservationPropertiesPane.uncertainty.ub.label", "\u03C3b"));
		uncertaintyTypeBLabel.setMinWidth(Control.USE_PREF_SIZE);
		this.squareRootDistanceDependentUncertaintyField = new LengthUnceraintySquareRootTextField(sigmaSquareRootDistance, true, DoubleTextField.ValueSupport.GREATER_THAN_OR_EQUAL_TO_ZERO, true);
		this.squareRootDistanceDependentUncertaintyField.setTooltip(new Tooltip(i18n.getString("UIObservationPropertiesPane.uncertainty.ub.tooltip", "Distance dependent part of combined uncertainty")));
		this.squareRootDistanceDependentUncertaintyField.setUserData(ObservationGroupUncertaintyType.SQUARE_ROOT_DISTANCE_DEPENDENT);
		this.squareRootDistanceDependentUncertaintyField.numberProperty().addListener(new NumberChangeListener(this.squareRootDistanceDependentUncertaintyField));
		this.squareRootDistanceDependentUncertaintyField.setMinWidth(150);
		this.squareRootDistanceDependentUncertaintyField.setMaxWidth(250);
		
		Label uncertaintyTypeCLabel = new Label(i18n.getString("UIObservationPropertiesPane.uncertainty.uc.label", "\u03C3c"));
		uncertaintyTypeCLabel.setMinWidth(Control.USE_PREF_SIZE);
		this.distanceDependentUncertaintyField = new UncertaintyTextField(sigmaDistanceDependent, distanceDependentUncertaintyCellValueType, true, DoubleTextField.ValueSupport.GREATER_THAN_OR_EQUAL_TO_ZERO);
		this.distanceDependentUncertaintyField.setTooltip(new Tooltip(i18n.getString("UIObservationPropertiesPane.uncertainty.uc.tooltip", "Squared distance dependent part of combined uncertainty")));
		this.distanceDependentUncertaintyField.setUserData(ObservationGroupUncertaintyType.DISTANCE_DEPENDENT);
		this.distanceDependentUncertaintyField.numberProperty().addListener(new NumberChangeListener(this.distanceDependentUncertaintyField));
		this.distanceDependentUncertaintyField.setMinWidth(150);
		this.distanceDependentUncertaintyField.setMaxWidth(250);
		
		uncertaintyTypeALabel.setLabelFor(this.zeroPointOffsetUncertaintyField);
		uncertaintyTypeBLabel.setLabelFor(this.squareRootDistanceDependentUncertaintyField);
		uncertaintyTypeCLabel.setLabelFor(this.distanceDependentUncertaintyField);
		
		GridPane.setHgrow(uncertaintyTypeALabel, Priority.SOMETIMES);
		GridPane.setHgrow(this.zeroPointOffsetUncertaintyField, Priority.ALWAYS);
		
		GridPane.setHgrow(uncertaintyTypeBLabel, Priority.SOMETIMES);
		GridPane.setHgrow(this.squareRootDistanceDependentUncertaintyField, Priority.ALWAYS);
		
		GridPane.setHgrow(uncertaintyTypeCLabel, Priority.SOMETIMES);
		GridPane.setHgrow(this.distanceDependentUncertaintyField, Priority.ALWAYS);
		
		gridPane.add(uncertaintyTypeALabel, 0, 0);
		gridPane.add(this.zeroPointOffsetUncertaintyField, 1, 0);

		gridPane.add(uncertaintyTypeBLabel, 0, 1);
		gridPane.add(this.squareRootDistanceDependentUncertaintyField, 1, 1);

		gridPane.add(uncertaintyTypeCLabel, 0, 2);
		gridPane.add(this.distanceDependentUncertaintyField, 1, 2);

		TitledPane uncertaintiesTitledPane = this.createTitledPane(i18n.getString("UIObservationPropertiesPane.uncertainty.title", "Uncertainties"));
		uncertaintiesTitledPane.setContent(gridPane);
		return uncertaintiesTitledPane;
	}

	private Node createCongruenceAnalysisPane() {
		GridPane gridPane = this.createGridPane();

		ToggleGroup group = new ToggleGroup();

		this.referenceEpochRadioButton = this.createRadioButton(i18n.getString("UIObservationPropertiesPane.congruenceanalysis.referenceepoch.label", "Reference epoch"), 
				i18n.getString("UIObservationPropertiesPane.congruenceanalysis.referenceepoch.tooltip", "Selected, if group is refer to reference epoch"), group, true, Epoch.REFERENCE);

		this.controlEpochRadioButton = this.createRadioButton(i18n.getString("UIObservationPropertiesPane.congruenceanalysis.controlepoch.label", "Control epoch"), 
				i18n.getString("UIObservationPropertiesPane.congruenceanalysis.controlepoch.tooltip", "Selected, if group is refer to control epoch"), group, false, Epoch.CONTROL);

		gridPane.add(this.referenceEpochRadioButton, 0, 0);
		gridPane.add(this.controlEpochRadioButton,   0, 1);

		TitledPane congruenceAnalysisTitledPane = this.createTitledPane(i18n.getString("UIObservationPropertiesPane.congruenceanalysis.title", "Congruence analysis"));
		congruenceAnalysisTitledPane.setContent(gridPane);
		return congruenceAnalysisTitledPane;
	}
	
	private void init() {
		VBox content = new VBox();
		content.getChildren().addAll(
				this.createUncertaintiesPane(),
				this.createAdditionalParametersPane(),
				this.createCongruenceAnalysisPane()
				);
		
		this.reset();

		ScrollPane scroller = new ScrollPane(content);
		scroller.setPadding(new Insets(20, 50, 20, 50)); // oben, links, unten, rechts
		scroller.setFitToHeight(true);
		scroller.setFitToWidth(true);
		
		this.propertiesNode = scroller;
	}
	
	private DoubleTextField createDoubleTextField(double value, CellValueType type, boolean displayUnit, ValueSupport valueSupport, String tooltipText, Object userData) {
		DoubleTextField field = new DoubleTextField(value, type, displayUnit, valueSupport);
		field.setTooltip(new Tooltip(tooltipText));
		field.setMinWidth(150);
		field.setMaxWidth(250);
		field.setUserData(userData);
		field.numberProperty().addListener(new NumberChangeListener(field));
		return field;
	}

	private CheckBox createCheckBox(String label, String tooltipText, boolean selected, Object userData) {
		CheckBox checkBox = new CheckBox(label);
		checkBox.setTooltip(new Tooltip(tooltipText));
		checkBox.setMinWidth(Control.USE_PREF_SIZE);
		checkBox.setSelected(selected);
		checkBox.setUserData(userData);
		checkBox.selectedProperty().addListener(new BooleanChangeListener(checkBox));
		return checkBox;
	}
	
	private RadioButton createRadioButton(String label, String tooltipText, ToggleGroup group, boolean selected, Object userData) {
		RadioButton radioButton = new RadioButton(label);
		radioButton.setTooltip(new Tooltip(tooltipText));
		radioButton.setMinWidth(Control.USE_PREF_SIZE);
		radioButton.setToggleGroup(group);
		radioButton.setSelected(selected);
		radioButton.setUserData(userData);
		radioButton.selectedProperty().addListener(new BooleanChangeListener(radioButton));
		return radioButton;
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
	
	private void saveUncertainties(ObservationGroupUncertaintyType uncertaintyType) {
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
				SQLManager.getInstance().saveUncertainty(uncertaintyType, value.doubleValue(), this.selectedObservationItemValues);
			}
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}
	
	private void saveAdditionalParameter(ParameterType parameterType) {
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
				SQLManager.getInstance().saveAdditionalParameter(parameterType, enable, value.doubleValue(), this.selectedObservationItemValues);
			}
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}
	
	private void saveEpoch() {
		try {
			if (this.selectedObservationItemValues != null && this.selectedObservationItemValues.length > 0)
				SQLManager.getInstance().saveEpoch(this.referenceEpochRadioButton.isSelected(), this.selectedObservationItemValues);
		} catch (SQLException e) {
			this.setReferenceEpoch(!this.referenceEpochRadioButton.isSelected());
			e.printStackTrace();
		}
	}
}