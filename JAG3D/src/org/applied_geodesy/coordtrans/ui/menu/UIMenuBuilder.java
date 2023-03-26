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

package org.applied_geodesy.coordtrans.ui.menu;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.applied_geodesy.adjustment.transformation.AffinTransformation;
import org.applied_geodesy.adjustment.transformation.Transformation;
import org.applied_geodesy.adjustment.transformation.TransformationChangeListener;
import org.applied_geodesy.adjustment.transformation.TransformationEvent;
import org.applied_geodesy.adjustment.transformation.TransformationEvent.TransformationEventType;
import org.applied_geodesy.adjustment.transformation.TransformationType;
import org.applied_geodesy.adjustment.transformation.point.FramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.HomologousFramePositionPair;
import org.applied_geodesy.adjustment.transformation.point.ObservedFramePosition;
import org.applied_geodesy.coordtrans.ui.CoordTrans;
import org.applied_geodesy.coordtrans.ui.dialog.FilePathsSelectionDialog;
import org.applied_geodesy.coordtrans.ui.dialog.FilePathsSelectionDialog.FilePathPair;
import org.applied_geodesy.coordtrans.ui.dialog.ReadFileProgressDialog;
import org.applied_geodesy.coordtrans.ui.i18n.I18N;
import org.applied_geodesy.coordtrans.ui.io.PositionFileReader;
import org.applied_geodesy.coordtrans.ui.table.UIFramePositionPairTableBuilder;
import org.applied_geodesy.coordtrans.ui.table.UIHomologousFramePositionPairTableBuilder;
import org.applied_geodesy.coordtrans.ui.tree.UITreeBuilder;
import org.applied_geodesy.util.ObservableUniqueList;

