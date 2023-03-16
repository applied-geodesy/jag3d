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

package org.applied_geodesy.coordtrans.ui;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.applied_geodesy.adjustment.transformation.TransformationAdjustment;
import org.applied_geodesy.adjustment.transformation.TransformationChangeListener;
import org.applied_geodesy.adjustment.transformation.TransformationEvent;
import org.applied_geodesy.adjustment.transformation.TransformationEvent.TransformationEventType;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.coordtrans.ui.dialog.AboutDialog;
import org.applied_geodesy.coordtrans.ui.dialog.FilePathsSelectionDialog;
import org.applied_geodesy.coordtrans.ui.dialog.MatrixDialog;
import org.applied_geodesy.coordtrans.ui.dialog.TransformationAdjustmentDialog;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.coordtrans.ui.menu.UIMenuBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIHomologousFramePositionPairTableBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIParameterTableBuilder;
import org.applied_geodesy.coordtrans.ui.tabpane.UITabPaneBuilder;
import org.applied_geodesy.coordtrans.ui.tree.TreeItemValue;
import org.applied_geodesy.coordtrans.ui.tree.UITreeBuilder;
import org.applied_geodesy.jag3d.ui.JAG3D;
import org.applied_geodesy.util.ImageUtils;
import org.applied_geodesy.version.coordtrans.Version;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
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

public class CoordTrans extends Application {
	private class AdjustmentTransformationChangedListener implements TransformationChangeListener {
		@Override
		public void transformationChanged(TransformationEvent evt) {
			adjustmentButton.setDisable(evt.getEventType() == TransformationEventType.TRANSFORMATION_MODEL_REMOVED);
		}
	}
	
	private static I18N i18n = I18N.getInstance();
	private final static String TITLE_TEMPLATE = "%s%sCoordTrans%s \u2014 Least-Squares Adjustment of Transformation Parameters";
	private static Stage primaryStage;
	private Button adjustmentButton;
	
	public static void setTitle(String title) {
		if (primaryStage != null && title != null && !title.trim().isEmpty())
			primaryStage.setTitle(String.format(Locale.ENGLISH, TITLE_TEMPLATE, title, " \u2014 ", (Version.isReleaseCandidate() ? " (RC)" : "")));
		else if (primaryStage != null)
			primaryStage.setTitle(String.format(Locale.ENGLISH, TITLE_TEMPLATE, "", "", (Version.isReleaseCandidate() ? " (RC)" : "")));
	}

	public static void close() {
		primaryStage.close();
	}
	
	public static Stage getStage() {
		return primaryStage;
	}
	
	private void setHostServices() {
		
	}
	
