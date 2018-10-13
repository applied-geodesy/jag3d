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

package org.applied_geodesy.jag3d.ui.resultpane;

import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateChangedListener;
import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateEvent;
import org.applied_geodesy.jag3d.sql.ProjectDatabaseStateType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.UIPrincipalComponentTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UITestStatisticTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIVarianceComponentTableBuilder;
import org.applied_geodesy.jag3d.ui.table.row.PrincipalComponentRow;
import org.applied_geodesy.jag3d.ui.table.row.TestStatisticRow;
import org.applied_geodesy.jag3d.ui.table.row.VarianceComponentRow;
import org.applied_geodesy.util.i18.I18N;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

public class UIGlobalResultPaneBuilder {
	private class DatabaseStateChangedListener implements ProjectDatabaseStateChangedListener {

		@Override
		public void projectDatabaseStateChanged(ProjectDatabaseStateEvent evt) {
			if (evt.getEventType() == ProjectDatabaseStateType.CLOSED) {
				// clear all global result tables
				UITestStatisticTableBuilder.getInstance().getTable().getItems().setAll(UITestStatisticTableBuilder.getInstance().getEmptyRow());
				UIVarianceComponentTableBuilder.getInstance().getTable().getItems().setAll(UIVarianceComponentTableBuilder.getInstance().getEmptyRow());
				UIPrincipalComponentTableBuilder.getInstance().getTable().getItems().setAll(UIPrincipalComponentTableBuilder.getInstance().getEmptyRow());
			}
		}
	}
	
	private static UIGlobalResultPaneBuilder resultPaneBuilder = new UIGlobalResultPaneBuilder();

	private Node resultDataNode = null;

	private UIGlobalResultPaneBuilder() {}

	public static UIGlobalResultPaneBuilder getInstance() {
		resultPaneBuilder.init();
		return resultPaneBuilder;
	}
	
	public Node getNode() {
		return this.resultDataNode;
	}

	private void init() {

		if (this.resultDataNode != null)
			return;

		I18N i18n = I18N.getInstance();
		
		TableView<TestStatisticRow> testStatisticTable = UITestStatisticTableBuilder.getInstance().getTable();
		testStatisticTable.setUserData(GlobalResultType.TEST_STATISTIC);
		testStatisticTable.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		TableView<VarianceComponentRow> varianceComponentTable = UIVarianceComponentTableBuilder.getInstance().getTable();
		varianceComponentTable.setUserData(GlobalResultType.VARIANCE_COMPONENT);
		varianceComponentTable.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		TableView<PrincipalComponentRow> principalComponentTable = UIPrincipalComponentTableBuilder.getInstance().getTable();
		principalComponentTable.setUserData(GlobalResultType.PRINCIPAL_COMPONENT);
		principalComponentTable.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		StackPane contenPane = new StackPane();
		contenPane.setPadding(new Insets(10, 30, 10, 30)); // oben, rechts, unten, links
		contenPane.getChildren().addAll(testStatisticTable, varianceComponentTable, principalComponentTable);
		varianceComponentTable.setVisible(false);
		principalComponentTable.setVisible(false);

		ComboBox<Node> paneSwitcherComboBox = new ComboBox<Node>();
		paneSwitcherComboBox.setCellFactory(new Callback<ListView<Node>, ListCell<Node>>() {
            @Override 
            public ListCell<Node> call(ListView<Node> param) {
				return new GlobalResultTypeListCell();
            }
		});
		paneSwitcherComboBox.setButtonCell(new GlobalResultTypeListCell());
		paneSwitcherComboBox.getItems().addAll(testStatisticTable, varianceComponentTable, principalComponentTable);
		paneSwitcherComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Node>() {

			@Override
			public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
				if (oldValue != null)
					oldValue.setVisible(false);
				if (newValue != null)
					newValue.setVisible(true);
			}
			
		});
		paneSwitcherComboBox.getSelectionModel().select(varianceComponentTable);
		paneSwitcherComboBox.setTooltip(new Tooltip(i18n.getString("UIGlobalResultPaneBuilder.global_result_switcher.tooltip", "Global network adjustment results")));
		Region spacer = new Region();
		HBox hbox = new HBox(10);
		hbox.setPadding(new Insets(15, 30, 5, 0));
		HBox.setHgrow(spacer, Priority.ALWAYS);
		hbox.getChildren().addAll(spacer, paneSwitcherComboBox);

		BorderPane borderPane = new BorderPane();
		borderPane.setTop(hbox);
		borderPane.setCenter(contenPane);
		
		this.resultDataNode = borderPane;
		
		SQLManager.getInstance().addProjectDatabaseStateChangedListener(new DatabaseStateChangedListener());
	}
}
