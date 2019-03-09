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

package org.applied_geodesy.jag3d.ui.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.applied_geodesy.adjustment.network.PointType;
import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateChangedListener;
import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateEvent;
import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dialog.OptionDialog;
import org.applied_geodesy.jag3d.ui.dialog.SearchAndReplaceDialog;
import org.applied_geodesy.jag3d.ui.dnd.CongruenceAnalysisRowDnD;
import org.applied_geodesy.jag3d.ui.dnd.GNSSObservationRowDnD;
import org.applied_geodesy.jag3d.ui.dnd.GroupTreeItemDnD;
import org.applied_geodesy.jag3d.ui.dnd.PointRowDnD;
import org.applied_geodesy.jag3d.ui.dnd.TerrestrialObservationRowDnD;
import org.applied_geodesy.jag3d.ui.io.DefaultFileChooser;
import org.applied_geodesy.jag3d.ui.table.UICongruenceAnalysisTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIGNSSObservationTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIPointTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UITerrestrialObservationTableBuilder;
import org.applied_geodesy.jag3d.ui.table.row.CongruenceAnalysisRow;
import org.applied_geodesy.jag3d.ui.table.row.GNSSObservationRow;
import org.applied_geodesy.jag3d.ui.table.row.PointRow;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.tabpane.TabType;
import org.applied_geodesy.jag3d.ui.tabpane.UITabPaneBuilder;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;

public class EditableMenuCheckBoxTreeCell extends CheckBoxTreeCell<TreeItemValue> {
	public static final DataFormat TREE_PARENT_ITEM_TYPE_DATA_FORMAT = new DataFormat(TreeItemType.class.toString());
	public static final DataFormat TREE_ITEM_TYPE_DATA_FORMAT = new DataFormat(EditableMenuCheckBoxTreeCell.class.toString());
	public static final DataFormat GROUP_ID_DATA_FORMAT = new DataFormat(TreeItemValue.class.toString());
	public static final DataFormat DIMENSION_DATA_FORMAT = new DataFormat(Integer.class.toString());
	public static final DataFormat TERRESTRIAL_OBSERVATION_ROWS_DATA_FORMAT = new DataFormat(TerrestrialObservationRowDnD.class.toString());
	public static final DataFormat GNSS_OBSERVATION_ROWS_DATA_FORMAT = new DataFormat(GNSSObservationRowDnD.class.toString());
	public static final DataFormat POINT_ROWS_DATA_FORMAT = new DataFormat(PointRowDnD.class.toString());
	public static final DataFormat CONGRUENCE_ANALYSIS_ROWS_DATA_FORMAT = new DataFormat(CongruenceAnalysisRowDnD.class.toString());
	private static final DataFormat TREE_ITEMS_DATA_FORMAT = new DataFormat(GroupTreeItemDnD.class.toString());
	
	private I18N i18n = I18N.getInstance();
	private BooleanProperty ignoreEvent = new SimpleBooleanProperty(Boolean.FALSE);
	private ContextMenu contextMenu;
	private TextField textField;
	private CheckBox checkBox;

	private enum ContextMenuType {
		ADD,
		REMOVE,
		EXPORT,
		SEARCH_AND_REPLACE,
		CHANGE_TO_REFERENCE_POINT_GROUP,
		CHANGE_TO_STOCHASTIC_POINT_GROUP,
		CHANGE_TO_DATUM_POINT_GROUP,
		CHANGE_TO_NEW_POINT_GROUP;
	}
	
	private class DatabaseStateChangedListener implements ProjectDatabaseStateChangedListener {
		@Override
		public void projectDatabaseStateChanged(ProjectDatabaseStateEvent evt) {
			if (contextMenu == null || contextMenu.getItems().isEmpty())
				return;
			
			boolean disable = evt.getEventType() != ProjectDatabaseStateType.OPENED;
			List<MenuItem> items = contextMenu.getItems();
			for (MenuItem item : items)
				this.disableItem(item, disable);
		}
		
		private void disableItem(MenuItem item, boolean disable) {
			if (item instanceof Menu)
				this.disableItem((Menu)item, disable);
			else 
				item.setDisable(disable);
		}
	}

	private class DropMouseEventHandler implements EventHandler<MouseEvent>  {

