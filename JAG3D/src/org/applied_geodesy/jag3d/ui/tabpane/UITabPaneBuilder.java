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

package org.applied_geodesy.jag3d.ui.tabpane;

import org.applied_geodesy.jag3d.ui.graphic.UIGraphicPaneBuilder;
import org.applied_geodesy.jag3d.ui.metadata.UIMetaDataPaneBuilder;
import org.applied_geodesy.jag3d.ui.propertiespane.UICongruenceAnalysisPropertiesPaneBuilder;
import org.applied_geodesy.jag3d.ui.propertiespane.UIObservationPropertiesPaneBuilder;
import org.applied_geodesy.jag3d.ui.propertiespane.UIPointPropertiesPaneBuilder;
import org.applied_geodesy.jag3d.ui.resultpane.UIGlobalResultPaneBuilder;
import org.applied_geodesy.jag3d.ui.table.ColumnType;
import org.applied_geodesy.jag3d.ui.table.UIAdditionalParameterTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UICongruenceAnalysisTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIGNSSObservationTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIPointTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UITerrestrialObservationTableBuilder;
import org.applied_geodesy.jag3d.ui.table.row.AdditionalParameterRow;
import org.applied_geodesy.jag3d.ui.table.row.CongruenceAnalysisRow;
import org.applied_geodesy.jag3d.ui.table.row.GNSSObservationRow;
import org.applied_geodesy.jag3d.ui.table.row.PointRow;
import org.applied_geodesy.jag3d.ui.table.row.Row;
import org.applied_geodesy.jag3d.ui.table.row.TerrestrialObservationRow;
import org.applied_geodesy.jag3d.ui.tree.CongruenceAnalysisTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.ObservationTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.PointTreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.TreeItemType;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
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
				
