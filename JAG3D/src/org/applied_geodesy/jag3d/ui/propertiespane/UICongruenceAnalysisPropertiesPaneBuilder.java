package org.applied_geodesy.jag3d.ui.propertiespane;

import java.util.Map;

import org.applied_geodesy.jag3d.ui.tree.TreeItemType;

import javafx.collections.FXCollections;

public class UICongruenceAnalysisPropertiesPaneBuilder {
	private static UICongruenceAnalysisPropertiesPaneBuilder propertyPaneBuilder = new UICongruenceAnalysisPropertiesPaneBuilder();
	private Map<TreeItemType, UICongruenceAnalysisPropertiesPane> propertiesPanes = FXCollections.observableHashMap();

	private UICongruenceAnalysisPropertiesPaneBuilder() { }

	public static UICongruenceAnalysisPropertiesPaneBuilder getInstance() {
		return propertyPaneBuilder;
	}

	public UICongruenceAnalysisPropertiesPane getCongruenceAnalysisPropertiesPane(TreeItemType type) {
		if (!this.propertiesPanes.containsKey(type))
			this.propertiesPanes.put(type, new UICongruenceAnalysisPropertiesPane(type));
		UICongruenceAnalysisPropertiesPane propertiesPane = this.propertiesPanes.get(type);
		return propertiesPane;
	}
}