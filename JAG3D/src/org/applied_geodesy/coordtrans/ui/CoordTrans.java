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
import org.applied_geodesy.coordtrans.ui.dialog.AboutDialog;
import org.applied_geodesy.coordtrans.ui.dialog.FilePathsSelectionDialog;
import org.applied_geodesy.coordtrans.ui.dialog.FormatterOptionDialog;
import org.applied_geodesy.coordtrans.ui.dialog.LeastSquaresSettingDialog;
import org.applied_geodesy.coordtrans.ui.dialog.MatrixDialog;
import org.applied_geodesy.coordtrans.ui.dialog.TestStatisticDialog;
import org.applied_geodesy.coordtrans.ui.dialog.TransformationAdjustmentDialog;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.coordtrans.ui.menu.UIMenuBuilder;
import org.applied_geodesy.coordtrans.ui.pane.UIRestrictionPaneBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIFramePositionPairTableBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIHomologousFramePositionPairTableBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIParameterTableBuilder;
import org.applied_geodesy.coordtrans.ui.tabpane.UITabPaneBuilder;
import org.applied_geodesy.coordtrans.ui.tree.TreeItemValue;
import org.applied_geodesy.coordtrans.ui.tree.UITreeBuilder;
import org.applied_geodesy.jag3d.ui.JAG3D;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.util.ImageUtils;
import org.applied_geodesy.version.coordtrans.Version;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
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
		OptionDialog.setOwner(primaryStage);
		MatrixDialog.setOwner(primaryStage);
		FilePathsSelectionDialog.setOwner(primaryStage);
		AboutDialog.setOwner(primaryStage);
		TransformationAdjustmentDialog.setOwner(primaryStage);
		LeastSquaresSettingDialog.setOwner(primaryStage);
		TestStatisticDialog.setOwner(primaryStage);
		FormatterOptionDialog.setOwner(primaryStage);
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
			adjustment.addTransformationChangeListener(menuBuilder);
			adjustment.addTransformationChangeListener(treeBuilder);
			adjustment.addTransformationChangeListener(UIRestrictionPaneBuilder.getInstance());
			adjustment.addTransformationChangeListener(UIParameterTableBuilder.getInstance());
			adjustment.addTransformationChangeListener(UIHomologousFramePositionPairTableBuilder.getInstance());
			adjustment.addTransformationChangeListener(UIFramePositionPairTableBuilder.getInstance());
			adjustment.addTransformationChangeListener(new AdjustmentTransformationChangedListener());
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
