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

package org.applied_geodesy.juniform.ui.propertiespane;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.function.Predicate;

import org.applied_geodesy.adjustment.geometry.GeometricPrimitive;
import org.applied_geodesy.adjustment.geometry.point.FeaturePoint;
import org.applied_geodesy.util.ObservableUniqueList;
import org.applied_geodesy.juniform.ui.i18n.I18N;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class UIPointSelectionPaneBuilder {
	private enum SelectionEventType {
		ADD, REMOVE, ADD_ALL, REMOVE_ALL;
	}
	
	private enum ListType {
		GLOBAL_POINTS, GEOMETRY_POINTS;
	}
	
	private class FilterModeChangeListener implements ChangeListener<Boolean> {
		private final ListType listType;
		private FilterModeChangeListener(ListType listType) {
			this.listType = listType;
		}
		
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			filterList(this.listType, null);
		}
	}
	
	private class FilterTextChangeListener implements ChangeListener<String> {
		private final ListType listType;
		private FilterTextChangeListener(ListType listType) {
			this.listType = listType;
		}
		
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			filterList(this.listType, newValue);
		}
	}
	
	private class PointSelectionEventHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() instanceof Button && ((Button)event.getSource()).getUserData() instanceof SelectionEventType) {
				Button button = (Button)event.getSource();
				SelectionEventType selectionEventType = (SelectionEventType)button.getUserData();
				
				switch (selectionEventType) {
				case ADD:
					Collection<FeaturePoint> selectedSourceItems = new LinkedHashSet<FeaturePoint>(sourceListView.getSelectionModel().getSelectedItems());
					sourcePointList.removeAll(selectedSourceItems);
					targetPointList.addAll(selectedSourceItems);
					targetListView.getSelectionModel().clearSelection();
					break;
					
				case ADD_ALL:
					Collection<FeaturePoint> filteredSourceItems = new LinkedHashSet<FeaturePoint>(filteredSourcePointList);
					sourcePointList.removeAll(new HashSet<>(filteredSourceItems));
					targetPointList.addAll(filteredSourceItems);
					break;
					
				case REMOVE:
					Collection<FeaturePoint> selectedTargetItems = new LinkedHashSet<FeaturePoint>(targetListView.getSelectionModel().getSelectedItems());
					targetPointList.removeAll(selectedTargetItems);
					sourcePointList.addAll(selectedTargetItems);
					sourceListView.getSelectionModel().clearSelection();
					break;
					
				case REMOVE_ALL:
					Collection<FeaturePoint> filteredTargetItems = new LinkedHashSet<FeaturePoint>(filteredTargetPointList);
					targetPointList.removeAll(new HashSet<>(filteredTargetItems));
					sourcePointList.addAll(filteredTargetItems);
					break;				
				}
			}
		}
	}
	
	private static UIPointSelectionPaneBuilder pointSelectionPaneBuilder = new UIPointSelectionPaneBuilder();
	private Node pointSelectionNode = null;
	private ListView<FeaturePoint> sourceListView;
	private ListView<FeaturePoint> targetListView;
	
	private FilteredList<FeaturePoint> filteredTargetPointList;
	private FilteredList<FeaturePoint> filteredSourcePointList;
	
	private ObservableUniqueList<FeaturePoint> targetPointList;
	private ObservableList<FeaturePoint> sourcePointList;
	
	private CheckBox regExpSourceListFilterCheckBox;
	private CheckBox regExpTargetListFilterCheckBox;
	private TextField sourceListFilterTextField;
	private TextField targetListFilterTextField;
	
	private I18N i18n = I18N.getInstance();
	
	private UIPointSelectionPaneBuilder() {
		super();
	}

	public static UIPointSelectionPaneBuilder getInstance() {
		pointSelectionPaneBuilder.init();
		return pointSelectionPaneBuilder;
	}
	
	public Node getNode(ObservableList<FeaturePoint> sourcePoints, GeometricPrimitive geometricPrimitive) {
		this.targetPointList = geometricPrimitive.getFeaturePoints();
		this.filteredTargetPointList = new FilteredList<FeaturePoint>(this.targetPointList);
				
		this.filteredSourcePointList = new FilteredList<FeaturePoint>(sourcePoints);
		this.filteredSourcePointList.setPredicate(
				new Predicate<FeaturePoint>(){
					public boolean test(FeaturePoint featurePoint){
						return !targetPointList.contains(featurePoint);
					}
				}
		);

		this.sourcePointList = FXCollections.observableArrayList(this.filteredSourcePointList);
		this.filteredSourcePointList = new FilteredList<FeaturePoint>(this.sourcePointList);
		
		this.sourceListView.setItems(this.filteredSourcePointList);
		this.targetListView.setItems(this.filteredTargetPointList);
		
		// apply last filter to point list
		for (ListType listType : ListType.values())
			filterList(listType, null);

		return this.pointSelectionNode;
	}
	
	
	
	private void init() {
		if (this.pointSelectionNode != null)
			return;
		
		// List of points
		this.sourceListView = this.createListView();
		this.targetListView = this.createListView();
		
		// usnig regexp
		this.regExpSourceListFilterCheckBox = this.createCheckBox(
				i18n.getString("UIPointSelectionPaneBuilder.mode.regex.global.label", "Regular expression"), 
				i18n.getString("UIPointSelectionPaneBuilder.mode.regex.global.tooltip", "If selected, regular expression mode will be applied to global point list"),
				ListType.GLOBAL_POINTS);
		
		this.regExpTargetListFilterCheckBox = this.createCheckBox(
				i18n.getString("UIPointSelectionPaneBuilder.mode.regex.geometry.label", "Regular expression"), 
				i18n.getString("UIPointSelectionPaneBuilder.mode.regex.geometry.tooltip", "If selected, regular expression mode will be applied to geometry point list"),
				ListType.GEOMETRY_POINTS);
		
		// Filtering textfields
		this.sourceListFilterTextField = this.createTextField(
				i18n.getString("UIPointSelectionPaneBuilder.filter.list.global.prompt", "Enter filter sequence"),
				i18n.getString("UIPointSelectionPaneBuilder.filter.list.global.tooltip", "Filtering global point list by character sequence"),
				ListType.GLOBAL_POINTS);
		
		this.targetListFilterTextField = this.createTextField(
				i18n.getString("UIPointSelectionPaneBuilder.filter.list.geometry.prompt", "Enter filter sequence"),
				i18n.getString("UIPointSelectionPaneBuilder.filter.list.geometry.tooltip", "Filtering geometry point list by character sequence"),
				ListType.GEOMETRY_POINTS);
		
		// Selection buttons		
		PointSelectionEventHandler pointSelectionEventHandler = new PointSelectionEventHandler();
		Button addSelectedPointsButton = this.createButton(
				i18n.getString("UIPointSelectionPaneBuilder.add.selection.label", "Add selection \u25B7"),
				i18n.getString("UIPointSelectionPaneBuilder.add.selection.tooltip", "Add selected points to geometric primitive."),
				pointSelectionEventHandler, SelectionEventType.ADD);
			
		Button removeSelectedPointsButton = this.createButton(
				i18n.getString("UIPointSelectionPaneBuilder.remove.selection.label", "\u25C1 Remove selection"),
				i18n.getString("UIPointSelectionPaneBuilder.remove.selection.tooltip", "Remove selected points from geometric primitive"),
				pointSelectionEventHandler, SelectionEventType.REMOVE);
		
		Button addAllPointsButton = this.createButton(
				i18n.getString("UIPointSelectionPaneBuilder.add.all.label", "Add all \u25B6"),
				i18n.getString("UIPointSelectionPaneBuilder.add.all.tooltip", "Add all points to geometric primitive"),
				pointSelectionEventHandler, SelectionEventType.ADD_ALL);
			
		Button removeAllPointsButton = this.createButton(
				i18n.getString("UIPointSelectionPaneBuilder.remove.all.label", "\u25C0 Remove all"),
				i18n.getString("UIPointSelectionPaneBuilder.remove.all.tooltip", "Remove all points from geometric primitive"),
				pointSelectionEventHandler, SelectionEventType.REMOVE_ALL);
				
		TitledPane sourceTitledPane = this.createTitledPane(i18n.getString("UIPointSelectionPaneBuilder.pointlist.global.title", "Points of project"));
		TitledPane targetTitledPane = this.createTitledPane(i18n.getString("UIPointSelectionPaneBuilder.pointlist.geometry.title", "Points of geometry"));
		
		sourceTitledPane.setContent(this.sourceListView);
		targetTitledPane.setContent(this.targetListView);

		VBox topVbox = new VBox();
		topVbox.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		topVbox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		VBox bottomVbox = new VBox();
		bottomVbox.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		bottomVbox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

		VBox buttonVbox = new VBox();
		buttonVbox.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		buttonVbox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		                                            // top, right, bottom, left 
		VBox.setMargin(addSelectedPointsButton,     new Insets( 5, 10,  5, 10));
		VBox.setMargin(removeSelectedPointsButton,  new Insets( 5, 10, 10, 10));
		VBox.setMargin(addAllPointsButton,          new Insets(10, 10,  5, 10));
		VBox.setMargin(removeAllPointsButton,       new Insets( 5, 10,  5, 10));
		
		VBox.setVgrow(addSelectedPointsButton, Priority.ALWAYS);
		VBox.setVgrow(removeSelectedPointsButton, Priority.ALWAYS);
		VBox.setVgrow(addAllPointsButton, Priority.ALWAYS);
		VBox.setVgrow(removeAllPointsButton, Priority.ALWAYS);
		
		buttonVbox.getChildren().addAll(
				addSelectedPointsButton, 
				removeSelectedPointsButton, 
				addAllPointsButton, 
				removeAllPointsButton
		);
		
		GridPane gridPane = this.createGridPane();
		
		GridPane.setMargin(sourceTitledPane, new Insets(2, 5, 2, 2)); // top, right, bottom, left 
		GridPane.setMargin(targetTitledPane, new Insets(2, 2, 2, 5)); // top, right, bottom, left 
		
		GridPane.setMargin(this.regExpSourceListFilterCheckBox, new Insets(0, 5, 2, 2)); // top, right, bottom, left 
		GridPane.setMargin(this.regExpTargetListFilterCheckBox, new Insets(0, 2, 2, 5)); // top, right, bottom, left
		
		GridPane.setMargin(this.sourceListFilterTextField, new Insets(2, 5, 0, 2)); // top, right, bottom, left 
		GridPane.setMargin(this.targetListFilterTextField, new Insets(2, 2, 0, 5)); // top, right, bottom, left
		
		gridPane.setAlignment(Pos.TOP_CENTER);
//		gridPane.setGridLinesVisible(true);
			
		GridPane.setHgrow(this.sourceListFilterTextField, Priority.ALWAYS);
		GridPane.setVgrow(this.sourceListFilterTextField, Priority.NEVER);
		
		GridPane.setHgrow(this.targetListFilterTextField, Priority.ALWAYS);
		GridPane.setVgrow(this.targetListFilterTextField, Priority.NEVER);
		
		GridPane.setHgrow(this.regExpSourceListFilterCheckBox, Priority.NEVER);
		GridPane.setVgrow(this.regExpSourceListFilterCheckBox, Priority.NEVER);
		
		GridPane.setHgrow(this.regExpTargetListFilterCheckBox, Priority.NEVER);
		GridPane.setVgrow(this.regExpTargetListFilterCheckBox, Priority.NEVER);
		

		GridPane.setHgrow(sourceTitledPane, Priority.ALWAYS);
		GridPane.setVgrow(sourceTitledPane, Priority.ALWAYS);

		GridPane.setHgrow(targetTitledPane, Priority.ALWAYS);
		GridPane.setVgrow(targetTitledPane, Priority.ALWAYS);
		
		GridPane.setHgrow(topVbox, Priority.NEVER);
		GridPane.setVgrow(topVbox, Priority.ALWAYS);
		
		GridPane.setHgrow(buttonVbox, Priority.NEVER);
		GridPane.setVgrow(buttonVbox, Priority.NEVER);
		
		GridPane.setHgrow(bottomVbox, Priority.NEVER);
		GridPane.setVgrow(bottomVbox, Priority.ALWAYS);
		
		int row = 0;
		
		gridPane.add(this.sourceListFilterTextField, 0, row);  // col, row, colspan, rowspan
		gridPane.add(this.targetListFilterTextField, 2, row++);
		
		gridPane.add(this.regExpSourceListFilterCheckBox, 0, row);  
		gridPane.add(this.regExpTargetListFilterCheckBox, 2, row++);
		
		gridPane.add(sourceTitledPane, 0, row, 1, 3);  // col, row, colspan, rowspan
		gridPane.add(targetTitledPane, 2, row, 1, 3);

		gridPane.add(topVbox,    1, row++);
		gridPane.add(buttonVbox, 1, row++);
		gridPane.add(bottomVbox, 1, row++);
		
		gridPane.setPadding(new Insets(10, 10, 10, 10)); // top, right, bottom, left 
		
//		ScrollPane scroller = new ScrollPane(gridPane);
//		scroller.setPadding(new Insets(10, 15, 10, 15)); // top, right, bottom, left 
//		scroller.setFitToHeight(true);
//		scroller.setFitToWidth(true);
		this.pointSelectionNode = gridPane;
	}
	
	
	private Button createButton(String title, String tooltip, PointSelectionEventHandler pointSelectionEventHandler, SelectionEventType selectionEventType) {
		Label label = new Label(title);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE);
		label.setAlignment(Pos.CENTER);
		label.setPadding(new Insets(0,0,0,3));
		Button button = new Button();
		button.setGraphic(label);
		button.setTooltip(new Tooltip(tooltip));
		button.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		button.setUserData(selectionEventType);
		button.setOnAction(pointSelectionEventHandler);
		return button;
	}
	
	private ListView<FeaturePoint> createListView() {
		ListView<FeaturePoint> listView = new ListView<FeaturePoint>();
		listView.setCellFactory(createFeaturePointCellFactory());
		listView.setMinSize(50, 100);
		listView.setPrefSize(50, 100);
		listView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		ListView<String> placeholderListView = new ListView<String>();
		placeholderListView.setDisable(true);
		placeholderListView.getItems().add(new String());
		listView.setPlaceholder(placeholderListView);
		
		return listView;
	}
	
	public static Callback<ListView<FeaturePoint>, ListCell<FeaturePoint>> createFeaturePointCellFactory() {
		return new Callback<ListView<FeaturePoint>, ListCell<FeaturePoint>>() {
			@Override
			public ListCell<FeaturePoint> call(ListView<FeaturePoint> listView) {
				return new ListCell<FeaturePoint>() { 
					@Override
					protected void updateItem(FeaturePoint featurePoint, boolean empty) {
						super.updateItem(featurePoint, empty);

						if (empty || featurePoint == null || featurePoint.getName() == null) {
							this.setText(null);
							this.setGraphic(null);
						}
						else 
							this.setText(featurePoint.getName());
					}
				};
			};
		};
	}
	
	private void filterList(ListType listType, String value) {
		FilteredList<FeaturePoint> filteredList = null;
		CheckBox regExpFilterCheckBox = null;
		TextField listFilterTextField = null;
		switch (listType) {
		case GLOBAL_POINTS:
			regExpFilterCheckBox = regExpSourceListFilterCheckBox;
			listFilterTextField  = sourceListFilterTextField;
			filteredList         = filteredSourcePointList;
			break;
		case GEOMETRY_POINTS:
			regExpFilterCheckBox = regExpTargetListFilterCheckBox;
			listFilterTextField  = targetListFilterTextField;
			filteredList         = filteredTargetPointList;
			break;		
		}

		if (filteredList != null && listFilterTextField != null) {
			final String filterText = value == null || value.isBlank() ? listFilterTextField.getText() : value;
			if (filterText == null || filterText.isBlank()) {
				filteredList.setPredicate(null);
			}
			else {				
				final boolean regExp = regExpFilterCheckBox != null && regExpFilterCheckBox.isSelected();
				filteredList.setPredicate(
					new Predicate<FeaturePoint>(){
						public boolean test(FeaturePoint featurePoint){
							if (!regExp)
								return featurePoint.getName().contains(filterText);
							else {
								try {
									return featurePoint.getName().matches(filterText);
								} catch(Exception e) {}
								return true;
							}
						}
				});
			}
		}
	}
	
	private TitledPane createTitledPane(String title) {
		TitledPane titledPane = new TitledPane();
		titledPane.setMinSize(Control.USE_PREF_SIZE, 200);
		titledPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		titledPane.setCollapsible(false);
		titledPane.setAnimated(false);
		titledPane.setText(title);
		return titledPane;
	}
	
	private TextField createTextField(String promptText, String tooltipText, ListType listType) {
		TextField field = new TextField();
		field.setTooltip(new Tooltip(tooltipText));
		field.setPromptText(promptText);
		field.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		field.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE);
		field.textProperty().addListener(new FilterTextChangeListener(listType));
		return field;
	}
	
	private CheckBox createCheckBox(String text, String tooltip, ListType listType) {
		Label label = new Label(text);
		label.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		label.setPadding(new Insets(0,0,0,3));
		CheckBox checkBox = new CheckBox();
		checkBox.setGraphic(label);
		checkBox.setTooltip(new Tooltip(tooltip));
		checkBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		checkBox.setMaxSize(Double.MAX_VALUE, Control.USE_PREF_SIZE);
		checkBox.selectedProperty().addListener(new FilterModeChangeListener(listType));
		return checkBox;
	}
	
	private GridPane createGridPane() {
		GridPane gridPane = new GridPane();
		gridPane.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		gridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		return gridPane;
	}
}
