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

import java.util.Locale;
import java.util.Optional;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.i18n.I18N;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.ui.textfield.DoubleTextField;
import org.applied_geodesy.ui.textfield.DoubleTextField.ValueSupport;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.ObservableLimitedList;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class InstrumentAndReflectorHeightAdaptionDialog {
	private I18N i18n = I18N.getInstance();
	private static InstrumentAndReflectorHeightAdaptionDialog instrumentAndReflectorHeightAdaptionDialog = new InstrumentAndReflectorHeightAdaptionDialog();
	private Dialog<Void> dialog = null;
	private Window window;
	private ComboBox<String> stationNameComboBox  = new ComboBox<String>();
	private ComboBox<String> targetNameComboBox = new ComboBox<String>();
	private Label statusLabel = new Label();
	private RadioButton normalModeRadioButton;
	private RadioButton regularExpressionRadioButton;
	private CheckBox keepDialogOpenCheckBox;
	private ComboBox<ScopeType> scopeTypeComboBox;
	private DoubleTextField instrumentHeightField;
	private DoubleTextField reflectorHeightField;
	private TreeItemValue itemValue;
	private TreeItemValue selectedTreeItemValues[];
	private InstrumentAndReflectorHeightAdaptionDialog() {}

	public static void setOwner(Window owner) {
		instrumentAndReflectorHeightAdaptionDialog.window = owner;
	}

	public static Optional<Void> showAndWait(TreeItemValue itemValue, TreeItemValue... selectedTreeItemValues) {
		instrumentAndReflectorHeightAdaptionDialog.itemValue = itemValue;
		instrumentAndReflectorHeightAdaptionDialog.selectedTreeItemValues = selectedTreeItemValues;
		instrumentAndReflectorHeightAdaptionDialog.init();
		instrumentAndReflectorHeightAdaptionDialog.statusLabel.setText(null);
		
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					instrumentAndReflectorHeightAdaptionDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) instrumentAndReflectorHeightAdaptionDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return instrumentAndReflectorHeightAdaptionDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;
		
		this.dialog = new Dialog<Void>();
		this.dialog.setTitle(i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.title", "Heights of instrument and reflector"));
		this.dialog.setHeaderText(i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.header", "Adaption of heights of instrument and reflector"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		//		this.dialog.initStyle(StageStyle.UTILITY);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		Button applyButton = (Button) this.dialog.getDialogPane().lookupButton(ButtonType.OK);
		applyButton.addEventFilter(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				save();
				if (keepDialogOpenCheckBox.isSelected())
					event.consume();
			}
		});
		this.dialog.setResultConverter(new Callback<ButtonType, Void>() {
			@Override
			public Void call(ButtonType buttonType) {
//				if (buttonType == ButtonType.OK) {
//					save();
//				}
				return null;
			}
		});
	}
	
	private Node createPane() {
		this.stationNameComboBox = this.createComboBox(
				i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.station.prompt", "Id of station point (optional)"),
				i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.station.tooltip", "Enter station name (leave blank, if not required)"));
		
		this.targetNameComboBox = this.createComboBox(
				i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.target.prompt", "Id of target point (optional)"),
				i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.target.tooltip", "Enter target name (leave blank, if not required)"));
		
		this.normalModeRadioButton = this.createRadioButton(
				i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.mode.normal.label", "Normal"), 
				i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.mode.normal.tooltip", "If selected, normal search mode will be applied"));
		
		this.regularExpressionRadioButton = this.createRadioButton(
				i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.mode.regex.label", "Regular expression"), 
				i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.mode.regex.tooltip", "If selected, regular expression mode will be applied"));
		
		this.keepDialogOpenCheckBox = this.createCheckBox(
				i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.keep_open.label", "Keep dialog open after modification"), 
				i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.keep_open.tooltip", "If selected, dialog will be kept open after data modification"));
		this.keepDialogOpenCheckBox.setSelected(false);
		
		this.scopeTypeComboBox = this.createScopeTypeComboBox(ScopeType.SELECTION, i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.scope.tooltip", "Select scope of application"));

		
		this.instrumentHeightField = this.createDoubleTextField(CellValueType.LENGTH, true, ValueSupport.NULL_VALUE_SUPPORT, i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.instrument.height.tooltip", "Set new instrument height (leave blank, if not required)"), i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.instrument.height.prompt", "New instrument height (optional)"));
		this.reflectorHeightField  = this.createDoubleTextField(CellValueType.LENGTH, true, ValueSupport.NULL_VALUE_SUPPORT, i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.reflector.height.tooltip", "Set new reflector height (leave blank, if not required)"), i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.reflector.height.prompt", "New reflector height (optional)"));
		
		Label scopeLabel       = new Label(i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.scope.label", "Scope:"));
		Label stationNameLabel = new Label(i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.station.label", "Station name:"));
		Label targetNameLabel  = new Label(i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.target.label", "Target name:"));
		Label modeLabel        = new Label(i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.mode.label", "Mode:"));
		Label instrumentHeightLabel = new Label(i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.instrument.height.label", "Instrument height:"));
		Label reflectorHeightLabel  = new Label(i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.reflector.height.label", "Reflector height:"));
		
		scopeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		stationNameLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		targetNameLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		instrumentHeightLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		reflectorHeightLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
	
		scopeLabel.setLabelFor(this.scopeTypeComboBox);
		stationNameLabel.setLabelFor(this.stationNameComboBox);
		targetNameLabel.setLabelFor(this.targetNameComboBox);
		instrumentHeightLabel.setLabelFor(this.instrumentHeightField);
		reflectorHeightLabel.setLabelFor(this.reflectorHeightField);
		
		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(this.normalModeRadioButton, this.regularExpressionRadioButton);
		this.normalModeRadioButton.setSelected(true);
		
		HBox hbox = new HBox(10, this.normalModeRadioButton, this.regularExpressionRadioButton);
		hbox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(5,15,5,15)); 
		//gridPane.setGridLinesVisible(true);
		
		GridPane.setHgrow(scopeLabel,       Priority.NEVER);
		GridPane.setHgrow(stationNameLabel, Priority.NEVER);
		GridPane.setHgrow(targetNameLabel,  Priority.NEVER);
		GridPane.setHgrow(modeLabel,        Priority.NEVER);
		
		GridPane.setHgrow(this.stationNameComboBox,    Priority.ALWAYS);
		GridPane.setHgrow(this.targetNameComboBox,     Priority.ALWAYS);
		GridPane.setHgrow(hbox,                        Priority.ALWAYS);
		GridPane.setHgrow(this.scopeTypeComboBox,      Priority.ALWAYS);
		GridPane.setHgrow(this.keepDialogOpenCheckBox, Priority.ALWAYS);
		GridPane.setHgrow(this.statusLabel,            Priority.ALWAYS);
				
		int row = 1;
		gridPane.add(scopeLabel,                  0, row);
		gridPane.add(this.scopeTypeComboBox,      1, row++, 1, 1);
		
		gridPane.add(stationNameLabel,            0, row);
		gridPane.add(this.stationNameComboBox,    1, row++, 1, 1);

		gridPane.add(targetNameLabel,             0, row);
		gridPane.add(this.targetNameComboBox,     1, row++, 1, 1);
		
		gridPane.add(instrumentHeightLabel,       0, row);
		gridPane.add(this.instrumentHeightField,  1, row++, 1, 1);
		
		gridPane.add(reflectorHeightLabel,        0, row);
		gridPane.add(this.reflectorHeightField,   1, row++, 1, 1);
		
		gridPane.add(modeLabel,                   0, row);
		gridPane.add(hbox,                        1, row++, 1, 1);
		
		gridPane.add(this.keepDialogOpenCheckBox, 1, row++, 1, 1);
		gridPane.add(this.statusLabel,            1, row++, 1, 1);
			
		Platform.runLater(new Runnable() {
			@Override public void run() {
				stationNameComboBox.requestFocus();
			}
		});
		
		return gridPane;
		
	}
	
	private DoubleTextField createDoubleTextField(CellValueType type, boolean displayUnit, ValueSupport valueSupport, String tooltipText, String promptText) {
		DoubleTextField field = new DoubleTextField(null, type, displayUnit, valueSupport);
		field.setTooltip(new Tooltip(tooltipText));
		field.setPromptText(promptText);
		field.setMinWidth(200);
		field.setMaxWidth(350);
		return field;
	}
	
	private RadioButton createRadioButton(String text, String tooltip) {
		Label label = new Label(text);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setPadding(new Insets(0,0,0,3));
		RadioButton radioButton = new RadioButton();
		radioButton.setGraphic(label);
		radioButton.setTooltip(new Tooltip(tooltip));
		radioButton.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		radioButton.setMaxWidth(Double.MAX_VALUE);
		return radioButton;
	}
	
	private CheckBox createCheckBox(String text, String tooltip) {
		Label label = new Label(text);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setPadding(new Insets(0,0,0,3));
		CheckBox checkBox = new CheckBox();
		checkBox.setGraphic(label);
		checkBox.setTooltip(new Tooltip(tooltip));
		checkBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		checkBox.setMaxWidth(Double.MAX_VALUE);
		return checkBox;
	}
	
	private ComboBox<ScopeType> createScopeTypeComboBox(ScopeType item, String tooltip) {
		ComboBox<ScopeType> typeComboBox = new ComboBox<ScopeType>();
		typeComboBox.getItems().setAll(ScopeType.values());
		typeComboBox.getSelectionModel().select(item);
		typeComboBox.setConverter(new StringConverter<ScopeType>() {

			@Override
			public String toString(ScopeType type) {
				if (type == null)
					return null;
				switch(type) {
				case SELECTION:
					return i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.scope.selection.label", "Selected items");
				case PROJECT:
					return i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.scope.project.label", "Whole project");
				case REFERENCE_EPOCH:
					return i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.scope.reference_epoch.label", "Observations of reference epoch");
				case CONTROL_EPOCH:
					return i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.scope.control_epoch.label", "Observations of control epoch");
				}
				return null;
			}

			@Override
			public ScopeType fromString(String string) {
				return ScopeType.valueOf(string);
			}
		});
		typeComboBox.setTooltip(new Tooltip(tooltip));
		typeComboBox.setMinWidth(150);
		typeComboBox.setMaxWidth(Double.MAX_VALUE);
		return typeComboBox;
	}
	
	private ComboBox<String> createComboBox(String promtText, String tooltip) {
		ComboBox<String> comboBox = new ComboBox<String>(new ObservableLimitedList<String>(150));
		comboBox.setEditable(true);
		comboBox.setPromptText(promtText);
		comboBox.setTooltip(new Tooltip(tooltip));
		comboBox.setMinSize(250, Control.USE_PREF_SIZE);
		comboBox.setMaxWidth(Double.MAX_VALUE);
		return comboBox;
	}
	
	private void save() {
		try {
			this.statusLabel.setText(null);
			this.stationNameComboBox.commitValue();
			this.targetNameComboBox.commitValue();
			this.instrumentHeightField.commitValue();
			this.reflectorHeightField.commitValue();
			
			String stationName = this.stationNameComboBox.getValue();
			String targetName  = this.targetNameComboBox.getValue();
			
			Double instrumentHeight = this.instrumentHeightField.getNumber();
			Double reflectorHeight  = this.reflectorHeightField.getNumber();

			if (instrumentHeight == null && reflectorHeight == null)
				return;
			
			stationName = stationName == null || stationName.trim().isEmpty() ? null : stationName;
			targetName  = targetName == null  || targetName.trim().isEmpty() ? null : targetName;

			// add new items to combo boxes
			if (!this.stationNameComboBox.getItems().contains(stationName == null ? "" : stationName))
				this.stationNameComboBox.getItems().add(stationName == null ? "" : stationName);
			this.stationNameComboBox.setValue(stationName == null ? "" : stationName);
			
			if (!this.targetNameComboBox.getItems().contains(targetName == null ? "" : targetName))
				this.targetNameComboBox.getItems().add(targetName == null ? "" : targetName);
			this.targetNameComboBox.setValue(targetName == null ? "" : targetName);
			
			ScopeType scopeType = this.scopeTypeComboBox.getValue();
			boolean regExp = this.regularExpressionRadioButton.isSelected();
			
			// masking values
			if (!regExp && stationName != null)
				stationName = "^\\Q"+stationName+"\\E";
			else if (stationName == null)
				stationName = ".*";
			
			if (!regExp && targetName != null)
				targetName = "^\\Q"+targetName+"\\E";
			else if (targetName == null)
				targetName = ".*";

			int rows = SQLManager.getInstance().adaptInstrumentAndReflectorHeights(stationName, targetName, instrumentHeight, reflectorHeight, scopeType, this.itemValue, this.selectedTreeItemValues);
			if (this.keepDialogOpenCheckBox.isSelected())
				this.statusLabel.setText(String.format(Locale.ENGLISH, i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.result.label", "%d row(s) edited\u2026"), rows));
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.message.error.save.exception.header", "Error, could not save renamed points to database."),
							i18n.getString("InstrumentAndReflectorHeightAdaptionDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
	
}
