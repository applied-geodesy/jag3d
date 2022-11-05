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

import org.applied_geodesy.adjustment.network.ParameterType;
import org.applied_geodesy.adjustment.network.VerticalDeflectionGroupUncertaintyType;
import org.applied_geodesy.adjustment.network.point.dov.VerticalDeflectionRestrictionType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.jag3d.ui.tree.VerticalDeflectionTreeItemValue;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.ui.textfield.UncertaintyTextField;
import org.applied_geodesy.ui.textfield.DoubleTextField.ValueSupport;
import org.applied_geodesy.util.CellValueType;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class UIVerticalDeflectionPropertiesPane {
	private class BooleanChangeListener implements ChangeListener<Boolean> {
		private final ButtonBase button;
		
		private BooleanChangeListener(ButtonBase button) {
			this.button = button;
		}
		
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (!ignoreValueUpdate && this.button instanceof CheckBox && this.button.getUserData() != null && this.button.getUserData() instanceof VerticalDeflectionRestrictionType) {
				VerticalDeflectionRestrictionType verticalDeflectionRestrictionType = (VerticalDeflectionRestrictionType)this.button.getUserData();
				save(verticalDeflectionRestrictionType, ((CheckBox)this.button).isSelected());
			}
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
					ParameterType parameterType = (ParameterType)this.field.getUserData();
					save(parameterType);
				}
				else if (this.field.getUserData() instanceof VerticalDeflectionGroupUncertaintyType) {
					VerticalDeflectionGroupUncertaintyType uncertaintyType = (VerticalDeflectionGroupUncertaintyType)this.field.getUserData();
					save(uncertaintyType);
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

	private UncertaintyTextField uncertaintyDeflectionXField;
	private UncertaintyTextField uncertaintyDeflectionYField;
	
	private CheckBox restrictionTypeIdenticalCheckBox;
	
	private DoubleTextField verticalDeflectionXField;
	private DoubleTextField verticalDeflectionYField;
	
	private Label selectionInfoLabel = new Label();
	
	private Map<Object, ProgressIndicator> databaseTransactionProgressIndicators = new HashMap<Object, ProgressIndicator>(10);
	private Map<Object, Node> warningIconNodes = new HashMap<Object, Node>(10);
	private SequentialTransition sequentialTransition = new SequentialTransition();

	private boolean ignoreValueUpdate = false;
	private VerticalDeflectionTreeItemValue selectedVerticalDeflectionItemValues[] = null;

	UIVerticalDeflectionPropertiesPane(TreeItemType type) {
		switch(type) {
		case REFERENCE_VERTICAL_DEFLECTION_LEAF:
		case STOCHASTIC_VERTICAL_DEFLECTION_LEAF:
		case UNKNOWN_VERTICAL_DEFLECTION_LEAF:
			this.type = type;
			this.init();
			break;
		default:
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Error, unsupported item type " + type);		
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
		
		this.setRestriction(VerticalDeflectionRestrictionType.IDENTICAL_DEFLECTIONS, Boolean.FALSE);
		
		this.setUncertaintyDeflectionY(VerticalDeflectionTreeItemValue.getDefaultUncertainty(VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y));
		this.setUncertaintyDeflectionX(VerticalDeflectionTreeItemValue.getDefaultUncertainty(VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y));
		
		this.setVerticalDeflectionY(0.0);
		this.setVerticalDeflectionX(0.0);
	}
	
	public void setTreeItemValue(String name, VerticalDeflectionTreeItemValue... selectedVerticalDeflectionItemValues) {
		if (this.selectedVerticalDeflectionItemValues != selectedVerticalDeflectionItemValues) {
			this.reset();
			this.selectedVerticalDeflectionItemValues = selectedVerticalDeflectionItemValues;
		}
		this.setGroupName(name, this.selectedVerticalDeflectionItemValues != null ? this.selectedVerticalDeflectionItemValues.length : 0);
	}
	
	private void setGroupName(String name, int cnt) {
		if (this.selectionInfoLabel != null) {
			String groupNameTmpl    = this.i18n.getString("UIVerticalDeflectionPropertiesPane.status.selection.name.label", "Status:");
			String selectionCntTmpl = cnt > 1 ? String.format(Locale.ENGLISH, this.i18n.getString("UIVerticalDeflectionPropertiesPane.status.selection.counter.label", "and %d more selected group(s)\u2026"), cnt) : "";
			String label = String.format(
					Locale.ENGLISH, 
					"%s %s %s", 
					groupNameTmpl, name, selectionCntTmpl);
			if (!this.selectionInfoLabel.getText().equals(label))
				this.selectionInfoLabel.setText(label);
		}
	}
	
	public boolean setUncertaintyDeflectionY(Double value) {
		if (this.uncertaintyDeflectionYField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.uncertaintyDeflectionYField.setValue(value != null && value > 0 ? value : null);
		this.ignoreValueUpdate = false;
		return true;
	}

	public boolean setUncertaintyDeflectionX(Double value) {
		if (this.uncertaintyDeflectionXField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.uncertaintyDeflectionXField.setValue(value != null && value > 0 ? value : null);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setUncertainty(VerticalDeflectionGroupUncertaintyType type, Double value, boolean displayWarningIcon) {
		if (this.warningIconNodes.containsKey(type)) {
			this.warningIconNodes.get(type).setVisible(displayWarningIcon);
			this.warningIconNodes.get(type).setManaged(displayWarningIcon);
		}
		
		switch(type) {
		case DEFLECTION_X:
			return this.setUncertaintyDeflectionX(value);
		case DEFLECTION_Y:
			return this.setUncertaintyDeflectionY(value);
		default:
			return false;
		}
	}
	
	public boolean setGroupParameter(ParameterType paramType, Double value, boolean displayWarningIcon) {
		if (this.warningIconNodes.containsKey(paramType)) {
			this.warningIconNodes.get(paramType).setVisible(displayWarningIcon);
			this.warningIconNodes.get(paramType).setManaged(displayWarningIcon);
		}
		
		switch(paramType) {
		case VERTICAL_DEFLECTION_X:
			return this.setVerticalDeflectionX(value);

		case VERTICAL_DEFLECTION_Y:
			return this.setVerticalDeflectionY(value);
			
		default:
			return false;	
		}
	}
	
	public boolean setVerticalDeflectionX(Double value) {
		if (this.verticalDeflectionXField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.verticalDeflectionXField.setValue(value);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setVerticalDeflectionY(Double value) {
		if (this.verticalDeflectionYField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.verticalDeflectionYField.setValue(value);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	public boolean setRestriction(VerticalDeflectionRestrictionType restrictionType, boolean selected) {
		if (this.restrictionTypeIdenticalCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.restrictionTypeIdenticalCheckBox.setSelected(selected);
		this.ignoreValueUpdate = false;
		return true;
	}
	
	private Node createUncertaintiesPane() {
		if (this.type == TreeItemType.STOCHASTIC_VERTICAL_DEFLECTION_LEAF) {
			double sigmaY = VerticalDeflectionTreeItemValue.getDefaultUncertainty(VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y);
			double sigmaX = VerticalDeflectionTreeItemValue.getDefaultUncertainty(VerticalDeflectionGroupUncertaintyType.DEFLECTION_X);

			Node warningIconUncertaintyTypeYNode = this.createWarningIcon(VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y, i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.y.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.y.warning.tooltip", "Note: The selected groups have different values and \u03C3y differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
			Node warningIconUncertaintyTypeXNode = this.createWarningIcon(VerticalDeflectionGroupUncertaintyType.DEFLECTION_X, i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.x.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.x.warning.tooltip", "Note: The selected groups have different values and \u03C3x differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
			
			ProgressIndicator uncertaintyDeflectionYProgressIndicator = this.createDatabaseTransactionProgressIndicator(VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y);
			ProgressIndicator uncertaintyDeflectionXProgressIndicator = this.createDatabaseTransactionProgressIndicator(VerticalDeflectionGroupUncertaintyType.DEFLECTION_X); 
									
			Label uncertaintyDeflectionYLabel = new Label(i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.y.label", "\u03C3y"));
			Label uncertaintyDeflectionXLabel = new Label(i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.x.label", "\u03C3x"));
			uncertaintyDeflectionYLabel.setMinWidth(Control.USE_PREF_SIZE);
			uncertaintyDeflectionXLabel.setMinWidth(Control.USE_PREF_SIZE);
			
			this.uncertaintyDeflectionYField = this.createUncertaintyTextField(sigmaY, CellValueType.ANGLE_UNCERTAINTY, true, ValueSupport.EXCLUDING_INCLUDING_INTERVAL, i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.y.tooltip", "Uncertainty of y-component of deflection of the vertical"), VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y); 
			this.uncertaintyDeflectionXField = this.createUncertaintyTextField(sigmaX, CellValueType.ANGLE_UNCERTAINTY, true, ValueSupport.EXCLUDING_INCLUDING_INTERVAL, i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.x.tooltip", "Uncertainty of x-component of deflection of the vertical"), VerticalDeflectionGroupUncertaintyType.DEFLECTION_X);
			
			uncertaintyDeflectionYLabel.setLabelFor(this.uncertaintyDeflectionYField);
			uncertaintyDeflectionXLabel.setLabelFor(this.uncertaintyDeflectionXField);
			
			GridPane gridPane = this.createGridPane();
//			GridPane.setHgrow(uncertaintyDeflectionYLabel, Priority.SOMETIMES);
//			GridPane.setHgrow(uncertaintyDeflectionXLabel, Priority.SOMETIMES);
//			GridPane.setHgrow(this.uncertaintyDeflectionYField, Priority.ALWAYS);
//			GridPane.setHgrow(this.uncertaintyDeflectionXField, Priority.ALWAYS);
			
			gridPane.add(uncertaintyDeflectionYLabel,      0, 0);
			gridPane.add(this.uncertaintyDeflectionYField, 1, 0);
			gridPane.add(new HBox(warningIconUncertaintyTypeYNode, uncertaintyDeflectionYProgressIndicator), 2, 0);

			gridPane.add(uncertaintyDeflectionXLabel,      0, 1);
			gridPane.add(this.uncertaintyDeflectionXField, 1, 1);
			gridPane.add(new HBox(warningIconUncertaintyTypeXNode, uncertaintyDeflectionXProgressIndicator), 2, 1);

			TitledPane uncertaintiesTitledPane = this.createTitledPane(i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.title", "Uncertainties of deflection of the vertical"));
			uncertaintiesTitledPane.setContent(gridPane);
			return uncertaintiesTitledPane;
		}
		return null;
	}
	
	private Node createRestrictionTypePane() {
		if (this.type != TreeItemType.REFERENCE_VERTICAL_DEFLECTION_LEAF) {
			GridPane gridPane = this.createGridPane();

			this.restrictionTypeIdenticalCheckBox = this.createCheckBox(i18n.getString("UIVerticalDeflectionPropertiesPane.deflection.identical.label", "Identical deflection of the vertical"), i18n.getString("UIVerticalDeflectionPropertiesPane.restriction.identical.tooltip", "Checked, if vertical deflection angles of points within the group are identical."), false, VerticalDeflectionRestrictionType.IDENTICAL_DEFLECTIONS);
			ProgressIndicator databaseTransactionRestrictionTypeIndividualProgressIndicator = this.createDatabaseTransactionProgressIndicator(VerticalDeflectionRestrictionType.IDENTICAL_DEFLECTIONS);
			
			Label deflectionLabelY = new Label(i18n.getString("UIVerticalDeflectionPropertiesPane.deflection.y.label", "Vertical deflection \u03B6y"));
			this.verticalDeflectionYField = this.createDoubleTextField(0.0, CellValueType.ANGLE_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIVerticalDeflectionPropertiesPane.deflection.y.tooltip", "Set y-component of vertical deflection"), ParameterType.VERTICAL_DEFLECTION_Y);				
			ProgressIndicator progressIndicatorY = this.createDatabaseTransactionProgressIndicator(ParameterType.VERTICAL_DEFLECTION_Y);
			Node warningIconY = this.createWarningIcon(ParameterType.VERTICAL_DEFLECTION_Y, i18n.getString("UIVerticalDeflectionPropertiesPane.deflection.y.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIVerticalDeflectionPropertiesPane.deflection.y.warning.tooltip", "Note: The selected groups have different values and the \u03B6y of the deflection of the vertical differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
			
			Label deflectionLabelX = new Label(i18n.getString("UIVerticalDeflectionPropertiesPane.deflection.x.label", "Vertical deflection \u03B6x"));
			this.verticalDeflectionXField = this.createDoubleTextField(0.0, CellValueType.ANGLE_RESIDUAL, true, ValueSupport.NON_NULL_VALUE_SUPPORT, i18n.getString("UIVerticalDeflectionPropertiesPane.deflection.x.tooltip", "Set x-component of vertical deflection"), ParameterType.VERTICAL_DEFLECTION_X);				
			ProgressIndicator progressIndicatorX = this.createDatabaseTransactionProgressIndicator(ParameterType.VERTICAL_DEFLECTION_X);
			Node warningIconX = this.createWarningIcon(ParameterType.VERTICAL_DEFLECTION_X, i18n.getString("UIVerticalDeflectionPropertiesPane.deflection.x.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIVerticalDeflectionPropertiesPane.deflection.x.warning.tooltip", "Note: The selected groups have different values and the \u03B6x of the deflection of the vertical differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
			
			
			int row = 0;
			this.restrictionTypeIdenticalCheckBox.setPadding(new Insets(0,5,0,0));
			gridPane.add(new HBox(this.restrictionTypeIdenticalCheckBox, databaseTransactionRestrictionTypeIndividualProgressIndicator), 0, row++, 3, 1);
//			gridPane.add(databaseTransactionRestrictionTypeIndividualProgressIndicator, 2, row++);

			gridPane.add(deflectionLabelY,  0, row);
			gridPane.add(this.verticalDeflectionYField,  1, row);
			gridPane.add(new HBox(warningIconY, progressIndicatorY), 2, row++);
			
			gridPane.add(deflectionLabelX,  0, row);
			gridPane.add(this.verticalDeflectionXField,  1, row);
			gridPane.add(new HBox(warningIconX, progressIndicatorX), 2, row++);

			TitledPane restrictionTitledPane = this.createTitledPane(i18n.getString("UIVerticalDeflectionPropertiesPane.restriction.title", "Estimation Restriction"));
			restrictionTitledPane.setContent(gridPane);
			return restrictionTitledPane;
		}
		return null;
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
	
	private DoubleTextField createDoubleTextField(double value, CellValueType type, boolean displayUnit, ValueSupport valueSupport, String tooltipText, ParameterType userData) {
		DoubleTextField field = new DoubleTextField(value, type, displayUnit, valueSupport);
		field.setTooltip(new Tooltip(tooltipText));
		field.setMinWidth(200);
		field.setMaxWidth(350);
		field.setUserData(userData);
		field.numberProperty().addListener(new NumberChangeListener(field));
		return field;
	}
	
	private UncertaintyTextField createUncertaintyTextField(double value, CellValueType type, boolean displayUnit, ValueSupport valueSupport, String tooltipText, VerticalDeflectionGroupUncertaintyType userData) {
		UncertaintyTextField field = new UncertaintyTextField(value, type, displayUnit, valueSupport);
		field.setTooltip(new Tooltip(tooltipText));
		field.setMinWidth(200);
		field.setMaxWidth(350);
		field.setUserData(userData);
		field.numberProperty().addListener(new NumberChangeListener(field));
		return field;
	}
	
	private CheckBox createCheckBox(String title, String tooltipText, boolean selected, Object userData) {
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
		VBox content = new VBox();
		
		Node restrictions = createRestrictionTypePane();
		if (restrictions != null)
			content.getChildren().add(restrictions);
		
		Node uncertainties = this.createUncertaintiesPane();
		if (uncertainties != null)
			content.getChildren().add(uncertainties);
		
		this.reset();
		
		ScrollPane scroller = new ScrollPane(content);
		scroller.setPadding(new Insets(20, 50, 20, 50)); // oben, links, unten, rechts
		scroller.setFitToHeight(true);
		scroller.setFitToWidth(true);

		Region spacer = new Region();
		spacer.setPrefHeight(0);
		VBox.setVgrow(spacer, Priority.ALWAYS);
		this.selectionInfoLabel.setPadding(new Insets(1,5,2,10));
		this.selectionInfoLabel.setFont(new Font(10.5));
		this.propertiesNode = new VBox(scroller, spacer, this.selectionInfoLabel);
//		this.propertiesNode = scroller;
		
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
	
	private void save(VerticalDeflectionRestrictionType restrictionType, boolean enable) {
		try {

			if (this.databaseTransactionProgressIndicators.containsKey(restrictionType)) {
				ProgressIndicator node = this.databaseTransactionProgressIndicators.get(restrictionType);
				node.setVisible(true);
				this.sequentialTransition.stop();
				this.sequentialTransition.setNode(node);
				this.sequentialTransition.playFromStart();
			}
			SQLManager.getInstance().saveRestriction(restrictionType, enable, this.selectedVerticalDeflectionItemValues);

		} catch (Exception e) {
			e.printStackTrace();

			this.setProgressIndicatorsVisible(false);
			this.sequentialTransition.stop();

			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIVerticalDeflectionPropertiesPane.message.error.save.restriction.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIVerticalDeflectionPropertiesPane.message.error.save.restriction.exception.header", "Error, could not save group restriction to database."),
							i18n.getString("UIVerticalDeflectionPropertiesPane.message.error.save.restriction.exception.message", "An exception has occurred during database transaction."),
							e
					);
				}
			});
		}
	}

	private void save(VerticalDeflectionGroupUncertaintyType uncertaintyType) {
		try {
			Double value = null;
			switch(uncertaintyType) {
			case DEFLECTION_X:
				value = this.uncertaintyDeflectionXField.getNumber();
				break;
			case DEFLECTION_Y:
				value = this.uncertaintyDeflectionYField.getNumber();
				break;
			default:
				System.err.println(this.getClass().getSimpleName() + " : Error, unsupported uncertainty type " + uncertaintyType);
				break;
			}

			if (value != null && value.doubleValue() > 0 && this.selectedVerticalDeflectionItemValues != null && this.selectedVerticalDeflectionItemValues.length > 0) {
				this.setProgressIndicatorsVisible(false);
				if (this.warningIconNodes.containsKey(uncertaintyType)) {
					Node warningIconNodes = this.warningIconNodes.get(uncertaintyType);
					warningIconNodes.setVisible(false);
					warningIconNodes.setManaged(false);
				}
				if (this.databaseTransactionProgressIndicators.containsKey(uncertaintyType)) {
					ProgressIndicator node = this.databaseTransactionProgressIndicators.get(uncertaintyType);
					node.setVisible(true);
					this.sequentialTransition.stop();
					this.sequentialTransition.setNode(node);
					this.sequentialTransition.playFromStart();
				}
				SQLManager.getInstance().saveUncertainty(uncertaintyType, value.doubleValue(), this.selectedVerticalDeflectionItemValues);
			}

		} catch (Exception e) {
			e.printStackTrace();
			
			this.setProgressIndicatorsVisible(false);
			this.sequentialTransition.stop();
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIVerticalDeflectionPropertiesPane.message.error.save.uncertainty.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIVerticalDeflectionPropertiesPane.message.error.save.uncertainty.exception.header", "Error, could not save group uncertainty to database."),
							i18n.getString("UIVerticalDeflectionPropertiesPane.message.error.save.uncertainty.exception.message", "An exception has occurred during database transaction."),
							e
					);
				}
			});
		}
	}
	
	private void save(ParameterType parameterType) {
		try {
			Double value = null;
			switch(parameterType) {
			case VERTICAL_DEFLECTION_X:
				value = this.verticalDeflectionXField.getNumber();
				break;
			case VERTICAL_DEFLECTION_Y:
				value = this.verticalDeflectionYField.getNumber();
				break;
			default:
				break;
			}
			
			if (value != null && this.selectedVerticalDeflectionItemValues != null && this.selectedVerticalDeflectionItemValues.length > 0) {
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
				SQLManager.getInstance().saveGroupParameter(parameterType, value.doubleValue(), this.selectedVerticalDeflectionItemValues);
			}

		} catch (Exception e) {
			e.printStackTrace();
			
			this.setProgressIndicatorsVisible(false);
			this.sequentialTransition.stop();
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIVerticalDeflectionPropertiesPane.message.error.save.parameter.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIVerticalDeflectionPropertiesPane.message.error.save.parameter.exception.header", "Error, could not save group parameter to database."),
							i18n.getString("UIVerticalDeflectionPropertiesPane.message.error.save.parameter.exception.message", "An exception has occurred during database transaction."),
							e
					);
				}
			});
		}
	}
}
