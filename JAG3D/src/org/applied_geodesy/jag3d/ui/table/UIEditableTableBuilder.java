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

package org.applied_geodesy.jag3d.ui.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.applied_geodesy.jag3d.DefaultApplicationProperty;
import org.applied_geodesy.jag3d.ui.table.row.GroupRow;
import org.applied_geodesy.jag3d.ui.tree.Groupable;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.ui.dialog.OptionDialog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;

public abstract class UIEditableTableBuilder<T extends GroupRow> extends UITableBuilder<T> {

	enum ContextMenuType {
		REMOVE,
		DUPLICATE,
		MOVETO,
		
		SELECT_GROUPS,

		MOVETO_NEW,
		MOVETO_REFERENCE,
		MOVETO_STOCHASTIC,
		MOVETO_DATUM;
	}

	private class ContextMenuEventHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() instanceof MenuItem && ((MenuItem)event.getSource()).getUserData() instanceof ContextMenuType) {
				ContextMenuType contextMenuType = (ContextMenuType)((MenuItem)event.getSource()).getUserData();
				switch(contextMenuType) {
				case REMOVE:
					boolean isDeleteConfirmed = !DefaultApplicationProperty.showConfirmDialogOnDelete();
					if (!isDeleteConfirmed) {
						Optional<ButtonType> result = OptionDialog.showConfirmationDialog(
								i18n.getString("UIEditableTableBuilder.message.confirmation.delete.title",   "Delete items"),
								i18n.getString("UIEditableTableBuilder.message.confirmation.delete.header",  "Delete items permanently?"),
								i18n.getString("UIEditableTableBuilder.message.confirmation.delete.message", "Are you sure you want to remove the selected items?")
								);
						isDeleteConfirmed = result.isPresent() && result.get() == ButtonType.OK;
					}
					
					if (isDeleteConfirmed)
						removeRows();
					
					break;
				case SELECT_GROUPS:
					selectGroups();
				case MOVETO:
				case MOVETO_REFERENCE:
				case MOVETO_STOCHASTIC:
				case MOVETO_DATUM:
				case MOVETO_NEW:
					moveRows(contextMenuType);
					break;
				case DUPLICATE:
					duplicateRows();
					break;
				}
			}
		}
	}

	private ContextMenuEventHandler listener = new ContextMenuEventHandler();

	@Override
	TableView<T> createTable() {
		this.table = super.createTable();
		this.table.setEditable(true);
		this.enableDragSupport();

		return this.table;
	}

	ContextMenu createContextMenu(boolean isPointTable) {
		MenuItem removeMenuItem = this.createMenuItem(
				i18n.getString("UIEditableTableBuilder.contextmenu.remove", "Remove selected items"),
				ContextMenuType.REMOVE,
				listener
				);

		MenuItem duplicateMenuItem = this.createMenuItem(
				i18n.getString("UIEditableTableBuilder.contextmenu.duplicate", "Duplicate selected items"),
				ContextMenuType.DUPLICATE,
				listener
				);
		
		MenuItem selectGroupsMenuItem = this.createMenuItem(
				i18n.getString("UIEditableTableBuilder.contextmenu.select", "Select item groups"),
				ContextMenuType.SELECT_GROUPS,
				listener
				);
		
		ContextMenu contextMenu = new ContextMenu(removeMenuItem, duplicateMenuItem);

		if (!isPointTable) {
			MenuItem moveToMenuItem = this.createMenuItem(
					i18n.getString("UIEditableTableBuilder.contextmenu.moveto", "Move selected items"),
					ContextMenuType.MOVETO,
					listener
					);
			contextMenu.getItems().add(moveToMenuItem);
		}
		else {
			Menu moveToMenu = this.createMenu(
					i18n.getString("UIEditableTableBuilder.contextmenu.moveto", "Move selected items")
					);

			MenuItem moveToReferenceMenuItem = this.createMenuItem(
					i18n.getString("UIEditableTableBuilder.contextmenu.moveto.reference", "Reference point group"),
					ContextMenuType.MOVETO_REFERENCE,
					listener
					);

			MenuItem moveToStochasticMenuItem = this.createMenuItem(
					i18n.getString("UIEditableTableBuilder.contextmenu.moveto.stochastic", "Stochastic point group"),
					ContextMenuType.MOVETO_STOCHASTIC,
					listener
					);

			MenuItem moveToDatumMenuItem = this.createMenuItem(
					i18n.getString("UIEditableTableBuilder.contextmenu.moveto.datum", "Datum point group"),
					ContextMenuType.MOVETO_DATUM,
					listener
					);

			MenuItem moveToNewMenuItem = this.createMenuItem(
					i18n.getString("UIEditableTableBuilder.contextmenu.moveto.new", "New point group"),
					ContextMenuType.MOVETO_NEW,
					listener
					);
			
			

			moveToMenu.getItems().addAll(moveToReferenceMenuItem, moveToStochasticMenuItem, moveToDatumMenuItem, moveToNewMenuItem);
			contextMenu.getItems().add(moveToMenu);
		}
		
		contextMenu.getItems().addAll(new SeparatorMenuItem(), selectGroupsMenuItem);
		return contextMenu;
	}

	void raiseErrorMessage(ContextMenuType type, Exception e) {
		Platform.runLater(new Runnable() {
			@Override public void run() {
				switch(type) {
				case DUPLICATE:
					OptionDialog.showThrowableDialog (
							i18n.getString("UIEditableTableBuilder.message.error.duplicate.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIEditableTableBuilder.message.error.duplicate.exception.header", "Error, could not save copies of selected items."),
							i18n.getString("UIEditableTableBuilder.message.error.duplicate.exception.message", "An exception has occurred during database transaction."),
							e);
					break;
				case REMOVE:
					OptionDialog.showThrowableDialog (
							i18n.getString("UIEditableTableBuilder.message.error.remove.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIEditableTableBuilder.message.error.remove.exception.header", "Error, could not remove selected items."),
							i18n.getString("UIEditableTableBuilder.message.error.remove.exception.message", "An exception has occurred during database transaction."),
							e);
					break;
				case MOVETO:
				case MOVETO_DATUM:
				case MOVETO_NEW:
				case MOVETO_REFERENCE:
				case MOVETO_STOCHASTIC:
					OptionDialog.showThrowableDialog (
							i18n.getString("UIEditableTableBuilder.message.error.moveto.exception.title", "Unexpected SQL-Error"),
							i18n.getString("UIEditableTableBuilder.message.error.moveto.exception.header", "Error, could not move selected items to new table."),
							i18n.getString("UIEditableTableBuilder.message.error.moveto.exception.message", "An exception has occurred during database transaction."),
							e);
					break;

				case SELECT_GROUPS:
					break;
				}
			}
		});
	}
	
	void raiseErrorMessageSaveValue(Exception e) {
		Platform.runLater(new Runnable() {
			@Override public void run() {
				OptionDialog.showThrowableDialog (
						i18n.getString("UIEditableTableBuilder.message.error.save.exception.title", "Unexpected SQL-Error"),
						i18n.getString("UIEditableTableBuilder.message.error.save.exception.header", "Error, could not save value to database."),
						i18n.getString("UIEditableTableBuilder.message.error.save.exception.message", "An exception has occurred during database transaction."),
						e
				);
			}
		});
	}

	private MenuItem createMenuItem(String label, ContextMenuType type, ContextMenuEventHandler listener) {
		MenuItem item = new MenuItem(label);
		item.setUserData(type);
		item.setOnAction(listener);
		return item;
	}

	private Menu createMenu(String label) {
		Menu menu = new Menu(label);
		return menu;
	}

	abstract void enableDragSupport();
	abstract void removeRows();
	abstract void duplicateRows();
	abstract void moveRows(ContextMenuType type);
	private void selectGroups() {
		try {
			List<GroupRow> selectedRows = new ArrayList<GroupRow>(this.table.getSelectionModel().getSelectedItems());
			Set<Integer> groupIds = new HashSet<Integer>();
			for (GroupRow selectedRow : selectedRows)
				groupIds.add(selectedRow.getGroupId());

			if (groupIds == null || groupIds.isEmpty())
				return;

			List<TreeItem<TreeItemValue>> selectedTreeItems = new ArrayList<TreeItem<TreeItemValue>>(UITreeBuilder.getInstance().getTree().getSelectionModel().getSelectedItems());
			if (selectedTreeItems.size() > 1) {
				List<Integer> selectedTreeItemIndices = new ArrayList<Integer>(UITreeBuilder.getInstance().getTree().getSelectionModel().getSelectedIndices());
				int indices[] = new int[groupIds.size()];

				for (int i = 0, j = 0; i < selectedTreeItems.size(); i++) {
					TreeItem<TreeItemValue> selectedTreeItem = selectedTreeItems.get(i);
					if (selectedTreeItem.getValue() instanceof Groupable && groupIds.contains(((Groupable)selectedTreeItem.getValue()).getGroupId())) {
						indices[j++] = selectedTreeItemIndices.get(i);
						if (j == indices.length) // check against IndexOutOfBoundException
							break;
					}
				}
				Platform.runLater(new Runnable() {
					@Override public void run() {
						UITreeBuilder.getInstance().getTree().getSelectionModel().clearSelection();
						UITreeBuilder.getInstance().getTree().getSelectionModel().selectIndices(indices[0], indices);
					}
				});
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
