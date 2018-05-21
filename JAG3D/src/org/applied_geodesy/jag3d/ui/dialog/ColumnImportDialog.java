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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.applied_geodesy.adjustment.network.ObservationType;
import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.jag3d.ui.textfield.LimitedTextField;
import org.applied_geodesy.util.i18.I18N;
import org.applied_geodesy.util.io.CSVObservationFileReader;
import org.applied_geodesy.util.io.CSVPointFileReader;
import org.applied_geodesy.util.io.ColumnDefinedObservationFileReader;
import org.applied_geodesy.util.io.ColumnDefinedPointFileReader;
import org.applied_geodesy.util.io.PreviewFileReader;
import org.applied_geodesy.util.io.SourceFileReader;
import org.applied_geodesy.util.io.csv.CSVColumnType;
import org.applied_geodesy.util.io.csv.CSVOptionType;
import org.applied_geodesy.util.io.csv.CSVParser;
import org.applied_geodesy.util.io.csv.ColumnRange;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ColumnImportDialog {
	
	private class CSVUserDefinedOptionChangeListener implements ChangeListener<String> {
		private final TextField textField;
		private final CSVOptionType type;
		public CSVUserDefinedOptionChangeListener(TextField textField) {
			this.textField = textField;
			if (this.textField.getUserData() == null || !(this.textField.getUserData() instanceof CSVOptionType))
				throw new IllegalArgumentException(this.getClass().getSimpleName() + " Error, no CSV option type defined!");
			this.type = (CSVOptionType)this.textField.getUserData();
		}
		
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			if (textField.getText() != null && !textField.getText().isEmpty()) {
				char c = textField.getText().charAt(0);
				switch (this.type) {
				case QUOTE:
					quotechar = c;
					break;
				case ESCAPE:
					escape = c;
					break;
				case SEPARATOR:
					separator = c;
					break;				
				}
				changeMode();
			}
		}
	}
	
	private class CSVOptionChangeListener implements ChangeListener<Boolean> {
		private final ToggleGroup group;
		private final CSVOptionType type;
		public CSVOptionChangeListener(ToggleGroup group) {
			this.group = group;
			if (this.group.getUserData() == null || !(this.group.getUserData() instanceof CSVOptionType))
				throw new IllegalArgumentException(this.getClass().getSimpleName() + " Error, no CSV option type defined!");
			this.type = (CSVOptionType)this.group.getUserData();
		}
		
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (newValue) {
				if (this.group.getSelectedToggle().getUserData() != null) {
					char c = CSVParser.NULL_CHARACTER;
					
					if (this.group.getSelectedToggle().getUserData() instanceof Character)
						c = (Character)this.group.getSelectedToggle().getUserData();
					
					else if (this.group.getSelectedToggle().getUserData() instanceof TextField) {
						TextField textField = (TextField)this.group.getSelectedToggle().getUserData();
						if (textField.getText() != null && !textField.getText().isEmpty())
							c = textField.getText().charAt(0);
					}
					
					switch (this.type) {
					case QUOTE:
						quotechar = c;
						break;
					case ESCAPE:
						escape = c;
						break;
					case SEPARATOR:
						separator = c;
						break;				
					}
					changeMode();
				}
			}
		}
	}
	
	private class LocaleOptionChangeListener implements ChangeListener<Boolean> {
		private final ToggleGroup group;
		
		public LocaleOptionChangeListener(ToggleGroup group) {
			this.group = group;
		}
		
		@Override
		public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
			if (this.group.getSelectedToggle().getUserData() != null && 
					this.group.getSelectedToggle().getUserData() instanceof Locale) {
				fileLocale = (Locale)this.group.getSelectedToggle().getUserData();
			}
		}
	}
		
	private class ColumnIndex {
		final int column;
		public ColumnIndex(int column) {
			this.column = column;
		}

		public int getColumn() {
			return this.column;
		}
	}

	private class ColumnSelectionEventHandler implements EventHandler<MouseEvent> {
		ColumnIndex columnIndex = null;
		@Override
		public void handle(MouseEvent event) {
			if (event.getSource() == editor
					&& event.getPickResult() != null 
					&& event.getPickResult().getIntersectedNode() != null && event.getPickResult().getIntersectedNode() instanceof Text 
					&& event.getPickResult().getIntersectedNode().getParent() != null && event.getPickResult().getIntersectedNode().getParent() instanceof VBox
					&& event.getPickResult().getIntersectedNode().getParent().getUserData() != null && event.getPickResult().getIntersectedNode().getParent().getUserData() instanceof ColumnIndex
					) {
				this.columnIndex = (ColumnIndex)event.getPickResult().getIntersectedNode().getParent().getUserData();
			}

			if (this.columnIndex != null) {
				boolean highlight = false;
				if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
					if (importCSVFileCheckBox.isSelected() && maxCharactersPerColumn != null) {
						int columnLength = 0;
						int column = columnIndex.getColumn();
						for (int i = 0; i < maxCharactersPerColumn.length; i++) {
							int nextColumLength = columnLength + maxCharactersPerColumn[i];
							if (column >= columnLength && column < nextColumLength) 
								startColumn = columnLength;
							if (column >= columnLength && column < nextColumLength) 
								endColumn   = nextColumLength - 1;	
							columnLength = nextColumLength;
						}
					}
					else {
						startColumn = this.columnIndex.getColumn();
						endColumn   = startColumn;
					}
					
					highlight = true;
				}

				else if (!importCSVFileCheckBox.isSelected() && startColumn >= 0 && endColumn >= 0 && event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
					endColumn = this.columnIndex.getColumn();
					highlight = true;
				}

				if (highlight)
					selectColumns(Math.min(startColumn, endColumn), Math.max(startColumn, endColumn));
			}
		}
	}
	
	
	private I18N i18n = I18N.getInstance();
	private static ColumnImportDialog columnImportDialog = new ColumnImportDialog();
	private Dialog<SourceFileReader> dialog = null;
	private Window window;

	private int maxCharactersPerColumn[] = null;
	private List<String> linesOfFile = new ArrayList<String>(20);
	private List<TextField> textFieldList = new ArrayList<TextField>(20);
	private ComboBox<Enum<?>> importTypes;
	private HBox editor;
	private List<VBox> columns = new ArrayList<VBox>();
	
	private Node csvOptionPane;
	private Node pointColumnPickerPane;
	private Node terrestrialObservationColumnPickerPane;
	private Node gnssObservationColumnPickerPane;
	
	private CheckBox importCSVFileCheckBox;
	private int startColumn = -1;
	private int endColumn   = -1;
	private static final int MAX_LINES = 10;
	private final String TABULATOR = "     ";
	private char separator = CSVParser.DEFAULT_SEPARATOR;
	private char quotechar = CSVParser.DEFAULT_QUOTE_CHARACTER;
	private char escape    = CSVParser.DEFAULT_ESCAPE_CHARACTER; 
	private Locale fileLocale = Locale.ENGLISH;
	
	private ColumnImportDialog() {}

	public static void setOwner(Window owner) {
		columnImportDialog.window = owner;
	}
	
	public static Optional<SourceFileReader> showAndWait(File selectedFile) {
		if (selectedFile == null)
			return null;

		columnImportDialog.init();
	
		try {
			columnImportDialog.readPreview(selectedFile);
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}

		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					columnImportDialog.dialog.getDialogPane().requestLayout();
					Stage stage = (Stage) columnImportDialog.dialog.getDialogPane().getScene().getWindow();
					stage.sizeToScene();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return columnImportDialog.dialog.showAndWait();
	}

	private void init() {
		if (this.dialog != null)
			return;
		
		this.dialog = new Dialog<SourceFileReader>();
		this.dialog.setTitle(i18n.getString("ColumnImportDialog.title", "Column based file import"));
		this.dialog.setHeaderText(i18n.getString("ColumnImportDialog.header", "User-defined import of column-based files"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.setResizable(true);
		
		this.dialog.getDialogPane().setContent(this.createMainPane());

		this.dialog.setResultConverter(new Callback<ButtonType, SourceFileReader>() {
			@Override
			public SourceFileReader call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					return getSourceFileReader();					
				}
				return null;
			}
		});
	}

	private Node createMainPane() {
		VBox vBox = new VBox();
		vBox.setSpacing(15);
		vBox.setPadding(new Insets(5,5,5,5));
		
		Node editorPane            = this.initEditor();
		this.importCSVFileCheckBox = this.createCSVCheckbox();
		this.pointColumnPickerPane = this.createPointColumnPickerPane();
		this.terrestrialObservationColumnPickerPane = this.createTerrestrialObservationColumnPickerPane();
		this.gnssObservationColumnPickerPane        = this.createGNSSObservationColumnPickerPane();
		this.importTypes           = this.createImportTypeComboBox();
		this.csvOptionPane         = this.createCSVOptionPane();
		Node globalImportOptions   = createImportOptionPane();

		((TitledPane)editorPane).prefWidthProperty().bind(
				Bindings.max(
						((TitledPane)this.gnssObservationColumnPickerPane).widthProperty(), 
						Bindings.max(
								((TitledPane)this.pointColumnPickerPane).widthProperty(), 
								((TitledPane)this.terrestrialObservationColumnPickerPane).widthProperty()
								)
						)
				);
		
		vBox.getChildren().addAll(
				globalImportOptions,
				this.csvOptionPane,
				this.pointColumnPickerPane,
				this.terrestrialObservationColumnPickerPane,
				this.gnssObservationColumnPickerPane,
				editorPane
				);
		
		ScrollPane scrollPane = new ScrollPane(vBox);
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		
		Platform.runLater(new Runnable() {
            @Override public void run() {
            	importTypes.requestFocus();
            }
		});

		return scrollPane;
	}
	
	private Node createImportOptionPane() {
		GridPane gridPane = this.createGridPane();

		ToggleGroup localeGroup = new ToggleGroup();
		LocaleOptionChangeListener localeChangeListener = new LocaleOptionChangeListener(localeGroup);
		RadioButton englishLocaleRadioButton = this.createRadioButton(
				i18n.getString("ColumnImportDialog.decimal.separator.point.label", "Point"), 
				i18n.getString("ColumnImportDialog.decimal.separator.point.tooltip", "If selected, decimal separator is set to point"), 
				true, Locale.ENGLISH, localeGroup, localeChangeListener);
		RadioButton germanLocaleRadioButton  = this.createRadioButton(
				i18n.getString("ColumnImportDialog.decimal.separator.comma.label", "Comma"), 
				i18n.getString("ColumnImportDialog.decimal.separator.comma.tooltip", "If selected, decimal separator is set to comma"), 
				false, Locale.GERMAN, localeGroup, localeChangeListener);
		
		
		
		int columnIndex = 0;
		int rowIndex = 0;
		
		gridPane.add(this.importTypes, columnIndex++, rowIndex);
		gridPane.add(this.importCSVFileCheckBox, columnIndex++, rowIndex);
		
		columnIndex = 0;
		rowIndex++;

		gridPane.add(new Label(i18n.getString("ColumnImportDialog.decimal.separator.label", "Decimal separator:")), columnIndex++, rowIndex);
		gridPane.add(englishLocaleRadioButton, columnIndex++, rowIndex);
		gridPane.add(germanLocaleRadioButton,  columnIndex++, rowIndex);
		
		TitledPane titledPane = this.createTitledPane(
				i18n.getString("ColumnImportDialog.import.label", "Import options"),
				i18n.getString("ColumnImportDialog.import.tooltip", "Specify global import options"),
				gridPane);

		return titledPane;
		
	}
	
	private Node createCSVOptionPane() {
		GridPane gridPane = this.createGridPane();
		
		char separator = CSVParser.DEFAULT_SEPARATOR;
		char quotechar = CSVParser.DEFAULT_QUOTE_CHARACTER;
		char escape    = CSVParser.DEFAULT_ESCAPE_CHARACTER; 
		
		ToggleGroup separatorGroup = new ToggleGroup();
		separatorGroup.setUserData(CSVOptionType.SEPARATOR);
		LimitedTextField separatorTextField = new LimitedTextField(1,"|");
		separatorTextField.setPrefColumnCount(1);
		separatorTextField.setTooltip(new Tooltip(i18n.getString("ColumnImportDialog.csv.separator.user.field.tooltip", "User-defined column separator")));
		separatorTextField.setUserData(CSVOptionType.SEPARATOR);
		separatorTextField.setMaxWidth(Control.USE_PREF_SIZE);
		separatorTextField.setDisable(true);
		separatorTextField.textProperty().addListener(new CSVUserDefinedOptionChangeListener(separatorTextField));
		
		CSVOptionChangeListener separatorChangeListener = new CSVOptionChangeListener(separatorGroup);
		RadioButton commaSeparatorRadioButton       = this.createRadioButton(
				i18n.getString("ColumnImportDialog.csv.separator.comma.label", "Comma"),
				i18n.getString("ColumnImportDialog.csv.separator.comma.tooltip", "If selected, column separator is set to comma"),
				separator == ',',  ',', separatorGroup, separatorChangeListener);
		RadioButton semicolonSeparatorRadioButton   = this.createRadioButton(
				i18n.getString("ColumnImportDialog.csv.separator.semicolon.label", "Semicolon"),
				i18n.getString("ColumnImportDialog.csv.separator.semicolon.tooltip", "If selected, column separator is set to semicolon"),
				separator == ';',  ';', separatorGroup, separatorChangeListener);
		RadioButton blankSeparatorRadioButton       = this.createRadioButton(
				i18n.getString("ColumnImportDialog.csv.separator.blank.label", "Blank"),
				i18n.getString("ColumnImportDialog.csv.separator.blank.tooltip", "If selected, column separator is set to blank"),
				separator == ' ',  ' ', separatorGroup, separatorChangeListener);
		RadioButton tabulatorSeparatorRadioButton   = this.createRadioButton(
				i18n.getString("ColumnImportDialog.csv.separator.tabulator.label", "Tabulator"),
				i18n.getString("ColumnImportDialog.csv.separator.tabulator.tooltip", "If selected, column separator is set to tabulator"),
				separator == '\t', '\t', separatorGroup, separatorChangeListener);
		RadioButton userdefinedSeparatorRadioButton = this.createRadioButton(
				i18n.getString("ColumnImportDialog.csv.separator.user.label", "User-defined"),
				i18n.getString("ColumnImportDialog.csv.separator.user.tooltip", "If selected, column separator is user-defined"),
				separatorTextField.getText().length() > 0 && separator == separatorTextField.getText().charAt(0), separatorTextField, separatorGroup, separatorChangeListener);
		HBox separatorBox = new HBox();
		separatorBox.setSpacing(3);
		separatorBox.setAlignment(Pos.CENTER_LEFT);
		separatorBox.getChildren().addAll(userdefinedSeparatorRadioButton, separatorTextField);
		userdefinedSeparatorRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				separatorTextField.setDisable(!newValue);
			}
		});
		
		ToggleGroup quoteGroup = new ToggleGroup();
		quoteGroup.setUserData(CSVOptionType.QUOTE);
		
		CSVOptionChangeListener quoteChangeListener = new CSVOptionChangeListener(quoteGroup);
		RadioButton noneQuoteCharRadioButton   = this.createRadioButton(
				i18n.getString("ColumnImportDialog.csv.quote.none.label", "None"),
				i18n.getString("ColumnImportDialog.csv.quote.none.tooltip", "If selected, quote character is undefined"),
				quotechar == CSVParser.NULL_CHARACTER, CSVParser.NULL_CHARACTER, quoteGroup, quoteChangeListener);
		RadioButton singleQuoteCharRadioButton = this.createRadioButton(
				i18n.getString("ColumnImportDialog.csv.quote.single.label", "Single quote"),
				i18n.getString("ColumnImportDialog.csv.quote.single.tooltip", "If selected, single quote character will used"),
				quotechar == '\'', '\'', quoteGroup, quoteChangeListener);
		RadioButton doubleQuoteCharRadioButton = this.createRadioButton(
				i18n.getString("ColumnImportDialog.csv.quote.double.label", "Double quote"),
				i18n.getString("ColumnImportDialog.csv.quote.double.tooltip", "If selected, double quote character will used"),
				quotechar == '"',  '"',  quoteGroup, quoteChangeListener);

		ToggleGroup escapeGroup = new ToggleGroup();
		escapeGroup.setUserData(CSVOptionType.ESCAPE);
		LimitedTextField escapeTextField = new LimitedTextField(1,String.valueOf(escape));
		escapeTextField.setPrefColumnCount(1);
		escapeTextField.setTooltip(new Tooltip(i18n.getString("ColumnImportDialog.csv.escape.user.field.tooltip", "User-defined escape character")));
		escapeTextField.setMaxWidth(Control.USE_PREF_SIZE);
		escapeTextField.setUserData(CSVOptionType.ESCAPE);
		escapeTextField.textProperty().addListener(new CSVUserDefinedOptionChangeListener(escapeTextField));
		
		CSVOptionChangeListener escapeChangeListener = new CSVOptionChangeListener(escapeGroup);
		RadioButton noneEscapeCharRadioButton    = this.createRadioButton(
				i18n.getString("ColumnImportDialog.csv.escape.none.label", "Note"),
				i18n.getString("ColumnImportDialog.csv.escape.none.tooltip", "If selected, escape character is undefined"),
				escape == CSVParser.NULL_CHARACTER, CSVParser.NULL_CHARACTER, escapeGroup, escapeChangeListener);
		RadioButton userdefinedEscapeRadioButton = this.createRadioButton(
				i18n.getString("ColumnImportDialog.csv.escape.user.label", "User-defined"),
				i18n.getString("ColumnImportDialog.csv.escape.user.tooltip", "If selected, column separator is user-defined"),
				escapeTextField.getText().length() > 0 && escape == escapeTextField.getText().charAt(0), escapeTextField, escapeGroup, escapeChangeListener);
		
		HBox escapeBox = new HBox();
		escapeBox.setSpacing(3);
		escapeBox.setAlignment(Pos.CENTER_LEFT);
		escapeBox.getChildren().addAll(userdefinedEscapeRadioButton, escapeTextField);
		userdefinedEscapeRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				escapeTextField.setDisable(!newValue);
			}
		});
		
		int columnIndex = 0;
		int rowIndex = 0;
		gridPane.add(new Label(i18n.getString("ColumnImportDialog.csv.separator.label", "Column separator:")),  columnIndex++, rowIndex);
		gridPane.add(commaSeparatorRadioButton,       columnIndex++, rowIndex);
		gridPane.add(semicolonSeparatorRadioButton,   columnIndex++, rowIndex); 
		gridPane.add(blankSeparatorRadioButton,       columnIndex++, rowIndex); 
		gridPane.add(tabulatorSeparatorRadioButton,   columnIndex++, rowIndex); 
		gridPane.add(separatorBox,                    columnIndex++, rowIndex); 
		
		columnIndex = 0;
		rowIndex++;
		gridPane.add(new Label(i18n.getString("ColumnImportDialog.csv.quote.label", "Quote character:")), columnIndex++, rowIndex);
		gridPane.add(noneQuoteCharRadioButton,      columnIndex++, rowIndex);
		gridPane.add(singleQuoteCharRadioButton,    columnIndex++, rowIndex); 
		gridPane.add(doubleQuoteCharRadioButton,    columnIndex++, rowIndex);
		
		columnIndex = 0;
		rowIndex++;
		gridPane.add(new Label(i18n.getString("ColumnImportDialog.csv.escape.label", "Escape character:")), columnIndex++, rowIndex);
		gridPane.add(noneEscapeCharRadioButton,      columnIndex++, rowIndex);
		gridPane.add(escapeBox,    columnIndex++, rowIndex); 
		
		TitledPane titledPane = this.createTitledPane(
				i18n.getString("ColumnImportDialog.csv.label", "CSV options"),
				i18n.getString("ColumnImportDialog.csv.tooltip", "Specify character-separated-values (CSV) import options"),
				gridPane);

		titledPane.setDisable(!this.importCSVFileCheckBox.isSelected());
		
		return titledPane;
		
	}
	
	private Node createPointColumnPickerPane() {
		GridPane gridPane = this.createGridPane();

		int columnIndex = 0;
		int rowIndex    = 0;
		
		String buttonLabel      = i18n.getString("ColumnImportDialog.column.point.name.label",        "Point-id \u25B6");
		String buttonTooltip    = i18n.getString("ColumnImportDialog.column.point.name.tooltip",      "Add selected range for point-id");
		String textFieldTooltip = i18n.getString("ColumnImportDialog.column.point.name.text.tooltip", "Range for point-id column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.POINT_ID, buttonLabel, buttonTooltip, textFieldTooltip);

		// a-priori y-com,ponent
		buttonLabel      = i18n.getString("ColumnImportDialog.column.point.y0.label",        "y0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.point.y0.tooltip",      "Add selected range for a-priori y-component");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.point.y0.text.tooltip", "Range for a-priori y-component column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.Y, buttonLabel, buttonTooltip, textFieldTooltip);

		// a-priori x-component
		buttonLabel      = i18n.getString("ColumnImportDialog.column.point.x0.label",        "x0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.point.x0.tooltip",      "Add selected range for a-priori x-component");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.point.x0.text.tooltip", "Range for a-priori x-component column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.X, buttonLabel, buttonTooltip, textFieldTooltip);

		// a-priori z-component
		buttonLabel      = i18n.getString("ColumnImportDialog.column.point.z0.label",        "z0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.point.z0.tooltip",      "Add selected range for a-priori z-component");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.point.z0.text.tooltip", "Range for a-priori z-component column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.Z, buttonLabel, buttonTooltip, textFieldTooltip);

		rowIndex++;
		columnIndex = 0;
		
		// Point code
		buttonLabel      = i18n.getString("ColumnImportDialog.column.point.code.label",        "Code \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.point.code.tooltip",      "Add selected range for code");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.point.code.text.tooltip", "Range for code column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.POINT_CODE, buttonLabel, buttonTooltip, textFieldTooltip);

		// Uncertainty in y
		buttonLabel      = i18n.getString("ColumnImportDialog.column.point.sigma.y0.label",        "\u03C3y0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.point.sigma.y0.tooltip",      "Add selected range for a-priori uncertainty of y-component");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.point.sigma.y0.text.tooltip", "Range for a-priori uncertainty of y-component column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.UNCERTAINTY_Y, buttonLabel, buttonTooltip, textFieldTooltip);

		// Uncertainty in x
		buttonLabel      = i18n.getString("ColumnImportDialog.column.point.sigma.x0.label",        "\u03C3x0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.point.sigma.x0.tooltip",      "Add selected range for a-priori uncertainty of x-component");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.point.sigma.x0.text.tooltip", "Range for a-priori uncertainty of x-component column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.UNCERTAINTY_X, buttonLabel, buttonTooltip, textFieldTooltip);

		// Uncertainty in z
		buttonLabel      = i18n.getString("ColumnImportDialog.column.point.sigma.z0.label",        "\u03C3z0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.point.sigma.z0.tooltip",      "Add selected range for a-priori uncertainty of z-component");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.point.sigma.z0.text.tooltip", "Range for a-priori uncertainty of z-component column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.UNCERTAINTY_Z, buttonLabel, buttonTooltip, textFieldTooltip);

		
		TitledPane titledPane = this.createTitledPane(
				i18n.getString("ColumnImportDialog.column.point.label",   "Column defintions for points"),
				i18n.getString("ColumnImportDialog.column.point.tooltip", "Specify column range of file for points import"),
				gridPane);
		
		
		return titledPane;
	}
	
	private Node createGNSSObservationColumnPickerPane() {
		GridPane gridPane = this.createGridPane();

		int columnIndex = 0;
		int rowIndex    = 0;
		
		String buttonLabel      = i18n.getString("ColumnImportDialog.column.gnss.station.label",        "Station \u25B6");
		String buttonTooltip    = i18n.getString("ColumnImportDialog.column.gnss.station.tooltip",      "Add selected range for station");
		String textFieldTooltip = i18n.getString("ColumnImportDialog.column.gnss.station.text.tooltip", "Range for station column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.STATION, buttonLabel, buttonTooltip, textFieldTooltip);

		// a-priori y-com,ponent
		buttonLabel      = i18n.getString("ColumnImportDialog.column.gnss.y0.label",        "y0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.gnss.y0.tooltip",      "Add selected range for a-priori y-component");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.gnss.y0.text.tooltip", "Range for a-priori y-component column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.Y, buttonLabel, buttonTooltip, textFieldTooltip);

		// a-priori x-component
		buttonLabel      = i18n.getString("ColumnImportDialog.column.gnss.x0.label",        "x0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.gnss.x0.tooltip",      "Add selected range for a-priori x-component");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.gnss.x0.text.tooltip", "Range for a-priori x-component column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.X, buttonLabel, buttonTooltip, textFieldTooltip);

		// a-priori z-component
		buttonLabel      = i18n.getString("ColumnImportDialog.column.gnss.z0.label",        "z0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.gnss.z0.tooltip",      "Add selected range for a-priori z-component");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.gnss.z0.text.tooltip", "Range for a-priori z-component column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.Z, buttonLabel, buttonTooltip, textFieldTooltip);

		rowIndex++;
		columnIndex = 0;
		
		// Target point
		buttonLabel      = i18n.getString("ColumnImportDialog.column.gnss.target.label",        "Target \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.gnss.target.tooltip",      "Add selected range for target point");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.gnss.target.text.tooltip", "Range for target point column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.TARGET, buttonLabel, buttonTooltip, textFieldTooltip);

		// Uncertainty in y
		buttonLabel      = i18n.getString("ColumnImportDialog.column.gnss.sigma.y0.label",        "\u03C3y0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.gnss.sigma.y0.tooltip",      "Add selected range for a-priori uncertainty of y-component");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.gnss.sigma.y0.text.tooltip", "Range for a-priori uncertainty of y-component column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.UNCERTAINTY_Y, buttonLabel, buttonTooltip, textFieldTooltip);

		// Uncertainty in x
		buttonLabel      = i18n.getString("ColumnImportDialog.column.gnss.sigma.x0.label",        "\u03C3x0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.gnss.sigma.x0.tooltip",      "Add selected range for a-priori uncertainty of x-component");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.gnss.sigma.x0.text.tooltip", "Range for a-priori uncertainty of x-component column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.UNCERTAINTY_X, buttonLabel, buttonTooltip, textFieldTooltip);

		// Uncertainty in z
		buttonLabel      = i18n.getString("ColumnImportDialog.column.gnss.sigma.z0.label",        "\u03C3z0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.gnss.sigma.z0.tooltip",      "Add selected range for a-priori uncertainty of z-component");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.gnss.sigma.z0.text.tooltip", "Range for a-priori uncertainty of z-component column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.UNCERTAINTY_Z, buttonLabel, buttonTooltip, textFieldTooltip);

		
		TitledPane titledPane = this.createTitledPane(
				i18n.getString("ColumnImportDialog.column.gnss.label",   "Column defintions for GNSS observations"),
				i18n.getString("ColumnImportDialog.column.gnss.tooltip", "Specify column range of file for GNSS observations import"),
				gridPane);
		
		
		return titledPane;
	}
	
	private Node createTerrestrialObservationColumnPickerPane() {
		GridPane gridPane = this.createGridPane();
		
		int columnIndex = 0;
		int rowIndex    = 0;
		
		String buttonLabel      = i18n.getString("ColumnImportDialog.column.terrestrial.station.label",        "Station \u25B6");
		String buttonTooltip    = i18n.getString("ColumnImportDialog.column.terrestrial.station.tooltip",      "Add selected range for station");
		String textFieldTooltip = i18n.getString("ColumnImportDialog.column.terrestrial.station.text.tooltip", "Range for station column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.STATION, buttonLabel, buttonTooltip, textFieldTooltip);

		// Instrumenten height
		buttonLabel      = i18n.getString("ColumnImportDialog.column.terrestrial.station.height.label",        "ih \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.terrestrial.station.height.tooltip",      "Add selected range for instrument height");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.terrestrial.station.height.text.tooltip", "Range for instrument height column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.INSTRUMENT_HEIGHT, buttonLabel, buttonTooltip, textFieldTooltip);

		// a-priori observation value
		buttonLabel      = i18n.getString("ColumnImportDialog.column.terrestrial.value.label",        "Value0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.terrestrial.value.tooltip",      "Add selected range for a-priori observation value");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.terrestrial.value.text.tooltip", "Range for observation value column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.VALUE, buttonLabel, buttonTooltip, textFieldTooltip);

		// Next column
		columnIndex = 0;
		rowIndex++;
		
		// Target point
		buttonLabel      = i18n.getString("ColumnImportDialog.column.terrestrial.target.label",        "Target \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.terrestrial.target.tooltip",      "Add selected range for target point");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.terrestrial.target.text.tooltip", "Range for target point column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.TARGET, buttonLabel, buttonTooltip, textFieldTooltip);

		// target height
		buttonLabel      = i18n.getString("ColumnImportDialog.column.terrestrial.target.height.label",        "th \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.terrestrial.target.height.tooltip",      "Add selected range for target height");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.terrestrial.target.height.text.tooltip", "Range for target height column");

		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.TARGET_HEIGHT, buttonLabel, buttonTooltip, textFieldTooltip);

		// Uncertainty
		buttonLabel      = i18n.getString("ColumnImportDialog.column.terrestrial.sigma0.label",        "\u03C30 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.terrestrial.sigma0.tooltip",      "Add selected range for a-priori uncertainty");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.terrestrial.sigma0.text.tooltip", "Range for uncertainty column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.UNCERTAINTY, buttonLabel, buttonTooltip, textFieldTooltip);

		// Distance for uncertainty
		buttonLabel      = i18n.getString("ColumnImportDialog.column.terrestrial.distance.label",        "d0 \u25B6");
		buttonTooltip    = i18n.getString("ColumnImportDialog.column.terrestrial.distance.tooltip",      "Add selected range for length approximation for distance dependent uncertainty");
		textFieldTooltip = i18n.getString("ColumnImportDialog.column.terrestrial.distance.text.tooltip", "Range for length approximation for distance dependent uncertainty calculation column");
		columnIndex = this.addPickerElement(gridPane, rowIndex, columnIndex, CSVColumnType.DISTANCE_FOR_UNCERTAINTY, buttonLabel, buttonTooltip, textFieldTooltip);

		TitledPane titledPane = this.createTitledPane(
				i18n.getString("ColumnImportDialog.column.terrestrial.label",   "Column defintions for terrestrial observations"),
				i18n.getString("ColumnImportDialog.column.terrestrial.tooltip", "Specify column range of file for terrestrial observations import"),
				gridPane);
		
		return titledPane;
	}

	private int addPickerElement(GridPane gridPane, int rowIndex, int columnIndex, CSVColumnType type, String buttonLabel, String buttonTooltip, String textFieldTooltip) {
		String promptText = i18n.getString("ColumnImportDialog.column.range.prompt", "from - to");
		TextField textField = new TextField();
		textField.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		textField.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		textField.setPrefWidth(100);
		textField.setTooltip(new Tooltip(textFieldTooltip));
		textField.setPromptText(promptText);
		textField.setUserData(type);
		
		UnaryOperator<Change> textFilter = new UnaryOperator<Change>() {
			@Override
			public Change apply(Change change) {
				String input = change.getControlNewText(); //change.getText();
				Pattern pattern;
				if (importCSVFileCheckBox.isSelected())
					pattern = Pattern.compile("^(\\d+)$");
				else
					pattern = Pattern.compile("^(\\d+)(\\s*-\\s*(\\d*))?$");
				
			    Matcher matcher = pattern.matcher(input.trim());
				if (!change.isContentChange() || input.trim().isEmpty())
					return change;

				if (!matcher.matches()) 
			    	return null;

				return change;
			}
		};
		
		textField.setTextFormatter(new TextFormatter<String>(textFilter));

		Button button = new Button(buttonLabel);
		button.setTooltip(new Tooltip(buttonTooltip));
		button.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		button.setUserData(textField);

		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				int columns[] = getSelectedColumns();
				int min = columns[0];
				int max = columns[1];
				
				if (min >= 0 && max >= 0) {
					if (importCSVFileCheckBox.isSelected())
						textField.setText(String.valueOf(min));
					else
						textField.setText(String.valueOf(min) + " - " + String.valueOf(max));
				}
			}
		});

		// child, columnIndex, rowIndex
		gridPane.add(button,    columnIndex++, rowIndex);
		gridPane.add(textField, columnIndex++, rowIndex);
		
		GridPane.setHgrow(button, Priority.NEVER);
		GridPane.setHgrow(textField, Priority.ALWAYS);

		this.textFieldList.add(textField);
		return columnIndex;
	}

	private Node initEditor() {
		ColumnSelectionEventHandler columnSelectionEventHandler = new ColumnSelectionEventHandler();
		this.editor = new HBox();
		this.editor.setOnMouseMoved(columnSelectionEventHandler);
		this.editor.setOnMouseReleased(columnSelectionEventHandler);
		this.editor.setOnMousePressed(columnSelectionEventHandler);
		this.editor.setOnMouseDragged(columnSelectionEventHandler);
		this.editor.setOnMouseClicked(columnSelectionEventHandler);

		this.editor.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.editor.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.editor.setPadding(new Insets(5));
		
		this.editor.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		this.editor.setBorder(new Border(new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		VBox column = new VBox(0);
		column.setBackground( new Background( new BackgroundFill( Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY ) ) );
		column.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT,
				BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE,
				CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)));
		
		for (int j=0; j<MAX_LINES; j++) {
			Text text = new Text(" ");
			text.setFont(Font.font("MonoSpace", FontWeight.NORMAL, 12));
			column.getChildren().add(text);
		}

		this.editor.getChildren().add(column);

		ScrollPane editorScrollPane = new ScrollPane(this.editor);
		editorScrollPane.setFitToHeight(true);
		editorScrollPane.setFitToWidth(true);
		
		TitledPane titledPane = this.createTitledPane(
				i18n.getString("ColumnImportDialog.file.preview.label", "File preview"),
				i18n.getString("ColumnImportDialog.file.preview.tooltip", "Preview of selected file"),
				editorScrollPane);
		
		return titledPane;
	}

	private void addColumnHeader(int columns, int columnSize[]) {
		StringBuffer headerLine = new StringBuffer();
		
		if (this.importCSVFileCheckBox != null && this.importCSVFileCheckBox.isSelected() && columnSize != null) {
			int j=0, index = 1;
			int columnLength = 0;
			int halfInterval = 0;
			for (int i = 0; i < columns; i++) {
				if (i == columnLength) {
					headerLine.append("["); //\uFE64
					columnLength += columnSize[j];
					halfInterval =  columnSize[j]/2 + 1; // new +1 to center marker
					j++;
				}
				else if (i == columnLength-1) {
					headerLine.append("]"); // \uFE65
				}
				else if (i == columnLength - halfInterval) {
					String str = String.valueOf(index++);
					int endPos = i + 1;
					int startPos = endPos - str.length();
					headerLine.replace(startPos, endPos, str);
				}
				else
					headerLine.append("\u2219");
			}
		}
		else {
			for (int i=0; i<columns; i++) {
				if (i % 5 == 0) // && i%10 != 0
					headerLine.append("+");
				else
					headerLine.append("\u2219");

				if (i % 10 == 0) {
					String str = String.valueOf(i);
					int endPos = i + 1;
					int startPos = endPos - str.length();
					headerLine.replace(startPos, endPos, str);
				}
			}
		}

		for (int j=0; j<columns; j++) {
			Text text = new Text(String.valueOf(headerLine.charAt(j)));
			text.setFont(Font.font("MonoSpace", FontWeight.BOLD, 12));
			text.setFill(Color.DARKBLUE);
			text.setTextAlignment(TextAlignment.CENTER);

			this.columns.get(j).getChildren().add(text);
		}
	}

	private void selectColumns(int startColumn, int endColumn) {
		for (int columnIndex = 0; columnIndex < this.columns.size(); columnIndex++) {
			VBox column = this.columns.get(columnIndex);
			if (columnIndex >= startColumn && columnIndex <= endColumn) {
				column.setBackground( new Background( new BackgroundFill( Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY ) ) );
				column.setBorder(new Border(new BorderStroke(
						Color.BLACK, 
						columnIndex == endColumn ? Color.BLACK : Color.TRANSPARENT, 
						Color.BLACK, 
						columnIndex == startColumn ? Color.BLACK : 	Color.TRANSPARENT,
						BorderStrokeStyle.DOTTED, 
						columnIndex == endColumn ? BorderStrokeStyle.DOTTED : BorderStrokeStyle.NONE, 
						BorderStrokeStyle.DOTTED, 
						columnIndex == startColumn ? BorderStrokeStyle.DOTTED : BorderStrokeStyle.NONE,
						CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)
				));
			}
			else {
				column.setBackground( new Background( new BackgroundFill( Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY ) ) );
				column.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT,
						BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE,
						CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)));
			}
		}
	}
	
	private void readPreview(File f) throws IOException, SQLException {
		PreviewFileReader reader = new PreviewFileReader(f, MAX_LINES);
		reader.ignoreLinesWhichStartWith("#");
		
		reader.read();
		this.linesOfFile.clear();
		this.linesOfFile = reader.getLines();
		this.changeMode();
	}
	
	private int[] getSelectedColumns() {
		if (this.importCSVFileCheckBox.isSelected()) {
			int columnLength = 0;
			for (int i = 0; i < this.maxCharactersPerColumn.length; i++) {
				if (this.startColumn <= columnLength && columnLength < this.endColumn) 
					return new int[] {i+1, i+1};
				columnLength += this.maxCharactersPerColumn[i];
			}
		}

		return new int[] {
			Math.min(this.endColumn, this.startColumn), 
			Math.max(this.endColumn, this.startColumn)
		};
	}
	
	private void showContentPreview() {
		// Size of content
		int contentColumns = 0;
		int contentRows    = this.linesOfFile.size();
		this.columns.clear();
		this.editor.getChildren().clear();
		
		this.maxCharactersPerColumn = null;
		List<String> formattedLines = new ArrayList<String>(contentRows);
		
		if (this.importCSVFileCheckBox != null && this.importCSVFileCheckBox.isSelected()) {
			if (anyCharactersAreTheSame(this.separator, this.quotechar, this.escape))
				return;
			
			final int BUFFER_CHARS = 5;
			try {
				boolean strictQuotes = CSVParser.DEFAULT_STRICT_QUOTES;
				boolean ignoreLeadingWhiteSpace = CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE;
				boolean ignoreQuotations = CSVParser.DEFAULT_IGNORE_QUOTATIONS;
				
				CSVParser csvParser = new CSVParser(this.separator, this.quotechar, this.escape, strictQuotes, ignoreLeadingWhiteSpace, ignoreQuotations);
				int columCounter = 0;
				
				// parse CSV 
				List<String[]> fileData = new ArrayList<String[]>(20);
				List<String> row = new ArrayList<String>(20);
				for (int i = 0; i < this.linesOfFile.size(); i++) {
					String line = this.linesOfFile.get(i);
					String parsedLine[] = csvParser.parseLineMulti(line);

					if (parsedLine != null && parsedLine.length > 0) {
						row.addAll(Arrays.asList(parsedLine));
					}

					if (!csvParser.isPending()) {
						fileData.add(row.toArray(new String[row.size()]));
						columCounter = Math.max(columCounter, row.size());
						row.clear();
					}
				}
				
				// max. number of characters per column (for cell formatter)
				this.maxCharactersPerColumn = new int[columCounter];
				for (String[] line : fileData) {
					for (int i=0; i<line.length; i++) {
						line[i] = line[i].replace('\n', ' ').replaceAll("\t", TABULATOR).trim();
						this.maxCharactersPerColumn[i] = Math.max(this.maxCharactersPerColumn[i], line[i].length() + BUFFER_CHARS);
					}
				}

				for (String[] line : fileData) {
					StringBuilder formattedLine = new StringBuilder();
					for (int i=0; i<line.length; i++) {
						formattedLine.append(
								String.format(Locale.ENGLISH, "%"+maxCharactersPerColumn[i]+"s", line[i])
						);
					}
					formattedLines.add(formattedLine.toString().replaceAll("\\s+$", ""));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			for (String line : this.linesOfFile)
				formattedLines.add(line.replaceAll("\t", TABULATOR));
		}
		
		// max row
		for (String str : formattedLines)
			contentColumns = Math.max(contentColumns,  str.length());

		// Add column nodes to editor
		for (int i = 0; i < contentColumns; i++) {
			VBox column = new VBox(0);
			column.setUserData(new ColumnIndex(i));

			column.setBackground( new Background( new BackgroundFill( Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY ) ) );
			column.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT,
					BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE,
					CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)));

			this.editor.getChildren().add(column);
			this.columns.add(column);
		}

		// set header line
		this.addColumnHeader(contentColumns, this.maxCharactersPerColumn);

		// add content
		for (int i = 0; i < Math.min(formattedLines.size(), MAX_LINES); i++) {
			String line = String.format(Locale.ENGLISH, "%-" + contentColumns + "s", formattedLines.get(i));
			for (int j = 0; j < contentColumns; j++) {
				Text character = new Text(String.valueOf(line.charAt(j)));
				character.setFont(Font.font("MonoSpace", FontWeight.NORMAL, 12));
				character.setFill(Color.BLACK);
				character.setTextAlignment(TextAlignment.CENTER);

				this.columns.get(j).getChildren().add(character);
			}
		}
		
		for (int i = formattedLines.size(); i < MAX_LINES; i++) {
			for (int j = 0; j < contentColumns; j++) {
				Text character = new Text(" ");
				character.setFont(Font.font("MonoSpace", FontWeight.NORMAL, 12));
				character.setFill(Color.TRANSPARENT);
				character.setTextAlignment(TextAlignment.CENTER);
				this.columns.get(j).getChildren().add(character);
			}
		}
	}

	private ComboBox<Enum<?>> createImportTypeComboBox() {
		ComboBox<Enum<?>> typeComboBox = new ComboBox<Enum<?>>();
		List<Enum<?>> model = typeComboBox.getItems();
		
		for (PointType type : PointType.values())
			model.add(type);
		
		for (ObservationType type : ObservationType.values())
			model.add(type);

		typeComboBox.setConverter(new StringConverter<Enum<?>>() {

			@Override
			public Enum<?> fromString(String string) {
				Enum<?> type = PointType.valueOf(string);
				return type != null ? type : ObservationType.valueOf(string);
			}

			@Override
			public String toString(Enum<?> item) {
				if (item instanceof PointType) {
					PointType type = (PointType)item;
					switch(type) {
					case DATUM_POINT:
						return i18n.getString("UITreeBuiler.directory.datumpoints", "Datum points");
						
					case NEW_POINT:
						return i18n.getString("UITreeBuiler.directory.newpoints", "New points");
						
					case REFERENCE_POINT:
						return i18n.getString("UITreeBuiler.directory.referencepoints", "Reference points");
						
					case STOCHASTIC_POINT:
						return i18n.getString("UITreeBuiler.directory.stochasticpoints", "Stochastic points");
					}
				}
				
				else if (item instanceof ObservationType) {
					ObservationType type = (ObservationType)item;
					switch(type) {
					case LEVELING:
						return i18n.getString("UITreeBuiler.directory.terrestrialobservations.leveling", "Leveling data");
						
					case DIRECTION:
						return i18n.getString("UITreeBuiler.directory.terrestrialobservations.direction", "Direction sets");
						
					case HORIZONTAL_DISTANCE:
						return i18n.getString("UITreeBuiler.directory.terrestrialobservations.horizontal_distance", "Horizontal distances");
					
					case SLOPE_DISTANCE:
						return i18n.getString("UITreeBuiler.directory.terrestrialobservations.slope_distance", "Slope distances");
						
					case ZENITH_ANGLE:
						return i18n.getString("UITreeBuiler.directory.terrestrialobservations.zenith_angle", "Zenith angles");
						
					case GNSS1D:
						return i18n.getString("UITreeBuiler.directory.gnssobservations.1d", "GNSS baselines 1D");
						
					case GNSS2D:
						return i18n.getString("UITreeBuiler.directory.gnssobservations.2d", "GNSS baselines 2D");
						
					case GNSS3D:
						return i18n.getString("UITreeBuiler.directory.gnssobservations.3d", "GNSS baselines 3D");
					}
				}
				
				return null;
			}
		});

		typeComboBox.setTooltip(new Tooltip(i18n.getString("ColumnImportDialog.import.type.tooltip", "Select import type")));
		
		typeComboBox.setMinWidth(Control.USE_PREF_SIZE);
		typeComboBox.setMaxWidth(Double.MAX_VALUE);
		typeComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Enum<?>>() {
			@Override
			public void changed(ObservableValue<? extends Enum<?>> observable, Enum<?> oldValue, Enum<?> newValue) {
				if (newValue instanceof PointType) {
					terrestrialObservationColumnPickerPane.setVisible(false);
					terrestrialObservationColumnPickerPane.setManaged(false);
					
					gnssObservationColumnPickerPane.setVisible(false);
					gnssObservationColumnPickerPane.setManaged(false);
					
					pointColumnPickerPane.setVisible(true);
					pointColumnPickerPane.setManaged(true);
				}

				
				else if (newValue instanceof ObservationType) {
					if (isGNSS((ObservationType)newValue)) {
						gnssObservationColumnPickerPane.setVisible(true);
						gnssObservationColumnPickerPane.setManaged(true);
						
						terrestrialObservationColumnPickerPane.setVisible(false);
						terrestrialObservationColumnPickerPane.setManaged(false);
					}
					else {
						terrestrialObservationColumnPickerPane.setVisible(true);
						terrestrialObservationColumnPickerPane.setManaged(true);
						
						gnssObservationColumnPickerPane.setVisible(false);
						gnssObservationColumnPickerPane.setManaged(false);
					}
					
					pointColumnPickerPane.setVisible(false);
					pointColumnPickerPane.setManaged(false);
				}
				
			}
		});
		typeComboBox.getSelectionModel().select(PointType.REFERENCE_POINT);
		return typeComboBox;
	}
	
	private CheckBox createCSVCheckbox() {
		String title   = i18n.getString("ColumnImportDialog.type.csv.label", "CSV file");
		String tooltip = i18n.getString("ColumnImportDialog.type.csv.tooltip", "If selected, file is parsed as character-separated-values (CSV) file");
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.setPadding(new Insets(0,0,0,3));
		CheckBox checkBox = new CheckBox();
		checkBox.setGraphic(label);
		checkBox.setTooltip(new Tooltip(tooltip));
		checkBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		checkBox.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				changeMode();
			}
			
		});
		this.changeMode();
		return checkBox;
	}
	
	private RadioButton createRadioButton(String title, String tooltip, boolean selected, Object userData, ToggleGroup group, ChangeListener<Boolean> listener) {
		Label label = new Label(title);
		label.setPadding(new Insets(0,0,0,3));
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		RadioButton radioButton = new RadioButton();
		radioButton.setGraphic(label);
		radioButton.setTooltip(new Tooltip(tooltip));
		radioButton.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		radioButton.setMaxWidth(Double.MAX_VALUE);
		radioButton.setUserData(userData);
		radioButton.setSelected(selected);
		radioButton.setToggleGroup(group);
		radioButton.selectedProperty().addListener(listener);
		return radioButton;
	}
	
	private TitledPane createTitledPane(String title, String tooltip, Node content) {
		Label label = new Label(title);
		label.setTooltip(new Tooltip(tooltip));
		TitledPane titledPane = new TitledPane();
//		titledPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		titledPane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		titledPane.setGraphic(label);
		titledPane.setCollapsible(false);
		titledPane.setAnimated(false);
		titledPane.setContent(content);
		titledPane.setPadding(new Insets(0, 10, 5, 10)); // oben, links, unten, rechts
		return titledPane;
	}
	
	private GridPane createGridPane() {		
		GridPane gridPane = new GridPane();
		gridPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		gridPane.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		gridPane.setAlignment(Pos.CENTER_LEFT);
		gridPane.setHgap(15);
		gridPane.setVgap(7);
		gridPane.setPadding(new Insets(5, 5, 5, 5)); // oben, recht, unten, links
		return gridPane;
	}
	
	private void changeMode() {
		boolean isCSVMode = this.importCSVFileCheckBox != null && this.importCSVFileCheckBox.isSelected();
		if (this.csvOptionPane != null) {
			this.csvOptionPane.setDisable(!isCSVMode);
		}
		
		String promptText = isCSVMode ? i18n.getString("ColumnImportDialog.column.index.prompt", "Index") : i18n.getString("ColumnImportDialog.column.range.prompt", "from - to");
		for (TextField textfield : this.textFieldList) {
			textfield.setPromptText(promptText);
			textfield.setText("");
		}
		
		showContentPreview();
	}
	
	private boolean anyCharactersAreTheSame(char separator, char quotechar, char escape) {
        return isSameCharacter(separator, quotechar) || isSameCharacter(separator, escape) || isSameCharacter(quotechar, escape);
    }

    private boolean isSameCharacter(char c1, char c2) {
        return c1 != CSVParser.NULL_CHARACTER && c1 == c2;
    }
    
    private ColumnRange getColumnRange(TextField textField) {
    	if (textField.getText() == null || textField.getText().trim().isEmpty() || 
    			textField.getUserData() == null || !(textField.getUserData() instanceof CSVColumnType))
    		return null;
    	
    	CSVColumnType type = (CSVColumnType)textField.getUserData();
		final Pattern pattern = Pattern.compile("^(\\d+)(\\s*-\\s*(\\d*))?$");
	    Matcher matcher = pattern.matcher(textField.getText().trim());
	    
	    if (matcher.matches()) {
	    	if (matcher.group(3) == null && this.importCSVFileCheckBox.isSelected()) 
	    		return new ColumnRange(type, Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(1)));
	    	else
	    		return new ColumnRange(type, Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(3)));
	    }
	    return null;
	}
    
    private SourceFileReader getSourceFileReader() {
    	List<ColumnRange> columnRanges = new ArrayList<ColumnRange>(this.textFieldList.size());
    	for (TextField textField : this.textFieldList) {
    		ColumnRange columnRange = this.getColumnRange(textField);
    		if (columnRange != null)
    			columnRanges.add(columnRange);
    	}

    	if (columnRanges.isEmpty())
    		return null;
    	
    	if (this.importCSVFileCheckBox.isSelected()) {
    		boolean strictQuotes = CSVParser.DEFAULT_STRICT_QUOTES;
    		boolean ignoreLeadingWhiteSpace = CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE;
    		boolean ignoreQuotations = CSVParser.DEFAULT_IGNORE_QUOTATIONS;
    		CSVParser csvParser = new CSVParser(this.separator, this.quotechar, this.escape, strictQuotes, ignoreLeadingWhiteSpace, ignoreQuotations);

    		if (this.importTypes.getValue() instanceof ObservationType) {
    			ObservationType observationType = (ObservationType)this.importTypes.getValue();
    			CSVObservationFileReader reader = new CSVObservationFileReader(observationType, csvParser);
    			reader.setColumnRanges(columnRanges);
    			reader.setFileLocale(this.fileLocale);
    			return reader;
    		}
    		else if (this.importTypes.getValue() instanceof PointType) {
    			PointType pointType = (PointType)this.importTypes.getValue();
    			CSVPointFileReader reader = new CSVPointFileReader(pointType, csvParser);
    			reader.setColumnRanges(columnRanges);
    			reader.setFileLocale(this.fileLocale);
    			return reader;
    		}
    	}
    	else {
    		if (this.importTypes.getValue() instanceof ObservationType) {
    			ObservationType observationType = (ObservationType)this.importTypes.getValue();
    			ColumnDefinedObservationFileReader reader = new ColumnDefinedObservationFileReader(observationType, TABULATOR);
    			reader.setColumnRanges(columnRanges);
    			reader.setFileLocale(this.fileLocale);
    			return reader;
    		}
    		else if (this.importTypes.getValue() instanceof PointType) {
    			PointType pointType = (PointType)this.importTypes.getValue();
    			ColumnDefinedPointFileReader reader = new ColumnDefinedPointFileReader(pointType, TABULATOR);
    			reader.setColumnRanges(columnRanges);
    			reader.setFileLocale(this.fileLocale);
    			return reader;
    		}				
    	}
    	return null;	
	}
    
    private boolean isGNSS(ObservationType type) {
    	switch(type) {
		case GNSS1D:
		case GNSS2D:
		case GNSS3D:
			return true;
		default:
			return false;
    	}
    }
}
