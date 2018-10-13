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

package org.applied_geodesy.jag3d.ui.metadata;

import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateChangedListener;
import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateEvent;
import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dialog.OptionDialog;
import org.applied_geodesy.jag3d.ui.textfield.LimitedTextArea;
import org.applied_geodesy.jag3d.ui.textfield.LimitedTextField;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class UIMetaDataPaneBuilder {
	
	private class DatabaseStateChangedListener implements ProjectDatabaseStateChangedListener {
		@Override
		public void projectDatabaseStateChanged(ProjectDatabaseStateEvent evt) {
			if (metaDataNode != null) {
				boolean disable = evt.getEventType() != ProjectDatabaseStateType.OPENED;
				disableComponents(disable);
				if (evt.getEventType() == ProjectDatabaseStateType.CLOSED)
					clearComponents();
			}
		}
	}
	
	private class MetaDataChangeListener implements ChangeListener<Boolean>, EventHandler<ActionEvent> {

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (!newValue) // focus lost
				save();
		}

		@Override
		public void handle(ActionEvent event) {
			save();
		}
	}

	private I18N i18n = I18N.getInstance();
	private static UIMetaDataPaneBuilder metaDataPane = new UIMetaDataPaneBuilder();
	private Node metaDataNode = null;
	private DatePicker datePicker;
	private LimitedTextField nameLimitedTextField;
	private LimitedTextField operatorLimitedTextField;
	private LimitedTextField customerIdLimitedTextField;
	private LimitedTextField projectIdLimitedTextField;
	private LimitedTextArea descriptionTextArea;
	private final MetaData metaData = new MetaData();

	private UIMetaDataPaneBuilder() {}
	
	public static UIMetaDataPaneBuilder getInstance() {
		metaDataPane.init();
		return metaDataPane;
	}
	
	public Node getNode() {
		return this.metaDataNode;
	}
	
	private void init() {
		if (this.metaDataNode != null)
			return;
		SQLManager.getInstance().addProjectDatabaseStateChangedListener(new DatabaseStateChangedListener());
		GridPane gridPane = this.createGridPane();

		Label nameLabel = new Label(i18n.getString("UIMetaDataPane.name.label", "Project name:"));
		this.nameLimitedTextField = this.createLimitedTextField(
				i18n.getString("UIMetaDataPane.name.tooltip", "Name of current project"), 
				i18n.getString("UIMetaDataPane.name.prompt", "Name of project")
		);
		nameLabel.setLabelFor(this.nameLimitedTextField);
		nameLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label operatorLabel = new Label(i18n.getString("UIMetaDataPane.operator.label", "Person in charge:"));
		this.operatorLimitedTextField = this.createLimitedTextField(
				i18n.getString("UIMetaDataPane.operator.tooltip", "Name of person in charge"), 
				i18n.getString("UIMetaDataPane.operator.prompt", "Operator")
		);
		operatorLabel.setLabelFor(this.operatorLimitedTextField);
		operatorLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label dateLabel = new Label(i18n.getString("UIMetaDataPane.date.label", "Processing date:"));
		this.datePicker = this.createDatePicker(
				i18n.getString("UIMetaDataPane.date.tooltip", "Date of project processing"), 
				i18n.getString("UIMetaDataPane.date.prompt", "Processing date")
		);
		dateLabel.setLabelFor(this.datePicker);
		dateLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label customerIdLabel = new Label(i18n.getString("UIMetaDataPane.customer.label", "Customer Id:"));
		this.customerIdLimitedTextField = this.createLimitedTextField(
				i18n.getString("UIMetaDataPane.customer.tooltip", "Id of customer"), 
				i18n.getString("UIMetaDataPane.customer.prompt", "Customer")
		);
		customerIdLabel.setLabelFor(this.customerIdLimitedTextField);
		customerIdLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label projectIdLabel = new Label(i18n.getString("UIMetaDataPane.project.label", "Project Id:"));
		this.projectIdLimitedTextField = this.createLimitedTextField(
				i18n.getString("UIMetaDataPane.project.tooltip", "Id of project"), 
				i18n.getString("UIMetaDataPane.project.prompt", "Project")
		);
		projectIdLabel.setLabelFor(this.projectIdLimitedTextField);
		projectIdLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		Label descriptionLabel = new Label(i18n.getString("UIMetaDataPane.description.label", "Project description:"));
		this.descriptionTextArea = this.createLimitedTextArea(
				i18n.getString("UIMetaDataPane.description.tooltip", "Project description and comments"),
				i18n.getString("UIMetaDataPane.description.prompt", "Project description and comments")
				);				

		descriptionLabel.setLabelFor(this.descriptionTextArea);
		descriptionLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		GridPane.setHgrow(nameLabel,        Priority.SOMETIMES);
		GridPane.setHgrow(dateLabel,        Priority.SOMETIMES);
		GridPane.setHgrow(operatorLabel,    Priority.SOMETIMES);
		GridPane.setHgrow(customerIdLabel,  Priority.SOMETIMES);
		GridPane.setHgrow(projectIdLabel,   Priority.SOMETIMES);
		GridPane.setHgrow(descriptionLabel, Priority.SOMETIMES);

		GridPane.setHgrow(this.nameLimitedTextField,       Priority.ALWAYS);
		GridPane.setHgrow(this.datePicker,                 Priority.ALWAYS);
		GridPane.setHgrow(this.operatorLimitedTextField,   Priority.ALWAYS);
		GridPane.setHgrow(this.customerIdLimitedTextField, Priority.ALWAYS);
		GridPane.setHgrow(this.projectIdLimitedTextField,  Priority.ALWAYS);
		GridPane.setHgrow(this.descriptionTextArea,        Priority.ALWAYS);
		GridPane.setVgrow(this.descriptionTextArea,        Priority.ALWAYS);
		int row = 0;
		
		gridPane.add(nameLabel,                 0, ++row, 2, 1);
		gridPane.add(this.nameLimitedTextField, 0, ++row, 2, 1);
		
		gridPane.add(new Region(),              0, ++row, 2, 1);
		
		gridPane.add(operatorLabel,             0, ++row);
		gridPane.add(dateLabel,                 1,   row);
		gridPane.add(this.operatorLimitedTextField, 0, ++row);
		gridPane.add(this.datePicker,           1,   row);
		
		gridPane.add(new Region(),              0, ++row, 2, 1);
		
		gridPane.add(customerIdLabel,           0, ++row);
		gridPane.add(projectIdLabel,            1,   row);
		gridPane.add(this.customerIdLimitedTextField, 0, ++row);
		gridPane.add(this.projectIdLimitedTextField,  1,   row);
		
		gridPane.add(new Region(),              0, ++row, 2, 1);
		
		gridPane.add(descriptionLabel,          0, ++row, 2, 1);
		gridPane.add(this.descriptionTextArea,  0, ++row, 2, 1);
				
		ScrollPane scroller = new ScrollPane(gridPane);
		scroller.setPadding(new Insets(20, 30, 20, 30)); // oben, links, unten, rechts
		scroller.setFitToHeight(true);
		scroller.setFitToWidth(true);
		
		this.nameLimitedTextField.textProperty().bindBidirectional(this.metaData.nameProperty());
		this.datePicker.valueProperty().bindBidirectional(this.metaData.dateProperty());
		this.operatorLimitedTextField.textProperty().bindBidirectional(this.metaData.operatorProperty());
		this.customerIdLimitedTextField.textProperty().bindBidirectional(this.metaData.customerIdProperty());
		this.projectIdLimitedTextField.textProperty().bindBidirectional(this.metaData.projectIdProperty());
		this.descriptionTextArea.textProperty().bindBidirectional(this.metaData.descriptionProperty());
		
		this.metaDataNode = scroller;
		this.disableComponents(true);
		this.clearComponents();
	}
	
	private LimitedTextField createLimitedTextField(String tooltip, String promptText) {
		MetaDataChangeListener listener = new MetaDataChangeListener();
		LimitedTextField textField = new LimitedTextField(255);
		textField.setPromptText(promptText);
		textField.setTooltip(new Tooltip(tooltip));
		textField.setMaxWidth(Double.MAX_VALUE);
		textField.setMinWidth(150);
		textField.setOnAction(listener);
		textField.focusedProperty().addListener(listener);
		return textField;
	}
	
	private DatePicker createDatePicker(String tooltip, String promptText) {
		MetaDataChangeListener listener = new MetaDataChangeListener();
		DatePicker datePicker = new DatePicker();
		datePicker.setPromptText(promptText);
		datePicker.setTooltip(new Tooltip(tooltip));
		datePicker.setMaxWidth(Double.MAX_VALUE);
		datePicker.setMinWidth(150);
		datePicker.setOnAction(listener);
		datePicker.focusedProperty().addListener(listener);
		datePicker.setEditable(false);
		return datePicker;
	}
	
	private LimitedTextArea createLimitedTextArea(String tooltip, String promptText) {
		MetaDataChangeListener listener = new MetaDataChangeListener();		
		LimitedTextArea textArea = new LimitedTextArea(10000);
		textArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		textArea.setPromptText(promptText);
		textArea.setTooltip(new Tooltip(tooltip));
		textArea.focusedProperty().addListener(listener);
		return textArea;
	}
	
	private GridPane createGridPane() {
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(20);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(10, 10, 10, 10)); // oben, links, unten, rechts
		return gridPane;
	}
	
	public MetaData getMetaData() {
		return this.metaData;
	}
	
	private void disableComponents(boolean disable) {
		this.nameLimitedTextField.setDisable(disable);
		this.datePicker.setDisable(disable);
		this.operatorLimitedTextField.setDisable(disable);
		this.customerIdLimitedTextField.setDisable(disable);
		this.projectIdLimitedTextField.setDisable(disable);
		this.descriptionTextArea.setDisable(disable);
	}
	
	private void clearComponents() {
		this.nameLimitedTextField.setText(null);
		this.operatorLimitedTextField.setText(null);
		this.customerIdLimitedTextField.setText(null);
		this.projectIdLimitedTextField.setText(null);
		this.descriptionTextArea.setText(null);
	}
	
	private void save() {
		try {
			SQLManager.getInstance().save(this.metaData);
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("UIMetaDataPane.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIMetaDataPane.message.error.save.exception.header", "Error, could not save project meta-data to database."),
							i18n.getString("UIMetaDataPane.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
					);
				}
			});
		}
	}
}
