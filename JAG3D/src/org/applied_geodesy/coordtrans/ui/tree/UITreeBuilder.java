/***********************************************************************
* Copyright by Michael Loesler, https://software.applied-geodesy.org   *
*                                                                      *
* This program is free software; you can redistribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation; either version 3 of the License, or    *
* at your option any later version.                                    *
*                                                                      *
* This program is distributed in the hope that it will be useful,      *
* but WITHOUT ANY WARRANTY; without even the implied warranty of       *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
* GNU General Public License for more details.                         *
*                                                                      *
* You should have received a copy of the GNU General Public License    *
* along with this program; if not, see <http://www.gnu.org/licenses/>  *
* or write to the                                                      *
* Free Software Foundation, Inc.,                                      *
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.            *
*                                                                      *
***********************************************************************/

package org.applied_geodesy.coordtrans.ui.tree;

import java.util.Arrays;

import org.applied_geodesy.adjustment.transformation.Transformation;
import org.applied_geodesy.adjustment.transformation.TransformationAdjustment;
import org.applied_geodesy.adjustment.transformation.TransformationChangeListener;
import org.applied_geodesy.adjustment.transformation.TransformationEvent;
import org.applied_geodesy.adjustment.transformation.TransformationEvent.TransformationEventType;
import org.applied_geodesy.adjustment.transformation.TransformationType;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.coordtrans.ui.tabpane.UITabPaneBuilder;