		@Override
		public void handle(MouseEvent event) {
			if (event.getEventType() == MouseEvent.DRAG_DETECTED) {
				List<TreeItem<TreeItemValue>> draggedItems = new ArrayList<TreeItem<TreeItemValue>>(getTreeView().getSelectionModel().getSelectedItems());

				TreeItemType itemType = getItem() != null ? getItem().getItemType() : null;
				
				if (itemType != null) {
					List<GroupTreeItemDnD> groupItemsDnD = new ArrayList<GroupTreeItemDnD>(draggedItems.size());
					for (TreeItem<TreeItemValue> draggedItem : draggedItems) {
						if (draggedItem.getValue() == null || draggedItem.getValue().getItemType() != itemType)
							return;

						GroupTreeItemDnD itemDnD = GroupTreeItemDnD.fromTreeItem(draggedItem);
						if (itemDnD == null) 
							return;					

						groupItemsDnD.add(itemDnD);
					}

					if (groupItemsDnD.size() > 0) {
						Dragboard db = startDragAndDrop(TransferMode.MOVE);

						TreeItemType parentType = groupItemsDnD.get(0).getParentType();
						int groupId             = groupItemsDnD.get(0).getGroupId();
						int dimension           = groupItemsDnD.get(0).getDimension();

						ClipboardContent content = new ClipboardContent();
						content.put(TREE_PARENT_ITEM_TYPE_DATA_FORMAT, parentType);
						content.put(TREE_ITEM_TYPE_DATA_FORMAT, itemType);
						content.put(GROUP_ID_DATA_FORMAT, groupId);
						content.put(DIMENSION_DATA_FORMAT, dimension);
						content.put(TREE_ITEMS_DATA_FORMAT, groupItemsDnD);
						
						db.setContent(content);
						event.consume();
					}
				}
			}
		}
	}
	
