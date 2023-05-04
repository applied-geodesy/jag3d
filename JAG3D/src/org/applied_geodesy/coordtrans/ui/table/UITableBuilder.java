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

package org.applied_geodesy.coordtrans.ui.table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.ui.table.ColumnTooltipHeader;
import org.applied_geodesy.ui.table.ColumnType;
import org.applied_geodesy.ui.table.EditableCell;
import org.applied_geodesy.ui.table.EditableDoubleCellConverter;
import org.applied_geodesy.ui.table.EditableIntegerCellConverter;
import org.applied_geodesy.ui.table.EditableStringCellConverter;
import org.applied_geodesy.util.CellValueType;
import org.applied_geodesy.util.FormatterChangedListener;
import org.applied_geodesy.util.FormatterEvent;
import org.applied_geodesy.util.FormatterOptions;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

public abstract class UITableBuilder<T> {
	
	class NumberAndUnitFormatterChangedListener implements FormatterChangedListener {

		@Override
		public void formatterChanged(FormatterEvent evt) {
			if (table != null)
				table.refresh();
		}
	}

	class TableKeyEventHandler implements EventHandler<KeyEvent> {
		KeyCodeCombination copyKeyCodeCompination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
		public void handle(final KeyEvent keyEvent) {

			if (copyKeyCodeCompination.match(keyEvent)) {
				if(keyEvent.getSource() instanceof TableView) {
					copySelectionToClipboard( (TableView<?>) keyEvent.getSource());
					keyEvent.consume();
				}
			}
		}
	}

	class TableCellChangeListener<S> implements ChangeListener<S> {
		private final int columnIndex;
		private final T rowData; 
		TableCellChangeListener(int columnIndex, T rowData) {
			this.columnIndex = columnIndex;
			this.rowData = rowData;
		}

		@Override
		public void changed(ObservableValue<? extends S> observable, S oldValue, S newValue) {
			if (this.rowData != null) {
				List<T> selectedItems = table.getSelectionModel().getSelectedItems();
				if (selectedItems == null || selectedItems.isEmpty() || !selectedItems.contains(this.rowData)) {
					selectedItems = new ArrayList<T>(1);
					selectedItems.add(this.rowData);
					table.getSelectionModel().clearSelection();
					table.getSelectionModel().select(this.rowData);
				}
				
				for (T item : selectedItems)
					setValue(item, columnIndex, oldValue, newValue);
				
				if (selectedItems.size() > 1)
					table.refresh();
				
//				setValue(rowData, columnIndex, oldValue, newValue);
			}
		}
	}

	class TableCellEvent<S> implements EventHandler<CellEditEvent<T, S>> {
		private final int columnIndex;
		TableCellEvent(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		@Override
		public void handle(CellEditEvent<T, S> event) {
			if (event.getTableColumn().isEditable()) {
				setValue(event.getRowValue(), this.columnIndex, event.getOldValue(), event.getNewValue());
			}
		}
	}

	final NumberAndUnitFormatterChangedListener numberAndUnitFormatterChangedListener = new NumberAndUnitFormatterChangedListener();
	static FormatterOptions options = FormatterOptions.getInstance();
	static I18N i18n = I18N.getInstance();
	TableView<T> table = this.createTable();

	UITableBuilder() {
		options.addFormatterChangedListener(this.numberAndUnitFormatterChangedListener);
	}

	TableView<T> createTable() {
		this.table = new TableView<T>();
		ObservableList<T> tableModel = FXCollections.observableArrayList();
		SortedList<T> sortedList = new SortedList<T>(tableModel);
		sortedList.comparatorProperty().bind(this.table.comparatorProperty());
		this.table.setItems(sortedList);
		this.table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		this.table.setTableMenuButtonVisible(false);
		this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.table.setOnKeyPressed(new TableKeyEventHandler());
		
		ListView<String> placeholderList = new ListView<String>();
		placeholderList.getItems().add(new String());
		placeholderList.setDisable(true);
		this.table.setPlaceholder(placeholderList);
		
		return table;
	}

	static <T> Callback<TableColumn<T,Integer>, TableCell<T,Integer>> getIntegerCallback() {
		return new Callback<TableColumn<T, Integer>, TableCell<T, Integer>>() {
			@Override
			public TableCell<T, Integer> call(TableColumn<T, Integer> cell) {
				TableCell<T, Integer> tableCell = new EditableCell<T, Integer>(new EditableIntegerCellConverter());
				tableCell.setAlignment(Pos.CENTER_RIGHT);
				return tableCell;
			}
		};
	}

	static <T> Callback<TableColumn<T,Double>, TableCell<T,Double>> getDoubleCallback(CellValueType cellValueType) {
		return new Callback<TableColumn<T, Double>, TableCell<T, Double>>() {
			@Override
			public TableCell<T, Double> call(TableColumn<T, Double> cell) {
				TableCell<T, Double> tableCell = new EditableCell<T, Double>(new EditableDoubleCellConverter(cellValueType));
				tableCell.setAlignment(Pos.CENTER_RIGHT);
				return tableCell;
			}
		};
	}