import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class UITreeBuilder implements TransformationChangeListener {
	
	private class TreeListSelectionChangeListener implements ListChangeListener<TreeItem<TreeItemValue<?>>> {
		@Override
		public void onChanged(Change<? extends TreeItem<TreeItemValue<?>>> change) {
			if (change != null && change.next() && treeView != null && treeView.getSelectionModel() != null && treeView.getSelectionModel().getSelectedItems().size() > 0) {				
				TreeItem<TreeItemValue<?>> treeItem = null;
				boolean hasValidTreeItem = false;
				
				try {
					if ((change.wasAdded() || change.wasReplaced()) && change.getAddedSubList() != null && !change.getAddedSubList().isEmpty()) {
						treeItem = change.getAddedSubList().get(0);
						hasValidTreeItem = true;
					}
				} catch (Exception e) {
					hasValidTreeItem = false;
					e.printStackTrace();
				}
				
				if (!hasValidTreeItem) {
					treeItem = treeView.getSelectionModel().getSelectedItem();
					int treeItemIndex = treeView.getSelectionModel().getSelectedIndex();
					if (treeItemIndex < 0 || treeItem == null || !treeView.getSelectionModel().isSelected(treeItemIndex))
						treeItem = treeView.getSelectionModel().getSelectedItems().get(0);
				}
				handleTreeSelections(treeItem);
			}
		}
	}
	
	private final I18N i18n = I18N.getInstance();
	private static UITreeBuilder treeBuilder = new UITreeBuilder();
	private UITabPaneBuilder tabPaneBuilder = UITabPaneBuilder.getInstance();
	private TreeView<TreeItemValue<?>> treeView;

	private TreeListSelectionChangeListener treeListSelectionChangeListener = new TreeListSelectionChangeListener();
	private TreeItem<TreeItemValue<?>> lastValidSelectedTreeItem = null;
	
	private UITreeBuilder() {}
		
	public static UITreeBuilder getInstance() {
		treeBuilder.init();
		return treeBuilder;
	}
	
	public TreeView<TreeItemValue<?>> getTree() {
		return this.treeView;
	}
	
	private void init() {
		if (this.treeView != null) 
			return;

		this.treeView = new TreeView<TreeItemValue<?>>();
		this.treeView.setEditable(false);

		AdjustmentTreeItemValue adjustmentTreeItemValue   = new AdjustmentTreeItemValue(i18n.getString("UITreeBuilder.root", "CoordTrans"));
		ObservationTreeItemValue observationTreeItemValue = new ObservationTreeItemValue(i18n.getString("UITreeBuilder.observation.label", "Homologous Points"));
		TransformationParameterTreeItemValue parameterTreeItemValue = new TransformationParameterTreeItemValue(i18n.getString("UITreeBuilder.parameter.label", "Parameters"));
		TransformationTreeItemValue transformationTreeItemValue     = new TransformationTreeItemValue(i18n.getString("UITreeBuilder.transformation.label", "Transformation"));

		TreeItem<TreeItemValue<?>> rootItem           = this.createItem(adjustmentTreeItemValue);
		TreeItem<TreeItemValue<?>> observationItem    = this.createItem(observationTreeItemValue);
		TreeItem<TreeItemValue<?>> parameterItem      = this.createItem(parameterTreeItemValue);
		TreeItem<TreeItemValue<?>> transformationItem = this.createItem(transformationTreeItemValue);

		rootItem.getChildren().addAll(Arrays.asList(observationItem, parameterItem, transformationItem));
		this.treeView.setRoot(rootItem);

		this.treeView.setShowRoot(false);

		this.treeView.getSelectionModel().getSelectedItems().addListener(this.treeListSelectionChangeListener);
		this.treeView.getSelectionModel().select(0);
	}
	
	public void handleTreeSelections() {
		this.handleTreeSelections(this.lastValidSelectedTreeItem);
	}
	
	private TreeItem<TreeItemValue<?>> createItem(TreeItemValue<?> value) {
        TreeItem<TreeItemValue<?>> item = new TreeItem<TreeItemValue<?>>(value);
        item.setExpanded(true);
        return item;
	}
		
	private TreeItem<TreeItemValue<?>> searchTreeItem(TreeItem<TreeItemValue<?>> item, TreeItemType treeItemType) {
		if (item == null)
			return null;

		if (item.getValue().getTreeItemType() == treeItemType) 
			return item;

		TreeItem<TreeItemValue<?>> result = null;
		for(TreeItem<TreeItemValue<?>> child : item.getChildren()) {
			result = searchTreeItem(child, treeItemType);
			if(result != null) 
				return result;
		}

		return result;
	}
	
	public TransformationAdjustment getTransformationAdjustment() {
		TreeItem<?> rootItem = this.treeView.getRoot();
		if (rootItem != null)
			return ((AdjustmentTreeItemValue)rootItem.getValue()).getObject();
		
		return null;
	}
	
	public void setTransformationType(TransformationType transformationType) {
		this.setTransformation(null);
		TreeItem<TreeItemValue<?>> observationItem = this.searchTreeItem(this.treeView.getRoot(), TreeItemType.OBSERVATIPON);
		if (observationItem != null)
			observationItem.getValue().setName(i18n.getString("UITreeBuilder.transformation.type.height", "Height Transformation"));

		this.getTransformationAdjustment().setTransformation(null);
	}
	
	private void setTransformation(Transformation transformation) {
		this.treeView.getSelectionModel().select(0);
		TreeItem<TreeItemValue<?>> observationItem = null;
		observationItem = this.searchTreeItem(this.treeView.getRoot(), TreeItemType.OBSERVATIPON);
		
		// TODO clean if null?

		if (observationItem != null && transformation != null)
			this.treeView.getSelectionModel().select(observationItem);
	}
	
	
	private void handleTreeSelections(TreeItem<TreeItemValue<?>> currentTreeItem) {
		// Save last option
		this.lastValidSelectedTreeItem = currentTreeItem;
		
		if (currentTreeItem == null)
			return;

		this.tabPaneBuilder.setTreeItemValue(currentTreeItem.getValue());
	}

	@Override
	public void transformationChanged(TransformationEvent evt) {
		if (evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_ADDED) {
			this.setTransformation(evt.getSource());
		}
		else if (evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_REMOVED) {
			this.setTransformation(null);
		}
	}
}