	private class DropEventHandler implements EventHandler<DragEvent> {
		@Override
		public void handle(DragEvent event) {
			if (event.getEventType() == DragEvent.DRAG_OVER) {
				if (acceptTransfer(event))
					event.acceptTransferModes(TransferMode.MOVE);
				//event.consume();
			}
			else if (event.getEventType() == DragEvent.DRAG_DROPPED) {

				Dragboard db = event.getDragboard();
				boolean success = false;
				if (acceptTransfer(event)) {
					success = true;
					
					if (db.hasContent(TREE_ITEMS_DATA_FORMAT)) {
						TreeItem<TreeItemValue> droppedOverItem = getTreeItem();
						boolean isDirectoryNode = db.hasContent(TREE_PARENT_ITEM_TYPE_DATA_FORMAT) && droppedOverItem != null && droppedOverItem.getValue() != null && droppedOverItem.getValue().getItemType() == db.getContent(TREE_PARENT_ITEM_TYPE_DATA_FORMAT); 
						
						TreeItem<TreeItemValue> droppedOverParentItem = isDirectoryNode ? droppedOverItem : droppedOverItem.getParent();
						List<TreeItem<TreeItemValue>> childItems = droppedOverParentItem.getChildren();
						
						List<?> groupItemsDnD = (List<?>)db.getContent(TREE_ITEMS_DATA_FORMAT);
						List<TreeItem<TreeItemValue>> draggedItems = new ArrayList<TreeItem<TreeItemValue>>(groupItemsDnD.size());
						
						for (int i = 0; i < groupItemsDnD.size(); i++) {
							if (groupItemsDnD.get(i) == null || !(groupItemsDnD.get(i) instanceof GroupTreeItemDnD))
								continue;
							GroupTreeItemDnD itemDnD = (GroupTreeItemDnD)groupItemsDnD.get(i);
							draggedItems.add(childItems.get(itemDnD.getIndex()));
						}
						if (draggedItems.size() > 0) {
							childItems.removeAll(draggedItems);
							int indexInParent = isDirectoryNode ? 0 : childItems.indexOf(droppedOverItem) + 1;
							childItems.addAll(indexInParent, draggedItems);

							// Save new order in item values
							for (int orderId = 0;  orderId < childItems.size(); orderId++) {
								TreeItem<TreeItemValue> item = childItems.get(orderId);
								TreeItemValue itemValue = item.getValue();
								
								if (!(itemValue instanceof Sortable) || itemValue.getItemType() == null)
									continue;

								((Sortable)itemValue).setOrderId(orderId);

								try {
									if (TreeItemType.isObservationTypeLeaf(itemValue.getItemType())) 
										SQLManager.getInstance().saveGroup((ObservationTreeItemValue)itemValue);

									else if (TreeItemType.isGNSSObservationTypeLeaf(itemValue.getItemType())) 
										SQLManager.getInstance().saveGroup((ObservationTreeItemValue)itemValue);

									else if (TreeItemType.isPointTypeLeaf(itemValue.getItemType())) 
										SQLManager.getInstance().saveGroup((PointTreeItemValue)itemValue);

									else if (TreeItemType.isCongruenceAnalysisTypeLeaf(itemValue.getItemType())) 
										SQLManager.getInstance().saveGroup((CongruenceAnalysisTreeItemValue)itemValue);

								} catch (Exception e) {
									e.printStackTrace();
									OptionDialog.showThrowableDialog (
											i18n.getString("EditableMenuCheckBoxTreeCell.message.error.save_dropped_tree_item.exception.title",   "Unexpected SQL-Error"),
											i18n.getString("EditableMenuCheckBoxTreeCell.message.error.save_dropped_tree_item.exception.header",  "Error, could not drop selected item to tree menu."),
											i18n.getString("EditableMenuCheckBoxTreeCell.message.error.save_dropped_tree_item.exception.message", "An exception has occurred during database transaction."),
											e);
									success = false;
									break;
								}
							}
							
							Platform.runLater(new Runnable() {
							    public void run() {
							    	if (draggedItems.size() > 0) {
							    		getTreeView().getSelectionModel().clearSelection();
							    		getTreeView().getSelectionModel().select(draggedItems.get(0));
							    	}
							    }
							});
						}
					}
					else {
						List<?> droppedRows = null;
						int targetGroupId = -1;
						
						if (TreeItemType.isObservationTypeLeaf(getItem().getItemType()) && db.hasContent(TERRESTRIAL_OBSERVATION_ROWS_DATA_FORMAT)) {
							targetGroupId = ((ObservationTreeItemValue)getItem()).getGroupId();
							droppedRows = (List<?>)db.getContent(TERRESTRIAL_OBSERVATION_ROWS_DATA_FORMAT);
						}
						else if (TreeItemType.isGNSSObservationTypeLeaf(getItem().getItemType()) && db.hasContent(GNSS_OBSERVATION_ROWS_DATA_FORMAT)) {
							targetGroupId = ((ObservationTreeItemValue)getItem()).getGroupId();
							droppedRows = (List<?>)db.getContent(GNSS_OBSERVATION_ROWS_DATA_FORMAT);
						}
						else if (TreeItemType.isPointTypeLeaf(getItem().getItemType()) && db.hasContent(POINT_ROWS_DATA_FORMAT)) {
							targetGroupId = ((PointTreeItemValue)getItem()).getGroupId();
							droppedRows = (List<?>)db.getContent(POINT_ROWS_DATA_FORMAT);
						}
						else if (TreeItemType.isCongruenceAnalysisTypeLeaf(getItem().getItemType()) && db.hasContent(CONGRUENCE_ANALYSIS_ROWS_DATA_FORMAT)) {
							targetGroupId = ((CongruenceAnalysisTreeItemValue)getItem()).getGroupId();
							droppedRows = (List<?>)db.getContent(CONGRUENCE_ANALYSIS_ROWS_DATA_FORMAT);
						}

						if (droppedRows != null && targetGroupId >= 0) {
							for (int i=0; i<droppedRows.size(); i++) {
								if (droppedRows.get(i) == null)
									continue;
								try {
									if (droppedRows.get(i) instanceof TerrestrialObservationRowDnD) {
										TerrestrialObservationRowDnD rowDnD = (TerrestrialObservationRowDnD)droppedRows.get(i);
										TerrestrialObservationRow row = rowDnD.toTerrestrialObservationRow();
										row.setGroupId(targetGroupId);
										SQLManager.getInstance().saveItem(row);
									}
									else if (droppedRows.get(i) instanceof GNSSObservationRowDnD) {
										GNSSObservationRowDnD rowDnD = (GNSSObservationRowDnD)droppedRows.get(i);
										GNSSObservationRow row = rowDnD.toGNSSObservationRow();
										row.setGroupId(targetGroupId);
										SQLManager.getInstance().saveItem(row);
									}
									else if (droppedRows.get(i) instanceof PointRowDnD) {
										PointRowDnD rowDnD = (PointRowDnD)droppedRows.get(i);
										PointRow row = rowDnD.toPointRow();
										row.setGroupId(targetGroupId);
										SQLManager.getInstance().saveItem(row);
									}
									else if (droppedRows.get(i) instanceof CongruenceAnalysisRowDnD) {
										CongruenceAnalysisRowDnD rowDnD = (CongruenceAnalysisRowDnD)droppedRows.get(i);
										CongruenceAnalysisRow row = rowDnD.toCongruenceAnalysisRow();
										row.setGroupId(targetGroupId);
										SQLManager.getInstance().saveItem(row);
									}
								} catch (Exception e) {
									e.printStackTrace();
									OptionDialog.showThrowableDialog (
											i18n.getString("EditableMenuCheckBoxTreeCell.message.error.save_dropped_table_row.exception.title",   "Unexpected SQL-Error"),
											i18n.getString("EditableMenuCheckBoxTreeCell.message.error.save_dropped_table_row.exception.header",  "Error, could not drop selected row to new table."),
											i18n.getString("EditableMenuCheckBoxTreeCell.message.error.save_dropped_table_row.exception.message", "An exception has occurred during database transaction."),
											e);
									success = false;
									break;
								}
							}
						}
						final TreeItem<TreeItemValue> targetTreeItem = getTreeItem();
						Platform.runLater(new Runnable() {
						    public void run() {
						    	if (targetTreeItem != null) {
									getTreeView().getSelectionModel().clearSelection();
									getTreeView().getSelectionModel().select(targetTreeItem);
								}
						    }
						});
					}
				}
				event.setDropCompleted(success);
			}
			event.consume();
		}

