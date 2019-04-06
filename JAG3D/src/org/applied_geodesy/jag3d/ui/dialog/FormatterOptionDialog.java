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

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.FormatterOptions.FormatterOption;
import org.applied_geodesy.util.i18.I18N;
import org.applied_geodesy.util.unit.AngleUnit;
import org.applied_geodesy.util.unit.LengthUnit;
import org.applied_geodesy.util.unit.ScaleUnit;
import org.applied_geodesy.util.unit.Unit;
import org.applied_geodesy.util.unit.UnitType;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class FormatterOptionDialog {
	
	private class DigitsChangeListener implements ChangeListener<Integer> {
		private CellValueType type;
		private DigitsChangeListener(CellValueType type) {
			this.type = type;
		}
		
		@Override
		public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
			if (!ignoreEvent) {
				FormatterOption option = options.getFormatterOptions().get(type);
				option.setFractionDigits(newValue);
				save(option);
			}
		}
	}
	
	private class UnitChangeListener<T extends Unit> implements ChangeListener<T> {
		private CellValueType type;
		
		private UnitChangeListener(CellValueType type) {
			this.type = type;
		}
		
		@Override
		public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
			if (!ignoreEvent) {
				FormatterOption option = options.getFormatterOptions().get(type);
				option.setUnit(newValue);
				save(option);
			}
		}
		
	}
	
	private Map<CellValueType, ComboBox<Unit>> unitComboBoxes = new HashMap<CellValueType, ComboBox<Unit>>();
	private Map<CellValueType, Spinner<Integer>> digitsSpinners = new HashMap<CellValueType, Spinner<Integer>>();
	private FormatterOptions options = FormatterOptions.getInstance();
	private I18N i18n = I18N.getInstance();
	private static FormatterOptionDialog formatterOptionDialog = new FormatterOptionDialog();
	private Dialog<Void> dialog = null;
	private Accordion accordion = null;
	private boolean ignoreEvent = false;
	private Window window;
	
	private FormatterOptionDialog() {}

	public static void setOwner(Window owner) {
		formatterOptionDialog.window = owner;
	}
	
	public static Optional<Void> showAndWait() {
		formatterOptionDialog.init();
		formatterOptionDialog.load();
		formatterOptionDialog.accordion.setExpandedPane(formatterOptionDialog.accordion.getPanes().get(0));
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					formatterOptionDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) formatterOptionDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return formatterOptionDialog.dialog.showAndWait();
	}
	
	private void init() {
		if (this.dialog != null)
			return;
		
		this.dialog = new Dialog<Void>();
		this.dialog.setTitle(i18n.getString("FormatterOptionDialog.title", "Preferences"));
		this.dialog.setHeaderText(i18n.getString("FormatterOptionDialog.header", "Formatter options and unit preferences"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);

		this.accordion = new Accordion();
		this.accordion.getPanes().addAll(
				this.createFormatterOptionValuesPane(),
				this.createFormatterOptionUncertaintiesPane(),
				this.createFormatterOptionResidualsPane(),
				this.createFormatterOptionStatisticsPane()
				);

		this.dialog.getDialogPane().setContent(this.accordion);
		this.dialog.setResizable(true);
		
		this.dialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
			@Override
			public void handle(DialogEvent event) {
				accordion.setExpandedPane(accordion.getPanes().get(0));
			}
		});
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				digitsSpinners.get(CellValueType.LENGTH).requestFocus();
			}
		});
	}
	
	private void load() {
		try {
			this.ignoreEvent = true;
			for (FormatterOption option : options.getFormatterOptions().values()) {
				CellValueType type = option.getType();
				if (this.digitsSpinners.containsKey(type)) {
					SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory)this.digitsSpinners.get(type).getValueFactory();
					int digits = option.getFractionDigits();
					digits = Math.max(Math.min(digits, valueFactory.getMax()), valueFactory.getMin());
					this.digitsSpinners.get(type).getValueFactory().setValue(digits);
				}
				if (this.unitComboBoxes.containsKey(type)) {
					this.unitComboBoxes.get(type).getSelectionModel().select(option.getUnit());
				}
			}
		} 
		finally {
			this.ignoreEvent = false;
		}
	}

	private TitledPane createFormatterOptionValuesPane() {
		String title = i18n.getString("FormatterOptionDialog.value.title", "Values");
		String tooltip = i18n.getString("FormatterOptionDialog.value.tooltip", "Preferences for measurement values");
		GridPane gridPane = this.createGridPane();

		Map<CellValueType, FormatterOption> formatterOptions = options.getFormatterOptions();
		int row = 0;
		this.addRow(
				i18n.getString("FormatterOptionDialog.unit.length.value.label", "Length:"),
    			i18n.getString("FormatterOptionDialog.unit.length.value.tooltip", "Set number of fraction digits for type length"),
    			i18n.getString("FormatterOptionDialog.unit.length.value.tooltip.unit", "Set unit for type length value"),
				formatterOptions.get(CellValueType.LENGTH),
				gridPane,
				++row);
		
		this.addRow(
				i18n.getString("FormatterOptionDialog.unit.angle.value.label", "Angle:"),
    			i18n.getString("FormatterOptionDialog.unit.angle.value.tooltip.digits", "Set number of fraction digits for type angle"),
    			i18n.getString("FormatterOptionDialog.unit.angle.value.tooltip.unit", "Set unit for type angle value"),
				formatterOptions.get(CellValueType.ANGLE),
				gridPane,
				++row);
		
		this.addRow(
				i18n.getString("FormatterOptionDialog.unit.scale.value.label", "Scale:"),
    			i18n.getString("FormatterOptionDialog.unit.scale.value.tooltip.digits", "Set number of fraction digits for type scale"),
    			i18n.getString("FormatterOptionDialog.unit.scale.value.tooltip.unit", "Set unit for type scale value"),
				formatterOptions.get(CellValueType.SCALE),
				gridPane,
				++row);

		Region spacer = new Region();
		GridPane.setVgrow(spacer, Priority.ALWAYS);
		gridPane.add(spacer, 1, ++row, 3, 1);
		return this.createTitledPane(title, tooltip, gridPane);
	}
	
	private TitledPane createFormatterOptionUncertaintiesPane() {
		String title   = i18n.getString("FormatterOptionDialog.uncertainty.title", "Uncertainties");
		String tooltip = i18n.getString("FormatterOptionDialog.uncertainty.tooltip", "Preferences for uncertainties");
		GridPane gridPane = this.createGridPane();

		Map<CellValueType, FormatterOption> formatterOptions = options.getFormatterOptions();
		int row = 0;
		this.addRow(
				i18n.getString("FormatterOptionDialog.unit.length.uncertainty.label", "Length uncertainty:"),
    			i18n.getString("FormatterOptionDialog.unit.length.uncertainty.tooltip.digits", "Set number of fraction digits for type length uncertainty"),
    			i18n.getString("FormatterOptionDialog.unit.length.uncertainty.tooltip.unit", "Set unit for type length uncertainty"),
				formatterOptions.get(CellValueType.LENGTH_UNCERTAINTY),
				gridPane,
				++row);
		
		this.addRow(
				i18n.getString("FormatterOptionDialog.unit.angle.uncertainty.label", "Angle uncertainty:"),
    			i18n.getString("FormatterOptionDialog.unit.angle.uncertainty.tooltip.digits", "Set number of fraction digits for type angle uncertainty"),
    			i18n.getString("FormatterOptionDialog.unit.angle.uncertainty.tooltip.unit", "Set unit for type angle uncertainty"),
				formatterOptions.get(CellValueType.ANGLE_UNCERTAINTY),
				gridPane,
				++row);
		
		this.addRow(
				i18n.getString("FormatterOptionDialog.unit.scale.uncertainty.label", "Scale uncertainty:"),
    			i18n.getString("FormatterOptionDialog.unit.scale.uncertainty.tooltip.digits", "Set number of fraction digits for type scale uncertainty"),
    			i18n.getString("FormatterOptionDialog.unit.scale.uncertainty.tooltip.unit", "Set unit for type scale uncertainty"),
				formatterOptions.get(CellValueType.SCALE_UNCERTAINTY),
				gridPane,
				++row);
		
		Region spacer = new Region();
		GridPane.setVgrow(spacer, Priority.ALWAYS);
		gridPane.add(spacer, 1, ++row, 3, 1);
		return this.createTitledPane(title, tooltip, gridPane);
	}
	
	private TitledPane createFormatterOptionResidualsPane() {
		String title   = i18n.getString("FormatterOptionDialog.residual.title", "Residuals");
		String tooltip = i18n.getString("FormatterOptionDialog.residual.tooltip", "Preferences for residuals");
		GridPane gridPane = this.createGridPane();

		Map<CellValueType, FormatterOption> formatterOptions = options.getFormatterOptions();
		int row = 0;
		this.addRow(
				i18n.getString("FormatterOptionDialog.unit.length.residual.label", "Length residual:"),
    			i18n.getString("FormatterOptionDialog.unit.length.residual.tooltip.digits", "Set number of fraction digits for type length residual"),
    			i18n.getString("FormatterOptionDialog.unit.length.residual.tooltip.unit", "Set unit for type length residual"),
				formatterOptions.get(CellValueType.LENGTH_RESIDUAL),
				gridPane,
				++row);
		
		this.addRow(
				i18n.getString("FormatterOptionDialog.unit.angle.residual.label", "Angle residual:"),
    			i18n.getString("FormatterOptionDialog.unit.angle.residual.tooltip.digits", "Set number of fraction digits for type angle residual"),
    			i18n.getString("FormatterOptionDialog.unit.angle.residual.tooltip.unit", "Set unit for type angle residual"),
				formatterOptions.get(CellValueType.ANGLE_RESIDUAL),
				gridPane,
				++row);
		
		this.addRow(
				i18n.getString("FormatterOptionDialog.unit.scale.residual.label", "Scale residual:"),
    			i18n.getString("FormatterOptionDialog.unit.scale.residual.tooltip.digits", "Set number of fraction digits for type scale residual"),
    			i18n.getString("FormatterOptionDialog.unit.scale.residual.tooltip.unit", "Set unit for type scale residual"),
				formatterOptions.get(CellValueType.SCALE_RESIDUAL),
				gridPane,
				++row);

		Region spacer = new Region();
		GridPane.setVgrow(spacer, Priority.ALWAYS);
		gridPane.add(spacer, 1, ++row, 3, 1);
		return this.createTitledPane(title, tooltip, gridPane);
	}
	
	private TitledPane createFormatterOptionStatisticsPane() {
		String title   = i18n.getString("FormatterOptionDialog.statistic.title", "Statistics");
		String tooltip = i18n.getString("FormatterOptionDialog.statistic.tooltip", "Preferences for unitless statistics values");
		GridPane gridPane = this.createGridPane();

		Map<CellValueType, FormatterOption> formatterOptions = options.getFormatterOptions();
		int row = 0;
		this.addRow(
				i18n.getString("FormatterOptionDialog.unit.statistic.label", "Statistic (unitless):"),
    			i18n.getString("FormatterOptionDialog.unit.statistic.tooltip.digits", "Set number of fraction digits for type statistic"),
    			null,
				formatterOptions.get(CellValueType.STATISTIC),
				gridPane,
				++row);

		Region spacer = new Region();
		GridPane.setVgrow(spacer, Priority.ALWAYS);
		gridPane.add(spacer, 1, ++row, 3, 1);
		return this.createTitledPane(title, tooltip, gridPane);
	}
	
	private TitledPane createTitledPane(String title, String tooltip, Node content) {
		Label label = new Label(title);
		label.setTooltip(new Tooltip(tooltip));
		TitledPane titledPane = new TitledPane();
		titledPane.setGraphic(label);
		titledPane.setCollapsible(true);
		titledPane.setAnimated(true);
		titledPane.setContent(content);
		titledPane.setPadding(new Insets(0, 10, 5, 10)); // oben, links, unten, rechts
//		titledPane.setText(title);
//		titledPane.setTooltip(new Tooltip(tooltip));
		return titledPane;
	}
	
	private GridPane createGridPane() {		
		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setHgap(15);
		gridPane.setVgap(7);
		gridPane.setPadding(new Insets(5, 5, 5, 5)); // oben, recht, unten, links
		return gridPane;
	}
	
	private void addRow(String label, String spinnerTooltip, String comboboxTooltip, FormatterOption option, GridPane parent, int row) {
		Label unitLabel = new Label(label);
		unitLabel.setWrapText(false);
		unitLabel.setMaxWidth(Double.MAX_VALUE);
		unitLabel.setPrefHeight(Control.USE_COMPUTED_SIZE);
		unitLabel.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		CellValueType type = option.getType();
		Spinner<Integer> digitsSpinner = this.createIntegerSpinner(type, 0, 10, 1, spinnerTooltip);
		
		ComboBox<Unit> unitComboBox = null;
		Unit unit = option.getUnit();
		if (unit != null) {
			if (AngleUnit.getUnit(unit.getType()) != null) {
				Collection<? extends Unit> angleUnits = new LinkedHashSet<AngleUnit>(AngleUnit.UNITS.values());
				unitComboBox = this.createUnitComboBox(type, comboboxTooltip, angleUnits);
			}
			else if (ScaleUnit.getUnit(unit.getType()) != null) {
				List<ScaleUnit> scaleUnits = null; 
				switch(type) {
				case SCALE:
					scaleUnits = List.of(
							ScaleUnit.getUnit(UnitType.UNITLESS),
							ScaleUnit.getUnit(UnitType.PARTS_PER_MILLION_WRT_ONE)
					);
					break;
				case SCALE_RESIDUAL:
				case SCALE_UNCERTAINTY:
					scaleUnits = List.of(
							ScaleUnit.getUnit(UnitType.UNITLESS),
							ScaleUnit.getUnit(UnitType.PARTS_PER_MILLION_WRT_ZERO)
					);
					break;
				default:
					System.out.println(this.getClass().getSimpleName() + " Error, not a scale type " + type);
					break;
				}
				if (scaleUnits != null)
					unitComboBox = this.createUnitComboBox(type, comboboxTooltip, scaleUnits);
			}
			else if (LengthUnit.getUnit(unit.getType()) != null) {
				unitComboBox = this.createUnitComboBox(type, comboboxTooltip, LengthUnit.UNITS.values());
			}
		}
		
		this.digitsSpinners.put(option.getType(), digitsSpinner);
				
		GridPane.setHgrow(unitLabel, Priority.NEVER);
		GridPane.setHgrow(digitsSpinner, Priority.SOMETIMES);
		
		parent.add(unitLabel,        0, row);
		parent.add(digitsSpinner,    1, row);
		if (unitComboBox != null) {
			GridPane.setHgrow(unitComboBox, Priority.ALWAYS);
			parent.add(unitComboBox, 2, row);
			this.unitComboBoxes.put(option.getType(), unitComboBox);
		}
		else {
			Label spacer = new Label();
			spacer.setMaxWidth(Double.MAX_VALUE);
			GridPane.setHgrow(spacer, Priority.ALWAYS);
			parent.add(spacer, 2, row);
		}
	}
	
	private ComboBox<Unit> createUnitComboBox(CellValueType type, String tooltip, Collection<? extends Unit> items) {
		ComboBox<Unit> unitComboBox = new ComboBox<Unit>();
		unitComboBox.getItems().setAll(items);
		unitComboBox.setConverter(new StringConverter<Unit>() {

			@Override
			public String toString(Unit unit) {
				return unit == null ? null : unit.getName();
			}

			@Override
			public Unit fromString(String string) {
				return null;
			}
		});
		unitComboBox.setTooltip(new Tooltip(tooltip));
		unitComboBox.valueProperty().addListener(new UnitChangeListener<Unit>(type));
		unitComboBox.setMinWidth(150);
		unitComboBox.setMaxWidth(Double.MAX_VALUE);
		return unitComboBox;
	}
		
	private Spinner<Integer> createIntegerSpinner(CellValueType type, int min, int max, int amountToStepBy, String tooltip) {
		NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
		numberFormat.setMaximumFractionDigits(0);
		numberFormat.setMinimumFractionDigits(0);
		numberFormat.setGroupingUsed(false);
		
		StringConverter<Integer> converter = new StringConverter<Integer>() {
		    @Override
		    public Integer fromString(String s) {
		    	if (s == null || s.trim().isEmpty())
		    		return null;
		    	else {
		    		try {
		    			return numberFormat.parse(s).intValue();
		    		}catch (Exception nfe) {
						nfe.printStackTrace();
					}
		    	}
		        return null;
		    }

		    @Override
		    public String toString(Integer d) {
		        return d == null ? "" : numberFormat.format(d);
		    }
		};
		
		SpinnerValueFactory.IntegerSpinnerValueFactory integerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max);
		Spinner<Integer> integerSpinner = new Spinner<Integer>();
		integerSpinner.setUserData(type);
		integerSpinner.setEditable(true);
		integerSpinner.setValueFactory(integerFactory);
		//integerSpinner.getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);
		
		integerFactory.setConverter(converter);
		integerFactory.setAmountToStepBy(amountToStepBy);
		
		TextFormatter<Integer> formatter = new TextFormatter<Integer>(integerFactory.getConverter(), integerFactory.getValue());
		integerSpinner.getEditor().setTextFormatter(formatter);
		integerSpinner.getEditor().setAlignment(Pos.BOTTOM_RIGHT);
		integerFactory.valueProperty().bindBidirectional(formatter.valueProperty());

		integerFactory.valueProperty().addListener(new DigitsChangeListener(type));
		integerSpinner.setMinWidth(75);
		integerSpinner.setPrefWidth(100);
		integerSpinner.setMaxWidth(Double.MAX_VALUE);
		integerSpinner.setTooltip(new Tooltip(tooltip));
		
		integerFactory.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				if (newValue == null)
					integerFactory.setValue(oldValue);
			}
		});
		
		return integerSpinner;
	}
	
	private void save(FormatterOption option) {
		try {
			SQLManager.getInstance().saveFormatterOption(option);
		}
		catch (Exception e) {
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("FormatterOptionDialog.message.error.save.exception.title", "Unexpected SQL-Error"),
							i18n.getString("FormatterOptionDialog.message.error.save.exception.header", "Error, could not save formatter properties to database."),
							i18n.getString("FormatterOptionDialog.message.error.save.exception.message", "An exception has occurred during database transaction."),
							e
							);
				}
			});
		}
	}
}
