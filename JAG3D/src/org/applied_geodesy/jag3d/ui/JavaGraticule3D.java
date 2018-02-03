package org.applied_geodesy.jag3d.ui;

import java.util.Locale;

import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.dialog.AboutDialog;
import org.applied_geodesy.jag3d.ui.dialog.ApproximationValuesDialog;
import org.applied_geodesy.jag3d.ui.dialog.AverageDialog;
import org.applied_geodesy.jag3d.ui.dialog.CongruentPointDialog;
import org.applied_geodesy.jag3d.ui.dialog.FormatterOptionDialog;
import org.applied_geodesy.jag3d.ui.dialog.LeastSquaresSettingDialog;
import org.applied_geodesy.jag3d.ui.dialog.NetworkAdjustmentDialog;
import org.applied_geodesy.jag3d.ui.dialog.OptionDialog;
import org.applied_geodesy.jag3d.ui.dialog.ProjectionDialog;
import org.applied_geodesy.jag3d.ui.dialog.RankDefectDialog;
import org.applied_geodesy.jag3d.ui.dialog.SearchAndReplaceDialog;
import org.applied_geodesy.jag3d.ui.dialog.TestStatisticDialog;
import org.applied_geodesy.jag3d.ui.graphic.layer.dialog.LayerManagerDialog;
import org.applied_geodesy.jag3d.ui.io.DefaultFileChooser;
import org.applied_geodesy.jag3d.ui.menu.UIMenuBuilder;
import org.applied_geodesy.jag3d.ui.tabpane.UITabPaneBuilder;
import org.applied_geodesy.jag3d.ui.tree.TreeItemValue;
import org.applied_geodesy.jag3d.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.ImageUtils;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class JavaGraticule3D extends Application {
	private final static String TITLE_TEMPLATE = "%s%sJAG3D \u00B7 Least-Squares Adjustment \u0026 Deformation Analysis \u00B7";
	private static Stage primaryStage;
	
	public static void setTitle(String title) {
		if (primaryStage != null && title != null && !title.trim().isEmpty())
			primaryStage.setTitle(String.format(Locale.ENGLISH, TITLE_TEMPLATE, title, " \u2014 "));
		else if (primaryStage != null)
			primaryStage.setTitle(String.format(Locale.ENGLISH, TITLE_TEMPLATE, "", ""));
	}
	
	public static void close() {
		primaryStage.close();
	}
	
	private void setStageToDialogs(Stage primaryStage) {
		OptionDialog.setOwner(primaryStage);
		DefaultFileChooser.setStage(primaryStage);
		NetworkAdjustmentDialog.setOwner(primaryStage);
		FormatterOptionDialog.setOwner(primaryStage);
		TestStatisticDialog.setOwner(primaryStage);
		ProjectionDialog.setOwner(primaryStage);
		RankDefectDialog.setOwner(primaryStage);
		CongruentPointDialog.setOwner(primaryStage);
		AverageDialog.setOwner(primaryStage);
		ApproximationValuesDialog.setOwner(primaryStage);
		LeastSquaresSettingDialog.setOwner(primaryStage);
		LayerManagerDialog.setOwner(primaryStage);
		SearchAndReplaceDialog.setOwner(primaryStage);
		AboutDialog.setOwner(primaryStage);
		AboutDialog.setHostServices(this.getHostServices());
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		JavaGraticule3D.primaryStage = primaryStage;
		
		UIMenuBuilder menuBuilder = UIMenuBuilder.getInstance();
		UITabPaneBuilder tabPaneBuilder = UITabPaneBuilder.getInstance();
		UITreeBuilder treeBuilder = UITreeBuilder.getInstance();
		
		TabPane tabPane = tabPaneBuilder.getTabPane();
		TreeView<TreeItemValue> tree = treeBuilder.getTree();
		
		SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.HORIZONTAL);
		splitPane.getItems().addAll(tree, tabPane);
		splitPane.setDividerPositions(0.30);
		SplitPane.setResizableWithParent(tree, false);
		
		BorderPane border = new BorderPane();
		border.setPrefSize(900, 650);
		border.setTop(menuBuilder.getMenuBar());
		border.setCenter(splitPane);
		
		UITreeBuilder.getInstance().getTree().getSelectionModel().clearSelection();
		UITreeBuilder.getInstance().getTree().getSelectionModel().selectFirst();
		
		Button adjusmentButton = new Button("Start adjustment process");
		adjusmentButton.setOnAction(new EventHandler<ActionEvent>() {
			 
		    @Override
		    public void handle(ActionEvent event) {	    	
		    	NetworkAdjustmentDialog.show();
		    }
		});
		
		DropShadow ds = new DropShadow();
		ds.setOffsetY(0.5f);
		ds.setColor(Color.gray(0.8));
		 
		Text applicationName = new Text();
		applicationName.setEffect(ds);
		applicationName.setCache(true);
		applicationName.setFill(Color.GREY);
		applicationName.setText("Java\u00B7Applied\u00B7Geodesy\u00B73D");
		applicationName.setFont(Font.font("SansSerif", FontWeight.NORMAL, 17));

		Region spacer = new Region();
		HBox hbox = new HBox(10);
		hbox.setPadding(new Insets(5, 10, 5, 15));
		HBox.setHgrow(spacer, Priority.ALWAYS);
		hbox.getChildren().addAll(applicationName, spacer, adjusmentButton);
		border.setBottom(hbox);

		Scene scene = new Scene(border);

		try {
			primaryStage.getIcons().addAll(
					ImageUtils.getImage("JAG3D_64x64.png"),
					ImageUtils.getImage("JAG3D_32x32.png"),
					ImageUtils.getImage("JAG3D_16x16.png")
			);
		} 
		catch(Exception e) {}
		primaryStage.setScene(scene);
		this.setStageToDialogs(primaryStage);
		setTitle(null);
		
		
		primaryStage.show();
	}
	
	public void stop() throws Exception {
		SQLManager.getInstance().closeDataBase();
		super.stop();
	}

	public static void main(String[] args) {
		Application.launch(args);
    }
}
