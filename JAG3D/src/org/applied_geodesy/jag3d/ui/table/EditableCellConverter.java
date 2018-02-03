package org.applied_geodesy.jag3d.ui.table;

import javafx.scene.control.TableCell;
import javafx.util.StringConverter;

public abstract class EditableCellConverter<S> extends StringConverter<S> {
	TableCell<?, S> tableCell = null;
	
	public abstract String toEditorString(S object);
	
	void setEditableCell(EditableCell<?, S> tableCell) {
		this.tableCell = tableCell;
	}
}
