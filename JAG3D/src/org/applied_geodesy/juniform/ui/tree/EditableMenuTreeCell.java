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

import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class EditableMenuTreeCell extends TreeCell<TreeItemValue<?>> {
	private TextField textField;

	@Override
	public void commitEdit(TreeItemValue<?> item) {
		if (!this.isEditing() && !item.equals(this.getItem())) {
			final TreeItem<TreeItemValue<?>> treeItem = getTreeItem();
			final TreeView<TreeItemValue<?>> treeView = getTreeView();
			if (treeView != null) {
				treeView.fireEvent(new TreeView.EditEvent<TreeItemValue<?>>(treeView, TreeView.<TreeItemValue<?>> editCommitEvent(), treeItem, getItem(), item));
			}
		}
		super.commitEdit(item);
	}    

	@Override
	public void startEdit() {
		super.startEdit();

		if (this.textField == null)
			this.createTextField();

		if (this.getItem() != null) {
			if (this.getItem().getObject() instanceof GeometricPrimitive) {
				this.setText(null);
				this.setGraphic(this.textField);
				this.textField.setText(this.getString());
				Platform.runLater(new Runnable() {
					@Override public void run() {
						textField.selectAll();
						textField.requestFocus();
					}
				});
			}
		}
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		this.setText(this.getItem().toString());
		this.setGraphic(this.getTreeItem().getGraphic());
	}

	@Override
	public void updateItem(TreeItemValue<?> item, boolean empty) {
		super.updateItem(item, empty);

		if (empty)
			setText(null);
		else {
			if (isEditing()) {
				// Editierbare Nodes sind alle Leafs, vgl. startEditing    		      		        		
				if (this.textField != null) 
					this.textField.setText(this.getString());
				this.setText(null);
				this.setGraphic(this.textField);
			} 
			else {
				this.setText(this.getString());
				this.setGraphic(this.getTreeItem().getGraphic());
			}
		}
	}

	private void createTextField() {
		this.textField = new TextField("");

		this.textField.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (getTreeItem() != null) {
					TreeItemValue<?> item = getTreeItem().getValue();
					item.setName(textField.getText());
					commitEdit(item);
				}
			}
		});

		this.textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					TreeItemValue<?> item = getTreeItem().getValue();
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
}
