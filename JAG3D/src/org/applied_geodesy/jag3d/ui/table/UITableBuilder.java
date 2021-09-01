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

package org.applied_geodesy.jag3d.ui.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.function.Function;

import org.applied_geodesy.jag3d.ui.table.column.ColumnContentType;
import org.applied_geodesy.jag3d.ui.table.column.ColumnPropertiesManager;
import org.applied_geodesy.jag3d.ui.table.column.ColumnProperty;
import org.applied_geodesy.jag3d.ui.table.column.ContentColumn;
import org.applied_geodesy.jag3d.ui.table.column.TableContentType;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlight;
import org.applied_geodesy.jag3d.ui.table.rowhighlight.TableRowHighlightRangeType;
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
import org.applied_geodesy.jag3d.ui.i18n.I18N;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
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
	
	private class SortOrderChangeListener implements ListChangeListener<TableColumn<T, ?>> {
		@Override
		public void onChanged(Change<? extends TableColumn<T, ?>> change) {
			while (change.next()) {
				if (change.wasRemoved()) {
					for (TableColumn<T, ?> removedColumnItem : change.getRemoved()) {
						if (removedColumnItem instanceof ContentColumn) {
							((ContentColumn<T,?>)removedColumnItem).getColumnProperty().setSortOrder(-1);
						}	
					}
				}
			}
			
			int idx = 0;
			for (TableColumn<T, ?> addedColumnItem : change.getList()) {
				if (addedColumnItem instanceof ContentColumn) {
					((ContentColumn<T,?>)addedColumnItem).getColumnProperty().setSortOrder(idx++);
				}
			}
		}
	}
	
	private class ColumnsOrderChangeListener implements ListChangeListener<TableColumn<T, ?>> {
		@Override
		public void onChanged(Change<? extends TableColumn<T, ?>> change) {	
			int idx = 0;
			for (TableColumn<T, ?> addedColumnItem : change.getList()) {
				if (addedColumnItem instanceof ContentColumn) {
					((ContentColumn<T,?>)addedColumnItem).getColumnProperty().setColumnOrder(idx++);
				}
			}
		}
	}
	
	private class SortOrderSequenceChangeListener implements ListChangeListener<ColumnContentType> {
		
		private final TableView<T> tableView;
		public SortOrderSequenceChangeListener(TableView<T> tableView) {
			this.tableView = tableView;
		}
		
		@Override
		public void onChanged(Change<? extends ColumnContentType> change) {
			setSortOrderSequence(this.tableView, change.getList());
		}
	}
	
	private class ColumnsOrderSequenceChangeListener implements ListChangeListener<ColumnContentType> {
		private final TableView<T> tableView;
		public ColumnsOrderSequenceChangeListener(TableView<T> tableView) {
			this.tableView = tableView;
		}
		
		@Override
		public void onChanged(Change<? extends ColumnContentType> change) {
			setColumnsOrderSequence(this.tableView, change.getList());
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
		this.table.setItems(tableModel);
		this.table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		this.table.setTableMenuButtonVisible(false);
		//		this.table.setPlaceholder(new Text(i18n.getString("UITableBuilder.emptytable", "No content in table.")));
		this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.table.setOnKeyPressed(new TableKeyEventHandler());
		tableModel.add(this.getEmptyRow());
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

	void addContextMenu(TableView<T> table, ContextMenu contextMenu) {
		table.setRowFactory(new Callback<TableView<T>, TableRow<T>>() {  
			@Override  
			public TableRow<T> call(TableView<T> tableView) {  
				final TableRow<T> row = new TableRow<T>() {
					@Override
					public void updateItem(T item, boolean empty) {
						super.updateItem(item, empty);
						// highlight current row
						highlightTableRow(this);
					}
				};
				// Set context menu on row, but use a binding to make it only show for non-empty rows:  
				row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu)null).otherwise(contextMenu)); 
				return row;  
			} 
			
			
		}); 
	}

	void addDynamicRowAdder(TableView<T> table) {
		table.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {

				if (event.getButton().equals(MouseButton.PRIMARY) && event.getSource() == table) {
					if (event.getTarget() instanceof TableCell && table.getItems() != null && ((TableCell<?, ?>)event.getTarget()).getIndex() >= table.getItems().size()) {
						table.getSelectionModel().clearAndSelect(table.getItems().size() - 1);
					}
					else if (event.getTarget() instanceof TableCell && table.getItems() != null && table.getItems().size() == table.getSelectionModel().getSelectedIndex() + 1) {
						table.getItems().add(getEmptyRow());
						//						table.scrollTo(emptyRow);
						//						table.getSelectionModel().clearAndSelect(table.getItems().size() - 1);
						//						table.getSelectionModel().select(emptyRow);

					}
				}
			}
		});
	}
	
	void addColumnOrderSequenceListeners(TableContentType tableContentType, TableView<T> table) {
		if (tableContentType != TableContentType.UNSPECIFIC) {
			ObservableList<ColumnContentType>  sortOrder    = ColumnPropertiesManager.getInstance().getSortOrder(tableContentType);
			ObservableList<ColumnContentType>  columnsOrder = ColumnPropertiesManager.getInstance().getColumnsOrder(tableContentType);
			this.setSortOrderSequence(table, sortOrder);
			this.setColumnsOrderSequence(table, columnsOrder);
			table.getSortOrder().addListener(new SortOrderChangeListener());
			table.getColumns().addListener(new ColumnsOrderChangeListener());
			sortOrder.addListener(new SortOrderSequenceChangeListener(table));
			columnsOrder.addListener(new ColumnsOrderSequenceChangeListener(table));
		}
	}

	<S>TableColumn<T, S> getColumn(ColumnTooltipHeader header, Function<T, ObservableValue<S>> property, Callback<TableColumn<T, S>, TableCell<T, S>> callback, ColumnType type, int columnIndex, boolean editable) {
		return getColumn(TableContentType.UNSPECIFIC, ColumnContentType.DEFAULT, header, property, callback, type, columnIndex, editable, Boolean.FALSE);
	}
	
	<S>TableColumn<T, S> getColumn(TableContentType tableType, ColumnContentType columnType, ColumnTooltipHeader header, Function<T, ObservableValue<S>> property, Callback<TableColumn<T, S>, TableCell<T, S>> callback, ColumnType type, int columnIndex, boolean editable, boolean reorderable) {
		ColumnPropertiesManager columnPropertiesManager = ColumnPropertiesManager.getInstance();
		ColumnProperty columnProperty = columnPropertiesManager.getProperty(tableType, columnType);
		columnProperty.setColumnOrder(columnIndex);
		columnProperty.setDefaultColumnOrder(columnIndex);
		// Sets width properties within columnProperty
		ContentColumn<T, S> column = new ContentColumn<T, S>(columnProperty);
		final TableCellEvent<S> tableCellEvent = new TableCellEvent<S>(columnIndex);
		Label columnLabel = header.getLabel();
		columnLabel.getStyleClass().add("column-header-label");
		columnLabel.setMaxWidth(Double.MAX_VALUE);
		columnLabel.setTooltip(header.getTooltip());
		column.setUserData(type);
		column.setEditable(editable);
		column.setReorderable(reorderable);
		column.setGraphic(columnLabel);
		
		column.setCellValueFactory(new Callback<CellDataFeatures<T, S>, ObservableValue<S>>() {
			@Override
			public ObservableValue<S> call(CellDataFeatures<T, S> param) {
				ObservableValue<S> observableValue = property.apply(param.getValue());
				return observableValue;
			}
		});
		column.setCellFactory(callback);
		column.setOnEditCommit(tableCellEvent);
		return column;
	}
	
	public void refreshTable() {
		if (this.table != null)
			this.table.refresh();
	}

	void highlightTableRow(TableRow<T> row) {}
	
	void setTableRowHighlight(TableRow<T> row, TableRowHighlightRangeType type) {
		Color color       = TableRowHighlight.getInstance().getColor(type);
		Color darkerColor = color.darker();
		
		String rgbColor       = String.format(Locale.ENGLISH, "rgb(%.0f, %.0f, %.0f)", color.getRed()*255, color.getGreen()*255, color.getBlue()*255);
		String rgbDarkerColor = String.format(Locale.ENGLISH, "rgb(%.0f, %.0f, %.0f)", darkerColor.getRed()*255, darkerColor.getGreen()*255, darkerColor.getBlue()*255);
		
		row.setStyle(color != null && color != Color.TRANSPARENT ? String.format("-fx-background-color: %s;", row.getIndex() % 2 == 0 ? rgbColor : rgbDarkerColor) : "");
	}
	
	public abstract T getEmptyRow();

	abstract void setValue(T row, int columnIndex, Object oldValue, Object newValue);

	public TableView<T> getTable() {
		return this.table;
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
	
	private void setSortOrderSequence(TableView<T> table, List<? extends ColumnContentType> columnTypes) {
		List<TableColumn<T, ?>> sortedColumns = new ArrayList<TableColumn<T, ?>>();
		List<TableColumn<T, ?>> columns = new ArrayList<TableColumn<T, ?>>(table.getColumns());
		
		// Reset sort order
		if (columnTypes.isEmpty()) {
			for (TableColumn<T, ?> column : columns) {
				if (column != null && (column instanceof ContentColumn)) 
					((ContentColumn<T, ?>)column).getColumnProperty().setSortOrder(-1);
			}
		}
				
		int idx = 0;
		for (ColumnContentType columnType : columnTypes) {
			ListIterator<TableColumn<T, ?>> columnIterator = columns.listIterator();
			while (columnIterator.hasNext()) {
				TableColumn<T, ?> column = columnIterator.next();
				if (column instanceof ContentColumn) {
					ColumnContentType currentColumnType = ((ContentColumn<T,?>)column).getColumnProperty().getColumnContentType();
					if (currentColumnType == columnType) {
						sortedColumns.add(column);
						((ContentColumn<T,?>)column).getColumnProperty().setSortOrder(idx++);
						columnIterator.remove();
						break;
					}
				}
			}
		}
		
		table.getSortOrder().setAll(sortedColumns);
	}
	
	private void setColumnsOrderSequence(TableView<T> table, List<? extends ColumnContentType> columnTypes) {
		List<TableColumn<T, ?>> orderedColumns = new ArrayList<TableColumn<T, ?>>();
		List<TableColumn<T, ?>> columns = new ArrayList<TableColumn<T, ?>>(table.getColumns());

		// Reset column order
		if (columnTypes.isEmpty()) {
			for (TableColumn<T, ?> column : columns) {
				if (column != null && (column instanceof ContentColumn)) {
					int defaultValue = ((ContentColumn<T, ?>)column).getColumnProperty().getDefaultColumnOrder();
					((ContentColumn<T, ?>)column).getColumnProperty().setColumnOrder(defaultValue);
				}
			}
			columns.sort(new Comparator<TableColumn<T, ?>>() {
				@Override
				public int compare(TableColumn<T, ?> column1, TableColumn<T, ?> column2) {
					if (column1 == null || !(column1 instanceof ContentColumn))
						return -1;
					if (column2 == null || !(column2 instanceof ContentColumn))
						return +1;

					int defaultValue1 = ((ContentColumn<T, ?>)column1).getColumnProperty().getDefaultColumnOrder();
					int defaultValue2 = ((ContentColumn<T, ?>)column2).getColumnProperty().getDefaultColumnOrder();
					return defaultValue1 - defaultValue2;
				}
			});
		}
		
		int idx = 0;
		for (ColumnContentType columnType : columnTypes) {
			ListIterator<TableColumn<T, ?>> columnIterator = columns.listIterator();
			while (columnIterator.hasNext()) {
				TableColumn<T, ?> column = columnIterator.next();
				if (column instanceof ContentColumn) {
					ColumnContentType currentColumnType = ((ContentColumn<T,?>)column).getColumnProperty().getColumnContentType();
					if (currentColumnType == columnType) {
						orderedColumns.add(column);
						((ContentColumn<T,?>)column).getColumnProperty().setColumnOrder(idx++);
						columnIterator.remove();
						break;
					}
				}
			}
		}
		// Add columns, which are not part of columnTypes list, i.e. if meanwhile,
		// a new column is defined to the table not stored to the database)
		orderedColumns.addAll(columns);
		table.getColumns().setAll(orderedColumns);
	}
}