//				// re-draw network plot
//				if (lastTreeItemValue != null && tabType != null && lastTreeItemValue.getItemType() == TreeItemType.ROOT && tabType == TabType.GRAPHIC)
//					UIGraphicPaneBuilder.getInstance().getLayerManager().redraw();
			}
		}
	}

	private TabPane tabPane = null;
	private static UITabPaneBuilder tabPaneBuilder = new UITabPaneBuilder();
	private I18N i18n = I18N.getInstance();
	private TabSelectionChangeListener tabSelectionChangeListener = new TabSelectionChangeListener();

	private UIPointTableBuilder pointTableBuilder                           = UIPointTableBuilder.getInstance();
	private UITerrestrialObservationTableBuilder observationTableBuilder    = UITerrestrialObservationTableBuilder.getInstance();
	private UIGNSSObservationTableBuilder gnssObservationTableBuilder       = UIGNSSObservationTableBuilder.getInstance();
	private UICongruenceAnalysisTableBuilder congruenceAnalysisTableBuilder = UICongruenceAnalysisTableBuilder.getInstance();

	private ObservableMap<TabType, Tab> tapMap = FXCollections.observableHashMap();
	private TreeItemValue lastTreeItemValue = null;
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
				i18n.getString("UITabPaneBuilder.tab.metadata.label", "Metadata"), 
				i18n.getString("UITabPaneBuilder.tab.metadata.title", "Project-specific metadata"), 
				TabType.META_DATA, null
				);

		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.graphic.label", "Graphic"), 
				i18n.getString("UITabPaneBuilder.tab.graphic.title", "Graphical visualisation of the network"), 
				TabType.GRAPHIC, null
				);

		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.raw.label", "Raw data"), 
				i18n.getString("UITabPaneBuilder.tab.raw.title", "Table of raw data"), 
				TabType.RAW_DATA, null
				);

		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.properties.label", "Properties"), 
				i18n.getString("UITabPaneBuilder.tab.properties.title", "Properties of group"), 
				TabType.PROPERTIES, null
				);

		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.result.label", "Result data"), 
				i18n.getString("UITabPaneBuilder.tab.result.title", "Table of estimated data"), 
				TabType.RESULT_DATA, null
				);

		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.param.label", "Additional parameters"), 
				i18n.getString("UITabPaneBuilder.tab.param.title", "Table of additional parameters"), 
				TabType.ADDITIONAL_PARAMETER, null
				);

		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.deflection.label", "Deflections"), 
				i18n.getString("UITabPaneBuilder.tab.deflection.title", "Table of estimated deflections of the vertical"), 
				TabType.RESULT_DEFLECTION, null
				);

		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.congruence.point.label", "Congruence of points"), 
				i18n.getString("UITabPaneBuilder.tab.congruence.point.title", "Result of congruence analysis of point"), 
				TabType.RESULT_CONGRUENCE_ANALYSIS_POINT, null
				);

		this.createTab(
				i18n.getString("UITabPaneBuilder.tab.congruence.deflection.label", "Congruence of deflections"), 
				i18n.getString("UITabPaneBuilder.tab.congruence.deflection.title", "Result of congruence analysis of deflections of the vertical"), 
				TabType.RESULT_CONGRUENCE_ANALYSIS_DEFLECTION, null
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
		TreeItemType treeItemType = this.lastTreeItemValue.getItemType();
		switch(treeItemType) {
		case ROOT:
			if (tabType == TabType.META_DATA)
				node = UIMetaDataPaneBuilder.getInstance().getNode();
			else if (tabType == TabType.RESULT_DATA)
				node = UIGlobalResultPaneBuilder.getInstance().getNode();
			else if (tabType == TabType.GRAPHIC) {
				UIGraphicPaneBuilder graphicPaneBuilder = UIGraphicPaneBuilder.getInstance();
				node = graphicPaneBuilder.getPane();
				// re-draw network plot
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						graphicPaneBuilder.getLayerManager().redraw();
					}
				});
			}
			break;

		case REFERENCE_POINT_1D_LEAF:
		case STOCHASTIC_POINT_1D_LEAF:
		case DATUM_POINT_1D_LEAF:
		case NEW_POINT_1D_LEAF:
		case REFERENCE_POINT_2D_LEAF:
		case STOCHASTIC_POINT_2D_LEAF:
		case DATUM_POINT_2D_LEAF:
		case NEW_POINT_2D_LEAF:
		case REFERENCE_POINT_3D_LEAF:
		case STOCHASTIC_POINT_3D_LEAF:
		case DATUM_POINT_3D_LEAF:
		case NEW_POINT_3D_LEAF:
			PointTreeItemValue pointItemValue = (PointTreeItemValue)this.lastTreeItemValue;
			if (tabType == TabType.RAW_DATA || tabType == TabType.RESULT_DATA || tabType == TabType.RESULT_DEFLECTION || tabType == TabType.RESULT_CONGRUENCE_ANALYSIS_POINT || tabType == TabType.RESULT_CONGRUENCE_ANALYSIS_DEFLECTION) {
				TableView<PointRow> pointTableView = this.pointTableBuilder.getTable(pointItemValue);
				this.setTableColumnView(tabType, pointTableView);
				node = pointTableView;
			}
			else if (tabType == TabType.PROPERTIES) {
				UIPointPropertiesPaneBuilder pointPropertiesBuilder = UIPointPropertiesPaneBuilder.getInstance();
				node = pointPropertiesBuilder.getPointPropertiesPane(pointItemValue.getItemType()).getNode();
			}

			break;
		case LEVELING_LEAF:
		case DIRECTION_LEAF:
		case HORIZONTAL_DISTANCE_LEAF:
		case SLOPE_DISTANCE_LEAF:
		case ZENITH_ANGLE_LEAF:
		case GNSS_1D_LEAF:
		case GNSS_2D_LEAF:
		case GNSS_3D_LEAF:
			ObservationTreeItemValue observationItemValue = (ObservationTreeItemValue)this.lastTreeItemValue;

			if (tabType == TabType.RAW_DATA || tabType == TabType.RESULT_DATA) {
				switch(treeItemType) {
				case LEVELING_LEAF:
				case DIRECTION_LEAF:
				case HORIZONTAL_DISTANCE_LEAF:
				case SLOPE_DISTANCE_LEAF:
				case ZENITH_ANGLE_LEAF:
					TableView<TerrestrialObservationRow> observationTableView = this.observationTableBuilder.getTable(observationItemValue);
					this.setTableColumnView(tabType, observationTableView);
					node = observationTableView;
					break;
				case GNSS_1D_LEAF:
				case GNSS_2D_LEAF:
				case GNSS_3D_LEAF:
					TableView<GNSSObservationRow> gnssObservationTableView = this.gnssObservationTableBuilder.getTable(observationItemValue);
					this.setTableColumnView(tabType, gnssObservationTableView);
					node = gnssObservationTableView;
					break;
				default:
					break;
				}
			}
			else if (tabType == TabType.PROPERTIES) {
				UIObservationPropertiesPaneBuilder terrestrialPropertiesBuilder = UIObservationPropertiesPaneBuilder.getInstance();
				node = terrestrialPropertiesBuilder.getObservationPropertiesPane(observationItemValue.getItemType()).getNode();
			}
			else if (tabType == TabType.ADDITIONAL_PARAMETER) {
				UIAdditionalParameterTableBuilder tableBuilder = UIAdditionalParameterTableBuilder.getInstance();
				TableView<AdditionalParameterRow> table = tableBuilder.getTable();
				node = table;
			}
			break;

		case CONGRUENCE_ANALYSIS_1D_LEAF:
		case CONGRUENCE_ANALYSIS_2D_LEAF:
		case CONGRUENCE_ANALYSIS_3D_LEAF:
			CongruenceAnalysisTreeItemValue congruenceAnalysisItemValue = (CongruenceAnalysisTreeItemValue)this.lastTreeItemValue;
			if (tabType == TabType.RAW_DATA || tabType == TabType.RESULT_CONGRUENCE_ANALYSIS_POINT) {
				TableView<CongruenceAnalysisRow> congruenceAnalysisTableView = this.congruenceAnalysisTableBuilder.getTable(congruenceAnalysisItemValue);
				this.setTableColumnView(tabType, congruenceAnalysisTableView);
				node = congruenceAnalysisTableView;
			}
			else if (tabType == TabType.PROPERTIES) {
				UICongruenceAnalysisPropertiesPaneBuilder congruenceAnalysisPropertiesBuilder = UICongruenceAnalysisPropertiesPaneBuilder.getInstance();
				node = congruenceAnalysisPropertiesBuilder.getCongruenceAnalysisPropertiesPane(congruenceAnalysisItemValue.getItemType()).getNode();
			}
			else if (tabType == TabType.ADDITIONAL_PARAMETER) {
				UIAdditionalParameterTableBuilder tableBuilder = UIAdditionalParameterTableBuilder.getInstance();
				TableView<AdditionalParameterRow> table = tableBuilder.getTable();
				node = table;
			}
			break;

		default:
			node = null;
			break;
		}
		return node;
	}

	private void setTableColumnView(TabType tabType, TableView<? extends Row> tableView) {
		int columnCount = tableView.getColumns().size();

		for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
			TableColumn<? extends Row, ?> column = tableView.getColumns().get(columnIndex);
			if (column.getUserData() instanceof ColumnType) {
				ColumnType columnType = (ColumnType)column.getUserData();
				switch(columnType) {
				case VISIBLE:
					column.setVisible(true);

					break;
				case HIDDEN:
					column.setVisible(false);

					break;

				case APRIORI_TERRESTRIAL_OBSERVATION:
				case APRIORI_GNSS_OBSERVATION:
				case APRIORI_POINT_CONGRUENCE:
				case APRIORI_POINT:
				case APRIORI_DEFLECTION:
					column.setVisible(tabType == TabType.RAW_DATA);

					break;

				case APOSTERIORI_TERRESTRIAL_OBSERVATION:
				case APOSTERIORI_GNSS_OBSERVATION:
				case APOSTERIORI_POINT:
					column.setVisible(tabType == TabType.RESULT_DATA);

					break;

				case APOSTERIORI_DEFLECTION:
					column.setVisible(tabType == TabType.RESULT_DEFLECTION);

					break;

				case APOSTERIORI_POINT_CONGRUENCE:
					column.setVisible(tabType == TabType.RESULT_CONGRUENCE_ANALYSIS_POINT);

					break;

				case APOSTERIORI_DEFLECTION_CONGRUENCE:
					column.setVisible(tabType == TabType.RESULT_CONGRUENCE_ANALYSIS_DEFLECTION);

					break;

				default:
					break;
				}
			}
		}
	}

	public void setTreeItemValue(TreeItemValue treeItemValue) {
		SingleSelectionModel<Tab> selectionModel = this.tabPane.getSelectionModel();
		try {
			//selectionModel.selectedItemProperty().removeListener(this.tabSelectionChangeListener);
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

						for (TabType tabType : newTabTypes) {
							if (this.tapMap.containsKey(tabType)) {
								newTabList.add(this.tapMap.get(tabType));
								if (this.lastSelectedTab != null && this.lastSelectedTab.getUserData() == tabType) {
									selectedTab = this.tapMap.get(tabType);
								}
							}
						}
						this.tabPane.getTabs().clear();
						this.tabPane.getTabs().addAll(newTabList);
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
			//selectionModel.selectedItemProperty().addListener(this.tabSelectionChangeListener);
		}
	}
}
