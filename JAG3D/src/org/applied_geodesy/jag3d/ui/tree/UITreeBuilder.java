package org.applied_geodesy.jag3d.ui.tree;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dialog.OptionDialog;
import org.applied_geodesy.jag3d.ui.tabpane.UITabPaneBuilder;
import org.applied_geodesy.util.i18.I18N;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class UITreeBuilder {

	private class TreeCheckBoxChangeListener implements ChangeListener<Boolean> {
		private final TreeItemValue treeItemValue;

		private TreeCheckBoxChangeListener(TreeItemValue treeItemValue) {
			this.treeItemValue = treeItemValue;
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (!isIgnoreEvent())
				saveGroup(this.treeItemValue);
		}
	}

	private class TreeItemNameChangeListener implements ChangeListener<String> {
		private final TreeItemValue treeItemValue;

		private TreeItemNameChangeListener(TreeItemValue treeItemValue) {
			this.treeItemValue = treeItemValue;
		}

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			if (isIgnoreEvent() || newValue == null || newValue.trim().isEmpty())
				this.treeItemValue.setName(oldValue);
			else if (!isIgnoreEvent())
				saveGroup(this.treeItemValue);
		}
	}

	private class TreeItemExpandingChangeListener implements ChangeListener<Boolean> {
		private final TreeItem<TreeItemValue> treeItem;

		private TreeItemExpandingChangeListener(TreeItem<TreeItemValue> treeItem) {
			this.treeItem = treeItem;
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (newValue && !ignoreExpanding) {
				selectChildren(treeItem);
			}
		}
	}

	private class TreeSelectionChangeListener implements ChangeListener<TreeItem<TreeItemValue>> {
		@Override
		public void changed(ObservableValue<? extends TreeItem<TreeItemValue>> observable, TreeItem<TreeItemValue> oldValue, TreeItem<TreeItemValue> newValue) {
			handleTreeSelections(newValue);
		}
	}

	private static UITreeBuilder treeBuilder = new UITreeBuilder();
	private static I18N i18n = I18N.getInstance();
	private UITabPaneBuilder tabPaneBuilder = UITabPaneBuilder.getInstance();
	private ObservableMap<TreeItemType, CheckBoxTreeItem<TreeItemValue>> directoryItemMap = FXCollections.observableHashMap();
	private TreeView<TreeItemValue> treeView;
	private boolean ignoreExpanding = false;
	private BooleanProperty ignoreEvent = new SimpleBooleanProperty(Boolean.FALSE);
	private TreeSelectionChangeListener treeSelectionChangeListener = new TreeSelectionChangeListener();
	private UITreeBuilder() {}

	public static UITreeBuilder getInstance() {
		if (treeBuilder.treeView == null)
			treeBuilder.init();
		return treeBuilder;
	}

	public TreeView<TreeItemValue> getTree() {
		return this.treeView;
	}

	private void init() {
		// TreeItemType.ROOT
		TreeItem<TreeItemValue> rootItem = new TreeItem<TreeItemValue> (new RootTreeItemValue(i18n.getString("UITreeBuiler.root", "JAG3D-Projekt")));

		TreeItem<TreeItemValue> referencePointItem           = new TreeItem<TreeItemValue> (new TreeItemValue(i18n.getString("UITreeBuiler.directory.referencepoints", "Reference points")));
		CheckBoxTreeItem<TreeItemValue> referencePoint1DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.REFERENCE_POINT_1D_DIRECTORY, i18n.getString("UITreeBuiler.directory.referencepoints.1d", "Reference points 1D")));
		CheckBoxTreeItem<TreeItemValue> referencePoint2DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.REFERENCE_POINT_2D_DIRECTORY, i18n.getString("UITreeBuiler.directory.referencepoints.2d", "Reference points 2D")));
		CheckBoxTreeItem<TreeItemValue> referencePoint3DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.REFERENCE_POINT_3D_DIRECTORY, i18n.getString("UITreeBuiler.directory.referencepoints.3d", "Reference points 3D")));
		referencePointItem.getChildren().addAll(Arrays.asList(referencePoint1DItem, referencePoint2DItem, referencePoint3DItem));

		TreeItem<TreeItemValue> stochasticPointItem           = new TreeItem<TreeItemValue> (new TreeItemValue(i18n.getString("UITreeBuiler.directory.stochasticpoints", "Stochastic points")));
		CheckBoxTreeItem<TreeItemValue> stochasticPoint1DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.STOCHASTIC_POINT_1D_DIRECTORY, i18n.getString("UITreeBuiler.directory.stochasticpoints.1d", "Stochastic points 1D")));
		CheckBoxTreeItem<TreeItemValue> stochasticPoint2DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.STOCHASTIC_POINT_2D_DIRECTORY, i18n.getString("UITreeBuiler.directory.stochasticpoints.2d", "Stochastic points 2D")));
		CheckBoxTreeItem<TreeItemValue> stochasticPoint3DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.STOCHASTIC_POINT_3D_DIRECTORY, i18n.getString("UITreeBuiler.directory.stochasticpoints.3d", "Stochastic points 3D")));
		stochasticPointItem.getChildren().addAll(Arrays.asList(stochasticPoint1DItem, stochasticPoint2DItem, stochasticPoint3DItem));

		TreeItem<TreeItemValue> datumPointItem           = new TreeItem<TreeItemValue> (new TreeItemValue(i18n.getString("UITreeBuiler.directory.datumpoints", "Datum points")));
		CheckBoxTreeItem<TreeItemValue> datumPoint1DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.DATUM_POINT_1D_DIRECTORY, i18n.getString("UITreeBuiler.directory.datumpoints.1d", "Datum points 1D")));
		CheckBoxTreeItem<TreeItemValue> datumPoint2DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.DATUM_POINT_2D_DIRECTORY, i18n.getString("UITreeBuiler.directory.datumpoints.2d", "Datum points 2D")));
		CheckBoxTreeItem<TreeItemValue> datumPoint3DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.DATUM_POINT_3D_DIRECTORY, i18n.getString("UITreeBuiler.directory.datumpoints.3d", "Datum points 3D")));
		datumPointItem.getChildren().addAll(Arrays.asList(datumPoint1DItem, datumPoint2DItem, datumPoint3DItem));

		TreeItem<TreeItemValue> newPointItem           = new TreeItem<TreeItemValue> (new TreeItemValue(i18n.getString("UITreeBuiler.directory.newpoints", "New points")));
		CheckBoxTreeItem<TreeItemValue> newPoint1DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.NEW_POINT_1D_DIRECTORY, i18n.getString("UITreeBuiler.directory.newpoints.1d", "New points 1D")));
		CheckBoxTreeItem<TreeItemValue> newPoint2DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.NEW_POINT_2D_DIRECTORY, i18n.getString("UITreeBuiler.directory.newpoints.2d", "New points 2D")));
		CheckBoxTreeItem<TreeItemValue> newPoint3DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.NEW_POINT_3D_DIRECTORY, i18n.getString("UITreeBuiler.directory.newpoints.3d", "New points 3D")));
		newPointItem.getChildren().addAll(Arrays.asList(newPoint1DItem, newPoint2DItem, newPoint3DItem));

		
		TreeItem<TreeItemValue> congruenceAnalysisItem           = new TreeItem<TreeItemValue> (new TreeItemValue(i18n.getString("UITreeBuiler.directory.congruenceanalysis", "Congruence analysis")));
		CheckBoxTreeItem<TreeItemValue> congruenceAnalysis1DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.CONGRUENCE_ANALYSIS_1D_DIRECTORY, i18n.getString("UITreeBuiler.directory.congruenceanalysis.1d", "Point nexus 1D")));
		CheckBoxTreeItem<TreeItemValue> congruenceAnalysis2DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.CONGRUENCE_ANALYSIS_2D_DIRECTORY, i18n.getString("UITreeBuiler.directory.congruenceanalysis.2d", "Point nexus 2D")));
		CheckBoxTreeItem<TreeItemValue> congruenceAnalysis3DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.CONGRUENCE_ANALYSIS_3D_DIRECTORY, i18n.getString("UITreeBuiler.directory.congruenceanalysis.3d", "Point nexus 3D")));
		congruenceAnalysisItem.getChildren().addAll(Arrays.asList(congruenceAnalysis1DItem, congruenceAnalysis2DItem, congruenceAnalysis3DItem));
		

		TreeItem<TreeItemValue> terrestrialObservationItem = new TreeItem<TreeItemValue> (new TreeItemValue(i18n.getString("UITreeBuiler.directory.terrestrialobservations", "Terrestrial Observations")));
		TreeItem<TreeItemValue> levelingObservationItem    = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.LEVELING_DIRECTORY, i18n.getString("UITreeBuiler.directory.terrestrialobservations.leveling", "Levelings")));
		TreeItem<TreeItemValue> directionObservationItem   = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.DIRECTION_DIRECTORY, i18n.getString("UITreeBuiler.directory.terrestrialobservations.direction", "Directions")));
		TreeItem<TreeItemValue> distance2dObservationItem  = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.HORIZONTAL_DISTANCE_DIRECTORY, i18n.getString("UITreeBuiler.directory.terrestrialobservations.distance2d", "Horizontal distances")));
		TreeItem<TreeItemValue> distance3dObservationItem  = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.SLOPE_DISTANCE_DIRECTORY, i18n.getString("UITreeBuiler.directory.terrestrialobservations.distance3d", "Slope distances")));
		TreeItem<TreeItemValue> zenithObservationItem      = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.ZENITH_ANGLE_DIRECTORY, i18n.getString("UITreeBuiler.directory.terrestrialobservations.zenith", "Zenith angles")));
		terrestrialObservationItem.getChildren().addAll(Arrays.asList(levelingObservationItem, directionObservationItem, distance2dObservationItem, distance3dObservationItem, zenithObservationItem));

		TreeItem<TreeItemValue> gnssBaselineItem   = new TreeItem<TreeItemValue> (new TreeItemValue(i18n.getString("UITreeBuiler.directory.gnssobservations", "GNSS Baselines")));
		CheckBoxTreeItem<TreeItemValue> gnssBaseline1DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.GNSS_1D_DIRECTORY, i18n.getString("UITreeBuiler.directory.gnssobservations.1d", "GNSS baselines 1D")));
		CheckBoxTreeItem<TreeItemValue> gnssBaseline2DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.GNSS_2D_DIRECTORY, i18n.getString("UITreeBuiler.directory.gnssobservations.2d", "GNSS baselines 2D")));
		CheckBoxTreeItem<TreeItemValue> gnssBaseline3DItem = new CheckBoxTreeItem<TreeItemValue> (new TreeItemValue(TreeItemType.GNSS_3D_DIRECTORY, i18n.getString("UITreeBuiler.directory.gnssobservations.3d", "GNSS baselines 3D")));
		gnssBaselineItem.getChildren().addAll(Arrays.asList(gnssBaseline1DItem, gnssBaseline2DItem, gnssBaseline3DItem));

		for (TreeItem<TreeItemValue> item : referencePointItem.getChildren()) {
			item.expandedProperty().addListener(new TreeItemExpandingChangeListener(item));
			this.directoryItemMap.put(item.getValue().getItemType(), (CheckBoxTreeItem<TreeItemValue>)item);
		}
		for (TreeItem<TreeItemValue> item : stochasticPointItem.getChildren()) {
			item.expandedProperty().addListener(new TreeItemExpandingChangeListener(item));
			this.directoryItemMap.put(item.getValue().getItemType(), (CheckBoxTreeItem<TreeItemValue>)item);
		}
		for (TreeItem<TreeItemValue> item : datumPointItem.getChildren()) {
			item.expandedProperty().addListener(new TreeItemExpandingChangeListener(item));
			this.directoryItemMap.put(item.getValue().getItemType(), (CheckBoxTreeItem<TreeItemValue>)item);
		}
		for (TreeItem<TreeItemValue> item : newPointItem.getChildren()) {
			item.expandedProperty().addListener(new TreeItemExpandingChangeListener(item));
			this.directoryItemMap.put(item.getValue().getItemType(), (CheckBoxTreeItem<TreeItemValue>)item);
		}
		for (TreeItem<TreeItemValue> item : congruenceAnalysisItem.getChildren()) {
			item.expandedProperty().addListener(new TreeItemExpandingChangeListener(item));
			this.directoryItemMap.put(item.getValue().getItemType(), (CheckBoxTreeItem<TreeItemValue>)item);
		}
		for (TreeItem<TreeItemValue> item : terrestrialObservationItem.getChildren()) {
			item.expandedProperty().addListener(new TreeItemExpandingChangeListener(item));
			this.directoryItemMap.put(item.getValue().getItemType(), (CheckBoxTreeItem<TreeItemValue>)item);
		}
		for (TreeItem<TreeItemValue> item : gnssBaselineItem.getChildren()) {
			item.expandedProperty().addListener(new TreeItemExpandingChangeListener(item));
			this.directoryItemMap.put(item.getValue().getItemType(), (CheckBoxTreeItem<TreeItemValue>)item);
		}

		// Add first Level to root item
		rootItem.getChildren().addAll(Arrays.asList(referencePointItem, stochasticPointItem, datumPointItem, newPointItem, congruenceAnalysisItem, terrestrialObservationItem, gnssBaselineItem)); 
		rootItem.setExpanded(true);

		this.treeView = new TreeView<TreeItemValue>(rootItem);
		this.treeView.setEditable(true);
		this.treeView.setCellFactory(new Callback<TreeView<TreeItemValue>, TreeCell<TreeItemValue>>() {
			@Override
			public TreeCell<TreeItemValue> call(TreeView<TreeItemValue> treeView) {
				EditableMenuCheckBoxTreeCell editableMenuCheckBoxTreeCell = new EditableMenuCheckBoxTreeCell();
				editableMenuCheckBoxTreeCell.ignoreEventProperty().bindBidirectional(ignoreEvent);
				return new EditableMenuCheckBoxTreeCell();
			}
		});
		this.treeView.getSelectionModel().select(rootItem);
		this.treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.treeView.getSelectionModel().selectedItemProperty().addListener(this.treeSelectionChangeListener);
	}

	public void removeAllItems() {
		for (CheckBoxTreeItem<TreeItemValue> item : this.directoryItemMap.values()) {
			item.getChildren().clear();
			item.setSelected(false);
		}
		//this.treeView.getSelectionModel().selectFirst();
	}

	public TreeItem<TreeItemValue> addItem(TreeItemType parentType) {
		return this.addItem(parentType, null);
	}
	
	public TreeItem<TreeItemValue> addItem(TreeItemType parentType, boolean select) {
		return this.addItem(parentType, -1, null, Boolean.TRUE, select);
	}
	
	public TreeItem<TreeItemValue> addItem(TreeItemType parentType, String name) {
		return this.addItem(parentType, -1, name, Boolean.TRUE, Boolean.TRUE);
	}

	public TreeItem<TreeItemValue> addItem(TreeItemType parentType, int id, String name, boolean enable, boolean select) {
		TreeItemType itemType = null;
		if (!this.directoryItemMap.containsKey(parentType) || (itemType = TreeItemType.getLeafByDirectoryType(parentType)) == null) {
			System.err.println(this.getClass().getSimpleName() + " : Error, unsupported parent tree node type " + parentType);
			return null;
		}

		final TreeItemValue itemValue;

		if (TreeItemType.isPointTypeLeaf(itemType))
			itemValue = new PointTreeItemValue(id, itemType, name == null || name.trim().isEmpty() ? i18n.getString("UITreeBuiler.directory.points", "Points") : name);

		else if (TreeItemType.isObservationTypeLeaf(itemType) || TreeItemType.isGNSSObservationTypeLeaf(itemType))
			itemValue = new ObservationTreeItemValue(id, itemType, name == null || name.trim().isEmpty() ? i18n.getString("UITreeBuiler.directory.observations", "Observations") : name);

		else if (TreeItemType.isCongruenceAnalysisTypeLeaf(itemType))
			itemValue = new CongruenceAnalysisTreeItemValue(id, itemType, name == null || name.trim().isEmpty() ? i18n.getString("UITreeBuiler.directory.congruenceanalysis", "Point nexus") : name);
			
		else	
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " NOT IMPLEMENTED YET!");

		CheckBoxTreeItem<TreeItemValue> newItem = new CheckBoxTreeItem<TreeItemValue>(itemValue);
		itemValue.setEnable(enable);

		directoryItemMap.get(parentType).getChildren().add(newItem);
		treeView.getSelectionModel().clearSelection();
		expand(newItem, true);
		if (select)
			treeView.getSelectionModel().select(newItem);
		newItem.selectedProperty().bindBidirectional(itemValue.enableProperty());
		newItem.selectedProperty().addListener(new TreeCheckBoxChangeListener(newItem.getValue()));
		newItem.getValue().nameProperty().addListener(new TreeItemNameChangeListener(newItem.getValue()));

		return newItem;
	}
	
	public void removeItems(List<TreeItem<TreeItemValue>> items) {
		if (items != null && !items.isEmpty()) {
			TreeItemType parentItemType = null;
			for (TreeItem<TreeItemValue> item : items) {
				this.removeItem(item);

				if (parentItemType == null && item.getValue() != null) {
					parentItemType = TreeItemType.getDirectoryByLeafType(item.getValue().getItemType());
				}
			}
		}
	}
	
	public void removeItem(TreeItem<TreeItemValue> treeItem) {
		if (treeItem == null || treeItem.getValue() == null || treeItem.getValue().getItemType() == null)
			return;

		TreeItemValue itemValue = treeItem.getValue();
		TreeItemType itemType = itemValue.getItemType();
		
		if (!TreeItemType.isPointTypeLeaf(itemType) && 
				!TreeItemType.isObservationTypeLeaf(itemType) && 
				!TreeItemType.isGNSSObservationTypeLeaf(itemType) &&
				!TreeItemType.isCongruenceAnalysisTypeLeaf(itemType))
			return;
		
		TreeItemType parentType = TreeItemType.getDirectoryByLeafType(itemType);
		
		if (parentType == null || !this.directoryItemMap.containsKey(parentType))
			return;

		if (this.removeGroup(itemValue)) {
			setIgnoreEvent(true);
			CheckBoxTreeItem<TreeItemValue> parent = directoryItemMap.get(parentType); 
			parent.getChildren().remove(treeItem);
			updateSelectionStageOfParentNode(parent);
			setIgnoreEvent(false);
		}
	}

	public void moveItems(TreeItemType newItemType, List<TreeItem<TreeItemValue>> selectedItems) {
		TreeItemType newParentType = TreeItemType.getDirectoryByLeafType(newItemType);
		if (TreeItemType.isPointTypeLeaf(newItemType) && this.directoryItemMap.containsKey(newParentType) && selectedItems != null && selectedItems.size() > 0) {
			CheckBoxTreeItem<TreeItemValue> newParent = directoryItemMap.get(newParentType);
			TreeItem<TreeItemValue> lastItem = null;
			this.setIgnoreEvent(true);
			for (TreeItem<TreeItemValue> selectedItem : selectedItems) {
				if (selectedItem != null && selectedItem.isLeaf() && selectedItem.getValue() != null && selectedItem.getValue() instanceof PointTreeItemValue) {
					PointTreeItemValue itemValue = (PointTreeItemValue)selectedItem.getValue();
					TreeItemType oldItemType     = itemValue.getItemType();
					TreeItemType oldParentType   = TreeItemType.getDirectoryByLeafType(oldItemType);
					if (this.directoryItemMap.containsKey(oldParentType)) {
						itemValue.setItemType(newItemType);
						if (!this.saveGroup(itemValue)) {
							itemValue.setItemType(oldItemType);
							continue;
						}
						lastItem = selectedItem;
						CheckBoxTreeItem<TreeItemValue> oldParent = directoryItemMap.get(oldParentType);
						// Remove Item
						oldParent.getChildren().remove(selectedItem);
						updateSelectionStageOfParentNode(oldParent);
						// add Item
						newParent.getChildren().add(selectedItem);
						//updateSelectionStageOfParentNode(newParent);
						expand(selectedItem, true);
					}
				}
			}

			if (lastItem != null) {
				TreeItem<TreeItemValue> lastSelectedItem = lastItem;
				updateSelectionStageOfParentNode(newParent);
				setIgnoreEvent(false);
				treeView.getSelectionModel().select(lastSelectedItem);
			}
		}
	}
	
	public void addEmptyGroup(TreeItemType parentType) {
		if (this.directoryItemMap.containsKey(parentType)) {
			TreeItem<TreeItemValue> newMenuItem = this.addItem(parentType);
			if (!this.saveGroup(newMenuItem.getValue())) {
				TreeItem<TreeItemValue> parentItem = directoryItemMap.get(parentType);
				parentItem.getChildren().remove(newMenuItem);
			}
		}		
	}
	
