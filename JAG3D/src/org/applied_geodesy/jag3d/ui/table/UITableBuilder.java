package org.applied_geodesy.jag3d.ui.table;

import java.util.function.Function;

import org.applied_geodesy.util.FormatterOptions;
import org.applied_geodesy.util.i18.I18N;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Callback;

public abstract class UITableBuilder<T>  {
	class TableCellChangeListener<S> implements ChangeListener<S> {
		private final int columnIndex;
		private final T rowData; 
		TableCellChangeListener(int columnIndex, T rowData) {
			this.columnIndex = columnIndex;
			this.rowData = rowData;
		}
		
		@Override
		public void changed(ObservableValue<? extends S> observable, S oldValue, S newValue) {
			if (rowData != null) {
				setValue(rowData, columnIndex, -1, oldValue, newValue);
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
//				event.getTablePosition().getRow()
				//int column = event.getTablePosition().getColumn();
        		//setValue(event.getRowValue(), column, event.getNewValue());
				setValue(event.getRowValue(), this.columnIndex, event.getTablePosition().getRow(), event.getOldValue(), event.getNewValue());
			}
		}
	}
	
	static FormatterOptions options = FormatterOptions.getInstance();
	static I18N i18n = I18N.getInstance();
	TableView<T> table = this.createTable();
	
	UITableBuilder() {}
	
	TableView<T> createTable() {
		this.table = new TableView<T>();
		ObservableList<T> tableModel = FXCollections.observableArrayList();
		this.table.setItems(tableModel);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table.setTableMenuButtonVisible(false);
		this.table.setPlaceholder(new Text(i18n.getString("UITableBuilder.emptytable", "No content in table.")));
		this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tableModel.add(this.getEmptyRow());
		
//		tableModel.addListener(new ListChangeListener<T>() {
//
//			@Override
//			public void onChanged(Change<? extends T> change) {
//				// TODO Auto-generated method stub
//				change.next();
//				System.out.println(tableModel.size());
//			}
//			
//		});
//		table.getColumns().addListener(
//                new ListChangeListener<TableColumn>() {
//
//					@Override
//					public void onChanged(Change<? extends TableColumn> change) {
//
//	                       change.next();
//
////	                        System.out.println("old list");
////	                        System.out.println(change.getRemoved());
//
//	                       System.out.println (change.wasPermutated()+"   "+change.wasUpdated()+"   "+change.wasReplaced()) ;
//	                        System.out.println(change.wasReplaced());
//	                        
//	                        System.out.println("new list");
//	                        System.out.println(change.getList());
////	                       }
//						
//					}
//
//
//
//                });
		
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
			public TableCell<T, Boolean> call(TableColumn<T, Boolean> column) {
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
				final TableRow<T> row = new TableRow<T>();  
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
					else if (table.getItems() != null && table.getItems().size() == table.getSelectionModel().getSelectedIndex() + 1) {
						table.getItems().add(getEmptyRow());
						//						table.scrollTo(emptyRow);
						//						table.getSelectionModel().clearAndSelect(table.getItems().size() - 1);
						//						table.getSelectionModel().select(emptyRow);

					}
				}
			}
		});
	}
		
	<S>TableColumn<T, S> getColumn(ColumnTooltipHeader header, Function<T, ObservableValue<S>> property, Callback<TableColumn<T, S>, TableCell<T, S>> callback, ColumnType type, int columnIndex, boolean editable) {
		final TableCellEvent<S> tableCellEvent = new TableCellEvent<S>(columnIndex);
		TableColumn<T, S> column = new TableColumn<T, S>();
		Label columnLabel = header.getLabel();
	    columnLabel.getStyleClass().add("column-header-label");
	    columnLabel.setMaxWidth(Double.MAX_VALUE);
	    columnLabel.setTooltip(header.getTooltip());

	    column.setUserData(type);
	    column.setEditable(editable);
	    column.setGraphic(columnLabel);
	    column.setMinWidth(75);
	    column.setPrefWidth(125);
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

	public abstract T getEmptyRow();

	abstract void setValue(T row, int columnIndex, int rowIndex, Object oldValue, Object newValue);

	public TableView<T> getTable() {
		return this.table;
	}
}
