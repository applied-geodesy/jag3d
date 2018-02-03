package org.applied_geodesy.jag3d.ui.propertiespane;

import java.util.Map;

import org.applied_geodesy.jag3d.ui.tree.TreeItemType;

import javafx.collections.FXCollections;


public class UIPointPropertiesPaneBuilder {
	private static UIPointPropertiesPaneBuilder propertyPaneBuilder = new UIPointPropertiesPaneBuilder();
	private Map<TreeItemType, UIPointPropertiesPane> propertiesPanes = FXCollections.observableHashMap();

	private UIPointPropertiesPaneBuilder() { }

	public static UIPointPropertiesPaneBuilder getInstance() {
		return propertyPaneBuilder;
	}

	public UIPointPropertiesPane getPointPropertiesPane(TreeItemType type) {
		if (!this.propertiesPanes.containsKey(type))
			this.propertiesPanes.put(type, new UIPointPropertiesPane(type));
		UIPointPropertiesPane propertiesPane = this.propertiesPanes.get(type);
		return propertiesPane;
	}
}
