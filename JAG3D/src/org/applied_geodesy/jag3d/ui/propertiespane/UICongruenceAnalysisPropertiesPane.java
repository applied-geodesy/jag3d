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
import java.util.Map;

import org.applied_geodesy.adjustment.network.congruence.strain.RestrictionType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dialog.OptionDialog;
import org.applied_geodesy.jag3d.ui.tree.CongruenceAnalysisTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.i18.I18N;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

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
					switch(paramType) {
					case IDENT_SCALES_XY:
					case IDENT_SCALES_XZ:
					case IDENT_SCALES_YZ:
						save(paramType, !this.button.isSelected());
						break;
					default:
						save(paramType,  this.button.isSelected());
						break;
					
					}
				}
			}
		}
	}
	
	private class BoundedRestrictionChangeListener implements ChangeListener<Boolean> {
		private final CheckBox dependendButton1, dependendButton2;
		
		private BoundedRestrictionChangeListener(CheckBox dependendButton1, CheckBox dependendButton2) {
			this.dependendButton1  = dependendButton1;
			this.dependendButton2  = dependendButton2;
		}
		
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (newValue) {
				this.dependendButton1.setSelected(true);
				this.dependendButton2.setSelected(true);
			}
		}
	}
	
	private class DependendRestrictionChangeListener implements ChangeListener<Boolean> {
		private final CheckBox boundedButton;
		
		private DependendRestrictionChangeListener(CheckBox boundedButton) {
			this.boundedButton = boundedButton;
		}
		
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (!newValue) {
				this.boundedButton.setSelected(false);
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
	
	private CheckBox translationXCheckBox;
	private CheckBox translationYCheckBox;
	private CheckBox translationZCheckBox;
	
	private CheckBox scaleXCheckBox;
	private CheckBox scaleYCheckBox;
	private CheckBox scaleZCheckBox;
	
	private CheckBox scaleXYCheckBox;
	private CheckBox scaleXZCheckBox;
	private CheckBox scaleYZCheckBox;
	
	private CheckBox rotationXCheckBox;
	private CheckBox rotationYCheckBox;
	private CheckBox rotationZCheckBox;
	
	private CheckBox shearXCheckBox;
	private CheckBox shearYCheckBox;
	private CheckBox shearZCheckBox;
	
	private Map<Object, ProgressIndicator> databaseTransactionProgressIndicators = new HashMap<Object, ProgressIndicator>(10);
	private SequentialTransition sequentialTransition = new SequentialTransition();
	
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
		if (this.selectedCongruenceAnalysisItemValues != selectedCongruenceAnalysisItemValues) {
			this.reset();
			this.selectedCongruenceAnalysisItemValues = selectedCongruenceAnalysisItemValues;
		}
	}

	public Node getNode() {
		return this.propertiesNode;
	}
	
	private void reset() {
		this.sequentialTransition.stop();
		this.setProgressIndicatorsVisible(false);
		
		// set focus to panel to commit text field values and to force db transaction
		UITreeBuilder.getInstance().getTree().requestFocus();
//		if (this.propertiesNode != null)
//			this.propertiesNode.requestFocus();
		
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
		
		this.setScaleRestrictionXY(true);
		this.setScaleRestrictionXZ(true);
		this.setScaleRestrictionYZ(true);
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
		case IDENT_SCALES_XY:
			return this.setScaleRestrictionXY(enable);
		case IDENT_SCALES_XZ:
			return this.setScaleRestrictionXZ(enable);
		case IDENT_SCALES_YZ:
			return this.setScaleRestrictionYZ(enable);
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
	
	
	public boolean setScaleRestrictionYZ(Boolean enable) {
		if (this.scaleYZCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.scaleYZCheckBox.setSelected(enable != null && enable == Boolean.FALSE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setScaleRestrictionXY(Boolean enable) {
		if (this.scaleXYCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.scaleXYCheckBox.setSelected(enable != null && enable == Boolean.FALSE);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setScaleRestrictionXZ(Boolean enable) {
		if (this.scaleXZCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.scaleXZCheckBox.setSelected(enable != null && enable == Boolean.FALSE);
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
			ProgressIndicator progressIndicator = null;
			
			switch (restrictionType) {
			case FIXED_TRANSLATION_Y:
				
				box = this.translationYCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.translation.y.label", "Translation y"), i18n.getString("UICongruenceAnalysisPropertiesPane.translation.y.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			case FIXED_TRANSLATION_X:
				
				box = this.translationXCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.translation.x.label", "Translation x"), i18n.getString("UICongruenceAnalysisPropertiesPane.translation.x.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			case FIXED_TRANSLATION_Z:
				
				box = this.translationZCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.translation.z.label", "Translation z"), i18n.getString("UICongruenceAnalysisPropertiesPane.translation.z.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			default:
				continue;
			}
			
			if (box != null && progressIndicator != null) {
				gridPane.add(box, 0, row);
				gridPane.add(progressIndicator, 1, row++);
			}
		}
		
		if (row == 0)
			return null;
		
		TitledPane parametersTitledPane = this.createTitledPane(i18n.getString("UICongruenceAnalysisPropertiesPane.translation.title", "Translation parameters"));
		parametersTitledPane.setContent(gridPane);
		return parametersTitledPane;
	}
	
	private Node createRotationPane(RestrictionType[] restrictionTypes) {
		GridPane gridPane = this.createGridPane();

		int row = 0;

		for (RestrictionType restrictionType : restrictionTypes) {
			CheckBox box = null;
			ProgressIndicator progressIndicator = null;
			
			switch (restrictionType) {
			case FIXED_ROTATION_Y:
				
				box = this.rotationYCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.rotation.y.label", "Rotation y"), i18n.getString("UICongruenceAnalysisPropertiesPane.rotation.y.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			case FIXED_ROTATION_X:
				
				box = this.rotationXCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.rotation.x.label", "Rotation x"), i18n.getString("UICongruenceAnalysisPropertiesPane.rotation.x.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			case FIXED_ROTATION_Z:
				
				box = this.rotationZCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.rotation.z.label", "Rotation z"), i18n.getString("UICongruenceAnalysisPropertiesPane.rotation.z.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			default:
				continue;
			}
				
			if (box != null && progressIndicator != null) {
				gridPane.add(box, 0, row);
				gridPane.add(progressIndicator, 1, row++);
			}
		}
		
		if (row == 0)
			return null;
		
		TitledPane parametersTitledPane = this.createTitledPane(i18n.getString("UICongruenceAnalysisPropertiesPane.rotation.title", "Rotation parameters"));
		parametersTitledPane.setContent(gridPane);
		return parametersTitledPane;
	}
	
	private Node createShearPane(RestrictionType[] restrictionTypes) {
		GridPane gridPane = this.createGridPane();

		int row = 0;

		for (RestrictionType restrictionType : restrictionTypes) {
			CheckBox box = null;
			ProgressIndicator progressIndicator = null; 
			switch (restrictionType) {
			case FIXED_SHEAR_Y:
				
				box = this.shearYCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.shear.y.label", "Shear y"), i18n.getString("UICongruenceAnalysisPropertiesPane.shear.y.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			case FIXED_SHEAR_X:
				
				box = this.shearXCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.shear.x.label", "Shear x"), i18n.getString("UICongruenceAnalysisPropertiesPane.shear.x.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			case FIXED_SHEAR_Z:
				
				box = this.shearZCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.shear.z.label", "Shear z"), i18n.getString("UICongruenceAnalysisPropertiesPane.shear.z.label.tooltip", "Checked, if translation is a strain parameter to be estimate"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			default:
				continue;
			}
			
			if (box != null) {
				gridPane.add(box, 0, row);
				gridPane.add(progressIndicator, 1, row++);
			}
		}

		if (row == 0)
			return null;

		TitledPane parametersTitledPane = this.createTitledPane(i18n.getString("UICongruenceAnalysisPropertiesPane.shear.title", "Shear parameters"));
		parametersTitledPane.setContent(gridPane);
		return parametersTitledPane;
	}
	
	private Node createScalePane(RestrictionType[] restrictionTypes) {
		GridPane gridPane = this.createGridPane();

		int row = 0;

		for (RestrictionType restrictionType : restrictionTypes) {
			CheckBox box = null;
			ProgressIndicator progressIndicator = null;
			
			switch (restrictionType) {
			case FIXED_SCALE_Y:
				
				box = this.scaleYCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.scale.y.label", "Scale y"), i18n.getString("UICongruenceAnalysisPropertiesPane.scale.y.label.tooltip", "Checked, if scale is a strain parameter to be estimate"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			case FIXED_SCALE_X:
				
				box = this.scaleXCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.scale.x.label", "Scale x"), i18n.getString("UICongruenceAnalysisPropertiesPane.scale.x.label.tooltip", "Checked, if scale is a strain parameter to be estimate"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			case FIXED_SCALE_Z:
				
				box = this.scaleZCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.scale.z.label", "Scale z"), i18n.getString("UICongruenceAnalysisPropertiesPane.scale.z.label.tooltip", "Checked, if scale is a strain parameter to be estimate"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			default:
				continue;
			}

			if (box != null) {
				gridPane.add(box, 0, row);
				gridPane.add(progressIndicator, 1, row++);
			}
		}

		if (row == 0)
			return null;
		
		TitledPane parametersTitledPane = this.createTitledPane(i18n.getString("UICongruenceAnalysisPropertiesPane.scale.title", "Scale parameters"));
		parametersTitledPane.setContent(gridPane);
		return parametersTitledPane;
	}
	
	private Node createScaleRestrictionPane(RestrictionType[] restrictionTypes) {
		GridPane gridPane = this.createGridPane();

		int row = 0;

		for (RestrictionType restrictionType : restrictionTypes) {
			CheckBox box = null;
			ProgressIndicator progressIndicator = null;
			
			switch (restrictionType) {
			case IDENT_SCALES_XY:
				
				box = this.scaleXYCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.scale.restriction.xy.label", "Scale y = x"), i18n.getString("UICongruenceAnalysisPropertiesPane.scale.restriction.xy.label.tooltip", "Checked, if scale restriction has to applied"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			case IDENT_SCALES_YZ:
				
				box = this.scaleYZCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.scale.restriction.yz.label", "Scale y = z"), i18n.getString("UICongruenceAnalysisPropertiesPane.scale.restriction.yz.label.tooltip", "Checked, if scale restriction has to applied"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			case IDENT_SCALES_XZ:
				
				box = this.scaleXZCheckBox = this.createCheckBox(i18n.getString("UICongruenceAnalysisPropertiesPane.scale.restriction.xz.label", "Scale x = z"), i18n.getString("UICongruenceAnalysisPropertiesPane.scale.restriction.xz.label.tooltip", "Checked, if scale restriction has to applied"), false, restrictionType);
				progressIndicator = this.createDatabaseTransactionProgressIndicator(restrictionType);
				
				break;
			default:
				continue;
			}

			if (box != null) {
				gridPane.add(box, 0, row);
				gridPane.add(progressIndicator, 1, row++);
			}
		}

		if (row == 0)
			return null;
		
		TitledPane parametersTitledPane = this.createTitledPane(i18n.getString("UICongruenceAnalysisPropertiesPane.scale.restriction.title", "Scale restrictions"));
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
		
		Node translationPane      = this.createTranslationPane(parameterTypes);
		Node rotationPane         = this.createRotationPane(parameterTypes);
		Node shearPane            = this.createShearPane(parameterTypes);
		Node scalePane            = this.createScalePane(parameterTypes);
		Node scaleRestrictionPane = this.createScaleRestrictionPane(parameterTypes);
		
		if (this.scaleXYCheckBox != null && this.scaleXCheckBox != null && this.scaleYCheckBox != null) {
			this.scaleXYCheckBox.selectedProperty().addListener(new BoundedRestrictionChangeListener(this.scaleXCheckBox, this.scaleYCheckBox));
			this.scaleXCheckBox.selectedProperty().addListener(new DependendRestrictionChangeListener(this.scaleXYCheckBox));
			this.scaleYCheckBox.selectedProperty().addListener(new DependendRestrictionChangeListener(this.scaleXYCheckBox));
		}
		
		if (this.scaleXZCheckBox != null && this.scaleXCheckBox != null && this.scaleZCheckBox != null) {
			this.scaleXZCheckBox.selectedProperty().addListener(new BoundedRestrictionChangeListener(this.scaleXCheckBox, this.scaleZCheckBox));
			this.scaleXCheckBox.selectedProperty().addListener(new DependendRestrictionChangeListener(this.scaleXZCheckBox));
			this.scaleZCheckBox.selectedProperty().addListener(new DependendRestrictionChangeListener(this.scaleXZCheckBox));
		}
		
		if (this.scaleYZCheckBox != null && this.scaleYCheckBox != null && this.scaleZCheckBox != null) {
			this.scaleYZCheckBox.selectedProperty().addListener(new BoundedRestrictionChangeListener(this.scaleYCheckBox, this.scaleZCheckBox));
			this.scaleYCheckBox.selectedProperty().addListener(new DependendRestrictionChangeListener(this.scaleYZCheckBox));
			this.scaleZCheckBox.selectedProperty().addListener(new DependendRestrictionChangeListener(this.scaleYZCheckBox));
		}

		this.reset();
		
		if (translationPane != null)
			content.getChildren().add(translationPane);
		
		if (rotationPane != null)
			content.getChildren().add(rotationPane);
		
		if (shearPane != null)
			content.getChildren().add(shearPane);
		
		if (scalePane != null)
			content.getChildren().add(scalePane);
		
		if (scaleRestrictionPane != null)
			content.getChildren().add(scaleRestrictionPane);

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
	}
	
	private CheckBox createCheckBox(String title, String tooltipText, boolean selected, RestrictionType userData) {
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
	
	private ProgressIndicator createDatabaseTransactionProgressIndicator(Object userData) {
		ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);

		progressIndicator.setVisible(false);
		progressIndicator.setMinSize(17, 17);
		progressIndicator.setMaxSize(17, 17);
		progressIndicator.setUserData(userData);
				
		this.databaseTransactionProgressIndicators.put(userData, progressIndicator);
		return progressIndicator;
	}
	
	private void setProgressIndicatorsVisible(boolean visible) {
		if (this.databaseTransactionProgressIndicators != null)
			for (ProgressIndicator progressIndicator : this.databaseTransactionProgressIndicators.values())
				progressIndicator.setVisible(visible);
	}

	private void save(RestrictionType parameterType, boolean selected) {
		try {
			if (this.selectedCongruenceAnalysisItemValues != null && this.selectedCongruenceAnalysisItemValues.length > 0) {
				this.setProgressIndicatorsVisible(false);
				if (this.databaseTransactionProgressIndicators.containsKey(parameterType)) {
					ProgressIndicator node = this.databaseTransactionProgressIndicators.get(parameterType);
					node.setVisible(true);
					this.sequentialTransition.stop();
					this.sequentialTransition.setNode(node);
					this.sequentialTransition.playFromStart();
				}
				SQLManager.getInstance().saveStrainParameter(parameterType, selected, this.selectedCongruenceAnalysisItemValues);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
			this.sequentialTransition.stop();
			this.setProgressIndicatorsVisible(false);
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("UICongruenceAnalysisPropertiesPane.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UICongruenceAnalysisPropertiesPane.message.error.save.exception.header", "Error, could not save strain properties to database."),
							i18n.getString("UICongruenceAnalysisPropertiesPane.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
					);
				}
			});
		}
	}
}
