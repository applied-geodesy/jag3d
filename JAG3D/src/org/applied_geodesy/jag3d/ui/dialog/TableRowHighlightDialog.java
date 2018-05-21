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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.DefaultTableRowHighlightValue;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlight;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightRangeType;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightType;
import org.applied_geodesy.jag3d.ui.textfield.DoubleTextField;
import org.applied_geodesy.jag3d.ui.textfield.MinMaxDoubleTextField;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterEventType;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class TableRowHighlightDialog implements FormatterChangedListener {

	private class HighlightRangeChangeListener implements ChangeListener<Double> {
		private final TableRowHighlightType tableRowHighlightType;
		
		public HighlightRangeChangeListener(TableRowHighlightType tableRowHighlightType) {
			this.tableRowHighlightType = tableRowHighlightType;
		}
		
		@Override
		public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
			if (!ignoreChangeEvent && newValue != null && highlightRangeFieldMap.containsKey(this.tableRowHighlightType)) {
				TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
				
				if (highlightRangeFieldMap.get(this.tableRowHighlightType) != null) {
					DoubleTextField[] fields = highlightRangeFieldMap.get(tableRowHighlightType);
					double leftBoundary  = fields[0].getNumber();
					double rightBoundary = fields[1].getNumber();
					tableRowHighlight.setTableRowHighlightType(this.tableRowHighlightType);
					tableRowHighlight.setRange(leftBoundary, rightBoundary);
				}
				
				ignoreChangeEvent = true;
				try {
					for (Toggle toggleButton : highlightOptionGroup.getToggles()) {
						if (toggleButton.getUserData() == this.tableRowHighlightType && toggleButton instanceof RadioButton) {
							RadioButton radioButton = (RadioButton)toggleButton;
							radioButton.setSelected(true);
							break;
						}
					}
				}
				finally {
					ignoreChangeEvent = false;
				}
				save();
			}
		}
	}
	
	private class HighlightTypeChangeListener implements ChangeListener<Toggle> {
		@Override
		public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
			if (!ignoreChangeEvent && newValue != null && newValue.isSelected() && newValue.getUserData() != null && newValue.getUserData() instanceof TableRowHighlightType && newValue instanceof RadioButton) {
				TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
				TableRowHighlightType tableRowHighlightType = (TableRowHighlightType)newValue.getUserData();
				tableRowHighlight.setTableRowHighlightType(tableRowHighlightType);
				
				if (highlightRangeFieldMap.containsKey(tableRowHighlightType) && highlightRangeFieldMap.get(tableRowHighlightType) != null) {
					DoubleTextField[] fields = highlightRangeFieldMap.get(tableRowHighlightType);
					double leftBoundary  = fields[0].getNumber();
					double rightBoundary = fields[1].getNumber();
					tableRowHighlight.setRange(leftBoundary, rightBoundary);
				}
				save();
			}
		}

	}

	private class HighlightColorChangeListener implements ChangeListener<Color> {
		private final TableRowHighlightRangeType tableRowHighlightRangeType;

		public HighlightColorChangeListener(TableRowHighlightRangeType tableRowHighlightRangeType) {
			this.tableRowHighlightRangeType = tableRowHighlightRangeType;
		}

		@Override
		public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
			if (!ignoreChangeEvent && newValue != null) {
				TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();
				tableRowHighlight.setColor(this.tableRowHighlightRangeType, newValue);
				save();
			}
		}
	}

	private I18N i18n = I18N.getInstance();
	private FormatterOptions options = FormatterOptions.getInstance();
	private static TableRowHighlightDialog tableRowHighlightDialog = new TableRowHighlightDialog();
	private Dialog<TableRowHighlight> dialog = null;
	private Window window;
	private Map<TableRowHighlightType, DoubleTextField[]> highlightRangeFieldMap = new HashMap<TableRowHighlightType, DoubleTextField[]>(5);

	private boolean ignoreChangeEvent = false;
	private ToggleGroup highlightOptionGroup = new ToggleGroup();
	private ColorPicker excellentColorPicker;
	private ColorPicker satisfactoryColorPicker;
	private ColorPicker inadequateColorPicker;
	
	private Accordion accordion = new Accordion();

	private TableRowHighlightDialog() {}

	public static void setOwner(Window owner) {
		tableRowHighlightDialog.window = owner;
	}

	public static Optional<TableRowHighlight> showAndWait() {
		tableRowHighlightDialog.init();
		tableRowHighlightDialog.load();
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					tableRowHighlightDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) tableRowHighlightDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return tableRowHighlightDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.ignoreChangeEvent = true;
		try {
			this.options.addFormatterChangedListener(this);

			this.dialog = new Dialog<TableRowHighlight>();
			this.dialog.setTitle(i18n.getString("TableRowHighlightDialog.title", "Highlighting table rows"));
			this.dialog.setHeaderText(i18n.getString("TableRowHighlightDialog.header", "Highlighting of specific table rows"));
			this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
			this.dialog.initModality(Modality.APPLICATION_MODAL);
			this.dialog.initOwner(window);
			this.dialog.setResizable(true);

			this.dialog.initModality(Modality.APPLICATION_MODAL);
			this.dialog.initOwner(window);
			this.dialog.getDialogPane().setContent(this.createPane());
			this.dialog.setResizable(true);
			this.dialog.setResultConverter(new Callback<ButtonType, TableRowHighlight>() {
				@Override
				public TableRowHighlight call(ButtonType buttonType) {
					return TableRowHighlight.getInstance();
				}
			});
			
			this.dialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
				@Override
				public void handle(DialogEvent event) {
					accordion.setExpandedPane(accordion.getPanes().get(0));
				}
			});
		}
		finally {
			this.ignoreChangeEvent = false;
		}
	}

	private Node createPane() {
		this.accordion.getPanes().addAll((TitledPane)this.createRangePane(), (TitledPane)this.createColorPropertiesPane());
		this.accordion.setExpandedPane(accordion.getPanes().get(0));
		this.accordion.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.accordion.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		if (highlightOptionGroup.getSelectedToggle() instanceof RadioButton)
			((RadioButton)highlightOptionGroup.getSelectedToggle()).requestFocus();
		
		return accordion;
	}

	private Node createRangePane() {
		GridPane gridPane = this.createGridPane();

		int row = 0;
		this.addRow(gridPane, row++, TableRowHighlightType.NONE,                  this.highlightOptionGroup);
		this.addRow(gridPane, row++, TableRowHighlightType.TEST_STATISTIC,        this.highlightOptionGroup);
		this.addRow(gridPane, row++, TableRowHighlightType.REDUNDANCY,            this.highlightOptionGroup);
		this.addRow(gridPane, row++, TableRowHighlightType.P_PRIO_VALUE,          this.highlightOptionGroup);
		this.addRow(gridPane, row++, TableRowHighlightType.INFLUENCE_ON_POSITION, this.highlightOptionGroup);

		this.highlightOptionGroup.selectedToggleProperty().addListener(new HighlightTypeChangeListener());

		TitledPane titledPane = this.createTitledPane(
				i18n.getString("TableRowHighlightDialog.range.label", "Range options"),
				i18n.getString("TableRowHighlightDialog.range.tooltip", "Specify range of table row highlighting"),
				gridPane);

		return titledPane;
	}

	private Node createColorPropertiesPane() {
		GridPane gridPane = this.createGridPane();

		this.excellentColorPicker    = this.createColorPicker(TableRowHighlightRangeType.EXCELLENT,    DefaultTableRowHighlightValue.getColor(TableRowHighlightRangeType.EXCELLENT));
		this.satisfactoryColorPicker = this.createColorPicker(TableRowHighlightRangeType.SATISFACTORY, DefaultTableRowHighlightValue.getColor(TableRowHighlightRangeType.SATISFACTORY));
		this.inadequateColorPicker   = this.createColorPicker(TableRowHighlightRangeType.INADEQUATE,   DefaultTableRowHighlightValue.getColor(TableRowHighlightRangeType.INADEQUATE));

		Label excellentColorLabel = new Label(i18n.getString("TableRowHighlightDialog.color.excellent.label",       "Excellent color:"));
		Label satisfactoryColorLabel = new Label(i18n.getString("TableRowHighlightDialog.color.satisfactory.label", "Satisfactory color:"));
		Label inadequateColorLabel = new Label(i18n.getString("TableRowHighlightDialog.color.inadequate.label",     "Inadequate color:"));

		excellentColorLabel.setLabelFor(this.excellentColorPicker);
		satisfactoryColorLabel.setLabelFor(this.satisfactoryColorPicker);
		inadequateColorLabel.setLabelFor(this.inadequateColorPicker);

		GridPane.setHgrow(excellentColorLabel,    Priority.NEVER);
		GridPane.setHgrow(satisfactoryColorLabel, Priority.NEVER);
		GridPane.setHgrow(inadequateColorLabel,   Priority.NEVER);

		GridPane.setHgrow(this.excellentColorPicker,    Priority.ALWAYS);
		GridPane.setHgrow(this.satisfactoryColorPicker, Priority.ALWAYS);
		GridPane.setHgrow(this.inadequateColorPicker,   Priority.ALWAYS);

		int row = 0;
		gridPane.add(excellentColorLabel,       0, row);
		gridPane.add(this.excellentColorPicker, 1, row++);

		gridPane.add(satisfactoryColorLabel,       0, row);
		gridPane.add(this.satisfactoryColorPicker, 1, row++);

		gridPane.add(inadequateColorLabel,       0, row);
		gridPane.add(this.inadequateColorPicker, 1, row++);

		TitledPane titledPane = this.createTitledPane(
				i18n.getString("TableRowHighlightDialog.color.label", "Color options"),
				i18n.getString("TableRowHighlightDialog.color.tooltip", "Specify color properties of table row highlighting"),
				gridPane);

		return titledPane;
	}
	
	private void addRow(GridPane parent, int row, TableRowHighlightType tableRowHighlightType, ToggleGroup group) {
		String radioButtonLabelText = null, radioButtonToolTipText = null;
		Label leftBoundary = null, middleBoundaray = null, rightBoundary = null;
		double range[] = DefaultTableRowHighlightValue.getRange(tableRowHighlightType);
		DoubleTextField leftRangeField = null, rightRangeField = null;

		switch(tableRowHighlightType) {
		case NONE:
			radioButtonLabelText   = i18n.getString("TableRowHighlightDialog.range.none.label", "None highlighting");
			radioButtonToolTipText = i18n.getString("TableRowHighlightDialog.range.none.tooltip", "None specific highlighting of table rows");
			break;

		case TEST_STATISTIC:
			radioButtonLabelText   = i18n.getString("TableRowHighlightDialog.range.test_statistic.label", "Test statistic Tprio \u2228 Tpost");
			radioButtonToolTipText = i18n.getString("TableRowHighlightDialog.range.test_statistic.tooltip", "Highlighting table rows depending on test statistic decisions");
			break;

		case REDUNDANCY:
			radioButtonLabelText   = i18n.getString("TableRowHighlightDialog.range.redundancy.label", "Redundancy r");
			radioButtonToolTipText = i18n.getString("TableRowHighlightDialog.range.redundancy.tooltip", "Highlighting table rows depending on redundancy");
			leftBoundary    = new Label("0 \u2264 "); 
			middleBoundaray = new Label(" \u003C ");
			rightBoundary   = new Label(" \u2264 1");

			leftRangeField  = this.createMinMaxDoubleTextField(tableRowHighlightType, range[0], 0, 1, CellValueType.STATISTIC, false, false);
			rightRangeField = this.createMinMaxDoubleTextField(tableRowHighlightType, range[1], 0, 1, CellValueType.STATISTIC, false, false);
			break;

		case P_PRIO_VALUE:
			radioButtonLabelText = String.format(
					Locale.ENGLISH, "%s [%s]", 
					i18n.getString("TableRowHighlightDialog.range.probability_value.label", "Probability value p"),
					"\u0025"
					);
			radioButtonToolTipText = i18n.getString("TableRowHighlightDialog.range.probability_value.tooltip", "Highlighting table rows depending on (a-priori) p-value");
			leftBoundary    = new Label("0 \u003C "); 
			middleBoundaray = new Label(" \u003C ");
			rightBoundary   = new Label(" \u003C 100");

			leftRangeField  = this.createMinMaxDoubleTextField(tableRowHighlightType, range[0], 0, 100, CellValueType.STATISTIC, true, true);
			rightRangeField = this.createMinMaxDoubleTextField(tableRowHighlightType, range[1], 0, 100, CellValueType.STATISTIC, true, true);
			break;

		case INFLUENCE_ON_POSITION:
			radioButtonLabelText = String.format(
					Locale.ENGLISH, "%s [%s]", 
					i18n.getString("TableRowHighlightDialog.range.influenceonposition.label", "Influence on point position EP"),
					options.getFormatterOptions().get(CellValueType.LENGTH_RESIDUAL).getUnit().getAbbreviation()
					);
			radioButtonToolTipText = i18n.getString("TableRowHighlightDialog.range.influenceonposition.tooltip", "Highlighting table rows depending on influence on point position due to an undetected gross-error");
			leftBoundary    = new Label("0 \u2264 "); 
			middleBoundaray = new Label(" \u003C ");
			rightBoundary   = new Label(" \u003C \u221E");

			leftRangeField  = this.createMinMaxDoubleTextField(tableRowHighlightType, range[0], 0, Double.POSITIVE_INFINITY, CellValueType.LENGTH_RESIDUAL, false, false);
			rightRangeField = this.createMinMaxDoubleTextField(tableRowHighlightType, range[1], 0, Double.POSITIVE_INFINITY, CellValueType.LENGTH_RESIDUAL, false, false);
			break;

		}

		if (tableRowHighlightType != null && radioButtonLabelText != null && radioButtonToolTipText != null) {
			int column = 0;

			RadioButton radioButton = this.createRadioButton(radioButtonLabelText, radioButtonToolTipText, tableRowHighlightType, group);
			radioButton.setSelected(tableRowHighlightType == TableRowHighlightType.NONE);
			
			GridPane.setMargin(radioButton, new Insets(0,10,0,0));
			GridPane.setHgrow(radioButton, Priority.SOMETIMES);
			parent.add(radioButton, column++, row);

			if (leftRangeField != null && rightRangeField != null && leftBoundary != null && middleBoundaray != null && rightBoundary != null) {
				GridPane.setHgrow(leftBoundary, Priority.NEVER);
				GridPane.setHgrow(middleBoundaray, Priority.NEVER);
				GridPane.setHgrow(rightBoundary, Priority.NEVER);
				GridPane.setHgrow(leftRangeField, Priority.ALWAYS);
				GridPane.setHgrow(rightRangeField, Priority.ALWAYS);

				parent.add(leftBoundary,    column++, row);
				parent.add(leftRangeField,  column++, row);
				parent.add(middleBoundaray, column++, row);
				parent.add(rightRangeField, column++, row);
				parent.add(rightBoundary,   column++, row);

				this.highlightRangeFieldMap.put(tableRowHighlightType, new DoubleTextField[] {leftRangeField, rightRangeField});
			}
		}
	}

	private void load() {
		this.ignoreChangeEvent = true;
		
		TableRowHighlight tableRowHighlight = TableRowHighlight.getInstance();

		try {
			SQLManager.getInstance().loadTableRowHighlight();

			TableRowHighlightType tableRowHighlightType = tableRowHighlight.getTableRowHighlightType();
			double leftBoundary  = tableRowHighlight.getLeftBoundary();
			double rightBoundary = tableRowHighlight.getRightBoundary();

			for (Toggle toggleButton : this.highlightOptionGroup.getToggles()) {
				if (toggleButton.getUserData() == tableRowHighlightType && toggleButton instanceof RadioButton) {
					RadioButton radioButton = (RadioButton)toggleButton;
					radioButton.setSelected(true);
					radioButton.requestFocus();
					break;
				}
			}

			if (tableRowHighlightType != TableRowHighlightType.NONE && this.highlightRangeFieldMap.containsKey(tableRowHighlightType) && this.highlightRangeFieldMap.get(tableRowHighlightType) != null) {
				DoubleTextField[] fields = this.highlightRangeFieldMap.get(tableRowHighlightType);
				fields[0].setNumber(leftBoundary);
				fields[1].setNumber(rightBoundary);
			}

			this.excellentColorPicker.setValue(tableRowHighlight.getColor(TableRowHighlightRangeType.EXCELLENT));
			this.satisfactoryColorPicker.setValue(tableRowHighlight.getColor(TableRowHighlightRangeType.SATISFACTORY));
			this.inadequateColorPicker.setValue(tableRowHighlight.getColor(TableRowHighlightRangeType.INADEQUATE));
			
			Platform.runLater(new Runnable() {
				@Override public void run() {
					if (highlightOptionGroup.getSelectedToggle() instanceof RadioButton)
						((RadioButton)highlightOptionGroup.getSelectedToggle()).requestFocus();
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("TableRowHighlightDialog.message.error.load.exception.title",   "Unexpected SQL-Error"),
							i18n.getString("TableRowHighlightDialog.message.error.load.exception.header",  "Error, could not load table row highlight properties from database."),
							i18n.getString("TableRowHighlightDialog.message.error.load.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
		finally {
			tableRowHighlight.refreshTables();
			this.ignoreChangeEvent = false;
		}		
	}

	private void save() {
		try {
			SQLManager.getInstance().saveTableRowHighlight();
		}
		catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("TableRowHighlightDialog.message.error.save.exception.title",   "Unexpected SQL-Error"),
							i18n.getString("TableRowHighlightDialog.message.error.save.exception.header",  "Error, could not save table row highlight properties to database."),
							i18n.getString("TableRowHighlightDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
		finally {
			TableRowHighlight.getInstance().refreshTables();
		}
	}

	private RadioButton createRadioButton(String title, String tooltip, TableRowHighlightType tableRowHighlightType, ToggleGroup group) {	
		Label label = new Label(title);
		label.setPadding(new Insets(0,0,0,3));
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		RadioButton radioButton = new RadioButton();
		radioButton.setGraphic(label);
		radioButton.setTooltip(new Tooltip(tooltip));
		radioButton.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		radioButton.setMaxWidth(Double.MAX_VALUE);
		radioButton.setUserData(tableRowHighlightType);
		radioButton.setToggleGroup(group);
		return radioButton;
	}

	private DoubleTextField createMinMaxDoubleTextField(TableRowHighlightType tableRowHighlightType, double value, double min, double max, CellValueType valueType, boolean exclusiveMin, boolean exclusiveMax) {
		MinMaxDoubleTextField doubleTextField = new MinMaxDoubleTextField(value, min, max, valueType, false, exclusiveMin, exclusiveMax);
		doubleTextField.setPrefColumnCount(3);
		doubleTextField.setMaxWidth(Double.MAX_VALUE);
		doubleTextField.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		doubleTextField.setUserData(tableRowHighlightType);
		doubleTextField.numberProperty().addListener(new HighlightRangeChangeListener(tableRowHighlightType));
		return doubleTextField;
	}

	private GridPane createGridPane() {		
		GridPane gridPane = new GridPane();
		gridPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		gridPane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		gridPane.setAlignment(Pos.TOP_CENTER);
		gridPane.setHgap(5);
		gridPane.setVgap(7);
		gridPane.setPadding(new Insets(7, 25, 0, 25)); // oben, recht, unten, links
		return gridPane;
	}

	private TitledPane createTitledPane(String title, String tooltip, Node content) {
		Label label = new Label(title);
		label.setTooltip(new Tooltip(tooltip));
		TitledPane titledPane = new TitledPane();
		titledPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		titledPane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		titledPane.setGraphic(label);
		titledPane.setCollapsible(true);
		titledPane.setAnimated(true);
		titledPane.setContent(content);
		titledPane.setPadding(new Insets(5, 5, 5, 5)); // oben, links, unten, rechts
		return titledPane;
	}

	private ColorPicker createColorPicker(TableRowHighlightRangeType tableRowHighlightRangeType, Color color) {	
		ColorPicker colorPicker = new ColorPicker(color);
		colorPicker.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		colorPicker.setMaxWidth(Double.MAX_VALUE);
		colorPicker.getStyleClass().add("split-button");
		colorPicker.valueProperty().addListener(new HighlightColorChangeListener(tableRowHighlightRangeType));
		colorPicker.setUserData(tableRowHighlightRangeType);
		return colorPicker;
	}

	@Override
	public void formatterChanged(FormatterEvent evt) {
		if (evt.getEventType() == FormatterEventType.UNIT_CHANGED) {
			for (Toggle toggleButton : this.highlightOptionGroup.getToggles()) {
				if (toggleButton.getUserData() == TableRowHighlightType.INFLUENCE_ON_POSITION && toggleButton instanceof RadioButton) {
					RadioButton radioButton = (RadioButton)toggleButton;
					if (radioButton.getGraphic() instanceof Label) {
						((Label)radioButton.getGraphic()).setText(
								String.format(
										Locale.ENGLISH, "%s [%s]", 
										i18n.getString("TableRowHighlightDialog.range.influenceonposition.label", "Influence on point position"),
										options.getFormatterOptions().get(CellValueType.LENGTH_RESIDUAL).getUnit().getAbbreviation()
										)
								);
					}
					else {
						radioButton.setText(
								String.format(
										Locale.ENGLISH, "%s [%s]", 
										i18n.getString("TableRowHighlightDialog.range.influenceonposition.label", "Influence on point position"),
										options.getFormatterOptions().get(CellValueType.LENGTH_RESIDUAL).getUnit().getAbbreviation()
										)
								);
					}
					break;
				}
			}
		}
	}
}
