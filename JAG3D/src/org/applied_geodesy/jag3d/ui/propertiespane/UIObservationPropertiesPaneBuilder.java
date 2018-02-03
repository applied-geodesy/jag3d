package org.applied_geodesy.jag3d.ui.propertiespane;

import java.util.Map;

import org.applied_geodesy.jag3d.ui.tree.TreeItemType;

import javafx.collections.FXCollections;

public class UIObservationPropertiesPaneBuilder {
	private static UIObservationPropertiesPaneBuilder propertyPaneBuilder = new UIObservationPropertiesPaneBuilder();
	private Map<TreeItemType, UIObservationPropertiesPane> propertiesPanes = FXCollections.observableHashMap();

	private UIObservationPropertiesPaneBuilder() { }

	public static UIObservationPropertiesPaneBuilder getInstance() {
		return propertyPaneBuilder;
	}

	public UIObservationPropertiesPane getObservationPropertiesPane(TreeItemType type) {
		if (!this.propertiesPanes.containsKey(type))
			this.propertiesPanes.put(type, new UIObservationPropertiesPane(type));
		UIObservationPropertiesPane propertiesPane = this.propertiesPanes.get(type);
		return propertiesPane;
	}
}