		private boolean acceptTransfer(DragEvent event) {
			Dragboard db = event.getDragboard();
			TreeItemValue itemValue = getItem();
			TreeItemType itemType = itemValue != null && itemValue.getItemType() != null ? itemValue.getItemType() : null;
			Set<Integer> groupItemIdsDnD = new HashSet<Integer>(0);

			if (db.hasContent(TREE_ITEMS_DATA_FORMAT)) {
				List<?> groupItemsDnD = (List<?>)db.getContent(TREE_ITEMS_DATA_FORMAT);
				groupItemIdsDnD = new HashSet<Integer>(groupItemsDnD.size());
				
				for (int i = 0; i < groupItemsDnD.size(); i++) {
					if (groupItemsDnD.get(i) == null || !(groupItemsDnD.get(i) instanceof GroupTreeItemDnD))
						continue;
					GroupTreeItemDnD itemDnD = (GroupTreeItemDnD)groupItemsDnD.get(i);
					groupItemIdsDnD.add(itemDnD.getGroupId());
				}
			}

			return (itemType != null && 
					(event.getGestureSource() instanceof TableView || event.getGestureSource() instanceof EditableMenuCheckBoxTreeCell) && 
					db.hasContent(TREE_ITEM_TYPE_DATA_FORMAT) &&
					db.hasContent(GROUP_ID_DATA_FORMAT) &&
					db.hasContent(DIMENSION_DATA_FORMAT) &&

					(
							// DnD is it the parent node
							db.hasContent(TREE_ITEMS_DATA_FORMAT) && db.hasContent(TREE_PARENT_ITEM_TYPE_DATA_FORMAT) && itemType == db.getContent(TREE_PARENT_ITEM_TYPE_DATA_FORMAT)
							||
							
							// Terrestrial observations
							((db.hasContent(TERRESTRIAL_OBSERVATION_ROWS_DATA_FORMAT) || db.hasContent(TREE_ITEMS_DATA_FORMAT)) && 
									TreeItemType.isObservationTypeLeaf(itemType) &&
									itemType == db.getContent(TREE_ITEM_TYPE_DATA_FORMAT) &&
									itemValue instanceof ObservationTreeItemValue &&
									((ObservationTreeItemValue)itemValue).getGroupId() != (Integer)db.getContent(GROUP_ID_DATA_FORMAT)) &&
									!groupItemIdsDnD.contains(((ObservationTreeItemValue)itemValue).getGroupId())

							||

							// GNSS observations
							((db.hasContent(GNSS_OBSERVATION_ROWS_DATA_FORMAT) || db.hasContent(TREE_ITEMS_DATA_FORMAT)) && 
									TreeItemType.isGNSSObservationTypeLeaf(itemType) &&
									itemType == db.getContent(TREE_ITEM_TYPE_DATA_FORMAT) &&
									itemValue instanceof ObservationTreeItemValue &&
									((ObservationTreeItemValue)itemValue).getDimension() == (Integer)db.getContent(DIMENSION_DATA_FORMAT) &&
									((ObservationTreeItemValue)itemValue).getGroupId() != (Integer)db.getContent(GROUP_ID_DATA_FORMAT)) &&
									!groupItemIdsDnD.contains(((ObservationTreeItemValue)itemValue).getGroupId())

							||

							// points
							((db.hasContent(POINT_ROWS_DATA_FORMAT) || db.hasContent(TREE_ITEMS_DATA_FORMAT)) && 
									TreeItemType.isPointTypeLeaf(itemType) && 
									TreeItemType.isPointTypeLeaf((TreeItemType)db.getContent(TREE_ITEM_TYPE_DATA_FORMAT)) && 
									itemValue instanceof PointTreeItemValue &&
									((PointTreeItemValue)itemValue).getDimension() == (Integer)db.getContent(DIMENSION_DATA_FORMAT) &&
									((PointTreeItemValue)itemValue).getGroupId() != (Integer)db.getContent(GROUP_ID_DATA_FORMAT)) &&
									!groupItemIdsDnD.contains(((PointTreeItemValue)itemValue).getGroupId())
					
							||

							// congruence analysis
							((db.hasContent(CONGRUENCE_ANALYSIS_ROWS_DATA_FORMAT) || db.hasContent(TREE_ITEMS_DATA_FORMAT)) && 
									TreeItemType.isCongruenceAnalysisTypeLeaf(itemType) && 
									itemType == db.getContent(TREE_ITEM_TYPE_DATA_FORMAT) &&
									itemValue instanceof CongruenceAnalysisTreeItemValue &&
									((CongruenceAnalysisTreeItemValue)itemValue).getDimension() == (Integer)db.getContent(DIMENSION_DATA_FORMAT) &&
									((CongruenceAnalysisTreeItemValue)itemValue).getGroupId() != (Integer)db.getContent(GROUP_ID_DATA_FORMAT)) &&
									!groupItemIdsDnD.contains(((CongruenceAnalysisTreeItemValue)itemValue).getGroupId())
							)
					);
		}
	}

	private class PointGroupChangeListener implements ChangeListener<Toggle> {

