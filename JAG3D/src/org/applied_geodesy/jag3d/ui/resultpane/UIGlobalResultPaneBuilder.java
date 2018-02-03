package org.applied_geodesy.jag3d.ui.resultpane;

import org.applied_geodesy.jag3d.ui.graphic.layer.dialog.ArrowSymbolTypeListCell;
import org.applied_geodesy.jag3d.ui.graphic.layer.symbol.ArrowSymbolType;
import org.applied_geodesy.jag3d.ui.table.UITestStatisticTableBuilder;
import org.applied_geodesy.jag3d.ui.table.UIVarianceComponentTableBuilder;
import org.applied_geodesy.jag3d.ui.table.row.TestStatisticRow;
import org.applied_geodesy.jag3d.ui.table.row.VarianceComponentRow;
import org.applied_geodesy.util.i18.I18N;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class UIGlobalResultPaneBuilder {
	private static UIGlobalResultPaneBuilder resultPaneBuilder = new UIGlobalResultPaneBuilder();
	private static I18N i18n = I18N.getInstance();
	
	
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


		//Label testStatisticLabel = new Label(i18n.getString("UIGlobalResultPaneBuilder.teststatistic.label", "Derived teststatistics"));
		// Label varianceComponentLabel = new Label(i18n.getString("UIGlobalResultPaneBuilder.variance_component.label", "Variance components estimation"));
		
		TableView<TestStatisticRow> testStatisticTable = UITestStatisticTableBuilder.getInstance().getTable();
		testStatisticTable.setUserData(GlobalResultType.TEST_STATISTIC);
		testStatisticTable.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		TableView<VarianceComponentRow> varianceComponentTable = UIVarianceComponentTableBuilder.getInstance().getTable();
		varianceComponentTable.setUserData(GlobalResultType.VARIANCE_COMPONENT);
		varianceComponentTable.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		StackPane contenPane = new StackPane();
		contenPane.setPadding(new Insets(10, 50, 20, 50)); // oben, rechts, unten, links
		contenPane.getChildren().addAll(testStatisticTable, varianceComponentTable);
		varianceComponentTable.setVisible(false);

		ComboBox<Node> paneSwitcherComboBox = new ComboBox<Node>();
		paneSwitcherComboBox.setCellFactory(new Callback<ListView<Node>, ListCell<Node>>() {
            @Override 
            public ListCell<Node> call(ListView<Node> param) {
				return new GlobalResultTypeListCell();
            }
		});
		paneSwitcherComboBox.setButtonCell(new GlobalResultTypeListCell());
		paneSwitcherComboBox.getItems().addAll(testStatisticTable, varianceComponentTable);
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
		
		Region spacer = new Region();
		HBox hbox = new HBox(10);
		hbox.setPadding(new Insets(15, 50, 5, 0));
		HBox.setHgrow(spacer, Priority.ALWAYS);
		hbox.getChildren().addAll(spacer, paneSwitcherComboBox);

		BorderPane borderPane = new BorderPane();
		borderPane.setTop(hbox);
		borderPane.setCenter(contenPane);
		
		this.resultDataNode = borderPane;
	}
	

}
