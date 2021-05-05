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

import java.util.Optional;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.util.ObservableLimitedList;
import org.applied_geodesy.jag3d.ui.i18n.I18N;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
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
import javafx.util.Pair;
import javafx.util.StringConverter;

public class SearchAndReplaceDialog {

	public enum ScopeType {
		SELECTION,
		PROJECT,
		REFERENCE_EPOCH,
		CONTROL_EPOCH;
	}
	
	private I18N i18n = I18N.getInstance();
	private static SearchAndReplaceDialog searchAndReplaceDialog = new SearchAndReplaceDialog();
	private Dialog<Pair<String, String>> dialog = null;
	private Window window;
	private ComboBox<String> searchComboBox  = new ComboBox<String>();
	private ComboBox<String> replaceComboBox = new ComboBox<String>();
	private RadioButton normalModeRadioButton;
	private RadioButton regularExpressionRadioButton;
	private ComboBox<ScopeType> scopeTypeComboBox;	
	private TreeItemValue itemValue;
	private TreeItemValue selectedTreeItemValues[];
	private SearchAndReplaceDialog() {}

	public static void setOwner(Window owner) {
		searchAndReplaceDialog.window = owner;
	}

	public static Optional<Pair<String, String>> showAndWait(TreeItemValue itemValue, TreeItemValue... selectedTreeItemValues) {
		searchAndReplaceDialog.itemValue = itemValue;
		searchAndReplaceDialog.selectedTreeItemValues = selectedTreeItemValues;
		searchAndReplaceDialog.init();

		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					searchAndReplaceDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) searchAndReplaceDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return searchAndReplaceDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<Pair<String, String>>();
		this.dialog.setTitle(i18n.getString("SearchAndReplaceDialog.title", "Search and replace"));
		this.dialog.setHeaderText(i18n.getString("SearchAndReplaceDialog.header", "Search and replace point names"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		//		this.dialog.initStyle(StageStyle.UTILITY);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, Pair<String, String>>() {
			@Override
			public Pair<String, String> call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					save();
					return new Pair<String, String>(normalModeRadioButton.getText(), regularExpressionRadioButton.getText());
				}
				return null;
			}
		});
	}

	private Node createPane() {

		this.searchComboBox = this.createComboBox(
				i18n.getString("SearchAndReplaceDialog.search.prompt", "Old point name"),
				i18n.getString("SearchAndReplaceDialog.search.tooltip", "Enter old point name"));
		
		this.replaceComboBox = this.createComboBox(
				i18n.getString("SearchAndReplaceDialog.replace.prompt", "Old point name"),
				i18n.getString("SearchAndReplaceDialog.replace.tooltip", "Enter old point name"));
		
		this.normalModeRadioButton = this.createRadioButton(
				i18n.getString("SearchAndReplaceDialog.mode.normal.label", "Normal"), 
				i18n.getString("SearchAndReplaceDialog.mode.normal.tooltip", "If selected, normal search and replace mode will be applied"));
		
		this.regularExpressionRadioButton = this.createRadioButton(
				i18n.getString("SearchAndReplaceDialog.mode.regex.label", "Regular expression"), 
				i18n.getString("SearchAndReplaceDialog.mode.regex.tooltip", "If selected, regular expression mode will be applied"));
		
		this.scopeTypeComboBox = this.createScopeTypeComboBox(ScopeType.SELECTION, i18n.getString("SearchAndReplaceDialog.scope.tooltip", "Select scope of application"));

		Label scopeLabel  = new Label(i18n.getString("SearchAndReplaceDialog.scope.label", "Scope:"));
		Label searchLabel  = new Label(i18n.getString("SearchAndReplaceDialog.search.label", "Find what:"));
		Label replaceLabel = new Label(i18n.getString("SearchAndReplaceDialog.replace.label", "Replace with:"));
		Label modeLabel    = new Label(i18n.getString("SearchAndReplaceDialog.mode.label", "Mode:"));
		
		scopeLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		searchLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		replaceLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		scopeLabel.setLabelFor(this.scopeTypeComboBox);
		searchLabel.setLabelFor(this.searchComboBox);
		replaceLabel.setLabelFor(this.replaceComboBox);
		
		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(this.normalModeRadioButton, this.regularExpressionRadioButton);
		this.normalModeRadioButton.setSelected(true);
		
		HBox hbox = new HBox(10, this.normalModeRadioButton, this.regularExpressionRadioButton);
		hbox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(20);
		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setPadding(new Insets(5,15,5,15)); 
		//gridPane.setGridLinesVisible(true);
		
		GridPane.setHgrow(scopeLabel,   Priority.NEVER);
		GridPane.setHgrow(searchLabel,  Priority.NEVER);
		GridPane.setHgrow(replaceLabel, Priority.NEVER);
		GridPane.setHgrow(modeLabel,    Priority.NEVER);
		
		GridPane.setHgrow(this.searchComboBox,  Priority.ALWAYS);
		GridPane.setHgrow(this.replaceComboBox, Priority.ALWAYS);
		GridPane.setHgrow(hbox,                 Priority.ALWAYS);
		GridPane.setHgrow(this.scopeTypeComboBox, Priority.ALWAYS);
				
		int row = 1;
		gridPane.add(scopeLabel,                   0, row);
		gridPane.add(this.scopeTypeComboBox, 1, row++);
		
		gridPane.add(searchLabel,           0, row);
		gridPane.add(this.searchComboBox,   1, row++);

		gridPane.add(replaceLabel,          0, row);
		gridPane.add(this.replaceComboBox,  1, row++);
		
		gridPane.add(modeLabel,             0, row);
		gridPane.add(hbox,                  1, row++);

			
		Platform.runLater(new Runnable() {
			@Override public void run() {
				searchComboBox.requestFocus();
			}
		});
		
		return gridPane;
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
					return i18n.getString("SearchAndReplaceDialog.scope.selection.label", "Selected items");
				case PROJECT:
					return i18n.getString("SearchAndReplaceDialog.scope.project.label", "Whole project");
				case REFERENCE_EPOCH:
					return i18n.getString("SearchAndReplaceDialog.scope.reference_epoch.label", "Observations of reference epoch");
				case CONTROL_EPOCH:
					return i18n.getString("SearchAndReplaceDialog.scope.control_epoch.label", "Observations of control epoch");
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

	private void save() {
		try {
			String search  = this.searchComboBox.getValue();
			String replace = this.replaceComboBox.getValue();

			search  = search == null  ? "" : search;
			replace = replace == null ? "" : replace;

			// add new items to combo boxes
			if (!search.isEmpty() && !this.searchComboBox.getItems().contains(search))
				this.searchComboBox.getItems().add(search);
			this.searchComboBox.setValue(search);
			
			if (!replace.isEmpty() && !this.replaceComboBox.getItems().contains(replace))
				this.replaceComboBox.getItems().add(replace);
			this.replaceComboBox.setValue(replace);
			
			ScopeType scopeType = this.scopeTypeComboBox.getValue();
			boolean regExp = this.regularExpressionRadioButton.isSelected();
			
			// masking values
			if (!regExp)
				search = "^\\Q"+search+"\\E";

			SQLManager.getInstance().searchAndReplacePointNames(search, replace, scopeType, this.itemValue, this.selectedTreeItemValues);
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("SearchAndReplaceDialog.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("SearchAndReplaceDialog.message.error.save.exception.header", "Error, could not save renamed points to database."),
							i18n.getString("SearchAndReplaceDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
}
