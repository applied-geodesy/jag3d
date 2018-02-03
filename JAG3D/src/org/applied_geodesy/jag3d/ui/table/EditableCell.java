package org.applied_geodesy.jag3d.ui.table;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public class EditableCell<T, S> extends TableCell<T, S> {

	private final TextField textField = new TextField();
	private final EditableCellConverter<S> converter;

	EditableCell(EditableCellConverter<S> converter) {
		this.converter = converter;
		this.converter.setEditableCell(this);
		
		this.init();
	}
	
	private void init() {
		this.itemProperty().addListener(new ChangeListener<S>() {
			@Override
			public void changed(ObservableValue<? extends S> observable, S oldValue, S newValue) {
				setText(newValue == null ? null : converter.toString(newValue));
			}
		});

		this.setGraphic(this.textField);
		this.setContentDisplay(ContentDisplay.TEXT_ONLY);

		this.textField.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				commitEdit(converter.fromString(textField.getText()));
			}
		});

		this.textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue)
					commitEdit(converter.fromString(textField.getText()));
			}
		});

		this.textField.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ESCAPE) {
					textField.setText(converter.toEditorString(getItem()));
					cancelEdit();
					event.consume();
				} else if (event.getCode() == KeyCode.RIGHT) {
					getTableView().getSelectionModel().selectRightCell();
					event.consume();
				} else if (event.getCode() == KeyCode.LEFT) {
					getTableView().getSelectionModel().selectLeftCell();
					event.consume();
				} else if (event.getCode() == KeyCode.UP) {
					getTableView().getSelectionModel().selectAboveCell();
					event.consume();
				} else if (event.getCode() == KeyCode.DOWN) {
					getTableView().getSelectionModel().selectBelowCell();
					event.consume();
				}
			}
		});
	}

	@Override
	public void startEdit() {
		super.startEdit();
		this.textField.setText(this.converter.toEditorString(getItem()));
		this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		this.textField.requestFocus(); 
		this.textField.selectAll();
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		this.setContentDisplay(ContentDisplay.TEXT_ONLY);
	}

	@Override
	public void commitEdit(S item) {
		if (item != null && getItem() != null && !this.isEditing() && !item.equals(getItem())) {
			TableView<T> table = getTableView();
			if (table != null) {
				TableColumn<T, S> column = getTableColumn();
				CellEditEvent<T, S> event = new CellEditEvent<>(table, new TablePosition<T,S>(table, getIndex(), column), TableColumn.editCommitEvent(), item);
				Event.fireEvent(column, event);
			}
		}
		super.commitEdit(item);
		this.setContentDisplay(ContentDisplay.TEXT_ONLY);
	}
}