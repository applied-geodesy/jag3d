package org.applied_geodesy.jag3d.ui.table;

public class EditableStringCellConverter extends EditableCellConverter<String> {

	@Override
	public String toEditorString(String object) {
		return toString(object);
	}

	@Override
	public String toString(String object) {
		return object;
	}

	@Override
	public String fromString(String string) {
		return string;
	}
}
