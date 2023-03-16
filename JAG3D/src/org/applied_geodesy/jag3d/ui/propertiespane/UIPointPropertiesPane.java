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

import org.applied_geodesy.adjustment.network.PointGroupUncertaintyType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.tree.PointTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.ui.textfield.UncertaintyTextField;
import org.applied_geodesy.util.CellValueType;
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

public class UIPointPropertiesPane {
	private class CommitTextFieldActionListener implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (!ignoreValueUpdate && event.getSource() != null && event.getSource() instanceof DoubleTextField && ((DoubleTextField)event.getSource()).getUserData() != null && ((DoubleTextField)event.getSource()).getUserData() instanceof PointGroupUncertaintyType) {
				DoubleTextField field = (DoubleTextField)event.getSource();
				PointGroupUncertaintyType uncertaintyType = (PointGroupUncertaintyType)field.getUserData();
				save(uncertaintyType);
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
			if (!ignoreValueUpdate && this.field.getUserData() != null && this.field.getUserData() instanceof PointGroupUncertaintyType) {
				PointGroupUncertaintyType uncertaintyType = (PointGroupUncertaintyType)this.field.getUserData();
				save(uncertaintyType);
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

	private UncertaintyTextField uncertaintyCoordinateXField;
	private UncertaintyTextField uncertaintyCoordinateYField;
	private UncertaintyTextField uncertaintyCoordinateZField;
	
	private Label selectionInfoLabel = new Label();
	
	private Map<Object, ProgressIndicator> databaseTransactionProgressIndicators = new HashMap<Object, ProgressIndicator>(10);
	private Map<Object, Node> warningIconNodes = new HashMap<Object, Node>(10);
	private SequentialTransition sequentialTransition = new SequentialTransition();

	private boolean ignoreValueUpdate = false;
	private PointTreeItemValue selectedPointItemValues[] = null;

	UIPointPropertiesPane(TreeItemType type) {
		switch(type) {
		case REFERENCE_POINT_1D_LEAF:
		case REFERENCE_POINT_2D_LEAF:
		case REFERENCE_POINT_3D_LEAF:
		case STOCHASTIC_POINT_1D_LEAF:
		case STOCHASTIC_POINT_2D_LEAF:
		case STOCHASTIC_POINT_3D_LEAF:
		case NEW_POINT_1D_LEAF:
		case NEW_POINT_2D_LEAF:
		case NEW_POINT_3D_LEAF:
		case DATUM_POINT_1D_LEAF:
		case DATUM_POINT_2D_LEAF:
		case DATUM_POINT_3D_LEAF:
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
		
		this.setUncertaintyY(PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.COMPONENT_Y));
		this.setUncertaintyX(PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.COMPONENT_X));
		this.setUncertaintyZ(PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.COMPONENT_Z));
	}
	
	public void setTreeItemValue(String name, PointTreeItemValue... selectedPointItemValues) {
		if (this.selectedPointItemValues != selectedPointItemValues) {
			this.reset();
			this.selectedPointItemValues = selectedPointItemValues;
		}
		this.setGroupName(name, this.selectedPointItemValues != null ? this.selectedPointItemValues.length : 0);
	}
	
	private void setGroupName(String name, int cnt) {
		if (this.selectionInfoLabel != null) {
			String groupNameTmpl    = this.i18n.getString("UIPointPropertiesPane.status.selection.name.label", "Status:");
			String selectionCntTmpl = cnt > 1 ? String.format(Locale.ENGLISH, this.i18n.getString("UIPointPropertiesPane.status.selection.counter.label", "and %d more selected group(s)\u2026"), cnt) : "";
			String label = String.format(
					Locale.ENGLISH, 
					"%s %s %s", 
					groupNameTmpl, name, selectionCntTmpl);
			if (!this.selectionInfoLabel.getText().equals(label))
				this.selectionInfoLabel.setText(label);
		}
	}

