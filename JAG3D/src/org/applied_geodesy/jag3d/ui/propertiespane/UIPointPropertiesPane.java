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

import org.applied_geodesy.adjustment.network.PointGroupUncertaintyType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dialog.OptionDialog;
import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.jag3d.ui.textfield.DoubleTextField;
import org.applied_geodesy.jag3d.ui.textfield.UncertaintyTextField;
import org.applied_geodesy.jag3d.ui.tree.PointTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class UIPointPropertiesPane {

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

	private class BooleanChangeListener implements ChangeListener<Boolean> {
		private final ButtonBase button;

		private BooleanChangeListener(ButtonBase button) {
			this.button = button;
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (!ignoreValueUpdate && this.button == deflectionCheckBox) {
				save();
			}
		}
	}


	private I18N i18n = I18N.getInstance();
	private Node propertiesNode = null;
	private final TreeItemType type;

	private UncertaintyTextField uncertaintyCoordinateXField;
	private UncertaintyTextField uncertaintyCoordinateYField;
	private UncertaintyTextField uncertaintyCoordinateZField;

	private CheckBox deflectionCheckBox;
	private UncertaintyTextField uncertaintyDeflectionXField;
	private UncertaintyTextField uncertaintyDeflectionYField;

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
	
	public void reset() {		
		this.setUncertaintyY(PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.CONSTANT_Y));
		this.setUncertaintyX(PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.CONSTANT_X));
		this.setUncertaintyZ(PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.CONSTANT_Z));
		
		this.setUncertaintyDeflectionY(PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.DEFLECTION_Y));
		this.setUncertaintyDeflectionX(PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.DEFLECTION_Y));
		
		this.setDeflection(false);
	}
	
	public void setTreeItemValue(PointTreeItemValue... selectedPointItemValues) {
		this.selectedPointItemValues = selectedPointItemValues;
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
	
	public boolean setDeflection(Boolean value) {
		if (this.deflectionCheckBox == null)
			return false;
		this.ignoreValueUpdate = true;
		this.deflectionCheckBox.setSelected(value != null && value == Boolean.TRUE);
		this.ignoreValueUpdate = false;
		return true;
	}

	public boolean setUncertainty(PointGroupUncertaintyType type, Double value) {
		switch(type) {
		case CONSTANT_X:
			return this.setUncertaintyX(value);
		case CONSTANT_Y:
			return this.setUncertaintyY(value);
		case CONSTANT_Z:
			return this.setUncertaintyZ(value);
		case DEFLECTION_X:
			return this.setUncertaintyDeflectionX(value);
		case DEFLECTION_Y:
			return this.setUncertaintyDeflectionY(value);
		default:
			return false;
		}
	}

	private Node createCoordinateUncertaintiesPane() {
		if (this.type == TreeItemType.STOCHASTIC_POINT_1D_LEAF || this.type == TreeItemType.STOCHASTIC_POINT_2D_LEAF || this.type == TreeItemType.STOCHASTIC_POINT_3D_LEAF) {
			GridPane gridPane = this.createGridPane();

			double sigmaY = PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.CONSTANT_Y);
			double sigmaX = PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.CONSTANT_X);
			double sigmaZ = PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.CONSTANT_Z);

			int row = 0;

			if (this.type == TreeItemType.STOCHASTIC_POINT_2D_LEAF || this.type == TreeItemType.STOCHASTIC_POINT_3D_LEAF) {
				Label uncertaintyCoordinateYLabel = new Label(i18n.getString("UIPointPropertiesPane.uncertainty.point.y.label", "\u03C3y"));
				this.uncertaintyCoordinateYField = new UncertaintyTextField(sigmaY, CellValueType.LENGTH_UNCERTAINTY, true, DoubleTextField.ValueSupport.GREATER_THAN_ZERO);
				this.uncertaintyCoordinateYField.setTooltip(new Tooltip(i18n.getString("UIPointPropertiesPane.uncertainty.point.y.tooltip", "Uncertainty of y-component of stochastic points")));
				this.uncertaintyCoordinateYField.setUserData(PointGroupUncertaintyType.CONSTANT_Y);
				this.uncertaintyCoordinateYField.numberProperty().addListener(new NumberChangeListener(this.uncertaintyCoordinateYField));
				this.uncertaintyCoordinateYField.setMinWidth(150);
				this.uncertaintyCoordinateYField.setMaxWidth(250);
				
				Label uncertaintyCoordinateXLabel = new Label(i18n.getString("UIPointPropertiesPane.uncertainty.point.x.label", "\u03C3x"));
				this.uncertaintyCoordinateXField = new UncertaintyTextField(sigmaX, CellValueType.LENGTH_UNCERTAINTY, true, DoubleTextField.ValueSupport.GREATER_THAN_ZERO);
				this.uncertaintyCoordinateXField.setTooltip(new Tooltip(i18n.getString("UIPointPropertiesPane.uncertainty.point.x.tooltip", "Uncertainty of x-component of stochastic points")));
				this.uncertaintyCoordinateXField.setUserData(PointGroupUncertaintyType.CONSTANT_X);
				this.uncertaintyCoordinateXField.numberProperty().addListener(new NumberChangeListener(this.uncertaintyCoordinateXField));
				this.uncertaintyCoordinateXField.setMinWidth(150);
				this.uncertaintyCoordinateXField.setMaxWidth(250);
				
				uncertaintyCoordinateYLabel.setLabelFor(this.uncertaintyCoordinateYField);
				uncertaintyCoordinateXLabel.setLabelFor(this.uncertaintyCoordinateXField);
				
				uncertaintyCoordinateYLabel.setMinWidth(Control.USE_PREF_SIZE);
				uncertaintyCoordinateXLabel.setMinWidth(Control.USE_PREF_SIZE);
				
				GridPane.setHgrow(uncertaintyCoordinateYLabel, Priority.SOMETIMES);
				GridPane.setHgrow(uncertaintyCoordinateXLabel, Priority.SOMETIMES);
				GridPane.setHgrow(this.uncertaintyCoordinateYField, Priority.ALWAYS);
				GridPane.setHgrow(this.uncertaintyCoordinateXField, Priority.ALWAYS);

				gridPane.add(uncertaintyCoordinateYLabel,      0, row);
				gridPane.add(this.uncertaintyCoordinateYField, 1, row++);

				gridPane.add(uncertaintyCoordinateXLabel,      0, row);
				gridPane.add(this.uncertaintyCoordinateXField, 1, row++);
			}

			if (this.type == TreeItemType.STOCHASTIC_POINT_1D_LEAF || this.type == TreeItemType.STOCHASTIC_POINT_3D_LEAF) {		
				Label uncertaintyCoordinateZLabel = new Label(i18n.getString("UIPointPropertiesPane.uncertainty.point.z.label", "\u03C3z"));
				this.uncertaintyCoordinateZField = new UncertaintyTextField(sigmaZ, CellValueType.LENGTH_UNCERTAINTY, true, DoubleTextField.ValueSupport.GREATER_THAN_ZERO);
				this.uncertaintyCoordinateZField.setTooltip(new Tooltip(i18n.getString("UIPointPropertiesPane.uncertainty.point.z.tooltip", "Uncertainty of z-component of stochastic points")));
				this.uncertaintyCoordinateZField.setUserData(PointGroupUncertaintyType.CONSTANT_Z);
				this.uncertaintyCoordinateZField.numberProperty().addListener(new NumberChangeListener(this.uncertaintyCoordinateZField));
				this.uncertaintyCoordinateZField.setMinWidth(150);
				this.uncertaintyCoordinateZField.setMaxWidth(250);
				
				uncertaintyCoordinateZLabel.setLabelFor(this.uncertaintyCoordinateZField);
				
				GridPane.setHgrow(uncertaintyCoordinateZLabel, Priority.SOMETIMES);
				GridPane.setHgrow(this.uncertaintyCoordinateZField, Priority.ALWAYS);
				
				gridPane.add(uncertaintyCoordinateZLabel,      0, row);
				gridPane.add(this.uncertaintyCoordinateZField, 1, row++);
			}

			TitledPane uncertaintiesTitledPane = this.createTitledPane(i18n.getString("UIPointPropertiesPane.uncertainty.title", "Uncertainties of stochastic points"));
			uncertaintiesTitledPane.setContent(gridPane);
			return uncertaintiesTitledPane;
		}
		return null;
	}

	private Node createDeflectionUncertaintiesPane() {
		if (this.type == TreeItemType.STOCHASTIC_POINT_3D_LEAF) {
			double sigmaY = PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.DEFLECTION_Y);
			double sigmaX = PointTreeItemValue.getDefaultUncertainty(PointGroupUncertaintyType.DEFLECTION_Y);

			Label uncertaintyDeflectionYLabel = new Label(i18n.getString("UIPointPropertiesPane.uncertainty.deflection.y.label", "\u03C3y"));
			Label uncertaintyDeflectionXLabel = new Label(i18n.getString("UIPointPropertiesPane.uncertainty.deflection.x.label", "\u03C3x"));
			uncertaintyDeflectionYLabel.setMinWidth(Control.USE_PREF_SIZE);
			uncertaintyDeflectionXLabel.setMinWidth(Control.USE_PREF_SIZE);
			
			this.uncertaintyDeflectionYField = new UncertaintyTextField(sigmaY, CellValueType.ANGLE_UNCERTAINTY, true, DoubleTextField.ValueSupport.GREATER_THAN_ZERO);
			this.uncertaintyDeflectionYField.setTooltip(new Tooltip(i18n.getString("UIPointPropertiesPane.uncertainty.deflection.y.tooltip", "Uncertainty of y-component of deflections of vertical")));
			this.uncertaintyDeflectionYField.setUserData(PointGroupUncertaintyType.DEFLECTION_Y);
			this.uncertaintyDeflectionYField.numberProperty().addListener(new NumberChangeListener(this.uncertaintyDeflectionYField));
			this.uncertaintyDeflectionYField.setMinWidth(150);
			this.uncertaintyDeflectionYField.setMaxWidth(250);
						
			this.uncertaintyDeflectionXField = new UncertaintyTextField(sigmaX, CellValueType.ANGLE_UNCERTAINTY, true, DoubleTextField.ValueSupport.GREATER_THAN_ZERO);
			this.uncertaintyDeflectionXField.setTooltip(new Tooltip(i18n.getString("UIPointPropertiesPane.uncertainty.deflection.x.tooltip", "Uncertainty of x-component of deflections of vertical")));
			this.uncertaintyDeflectionXField.setUserData(PointGroupUncertaintyType.DEFLECTION_X);
			this.uncertaintyDeflectionXField.numberProperty().addListener(new NumberChangeListener(this.uncertaintyDeflectionXField));
			this.uncertaintyDeflectionXField.setMinWidth(150);
			this.uncertaintyDeflectionXField.setMaxWidth(250);
			
			uncertaintyDeflectionYLabel.setLabelFor(this.uncertaintyDeflectionYField);
			uncertaintyDeflectionXLabel.setLabelFor(this.uncertaintyDeflectionXField);
			
			GridPane gridPane = this.createGridPane();
			GridPane.setHgrow(uncertaintyDeflectionYLabel, Priority.SOMETIMES);
			GridPane.setHgrow(uncertaintyDeflectionXLabel, Priority.SOMETIMES);
			GridPane.setHgrow(this.uncertaintyDeflectionYField, Priority.ALWAYS);
			GridPane.setHgrow(this.uncertaintyDeflectionXField, Priority.ALWAYS);
			
			gridPane.add(uncertaintyDeflectionYLabel,      0, 0);
			gridPane.add(this.uncertaintyDeflectionYField, 1, 0);

			gridPane.add(uncertaintyDeflectionXLabel,      0, 1);
			gridPane.add(this.uncertaintyDeflectionXField, 1, 1);

			TitledPane uncertaintiesTitledPane = this.createTitledPane(i18n.getString("UIPointPropertiesPane.uncertainty.deflection.title", "Uncertainties of deflections of vertical"));
			uncertaintiesTitledPane.setContent(gridPane);
			return uncertaintiesTitledPane;
		}
		return null;
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

	private CheckBox createDeflectionCheckBox() {
		switch(this.type) {
		case REFERENCE_POINT_3D_LEAF:
		case STOCHASTIC_POINT_3D_LEAF:
		case DATUM_POINT_3D_LEAF:
		case NEW_POINT_3D_LEAF:
			this.deflectionCheckBox = new CheckBox(i18n.getString("UIPointPropertiesPane.deflection.label", "Consider deflections of the vertical"));
			this.deflectionCheckBox.setTooltip(new Tooltip(i18n.getString("UIPointPropertiesPane.deflection.tooltip", "If checked, deflections of the vertical are considered during adjustment")));
			this.deflectionCheckBox.setSelected(false);
			this.deflectionCheckBox.setMinWidth(Control.USE_PREF_SIZE);
			this.deflectionCheckBox.setPadding(new Insets(10, 0, 5, 0)); // oben, rechts, unten, links
			this.deflectionCheckBox.selectedProperty().addListener(new BooleanChangeListener(this.deflectionCheckBox));
			return this.deflectionCheckBox;
		default:
			return null;
		}
	}

	private void init() {
		VBox content = new VBox();

		Node coordinateUncertainties = this.createCoordinateUncertaintiesPane();
		Node deflectionUncertainties = this.createDeflectionUncertaintiesPane();
		this.deflectionCheckBox      = this.createDeflectionCheckBox();
		if (coordinateUncertainties != null)
			content.getChildren().add(coordinateUncertainties);
		if (this.deflectionCheckBox != null)
			content.getChildren().add(this.deflectionCheckBox);
		if (deflectionUncertainties != null)
			content.getChildren().add(deflectionUncertainties);
		
		this.reset();
		
		ScrollPane scroller = new ScrollPane(content);
		scroller.setPadding(new Insets(20, 50, 20, 50)); // oben, links, unten, rechts
		scroller.setFitToHeight(true);
		scroller.setFitToWidth(true);
		this.propertiesNode = scroller;
	}

	private void save(PointGroupUncertaintyType uncertaintyType) {
		try {
			Double value = null;
			switch(uncertaintyType) {
			case CONSTANT_Y:
				value = this.uncertaintyCoordinateYField.getNumber();
				break;
			case CONSTANT_X:
				value = this.uncertaintyCoordinateXField.getNumber();
				break;
			case CONSTANT_Z:
				value = this.uncertaintyCoordinateZField.getNumber();
				break;
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

			if (value != null && value.doubleValue() > 0 && this.selectedPointItemValues != null && this.selectedPointItemValues.length > 0) {
				SQLManager.getInstance().saveUncertainty(uncertaintyType, value.doubleValue(), this.selectedPointItemValues);
			}

		} catch (Exception e) {
			e.printStackTrace();
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

	private void save() {
		try {
			if (this.selectedPointItemValues != null && this.selectedPointItemValues.length > 0)
				SQLManager.getInstance().saveDeflection(this.deflectionCheckBox.isSelected(), this.selectedPointItemValues);
		} catch (Exception e) {
			this.setDeflection(!this.deflectionCheckBox.isSelected());
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIPointPropertiesPane.message.error.save.parameter.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIPointPropertiesPane.message.error.save.parameter.exception.header", "Error, could not save properties of vertical deflection to database."),
							i18n.getString("UIPointPropertiesPane.message.error.save.parameter.exception.message", "An exception has occurred during database transaction."),
							e
					);
				}
			});
		}
	}
}