		@Override
		public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {

			if (!isIgnoreEvent() && newValue != null 
					&& newValue.getUserData() instanceof ContextMenuType
					&& getTreeItem() != null && getTreeItem().getValue() != null 
					&& getTreeItem().getValue() instanceof PointTreeItemValue) {// newValue instanceof RadioMenuItem &&
				ContextMenuType menuItemType = (ContextMenuType)newValue.getUserData();
				PointTreeItemValue itemValue = (PointTreeItemValue)getTreeItem().getValue();

				int dimension = itemValue.getDimension();
				PointType pointType = itemValue.getPointType();

				switch(menuItemType) {
				case CHANGE_TO_REFERENCE_POINT_GROUP:
					if (pointType == PointType.REFERENCE_POINT)
						return;

					if (dimension == 1)
						changePointGroupType(TreeItemType.REFERENCE_POINT_1D_LEAF);
					else if (dimension == 2)
						changePointGroupType(TreeItemType.REFERENCE_POINT_2D_LEAF);
					else if (dimension == 3)
						changePointGroupType(TreeItemType.REFERENCE_POINT_3D_LEAF);
					break;
				case CHANGE_TO_STOCHASTIC_POINT_GROUP:
					if (pointType == PointType.STOCHASTIC_POINT)
						return;

					if (dimension == 1)
						changePointGroupType(TreeItemType.STOCHASTIC_POINT_1D_LEAF);
					else if (dimension == 2)
						changePointGroupType(TreeItemType.STOCHASTIC_POINT_2D_LEAF);
					else if (dimension == 3)
						changePointGroupType(TreeItemType.STOCHASTIC_POINT_3D_LEAF);
					break;
				case CHANGE_TO_DATUM_POINT_GROUP:
					if (pointType == PointType.DATUM_POINT)
						return;

					if (dimension == 1)
						changePointGroupType(TreeItemType.DATUM_POINT_1D_LEAF);
					else if (dimension == 2)
						changePointGroupType(TreeItemType.DATUM_POINT_2D_LEAF);
					else if (dimension == 3)
						changePointGroupType(TreeItemType.DATUM_POINT_3D_LEAF);
					break;
				case CHANGE_TO_NEW_POINT_GROUP:
					if (pointType == PointType.NEW_POINT)
						return;

					if (dimension == 1)
						changePointGroupType(TreeItemType.NEW_POINT_1D_LEAF);
					else if (dimension == 2)
						changePointGroupType(TreeItemType.NEW_POINT_2D_LEAF);
					else if (dimension == 3)
						changePointGroupType(TreeItemType.NEW_POINT_3D_LEAF);
					break;

				default:
					System.err.println(EditableMenuCheckBoxTreeCell.class.getSimpleName() + " : Error, unsupported context menu item type " + menuItemType);
					break;
				}
			}
		}
	}

