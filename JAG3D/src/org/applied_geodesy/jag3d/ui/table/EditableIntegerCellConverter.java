package org.applied_geodesy.jag3d.ui.table;

public class EditableIntegerCellConverter extends EditableCellConverter<Integer> {

	EditableIntegerCellConverter() {}

	@Override
	public String toEditorString(Integer value) {
		try {
			if (value == null)
				return "";
			return String.valueOf(value);
		}
		catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		}
		return "";
	}

	@Override
	public String toString(Integer value) {
		if (value == null)
			return "";
		return String.valueOf(value);
	}

	@Override
	public Integer fromString(String string) {
		if (string != null && !string.trim().isEmpty()) {
			try {
				return Integer.parseInt(string);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