import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class UIMenuBuilder implements TransformationChangeListener {

	private static UIMenuBuilder menuBuilder = new UIMenuBuilder();
	private I18N i18n = I18N.getInstance();
	private MenuEventHandler menuEventHandler = new MenuEventHandler(this);
	private MenuBar menuBar;
	private UIMenuBuilder() {}
	private Menu reportMenu;

	public static UIMenuBuilder getInstance() {
		return menuBuilder;
	}
	
	public MenuBar getMenuBar() {
		if (this.menuBar == null)
			this.init();
		return this.menuBar;
	}
	
	private void init() {
		this.menuBar = new MenuBar();
		Menu fileMenu       = createMenu(i18n.getString("UIMenuBuilder.menu.file.label", "_File"),  true);
		
		Menu propertiesMenu = createMenu(i18n.getString("UIMenuBuilder.menu.properties.label", "Propert_ies"), true);
		Menu adjustmentMenu = createMenu(i18n.getString("UIMenuBuilder.menu.adjustment.label", "Ad_justment"), true);
		this.reportMenu     = createMenu(i18n.getString("UIMenuBuilder.menu.report.label", "Repor_t"), true);
		Menu helpMenu       = createMenu(i18n.getString("UIMenuBuilder.menu.help.label", "_?"), true);
		
		this.createFileMenu(fileMenu);
		this.createPropertiesMenu(propertiesMenu);
		this.createAdjustmentMenu(adjustmentMenu);
		this.createReportMenu(this.reportMenu);
		this.createHelpMenu(helpMenu);
		
		this.menuBar.getMenus().addAll(
				fileMenu,
				propertiesMenu,
				adjustmentMenu,
				reportMenu,
				helpMenu
		);
	}
	
	private void createFileMenu(Menu parentMenu) {
		MenuItem importPositionsItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.file.import.positions.label",  "Imp_ort data"), true, MenuItemType.IMPORT_POSITIONS, new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		MenuItem exitItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.file.exit.label", "_Exit"), true, MenuItemType.EXIT, new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		
		parentMenu.getItems().addAll(
				importPositionsItem,
				new SeparatorMenuItem(),
				exitItem
		);
	}
	
	private void createAdjustmentMenu(Menu parentMenu) {
		MenuItem leastSquaresItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.adjustment.leastsquares.label", "_Least-squares"), true, MenuItemType.LEAST_SQUARES, new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem teststatisticItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.adjustment.teststatistic.label", "Test st_atistic"), true, MenuItemType.TEST_STATISTIC, new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
		MenuItem preferencesItem   = createMenuItem(i18n.getString("UIMenuBuilder.menu.adjustment.preferences.label", "Preferen_ces"), true, MenuItemType.PREFERENCES, new KeyCodeCombination(KeyCode.ENTER, KeyCombination.ALT_DOWN), this.menuEventHandler, false);
		parentMenu.getItems().addAll(
				leastSquaresItem,
				teststatisticItem,
				new SeparatorMenuItem(),
				preferencesItem
		);
	}
	
	private void createPropertiesMenu(Menu parentMenu) {
//
//		MenuItem featurePropertiesItem        = createMenuItem(i18n.getString("UIMenuBuilder.menu.setting.feature.label", "_Feature"), true, MenuItemType.FEATURE_PROPERTIES, new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
//		MenuItem paramameterPropertiesItem    = createMenuItem(i18n.getString("UIMenuBuilder.menu.setting.parameter.label", "P_arameter"), true, MenuItemType.PARAMETER_PROPERTIES, new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
//		MenuItem restrictionPropertiesItem    = createMenuItem(i18n.getString("UIMenuBuilder.menu.setting.restriction.label", "_Restriction"), true, MenuItemType.RESTRICTION_PROPERTIES, new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
//		MenuItem postprocessingPropertiesItem = createMenuItem(i18n.getString("UIMenuBuilder.menu.setting.postprocessing.label", "_Post processing"), true, MenuItemType.POSTPROCESSING_PROPERTIES, new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, true);
//			
//		parentMenu.getItems().addAll(
//				featurePropertiesItem,
//				paramameterPropertiesItem,
//				restrictionPropertiesItem,
//				postprocessingPropertiesItem
//		);
	}
	
	private void createReportMenu(Menu parentMenu) {
//		List<File> templateFiles = FTLReport.getTemplates();
//		if (templateFiles != null && !templateFiles.isEmpty()) {
//			for (File templateFile : templateFiles) {
//				MenuItem templateFileItem = createMenuItem(templateFile.getName(), false, MenuItemType.REPORT, templateFile, null, this.menuEventHandler, true);
//
//				parentMenu.getItems().add(templateFileItem);
//			}
//		}
	}
	
	private void createHelpMenu(Menu parentMenu) {
		MenuItem aboutItem  = createMenuItem(i18n.getString("UIMenuBuilder.menu.help.about.label", "A_bout CoordTrans"), true, MenuItemType.ABOUT, new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN), this.menuEventHandler, false);
		parentMenu.getItems().addAll(
				aboutItem
		);
	}
	
	private static Menu createMenu(String label, boolean mnemonicParsing) {
		Menu menu = new Menu(label);
		menu.setMnemonicParsing(mnemonicParsing);		
		return menu;
	}
	
	private static MenuItem createMenuItem(MenuItem menuItem, boolean mnemonicParsing, Object userDate, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		menuItem.setMnemonicParsing(mnemonicParsing);
		if (keyCodeCombination != null)
			menuItem.setAccelerator(keyCodeCombination);
		menuItem.setOnAction(menuEventHandler);
		menuItem.setDisable(disable);
		menuItem.setUserData(userDate);
		return menuItem;
	}

	private static MenuItem createMenuItem(String label, boolean mnemonicParsing, MenuItemType menuItemType, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		MenuItem menuItem = createMenuItem(new MenuItem(label), mnemonicParsing, menuItemType, keyCodeCombination, menuEventHandler, disable);

		return menuItem;
	}
	
	private static RadioMenuItem createRadioMenuItem(String label, boolean mnemonicParsing, MenuItemType menuItemType, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		RadioMenuItem menuItem = (RadioMenuItem)createMenuItem(new RadioMenuItem(label), mnemonicParsing, menuItemType, keyCodeCombination, menuEventHandler, disable);

		return menuItem;
	}

	private static MenuItem createMenuItem(String label, boolean mnemonicParsing, MenuItemType menuItemType, File file, KeyCodeCombination keyCodeCombination, MenuEventHandler menuEventHandler, boolean disable) {
		FileMenuItem menuItem = (FileMenuItem)createMenuItem(new FileMenuItem(label), mnemonicParsing, menuItemType, keyCodeCombination, menuEventHandler, disable);
		menuItem.setFile(file);
		return menuItem;
	}
	
	
	void importPositions() {
		FilePathPair filePathPair = null;
		Optional<FilePathPair> optional = FilePathsSelectionDialog.showAndWait();
		
		if (optional.isPresent())
			filePathPair = optional.get();
		
		if (filePathPair != null) 
			this.importPositions(filePathPair);
	}
	
	private void importPositions(FilePathPair filePathPair) {
		TransformationType transformationType = filePathPair.getTransformationType();
		PositionFileReader fileReader = new PositionFileReader(transformationType);
		ReadFileProgressDialog<Map<String, ObservedFramePosition>> dialog = new ReadFileProgressDialog<Map<String, ObservedFramePosition>>();
		Optional<List<Map<String, ObservedFramePosition>>> optional = dialog.showAndWait(
				fileReader, 
				List.of(filePathPair.getSourceFilePath().toFile(), filePathPair.getTargetFilePath().toFile()),
				Boolean.TRUE
				);
		
		List<Map<String, ObservedFramePosition>> positions = null;
		
		if (optional.isPresent()) {
			positions = optional.get();
		}

		if (positions == null || positions.size() < 2)
			return;
		
		Map<String, ObservedFramePosition> sourceSystemPositions = positions.get(0);
		Map<String, ObservedFramePosition> targetSystemPositions = positions.get(1); 
		
		if (sourceSystemPositions == null || sourceSystemPositions.isEmpty())
			return;
		
		ObservableUniqueList<HomologousFramePositionPair> homologousFramePositionPairs = new ObservableUniqueList<HomologousFramePositionPair>(Math.min(sourceSystemPositions.size(), targetSystemPositions.size()));
		ObservableUniqueList<FramePositionPair> framePositionPairs = new ObservableUniqueList<FramePositionPair>(sourceSystemPositions.size());
		
		for (Map.Entry<String, ObservedFramePosition> point : sourceSystemPositions.entrySet()) {
			String name = point.getKey();
			ObservedFramePosition sourcePosition = point.getValue();
			
			framePositionPairs.add(new FramePositionPair(name, sourcePosition));
			
			if (targetSystemPositions.containsKey(name)) {
				ObservedFramePosition targetPosition = targetSystemPositions.get(name);

				switch (transformationType) {
				case HEIGHT:
					homologousFramePositionPairs.add(
							new HomologousFramePositionPair(
									name, 
									
									sourcePosition.getZ(), 
									sourcePosition.getDispersionApriori(), 
									
									targetPosition.getZ(), 
									targetPosition.getDispersionApriori())
							);
					break;
				case PLANAR:
					homologousFramePositionPairs.add(
							new HomologousFramePositionPair(
									name, 
									
									sourcePosition.getX(), 
									sourcePosition.getY(), 
									sourcePosition.getDispersionApriori(), 
									
									targetPosition.getX(), 
									targetPosition.getY(), 
									targetPosition.getDispersionApriori())
							);
					break;
				case SPATIAL:
					homologousFramePositionPairs.add(
							new HomologousFramePositionPair(
									name, 
									
									sourcePosition.getX(), 
									sourcePosition.getY(), 
									sourcePosition.getZ(), 
									sourcePosition.getDispersionApriori(), 
									
									targetPosition.getX(), 
									targetPosition.getY(), 
									targetPosition.getZ(), 
									targetPosition.getDispersionApriori())
							);
					break;			
				}
			}
		}
		UIHomologousFramePositionPairTableBuilder homologousFramePositionPairTableBuilder = UIHomologousFramePositionPairTableBuilder.getInstance();
		homologousFramePositionPairTableBuilder.setTransformationType(transformationType);
		TableView<HomologousFramePositionPair> homologousPositionPairTable = homologousFramePositionPairTableBuilder.getTable();
		ObservableList<HomologousFramePositionPair> homologousPositionPairTableModel = homologousFramePositionPairTableBuilder.getTableModel(homologousPositionPairTable);
		homologousPositionPairTableModel.setAll(homologousFramePositionPairs);

		UIFramePositionPairTableBuilder framePositionPairTableBuilder = UIFramePositionPairTableBuilder.getInstance();
		framePositionPairTableBuilder.setTransformationType(transformationType);
		TableView<FramePositionPair> positionPairTable = framePositionPairTableBuilder.getTable();
		ObservableList<FramePositionPair> positionPairTableModel = framePositionPairTableBuilder.getTableModel(positionPairTable);
		positionPairTableModel.setAll(framePositionPairs);
		
				
		Transformation transformation = null;
		switch(transformationType) {
		case HEIGHT:
			CoordTrans.setTitle(i18n.getString("CoordTrans.transformation.type.height.title", "Height transformation"));
			break;
		case PLANAR:
			CoordTrans.setTitle(i18n.getString("CoordTrans.transformation.type.planar.title", "Planar transformation"));
			break;
		case SPATIAL:
			transformation = new AffinTransformation();
			CoordTrans.setTitle(i18n.getString("CoordTrans.transformation.type.spatial.title", "Spatial transformation"));
			break;
		default:
			break;
		
		}
		
		this.fireTransformationChanged(transformation);
		
		if (transformation == null)
			CoordTrans.setTitle(null);
	}
	
	void createReport(File templateFile) {
//		try {
//			if (UITreeBuilder.getInstance().getFeatureAdjustment().getFeature() == null)
//				return;
//
//			Pattern pattern = Pattern.compile(".*?\\.(\\w+)\\.ftlh$", Pattern.CASE_INSENSITIVE);
//			Matcher matcher = pattern.matcher(templateFile.getName().toLowerCase());
//			String extension = "html";
//			ExtensionFilter extensionFilter = new ExtensionFilter(i18n.getString("UIMenuBuilder.report.extension.html", "Hypertext Markup Language"), "*.html", "*.htm", "*.HTML", "*.HTM");
//			if (matcher.find() && matcher.groupCount() == 1) {
//				extension = matcher.group(1);
//				extensionFilter = new ExtensionFilter(String.format(Locale.ENGLISH, i18n.getString("UIMenuBuilder.report.extension.template", "%s-File"), extension), "*." + extension); 
//			}
//			
//			String fileNameSuggestion = "report." + extension;
//
//			FTLReport ftl = new FTLReport(UITreeBuilder.getInstance().getFeatureAdjustment());
//			File reportFile = DefaultFileChooser.showSaveDialog(
//					JUniForm.getStage(),
//					i18n.getString("UIMenuBuilder.filechooser.report.title", "Save adjustment report"), 
//					fileNameSuggestion,
//					extensionFilter
//			);
//			if (reportFile != null && ftl != null) {
//				ftl.setTemplate(templateFile.getName());
//				ftl.toFile(reportFile);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			OptionDialog.showThrowableDialog (
//					i18n.getString("UIMenuBuilder.message.error.report.exception.title", "I/O Error"),
//					i18n.getString("UIMenuBuilder.message.error.report.exception.header", "Error, could not create adjustment report."),
//					i18n.getString("UIMenuBuilder.message.error.report.exception.message", "An exception has occurred during report creation."),
//					e
//					);
//		}
	}
	
	public void setReportMenuDisable(boolean disable) {
		for (MenuItem item : this.reportMenu.getItems())
			item.setDisable(disable);
	}
	
	private void fireTransformationChanged(Transformation transformation) {
//		this.setTransformation(transformation);
		UITreeBuilder treeBuilder = UITreeBuilder.getInstance();
		treeBuilder.getTransformationAdjustment().setTransformation(transformation);
		treeBuilder.handleTreeSelections();
	}
	
	void disableMenu(boolean disable) {
		List<Menu> menus = this.menuBar.getMenus();
		for (Menu menu : menus)
			this.disableMenu(menu, disable);
	}
	
	private void disableMenu(Menu menu, boolean disable) {
		List<MenuItem> items = menu.getItems();
		for (MenuItem item : items) {
			if (item instanceof Menu)
				this.disableMenu((Menu)item, disable);
			else if (item.getUserData() != null && item.getUserData() instanceof MenuItemType) { 
				MenuItemType itemType = (MenuItemType)item.getUserData();
				switch(itemType) {

				case TEST_STATISTIC:
				case LEAST_SQUARES:
					item.setDisable(disable);
					break;
					
				case REPORT:
					this.setReportMenuDisable(true);
					break;
					
				case EXIT:
				case ABOUT:
				case PREFERENCES:
				case IMPORT_POSITIONS:
					// do nothing
					break;
				}
			}
		}
	}

	@Override
	public void transformationChanged(TransformationEvent evt) {
		this.disableMenu(evt.getEventType() != TransformationEventType.TRANSFORMATION_MODEL_ADDED);
	}
}
