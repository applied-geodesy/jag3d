package org.applied_geodesy.jag3d.ui.resultpane;

import org.applied_geodesy.util.i18.I18N;

import javafx.scene.Node;
import javafx.scene.control.ListCell;

public class GlobalResultTypeListCell extends ListCell<Node> {
	private static I18N i18n = I18N.getInstance();
	
	protected void updateItem(Node item, boolean empty){
		super.updateItem(item, empty);
	
		this.setGraphic(null);
		this.setText(null);
		
		if(item != null && !empty && item.getUserData() instanceof GlobalResultType) {
			this.setText(this.getText((GlobalResultType)item.getUserData()));
		}
	}
	
	private String getText(GlobalResultType type) {
		switch(type) {
		case TEST_STATISTIC:
			return i18n.getString("GlobalResultTableListCell.test_statistic.label", "Test statistics");
		case VARIANCE_COMPONENT:
			return i18n.getString("GlobalResultTableListCell.variance_component.label", "Variance components estimation");
		}
		return null;
	}
}