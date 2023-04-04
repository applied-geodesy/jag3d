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

package org.applied_geodesy.juniform.ui.dialog;

import java.util.Optional;

import org.applied_geodesy.adjustment.geometry.VarianceComponent;
import org.applied_geodesy.juniform.ui.i18n.I18N;
import org.applied_geodesy.juniform.ui.table.UIVarianceComponentTableBuilder;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class VarianceComponentsDialog {
	private static I18N i18N = I18N.getInstance();
	private static VarianceComponentsDialog varianceComponentsDialog = new VarianceComponentsDialog();
	private Dialog<Void> dialog = null;
	private Window window;
	private ObservableList<VarianceComponent> tableModel;
	private VarianceComponentsDialog() {}
	private VBox contentPane;
	
	public static void setOwner(Window owner) {
		varianceComponentsDialog.window = owner;
	}

	public static Optional<Void> showAndWait(VarianceComponent... varianceComponents) {
		varianceComponentsDialog.init();
		varianceComponentsDialog.setVarianceComponents(varianceComponents);
		// @see https://bugs.openjdk.java.net/browse/JDK-8087458
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	try {
            		varianceComponentsDialog.dialog.getDialogPane().requestLayout();
            		Stage stage = (Stage) varianceComponentsDialog.dialog.getDialogPane().getScene().getWindow();
            		stage.sizeToScene();
            	} 
            	catch (Exception e) {
            		e.printStackTrace();
            	}
            }
		});
		return varianceComponentsDialog.dialog.showAndWait();
	}
	
	private void setVarianceComponents(VarianceComponent... varianceComponents) {
		if (varianceComponents != null && varianceComponents.length > 0) {
			this.tableModel.setAll(varianceComponents);
			this.contentPane.setPrefHeight(this.contentPane.getMinHeight() + varianceComponents.length * 30);
		}
		else
			this.tableModel.clear();
	}

	private void init() {
		if (this.dialog != null)
			return;

		this.contentPane = this.createPane();
		this.dialog = new Dialog<Void>();
		this.dialog.setTitle(i18N.getString("VarianceComponentsDialog.title", "Variance component estimation"));
		this.dialog.setHeaderText(i18N.getString("VarianceComponentsDialog.header", "Estimated variance components"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		this.dialog.initOwner(window);
		this.dialog.getDialogPane().setContent(this.contentPane);
		this.dialog.setResizable(true);
		this.dialog.setResultConverter(new Callback<ButtonType, Void>() {
			@Override
			public Void call(ButtonType buttonType) {
				return null;
			}
		});
	}
	
	private VBox createPane() {
		TableView<VarianceComponent> varianceComponentTableView = UIVarianceComponentTableBuilder.getInstance().getTable();
		this.tableModel = UIVarianceComponentTableBuilder.getInstance().getTableModel(varianceComponentTableView);
		VBox contentPane = new VBox();
		contentPane.setPadding(new Insets(5,10,5,10));
		contentPane.getChildren().setAll(varianceComponentTableView);
		contentPane.setMinHeight(75);
		return contentPane;
	}
}