	private class ContextMenuEventHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			if (!isIgnoreEvent() && event.getSource() instanceof MenuItem 
					&& ((MenuItem)event.getSource()).getUserData() instanceof ContextMenuType
					&& getTreeItem() != null && getTreeItem().getValue() != null) {
				ContextMenuType contextMenuType = (ContextMenuType)((MenuItem)event.getSource()).getUserData();
				TreeItem<TreeItemValue> selectedItem = getTreeItem();
				TreeItemValue itemValue = selectedItem.getValue();

				switch(contextMenuType) {
				case ADD:
					addNewEmptyGroup(itemValue.getItemType());
					break;
				case REMOVE:
					removeSelectedGroups();
					break;
				case EXPORT:
					exportTableData(itemValue);
					break;
				case SEARCH_AND_REPLACE:
					searchAndReplace(selectedItem);
					break;
				default:
					System.out.println(this.getClass().getSimpleName() + " : " + event);
					break;

				}
			}
		}
	}			

	EditableMenuCheckBoxTreeCell() {
		DropEventHandler dropEventHandler = new DropEventHandler();
		DropMouseEventHandler dropMouseEventHandler = new DropMouseEventHandler();
		
		this.setOnDragOver(dropEventHandler);
		this.setOnDragDropped(dropEventHandler);
		this.setOnDragDetected(dropMouseEventHandler);		
	}

	private void initContextMenu(TreeItemType itemType) {
		boolean disable = !SQLManager.getInstance().hasDatabase();
		SQLManager.getInstance().addProjectDatabaseStateChangedListener(new DatabaseStateChangedListener());
		ContextMenuEventHandler listener = new ContextMenuEventHandler();
		MenuItem addItem = new MenuItem(i18n.getString("EditableMenuCheckBoxTreeCell.contextmenu.add", "Add item"));
		addItem.setUserData(ContextMenuType.ADD);
		addItem.setOnAction(listener);
		addItem.setDisable(disable);
		
		if (TreeItemType.isPointTypeDirectory(itemType) || 
				TreeItemType.isObservationTypeDirectory(itemType) || 
				TreeItemType.isGNSSObservationTypeDirectory(itemType) ||
				TreeItemType.isCongruenceAnalysisTypeDirectory(itemType)) {
			this.contextMenu = new ContextMenu(addItem);
			return;
		}

		MenuItem removeItem = new MenuItem(i18n.getString("EditableMenuCheckBoxTreeCell.contextmenu.remove", "Remove items"));
		removeItem.setUserData(ContextMenuType.REMOVE);
		removeItem.setOnAction(listener);

		MenuItem exportItem = new MenuItem(i18n.getString("EditableMenuCheckBoxTreeCell.contextmenu.export", "Export items"));
		exportItem.setUserData(ContextMenuType.EXPORT);
		exportItem.setOnAction(listener);
		
		MenuItem searchAndReplaceItem = new MenuItem(i18n.getString("EditableMenuCheckBoxTreeCell.contextmenu.search_and_replace", "Search and replace"));
		searchAndReplaceItem.setUserData(ContextMenuType.SEARCH_AND_REPLACE);
		searchAndReplaceItem.setOnAction(listener);

		this.contextMenu = new ContextMenu(addItem, removeItem, searchAndReplaceItem, exportItem);

		if (TreeItemType.isPointTypeLeaf(itemType)) {
			ToggleGroup pointTypeToogleGroup = new ToggleGroup();

			RadioMenuItem referencePointMenuItem = createRadioMenuItem(
					i18n.getString("EditableMenuCheckBoxTreeCell.contextmenu.pointgroup.reference", "Reference point group"),
					itemType == TreeItemType.REFERENCE_POINT_1D_LEAF || itemType == TreeItemType.REFERENCE_POINT_2D_LEAF || itemType == TreeItemType.REFERENCE_POINT_3D_LEAF,
					pointTypeToogleGroup,
					ContextMenuType.CHANGE_TO_REFERENCE_POINT_GROUP
					);

			RadioMenuItem stochasticPointMenuItem = createRadioMenuItem(
					i18n.getString("EditableMenuCheckBoxTreeCell.contextmenu.pointgroup.stochastic", "Stochastic point group"),
					itemType == TreeItemType.STOCHASTIC_POINT_1D_LEAF || itemType == TreeItemType.STOCHASTIC_POINT_2D_LEAF || itemType == TreeItemType.STOCHASTIC_POINT_3D_LEAF,
					pointTypeToogleGroup,
					ContextMenuType.CHANGE_TO_STOCHASTIC_POINT_GROUP
					);

			RadioMenuItem datumPointMenuItem = createRadioMenuItem(
					i18n.getString("EditableMenuCheckBoxTreeCell.contextmenu.pointgroup.datum", "Datum point group"),
					itemType == TreeItemType.DATUM_POINT_1D_LEAF || itemType == TreeItemType.DATUM_POINT_2D_LEAF || itemType == TreeItemType.DATUM_POINT_3D_LEAF,
					pointTypeToogleGroup,
					ContextMenuType.CHANGE_TO_DATUM_POINT_GROUP
					);

			RadioMenuItem newPointMenuItem = createRadioMenuItem(
					i18n.getString("EditableMenuCheckBoxTreeCell.contextmenu.pointgroup.new", "New point group"),
					itemType == TreeItemType.NEW_POINT_1D_LEAF || itemType == TreeItemType.NEW_POINT_2D_LEAF || itemType == TreeItemType.NEW_POINT_3D_LEAF,
					pointTypeToogleGroup,
					ContextMenuType.CHANGE_TO_NEW_POINT_GROUP
					);

			pointTypeToogleGroup.selectedToggleProperty().addListener(new PointGroupChangeListener());
			this.contextMenu.getItems().addAll(
					new SeparatorMenuItem(),
					referencePointMenuItem,
					stochasticPointMenuItem,
					datumPointMenuItem,
					newPointMenuItem
					);
		}
		
		// disable all items (until a database is selected)
		for (MenuItem item : this.contextMenu.getItems())
			item.setDisable(disable);
	}

	private static RadioMenuItem createRadioMenuItem(String label, boolean selected, ToggleGroup group, ContextMenuType type) {
		RadioMenuItem radioMenuItem = new RadioMenuItem(label);
		radioMenuItem.setUserData(type);
		radioMenuItem.setSelected(selected);
		radioMenuItem.setToggleGroup(group);
		return radioMenuItem;
	}

	@Override
	public void commitEdit(TreeItemValue item) {
		if (!this.isEditing() && !item.equals(this.getItem())) {
			final TreeItem<TreeItemValue> treeItem = getTreeItem();
			final TreeView<TreeItemValue> treeView = getTreeView();
			if (treeView != null) {
				// Inform the TreeView of the edit being ready to be committed.
				treeView.fireEvent(new TreeView.EditEvent<TreeItemValue>(treeView, TreeView.<TreeItemValue> editCommitEvent(), treeItem, getItem(), item));
				//treeView.getSelectionModel().select(treeItem);
			}
		}
		super.commitEdit(item);
	}    

	@Override
	public void startEdit() {
		super.startEdit();

		if (this.getGraphic() != null && this.getGraphic() instanceof CheckBox)
			this.checkBox = (CheckBox)this.getGraphic();

		if (this.textField == null)
			this.createTextField();

		if (this.getItem() != null) {
			switch(this.getItem().getItemType()) {
//			case ROOT: // root node is editable
			case REFERENCE_POINT_1D_LEAF:
			case REFERENCE_POINT_2D_LEAF:
			case REFERENCE_POINT_3D_LEAF:		
			case STOCHASTIC_POINT_1D_LEAF:
			case STOCHASTIC_POINT_2D_LEAF:
			case STOCHASTIC_POINT_3D_LEAF:			
			case DATUM_POINT_1D_LEAF:
			case DATUM_POINT_2D_LEAF:
			case DATUM_POINT_3D_LEAF:			
			case NEW_POINT_1D_LEAF:
			case NEW_POINT_2D_LEAF:
			case NEW_POINT_3D_LEAF:
			case LEVELING_LEAF:
			case DIRECTION_LEAF:
			case HORIZONTAL_DISTANCE_LEAF:
			case SLOPE_DISTANCE_LEAF:
			case ZENITH_ANGLE_LEAF:
			case GNSS_1D_LEAF:
			case GNSS_2D_LEAF:
			case GNSS_3D_LEAF:
			case CONGRUENCE_ANALYSIS_1D_LEAF:
			case CONGRUENCE_ANALYSIS_2D_LEAF:
			case CONGRUENCE_ANALYSIS_3D_LEAF:
				this.setText(null);
				this.setGraphic(this.textField);
				this.textField.setText(this.getString());
				Platform.runLater(new Runnable() {
		            @Override public void run() {
		            	textField.selectAll();
		            	textField.requestFocus();
		            }
		        });
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		this.setText(this.getItem().toString());
		this.setGraphic(this.checkBox);
	}

	@Override
	public void updateItem(TreeItemValue item, boolean empty) {
		super.updateItem(item, empty);

		// Setze Kontextmenu
		if (item != null) {
			switch(item.getItemType()) {
			case REFERENCE_POINT_1D_LEAF:
			case REFERENCE_POINT_2D_LEAF:
			case REFERENCE_POINT_3D_LEAF:		
			case STOCHASTIC_POINT_1D_LEAF:
			case STOCHASTIC_POINT_2D_LEAF:
			case STOCHASTIC_POINT_3D_LEAF:			
			case DATUM_POINT_1D_LEAF:
			case DATUM_POINT_2D_LEAF:
			case DATUM_POINT_3D_LEAF:			
			case NEW_POINT_1D_LEAF:
			case NEW_POINT_2D_LEAF:
			case NEW_POINT_3D_LEAF:
			case LEVELING_LEAF:
			case DIRECTION_LEAF:
			case HORIZONTAL_DISTANCE_LEAF:
			case SLOPE_DISTANCE_LEAF:
			case ZENITH_ANGLE_LEAF:
			case GNSS_1D_LEAF:
			case GNSS_2D_LEAF:
			case GNSS_3D_LEAF:
			case CONGRUENCE_ANALYSIS_1D_LEAF:
			case CONGRUENCE_ANALYSIS_2D_LEAF:
			case CONGRUENCE_ANALYSIS_3D_LEAF:
				this.initContextMenu(item.getItemType());
				this.setContextMenu(this.contextMenu);
				break;
			case REFERENCE_POINT_1D_DIRECTORY:
			case REFERENCE_POINT_2D_DIRECTORY:
			case REFERENCE_POINT_3D_DIRECTORY:		
			case STOCHASTIC_POINT_1D_DIRECTORY:
			case STOCHASTIC_POINT_2D_DIRECTORY:
			case STOCHASTIC_POINT_3D_DIRECTORY:			
			case DATUM_POINT_1D_DIRECTORY:
			case DATUM_POINT_2D_DIRECTORY:
			case DATUM_POINT_3D_DIRECTORY:			
			case NEW_POINT_1D_DIRECTORY:
			case NEW_POINT_2D_DIRECTORY:
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
				if (getTreeItem() != null && getTreeItem().isLeaf()) {
					this.initContextMenu(item.getItemType());
					this.setContextMenu(this.contextMenu);
				}
				else {
					this.setContextMenu(null);
				}
				break;
			default:
				this.setContextMenu(null);
				break;
			}
		}

		if (empty)
			setText(null);
		else {
			if (isEditing()) {
				// Editierbare Nodes sind alle Leafs, vgl. startEditing    		      		        		
				if (this.textField != null) 
					this.textField.setText(getString());
				this.setText(null);
				this.setGraphic(this.textField);
			} 
			else {
				this.setText(getString());
				if (getTreeItem() != null && getTreeItem().getValue() != null && 
						(getTreeItem().getValue().getItemType() == TreeItemType.ROOT ||
						getTreeItem().getValue().getItemType() == TreeItemType.UNSPECIFIC))
					setGraphic(new Text());
				else if (this.checkBox != null) {
					setGraphic(this.checkBox);
				}
			}
		}
	}

	private void createTextField() {
		this.textField = new TextField("");

		this.textField.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (getTreeItem() != null) {
					TreeItemValue item = getTreeItem().getValue();
					item.setName(textField.getText());
					commitEdit(item);
				}
			}
		});

		this.textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					TreeItemValue item = getTreeItem().getValue();
					item.setName(textField.getText());
					commitEdit(item);
				}
			}
		});

		this.textField.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ESCAPE) {
					textField.setText(getString());
					cancelEdit();
					event.consume();
				}
			}
		}); 
	}

	private String getString() {
		return this.getItem() == null ? "" : this.getItem().toString();
	}

	private void changePointGroupType(TreeItemType newItemType) {
		List<TreeItem<TreeItemValue>> selectedItems = this.getTreeView().getSelectionModel().getSelectedItems();
		if (newItemType != null && selectedItems != null && !selectedItems.isEmpty())
			UITreeBuilder.getInstance().moveItems(newItemType, new ArrayList<TreeItem<TreeItemValue>>(selectedItems));
	}

	private void addNewEmptyGroup(TreeItemType itemType) {
		TreeItemType parentType = null;
		if ((parentType = TreeItemType.getDirectoryByLeafType(itemType)) != null && TreeItemType.getLeafByDirectoryType(itemType) == null) {
			UITreeBuilder.getInstance().addEmptyGroup(parentType);
		}
		else if (TreeItemType.getDirectoryByLeafType(itemType) == null && TreeItemType.getLeafByDirectoryType(itemType) != null) {
			parentType = itemType;
			UITreeBuilder.getInstance().addEmptyGroup(parentType);
		}
	}

	private void removeSelectedGroups() {
		List<TreeItem<TreeItemValue>> selectedItems = this.getTreeView().getSelectionModel().getSelectedItems();
		if (selectedItems != null && !selectedItems.isEmpty())
			UITreeBuilder.getInstance().removeItems(new ArrayList<TreeItem<TreeItemValue>>(selectedItems));

	}
	
	private void searchAndReplace(TreeItem<TreeItemValue> selectedItem) {
		List<TreeItem<TreeItemValue>> selectedItems = this.getTreeView().getSelectionModel().getSelectedItems();

		TreeItemValue selectedTreeItemValues[] = new TreeItemValue[selectedItems != null ? selectedItems.size() : 0];
		for (int i=0; i<selectedItems.size(); i++) {
			selectedTreeItemValues[i] = selectedItems.get(i).getValue();
		}
		
//		this.getTreeView().getSelectionModel().clearSelection();
		SearchAndReplaceDialog.showAndWait(selectedItem.getValue(), selectedTreeItemValues);
//		this.getTreeView().getSelectionModel().select(selectedItem);
	}
	
	private void exportTableData(TreeItemValue itemValue) {
		try {
			TabPane tabPane = UITabPaneBuilder.getInstance().getTabPane();
			if (tabPane != null && tabPane.getSelectionModel().getSelectedItem() != null 
					&& tabPane.getSelectionModel().getSelectedItem().getUserData() != null
					&& tabPane.getSelectionModel().getSelectedItem().getUserData() instanceof TabType) {

				TreeItemType itemType = itemValue.getItemType();
				TabType tabType = (TabType)tabPane.getSelectionModel().getSelectedItem().getUserData();

				boolean aprioriValues = tabType == TabType.RAW_DATA;

				File selectedFile = DefaultFileChooser.showSaveDialog(
						i18n.getString("EditableMenuCheckBoxTreeCell.filechooser.save.title", "Export table data"),
						this.getItem().getName() + (aprioriValues ? "_apriori" : "_aposteriori") +  ".txt"
						);
				
				if (selectedFile != null) {
					if (TreeItemType.isPointTypeLeaf(itemType))
						UIPointTableBuilder.getInstance().export(selectedFile, aprioriValues);
					
					else if (TreeItemType.isObservationTypeLeaf(itemType))
						UITerrestrialObservationTableBuilder.getInstance().export(selectedFile, aprioriValues);
					
					else if (TreeItemType.isGNSSObservationTypeLeaf(itemType))
						UIGNSSObservationTableBuilder.getInstance().export(selectedFile, aprioriValues);
					
					else if (TreeItemType.isCongruenceAnalysisTypeLeaf(itemType))
						UICongruenceAnalysisTableBuilder.getInstance().export(selectedFile, aprioriValues);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			OptionDialog.showThrowableDialog (
					i18n.getString("EditableMenuCheckBoxTreeCell.message.error.export.exception.title", "I/O Error"),
					i18n.getString("EditableMenuCheckBoxTreeCell.message.error.export.exception.header", "Error, could not export selected table data."),
					i18n.getString("EditableMenuCheckBoxTreeCell.message.error.export.exception.message", "An exception has occurred during data export."),
					e);
		}
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
