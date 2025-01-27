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

package org.applied_geodesy.juniform.ui.tree;

import java.util.Arrays;

import org.applied_geodesy.adjustment.geometry.Feature;
import org.applied_geodesy.adjustment.geometry.FeatureAdjustment;
import org.applied_geodesy.adjustment.geometry.FeatureChangeListener;
import org.applied_geodesy.adjustment.geometry.FeatureEvent;
import org.applied_geodesy.adjustment.geometry.FeatureType;
import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.PrimitiveType;
import org.applied_geodesy.adjustment.geometry.FeatureEvent.FeatureEventType;
import org.applied_geodesy.adjustment.geometry.curve.primitive.Curve;
import org.applied_geodesy.adjustment.geometry.surface.primitive.Surface;
import org.applied_geodesy.juniform.ui.dialog.GeometricPrimitiveDialog;
import org.applied_geodesy.juniform.ui.tabpane.UITabPaneBuilder;
import org.applied_geodesy.juniform.ui.i18n.I18N;

import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class UITreeBuilder implements FeatureChangeListener {
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
	
	private class GeometricPrimitiveListChangeListener implements ListChangeListener<GeometricPrimitive> {

		@Override
		public void onChanged(Change<? extends GeometricPrimitive> change) {
			while (change.next()) {
				if (change.wasAdded()) {
					for (GeometricPrimitive geometry : change.getAddedSubList()) {
						addItem(geometry);
					}
				}
				else if (change.wasRemoved()) {
					for (GeometricPrimitive geometry : change.getRemoved()) {
						removeItem(geometry);
					}
				}
			}
		}
	}
		
	private final I18N i18n = I18N.getInstance();
	private static UITreeBuilder treeBuilder = new UITreeBuilder();
	private UITabPaneBuilder tabPaneBuilder = UITabPaneBuilder.getInstance();
	private TreeView<TreeItemValue<?>> treeView;
	private GeometricPrimitiveListChangeListener geometricPrimitiveListChangeListener = new GeometricPrimitiveListChangeListener();
	private TreeListSelectionChangeListener treeListSelectionChangeListener = new TreeListSelectionChangeListener();
	private FeatureType featureType = FeatureType.SURFACE;
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
		this.treeView.setEditable(true);
		this.treeView.setCellFactory(new Callback<TreeView<TreeItemValue<?>>, TreeCell<TreeItemValue<?>>>() {
			@Override
			public TreeCell<TreeItemValue<?>> call(TreeView<TreeItemValue<?>> treeView) {
				return new EditableMenuTreeCell();
			}
		});

		AdjustmentTreeItemValue adjustmentTreeItemValue = new AdjustmentTreeItemValue(i18n.getString("UITreeBuilder.root", "JUniForm"));
		FeatureTreeItemValue featureTreeItemValue       = new FeatureTreeItemValue(i18n.getString("UITreeBuilder.feature.label", "Feature"));

		TreeItem<TreeItemValue<?>> rootItem    = this.createItem(adjustmentTreeItemValue);
		TreeItem<TreeItemValue<?>> featureItem = this.createItem(featureTreeItemValue);

		rootItem.getChildren().addAll(Arrays.asList(featureItem));
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
	
	private TreeItem<TreeItemValue<?>> searchTreeItem(TreeItem<TreeItemValue<?>> item, Object object) {
		if (item == null)
			return null;
		
		if (item.getValue().getObject() == object) 
			return item;
		
		TreeItem<TreeItemValue<?>> result = null;
		for(TreeItem<TreeItemValue<?>> child : item.getChildren()) {
			result = searchTreeItem(child, object);
			if(result != null) 
				return result;
		}

		return result;
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
	
	public GeometricPrimitive getSelectedGeometry() {
		TreeItem<TreeItemValue<?>> item = this.treeView.getSelectionModel().getSelectedItem();
		TreeItemValue<?> itemValue = item.getValue();
		
		if (itemValue instanceof CurveTreeItemValue) 
			return ((CurveTreeItemValue)item.getValue()).getObject();

		else if (itemValue instanceof SurfaceTreeItemValue) 
			return ((SurfaceTreeItemValue)item.getValue()).getObject();

		return null;
	}
	
	public FeatureAdjustment getFeatureAdjustment() {
		TreeItem<?> rootItem = this.treeView.getRoot();
		if (rootItem != null)
			return ((AdjustmentTreeItemValue)rootItem.getValue()).getObject();
		
		return null;
	}
	
	public void setFeatureType(FeatureType featureType) {
		this.setFeature(null);
		TreeItem<TreeItemValue<?>> featureItem = this.searchTreeItem(this.treeView.getRoot(), TreeItemType.FEATURE);
		if (featureItem != null)
			featureItem.getValue().setName(i18n.getString("UITreeBuilder.feature.label", "Feature"));

		this.getFeatureAdjustment().setFeature(null);
		this.featureType = featureType;
	}
	
	private void setFeature(Feature feature) {
		this.treeView.getSelectionModel().select(0);
		TreeItem<TreeItemValue<?>> featureItem = null;
		featureItem = this.searchTreeItem(this.treeView.getRoot(), TreeItemType.FEATURE);

		if (featureItem != null) {
			if (feature != null)
				this.featureType = feature.getFeatureType();
		
			// change name of feature tree item
			featureItem.getValue().setName(this.featureType == FeatureType.CURVE ? i18n.getString("UITreeBuilder.feature.curves", "Curves") : i18n.getString("UITreeBuilder.feature.surfaces", "Surfaces"));
			
			// cleaning tree
			for (TreeItem<TreeItemValue<?>> primitives : featureItem.getChildren())
				primitives.getChildren().clear();
			
			featureItem.getChildren().clear();
			
			// adding geometric primitives of feature
			if (feature != null) {
				for (GeometricPrimitive geometricPrimitive : feature.getGeometricPrimitives())
					this.addItem(geometricPrimitive);
			}
			else if (feature == null && this.getFeatureAdjustment().getFeature() != null) {
				this.getFeatureAdjustment().getFeature().getGeometricPrimitives().removeListener(this.geometricPrimitiveListChangeListener);	
			}
				
			this.treeView.getSelectionModel().select(featureItem);
		}
	}

	private TreeItem<TreeItemValue<?>> removeItem(GeometricPrimitive geometry) {
		TreeItem<TreeItemValue<?>> treeItem = this.searchTreeItem(this.treeView.getRoot(), geometry);
		if (treeItem == null)
			return null;
		TreeItem<TreeItemValue<?>> parent = treeItem.getParent();
		parent.getChildren().remove(treeItem);
		if (parent.getChildren().size() == 0) {
			TreeItem<TreeItemValue<?>> featureItem = parent.getParent();
			featureItem.getChildren().remove(parent);
		}
		return treeItem;
	}
	
	private TreeItem<TreeItemValue<?>> addItem(GeometricPrimitive geometry) {
		TreeItem<TreeItemValue<?>> featureItem = this.searchTreeItem(this.treeView.getRoot(), TreeItemType.FEATURE);
		TreeItem<TreeItemValue<?>> newItem = null;
		TreeItem<TreeItemValue<?>> parent  = null;
		
		if (featureItem == null)
			return null;

		if (geometry != null) {
			String geometryName = geometry.getName();
			if (geometryName == null || geometryName.isBlank())
				GeometricPrimitiveDialog.setDefaultName(geometry);
			
			TreeItemType treeItemType = null;
			String parentLabel = null;
		
			if (this.featureType == FeatureType.CURVE) {
				if (geometry.getPrimitiveType() == PrimitiveType.LINE) {
					treeItemType = TreeItemType.LINE;
					parentLabel = i18n.getString("UITreeBuilder.curves.lines", "Lines");
					
				}
				else if (geometry.getPrimitiveType() == PrimitiveType.CIRCLE) {
					treeItemType = TreeItemType.CIRCLE;
					parentLabel = i18n.getString("UITreeBuilder.curves.circles", "Circles");
				}
				
				else if (geometry.getPrimitiveType() == PrimitiveType.ELLIPSE) {
					treeItemType = TreeItemType.ELLIPSE;
					parentLabel = i18n.getString("UITreeBuilder.curves.ellipses", "Ellipses");
				}
				
				else if (geometry.getPrimitiveType() == PrimitiveType.QUADRATIC_CURVE) {
					treeItemType = TreeItemType.QUADRATIC_CURVE;
					parentLabel = i18n.getString("UITreeBuilder.curves.quadrics", "Quadratic curves");
				}

				else {
					throw new IllegalArgumentException("Error, unknown primitive type " + geometry.getPrimitiveType() + "!");
				}
					
				
				parent = this.searchTreeItem(featureItem, treeItemType);
				if (parent == null) {
					parent = this.createItem(new GeometricPrimitivesTreeItemValue(parentLabel, treeItemType));
					featureItem.getChildren().addAll(Arrays.asList(parent));
				}
				
				CurveTreeItemValue itemValue = new CurveTreeItemValue(geometry.getName(), treeItemType);
				itemValue.setObject((Curve)geometry);
				newItem = this.createItem(itemValue);

			}
			else if (this.featureType == FeatureType.SURFACE) {
				if (geometry.getPrimitiveType() == PrimitiveType.PLANE) {
					treeItemType = TreeItemType.PLANE;
					parentLabel = i18n.getString("UITreeBuilder.surfaces.planes", "Planes");
				}
				
				else if (geometry.getPrimitiveType() == PrimitiveType.SPHERE) {
					treeItemType = TreeItemType.SPHERE;
					parentLabel = i18n.getString("UITreeBuilder.surfaces.spheres", "Spheres");
				}
				
				else if (geometry.getPrimitiveType() == PrimitiveType.ELLIPSOID) {
					treeItemType = TreeItemType.ELLIPSOID;
					parentLabel = i18n.getString("UITreeBuilder.surfaces.ellipsoids", "Ellipsoids");
				}

				else if (geometry.getPrimitiveType() == PrimitiveType.CYLINDER) {
					treeItemType = TreeItemType.CYLINDER;
					parentLabel = i18n.getString("UITreeBuilder.surfaces.cylinders", "Cylinders");
				}
				
				else if (geometry.getPrimitiveType() == PrimitiveType.CONE) {
					treeItemType = TreeItemType.CONE;
					parentLabel = i18n.getString("UITreeBuilder.surfaces.cones", "Cones");
				}
				
				else if (geometry.getPrimitiveType() == PrimitiveType.PARABOLOID) {
					treeItemType = TreeItemType.PARABOLOID;
					parentLabel = i18n.getString("UITreeBuilder.surfaces.paraboloids", "Paraboloids");
				}
				
				else if (geometry.getPrimitiveType() == PrimitiveType.TORUS) {
					treeItemType = TreeItemType.TORUS;
					parentLabel = i18n.getString("UITreeBuilder.surfaces.tori", "Tori");
				}
				
				else if (geometry.getPrimitiveType() == PrimitiveType.QUADRATIC_SURFACE) {
					treeItemType = TreeItemType.QUADRATIC_SURFACE;
					parentLabel = i18n.getString("UITreeBuilder.surfaces.quadrics", "Quadratic surfaces");
				}
				
				else {
					throw new IllegalArgumentException("Error, unknown primitive type " + geometry.getPrimitiveType() + "!");
				}
				
				parent = this.searchTreeItem(featureItem, treeItemType);
				if (parent == null) {
					parent = this.createItem(new GeometricPrimitivesTreeItemValue(parentLabel, treeItemType));
					featureItem.getChildren().addAll(Arrays.asList(parent));
				}

				SurfaceTreeItemValue itemValue = new SurfaceTreeItemValue(geometry.getName(), treeItemType);
				itemValue.setObject((Surface)geometry);
				newItem = this.createItem(itemValue);
			}
			else {
				newItem = null;
			}
		}

		if (parent != null && newItem != null && parent.getChildren().add(newItem)) {		
			geometry.nameProperty().bindBidirectional(newItem.getValue().nameProperty());
			parent.setExpanded(true);
		}
		return newItem;
	}
	
	private void handleTreeSelections(TreeItem<TreeItemValue<?>> currentTreeItem) {
		// Save last option
		this.lastValidSelectedTreeItem = currentTreeItem;
		
		if (currentTreeItem == null)
			return;

		this.tabPaneBuilder.setTreeItemValue(currentTreeItem.getValue());
		
	}
	
	@Override
	public void featureChanged(FeatureEvent evt) {
		if (evt.getEventType() == FeatureEventType.FEATURE_ADDED) {
			this.setFeature(evt.getSource());
			// add listener to handle new feature
			evt.getSource().getGeometricPrimitives().addListener(this.geometricPrimitiveListChangeListener);
		}
		else if (evt.getEventType() == FeatureEventType.FEATURE_REMOVED) {
			// remove listener from old feature
			evt.getSource().getGeometricPrimitives().removeListener(this.geometricPrimitiveListChangeListener);	
		}
	}
}
