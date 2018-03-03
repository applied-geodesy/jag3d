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
import org.applied_geodesy.jag3d.ui.textfield.LimitedTextField;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
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
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.Pair;

public class SearchAndReplaceDialog {

	private I18N i18n = I18N.getInstance();
	private static SearchAndReplaceDialog searchAndReplaceDialog = new SearchAndReplaceDialog();
	private Dialog<Pair<String, String>> dialog = null;
	private Window window;
	private LimitedTextField searchTextField;
	private LimitedTextField replaceTextField;
	private RadioButton normalModeRadioButton;
	private RadioButton regularExpressionRadioButton;
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

		this.searchTextField = this.createLimitedTextField(
				255,
				i18n.getString("SearchAndReplaceDialog.search.promt", "Old point name"),
				i18n.getString("SearchAndReplaceDialog.search.tooltip", "Enter old point name"));
		
		this.replaceTextField = this.createLimitedTextField(
				255,
				i18n.getString("SearchAndReplaceDialog.replace.promt", "New point name"),
				i18n.getString("SearchAndReplaceDialog.replace.tooltip", "Enter new point name"));
		
		this.normalModeRadioButton = this.createRadioButton(
				i18n.getString("SearchAndReplaceDialog.mode.normal.label", "Normal"), 
				i18n.getString("SearchAndReplaceDialog.mode.normal.tooltip", "If selected, normal search and replace mode will be applied"));
		
		this.regularExpressionRadioButton = this.createRadioButton(
				i18n.getString("SearchAndReplaceDialog.mode.regex.label", "Regular expression"), 
				i18n.getString("SearchAndReplaceDialog.mode.regex.tooltip", "If selected, regular expression mode will be applied"));
		
		Label searchLabel  = new Label(i18n.getString("SearchAndReplaceDialog.search.label", "Find what:"));
		Label replaceLabel = new Label(i18n.getString("SearchAndReplaceDialog.replace.label", "Replace with:"));
		Label modeLabel    = new Label(i18n.getString("SearchAndReplaceDialog.mode.label", "Mode:"));
		
		searchLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		replaceLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		searchLabel.setLabelFor(this.searchTextField);
		replaceLabel.setLabelFor(this.replaceTextField);
		
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
		
		GridPane.setHgrow(searchLabel,  Priority.NEVER);
		GridPane.setHgrow(replaceLabel, Priority.NEVER);
		
		GridPane.setHgrow(this.searchTextField,  Priority.ALWAYS);
		GridPane.setHgrow(this.replaceTextField, Priority.ALWAYS);
				
		int row = 1;
		gridPane.add(searchLabel,           0, row);
		gridPane.add(this.searchTextField,  1, row++);

		gridPane.add(replaceLabel,          0, row);
		gridPane.add(this.replaceTextField, 1, row++);
		
		gridPane.add(modeLabel,             0, row);
		gridPane.add(hbox,                  1, row++);
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				searchTextField.requestFocus();
			}
		});
		
		return gridPane;
	}
	
	private LimitedTextField createLimitedTextField(int size, String promtText, String tooltip) {
		LimitedTextField textField = new LimitedTextField(size);
		textField.setPromptText(promtText);
		textField.setTooltip(new Tooltip(tooltip));
		textField.setMinWidth(250);
		textField.setMaxWidth(Double.MAX_VALUE);
		return textField;
	}
	
	private RadioButton createRadioButton(String text, String tooltip) {
		Label label = new Label(text);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		RadioButton radioButton = new RadioButton();
		radioButton.setGraphic(label);
		radioButton.setTooltip(new Tooltip(tooltip));
		radioButton.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		radioButton.setMaxWidth(Double.MAX_VALUE);
		return radioButton;
	}

	private void save() {
		try {
			String search  = this.searchTextField.getText();
			String replace = this.replaceTextField.getText();
			boolean regExp = this.regularExpressionRadioButton.isSelected();
			
			// masking values
			if (!regExp)
				search = "^\\Q"+search+"\\E";

			SQLManager.getInstance().searchAndReplacePointNames(search, replace, this.itemValue, this.selectedTreeItemValues);
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
