package org.applied_geodesy.jag3d.ui.propertiespane;

import java.sql.SQLException;

import org.applied_geodesy.adjustment.network.congruence.strain.RestrictionType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.tree.CongruenceAnalysisTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.util.i18.I18N;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class UICongruenceAnalysisPropertiesPane {

	private class BooleanChangeListener implements ChangeListener<Boolean> {
		private final CheckBox button;
		
		private BooleanChangeListener(CheckBox button) {
			this.button = button;
		}
		
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (!ignoreValueUpdate && this.button.getUserData() != null) {
				if (this.button.getUserData() instanceof RestrictionType) {
					RestrictionType paramType = (RestrictionType)this.button.getUserData();
					saveStrainParameter(paramType, this.button.isSelected());
				}
			}
		}
	}

	private static I18N i18n = I18N.getInstance();
	private Node propertiesNode = null;
	private final TreeItemType type;
	
	private CheckBox translationXCheckBox;
	private CheckBox translationYCheckBox;
	private CheckBox translationZCheckBox;
	
	private CheckBox scaleXCheckBox;
	private CheckBox scaleYCheckBox;
	private CheckBox scaleZCheckBox;
	
	private CheckBox rotationXCheckBox;
	private CheckBox rotationYCheckBox;
	private CheckBox rotationZCheckBox;
	
	private CheckBox shearXCheckBox;
	private CheckBox shearYCheckBox;
	private CheckBox shearZCheckBox;
	
	private boolean ignoreValueUpdate = false;
	private CongruenceAnalysisTreeItemValue selectedCongruenceAnalysisItemValues[] = null;
	
	UICongruenceAnalysisPropertiesPane(TreeItemType type) {
		switch(type) {
		case CONGRUENCE_ANALYSIS_1D_LEAF:
		case CONGRUENCE_ANALYSIS_2D_LEAF:
		case CONGRUENCE_ANALYSIS_3D_LEAF:
			this.type = type;
			this.init();
			break;
		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Error, unsupported item type " + type);		
		}
	}
	
	public void setTreeItemValue(CongruenceAnalysisTreeItemValue... selectedCongruenceAnalysisItemValues) {
		this.selectedCongruenceAnalysisItemValues = selectedCongruenceAnalysisItemValues;
	}

	public Node getNode() {
		return this.propertiesNode;
	}
	
	public void reset() {
		this.setTranslationY(false);
		this.setTranslationX(false);
		this.setTranslationZ(false);
		
		this.setRotationY(false);
		this.setRotationX(false);
		this.setRotationZ(false);
		
		this.setScaleY(false);
		this.setScaleX(false);
		this.setScaleZ(false);
		
		this.setShearY(false);
		this.setShearX(false);
		this.setShearZ(false);
	}
	
	public boolean setStrainParameter(RestrictionType restrictionType, Boolean enable) {
		switch(restrictionType) {
		case FIXED_ROTATION_X:
			return this.setRotationX(enable);
		case FIXED_ROTATION_Y:
			return this.setRotationY(enable);
		case FIXED_ROTATION_Z:
			return this.setRotationZ(enable);
		case FIXED_SCALE_X:
			return this.setScaleX(enable);
		case FIXED_SCALE_Y:
			return this.setScaleY(enable);
		case FIXED_SCALE_Z:
			return this.setScaleZ(enable);
		case FIXED_SHEAR_X:
			return this.setShearX(enable);
		case FIXED_SHEAR_Y:
			return this.setShearY(enable);
		case FIXED_SHEAR_Z:
			return this.setShearZ(enable);
		case FIXED_TRANSLATION_X:
			return this.setTranslationX(enable);
		case FIXED_TRANSLATION_Y:
			return this.setTranslationY(enable);
		case FIXED_TRANSLATION_Z:
			return this.setTranslationZ(enable);
		default:
			return false;
		}
	}
	
	public boolean setTranslationX(Boolean enable) {
		if (this.translationXCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.translationXCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setTranslationY(Boolean enable) {
		if (this.translationYCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.translationYCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setTranslationZ(Boolean enable) {
		if (this.translationZCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.translationZCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setScaleX(Boolean enable) {
		if (this.scaleXCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.scaleXCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setScaleY(Boolean enable) {
		if (this.scaleYCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.scaleYCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setScaleZ(Boolean enable) {
		if (this.scaleZCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.scaleZCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setRotationX(Boolean enable) {
		if (this.rotationXCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.rotationXCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setRotationY(Boolean enable) {
		if (this.rotationYCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.rotationYCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setRotationZ(Boolean enable) {
		if (this.rotationZCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.rotationZCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}

	public boolean setShearX(Boolean enable) {
		if (this.shearXCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.shearXCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setShearY(Boolean enable) {
		if (this.shearYCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.shearYCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setShearZ(Boolean enable) {
		if (this.shearZCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.shearZCheckBox.setSelected(enable != null && enable == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}

	private Node createTranslationPane(RestrictionType[] restrictionTypes) {
		GridPane gridPane = this.createGridPane();

		int row = 0;

		for (RestrictionType restrictionType : restrictionTypes) {
			CheckBox box = null;
			
			switch (restrictionType) {
			case FIXED_TRANSLATION_Y:
				box = this.translationYCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.translation.y.label", "Translation y"), i18n.getString("UIObservationPropertiesPane.translation.y.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				break;
			case FIXED_TRANSLATION_X:
				box = this.translationXCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.translation.x.label", "Translation x"), i18n.getString("UIObservationPropertiesPane.translation.x.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				break;
			case FIXED_TRANSLATION_Z:
				box = this.translationZCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.translation.z.label", "Translation z"), i18n.getString("UIObservationPropertiesPane.translation.z.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				break;
			default:
				continue;
			}
			
			if (box != null)
				gridPane.add(box,   0, row++);
		}
		
		if (row == 0)
			return null;
		
		TitledPane parametersTitledPane = this.createTitledPane(i18n.getString("UIObservationPropertiesPane.translation.title", "Translation parameters"));
		parametersTitledPane.setContent(gridPane);
		return parametersTitledPane;
	}
	
	private Node createRotationPane(RestrictionType[] restrictionTypes) {
		GridPane gridPane = this.createGridPane();

		int row = 0;

		for (RestrictionType restrictionType : restrictionTypes) {
			CheckBox box = null;
			
			switch (restrictionType) {
			case FIXED_ROTATION_Y:
				box = this.rotationYCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.rotation.y.label", "Rotation y"), i18n.getString("UIObservationPropertiesPane.rotation.y.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				break;
			case FIXED_ROTATION_X:
				box = this.rotationXCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.rotation.x.label", "Rotation x"), i18n.getString("UIObservationPropertiesPane.rotation.x.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				break;
			case FIXED_ROTATION_Z:
				box = this.rotationZCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.rotation.z.label", "Rotation z"), i18n.getString("UIObservationPropertiesPane.rotation.z.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				break;
			default:
				continue;
			}
				
			if (box != null)
				gridPane.add(box,   0, row++);
		}
		
		if (row == 0)
			return null;
		
		TitledPane parametersTitledPane = this.createTitledPane(i18n.getString("UIObservationPropertiesPane.rotation.title", "Rotation parameters"));
		parametersTitledPane.setContent(gridPane);
		return parametersTitledPane;
	}
	
	private Node createShearPane(RestrictionType[] restrictionTypes) {
		GridPane gridPane = this.createGridPane();

		int row = 0;

		for (RestrictionType restrictionType : restrictionTypes) {
			CheckBox box = null;
			
			switch (restrictionType) {
			case FIXED_SHEAR_Y:
				box = this.shearYCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.shear.y.label", "Shear y"), i18n.getString("UIObservationPropertiesPane.shear.y.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				break;
			case FIXED_SHEAR_X:
				box = this.shearXCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.shear.x.label", "Shear x"), i18n.getString("UIObservationPropertiesPane.shear.x.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				break;
			case FIXED_SHEAR_Z:
				box = this.shearZCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.shear.z.label", "Shear z"), i18n.getString("UIObservationPropertiesPane.shear.z.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				break;
			default:
				continue;
			}
			
			if (box != null)
				gridPane.add(box,   0, row++);
		}

		if (row == 0)
			return null;

		TitledPane parametersTitledPane = this.createTitledPane(i18n.getString("UIObservationPropertiesPane.shear.title", "Shear parameters"));
		parametersTitledPane.setContent(gridPane);
		return parametersTitledPane;
	}
	
	private Node createScalePane(RestrictionType[] restrictionTypes) {
		GridPane gridPane = this.createGridPane();

		int row = 0;

		for (RestrictionType restrictionType : restrictionTypes) {
			CheckBox box = null;

			switch (restrictionType) {
			case FIXED_SCALE_Y:
				box = this.scaleYCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.scale.y.label", "Scale y"), i18n.getString("UIObservationPropertiesPane.scale.y.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				break;
			case FIXED_SCALE_X:
				box = this.scaleXCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.scale.x.label", "Scale x"), i18n.getString("UIObservationPropertiesPane.scale.x.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				break;
			case FIXED_SCALE_Z:
				box = this.scaleZCheckBox = this.createCheckBox(i18n.getString("UIObservationPropertiesPane.scale.z.label", "Scale z"), i18n.getString("UIObservationPropertiesPane.scale.z.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				break;
			default:
				continue;
			}

			if (box != null)
				gridPane.add(box,   0, row++);
		}

		if (row == 0)
			return null;
		
		TitledPane parametersTitledPane = this.createTitledPane(i18n.getString("UIObservationPropertiesPane.scale.title", "Scale parameters"));
		parametersTitledPane.setContent(gridPane);
		return parametersTitledPane;
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
	
	private void init() {
		
		RestrictionType[] parameterTypes = CongruenceAnalysisTreeItemValue.getRestrictionTypes(this.type);
		
		VBox content = new VBox();
		
		Node translationPane = this.createTranslationPane(parameterTypes);
		Node rotationPane    = this.createRotationPane(parameterTypes);
		Node shearPane       = this.createShearPane(parameterTypes);
		Node scalePane       = this.createScalePane(parameterTypes);
		
		this.reset();
		
		if (translationPane != null)
			content.getChildren().add(translationPane);
		
		if (rotationPane != null)
			content.getChildren().add(rotationPane);
		
		if (shearPane != null)
			content.getChildren().add(shearPane);
		
		if (scalePane != null)
			content.getChildren().add(scalePane);

		ScrollPane scroller = new ScrollPane(content);
		scroller.setPadding(new Insets(20, 50, 20, 50)); // oben, links, unten, rechts
		scroller.setFitToHeight(true);
		scroller.setFitToWidth(true);
		
		this.propertiesNode = scroller;
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

	private void saveStrainParameter(RestrictionType parameterType, boolean selected) {
		try {
			if (this.selectedCongruenceAnalysisItemValues != null && this.selectedCongruenceAnalysisItemValues.length > 0)
				SQLManager.getInstance().saveStrainParameter(parameterType, selected, this.selectedCongruenceAnalysisItemValues);
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}
}
