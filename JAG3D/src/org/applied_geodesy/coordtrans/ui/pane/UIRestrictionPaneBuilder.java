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

package org.applied_geodesy.coordtrans.ui.pane;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.applied_geodesy.adjustment.transformation.ParameterRestrictionType;
import org.applied_geodesy.adjustment.transformation.Transformation;
import org.applied_geodesy.adjustment.transformation.TransformationChangeListener;
import org.applied_geodesy.adjustment.transformation.TransformationEvent;
import org.applied_geodesy.adjustment.transformation.TransformationEvent.TransformationEventType;
import org.applied_geodesy.adjustment.transformation.TransformationType;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.coordtrans.ui.utils.UiUtil;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class UIRestrictionPaneBuilder implements TransformationChangeListener {
	private enum TransformationPropertyType {
		CONGRUENT,
		SIMILAR,
		AFFINE
	}
	
	private class RestrictionChangeListener implements ChangeListener<Boolean> {
		private final CheckBox checkBox;
		
		private RestrictionChangeListener(CheckBox checkBox) {
			this.checkBox = checkBox;
		}
		
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (this.checkBox.getUserData() instanceof ParameterRestrictionType) {
				handleSelection((ParameterRestrictionType)this.checkBox.getUserData(), this.checkBox.isSelected());
			}
		}
	}
	
	private class TransformationPropertyMenuEventHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() instanceof MenuItem) {
				MenuItem menuItem = (MenuItem)event.getSource();
				if (menuItem.getUserData() instanceof TransformationPropertyType) {
					TransformationPropertyType transformationPropertyType = (TransformationPropertyType)menuItem.getUserData();
					switch(transformationPropertyType) {
					case CONGRUENT:
						setCongruentProperty();
						break;
					case SIMILAR:
						setSimilarProperty();
						break;
					case AFFINE:
						setAffineProperty();
						break;
					}
				}
			}
		}
	}
	
	private static UIRestrictionPaneBuilder restrictionPaneBuilder = new UIRestrictionPaneBuilder();
	private Transformation transformation = null;
	
	private I18N i18n = I18N.getInstance();
	
	private Node restrictionNode = null;
	private Map<ParameterRestrictionType, CheckBox> parameterRestrictionCheckboxes = new HashMap<ParameterRestrictionType, CheckBox>();
	private TransformationType lastTransformationType = null;
	private MenuBar transformationMenuBar = new MenuBar();
	
	private UIRestrictionPaneBuilder() {
		this.init();
	}

	public static UIRestrictionPaneBuilder getInstance() {
		return restrictionPaneBuilder;
	}
	
	private void init() {
		if (this.restrictionNode != null)
			return;
		
		this.restrictionNode = this.createPane();
		this.setDisable();
	}
	
	public Node getNode() {
		return this.restrictionNode;
	}
	
	private Node createPane() {
		Node parameterTitledPane = this.createParameterRestrictionPane();
		Node identScalePane      = this.createIdentScalePane();
		
		GridPane gridPane = UiUtil.createGridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(20);
		gridPane.setPadding(new Insets(20,15,20,15)); // oben, recht, unten, links
		gridPane.setAlignment(Pos.BASELINE_CENTER);

		Region leftSpacer = new Region();
		leftSpacer.setPrefSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		leftSpacer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		Region rightSpacer = new Region();
		rightSpacer.setPrefSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		rightSpacer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		GridPane.setHgrow(leftSpacer,          Priority.ALWAYS);
		GridPane.setHgrow(rightSpacer,         Priority.ALWAYS);
		GridPane.setHgrow(parameterTitledPane, Priority.SOMETIMES);
		GridPane.setHgrow(identScalePane,      Priority.SOMETIMES);
		
		int row = 0;		
		int col = 0;
		gridPane.add(leftSpacer,          col++, row,   1, 1);
		gridPane.add(parameterTitledPane, col++, row,   1, 1);
		gridPane.add(rightSpacer,         col++, row++, 1, 1);
		
		col = 1;
		gridPane.add(identScalePane,      col++, row++,   1, 1);

		ScrollPane scrollPane = new ScrollPane(gridPane);
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		return scrollPane;
	}
	
	private Node createParameterRestrictionPane() {
		TransformationPropertyMenuEventHandler transformationPropertyEventHandler = new TransformationPropertyMenuEventHandler();

		CheckBox shiftXCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.shift.x.label", "Shift tx"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.shift.x.tooltip", "Checked, if x-shift is an unknown parameter to be estimated"), ParameterRestrictionType.FIXED_SHIFT_X, this.parameterRestrictionCheckboxes);
		CheckBox shiftYCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.shift.y.label", "Shift ty"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.shift.y.tooltip", "Checked, if y-shift is an unknown parameter to be estimated"), ParameterRestrictionType.FIXED_SHIFT_Y, this.parameterRestrictionCheckboxes);
		CheckBox shiftZCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.shift.z.label", "Shift tz"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.shift.z.tooltip", "Checked, if z-shift is an unknown parameter to be estimated"), ParameterRestrictionType.FIXED_SHIFT_Z, this.parameterRestrictionCheckboxes);
		
		CheckBox scaleXCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.x.label", "Scale mx"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.x.tooltip", "Checked, if x-scale is an unknown parameter to be estimated"), ParameterRestrictionType.FIXED_SCALE_X, this.parameterRestrictionCheckboxes);
		CheckBox scaleYCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.y.label", "Scale my"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.y.tooltip", "Checked, if y-scale is an unknown parameter to be estimated"), ParameterRestrictionType.FIXED_SCALE_Y, this.parameterRestrictionCheckboxes);
		CheckBox scaleZCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.z.label", "Scale mz"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.z.tooltip", "Checked, if z-scale is an unknown parameter to be estimated"), ParameterRestrictionType.FIXED_SCALE_Z, this.parameterRestrictionCheckboxes);
		
		CheckBox shearXCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.shear.x.label", "Shear sx"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.shear.x.tooltip", "Checked, if x-shear is an unknown parameter to be estimated"), ParameterRestrictionType.FIXED_SHEAR_X, this.parameterRestrictionCheckboxes);
		CheckBox shearYCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.shear.y.label", "Shear sy"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.shear.y.tooltip", "Checked, if y-shear is an unknown parameter to be estimated"), ParameterRestrictionType.FIXED_SHEAR_Y, this.parameterRestrictionCheckboxes);
		CheckBox shearZCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.shear.z.label", "Shear sz"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.shear.z.tooltip", "Checked, if z-shear is an unknown parameter to be estimated"), ParameterRestrictionType.FIXED_SHEAR_Z, this.parameterRestrictionCheckboxes);
		
		CheckBox rotationXCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.rotation.x.label", "Rotation rx"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.rotation.x.tooltip", "Checked, if x-rotation is an unknown parameter to be estimated"), ParameterRestrictionType.FIXED_ROTATION_X, this.parameterRestrictionCheckboxes);
		CheckBox rotationYCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.rotation.y.label", "Rotation ry"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.rotation.y.tooltip", "Checked, if y-rotation is an unknown parameter to be estimated"), ParameterRestrictionType.FIXED_ROTATION_Y, this.parameterRestrictionCheckboxes);
		CheckBox rotationZCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.rotation.z.label", "Rotation rz"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.rotation.z.tooltip", "Checked, if z-rotation is an unknown parameter to be estimated"), ParameterRestrictionType.FIXED_ROTATION_Z, this.parameterRestrictionCheckboxes);
		
		Region spacer = new Region();
		spacer.setPrefSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		spacer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		HBox shiftNode = new HBox(50);
		shiftNode.getChildren().setAll(shiftXCheckBox, shiftYCheckBox, shiftZCheckBox);
		TitledPane shiftPane = UiUtil.createTitledPane(
				i18n.getString("UIRestrictionPaneBuilder.restriction.type.shift.title", "Shift parameters"), 
				i18n.getString("UIRestrictionPaneBuilder.restriction.type.shift.tooltip", "Select shift parameters to be estimated"), 
				shiftNode
		);
		shiftPane.setCollapsible(false);
		
		HBox scaleNode = new HBox(50);
		scaleNode.getChildren().setAll(scaleXCheckBox, scaleYCheckBox, scaleZCheckBox);
		TitledPane scalePane = UiUtil.createTitledPane(
				i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.title", "Scale parameters"), 
				i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.tooltip", "Select scale parameters to be estimated"), 
				scaleNode
		);
		scalePane.setCollapsible(false);
		
		
		HBox shearNode = new HBox(50);
		shearNode.getChildren().setAll(shearXCheckBox, shearYCheckBox, shearZCheckBox);
		TitledPane shearPane = UiUtil.createTitledPane(
				i18n.getString("UIRestrictionPaneBuilder.restriction.type.shear.title", "Shear parameters"), 
				i18n.getString("UIRestrictionPaneBuilder.restriction.type.shear.tooltip", "Select shear parameters to be estimated"), 
				shearNode
		);
		shearPane.setCollapsible(false);
		
		HBox rotationNode = new HBox(50);
		rotationNode.getChildren().setAll(rotationXCheckBox, rotationYCheckBox, rotationZCheckBox);
		TitledPane rotationPane = UiUtil.createTitledPane(
				i18n.getString("UIRestrictionPaneBuilder.restriction.type.rotation.title", "Rotation parameters"), 
				i18n.getString("UIRestrictionPaneBuilder.restriction.type.rotation.tooltip", "Select rotation parameters to be estimated"), 
				rotationNode
		);
		rotationPane.setCollapsible(false);

				
		GridPane parameterGridPane = UiUtil.createGridPane();
		parameterGridPane.setHgap(50);
		parameterGridPane.setVgap(15);
		parameterGridPane.setAlignment(Pos.BASELINE_CENTER);
		
		int row = 0;
		int col = 0;
		parameterGridPane.add(shiftXCheckBox,    col++, row,   1, 1);
		parameterGridPane.add(scaleXCheckBox,    col++, row,   1, 1);
		parameterGridPane.add(rotationXCheckBox, col++, row,   1, 1);
		parameterGridPane.add(shearXCheckBox,    col++, row++, 1, 1);

		col = 0;
		parameterGridPane.add(shiftYCheckBox,    col++, row,   1, 1);
		parameterGridPane.add(scaleYCheckBox,    col++, row,   1, 1);
		parameterGridPane.add(rotationYCheckBox, col++, row,   1, 1);
		parameterGridPane.add(shearYCheckBox,    col++, row++, 1, 1);
		
		col = 0;
		parameterGridPane.add(shiftZCheckBox,    col++, row,   1, 1);
		parameterGridPane.add(scaleZCheckBox,    col++, row,   1, 1);
		parameterGridPane.add(rotationZCheckBox, col++, row,   1, 1);
		parameterGridPane.add(shearZCheckBox,    col++, row++, 1, 1);

		TitledPane titledPane = UiUtil.createTitledPane(
				i18n.getString("UIRestrictionPaneBuilder.restriction.parameter.title", "Transformation parameters"), 
				i18n.getString("UIRestrictionPaneBuilder.restriction.parameter.tooltip", "Select transformation parameters to be estimated"), 
				parameterGridPane
		);
		titledPane.setCollapsible(false);

		Menu menu = new Menu();
		Label label = new Label(i18n.getString("UIRestrictionPaneBuilder.transformation.property.label", "\u25BC"));
		label.setTooltip(new Tooltip(i18n.getString("UIRestrictionPaneBuilder.transformation.property.tooltip", "Select transformation properties")));
		menu.setGraphic(label); 
		menu.getItems().addAll(
				getMenuItem(i18n.getString("UIRestrictionPaneBuilder.transformation.property.congruent.label", "Congruent"), transformationPropertyEventHandler, TransformationPropertyType.CONGRUENT),
				getMenuItem(i18n.getString("UIRestrictionPaneBuilder.transformation.property.similar.label", "Similar"), transformationPropertyEventHandler, TransformationPropertyType.SIMILAR),
				getMenuItem(i18n.getString("UIRestrictionPaneBuilder.transformation.property.affine.label", "Affine"), transformationPropertyEventHandler, TransformationPropertyType.AFFINE)
		);
		this.transformationMenuBar.setBackground(null);
		this.transformationMenuBar.setPadding(new Insets(0,5,0,0));
		this.transformationMenuBar.getMenus().add(menu);
		this.transformationMenuBar.setDisable(true);
		
		HBox header = new HBox();
		header.setPadding(new Insets(0));
		header.setAlignment(Pos.CENTER_LEFT);
		header.setSpacing(0);
		HBox.setHgrow(spacer, Priority.ALWAYS);
		header.setSpacing(0);
		header.getChildren().setAll(titledPane.getGraphic(), spacer, this.transformationMenuBar);
		titledPane.setGraphic(header);
