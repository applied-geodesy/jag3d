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

package org.applied_geodesy.coordtrans.ui.tabpane;

import org.applied_geodesy.adjustment.transformation.parameter.UnknownParameter;
import org.applied_geodesy.adjustment.transformation.point.FramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.coordtrans.ui.pane.UIInterpolationPaneBuilder;
import org.applied_geodesy.coordtrans.ui.pane.UIRestrictionPaneBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIFramePositionPairTableBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIHomologousFramePositionPairTableBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIParameterTableBuilder;
import org.applied_geodesy.coordtrans.ui.tree.TreeItemType;
import org.applied_geodesy.coordtrans.ui.tree.TreeItemValue;
import org.applied_geodesy.ui.table.ColumnType;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;

public class UITabPaneBuilder {
	private class TabSelectionChangeListener implements ChangeListener<Tab> {
		@Override
		public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
			lastSelectedTab = newTab == null ? lastSelectedTab : newTab;

			// remove old Content
			if (oldTab != null)
				oldTab.setContent(null);

			if (newTab != null && newTab.getUserData() instanceof TabType) {
				TabType tabType = (TabType)newTab.getUserData();
				newTab.setContent(getNode(tabType));
			}
		}
	}

	private TabPane tabPane = null;
	private static UITabPaneBuilder tabPaneBuilder = new UITabPaneBuilder();
	private I18N i18n = I18N.getInstance();
	private TabSelectionChangeListener tabSelectionChangeListener = new TabSelectionChangeListener();
	
	private UIRestrictionPaneBuilder restrictionPaneBuilder = UIRestrictionPaneBuilder.getInstance();
	private UIInterpolationPaneBuilder interpolationPaneBuilder = UIInterpolationPaneBuilder.getInstance();
	private UIHomologousFramePositionPairTableBuilder homologousFramePositionPairTableBuilder = UIHomologousFramePositionPairTableBuilder.getInstance();
	private UIParameterTableBuilder parameterTableBuilder = UIParameterTableBuilder.getInstance();
	private UIFramePositionPairTableBuilder framePositionPairTableBuilder = UIFramePositionPairTableBuilder.getInstance();
	
	private ObservableMap<TabType, Tab> tapMap = FXCollections.observableHashMap();
	private TreeItemValue<?> lastTreeItemValue = null;
	private Tab lastSelectedTab = null;

	public static UITabPaneBuilder getInstance() {
		return tabPaneBuilder;
	}

	public TabPane getTabPane() {
		if (this.tabPane == null)
			this.init();
		return this.tabPane;
	}

	private void init() {
		
		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.restriction.label", "Restriction"), 
				i18n.getString("UITabPaneBuilder.tab.restriction.title", "Parameter restrictions of transformation"), 
				TabType.RESTRICTIONS, null
		);
		
		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.interpolation.label", "Interpolation"), 
				i18n.getString("UITabPaneBuilder.tab.interpolation.title", "Interpolation approaches for residual gaps"), 
				TabType.INTERPOLATION, null
		);
		
		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.homologous_pair.apriori.label", "Position pairs"), 
				i18n.getString("UITabPaneBuilder.tab.homologous_pair.apriori.title", "Table of observed homologous position pairs"), 
				TabType.APRIORI_HOMOLOGOUS_PAIRS, null
		);
		
		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.position.source.apriori.label", "Source system"), 
				i18n.getString("UITabPaneBuilder.tab.position.source.apriori.title", "Table of source system positions"), 
				TabType.APRIORI_SOURCE_SYSTEM_POINTS, null
		);

		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.position.source.aposteriori.label", "Source system"), 
				i18n.getString("UITabPaneBuilder.tab.position.source.aposteriori.title", "Table of estimated source system positions"), 
				TabType.APOSTERIORI_SOURCE_SYSTEM_POINTS, null
		);
		
		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.position.target.aposteriori.label", "Target system"), 
				i18n.getString("UITabPaneBuilder.tab.position.target.aposteriori.title", "Table of estimated target system positions"), 
				TabType.APOSTERIORI_TARGET_SYSTEM_POINTS, null
				);
		
		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.position.outlier.label", "Outliers"), 
				i18n.getString("UITabPaneBuilder.tab.position.outlier.title", "Table of estimated stochastic parameters"), 
				TabType.APOSTERIORI_OUTLIERS, null
				);
		
		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.parameter.aposteriori.label", "Parameters"), 
				i18n.getString("UITabPaneBuilder.tab.parameter.aposteriori.title", "Table of estimated transformation parameters"), 
				TabType.APOSTERIORI_PARAMETERS, null
				);
				
		this.tabPane = new TabPane();
		this.tabPane.setSide(Side.BOTTOM);
		
		this.tabPane.getSelectionModel().selectedItemProperty().addListener(this.tabSelectionChangeListener);
	}
	
	private Tab createTab(String name, String tooltip, TabType type, Node node) {
		Tab tab = new Tab(name, node);
		tab.setClosable(false);
		tab.setTooltip(new Tooltip(tooltip));
		tab.setUserData(type);
		this.tapMap.put(type, tab);
		return tab;
	}

	private Node getNode(TabType tabType) {
		if (this.lastTreeItemValue == null || tabType == null)
			return null;

		Node node = null;
		TreeItemType treeItemType = this.lastTreeItemValue.getTreeItemType();
		switch(treeItemType) {
		case ADJUSTMENT:
			Node propertyPane = (tabType == TabType.INTERPOLATION) ? this.interpolationPaneBuilder.getNode() : this.restrictionPaneBuilder.getNode();
			node = propertyPane;

			break;
		case OBSERVED_POSITIONS:
			TableView<HomologousFramePositionPair> homologousFramePositionPairTableView = this.homologousFramePositionPairTableBuilder.getTable();
			this.setTableColumnView(tabType, homologousFramePositionPairTableView);
			node = homologousFramePositionPairTableView;

			break;
		case TRANSFORMATION_PARAMETERS:
			TableView<UnknownParameter> parameterTableView = this.parameterTableBuilder.getTable();
			this.setTableColumnView(tabType, parameterTableView);
			node = parameterTableView;

			break;
		case TRANSFORMED_POSITIONS:
			TableView<FramePositionPair> framePositionPairTableBuilder = this.framePositionPairTableBuilder.getTable();
			this.setTableColumnView(tabType, framePositionPairTableBuilder);
			node = framePositionPairTableBuilder;
			
			break;
		case UNSPECIFIC:
			break;
		default:
			break;
		
		}
		return node;
	}
	
	private void setTableColumnView(TabType tabType, TableView<?> tableView) {
		int columnCount = tableView.getColumns().size();

		for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
			TableColumn<?, ?> column = tableView.getColumns().get(columnIndex);
			if (column.getUserData() instanceof ColumnType) {
				ColumnType columnType = (ColumnType)column.getUserData();
				switch(columnType) {
				case VISIBLE:
					column.setVisible(true);
					break;
					
				case HIDDEN:
					column.setVisible(false);
					break;
					
				case APRIORI_POINT:
					column.setVisible(tabType == TabType.APRIORI_SOURCE_SYSTEM_POINTS || tabType == TabType.APRIORI_HOMOLOGOUS_PAIRS);
					break;
					
				case APOSTERIORI_SOURCE_SYSTEM:
					column.setVisible(tabType == TabType.APOSTERIORI_SOURCE_SYSTEM_POINTS);
					break;
					
				case APOSTERIORI_TARGET_SYSTEM:
					column.setVisible(tabType == TabType.APOSTERIORI_TARGET_SYSTEM_POINTS);
					break;

				case APOSTERIORI_POINT:
					column.setVisible(tabType == TabType.APOSTERIORI_OUTLIERS);
					break;

				default:
					throw new IllegalArgumentException("Error, unsupported column type " + columnType + "!");
				}
			}
		}
	}
	
	public void setTreeItemValue(TreeItemValue<?> treeItemValue) {
		if (this.tabPane == null)
			this.init();
		
		SingleSelectionModel<Tab> selectionModel = this.tabPane.getSelectionModel();
		try {
			this.lastSelectedTab = this.lastSelectedTab != null ? this.lastSelectedTab : selectionModel.getSelectedItem();
			selectionModel.clearSelection();

			this.lastTreeItemValue = treeItemValue;
			if (this.tabPane != null && treeItemValue != null) {
				TabType[] newTabTypes = treeItemValue.getTabTypes();
				if (newTabTypes != null && newTabTypes.length > 0) {
					Tab selectedTab = null;

					ObservableList<Tab> oldTabList = tabPane.getTabs();
					boolean equalTabOrderAndTypes = oldTabList.size() == newTabTypes.length;

					if (equalTabOrderAndTypes) {
						for (int idx = 0; idx < newTabTypes.length; idx++) {
							Tab tab = oldTabList.get(idx);
							if (tab.getUserData() == null || tab.getUserData() != newTabTypes[idx]) {
								equalTabOrderAndTypes = false;
								break;
							}
						}
					}

					if (!equalTabOrderAndTypes) {
						ObservableList<Tab> newTabList = FXCollections.observableArrayList();
						
						this.tabPane.getTabs().clear();
						for (TabType tabType : newTabTypes) {
							if (this.tapMap.containsKey(tabType)) {
								newTabList.add(this.tapMap.get(tabType));
								if (this.lastSelectedTab != null && this.lastSelectedTab.getUserData() == tabType) {
									selectedTab = this.tapMap.get(tabType);
								}
							}
						}
						
						this.tabPane.getTabs().setAll(newTabList);
					}
					else {
						boolean validLastSelectedTabType = false;
						for (TabType newType : newTabTypes) {
							if (this.lastSelectedTab.getUserData() == newType) {
								validLastSelectedTabType = true;
								break;
							}
						}

						selectedTab = validLastSelectedTabType ? this.lastSelectedTab : null;
					}

					if (selectedTab == null && this.tabPane.getTabs().size() > 0)
						selectedTab = this.tabPane.getTabs().get(0);

					selectionModel.select(selectedTab);

					// setContent() is called by TabSelectionChangeListener
					//					if (selectedTab != null && selectedTab.getUserData() instanceof TabType) {
					//						TabType tabType = (TabType)selectedTab.getUserData();
					//						selectedTab.setContent(getNode(tabType));
					//					}
				}
				else {
					tabPane.getTabs().clear();
					System.out.println(this.getClass().getSimpleName() + " : No known tab types " + newTabTypes + " for " + treeItemValue);
				}
			}
		}
		finally {
		}
	}
}
