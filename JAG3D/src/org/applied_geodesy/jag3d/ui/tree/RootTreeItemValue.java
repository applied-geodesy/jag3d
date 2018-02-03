package org.applied_geodesy.jag3d.ui.tree;

import org.applied_geodesy.jag3d.ui.tabpane.TabType;

public class RootTreeItemValue extends TreeItemValue {
	
	RootTreeItemValue(String name) throws IllegalArgumentException {
		super(TreeItemType.ROOT, name);
	}

	@Override
	public TabType[] getTabTypes() {
		TabType[] tabTypes = new TabType[] {
				TabType.META_DATA,
				TabType.RESULT_DATA,
				TabType.GRAPHIC
		};
		return tabTypes;
	}
}