	private void setStageToDialogs(Stage primaryStage) {
		MatrixDialog.setOwner(primaryStage);
		FilePathsSelectionDialog.setOwner(primaryStage);
		AboutDialog.setOwner(primaryStage);
		TransformationAdjustmentDialog.setOwner(primaryStage);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		if (CoordTrans.primaryStage != primaryStage) {

			CoordTrans.primaryStage = primaryStage;
			
			UITabPaneBuilder tabPaneBuilder = UITabPaneBuilder.getInstance();
			UIMenuBuilder menuBuilder = UIMenuBuilder.getInstance();
			UITreeBuilder treeBuilder = UITreeBuilder.getInstance();
			
			this.adjustmentButton = new Button(i18n.getString("CoordTrans.button.adjust.label", "Adjust transformation"));
			this.adjustmentButton.setTooltip(new Tooltip(i18n.getString("CoordTrans.button.adjust.tooltip", "Start transformation adjustment process")));
			this.adjustmentButton.setOnAction(new EventHandler<ActionEvent>() { 
				@Override
				public void handle(ActionEvent event) {	   
					TransformationAdjustmentDialog.show();
				}
			});
			this.adjustmentButton.setDisable(true);

			TabPane tabPane = tabPaneBuilder.getTabPane();
			TreeView<TreeItemValue<?>> tree = treeBuilder.getTree();
			
			SplitPane splitPane = new SplitPane();
			splitPane.setOrientation(Orientation.HORIZONTAL);
			splitPane.getItems().addAll(tree, tabPane);
			splitPane.setDividerPositions(0.30);
			SplitPane.setResizableWithParent(tree, false);
			
			
			DropShadow ds = new DropShadow();
			ds.setOffsetY(0.5f);
			ds.setColor(Color.gray(0.8));

			Text applicationName = new Text();
			applicationName.setEffect(ds);
			applicationName.setCache(true);
			applicationName.setFill(Color.GREY);
			applicationName.setText("Universal\u00B7Coordinate\u00B7Transformation");
			applicationName.setFont(Font.font("SansSerif", FontWeight.NORMAL, 17));

			Region spacer = new Region();
			HBox hbox = new HBox(10);
			hbox.setPadding(new Insets(5, 10, 5, 15));
			HBox.setHgrow(spacer, Priority.ALWAYS);
			hbox.getChildren().addAll(applicationName, spacer, this.adjustmentButton);


			BorderPane border = new BorderPane();
			border.setPrefSize(900, 650);
			border.setTop(menuBuilder.getMenuBar());
			border.setCenter(splitPane);
			border.setBottom(hbox);

			Scene scene = new Scene(border);
			
			// add external style definitions
			try {
				URL cssURL = null;
				if ((cssURL = JAG3D.class.getClassLoader().getResource("css/")) != null) {
					cssURL = cssURL.toURI().resolve("coordtrans.css").toURL();
					if (Files.exists(Paths.get(cssURL.toURI()))) 
						scene.getStylesheets().add(cssURL.toExternalForm());
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			// add icons
			try {
				primaryStage.getIcons().addAll(
						ImageUtils.getImage("CoordTrans_16x16.png"),
						ImageUtils.getImage("CoordTrans_32x32.png"),
						ImageUtils.getImage("CoordTrans_64x64.png")
						);
			} 
			catch(Exception e) {
				e.printStackTrace();
			}
			primaryStage.setScene(scene);

			setTitle(null);

			primaryStage.show();
			primaryStage.toFront();

			this.setHostServices();
			this.setStageToDialogs(primaryStage);
			
			// add listener to UI components
			TransformationAdjustment adjustment = treeBuilder.getTransformationAdjustment();
			adjustment.addTransformationChangeListener(treeBuilder);
			adjustment.addTransformationChangeListener(UIParameterTableBuilder.getInstance());
			adjustment.addTransformationChangeListener(UIHomologousFramePositionPairTableBuilder.getInstance());
			adjustment.addTransformationChangeListener(new AdjustmentTransformationChangedListener());
			
			
			TableView<HomologousFramePositionPair> table = UIHomologousFramePositionPairTableBuilder.getInstance().getTable();
			ObservableList<HomologousFramePositionPair> tableModel = UIHomologousFramePositionPairTableBuilder.getInstance().getTableModel(table);
					
			tableModel.addAll(
			new HomologousFramePositionPair("1", 4157222.543, 664789.307, 4774952.099,   4157870.237, 664818.678, 4775416.524),
			new HomologousFramePositionPair("2", 4149043.336, 688836.443, 4778632.188,   4149691.049, 688865.785, 4779096.588),
			new HomologousFramePositionPair("3", 4172803.511, 690340.078, 4758129.701,   4173451.354, 690369.375, 4758594.075),
			new HomologousFramePositionPair("4", 4177148.376, 642997.635, 4760764.800,   4177796.064, 643026.700, 4761228.899),
			new HomologousFramePositionPair("5", 4137012.190, 671808.029, 4791128.215,   4137659.549, 671837.337, 4791592.531),
			new HomologousFramePositionPair("6", 4146292.729, 666952.887, 4783859.856,   4146940.228, 666982.151, 4784324.099),
			new HomologousFramePositionPair("7", 4138759.902, 702670.738, 4785552.196,   4139407.506, 702700.227, 4786016.645)

//			new HomologousFramePositionPair("1", 585.435,  755.475, 102.520, 929.580, 422.800, -0.210),
//			new HomologousFramePositionPair("2", 553.175,  988.105, 104.190, 575.360, 480.900,  2.370),
//			new HomologousFramePositionPair("3", 424.045,  785.635, 106.125, 812.370, 200.820, -0.240),
//			new HomologousFramePositionPair("4", 394.950, 1061.700, 106.070, 396.280, 283.240,  0.410),
			
//			new HomologousFramePositionPair("1", 1094.883,  820.085, 109.821, 10037.81, 5262.09, 772.04),
//			new HomologousFramePositionPair("2",  503.891, 1598.698, 117.685, 10956.68, 5128.17, 783.00),
//			new HomologousFramePositionPair("3", 2349.343,  207.658, 151.387,  8780.08, 4840.29, 782.62),
//			new HomologousFramePositionPair("4", 1395.320, 1348.853, 215.261, 10185.80, 4700.21, 851.32)
			);
		}
	}

	
	public static void main(String[] args) {
		//Locale.setDefault(Locale.GERMAN);
		try {
			Logger[] loggers = new Logger[] {
					Logger.getLogger("hsqldb.db"),
					Logger.getLogger("com.github.fommil.netlib.LAPACK"),
					Logger.getLogger("com.github.fommil.netlib.BLAS")
			};

			for (Logger logger : loggers) {
				logger.setUseParentHandlers(false);
				logger.setLevel(Level.OFF);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		Application.launch(CoordTrans.class, args);
	}
}
