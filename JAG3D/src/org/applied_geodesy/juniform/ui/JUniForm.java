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

package org.applied_geodesy.juniform.ui;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.applied_geodesy.adjustment.geometry.FeatureAdjustment;
import org.applied_geodesy.adjustment.geometry.FeatureChangeListener;
import org.applied_geodesy.adjustment.geometry.FeatureEvent;
import org.applied_geodesy.adjustment.geometry.FeatureEvent.FeatureEventType;
import org.applied_geodesy.jag3d.ui.JAG3D;
import org.applied_geodesy.juniform.io.writer.FTLReport;
import org.applied_geodesy.juniform.ui.dialog.AboutDialog;
import org.applied_geodesy.juniform.ui.dialog.AverageRestrictionDialog;
import org.applied_geodesy.juniform.ui.dialog.FeatureAdjustmentDialog;
import org.applied_geodesy.juniform.ui.dialog.FeatureDialog;
import org.applied_geodesy.juniform.ui.dialog.FeaturePointRestrictionDialog;
import org.applied_geodesy.juniform.ui.dialog.FormatterOptionDialog;
import org.applied_geodesy.juniform.ui.dialog.GeometricPrimitiveDialog;
import org.applied_geodesy.juniform.ui.dialog.LeastSquaresSettingDialog;
import org.applied_geodesy.juniform.ui.dialog.MatrixDialog;
import org.applied_geodesy.juniform.ui.dialog.ProductSumRestrictionDialog;
import org.applied_geodesy.juniform.ui.dialog.QuantilesDialog;
import org.applied_geodesy.juniform.ui.dialog.ReadFileProgressDialog;
import org.applied_geodesy.juniform.ui.dialog.RestrictionDialog;
import org.applied_geodesy.juniform.ui.dialog.RestrictionTypeDialog;
import org.applied_geodesy.juniform.ui.dialog.TestStatisticDialog;
import org.applied_geodesy.juniform.ui.dialog.TrigonometricRestrictionDialog;
import org.applied_geodesy.juniform.ui.dialog.UnknownParameterDialog;
import org.applied_geodesy.juniform.ui.dialog.UnknownParameterTypeDialog;
import org.applied_geodesy.juniform.ui.dialog.VarianceComponentsDialog;
import org.applied_geodesy.juniform.ui.dialog.VectorAngleRestrictionDialog;
import org.applied_geodesy.juniform.ui.i18n.I18N;
import org.applied_geodesy.juniform.ui.menu.UIMenuBuilder;
import org.applied_geodesy.juniform.ui.table.UIParameterTableBuilder;
import org.applied_geodesy.juniform.ui.table.UIPointTableBuilder;
import org.applied_geodesy.juniform.ui.tabpane.UITabPaneBuilder;
import org.applied_geodesy.juniform.ui.tree.TreeItemValue;
import org.applied_geodesy.juniform.ui.tree.UITreeBuilder;
import org.applied_geodesy.ui.dialog.OptionDialog;
import org.applied_geodesy.util.ImageUtils;
import org.applied_geodesy.version.juniform.Version;