//	public List<TreeItem<TreeItemValue>> getChildrenByParent(TreeItemType parentType) {
//		if (this.directoryItemMap.containsKey(parentType)) {
//			return this.directoryItemMap.get(parentType).getChildren();
//		}
//		return null;
//	}
	
	private void updateSelectionStageOfParentNode(CheckBoxTreeItem<TreeItemValue> parentNode) {
		if (parentNode.isLeaf()) {
			parentNode.setIndeterminate(false);
			parentNode.setSelected(false);
		}
		else if (parentNode.getChildren().get(0) instanceof CheckBoxTreeItem && parentNode.getChildren().get(0).isLeaf()){
			CheckBoxTreeItem<TreeItemValue> firstChild = (CheckBoxTreeItem<TreeItemValue>)parentNode.getChildren().get(0);
			firstChild.setSelected(!firstChild.isSelected());
			firstChild.setSelected(!firstChild.isSelected());
		}
	}

	private void expand(TreeItem<TreeItemValue> item, boolean expand) {
		try {
			this.ignoreExpanding = true;
			if (item != null) {
				if (!item.isExpanded())
					item.setExpanded(expand);
				this.expand(item.getParent(), expand);
			}
		}
		finally {
			this.ignoreExpanding = false;
		}
	}

	private void handleTreeSelections(TreeItem<TreeItemValue> currentTreeItem) {
		if (currentTreeItem == null)
			return;

		TreeItemType currentItemType = currentTreeItem.getValue().getItemType();
		boolean isValidSelection = true;
		ObservableList<TreeItem<TreeItemValue>> treeItems = treeView.getSelectionModel().getSelectedItems();
		for (TreeItem<TreeItemValue> item : treeItems) {
			if (item == null || item.getValue() == null || item.getValue().getItemType() != currentItemType) {
				isValidSelection = false;
				break;
			}
		}

		if (!isValidSelection) {
			treeView.getSelectionModel().clearSelection();
			treeView.getSelectionModel().select(currentTreeItem);
		}
		else if (currentTreeItem != null && currentTreeItem.getValue() != null) {
			TreeItemValue itemValue = currentTreeItem.getValue();
			if (!currentTreeItem.isLeaf()) {
				switch(itemValue.getItemType()) {
				case ROOT:
					this.load(itemValue, treeItems);
					break;
					
				case REFERENCE_POINT_1D_DIRECTORY:
				case STOCHASTIC_POINT_1D_DIRECTORY:
				case DATUM_POINT_1D_DIRECTORY:
				case NEW_POINT_1D_DIRECTORY:

				case REFERENCE_POINT_2D_DIRECTORY:
				case STOCHASTIC_POINT_2D_DIRECTORY:
				case DATUM_POINT_2D_DIRECTORY:
				case NEW_POINT_2D_DIRECTORY:

				case REFERENCE_POINT_3D_DIRECTORY:
				case STOCHASTIC_POINT_3D_DIRECTORY:
				case DATUM_POINT_3D_DIRECTORY:
				case NEW_POINT_3D_DIRECTORY:

				case LEVELING_DIRECTORY:
				case DIRECTION_DIRECTORY:
				case HORIZONTAL_DISTANCE_DIRECTORY:
				case SLOPE_DISTANCE_DIRECTORY:
				case ZENITH_ANGLE_DIRECTORY:

				case GNSS_1D_DIRECTORY:
				case GNSS_2D_DIRECTORY:
				case GNSS_3D_DIRECTORY:

				case CONGRUENCE_ANALYSIS_1D_DIRECTORY:
				case CONGRUENCE_ANALYSIS_2D_DIRECTORY:
				case CONGRUENCE_ANALYSIS_3D_DIRECTORY:

					this.selectChildren(currentTreeItem);

					break;

//				default:
//					break;
				}
			}
			else if (currentTreeItem.isLeaf()) {
				this.load(itemValue, treeItems);
			}
		}
	}

	private boolean saveGroup(TreeItemValue treeItemValue) {
		try {
			TreeItemType type = treeItemValue.getItemType();
			if (TreeItemType.isPointTypeLeaf(type) && treeItemValue instanceof PointTreeItemValue) 
				SQLManager.getInstance().saveGroup((PointTreeItemValue)treeItemValue);

			else if ((TreeItemType.isGNSSObservationTypeLeaf(type) || TreeItemType.isObservationTypeLeaf(type)) && treeItemValue instanceof ObservationTreeItemValue) 
				SQLManager.getInstance().saveGroup((ObservationTreeItemValue)treeItemValue);

			else if ((TreeItemType.isCongruenceAnalysisTypeLeaf(type)) && treeItemValue instanceof CongruenceAnalysisTreeItemValue) 
				SQLManager.getInstance().saveGroup((CongruenceAnalysisTreeItemValue)treeItemValue);

			else {
				System.err.println(this.getClass().getSimpleName() + " : Error, item has no saveable properties " + treeItemValue);
				return false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			OptionDialog.showThrowableDialog (
					i18n.getString("UITreeBuiler.message.error.sql.save.title", "SQL-Error"),
					i18n.getString("UITreeBuiler.message.error.sql.save.header", "Could not save properties of group."), 
					i18n.getString("UITreeBuiler.message.error.sql.save.title", "An exception occured while update database."), 
					e
					);
			return false;
		}
		return true;
	}
	
	private boolean removeGroup(TreeItemValue treeItemValue) {
		try {
			TreeItemType type = treeItemValue.getItemType();
			if (TreeItemType.isPointTypeLeaf(type) || TreeItemType.isGNSSObservationTypeLeaf(type) || TreeItemType.isObservationTypeLeaf(type)) {
				SQLManager.getInstance().removeGroup(treeItemValue);
			}
			else {
				System.err.println(this.getClass().getSimpleName() + " : Error, item has no saveable properties " + treeItemValue);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			OptionDialog.showThrowableDialog (
					i18n.getString("UITreeBuiler.message.error.sql.remove.title", "SQL-Error"),
					i18n.getString("UITreeBuiler.message.error.sql.remove.header", "Could not remove table."), 
					i18n.getString("UITreeBuiler.message.error.sql.remove.title", "An exception occured while update database."), 
					e
					);
			return false;
		}
		return true;
	}

	private void load(TreeItemValue itemValue, ObservableList<TreeItem<TreeItemValue>> treeItems) {
		try {
			TreeItemValue[] itemValues = new TreeItemValue[treeItems == null ? 0 : treeItems.size()];
			if (treeItems != null) {
				for (int i=0; i<treeItems.size(); i++) {
					itemValues[i] = treeItems.get(i).getValue();
				}
			}
			SQLManager.getInstance().loadData(itemValue, itemValues);
			this.tabPaneBuilder.setTreeItemValue(itemValue);
		} catch (SQLException e) {
			e.printStackTrace();
			OptionDialog.showThrowableDialog (
					i18n.getString("UITreeBuiler.message.error.sql.load.title", "SQL-Error"),
					i18n.getString("UITreeBuiler.message.error.sql.load.header", "Could not load table."), 
					i18n.getString("UITreeBuiler.message.error.sql.load.title", "An exception occured while update database."), 
					e
					);
		}
	}

	private void selectChildren(TreeItem<TreeItemValue> parent) {
		this.treeView.getSelectionModel().selectedItemProperty().removeListener(this.treeSelectionChangeListener);
		if (!parent.isLeaf() && parent.isExpanded()) {
			treeView.getSelectionModel().clearSelection();
			ObservableList<TreeItem<TreeItemValue>> children = parent.getChildren();
			TreeItem<TreeItemValue> lastSelectedChild = null;
			for (TreeItem<TreeItemValue> child : children) {
				treeView.getSelectionModel().select(child);
				lastSelectedChild = child;
			}
			//int currentTreeItemIndex = treeView.getSelectionModel().getSelectedIndex();
			//treeView.getSelectionModel().selectRange(currentTreeItemIndex + 1, currentTreeItemIndex + 1 + children.size());
			if (lastSelectedChild != null)
				this.load(lastSelectedChild.getValue(), parent.getChildren());
		}
		this.treeView.getSelectionModel().selectedItemProperty().addListener(this.treeSelectionChangeListener);
	}

	final BooleanProperty ignoreEventProperty() {
		return this.ignoreEvent;
	}

	final boolean isIgnoreEvent() {
		return this.ignoreEventProperty().get();
	}

	final void setIgnoreEvent(final boolean ignoreEvent) {
		this.ignoreEventProperty().set(ignoreEvent);
	}

}
