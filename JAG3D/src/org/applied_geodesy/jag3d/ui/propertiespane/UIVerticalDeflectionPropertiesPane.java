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

import org.applied_geodesy.adjustment.network.VerticalDeflectionGroupUncertaintyType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.jag3d.ui.tree.VerticalDeflectionTreeItemValue;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.ui.textfield.UncertaintyTextField;
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
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class UIVerticalDeflectionPropertiesPane {
	private class NumberChangeListener implements ChangeListener<Double> {
		private final DoubleTextField field;

		private NumberChangeListener(DoubleTextField field) {
			this.field = field;
		}

		@Override
		public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
			if (!ignoreValueUpdate && this.field.getUserData() != null && this.field.getUserData() instanceof VerticalDeflectionGroupUncertaintyType) {
				VerticalDeflectionGroupUncertaintyType uncertaintyType = (VerticalDeflectionGroupUncertaintyType)this.field.getUserData();
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

	private UncertaintyTextField uncertaintyDeflectionXField;
	private UncertaintyTextField uncertaintyDeflectionYField;
	
	private Map<Object, ProgressIndicator> databaseTransactionProgressIndicators = new HashMap<Object, ProgressIndicator>(10);
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
		
		// set focus to panel to commit text field values and to force db transaction
		UITreeBuilder.getInstance().getTree().requestFocus();
//		if (this.propertiesNode != null)
//			this.propertiesNode.requestFocus();
		
		this.setUncertaintyDeflectionY(VerticalDeflectionTreeItemValue.getDefaultUncertainty(VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y));
		this.setUncertaintyDeflectionX(VerticalDeflectionTreeItemValue.getDefaultUncertainty(VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y));
	}
	
	public void setTreeItemValue(VerticalDeflectionTreeItemValue... selectedVerticalDeflectionItemValues) {
		if (this.selectedVerticalDeflectionItemValues != selectedVerticalDeflectionItemValues) {
			this.reset();
			this.selectedVerticalDeflectionItemValues = selectedVerticalDeflectionItemValues;
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
	
	public boolean setUncertainty(VerticalDeflectionGroupUncertaintyType type, Double value) {
		switch(type) {
		case DEFLECTION_X:
			return this.setUncertaintyDeflectionX(value);
		case DEFLECTION_Y:
			return this.setUncertaintyDeflectionY(value);
		default:
			return false;
		}
	}
	
	private Node createUncertaintiesPane() {
		if (this.type == TreeItemType.STOCHASTIC_VERTICAL_DEFLECTION_LEAF) {
			double fieldMinWidth = 200;
			double fieldMaxWidth = 350;
			
			double sigmaY = VerticalDeflectionTreeItemValue.getDefaultUncertainty(VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y);
			double sigmaX = VerticalDeflectionTreeItemValue.getDefaultUncertainty(VerticalDeflectionGroupUncertaintyType.DEFLECTION_X);

			ProgressIndicator uncertaintyDeflectionYProgressIndicator = this.createDatabaseTransactionProgressIndicator(VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y);
			ProgressIndicator uncertaintyDeflectionXProgressIndicator = this.createDatabaseTransactionProgressIndicator(VerticalDeflectionGroupUncertaintyType.DEFLECTION_X); 
									
			Label uncertaintyDeflectionYLabel = new Label(i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.y.label", "\u03C3y"));
			Label uncertaintyDeflectionXLabel = new Label(i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.x.label", "\u03C3x"));
			uncertaintyDeflectionYLabel.setMinWidth(Control.USE_PREF_SIZE);
			uncertaintyDeflectionXLabel.setMinWidth(Control.USE_PREF_SIZE);
			
			this.uncertaintyDeflectionYField = new UncertaintyTextField(sigmaY, CellValueType.ANGLE_UNCERTAINTY, true, DoubleTextField.ValueSupport.EXCLUDING_INCLUDING_INTERVAL);
			this.uncertaintyDeflectionYField.setTooltip(new Tooltip(i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.y.tooltip", "Uncertainty of y-component of deflection of the vertical")));
			this.uncertaintyDeflectionYField.setUserData(VerticalDeflectionGroupUncertaintyType.DEFLECTION_Y);
			this.uncertaintyDeflectionYField.numberProperty().addListener(new NumberChangeListener(this.uncertaintyDeflectionYField));
			this.uncertaintyDeflectionYField.setMinWidth(fieldMinWidth);
			this.uncertaintyDeflectionYField.setMaxWidth(fieldMaxWidth);
						
			this.uncertaintyDeflectionXField = new UncertaintyTextField(sigmaX, CellValueType.ANGLE_UNCERTAINTY, true, DoubleTextField.ValueSupport.EXCLUDING_INCLUDING_INTERVAL);
			this.uncertaintyDeflectionXField.setTooltip(new Tooltip(i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.x.tooltip", "Uncertainty of x-component of deflection of the vertical")));
			this.uncertaintyDeflectionXField.setUserData(VerticalDeflectionGroupUncertaintyType.DEFLECTION_X);
			this.uncertaintyDeflectionXField.numberProperty().addListener(new NumberChangeListener(this.uncertaintyDeflectionXField));
			this.uncertaintyDeflectionXField.setMinWidth(fieldMinWidth);
			this.uncertaintyDeflectionXField.setMaxWidth(fieldMaxWidth);
			
			uncertaintyDeflectionYLabel.setLabelFor(this.uncertaintyDeflectionYField);
			uncertaintyDeflectionXLabel.setLabelFor(this.uncertaintyDeflectionXField);
			
			GridPane gridPane = this.createGridPane();
//			GridPane.setHgrow(uncertaintyDeflectionYLabel, Priority.SOMETIMES);
//			GridPane.setHgrow(uncertaintyDeflectionXLabel, Priority.SOMETIMES);
//			GridPane.setHgrow(this.uncertaintyDeflectionYField, Priority.ALWAYS);
//			GridPane.setHgrow(this.uncertaintyDeflectionXField, Priority.ALWAYS);
			
			gridPane.add(uncertaintyDeflectionYLabel,      0, 0);
			gridPane.add(this.uncertaintyDeflectionYField, 1, 0);
			gridPane.add(uncertaintyDeflectionYProgressIndicator, 2, 0);

			gridPane.add(uncertaintyDeflectionXLabel,      0, 1);
			gridPane.add(this.uncertaintyDeflectionXField, 1, 1);
			gridPane.add(uncertaintyDeflectionXProgressIndicator, 2, 1);

			TitledPane uncertaintiesTitledPane = this.createTitledPane(i18n.getString("UIVerticalDeflectionPropertiesPane.uncertainty.title", "Uncertainties of deflection of the vertical"));
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

		Node uncertainties = this.createUncertaintiesPane();
		if (uncertainties != null)
			content.getChildren().add(uncertainties);
		
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
	}
	
	private void setProgressIndicatorsVisible(boolean visible) {
		if (this.databaseTransactionProgressIndicators != null)
			for (ProgressIndicator progressIndicator : this.databaseTransactionProgressIndicators.values())
				progressIndicator.setVisible(visible);
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
							i18n.getString("UIVerticalDeflectionPropertiesPane.message.error.save.uncertainty.exception.header", "Error, could not save group uncertainties to database."),
							i18n.getString("UIVerticalDeflectionPropertiesPane.message.error.save.uncertainty.exception.message", "An exception has occurred during database transaction."),
							e
					);
				}
			});
		}
	}
}