//		header.minWidthProperty().bind(titledPane.widthProperty().subtract(this.transformationMenuBar.widthProperty()));
		
		return titledPane;
	}
	
	private Node createIdentScalePane() {
		CheckBox scaleXYCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.xy.label", "Scales mx = my"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.xy.tooltip", "Checked, if x-scale is identical with y-scale"), ParameterRestrictionType.IDENTICAL_SCALE_XY, this.parameterRestrictionCheckboxes);
		CheckBox scaleXZCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.xz.label", "Scales mx = mz"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.xz.tooltip", "Checked, if x-scale is identical with z-scale"), ParameterRestrictionType.IDENTICAL_SCALE_XZ, this.parameterRestrictionCheckboxes);
		CheckBox scaleYZCheckBox = createRestrictionCheckBox(i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.yz.label", "Scales my = mz"), i18n.getString("UIRestrictionPaneBuilder.restriction.type.scale.yz.tooltip", "Checked, if y-scale is identical with z-scale"), ParameterRestrictionType.IDENTICAL_SCALE_YZ, this.parameterRestrictionCheckboxes);
			
		HBox identScaleNode = new HBox(50);
		identScaleNode.getChildren().setAll(scaleXYCheckBox, scaleXZCheckBox, scaleYZCheckBox);

		TitledPane titledPane = UiUtil.createTitledPane(
				i18n.getString("UIRestrictionPaneBuilder.restriction.type.scales.title", "Scale conditions"), 
				i18n.getString("UIRestrictionPaneBuilder.restriction.type.scales.tooltip", "Select scale conditions"), 
				identScaleNode
		);
		titledPane.setCollapsible(false);
		
		return titledPane;
	}
	
	private void setDisable() {
		Set<ParameterRestrictionType> supportedRestrictionTypes = this.transformation == null ? Collections.<ParameterRestrictionType>emptySet() : this.transformation.getSupportedParameterRestrictions().keySet();
		for (CheckBox checkBox : this.parameterRestrictionCheckboxes.values()) {
			checkBox.setDisable(!supportedRestrictionTypes.contains(checkBox.getUserData()));
//			checkBox.setSelected(!supportedRestrictionTypes.contains(checkBox.getUserData()));
		}
	}
	
	private CheckBox createRestrictionCheckBox(String label, String tooltip, ParameterRestrictionType restrictionType, Map<ParameterRestrictionType, CheckBox> checkBoxMap) {
		CheckBox checkBox = UiUtil.createCheckBox(label, tooltip);
		checkBox.setUserData(restrictionType);
		checkBox.setSelected(true);
		checkBox.selectedProperty().addListener(new RestrictionChangeListener(checkBox));
		checkBoxMap.put(restrictionType, checkBox);
		
		return checkBox;
	}
	
	private MenuItem getMenuItem(String name, TransformationPropertyMenuEventHandler eventHandler, TransformationPropertyType propertyType) {
		MenuItem item = new MenuItem(name);
		item.setOnAction(eventHandler);
		item.setUserData(propertyType);
		return item;
	}

	@Override
	public void transformationChanged(TransformationEvent evt) {
		if (this.transformation != null)
			this.lastTransformationType = this.transformation.getTransformationEquations().getTransformationType();
		
		if (evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_REMOVED)
			this.transformation = null;
		else if (evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_ADDED)
			this.transformation = evt.getSource();	

		this.transformationMenuBar.setDisable(evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_REMOVED);
		this.setDisable();
		
		if (this.lastTransformationType == null || this.transformation == null || this.lastTransformationType != this.transformation.getTransformationEquations().getTransformationType())
			this.setSimilarProperty();
		//else if(this.transformation != null && lastTransformationType == this.transformation.getTransformationEquations().getTransformationType()) {
		if (this.transformation != null) {
			for (CheckBox checkBox : this.parameterRestrictionCheckboxes.values())
				this.handleSelection((ParameterRestrictionType)checkBox.getUserData(), checkBox.isSelected());
		}
	}
	
	private void setAffineProperty() {
		final Set<ParameterRestrictionType> shearRestrictionTypes = Set.of(
				ParameterRestrictionType.FIXED_SHEAR_X,
				ParameterRestrictionType.FIXED_SHEAR_Y,
				ParameterRestrictionType.FIXED_SHEAR_Z
				);
		
		final Set<ParameterRestrictionType> scaleRestrictionTypes = Set.of(
				ParameterRestrictionType.IDENTICAL_SCALE_XY,
				ParameterRestrictionType.IDENTICAL_SCALE_XZ,
				ParameterRestrictionType.IDENTICAL_SCALE_YZ
				);
		
		this.setSimilarProperty();
		
		for (ParameterRestrictionType restrictionType : shearRestrictionTypes) {
			CheckBox checkBox = this.parameterRestrictionCheckboxes.get(restrictionType);
			if (!checkBox.isDisable() && checkBox.getUserData() instanceof ParameterRestrictionType)
				checkBox.setSelected(true);
		}
		
		for (ParameterRestrictionType restrictionType : scaleRestrictionTypes) {
			CheckBox checkBox = this.parameterRestrictionCheckboxes.get(restrictionType);
			if (!checkBox.isDisable() && checkBox.getUserData() instanceof ParameterRestrictionType)
				checkBox.setSelected(false);
		}
	}
	
	private void setSimilarProperty() {
		final Set<ParameterRestrictionType> scaleRestrictionTypes = Set.of(
				ParameterRestrictionType.FIXED_SCALE_X,
				ParameterRestrictionType.FIXED_SCALE_Y,
				ParameterRestrictionType.FIXED_SCALE_Z,

				ParameterRestrictionType.IDENTICAL_SCALE_XY,
				ParameterRestrictionType.IDENTICAL_SCALE_XZ,
				ParameterRestrictionType.IDENTICAL_SCALE_YZ
				);
		
		this.setCongruentProperty();
		
		for (ParameterRestrictionType restrictionType : scaleRestrictionTypes) {
			CheckBox checkBox = this.parameterRestrictionCheckboxes.get(restrictionType);
			if (!checkBox.isDisable() && checkBox.getUserData() instanceof ParameterRestrictionType)
				checkBox.setSelected(true);
		}
	}
	
	private void setCongruentProperty() {
		for (CheckBox checkBox : this.parameterRestrictionCheckboxes.values()) {
			if (!checkBox.isDisable() && checkBox.getUserData() instanceof ParameterRestrictionType) {
				ParameterRestrictionType restrictionType = (ParameterRestrictionType)checkBox.getUserData();
				switch (restrictionType) {
				
				case FIXED_SCALE_X:
				case FIXED_SCALE_Y:
				case FIXED_SCALE_Z:
					
				case FIXED_SHEAR_X:
				case FIXED_SHEAR_Y:
				case FIXED_SHEAR_Z:
				
				case IDENTICAL_SCALE_XY:
				case IDENTICAL_SCALE_XZ:
				case IDENTICAL_SCALE_YZ:
					checkBox.setSelected(false);
					break;
				default:
					checkBox.setSelected(true);
					break;
				}
			}
		}
	}
	
	private void handleSelection(ParameterRestrictionType parameterRestrictionType, boolean selected) {
		if (this.transformation == null)
			return;

		this.transformation.removeRestriction(parameterRestrictionType);
		
		switch (parameterRestrictionType) {
		case IDENTICAL_SCALE_XY:
			
			if (selected) {
				this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.FIXED_SCALE_X).setSelected(true);
				this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.FIXED_SCALE_Y).setSelected(true);
			}
			
			break;
		case IDENTICAL_SCALE_XZ:
			
			if (selected) {
				this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.FIXED_SCALE_X).setSelected(true);
				this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.FIXED_SCALE_Z).setSelected(true);
			}
			
			break;
		case IDENTICAL_SCALE_YZ:
			
			if (selected) {
				this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.FIXED_SCALE_Y).setSelected(true);
				this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.FIXED_SCALE_Z).setSelected(true);
			}
			
			break;
			
		case FIXED_SCALE_X:
			
			if (!selected) {
				this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.IDENTICAL_SCALE_XY).setSelected(false);
				this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.IDENTICAL_SCALE_XZ).setSelected(false);
				
				this.transformation.addRestriction(parameterRestrictionType);
			}
				
			break;
			
		case FIXED_SCALE_Y:
			if (!selected) {
				this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.IDENTICAL_SCALE_XY).setSelected(false);
				this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.IDENTICAL_SCALE_YZ).setSelected(false);
				
				this.transformation.addRestriction(parameterRestrictionType);
			}
				
			break;
			
		case FIXED_SCALE_Z:
			
			if (!selected) {
				this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.IDENTICAL_SCALE_XZ).setSelected(false);
				this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.IDENTICAL_SCALE_YZ).setSelected(false);
				
				this.transformation.addRestriction(parameterRestrictionType);
			}
				
			break;
			
		default:
			
			if (!selected) 
				this.transformation.addRestriction(parameterRestrictionType);
			
			break;
		}
		
		if (parameterRestrictionType == ParameterRestrictionType.IDENTICAL_SCALE_XY || 
				parameterRestrictionType == ParameterRestrictionType.IDENTICAL_SCALE_XZ || 
				parameterRestrictionType == ParameterRestrictionType.IDENTICAL_SCALE_YZ) {
			
			CheckBox identScaleXYCheckBox = this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.IDENTICAL_SCALE_XY);
			CheckBox identScaleXZCheckBox = this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.IDENTICAL_SCALE_XZ);
			CheckBox identScaleYZCheckBox = this.parameterRestrictionCheckboxes.get(ParameterRestrictionType.IDENTICAL_SCALE_YZ);
			
			if (identScaleXYCheckBox.isSelected() && identScaleXZCheckBox.isSelected() && identScaleYZCheckBox.isSelected()) {
				this.transformation.removeRestriction(ParameterRestrictionType.IDENTICAL_SCALE_XY);
				this.transformation.removeRestriction(ParameterRestrictionType.IDENTICAL_SCALE_XZ);
				this.transformation.removeRestriction(ParameterRestrictionType.IDENTICAL_SCALE_YZ);
				
				this.transformation.addRestriction(ParameterRestrictionType.IDENTICAL_SCALE_XY);
				this.transformation.addRestriction(ParameterRestrictionType.IDENTICAL_SCALE_XZ);
			}
			else if (selected) {
				this.transformation.addRestriction(parameterRestrictionType);
			}
		}
	}
}