import javafx.application.Application;
import javafx.application.HostServices;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class JUniForm extends Application {
	private class AdjustmentFeatureChangedListener implements FeatureChangeListener {
		@Override
		public void featureChanged(FeatureEvent evt) {
			adjustmentButton.setDisable(evt.getEventType() == FeatureEventType.FEATURE_REMOVED);
		}
	}
	
	private final static String TITLE_TEMPLATE = "%s%sJUniForm%s \u00B7 Curves and Surfaces Fitting \u00B7";
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
		HostServices hostServices = this.getHostServices();
		FTLReport.setHostServices(hostServices);
		AboutDialog.setHostServices(hostServices);
	}
	
	private void setStageToDialogs(Stage primaryStage) {
		OptionDialog.setOwner(primaryStage);
		TestStatisticDialog.setOwner(primaryStage);
		UnknownParameterDialog.setOwner(primaryStage);
		UnknownParameterTypeDialog.setOwner(primaryStage);
		RestrictionDialog.setOwner(primaryStage);
		RestrictionTypeDialog.setOwner(primaryStage);
		AverageRestrictionDialog.setOwner(primaryStage);
		ProductSumRestrictionDialog.setOwner(primaryStage);
		FeatureDialog.setOwner(primaryStage);
		GeometricPrimitiveDialog.setOwner(primaryStage);
		MatrixDialog.setOwner(primaryStage);
		FeatureAdjustmentDialog.setOwner(primaryStage);
		LeastSquaresSettingDialog.setOwner(primaryStage);
		ReadFileProgressDialog.setOwner(primaryStage);
		FormatterOptionDialog.setOwner(primaryStage);
		AboutDialog.setOwner(primaryStage);
		FeaturePointRestrictionDialog.setOwner(primaryStage);
		VectorAngleRestrictionDialog.setOwner(primaryStage);
		TrigonometricRestrictionDialog.setOwner(primaryStage);
		QuantilesDialog.setOwner(primaryStage);
		VarianceComponentsDialog.setOwner(primaryStage);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		if (JUniForm.primaryStage != primaryStage) {
			I18N i18n = I18N.getInstance();

			JUniForm.primaryStage = primaryStage;

			UITabPaneBuilder tabPaneBuilder = UITabPaneBuilder.getInstance();
			UIMenuBuilder menuBuilder = UIMenuBuilder.getInstance();
			UITreeBuilder treeBuilder = UITreeBuilder.getInstance();

			this.adjustmentButton = new Button(i18n.getString("JUniForm.button.adjust.label", "Adjust feature"));
			this.adjustmentButton.setTooltip(new Tooltip(i18n.getString("JUniForm.button.adjust.tooltip", "Start feature adjustment process")));
			this.adjustmentButton.setOnAction(new EventHandler<ActionEvent>() { 
				@Override
				public void handle(ActionEvent event) {	   
					FeatureAdjustmentDialog.show();
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
			applicationName.setText("Java\u00B7Unified\u00B7Form\u00B7Fitting");
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
			
			scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				final KeyCombination adjustKeyComb  = new KeyCodeCombination(KeyCode.F5);
				
				final KeyCombination tabOneKeyComb   = new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabTwoKeyComb   = new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabThreeKeyComb = new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabFourKeyComb  = new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabFiveKeyComb  = new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabSixKeyComb   = new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabSevenKeyComb = new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabEightKeyComb = new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabNineKeyComb  = new KeyCodeCombination(KeyCode.DIGIT9, KeyCombination.SHORTCUT_DOWN);
				
				final KeyCombination tabOneKeyCombNum   = new KeyCodeCombination(KeyCode.NUMPAD1, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabTwoKeyCombNum   = new KeyCodeCombination(KeyCode.NUMPAD2, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabThreeKeyCombNum = new KeyCodeCombination(KeyCode.NUMPAD3, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabFourKeyCombNum  = new KeyCodeCombination(KeyCode.NUMPAD4, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabFiveKeyCombNum  = new KeyCodeCombination(KeyCode.NUMPAD5, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabSixKeyCombNum   = new KeyCodeCombination(KeyCode.NUMPAD6, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabSevenKeyCombNum = new KeyCodeCombination(KeyCode.NUMPAD7, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabEightKeyCombNum = new KeyCodeCombination(KeyCode.NUMPAD8, KeyCombination.SHORTCUT_DOWN);
				final KeyCombination tabNineKeyCombNum  = new KeyCodeCombination(KeyCode.NUMPAD9, KeyCombination.SHORTCUT_DOWN);
				
			    public void handle(KeyEvent keyEvent) {
			        if (adjustKeyComb.match(keyEvent) && !adjustmentButton.isDisabled()) {
			        	adjustmentButton.fire();
			        	keyEvent.consume();
			        }
			        else if (tabPane.getSelectionModel().getSelectedItem() != null) {
			        	if (tabOneKeyComb.match(keyEvent) || tabTwoKeyComb.match(keyEvent) || tabThreeKeyComb.match(keyEvent) || tabFourKeyComb.match(keyEvent) || 
			        			tabFiveKeyComb.match(keyEvent) || tabSixKeyComb.match(keyEvent) || tabSevenKeyComb.match(keyEvent) || tabEightKeyComb.match(keyEvent) || tabNineKeyComb.match(keyEvent) ||
			        			tabOneKeyCombNum.match(keyEvent) || tabTwoKeyCombNum.match(keyEvent) || tabThreeKeyCombNum.match(keyEvent) || tabFourKeyCombNum.match(keyEvent) || 
			        			tabFiveKeyCombNum.match(keyEvent) || tabSixKeyCombNum.match(keyEvent) || tabSevenKeyCombNum.match(keyEvent) || tabEightKeyCombNum.match(keyEvent) || tabNineKeyCombNum.match(keyEvent)) {
			        		
			        		int size = tabPane.getTabs().size();
			        		
			        		if ((tabOneKeyComb.match(keyEvent) || tabOneKeyCombNum.match(keyEvent)) && size > 0)
			        			tabPane.getSelectionModel().clearAndSelect(0);
			        		else if ((tabTwoKeyComb.match(keyEvent) || tabTwoKeyCombNum.match(keyEvent))&& size > 1)
			        			tabPane.getSelectionModel().clearAndSelect(1);
			        		else if ((tabThreeKeyComb.match(keyEvent) || tabThreeKeyCombNum.match(keyEvent)) && size > 2)
			        			tabPane.getSelectionModel().clearAndSelect(2);
			        		else if ((tabFourKeyComb.match(keyEvent) || tabFourKeyCombNum.match(keyEvent)) && size > 3)
			        			tabPane.getSelectionModel().clearAndSelect(3);
			        		else if ((tabFiveKeyComb.match(keyEvent) || tabFiveKeyCombNum.match(keyEvent)) && size > 4)
			        			tabPane.getSelectionModel().clearAndSelect(4);
			        		else if ((tabSixKeyComb.match(keyEvent) || tabSixKeyCombNum.match(keyEvent)) && size > 5)
			        			tabPane.getSelectionModel().clearAndSelect(5);
			        		else if ((tabSevenKeyComb.match(keyEvent) || tabSevenKeyCombNum.match(keyEvent)) && size > 6)
			        			tabPane.getSelectionModel().clearAndSelect(6);
			        		else if ((tabEightKeyComb.match(keyEvent) || tabEightKeyCombNum.match(keyEvent)) && size > 7)
			        			tabPane.getSelectionModel().clearAndSelect(7);
			        		else if ((tabNineKeyComb.match(keyEvent) || tabNineKeyCombNum.match(keyEvent)) && size > 8)
			        			tabPane.getSelectionModel().clearAndSelect(8);
			        		keyEvent.consume();
			        	}
			        }
			    }
			});
			
			// add external style definitions
			try {
				URL cssURL = null;
				if ((cssURL = JAG3D.class.getClassLoader().getResource("css/")) != null) {
					cssURL = cssURL.toURI().resolve("juniform.css").toURL();
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
						ImageUtils.getImage("JUniForm_16x16.png"),
						ImageUtils.getImage("JUniForm_32x32.png"),
						ImageUtils.getImage("JUniForm_64x64.png")
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
			FeatureAdjustment adjustment = treeBuilder.getFeatureAdjustment();
			adjustment.addFeatureChangeListener(treeBuilder);
			adjustment.addFeatureChangeListener(UIMenuBuilder.getInstance());
			adjustment.addFeatureChangeListener(UIParameterTableBuilder.getInstance());
			adjustment.addFeatureChangeListener(UIPointTableBuilder.getInstance());
			adjustment.addFeatureChangeListener(new AdjustmentFeatureChangedListener());
		}
		else {
			primaryStage.show();
			primaryStage.toFront();
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
		Application.launch(JUniForm.class, args);
	}
}
