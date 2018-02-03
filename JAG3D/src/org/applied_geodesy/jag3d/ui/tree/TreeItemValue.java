package org.applied_geodesy.jag3d.ui.tree;

import org.applied_geodesy.jag3d.ui.tabpane.TabType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TreeItemValue {
	private TreeItemType type;
	private StringProperty name = new SimpleStringProperty();
	private BooleanProperty enable = new SimpleBooleanProperty(Boolean.TRUE);
	
	TreeItemValue(String name) {
		this(TreeItemType.UNSPECIFIC, name);
	}
	
	TreeItemValue(TreeItemType type, String name) {
		this.setItemType(type);
		this.setName(name);
	}
	
	public TreeItemType getItemType() {
		return this.type;
	}
	
	void setItemType(TreeItemType type) {
		this.type = type;
	}
	
	public StringProperty nameProperty() {
		return this.name;
	}
	
	public String getName() {
		return this.nameProperty().get();
	}
	
	public void setName(final String name) {
		this.nameProperty().set(name);
	}
	
	public TabType[] getTabTypes() {
		return null;
	}
	
	BooleanProperty enableProperty() {
		return this.enable;
	}
	
	public boolean isEnable() {
		return this.enableProperty().get();
	}
	
	void setEnable(final boolean enable) {
		this.enableProperty().set(enable);
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
}