	public boolean setUncertaintyY(Double value) {
		if (this.uncertaintyCoordinateYField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.uncertaintyCoordinateYField.setValue(value != null && value > 0 ? value : null);
		this.ignoreValueUpdate = false;
		return true;
	}

	public boolean setUncertaintyX(Double value) {
		if (this.uncertaintyCoordinateXField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.uncertaintyCoordinateXField.setValue(value != null && value > 0 ? value : null);
		this.ignoreValueUpdate = false;
		return true;
	}

	public boolean setUncertaintyZ(Double value) {
		if (this.uncertaintyCoordinateZField == null)
			return false;
		this.ignoreValueUpdate = true;
		this.uncertaintyCoordinateZField.setValue(value != null && value > 0 ? value : null);
		this.ignoreValueUpdate = false;
		return true;
	}

	public boolean setUncertainty(PointGroupUncertaintyType type, Double value, boolean displayWarningIcon) {
		if (this.warningIconNodes.containsKey(type)) {
			this.warningIconNodes.get(type).setVisible(displayWarningIcon);
			this.warningIconNodes.get(type).setManaged(displayWarningIcon);
		}
		
		switch(type) {
		case COMPONENT_X:
			return this.setUncertaintyX(value);
		case COMPONENT_Y:
			return this.setUncertaintyY(value);
		case COMPONENT_Z:
			return this.setUncertaintyZ(value);
		default:
			return false;
		}
	}

	private Node createCoordinateUncertaintiesPane() {
		if (this.type == TreeItemType.STOCHASTIC_POINT_1D_LEAF || this.type == TreeItemType.STOCHASTIC_POINT_2D_LEAF || this.type == TreeItemType.STOCHASTIC_POINT_3D_LEAF) {
			GridPane gridPane = this.createGridPane();

			double sigmaY = PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.COMPONENT_Y);
			double sigmaX = PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.COMPONENT_X);
			double sigmaZ = PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.COMPONENT_Z);

			double fieldMinWidth = 200;
			double fieldMaxWidth = 350;
			
			int row = 0;

			if (this.type == TreeItemType.STOCHASTIC_POINT_2D_LEAF || this.type == TreeItemType.STOCHASTIC_POINT_3D_LEAF) {
				Node warningIconUncertaintyTypeYNode = this.createWarningIcon(PointGroupUncertaintyType.COMPONENT_Y, i18n.getString("UIPointPropertiesPane.uncertainty.point.y.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIPointPropertiesPane.uncertainty.point.y.warning.tooltip", "Note: The selected groups have different values and \u03C3y differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
				ProgressIndicator databaseTransactionuncertaintyCoordinateYLabelProgressIndicator = this.createDatabaseTransactionProgressIndicator(PointGroupUncertaintyType.COMPONENT_Y);
				Label uncertaintyCoordinateYLabel = new Label(i18n.getString("UIPointPropertiesPane.uncertainty.point.y.label", "\u03C3y"));
				this.uncertaintyCoordinateYField = new UncertaintyTextField(sigmaY, CellValueType.LENGTH_UNCERTAINTY, true, DoubleTextField.ValueSupport.EXCLUDING_INCLUDING_INTERVAL);
				this.uncertaintyCoordinateYField.setTooltip(new Tooltip(i18n.getString("UIPointPropertiesPane.uncertainty.point.y.tooltip", "Uncertainty of y-component of stochastic points")));
				this.uncertaintyCoordinateYField.setUserData(PointGroupUncertaintyType.COMPONENT_Y);
				this.uncertaintyCoordinateYField.numberProperty().addListener(new NumberChangeListener(this.uncertaintyCoordinateYField));
				this.uncertaintyCoordinateYField.setOnAction(new CommitTextFieldActionListener());
				this.uncertaintyCoordinateYField.setMinWidth(fieldMinWidth);
				this.uncertaintyCoordinateYField.setMaxWidth(fieldMaxWidth);
				
				Node warningIconUncertaintyTypeXNode = this.createWarningIcon(PointGroupUncertaintyType.COMPONENT_X, i18n.getString("UIPointPropertiesPane.uncertainty.point.x.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIPointPropertiesPane.uncertainty.point.x.warning.tooltip", "Note: The selected groups have different values and \u03C3x differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
				ProgressIndicator databaseTransactionuncertaintyCoordinateXLabelProgressIndicator = this.createDatabaseTransactionProgressIndicator(PointGroupUncertaintyType.COMPONENT_X);
				Label uncertaintyCoordinateXLabel = new Label(i18n.getString("UIPointPropertiesPane.uncertainty.point.x.label", "\u03C3x"));
				this.uncertaintyCoordinateXField = new UncertaintyTextField(sigmaX, CellValueType.LENGTH_UNCERTAINTY, true, DoubleTextField.ValueSupport.EXCLUDING_INCLUDING_INTERVAL);
				this.uncertaintyCoordinateXField.setTooltip(new Tooltip(i18n.getString("UIPointPropertiesPane.uncertainty.point.x.tooltip", "Uncertainty of x-component of stochastic points")));
				this.uncertaintyCoordinateXField.setUserData(PointGroupUncertaintyType.COMPONENT_X);
				this.uncertaintyCoordinateXField.numberProperty().addListener(new NumberChangeListener(this.uncertaintyCoordinateXField));
				this.uncertaintyCoordinateXField.setOnAction(new CommitTextFieldActionListener());
				this.uncertaintyCoordinateXField.setMinWidth(fieldMinWidth);
				this.uncertaintyCoordinateXField.setMaxWidth(fieldMaxWidth);
				
				uncertaintyCoordinateYLabel.setLabelFor(this.uncertaintyCoordinateYField);
				uncertaintyCoordinateXLabel.setLabelFor(this.uncertaintyCoordinateXField);
				
				uncertaintyCoordinateYLabel.setMinWidth(Control.USE_PREF_SIZE);
				uncertaintyCoordinateXLabel.setMinWidth(Control.USE_PREF_SIZE);
				
//				GridPane.setHgrow(uncertaintyCoordinateYLabel, Priority.SOMETIMES);
//				GridPane.setHgrow(uncertaintyCoordinateXLabel, Priority.SOMETIMES);
//				GridPane.setHgrow(this.uncertaintyCoordinateYField, Priority.ALWAYS);
//				GridPane.setHgrow(this.uncertaintyCoordinateXField, Priority.ALWAYS);

				gridPane.add(uncertaintyCoordinateYLabel,      0, row);
				gridPane.add(this.uncertaintyCoordinateYField, 1, row);
				gridPane.add(new HBox(warningIconUncertaintyTypeYNode, databaseTransactionuncertaintyCoordinateYLabelProgressIndicator), 2, row++);

				gridPane.add(uncertaintyCoordinateXLabel,      0, row);
				gridPane.add(this.uncertaintyCoordinateXField, 1, row);
				gridPane.add(new HBox(warningIconUncertaintyTypeXNode, databaseTransactionuncertaintyCoordinateXLabelProgressIndicator), 2, row++);
			}

			if (this.type == TreeItemType.STOCHASTIC_POINT_1D_LEAF || this.type == TreeItemType.STOCHASTIC_POINT_3D_LEAF) {
				Node warningIconUncertaintyTypeZNode = this.createWarningIcon(PointGroupUncertaintyType.COMPONENT_Z, i18n.getString("UIPointPropertiesPane.uncertainty.point.z.warning.label", "\u26A0"), String.format(Locale.ENGLISH, i18n.getString("UIPointPropertiesPane.uncertainty.point.z.warning.tooltip", "Note: The selected groups have different values and \u03C3z differs by more than %.1f \u2030."), SQLManager.EQUAL_VALUE_TRESHOLD * 1000.));
				ProgressIndicator databaseTransactionuncertaintyCoordinateZLabelProgressIndicator = this.createDatabaseTransactionProgressIndicator(PointGroupUncertaintyType.COMPONENT_Z);
				Label uncertaintyCoordinateZLabel = new Label(i18n.getString("UIPointPropertiesPane.uncertainty.point.z.label", "\u03C3z"));
				this.uncertaintyCoordinateZField = new UncertaintyTextField(sigmaZ, CellValueType.LENGTH_UNCERTAINTY, true, DoubleTextField.ValueSupport.EXCLUDING_INCLUDING_INTERVAL);
				this.uncertaintyCoordinateZField.setTooltip(new Tooltip(i18n.getString("UIPointPropertiesPane.uncertainty.point.z.tooltip", "Uncertainty of z-component of stochastic points")));
				this.uncertaintyCoordinateZField.setUserData(PointGroupUncertaintyType.COMPONENT_Z);
				this.uncertaintyCoordinateZField.numberProperty().addListener(new NumberChangeListener(this.uncertaintyCoordinateZField));
				this.uncertaintyCoordinateZField.setOnAction(new CommitTextFieldActionListener());
				this.uncertaintyCoordinateZField.setMinWidth(fieldMinWidth);
				this.uncertaintyCoordinateZField.setMaxWidth(fieldMaxWidth);
				
				uncertaintyCoordinateZLabel.setLabelFor(this.uncertaintyCoordinateZField);
				
//				GridPane.setHgrow(uncertaintyCoordinateZLabel, Priority.SOMETIMES);
//				GridPane.setHgrow(this.uncertaintyCoordinateZField, Priority.ALWAYS);
				
				gridPane.add(uncertaintyCoordinateZLabel,      0, row);
				gridPane.add(this.uncertaintyCoordinateZField, 1, row);
				gridPane.add(new HBox(warningIconUncertaintyTypeZNode, databaseTransactionuncertaintyCoordinateZLabelProgressIndicator), 2, row++);
			}

			TitledPane uncertaintiesTitledPane = this.createTitledPane(i18n.getString("UIPointPropertiesPane.uncertainty.title", "Uncertainties of stochastic points"));
			uncertaintiesTitledPane.setContent(gridPane);
			return uncertaintiesTitledPane;
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
		
		Node coordinateUncertainties = this.createCoordinateUncertaintiesPane();
		if (coordinateUncertainties != null)
			content.getChildren().add(coordinateUncertainties);
		
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

	private void save(PointGroupUncertaintyType uncertaintyType) {
		try {
			Double value = null;
			switch(uncertaintyType) {
			case COMPONENT_Y:
				value = this.uncertaintyCoordinateYField.getNumber();
				break;
			case COMPONENT_X:
				value = this.uncertaintyCoordinateXField.getNumber();
				break;
			case COMPONENT_Z:
				value = this.uncertaintyCoordinateZField.getNumber();
				break;
			default:
				System.err.println(this.getClass().getSimpleName() + " : Error, unsupported uncertainty type " + uncertaintyType);
				break;
			}

			if (value != null && value.doubleValue() > 0 && this.selectedPointItemValues != null && this.selectedPointItemValues.length > 0) {
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
				SQLManager.getInstance().saveUncertainty(uncertaintyType, value.doubleValue(), this.selectedPointItemValues);
			}

		} catch (Exception e) {
			e.printStackTrace();
			
			this.setProgressIndicatorsVisible(false);
			this.sequentialTransition.stop();
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIPointPropertiesPane.message.error.save.uncertainty.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIPointPropertiesPane.message.error.save.uncertainty.exception.header", "Error, could not save group uncertainties to database."),
							i18n.getString("UIPointPropertiesPane.message.error.save.uncertainty.exception.message", "An exception has occurred during database transaction."),
							e
					);
				}
			});
		}
	}
}