	static <T> Callback<TableColumn<T,Double>, TableCell<T,Double>> getDoubleCallback(CellValueType cellValueType, boolean displayUnit) {
		return new Callback<TableColumn<T, Double>, TableCell<T, Double>>() {
			@Override
			public TableCell<T, Double> call(TableColumn<T, Double> cell) {
				TableCell<T, Double> tableCell = new EditableCell<T, Double>(new EditableDoubleCellConverter(cellValueType, displayUnit));
				tableCell.setAlignment(Pos.CENTER_RIGHT);
				return tableCell;
			}
		};
	}

	static <T> Callback<TableColumn<T,String>, TableCell<T,String>> getStringCallback() {
		return new Callback<TableColumn<T,String>, TableCell<T,String>>() {
			@Override
			public TableCell<T,String> call(TableColumn<T,String> cell) {
				TableCell<T,String> tableCell = new EditableCell<T, String>(new EditableStringCellConverter());
				tableCell.setAlignment(Pos.CENTER_LEFT);
				return tableCell;
			}
		};
	}

	static <T> Callback<TableColumn<T,Boolean>, TableCell<T,Boolean>> getBooleanCallback() {
		return new Callback<TableColumn<T, Boolean>, TableCell<T, Boolean>>() {
			@Override
			public TableCell<T, Boolean> call(TableColumn<T, Boolean> cell) {
				TableCell<T, Boolean> tableCell = new CheckBoxTableCell<T, Boolean>();
				tableCell.setAlignment(Pos.CENTER);
				return tableCell;
			}
		};
	}
	
	static <T, S> Callback<CellDataFeatures<T, S>, ObservableValue<S>> getCellValueFactoryCallback(Function<T, ObservableValue<S>> property) {
		return new Callback<CellDataFeatures<T, S>, ObservableValue<S>>() {
			@Override
			public ObservableValue<S> call(CellDataFeatures<T, S> param) {
				ObservableValue<S> observableValue = property.apply(param.getValue());
				return observableValue;
			}
		};
	}
	
	<S>TableColumn<T, S> getColumn(ColumnTooltipHeader header, Function<T, ObservableValue<S>> property, Callback<TableColumn<T, S>, TableCell<T, S>> callback, ColumnType type, int columnIndex, boolean editable) {
		return getColumn(header, getCellValueFactoryCallback(property), callback, type, columnIndex, editable);
	}

	<S>TableColumn<T, S> getColumn(ColumnTooltipHeader header, Callback<CellDataFeatures<T, S>, ObservableValue<S>> cellValueFactoryCallback, Callback<TableColumn<T, S>, TableCell<T, S>> callback, ColumnType type, int columnIndex, boolean editable) {
		final TableCellEvent<S> tableCellEvent = new TableCellEvent<S>(columnIndex);
		TableColumn<T, S> column = new TableColumn<T, S>();
		Label columnLabel = header.getLabel();
		columnLabel.getStyleClass().add("column-header-label");
		columnLabel.setMaxWidth(Double.MAX_VALUE);
		columnLabel.setTooltip(header.getTooltip());

		column.setUserData(type);
		column.setEditable(editable);
		column.setGraphic(columnLabel);
		column.setMinWidth(25);
		column.setPrefWidth(125);
		column.setCellValueFactory(cellValueFactoryCallback);
		column.setCellFactory(callback);
		column.setOnEditCommit(tableCellEvent);
		return column;
	}
	
	public void refreshTable() {
		if (this.table != null)
			this.table.refresh();
	}

	abstract void setValue(T row, int columnIndex, Object oldValue, Object newValue);

	public TableView<T> getTable() {
		return this.table;
	}
	
	@SuppressWarnings("unchecked")
	public ObservableList<T> getTableModel(TableView<T> tableView) {
		if (tableView.getItems() instanceof SortedList) {
			SortedList<T> sortedList = (SortedList<T>)tableView.getItems();
			return (ObservableList<T>)sortedList.getSource();
		}
		return tableView.getItems();
	}

	public static void copySelectionToClipboard(TableView<?> table) {
		StringBuilder clipboardString = new StringBuilder();

		for (TablePosition<?,?> position : table.getSelectionModel().getSelectedCells()) {
			int rowIndex = position.getRow();

			if (rowIndex < 0 || rowIndex >= table.getItems().size())
				continue;

			for (TableColumn<?, ?> column : table.getColumns()) {
				if (!column.isVisible())
					continue;

				Object cell = column.getCellData(rowIndex);

				if (cell == null)
					cell = " ";

				clipboardString.append(cell.toString()).append('\t');
			} 
			clipboardString.append("\r\n");
		}

		final ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.putString(clipboardString.toString());
		Clipboard.getSystemClipboard().setContent(clipboardContent);
	}

	public static Object getValueAt(TableView<?> table, int columnIndex, int rowIndex) {
		return table.getColumns().get(columnIndex).getCellObservableValue(rowIndex).getValue();
	}
}